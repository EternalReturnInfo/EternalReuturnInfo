# tools

## purge_passwords.py

Firebase에 평문으로 저장된 비밀번호 필드를 삭제해요. 기본은 dry-run이라 `--apply` 없이는 아무것도 지우지 않아요.

### 준비

1. Firebase 콘솔 → 프로젝트 설정 → 서비스 계정 → **새 비공개 키 생성**으로 JSON을 받아요.
2. 그 JSON은 **이 저장소 밖**에 두세요. (예: `~/secrets/er-admin.json`) 저장소 안에 있으면 스크립트가 중단돼요.
3. 설치:

```bash
python -m venv .venv && source .venv/bin/activate
pip install firebase-admin
```

### 실행

먼저 조회만 해요.

```bash
python tools/purge_passwords.py --key ~/secrets/er-admin.json --db-url https://eternalreturninfo-4dc4b-default-rtdb.firebaseio.com
```

개수가 예상과 맞으면 삭제해요.

```bash
python tools/purge_passwords.py --key ~/secrets/er-admin.json --db-url https://eternalreturninfo-4dc4b-default-rtdb.firebaseio.com --apply
```

마지막에 다시 조회해서 `남은 항목 ... 0개 / 0개`가 나오면 끝이에요.

### 끝난 뒤

- 서비스 계정 키는 콘솔(서비스 계정 → 키 관리, 또는 Google Cloud 콘솔 IAM → 서비스 계정 → 키)에서 **삭제**하고, 로컬 JSON도 지워요.
- 관리자 SDK는 보안 규칙을 무시하고 접근해요. 규칙을 잠가둔 상태에서도 동작해요.

## rotate_passwords.py

노출된 비밀번호를 무효화해요. 이메일/비밀번호 계정마다 ① 비밀번호를 무작위 값으로 교체하고 ② 기존 로그인 세션을 폐기한 뒤 ③ 비밀번호 재설정 메일(한국어)을 보내요. Google 로그인 전용 계정은 건드리지 않아요.

**교체된 계정은 재설정 메일의 링크로 새 비밀번호를 만들기 전까지 로그인할 수 없어요.** 아래 순서를 지켜주세요.

### 실행 전

1. 사용자에게 보낼 **안내 메일을 먼저(또는 동시에)** 보내요. 설명 없이 재설정 메일만 가면 피싱으로 오해받아요.
2. 콘솔 → Authentication → 템플릿 → 비밀번호 재설정에서 제목을 알아보기 쉽게 고쳐요. (예: `[ER info] 비밀번호를 다시 설정해 주세요`)
3. 서비스 계정 키를 새로 만들어요. (이전 키는 삭제했을 거예요.) 저장소 밖에 두세요.
4. 같은 가상환경(`firebase-admin` 설치됨)을 써요.

### 실행

먼저 대상 개수만 조회해요. 전체 계정 수와 이메일/비밀번호 계정 수를 확인하세요.

```bash
python tools/rotate_passwords.py --key ~/secrets/er-admin.json
```

맞으면 실행해요. `비밀번호 무효화` 를 직접 입력해야 시작돼요. 메일은 계정당 1초 간격으로 보내요.

```bash
python tools/rotate_passwords.py --key ~/secrets/er-admin.json --apply
```

### 문제가 생기면

- 메일 실패 계정이 나오면 마지막에 재시도 명령이 출력돼요. 그대로 실행하면 그 계정에만 메일을 다시 보내요. (`--apply --skip-rotate --only 이메일...`)
- `API_KEY_ANDROID_APP_BLOCKED` 같은 오류가 나오면 API 키에 Android 앱 제한이 걸려 있는 거예요. 콘솔 → 프로젝트 설정 → 내 앱의 **SHA-1 인증서 지문**을 콜론(:) 없이 `--android-cert` 로 넘기세요.
- `TOO_MANY_ATTEMPTS_TRY_LATER` 는 발송 한도예요. 잠시 뒤 재시도하세요.
- 끝나면 서비스 계정 키를 다시 삭제하세요.

## send_notice.py

가입자에게 보안 사고 안내 메일을 **한 명씩 개별 발송**해요. (서로의 주소가 보이지 않아요.) 안내문은 `tools/notice_template.txt`이고, 첫 줄 `제목: ...`이 제목, 나머지가 본문이에요. `{contact}`는 발신 주소로 바뀌어요. **발송 전에 안내문 내용을 꼭 읽고 사실과 다른 부분을 고치세요.**

수신자는 이메일/비밀번호 계정이에요(`rotate_passwords.py` 대상과 같아요). Google 로그인 계정까지 보내려면 `--all`을 붙이세요.

### 발신 계정 준비 (ER info 전용 Gmail)

1. 새 Gmail 계정을 만들어요. (예: `erinfo.help@gmail.com`) 표시 이름은 `ER info`로 해요.
2. Google 계정 → 보안 → **2단계 인증**을 켜요. (앱 비밀번호의 전제 조건이에요.)
3. Google 계정 → 보안 → 2단계 인증 → **앱 비밀번호**에서 하나 만들어요. 16자리가 나와요.
4. 앱 비밀번호는 명령줄에 쓰지 말고, 실행할 때 입력창에 붙여 넣거나 환경변수 `ER_SMTP_PASSWORD`로 넘겨요.

### 순서

1. **dry-run:** 대상 수와 메일 미리보기를 봐요. 아무것도 보내지 않아요.

```powershell
.\.venv\Scripts\python tools\send_notice.py --smtp-user erinfo.help@gmail.com --key "C:\...\secrets\<키>.json"
```

2. **테스트 1통:** 본인 개인 메일로 먼저 한 통만 보내서 스팸함에 가는지, 글이 깨지지 않는지 확인해요.

```powershell
.\.venv\Scripts\python tools\send_notice.py --smtp-user erinfo.help@gmail.com --test-to 내개인메일@gmail.com --apply
```

3. **본 발송:** `안내 메일 발송`을 입력해야 시작돼요. 메일 사이에 3초씩 쉬어요.

```powershell
.\.venv\Scripts\python tools\send_notice.py --smtp-user erinfo.help@gmail.com --key "C:\...\secrets\<키>.json" --apply
```

4. 안내 메일이 나가면 **바로 이어서** `rotate_passwords.py --apply`를 실행해요. 안내문이 "곧 재설정 메일이 간다"고 약속하고 있어요.

### 참고

- 발송한 주소는 키 파일 옆의 `notice_sent.log`에 기록돼요. 다시 실행해도 중복 발송하지 않아요. 이메일이 들어 있으니 저장소에 올리지 마세요.
- 새 Gmail 계정은 한꺼번에 많이 보내면 스팸이나 의심 활동으로 막힐 수 있어요. 불안하면 `--limit 20`으로 나눠서 보내세요.
- 실패한 주소는 마지막에 재시도 명령이 출력돼요.
- Gmail 로그인 실패가 나오면 앱 비밀번호와 2단계 인증을 확인하세요.
- `Connection unexpectedly closed` 처럼 연결이 끊기면 `--port 587` 을 붙여 다시 시도하세요. 계정의 '보안 알림' 메일(차단된 로그인 시도)과 백신의 메일 검사 기능도 확인하세요.
- 포트 465, 587 모두 끊기면 `smtp_check.py` 로 어느 단계에서 막히는지 확인하세요. 메일은 보내지 않고, 비밀번호도 출력하지 않아요. 연결만 보려면 `--skip-login` 을 붙이세요.

```powershell
.\.venv\Scripts\python tools\smtp_check.py --smtp-user erinfomanager@gmail.com
```

- 터미널 출력을 복사하면 줄이 바뀌는 자리의 공백이 빠질 수 있어요. 안내문이 이상해 보여도 실제 파일 문제가 아닐 수 있으니, 테스트 메일로 확인하세요.
- `Read-Host "..."` 의 따옴표 안은 안내 문구예요. **여기에 앱 비밀번호를 쓰면 화면과 명령 기록에 남아요.** 비밀번호는 뜨는 입력칸에만 입력하세요.

## make_apps_script.py (SMTP 로그인이 막힐 때)

`send_notice.py` 가 Gmail 로그인 단계에서 계속 끊기면, 구글 서버에서 직접 보내는 Google Apps Script 방식으로 보낼 수 있어요. 앱 비밀번호가 필요 없고, PC의 네트워크나 백신과도 무관해요. 이 도구가 **수신자 목록과 안내문이 들어간 `.gs` 파일**을 만들어 줘요.

### 순서

1. 파일을 만들어요. 키 파일 옆에 `notice_sender.gs` 가 생겨요. (이메일이 들어 있으니 저장소 밖이에요.)

```powershell
.\.venv\Scripts\python tools\make_apps_script.py --key "C:\...\secrets\<키>.json" --contact erinfomanager@gmail.com --test-to <내개인메일>
```

2. **ER info 계정으로** script.google.com 에 접속해서 **새 프로젝트**를 만들어요.
3. 기본 코드를 전부 지우고 `notice_sender.gs` 내용을 붙여 넣은 뒤 저장(Ctrl+S)해요.
4. 위쪽 함수 선택에서 `testSend` 를 고르고 **실행**해요. 처음에는 권한 승인 화면이 떠요. **권한 검토 → 계정 선택 → 고급 → (프로젝트 이름)으로 이동(안전하지 않음) → 허용** 순서예요. 본인이 만든 스크립트라서 안전해요.
5. 개인 메일로 테스트 메일 1통이 왔는지, 스팸함은 아닌지 확인해요.
6. 함수를 `sendToAll` 로 바꿔서 **실행**해요. 실행 로그에 성공/실패 수가 나와요. 다시 실행해도 이미 보낸 사람은 건너뛰어요.
7. 끝나면 Apps Script 프로젝트를 **삭제**하고, 로컬의 `notice_sender.gs` 도 지워요. 이메일이 들어 있어요.

### 참고

- 일반 Gmail 은 하루 발송 한도가 있어요(대략 100명). 스크립트가 남은 한도를 확인해서 그만큼만 보내고, 남으면 내일 다시 실행하라고 알려줘요.
- 한 번 실행은 6분 제한이 있어요. 71명은 약 2분이면 끝나요.
- 보낸 사람 기록은 **안내문(제목)마다 따로** 저장돼요. 같은 프로젝트에서 다른 안내문을 보내도 막히지 않아요. (예전 버전은 하나의 기록을 같이 써서, 두 번째 안내문이 `남은 대상 0명`으로 막혔어요.) 그래도 개인정보가 든 프로젝트는 발송 후 삭제하세요.
- `남은 대상 0명`이 나오면 프로젝트 설정(톱니바퀴) → 스크립트 속성에서 `sent...` 항목을 삭제하고 다시 실행하세요.

### 후속 안내 메일 보내기 (재설정 링크가 만료된 사용자용)

재설정 링크는 유효 시간이 짧아서 며칠 뒤에 누르면 "만료되었거나 이미 사용되었습니다"가 나와요. 앱 로그인 화면의 "비밀번호 찾기"로 새 링크를 받을 수 있다는 안내문이 `tools/notice_followup.txt` 에 있어요. `--notice` 로 이 파일을 지정하면 같은 방식으로 보내요. `--exclude` 로 오타 주소 등을 뺄 수 있어요.

```powershell
.\.venv\Scripts\python tools\make_apps_script.py --key "C:\...\secrets\<키>.json" --contact erinfomanager@gmail.com --test-to <내개인메일> --notice tools\notice_followup.txt --exclude 오타주소@gmial.com
```

## user_stats.py

가입자 현황을 **집계로만** 보여줘요. 이메일 등 개인정보는 출력하지 않고, 아무것도 바꾸지 않는 읽기 전용이에요. 계정 수, 로그인 방식, 월별 가입 수, 마지막 활동 분포, 가입 후 다시 오지 않은 계정 수를 보여줘요. `--db-url` 을 주면 게시글·댓글·채팅 개수도 세요.

```powershell
.\.venv\Scripts\python tools\user_stats.py --key "C:\...\secrets\<키>.json" --db-url https://eternalreturninfo-4dc4b-default-rtdb.firebaseio.com
```

마지막 활동은 로그인과 토큰 갱신 기록 기준이에요. 끝나면 서비스 계정 키를 삭제하세요.

## scrub_rtdb_email.py

Realtime Database 의 `user/<uid>/email` 필드를 지워요. `user` 노드는 로그인한 모든 사용자가 읽을 수 있는데 앱은 이 노드의 이메일을 쓰지 않아서(내 정보 화면은 Firestore 에서 읽어요), 남아 있는 이메일 값을 정리하는 일회성 도구예요. 이메일 값은 출력하지 않고 개수만 보여줘요.

앱이 새 가입자의 이메일을 이 노드에 쓰지 않는 버전(PR "RTDB user 노드에 이메일 저장 중단")이 `main` 에 들어간 뒤에 실행하세요.

먼저 dry-run 으로 대상 개수만 봐요.

```powershell
.\.venv\Scripts\python tools\scrub_rtdb_email.py --key "C:\...\secrets\<키>.json" --db-url https://eternalreturninfo-4dc4b-default-rtdb.firebaseio.com
```

개수가 예상과 맞으면 `--apply` 를 붙여 실제로 지워요. 끝에 다시 조회해서 남은 항목이 0개인지 확인하고, 0개이면 종료 코드 0 으로 끝나요.

```powershell
.\.venv\Scripts\python tools\scrub_rtdb_email.py --key "C:\...\secrets\<키>.json" --db-url https://eternalreturninfo-4dc4b-default-rtdb.firebaseio.com --apply
```

끝나면 서비스 계정 키를 삭제하세요. (Firebase 콘솔 > 프로젝트 설정 > 서비스 계정 > 키 관리)

## reports_review.py

신고가 접수된 게시글·댓글을 한눈에 보고, **탈퇴한 작성자의 신고된 글/댓글을 3개월 보관한 뒤 지우는** 도구예요. 앱의 회원탈퇴는 다른 사람의 신고가 접수된 글/댓글을 지우지 않고 남겨 두기 때문에(개인정보처리방침 참고), 보관 기간이 끝난 것을 이 도구로 정리해요.

- 기본은 읽기 전용이에요. 목록에는 상태가 붙어요: `작성자 활동 중`(건드리지 않음), `탈퇴, 보관 중`, `보관 만료`(첫 신고 후 90일 경과), `탈퇴, 날짜 없음(직접 판단)`(시각이 없는 옛 신고).
- 내용은 앞 30자만 보여줘요. 본인이 자기 글을 신고한 것은 세지 않아요.

```powershell
.\.venv\Scripts\python tools\reports_review.py --key "C:\...\secrets\<키>.json" --db-url https://eternalreturninfo-4dc4b-default-rtdb.firebaseio.com
```

보관 만료된 것을 지울 때는 먼저 대상만 확인하고, 맞으면 `--apply` 를 붙여요.

```powershell
.\.venv\Scripts\python tools\reports_review.py --key "C:\...\secrets\<키>.json" --db-url https://eternalreturninfo-4dc4b-default-rtdb.firebaseio.com --delete-expired
.\.venv\Scripts\python tools\reports_review.py --key "C:\...\secrets\<키>.json" --db-url https://eternalreturninfo-4dc4b-default-rtdb.firebaseio.com --delete-expired --apply
```

끝나면 서비스 계정 키를 삭제하세요. 한 달에 한 번 정도 점검하면 돼요.


## moderation.py (신고 접수함과 처리 기록)

이용약관 7항의 약속(확인 3일, 위험이 큰 내용 2일, 결정 늦어도 1개월, 신고 내역 3개월 보관)을 지키기 위한 도구예요. 운영 절차는 Notion의 "신고 처리 운영 절차"를 따르세요. **확인(inbox)은 읽기 전용이고, 바꾸는 명령은 `--apply` 를 붙여야 실제로 실행돼요.** 같은 가상환경(`firebase-admin` 설치됨)과 서비스 계정 키(저장소 밖)를 써요. 작업이 끝나면 키는 다시 지우세요.

```powershell
$key = "C:\dev\secrets\<키>.json"
$url = "https://eternalreturninfo-4dc4b-default-rtdb.firebaseio.com"
```

### 1. 접수함 (일주일에 두세 번, 최소 3일에 한 번)
처리하지 않은 이용자 신고와 신고된 글/댓글을 급한 순서로 보여줘요. `[확인 기한 지남]`, `[결정 기한 지남]` 이 붙은 것부터 처리하세요. 위험이 큰 신고(미성년자 보호 위반, 성적인 내용)는 `[우선]` 이 붙고 2일 기한이에요.
```powershell
.\.venv\Scripts\python tools\moderation.py inbox --key $key --db-url $url
```

### 2. 한 건 처리하기
먼저 `--apply` 없이 실행해서 무엇을 할지 확인하고, 맞으면 `--apply` 를 붙여요. `--note` 에는 처리 내용을 한 줄로 적어요(이름, 이메일 같은 개인정보는 적지 마세요).

| 조치 `--action` | 하는 일 |
| --- | --- |
| `none` | 문제 없음으로 판단, 기록만 남김 |
| `warn` | 경고(앱 밖에서 안내하거나 기록만), 기록만 남김 |
| `delete-content` | 글/댓글 삭제 (REF 가 `post/...` 일 때만) |
| `suspend` | 계정 사용 중지와 로그인 정보 만료 (REF 의 대상 사용자) |

```powershell
.\.venv\Scripts\python tools\moderation.py resolve "userReports/<id>" --action suspend --note "반복적인 욕설, 경고 후에도 재발" --key $key --db-url $url
.\.venv\Scripts\python tools\moderation.py resolve "userReports/<id>" --action suspend --note "반복적인 욕설, 경고 후에도 재발" --key $key --db-url $url --apply
```
이용 제한한 사용자를 풀 때:
```powershell
.\.venv\Scripts\python tools\moderation.py unsuspend <uid> --note "이의 제기 수용" --key $key --db-url $url --apply
```

### 2-1. 신고 시각이 없는 옛 신고 한꺼번에 처리하기 (처음 한 번)
옛 앱 시절의 신고에는 시각이 없어서 기한을 셀 수 없고, 접수함에 계속 남아요. 이런 옛 글/댓글 신고만 골라서 한 번에 처리해요(이용자 신고는 항상 시각이 있어서 해당 없어요). 먼저 `--apply` 없이 **무엇이 처리되는지 목록을 확인**하세요.
- `--action none`: 기록만 남기고 글/댓글은 그대로 둬요.
- `--action delete-content`: 글/댓글을 **삭제**하고 기록을 남겨요. 삭제는 되돌릴 수 없어요. 글을 지우면 그 글에 달린 댓글도 함께 지워져요.
```powershell
.\.venv\Scripts\python tools\moderation.py resolve-legacy --action delete-content --note "옛 테스트 신고 일괄 정리" --key $key --db-url $url
.\.venv\Scripts\python tools\moderation.py resolve-legacy --action delete-content --note "옛 테스트 신고 일괄 정리" --key $key --db-url $url --apply
```

### 3. 정리 (한 달에 한 번)
접수일로부터 90일이 지난 이용자 신고와 처리 기록을 지워요. 신고가 접수된 글/댓글 중 작성자가 탈퇴한 것은 `reports_review.py --delete-expired` 로 따로 정리해요.
```powershell
.\.venv\Scripts\python tools\moderation.py purge --key $key --db-url $url
.\.venv\Scripts\python tools\moderation.py purge --key $key --db-url $url --apply
```

### 테스트
핵심 계산(기한, 정렬, 정리 대상, REF 검증)은 Firebase 없이 확인해요.
```powershell
python tools\test_moderation.py
```

### 참고
- 처리 기록은 `moderationLog` 에 남고, 앱에서는 읽을 수도 쓸 수도 없어요(서버 규칙에 정의되지 않은 경로).
- 사용 중지한 계정이 이미 앱을 열어 둔 경우 최대 1시간 안에 로그아웃돼요.
