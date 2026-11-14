package com.back;

import com.back.standard.util.TestUtil;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Consumer;

public final class AppTestRunner {
    public static String run(String input, Consumer<Scanner> runApp) {
        var bytes = TestUtil.setOutToByteArray();
        try (Scanner scanner = TestUtil.genScanner(input)) {
            runApp.accept(scanner);
            System.out.flush();
            return bytes.toString(StandardCharsets.UTF_8);
        } finally {
            TestUtil.clearSetOutToByteArray();
        }
    }

    public static String run(String input) {
        return run(input, scanner -> new App(scanner).run());
    }

    // 실제 db/ 대신 테스트 전용 폴더(db/test/wiseSaying)를 쓰도록 바꾸고 비운다. 각 테스트 전에 호출한다.
    public static void resetTestDb() {
        AppConfig.setTestMode();
        TestUtil.deleteDir(AppConfig.getWiseSayingDbDir());
    }
}
