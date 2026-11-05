"""Realtime Database 의 user/<uid>/email 필드를 삭제하는 일회성 스크립트.

user 노드는 로그인한 모든 사용자가 읽을 수 있는데 앱은 이 노드의 이메일을 쓰지 않아요.
(내 정보 화면의 이메일은 Firestore 에서 읽어요.) 그래서 남아 있는 이메일 값을 지워요.

기본은 dry-run(조회만)이에요. 실제로 지우려면 --apply 를 붙이세요.
이메일 값은 화면에 출력하지 않고 개수만 보여줘요.

사용법은 tools/README.md 참고.
"""
import argparse
import sys
from pathlib import Path

import firebase_admin
from firebase_admin import credentials, db

from purge_passwords import abort_if_key_in_repo

RTDB_PATH = "user"
RTDB_FIELD = "email"


def find_targets(users: dict | None) -> list[str]:
    """email 필드를 가진 user 노드의 uid 목록."""
    return [uid for uid, node in (users or {}).items() if isinstance(node, dict) and RTDB_FIELD in node]


def scrub(uids: list[str], apply: bool) -> None:
    print(f"[Realtime DB] {RTDB_PATH}/*/{RTDB_FIELD} 가 있는 계정: {len(uids)}개")
    if apply and uids:
        db.reference(RTDB_PATH).update({f"{uid}/{RTDB_FIELD}": None for uid in uids})
        print("[Realtime DB] 삭제 완료")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--key", required=True, type=Path, help="서비스 계정 키(JSON) 경로 (저장소 밖에 둘 것)")
    parser.add_argument("--db-url", required=True, help="Realtime Database URL")
    parser.add_argument("--apply", action="store_true", help="실제로 삭제한다 (없으면 dry-run)")
    args = parser.parse_args()

    abort_if_key_in_repo(args.key)
    firebase_admin.initialize_app(credentials.Certificate(str(args.key)), {"databaseURL": args.db_url})

    print("모드:", "삭제 (--apply)" if args.apply else "dry-run (조회만)")
    scrub(find_targets(db.reference(RTDB_PATH).get()), args.apply)

    if args.apply:
        print("\n검증: 다시 조회합니다.")
        left = len(find_targets(db.reference(RTDB_PATH).get()))
        print(f"남은 항목: {left}개")
        sys.exit(0 if left == 0 else 1)


if __name__ == "__main__":
    main()
