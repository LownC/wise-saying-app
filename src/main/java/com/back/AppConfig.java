package com.back;

import java.nio.file.Path;

// 실행 모드별 설정. 테스트는 setTestMode() 로 실제 db/ 가 아닌 테스트 전용 폴더를 쓴다.
public final class AppConfig {
    private static String mode = "prod";

    private AppConfig() {
    }

    public static void setProdMode() {
        mode = "prod";
    }

    public static void setTestMode() {
        mode = "test";
    }

    public static boolean isTestMode() {
        return mode.equals("test");
    }

    // 명언 파일 저장 폴더: 운영은 db/wiseSaying, 테스트는 db/test/wiseSaying
    public static Path getWiseSayingDbDir() {
        return isTestMode() ? Path.of("db", "test", "wiseSaying") : Path.of("db", "wiseSaying");
    }
}
