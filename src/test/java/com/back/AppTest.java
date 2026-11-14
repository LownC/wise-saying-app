package com.back;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {
    @BeforeEach
    void beforeEach() {
        AppTestRunner.resetTestDb();
    }

    @Test
    public void 더하기() {
        assertEquals(30, 30);
    }

    @Test
    @DisplayName("시작하면 '== 명언 앱 ==' 을 출력하고, 종료하면 끝난다")
    void start() {
        String out = AppTestRunner.run("""
                종료
                """);

        assertThat(out)
                .contains("== 명언 앱 ==")
                .contains("명령) ");
    }
}
