package com.back;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

// 파일 영속성·빌드 테스트. 테스트 전용 폴더(db/test/wiseSaying)를 쓰고 매 테스트 전에 비운다.
// AppTestRunner.run 을 두 번 호출하는 것이 "프로그램 종료 후 다시 시작" 이다.
class WiseSayingFilePersistenceTest {
    private Path dir;

    @BeforeEach
    void beforeEach() {
        AppTestRunner.resetTestDb();
        dir = AppConfig.getWiseSayingDbDir();
    }

    private String read(String fileName) throws IOException {
        return Files.readString(dir.resolve(fileName), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("테스트는 실제 db/wiseSaying 이 아닌 테스트 전용 폴더를 쓴다")
    void t1() {
        assertThat(dir.toString().replace('\\', '/')).isEqualTo("db/test/wiseSaying");
    }

    @Test
    @DisplayName("등록하면 {번호}.json 에 id·content·author 를 저장하고 lastId.txt 를 갱신한다")
    void t2() throws IOException {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                종료
                """);

        assertThat(read("1.json"))
                .contains("\"id\": 1")
                .contains("\"content\": \"현재를 사랑하라.\"")
                .contains("\"author\": \"작자미상\"");
        assertThat(read("lastId.txt").trim()).isEqualTo("1");
    }

    @Test
    @DisplayName("다시 시작해도 파일에서 명언을 읽어 목록에 보여 준다")
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
    @DisplayName("수정하면 해당 파일이 갱신되고, 다시 시작해도 수정 내용이 남는다")
    void t4() throws IOException {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                수정?id=1
                현재와 자신을 사랑하라.
                홍길동
                종료
                """);

        assertThat(read("1.json"))
                .contains("\"content\": \"현재와 자신을 사랑하라.\"")
                .contains("\"author\": \"홍길동\"");

        String out = AppTestRunner.run("""
                목록
                종료
                """);

        assertThat(out).contains("1 / 홍길동 / 현재와 자신을 사랑하라.");
    }

    @Test
    @DisplayName("삭제하면 파일도 지우고, 다시 시작해도 다음 번호는 삭제한 번호를 재사용하지 않는다")
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

        assertThat(Files.exists(dir.resolve("2.json"))).isFalse();
        assertThat(Files.exists(dir.resolve("1.json"))).isTrue();

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
                .doesNotContain("2 / 작자미상 / 과거에 집착하지 마라.");
    }

    @Test
    @DisplayName("빌드하면 모든 명언을 배열 형태의 data.json 으로 만들고 안내를 출력한다")
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

        String json = read("data.json").trim();

        assertThat(json).startsWith("[").endsWith("]");
        assertThat(json)
                .contains("\"id\": 1")
                .contains("\"content\": \"명언 1\"")
                .contains("\"author\": \"작가 1\"")
                .contains("\"id\": 2")
                .contains("\"content\": \"명언 2\"");
        assertThat(json.indexOf("\"id\": 1")).isLessThan(json.indexOf("\"id\": 2"));
    }

    @Test
    @DisplayName("따옴표가 들어간 명언도 저장 후 그대로 읽어 온다")
    void t7() {
        AppTestRunner.run("""
                등록
                그는 "할 수 있다" 고 말했다.
                작자미상
                종료
                """);

        String out = AppTestRunner.run("""
                목록
                종료
                """);

        assertThat(out).contains("1 / 작자미상 / 그는 \"할 수 있다\" 고 말했다.");
    }
}
