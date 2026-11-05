"""moderation.py 의 계산 부분 테스트 (Firebase 없이): python -m unittest tools/test_moderation.py"""
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
import moderation as m  # noqa: E402

DAY = m.DAY_MS
NOW = 1_800_000_000_000


class LevelTests(unittest.TestCase):
    def test_general_report_levels(self):
        self.assertEqual(m.level_of(0, False), "ok")
        self.assertEqual(m.level_of(2, False), "ok")
        self.assertEqual(m.level_of(3, False), "due")
        self.assertEqual(m.level_of(29, False), "due")
        self.assertEqual(m.level_of(30, False), "overdue")

    def test_priority_report_is_due_after_two_days(self):
        self.assertEqual(m.level_of(1, True), "ok")
        self.assertEqual(m.level_of(2, True), "due")
        self.assertEqual(m.level_of(30, True), "overdue")


class InboxTests(unittest.TestCase):
    def setUp(self):
        self.users = {"u1": {"name": "홍길동"}, "u2": {"withdrawn": True, "name": "탈퇴한 사용자"}}

    def test_sorted_by_urgency_and_priority_flag(self):
        reports = {
            "new": {"target": "u1", "reason": "spam", "source": "chat", "date": NOW - 1 * DAY},
            "old": {"target": "u1", "reason": "harassment", "source": "post", "date": NOW - 5 * DAY},
            "pri": {"target": "u2", "reason": "minor", "source": "chat", "date": NOW - 2 * DAY},
        }
        inbox = m.build_inbox(reports, {}, self.users, set(), NOW)
        self.assertEqual([i["ref"] for i in inbox], ["userReports/old", "userReports/pri", "userReports/new"])
        self.assertTrue(inbox[1]["priority"])
        self.assertEqual(inbox[1]["level"], "due")
        self.assertEqual(inbox[2]["level"], "ok")
        self.assertIn("탈퇴한 사용자", inbox[1]["target"])

    def test_handled_reports_are_excluded(self):
        reports = {"a": {"target": "u1", "reason": "spam", "source": "chat", "date": NOW - DAY},
                   "b": {"target": "u1", "reason": "spam", "source": "chat", "date": NOW - DAY, "status": "handled"}}
        inbox = m.build_inbox(reports, {}, self.users, {"userReports/a"}, NOW)
        self.assertEqual(inbox, [])

    def test_reported_posts_and_comments_ignore_self_reports(self):
        posts = {"p1": {"author": "u1", "title": "제목", "report": {"r1": {"uid": "u3", "date": NOW - 4 * DAY}},
                        "comments": {"c1": {"author": "u1", "content": "댓글", "report": {"r": {"uid": "u1", "date": NOW}}},
                                     "c2": {"author": "u3", "content": "다른 댓글", "report": {"r": {"uid": "u1"}}}}},
                 "p2": {"author": "u1", "title": "자기 신고", "report": {"r": {"uid": "u1", "date": NOW}}}}
        inbox = m.build_inbox({}, posts, self.users, set(), NOW)
        refs = {i["ref"]: i for i in inbox}
        self.assertEqual(set(refs), {"post/p1", "post/p1/comments/c2"})
        self.assertEqual(refs["post/p1"]["level"], "due")
        self.assertEqual(refs["post/p1/comments/c2"]["level"], "undated")

    def test_missing_date_is_marked_undated(self):
        inbox = m.build_inbox({"x": {"target": "u1", "reason": "spam", "source": "chat"}}, {}, self.users, set(), NOW)
        self.assertEqual(inbox[0]["level"], "undated")


class LegacyTests(unittest.TestCase):
    def test_only_undated_post_and_comment_reports(self):
        users = {"u1": {"name": "홍길동"}}
        reports = {"r": {"target": "u1", "reason": "spam", "source": "chat"}}   # 날짜 없는 이용자 신고는 제외 대상
        posts = {"old": {"author": "u1", "title": "옛 글", "report": {"x": {"uid": "u2"}}},
                 "dated": {"author": "u1", "title": "새 글", "report": {"x": {"uid": "u2", "date": NOW - DAY}}},
                 "withComment": {"author": "u1", "title": "t", "comments": {"c": {"author": "u1", "content": "옛 댓글",
                                                                              "report": {"x": {"uid": "u2"}}}}}}
        inbox = m.build_inbox(reports, posts, users, set(), NOW)
        refs = [i["ref"] for i in m.legacy_undated_items(inbox)]
        self.assertEqual(sorted(refs), ["post/old", "post/withComment/comments/c"])
        self.assertTrue(all(i["target_uid"] == "u1" for i in inbox if i["ref"] in refs))


class PurgeTests(unittest.TestCase):
    def test_only_entries_older_than_retention_are_expired(self):
        entries = {"old": {"date": NOW - 91 * DAY}, "edge": {"date": NOW - 90 * DAY}, "new": {"date": NOW - DAY},
                   "nodate": {"reason": "x"}}
        self.assertEqual(m.expired_entries(entries, "date", NOW), ["old"])

    def test_log_uses_its_own_field(self):
        self.assertEqual(m.expired_entries({"l": {"at": NOW - 100 * DAY}}, "at", NOW), ["l"])
        self.assertEqual(m.expired_entries({"l": {"at": NOW - 100 * DAY}}, "date", NOW), [])


class RefTests(unittest.TestCase):
    def test_valid_refs(self):
        for ref in ["userReports/abc", "post/p1", "post/p1/comments/c1"]:
            m.validate_ref(ref)

    def test_invalid_refs(self):
        for ref in ["user/u1", "post", "post/p1/comments", "post/p1/x/c1", "userReports/", "chats/a"]:
            with self.assertRaises(ValueError):
                m.validate_ref(ref)

    def test_delete_content_not_for_user_reports(self):
        with self.assertRaises(ValueError):
            m.check_action("userReports/a", "delete-content")
        m.check_action("post/p1", "delete-content")
        m.check_action("userReports/a", "suspend")

    def test_target_uid(self):
        self.assertEqual(m.target_uid_of("userReports/a", {"target": "t"}), "t")
        self.assertEqual(m.target_uid_of("post/p", {"author": "a"}), "a")


if __name__ == "__main__":
    unittest.main()
