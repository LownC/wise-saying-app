package com.back;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// 검색·페이징 통합 테스트: 샘플 명언 10개("명언 1"~"명언 10")를 만든 앱으로 실행한다
class WiseSayingListTest {
    @BeforeEach
    void beforeEach() {
        AppTestRunner.resetTestDb();
    }

    private static String runWithSampleData(String input) {
        return AppTestRunner.run(input, scanner -> new App(scanner, true).run());
    }

    @Test
    @DisplayName("샘플 데이터 : 시작할 때 명언이 0개면 10개를 만든다")
    void t1() {
        String out = runWithSampleData("""
                목록?page=2
                종료
                """);

        assertThat(out)
                .contains("1 / 작자미상 1 / 명언 1")
                .contains("5 / 작자미상 5 / 명언 5");
    }

    @Test
    @DisplayName("목록 : page 를 생략하면 1페이지(10~6번)를 최신순으로 보여 준다")
    void t2() {
        String out = runWithSampleData("""
                목록
                종료
                """);

        assertThat(out)
                .contains("10 / 작자미상 10 / 명언 10")
                .contains("6 / 작자미상 6 / 명언 6")
                .doesNotContain("5 / 작자미상 5 / 명언 5")
                .contains("페이지 : [1] / 2");

        assertThat(out.indexOf("10 / 작자미상 10 / 명언 10"))
                .isLessThan(out.indexOf("6 / 작자미상 6 / 명언 6"));
    }

    @Test
    @DisplayName("목록?page=2 : 5~1번을 보여 준다")
    void t3() {
        String out = runWithSampleData("""
                목록?page=2
                종료
                """);

        assertThat(out)
                .contains("5 / 작자미상 5 / 명언 5")
                .contains("1 / 작자미상 1 / 명언 1")
                .doesNotContain("6 / 작자미상 6 / 명언 6")
                .contains("페이지 : 1 / [2]");
    }

    @Test
    @DisplayName("검색 : keywordType=content 로 내용에 '1' 이 들어간 명언(10번·1번)만 보여 준다")
    void t4() {
        String out = runWithSampleData("""
                목록?keywordType=content&keyword=1
                종료
                """);

        assertThat(out)
                .contains("검색타입 : content")
                .contains("검색어 : 1")
                .contains("10 / 작자미상 10 / 명언 10")
                .contains("1 / 작자미상 1 / 명언 1")
                .doesNotContain("2 / 작자미상 2 / 명언 2")
                .contains("페이지 : [1]");
    }

    @Test
    @DisplayName("검색 : keywordType=author 로 작가를 검색한다")
    void t5() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                홍길동
                목록?keywordType=author&keyword=작자
                종료
                """);

        assertThat(out)
                .contains("검색타입 : author")
                .contains("검색어 : 작자")
                .contains("1 / 작자미상 / 현재를 사랑하라.")
                .doesNotContain("2 / 홍길동 / 과거에 집착하지 마라.");
    }

    @Test
    @DisplayName("검색 : 예시처럼 내용에 '과거' 가 들어간 명언만 보여 준다")
    void t6() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                목록?keywordType=content&keyword=과거
                종료
                """);

        assertThat(out)
                .contains("검색타입 : content")
                .contains("검색어 : 과거")
                .contains("2 / 작자미상 / 과거에 집착하지 마라.")
                .doesNotContain("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("검색 + 페이징 : 검색 결과를 기준으로 페이징한다")
    void t7() {
        // 샘플 10개 + "명언" 이 들어간 명언 2개 더 → 내용에 '명언' 검색 결과 12개, 3페이지
        String out = runWithSampleData("""
                등록
                명언 11
                작자미상 11
                등록
                명언 12
                작자미상 12
                목록?keywordType=content&keyword=명언&page=3
                종료
                """);

        assertThat(out)
                .contains("2 / 작자미상 2 / 명언 2")
                .contains("1 / 작자미상 1 / 명언 1")
                .doesNotContain("3 / 작자미상 3 / 명언 3")
                .contains("페이지 : 1 / 2 / [3]");
    }

    @Test
    @DisplayName("검색 + 페이징 : 검색 결과가 5개 이하면 1페이지뿐이다")
    void t8() {
        String out = runWithSampleData("""
                목록?keywordType=author&keyword=작자미상 1&page=1
                종료
                """);

        assertThat(out)
                .contains("10 / 작자미상 10 / 명언 10")
                .contains("1 / 작자미상 1 / 명언 1")
                .contains("페이지 : [1]")
                .doesNotContain("페이지 : [1] / 2");
    }
}
