package com.back.standard.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Scanner;
import java.util.stream.Stream;

public final class TestUtil {
    private static PrintStream originalOut;
    private static PrintStream captureOut;

    public static Scanner genScanner(String input) {
        return new Scanner(input);
    }

    public static ByteArrayOutputStream setOutToByteArray() {
        if (captureOut != null) throw new IllegalStateException("capture already active");
        originalOut = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        captureOut = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        System.setOut(captureOut);
        return bytes;
    }

    // 폴더와 그 안의 파일을 모두 지운다 (테스트 전용 저장 폴더 초기화용)
    public static void deleteDir(Path dir) {
        if (!Files.exists(dir)) return;

        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void clearSetOutToByteArray() {
        if (captureOut == null) return;
        System.setOut(originalOut);
        captureOut.close();
        captureOut = null;
        originalOut = null;
    }
}
