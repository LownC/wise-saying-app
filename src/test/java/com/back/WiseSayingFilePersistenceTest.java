package com.back;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.domain.wiseSaying.repository.WiseSayingFileRepository;
import com.back.standard.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

// ⑦단계 파일 저장소(WiseSayingFileRepository) 단위 테스트.
// 앱은 ⑧단계부터 MySQL 저장소를 쓰므로, 파일 저장소는 리포지터리를 직접 만들어 검증한다.
// 테스트 전용 폴더(db/test/wiseSaying)를 쓰고 매 테스트 전에 비운다. 새 리포지터리 객체 = 프로그램 재시작.
class WiseSayingFilePersistenceTest {
    private Path dir;

    @BeforeEach
    void beforeEach() {
        AppConfig.setTestMode();
        dir = AppConfig.getWiseSayingDbDir();
        TestUtil.deleteDir(dir);
    }

    private WiseSayingFileRepository newRepository() {
        return new WiseSayingFileRepository(dir);
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
    @DisplayName("저장하면 {번호}.json 에 id·content·author 를 쓰고 lastId.txt 를 갱신한다")
    void t2() throws IOException {
        newRepository().save(new WiseSaying("현재를 사랑하라.", "작자미상"));

        assertThat(read("1.json"))
                .contains("\"id\": 1")
                .contains("\"content\": \"현재를 사랑하라.\"")
                .contains("\"author\": \"작자미상\"");
        assertThat(read("lastId.txt").trim()).isEqualTo("1");
    }

    @Test
    @DisplayName("다시 시작해도 파일에서 명언을 최신순으로 읽어 온다")
    void t3() {
        WiseSayingFileRepository repository = newRepository();
        repository.save(new WiseSaying("현재를 사랑하라.", "작자미상"));
        repository.save(new WiseSaying("과거에 집착하지 마라.", "작자미상"));

        assertThat(newRepository().findAll())
                .extracting(WiseSaying::getId, WiseSaying::getContent)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(2, "과거에 집착하지 마라."),
                        org.assertj.core.groups.Tuple.tuple(1, "현재를 사랑하라."));
    }

    @Test
    @DisplayName("수정하면 해당 파일이 갱신된다")
    void t4() throws IOException {
        WiseSayingFileRepository repository = newRepository();
        WiseSaying wiseSaying = repository.save(new WiseSaying("현재를 사랑하라.", "작자미상"));

        wiseSaying.setContent("현재와 자신을 사랑하라.");
        wiseSaying.setAuthor("홍길동");
        repository.save(wiseSaying);

        assertThat(read("1.json"))
                .contains("\"content\": \"현재와 자신을 사랑하라.\"")
                .contains("\"author\": \"홍길동\"");
    }

    @Test
    @DisplayName("삭제하면 파일도 지우고, 다시 시작해도 삭제한 번호를 재사용하지 않는다")
    void t5() {
        WiseSayingFileRepository repository = newRepository();
        repository.save(new WiseSaying("현재를 사랑하라.", "작자미상"));
        repository.save(new WiseSaying("과거에 집착하지 마라.", "작자미상"));
        repository.deleteById(2);

        assertThat(Files.exists(dir.resolve("2.json"))).isFalse();

        WiseSaying saved = newRepository().save(new WiseSaying("새로운 명언", "새 작가"));

        assertThat(saved.getId()).isEqualTo(3);
    }

    @Test
    @DisplayName("빌드하면 모든 명언을 번호 오름차순 배열로 data.json 에 쓴다")
    void t6() throws IOException {
        WiseSayingFileRepository repository = newRepository();
        repository.save(new WiseSaying("명언 1", "작가 1"));
        repository.save(new WiseSaying("명언 2", "작가 2"));
        repository.build();

        String json = read("data.json").trim();

        assertThat(json).startsWith("[").endsWith("]");
        assertThat(json).contains("\"id\": 1", "\"content\": \"명언 1\"", "\"id\": 2");
        assertThat(json.indexOf("\"id\": 1")).isLessThan(json.indexOf("\"id\": 2"));
    }

    @Test
    @DisplayName("따옴표가 들어간 명언도 저장 후 그대로 읽어 온다")
    void t7() {
        newRepository().save(new WiseSaying("그는 \"할 수 있다\" 고 말했다.", "작자미상"));

        assertThat(newRepository().findById(1))
                .hasValueSatisfying(w -> assertThat(w.getContent()).isEqualTo("그는 \"할 수 있다\" 고 말했다."));
    }
}
