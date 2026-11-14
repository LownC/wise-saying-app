package com.back;

import com.back.standard.simpleDb.SimpleDb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

// MySQL 저장소 통합 테스트. 테스트용 DB(wiseSaying__test)를 쓰고 매 테스트 전에 TRUNCATE 한다.
// AppTestRunner.run 을 두 번 호출하는 것이 "프로그램 종료 후 다시 시작" 이다.
class WiseSayingDbPersistenceTest {
    @BeforeEach
    void beforeEach() {
        AppTestRunner.resetTestDb();
    }

    private static long countRows() {
        SimpleDb simpleDb = AppContext.createSimpleDb();
        try {
            return simpleDb.genSql().append("SELECT COUNT(*) FROM wise_saying").selectLong();
        } finally {
            simpleDb.close();
        }
    }

    @Test
    @DisplayName("테스트는 개발 DB 가 아닌 테스트용 DB 를 쓴다")
    void t1() {
        assertThat(AppConfig.getDbName()).isEqualTo("wiseSaying__test");
    }

    @Test
    @DisplayName("등록하면 wise_saying 테이블에 저장된다")
    void t2() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                종료
                """);

        assertThat(countRows()).isEqualTo(1);
    }

    @Test
    @DisplayName("다시 시작해도 DB 에서 명언을 읽어 목록에 보여 준다")
    void t3() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                종료
                """);

        String out = AppTestRunner.run("""
                목록
                종료
                """);

        assertThat(out)
                .contains("2 / 작자미상 / 과거에 집착하지 마라.")
                .contains("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("수정 내용이 다시 시작한 뒤에도 남는다")
    void t4() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                수정?id=1
                현재와 자신을 사랑하라.
                홍길동
                종료
                """);

        String out = AppTestRunner.run("""
                목록
                종료
                """);

        assertThat(out).contains("1 / 홍길동 / 현재와 자신을 사랑하라.");
    }

    @Test
    @DisplayName("삭제한 뒤 다시 시작해도 번호가 이어지고 삭제한 번호를 재사용하지 않는다")
    void t5() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                삭제?id=2
                종료
                """);

        String out = AppTestRunner.run("""
                등록
                새로운 명언
                새 작가
                목록
                종료
                """);

        assertThat(out)
                .contains("3번 명언이 등록되었습니다.")
                .contains("3 / 새 작가 / 새로운 명언")
                .doesNotContain("과거에 집착하지 마라.");
    }

    @Test
    @DisplayName("빌드하면 DB 의 모든 명언을 배열 형태의 data.json 으로 만든다")
    void t6() throws IOException {
        String out = AppTestRunner.run("""
                등록
                명언 1
                작가 1
                등록
                명언 2
                작가 2
                빌드
                종료
                """);

        assertThat(out).contains("data.json 파일의 내용이 갱신되었습니다.");

        String json = Files.readString(AppConfig.getWiseSayingDbDir().resolve("data.json"), StandardCharsets.UTF_8).trim();

        assertThat(json).startsWith("[").endsWith("]");
        assertThat(json).contains("\"id\": 1", "\"content\": \"명언 1\"", "\"author\": \"작가 1\"", "\"id\": 2");
        assertThat(json.indexOf("\"id\": 1")).isLessThan(json.indexOf("\"id\": 2"));
    }

    @Test
    @DisplayName("SQL 검색은 작은따옴표가 들어간 검색어도 바인딩 값으로 안전하게 처리한다")
    void t7() {
        String out = AppTestRunner.run("""
                등록
                100% 노력하라
                작자미상
                등록
                대충 하라
                작자미상
                목록?keywordType=content&keyword=100'
                종료
                """);

        assertThat(out).contains("검색어 : 100'").doesNotContain("대충 하라");
    }
}
