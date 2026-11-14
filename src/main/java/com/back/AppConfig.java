package com.back;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 실행 모드별 설정.
 * <ul>
 *   <li>DB 접속 정보(host·username·password)는 소스에 두지 않고 환경변수(DB_HOST, DB_USERNAME, DB_PASSWORD)
 *       → 프로젝트 루트의 db.properties(git 에 올리지 않음) 순서로 읽는다.</li>
 *   <li>DB 이름은 모드로 정한다: 운영(개발) wiseSaying__dev, 테스트 wiseSaying__test.</li>
 * </ul>
 */
public final class AppConfig {
    private static final Path DB_PROPERTIES_FILE = Path.of("db.properties");

    private static String mode = "prod";
    private static Properties dbProperties;

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

    // 파일 저장소·빌드 결과 폴더: 운영은 db/wiseSaying, 테스트는 db/test/wiseSaying
    public static Path getWiseSayingDbDir() {
        return isTestMode() ? Path.of("db", "test", "wiseSaying") : Path.of("db", "wiseSaying");
    }

    public static String getDbHost() {
        return get("DB_HOST", "db.host", "localhost");
    }

    public static String getDbUsername() {
        return get("DB_USERNAME", "db.username", "root");
    }

    public static String getDbPassword() {
        String password = get("DB_PASSWORD", "db.password", null);

        if (password == null) {
            throw new IllegalStateException("DB 비밀번호가 없습니다. 환경변수 DB_PASSWORD 를 설정하거나 db.properties 를 만드세요 (db.properties.example 참고).");
        }

        return password;
    }

    public static String getDbName() {
        return isTestMode() ? "wiseSaying__test" : "wiseSaying__dev";
    }

    private static String get(String envName, String propertyName, String defaultValue) {
        String fromEnv = System.getenv(envName);
        if (fromEnv != null && !fromEnv.isBlank()) return fromEnv;

        return loadDbProperties().getProperty(propertyName, defaultValue);
    }

    private static synchronized Properties loadDbProperties() {
        if (dbProperties != null) return dbProperties;

        dbProperties = new Properties();

        if (Files.exists(DB_PROPERTIES_FILE)) {
            try (InputStream in = Files.newInputStream(DB_PROPERTIES_FILE)) {
                dbProperties.load(in);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return dbProperties;
    }
}
