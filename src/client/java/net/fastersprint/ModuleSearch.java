package net.fastersprint;

import java.util.Locale;

final class ModuleSearch {
    private ModuleSearch() { }
    static boolean matches(String title, String description, String query) {
        String searchable = (title + " " + description + " " + title.replace(" ", "")).toLowerCase(Locale.ROOT);
        for (String word : query.strip().toLowerCase(Locale.ROOT).split("\\s+")) {
            if (!searchable.contains(word)) return false;
        }
        return true;
    }
    static String append(String query, String addition) {
        StringBuilder result = new StringBuilder(query);
        addition.codePoints().filter(c -> !Character.isISOControl(c)).limit(Math.max(0, 64 - query.codePointCount(0, query.length())))
            .forEach(result::appendCodePoint);
        return result.toString();
    }
    static String backspace(String query) {
        return query.isEmpty() ? query : query.substring(0, query.offsetByCodePoints(query.length(), -1));
    }
}
