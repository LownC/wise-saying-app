package com.back;

import java.util.HashMap;
import java.util.Map;

public class Rq {
    private final String actionName;
    private final Map<String, String> params = new HashMap<>();

    public Rq(String cmd) {
        String[] cmdBits = cmd.trim().split("\\?", 2);
        actionName = cmdBits[0].trim();

        if (cmdBits.length < 2) return;

        for (String queryParam : cmdBits[1].split("&")) {
            String[] keyValue = queryParam.split("=", 2);
            String key = keyValue[0].trim();
            if (key.isEmpty() || keyValue.length < 2) continue;

            String value = keyValue[1].trim();
            if (value.isEmpty()) continue;

            params.put(key, value);
        }
    }

    public String getActionName() {
        return actionName;
    }

    public String getParam(String paramName, String defaultValue) {
        return params.getOrDefault(paramName, defaultValue);
    }

    public int getParamAsInt(String paramName, int defaultValue) {
        String value = params.get(paramName);
        if (value == null) return defaultValue;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
