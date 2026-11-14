package com.back;

import com.back.domain.wiseSaying.repository.WiseSayingDbRepository;
import com.back.standard.simpleDb.SimpleDb;
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

    // 테스트용 DB(wiseSaying__test)와 테스트 전용 폴더(db/test/wiseSaying)를 쓰도록 바꾸고 둘 다 비운다.
    // 각 테스트 전에 호출한다.
    public static void resetTestDb() {
        AppConfig.setTestMode();
        TestUtil.deleteDir(AppConfig.getWiseSayingDbDir());

        SimpleDb simpleDb = AppContext.createSimpleDb();
        try {
            WiseSayingDbRepository repository = new WiseSayingDbRepository(simpleDb, AppConfig.getWiseSayingDbDir().resolve("data.json"));
            repository.createTableIfNotExists();
            repository.truncate();
        } finally {
            simpleDb.close();
        }
    }
}
