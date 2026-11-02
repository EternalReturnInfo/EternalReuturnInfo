"""신고가 접수된 게시글·댓글을 한눈에 보고, 탈퇴한 작성자의 보관 기간(3개월)이 끝난 것을 지우는 도구.

  - 기본: 신고가 있는 글/댓글 목록을 보여준다 (읽기 전용). 내용은 앞 30자만 보여준다.
  - 작성자가 탈퇴했고 첫 신고로부터 90일이 지난 것은 "보관 만료"로 표시한다.
  - --delete-expired --apply : 보관 만료된 것만 삭제한다. (--apply 가 없으면 삭제 대상만 보여준다)

앱의 회원탈퇴는 다른 사람의 신고가 접수된 글/댓글을 지우지 않고 남겨둔다 (AccountDeleter).
신고에 시각(date)이 없는 옛 신고는 기간을 셀 수 없으니 "날짜 없음"으로 표시하고 직접 판단한다.
작성자가 탈퇴하지 않은 글은 건드리지 않는다. 사용법은 tools/README.md 참고.
"""
import argparse
import sys
import time
from pathlib import Path

import firebase_admin
from firebase_admin import credentials, db

from purge_passwords import abort_if_key_in_repo

RETENTION_DAYS = 90
DAY_MS = 24 * 60 * 60 * 1000


def reports_by_others(node: dict, author: str) -> list[dict]:
    reports = (node or {}).get("report") or {}
    return [r for r in reports.values() if isinstance(r, dict) and r.get("uid") != author]


def classify(reports: list[dict], author_withdrawn: bool, now_ms: int) -> str:
    """'active'(작성자 활동 중) / 'held'(탈퇴, 보관 중) / 'expired'(보관 만료) / 'undated'(탈퇴, 날짜 없는 옛 신고)"""
    if not author_withdrawn:
        return "active"
    dates = [r.get("date") for r in reports if isinstance(r.get("date"), (int, float)) and r.get("date") > 0]
    if not dates:
        return "undated"
    return "expired" if now_ms - min(dates) > RETENTION_DAYS * DAY_MS else "held"


def collect(posts: dict, users: dict, now_ms: int) -> list[dict]:
    """신고가 접수된(다른 사람 기준) 글/댓글 목록."""
    withdrawn = {uid for uid, node in (users or {}).items() if isinstance(node, dict) and node.get("withdrawn")}
    found = []
    for post_id, post in (posts or {}).items():
        if not isinstance(post, dict):
            continue
        author = post.get("author")
        reports = reports_by_others(post, author)
        if reports:
            found.append({"kind": "글", "path": f"post/{post_id}", "author": author, "reports": len(reports),
                          "status": classify(reports, author in withdrawn, now_ms),
                          "snippet": str(post.get("title", ""))[:30]})
        for comment_id, comment in (post.get("comments") or {}).items():
            if not isinstance(comment, dict):
                continue
            c_author = comment.get("author")
            c_reports = reports_by_others(comment, c_author)
            if c_reports:
                found.append({"kind": "댓글", "path": f"post/{post_id}/comments/{comment_id}", "author": c_author,
                              "reports": len(c_reports), "status": classify(c_reports, c_author in withdrawn, now_ms),
                              "snippet": str(comment.get("content", ""))[:30]})
    return found


LABEL = {"active": "작성자 활동 중", "held": "탈퇴, 보관 중", "expired": "보관 만료", "undated": "탈퇴, 날짜 없음(직접 판단)"}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--key", required=True, type=Path, help="서비스 계정 키(JSON) 경로 (저장소 밖에 둘 것)")
    parser.add_argument("--db-url", required=True, help="Realtime Database URL")
    parser.add_argument("--delete-expired", action="store_true", help="보관 만료된 글/댓글을 삭제 대상으로 삼는다")
    parser.add_argument("--apply", action="store_true", help="실제로 삭제한다 (--delete-expired 와 함께)")
    args = parser.parse_args()

    abort_if_key_in_repo(args.key)
    firebase_admin.initialize_app(credentials.Certificate(str(args.key)), {"databaseURL": args.db_url})
    now_ms = int(time.time() * 1000)
    items = collect(db.reference("post").get(), db.reference("user").get(), now_ms)

    print(f"신고가 접수된 글/댓글: {len(items)}개")
    for item in sorted(items, key=lambda i: i["status"]):
        print(f"  [{LABEL[item['status']]}] {item['kind']} {item['path']} / 작성자 {item['author']} / 신고 {item['reports']}건 / {item['snippet']!r}")

    if args.delete_expired:
        expired = [i for i in items if i["status"] == "expired"]
        print(f"\n보관 만료: {len(expired)}개", "-> 삭제" if args.apply else "(dry-run, --apply 를 붙이면 삭제)")
        if args.apply:
            for item in expired:
                db.reference(item["path"]).delete()
            print("삭제 완료")


if __name__ == "__main__":
    main()
