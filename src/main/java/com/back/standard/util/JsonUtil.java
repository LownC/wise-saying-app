package com.back.standard.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 라이브러리 없이 평평한(중첩 없는) JSON 객체를 만들고 읽는 최소한의 유틸.
 * 값은 숫자·문자열만 다룬다.
 */
public final class JsonUtil {
    private JsonUtil() {
    }

    // {"id": 1, "content": "..."} 형태를 2칸 들여쓰기로 만든다
    public static String toJson(Map<String, Object> map) {
        return toJson(map, "");
    }

    public static String toJsonArray(List<Map<String, Object>> maps) {
        if (maps.isEmpty()) return "[]";

        return maps.stream()
                .map(map -> "  " + toJson(map, "  "))
                .collect(Collectors.joining(",\n", "[\n", "\n]"));
    }

    private static String toJson(Map<String, Object> map, String indent) {
        return map.entrySet().stream()
                .map(e -> indent + "  \"" + escape(e.getKey()) + "\": " + toJsonValue(e.getValue()))
                .collect(Collectors.joining(",\n", "{\n", "\n" + indent + "}"));
    }

    private static String toJsonValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        return "\"" + escape(value.toString()) + "\"";
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();

        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }

        return sb.toString();
    }

    // 평평한 JSON 객체를 읽어 키 → 값(문자열) Map 으로 돌려준다. 숫자도 문자열로 담는다.
    public static Map<String, String> parseObject(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        int i = json.indexOf('{') + 1;

        while (i < json.length()) {
            i = skipBlank(json, i);
            if (i >= json.length() || json.charAt(i) == '}') break;
            if (json.charAt(i) == ',') {
                i++;
                continue;
            }

            // 키
            StringBuilder key = new StringBuilder();
            i = readString(json, i, key);
            i = skipBlank(json, i);
            i++; // ':'
            i = skipBlank(json, i);

            // 값
            StringBuilder value = new StringBuilder();
            if (json.charAt(i) == '"') {
                i = readString(json, i, value);
            } else {
                while (i < json.length() && ",}".indexOf(json.charAt(i)) == -1 && !Character.isWhitespace(json.charAt(i))) {
                    value.append(json.charAt(i++));
                }
            }

            result.put(key.toString(), value.toString());
        }

        return result;
    }

    private static int skipBlank(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        return i;
    }

    // s[i] 가 여는 따옴표일 때 문자열을 읽어 out 에 담고, 닫는 따옴표 다음 위치를 돌려준다
    private static int readString(String s, int i, StringBuilder out) {
        i++; // 여는 따옴표

        while (i < s.length() && s.charAt(i) != '"') {
            char c = s.charAt(i);

            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    default -> out.append(next);
                }
            } else {
                out.append(c);
            }

            i++;
        }

        return i + 1; // 닫는 따옴표 다음
    }
}
