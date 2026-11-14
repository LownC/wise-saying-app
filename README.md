# 명언 앱

콘솔 명언 앱. Controller · Service · Repository 레이어드 구조이며, 저장소 구현체를 갈아 끼우며 메모리 → 파일 → MySQL 로 영속성을 바꿔 왔습니다.

| 단계 | 태그 | 내용 |
| --- | --- | --- |
| ③ | `stage-3` | List·스트림·Rq·모던 switch |
| ⑤ | `stage-5` | TestUtil·AppTestRunner 통합 테스트 |
| ⑥ | `stage-6` | 검색·페이징 |
| ⑦ | `stage-7` | 파일 영속성(`db/wiseSaying/{id}.json`, `lastId.txt`)·빌드(`data.json`) |
| ⑧ | `stage-8` | MySQL 전환 |

## 명령

`등록`, `목록`, `목록?keywordType=content&keyword=과거&page=2`, `삭제?id=1`, `수정?id=1`, `빌드`, `종료`

## MySQL 준비 (⑧)

1. MySQL 띄우기

   ```powershell
   docker run --name mysql-1 -p 127.0.0.1:3306:3306 -e TZ=Asia/Seoul -e MYSQL_ROOT_PASSWORD=<비밀번호> -d mysql:8.4
   ```

2. DB·테이블 만들기 — 개발 DB `wiseSaying__dev`, 테스트 DB `wiseSaying__test`

   ```powershell
   Get-Content schema.sql | docker exec -i mysql-1 mysql -uroot -p<비밀번호>
   ```

   테이블 정의는 [`schema.sql`](schema.sql) 에 있습니다. 앱도 시작할 때 `CREATE TABLE IF NOT EXISTS` 로 같은 테이블을 만듭니다.

3. 접속 정보 — 소스에 하드코딩하지 않습니다. 환경변수 `DB_HOST`·`DB_USERNAME`·`DB_PASSWORD` 를 쓰거나,
   `db.properties.example` 을 `db.properties` 로 복사해 채웁니다(`db.properties` 는 `.gitignore` 대상).

4. 실행·테스트

   ```powershell
   .\gradlew.bat test
   ```

   테스트는 `wiseSaying__test` 를 쓰고 매 테스트 전에 `TRUNCATE wise_saying` 합니다.

## 리포지터리를 어떻게 갈아 끼웠나

- `WiseSayingRepository` 를 인터페이스로 두고 서비스는 이것만 의존합니다.
- 구현체: `WiseSayingMemoryRepository`(⑥까지) → `WiseSayingFileRepository`(⑦) → `WiseSayingDbRepository`(⑧).
- 어떤 구현체를 쓸지는 `AppContext` 한 곳에서만 정합니다. ⑧에서는 그 부분만 `WiseSayingDbRepository` 로 바꿨고, 컨트롤러·서비스 코드는 그대로입니다.
- `WiseSayingDbRepository` 는 앞 과제에서 만든 `SimpleDb`(`com.back.standard.simpleDb`)로 JDBC 를 감쌌습니다.
  검색·페이징은 SQL(`WHERE ... LIKE`, `ORDER BY id DESC`, `LIMIT ? OFFSET ?`)로 처리하고, 번호는 `AUTO_INCREMENT` 가 정하므로 재시작해도 이어지며 삭제한 번호를 재사용하지 않습니다.
- `빌드` 는 DB 의 모든 명언을 번호 오름차순 배열로 `db/wiseSaying/data.json` 에 씁니다.
