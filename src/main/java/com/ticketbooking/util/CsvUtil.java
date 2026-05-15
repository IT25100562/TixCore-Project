package com.ticketbooking.util;

public class CsvUtil {
    public static final String DELIMITER = "|";

    public static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("|", "\\p").replace("\n", "\\n").replace("\r", "");
    }

    public static String unescape(String value) {
        if (value == null) return "";
        return value.replace("\\n", "\n").replace("\\p", "|").replace("\\\\", "\\");
    }

    public static String[] split(String line) {
        return line.split("\\|", -1);
    }

    public static String join(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(DELIMITER);
            sb.append(escape(values[i] == null ? "" : values[i].toString()));
        }
        return sb.toString();
    }
}
