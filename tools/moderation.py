"""신고 접수함과 처리 기록 (운영 절차 도구).

이용약관 7항의 약속을 지키기 위한 도구예요.
  - 신고 접수 후 3일 이내(위험이 큰 내용은 2일 이내) 확인을 시작한다.
  - 늦어도 접수일로부터 1개월 이내에 처리 결과를 결정한다.
  - 이용자 신고 내역은 접수일로부터 3개월 보관한 뒤 삭제한다.

하위 명령 (모두 --key, --db-url 이 필요하고, 바꾸는 명령은 --apply 가 있어야 실제로 실행돼요):
  inbox        처리하지 않은 신고를 급한 순서로 보여준다 (읽기 전용)
  resolve REF  신고 한 건을 처리하고 기록을 남긴다 (--action, --note 필요)
  unsuspend    이용 제한(계정 사용 중지)을 푼다
  purge        3개월이 지난 이용자 신고와 처리 기록을 지운다

REF 는 inbox 가 보여주는 경로예요: userReports/<id>, post/<id>, post/<id>/comments/<id>
핵심 계산 함수는 Firebase 없이 test_moderation.py 로 검증한다.
"""
import argparse
import sys
import time
from pathlib import Path

DAY_MS = 24 * 60 * 60 * 1000
FIRST_CHECK_DAYS = 3      # 확인 시작 기한
PRIORITY_CHECK_DAYS = 2   # 위험이 큰 내용의 확인 기한
MAX_DECISION_DAYS = 30    # 처리 결정 상한
RETENTION_DAYS = 90       # 신고 내역/처리 기록 보관 기간

PRIORITY_REASONS = {"minor", "sexual"}
REASON_LABEL = {
    "harassment": "욕설·모욕·괴롭힘·협박", "sexual": "성적인 내용", "minor": "미성년자 보호 위반",
    "spam": "스팸·광고", "impersonation": "사칭", "other": "기타",
}
SOURCE_LABEL = {"chat": "채팅", "post": "게시글"}
LEVEL_LABEL = {"overdue": "결정 기한 지남", "due": "확인 기한 지남", "ok": "기한 내", "undated": "날짜 없음(직접 판단)"}
LEVEL_ORDER = {"overdue": 0, "due": 1, "ok": 2, "undated": 3}
ACTIONS = ("none", "warn", "delete-content", "suspend")


# ---------------------------------------------------------------- 계산 (Firebase 없이 테스트)

def age_days(date_ms, now_ms: int) -> int:
    return int((now_ms - date_ms) // DAY_MS)


def level_of(age: int, priority: bool) -> str:
    """overdue(30일 지남) / due(확인 기한 지남) / ok"""
    if age >= MAX_DECISION_DAYS:
        return "overdue"
    return "due" if age >= (PRIORITY_CHECK_DAYS if priority else FIRST_CHECK_DAYS) else "ok"


def short(uid) -> str:
    return (uid or "?")[:6] + "…"


def display_name(users: dict, uid) -> str:
    node = (users or {}).get(uid) if uid else None
    if isinstance(node, dict):
        if node.get("withdrawn"):
            return f"탈퇴한 사용자({short(uid)})"
        if node.get("name"):
            return f"{node['name']}({short(uid)})"
    return f"알 수 없음({short(uid)})"


def _valid_date(value) -> bool:
    return isinstance(value, (int, float)) and value > 0


def reports_by_others(node: dict, author) -> list:
    reports = (node or {}).get("report") or {}
    return [r for r in reports.values() if isinstance(r, dict) and r.get("uid") != author]


def build_inbox(user_reports: dict, posts: dict, users: dict, handled_refs: set, now_ms: int) -> list:
    """처리하지 않은 신고(이용자 신고 + 신고된 글/댓글)를 급한 순서로 돌려준다."""
    items = []
    for rid, r in (user_reports or {}).items():
        ref = f"userReports/{rid}"
        if not isinstance(r, dict) or ref in handled_refs or r.get("status") == "handled":
            continue
        priority = r.get("reason") in PRIORITY_REASONS
        date = r.get("date")
        age = age_days(date, now_ms) if _valid_date(date) else None
        items.append({
            "ref": ref, "kind": "이용자 신고", "priority": priority, "age": age,
            "level": level_of(age, priority) if age is not None else "undated",
            "summary": f"{SOURCE_LABEL.get(r.get('source'), r.get('source'))} / {REASON_LABEL.get(r.get('reason'), r.get('reason'))}",
            "target": display_name(users, r.get("target")), "context": r.get("context", ""),
        })
    for pid, post in (posts or {}).items():
        if not isinstance(post, dict):
            continue
        pairs = [(f"post/{pid}", "신고된 글", post, str(post.get("title", ""))[:30])]
        for cid, c in (post.get("comments") or {}).items():
            if isinstance(c, dict):
                pairs.append((f"post/{pid}/comments/{cid}", "신고된 댓글", c, str(c.get("content", ""))[:30]))
        for ref, kind, node, snippet in pairs:
            reports = reports_by_others(node, node.get("author"))
            if not reports or ref in handled_refs:
                continue
            dates = [r.get("date") for r in reports if _valid_date(r.get("date"))]
            age = age_days(min(dates), now_ms) if dates else None
            items.append({
                "ref": ref, "kind": kind, "priority": False, "age": age,
                "level": level_of(age, False) if age is not None else "undated",
                "summary": f"신고 {len(reports)}건 / {snippet!r}",
                "target": display_name(users, node.get("author")), "context": "",
            })
    return sorted(items, key=lambda i: (LEVEL_ORDER[i["level"]], -(i["age"] or 0)))


def expired_entries(entries: dict, field: str, now_ms: int) -> list:
    """field(밀리초 시각)가 보관 기간(90일)을 넘은 항목의 키 목록. 시각이 없으면 지우지 않는다."""
    out = []
    for key, node in (entries or {}).items():
        if isinstance(node, dict) and _valid_date(node.get(field)) and now_ms - node[field] > RETENTION_DAYS * DAY_MS:
            out.append(key)
    return out


def target_uid_of(ref: str, node: dict):
    return node.get("target") if ref.startswith("userReports/") else node.get("author")


def validate_ref(ref: str) -> None:
    parts = ref.split("/")
    ok = (len(parts) == 2 and parts[0] in ("userReports", "post")) or \
         (len(parts) == 4 and parts[0] == "post" and parts[2] == "comments")
    if not ok or not all(parts):
        raise ValueError("REF 는 userReports/<id>, post/<id>, post/<id>/comments/<id> 중 하나여야 해요")


def check_action(ref: str, action: str) -> None:
    if action == "delete-content" and ref.startswith("userReports/"):
        raise ValueError("delete-content 는 글/댓글(post/...) REF 에만 쓸 수 있어요. 이용자 신고는 none, warn, suspend 를 쓰세요")
    if action == "suspend" and not ref:
        raise ValueError("suspend 에는 REF 가 필요해요")


# ---------------------------------------------------------------- 실행 (Firebase 필요)

def abort_if_key_in_repo(key_path: Path) -> None:
    for parent in [key_path.resolve().parent, *key_path.resolve().parents]:
        if (parent / ".git").exists():
            sys.exit(f"중단: 키 파일이 git 저장소({parent}) 안에 있어요. 저장소 밖으로 옮기세요.")


def init_firebase(args) -> None:
    import firebase_admin
    from firebase_admin import credentials
    abort_if_key_in_repo(args.key)
    firebase_admin.initialize_app(credentials.Certificate(str(args.key)), {"databaseURL": args.db_url})


def handled_refs_from(log: dict) -> set:
    return {n.get("ref") for n in (log or {}).values() if isinstance(n, dict) and n.get("ref")}


def cmd_inbox(args) -> None:
    from firebase_admin import db
    now_ms = int(time.time() * 1000)
    items = build_inbox(db.reference("userReports").get(), db.reference("post").get(), db.reference("user").get(),
                        handled_refs_from(db.reference("moderationLog").get()), now_ms)
    print(f"처리하지 않은 신고: {len(items)}건 "
          f"(확인 {FIRST_CHECK_DAYS}일, 위험이 큰 내용 {PRIORITY_CHECK_DAYS}일, 결정 {MAX_DECISION_DAYS}일 이내)")
    for i in items:
        age = "?" if i["age"] is None else f"{i['age']}일"
        flag = " [우선]" if i["priority"] else ""
        ctx = f" / 맥락 {i['context']}" if i["context"] else ""
        print(f"  [{LEVEL_LABEL[i['level']]}]{flag} 접수 {age} 경과 / {i['kind']} / {i['summary']} / 대상 {i['target']}{ctx}")
        print(f"      REF {i['ref']}")


def cmd_resolve(args) -> None:
    from firebase_admin import auth, db
    validate_ref(args.ref)
    check_action(args.ref, args.action)
    node = db.reference(args.ref).get()
    if not isinstance(node, dict):
        sys.exit(f"중단: {args.ref} 를 찾지 못했어요 (이미 삭제됐거나 REF 가 틀렸어요)")
    target = target_uid_of(args.ref, node)
    print(f"처리 대상: {args.ref} / 대상 사용자 {short(target)} / 조치 {args.action} / 메모 {args.note!r}")
    if args.action == "suspend" and not target:
        sys.exit("중단: 이용 제한할 사용자를 알 수 없어요")
    if not args.apply:
        print("(dry-run) --apply 를 붙이면 실제로 처리하고 기록을 남겨요")
        return
    if args.action == "delete-content":
        db.reference(args.ref).delete()
        print("글/댓글을 삭제했어요")
    elif args.action == "suspend":
        auth.update_user(target, disabled=True)
        auth.revoke_refresh_tokens(target)
        print("계정을 사용 중지하고 로그인 정보를 만료시켰어요 (이미 열린 앱은 최대 1시간 안에 로그아웃돼요)")
    now_ms = int(time.time() * 1000)
    db.reference("moderationLog").push({"ref": args.ref, "action": args.action, "note": args.note,
                                        "target": target, "at": now_ms})
    if args.ref.startswith("userReports/"):
        db.reference(args.ref).update({"status": "handled", "handledAt": now_ms})
    print("처리 기록을 남겼어요")


def cmd_unsuspend(args) -> None:
    from firebase_admin import auth, db
    print(f"이용 제한 해제 대상: {short(args.uid)} / 메모 {args.note!r}")
    if not args.apply:
        print("(dry-run) --apply 를 붙이면 실제로 해제해요")
        return
    auth.update_user(args.uid, disabled=False)
    db.reference("moderationLog").push({"ref": f"user/{args.uid}", "action": "unsuspend", "note": args.note,
                                        "target": args.uid, "at": int(time.time() * 1000)})
    print("해제하고 기록을 남겼어요")


def cmd_purge(args) -> None:
    from firebase_admin import db
    now_ms = int(time.time() * 1000)
    reports = expired_entries(db.reference("userReports").get(), "date", now_ms)
    logs = expired_entries(db.reference("moderationLog").get(), "at", now_ms)
    print(f"보관 {RETENTION_DAYS}일이 지난 이용자 신고 {len(reports)}건, 처리 기록 {len(logs)}건")
    if not args.apply:
        print("(dry-run) --apply 를 붙이면 삭제해요")
        return
    for key in reports:
        db.reference(f"userReports/{key}").delete()
    for key in logs:
        db.reference(f"moderationLog/{key}").delete()
    print("삭제했어요")


def main() -> None:
    common = argparse.ArgumentParser(add_help=False)
    common.add_argument("--key", required=True, type=Path, help="서비스 계정 키(JSON) 경로 (저장소 밖에 둘 것)")
    common.add_argument("--db-url", required=True, help="Realtime Database URL")
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="cmd", required=True)

    sub.add_parser("inbox", parents=[common], help="처리하지 않은 신고 목록").set_defaults(fn=cmd_inbox)

    p = sub.add_parser("resolve", parents=[common], help="신고 한 건 처리와 기록")
    p.add_argument("ref")
    p.add_argument("--action", required=True, choices=ACTIONS)
    p.add_argument("--note", required=True, help="처리 내용 메모 (개인정보는 적지 마세요)")
    p.add_argument("--apply", action="store_true")
    p.set_defaults(fn=cmd_resolve)

    p = sub.add_parser("unsuspend", parents=[common], help="이용 제한 해제")
    p.add_argument("uid")
    p.add_argument("--note", required=True)
    p.add_argument("--apply", action="store_true")
    p.set_defaults(fn=cmd_unsuspend)

    p = sub.add_parser("purge", parents=[common], help="보관 기간이 지난 신고와 기록 삭제")
    p.add_argument("--apply", action="store_true")
    p.set_defaults(fn=cmd_purge)

    args = parser.parse_args()
    init_firebase(args)
    args.fn(args)


if __name__ == "__main__":
    main()
