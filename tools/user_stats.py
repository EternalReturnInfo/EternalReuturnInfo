"""가입자 현황을 집계로만 보여주는 도구. 이메일 같은 개인정보는 출력하지 않아요. (읽기 전용)

  - 계정 수, 로그인 방식, 월별 가입 수
  - 마지막 활동 시점 분포 (최근 30일 / 90일 / 1년 / 1년 초과)
  - 가입 후 다시 오지 않은 계정 수
  - (선택) --db-url 을 주면 Realtime DB 의 게시글·댓글·채팅 개수

실제 이용자가 있는지 짐작이 아니라 숫자로 확인하려는 용도예요. 사용법은 tools/README.md 참고.
"""
import argparse
import datetime as dt
from collections import Counter
from pathlib import Path

import firebase_admin
from firebase_admin import auth, credentials, db

from purge_passwords import abort_if_key_in_repo

KST = dt.timezone(dt.timedelta(hours=9))
IDLE_BUCKETS = (("최근 30일", 30), ("31~90일 전", 90), ("91일~1년 전", 365))
IDLE_ORDER = [label for label, _ in IDLE_BUCKETS] + ["1년 넘게 활동 없음", "기록 없음"]


def to_kst(millis):
    return dt.datetime.fromtimestamp(millis / 1000, tz=KST) if millis else None


def summarize(users, now: dt.datetime) -> dict:
    providers, created, idle = Counter(), Counter(), Counter()
    total = never_returned = 0
    for user in users:
        total += 1
        providers["+".join(sorted({p.provider_id for p in user.provider_data})) or "(없음)"] += 1
        meta = user.user_metadata
        joined = to_kst(meta.creation_timestamp)
        last = max(filter(None, [to_kst(meta.last_sign_in_timestamp), to_kst(meta.last_refresh_timestamp)]), default=None)
        if joined:
            created[joined.strftime("%Y-%m")] += 1
        if last is None:
            idle["기록 없음"] += 1
        else:
            days = (now - last).days
            idle[next((label for label, limit in IDLE_BUCKETS if days <= limit), "1년 넘게 활동 없음")] += 1
        if joined and (last is None or (last - joined).days < 1):
            never_returned += 1
    return {"total": total, "providers": providers, "created": created, "idle": idle, "never_returned": never_returned}


def rtdb_counts() -> dict:
    root = db.reference().get() or {}
    posts = root.get("post") or {}
    rooms = root.get("chats") or {}
    return {
        "user 노드": len(root.get("user") or {}),
        "게시글": len(posts),
        "댓글": sum(len(p.get("comments") or {}) for p in posts.values() if isinstance(p, dict)),
        "채팅방": len(rooms),
        "채팅 메시지": sum(len(r.get("messages") or {}) for r in rooms.values() if isinstance(r, dict)),
    }


def show(summary: dict, counts: dict | None) -> None:
    print(f"전체 계정: {summary['total']}개")
    print("\n[로그인 방식]")
    for name, count in summary["providers"].most_common():
        print(f"  {name}: {count}개")
    print("\n[월별 가입 수]")
    for month in sorted(summary["created"]):
        print(f"  {month}: {summary['created'][month]}개")
    print("\n[마지막 활동 시점]")
    for label in IDLE_ORDER:
        if summary["idle"][label]:
            print(f"  {label}: {summary['idle'][label]}개")
    print(f"\n가입 후 다시 오지 않은 계정(가입 당일 이후 활동 없음): {summary['never_returned']}개")
    if counts is not None:
        print("\n[Realtime DB 항목 수]")
        for name, count in counts.items():
            print(f"  {name}: {count}개")
    print("\n참고: 마지막 활동은 로그인과 토큰 갱신 기록 기준이에요. 2026-10-30에 전 계정 세션을 폐기했으니, 그 이후 기록은 다시 쌓이는 중이에요.")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--key", required=True, type=Path, help="서비스 계정 키(JSON) 경로 (저장소 밖에 둘 것)")
    parser.add_argument("--db-url", help="Realtime Database URL (주면 게시글·채팅 개수도 센다)")
    args = parser.parse_args()

    abort_if_key_in_repo(args.key)
    options = {"databaseURL": args.db_url} if args.db_url else None
    firebase_admin.initialize_app(credentials.Certificate(str(args.key)), options)
    summary = summarize(auth.list_users().iterate_all(), dt.datetime.now(KST))
    show(summary, rtdb_counts() if args.db_url else None)


if __name__ == "__main__":
    main()
