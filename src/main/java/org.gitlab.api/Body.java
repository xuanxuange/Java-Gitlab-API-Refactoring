package org.gitlab.api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to construct a HTTP request body in JSON format.
 */
class Body {
    /**
     * The formatter specially for the Gitlab API format
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateUtil.DATE_FORMATTER;
    /**
     * The internal map
     */
    private final Map<String, Object> map = new HashMap<>();

    /**
     * put a {@link String} key with a {@code int} value in the body
     *
     * @param key   - the key
     * @param value - the value
     * @return this Body
     */
    Body putInt(String key, int value) {
        map.put(key, value);
        return this;
    }

    /**
     * put a {@link String} key with a {@link LocalDate} value in the body
     *
     * @param key   - the key
     * @param value - the value
     * @return this Body
     */
    Body putDate(String key, LocalDate value) {
        map.put(key, value == null ? null : value.format(DATE_FORMATTER));
        return this;
    }

    /**
     * put a {@link String} key with a {@link String} value in the body
     *
     * @param key   - the key
     * @param value - the value
     * @return this Body
     */
    Body putString(String key, String value) {
        map.put(key, value);
        return this;
    }

    /**
     * put a {@link String} key with a {@code boolean} value in the body
     *
     * @param key   - the key
     * @param value - the value
     * @return this Body
     */
    Body putBoolean(String key, boolean value) {
        map.put(key, value);
        return this;
    }

    /**
     * put a {@link String} key with a {@code int[]} value in the body
     *
     * @param key   - the key
     * @param value - the value
     * @return this Body
     */
    Body putIntArray(String key, int[] value) {
        map.put(key, value);
        return this;
    }

    /**
     * put a {@link String} key with a {@code List<Integer>} value in the body
     *
     * @param key   - the key
     * @param value - the value
     * @return this Body
     */
    Body putIntArray(String key, List<Integer> value) {
        map.put(key, value.stream().mapToInt(i -> i).toArray());
        return this;
    }

    /**
     * put a {@link String} key with a {@code String[]} value in the body
     *
     * @param key   - the key
     * @param value - the value
     * @return this Body
     */
    Body putStringArray(String key, String[] value) {
        map.put(key, value);
        return this;
    }

    /**
     * put a {@link String} key with a {@code List<String>} value in the body
     *
     * @param key   - the key
     * @param value - the value
     * @return this Body
     */
    Body putStringArray(String key, List<String> value) {
        map.put(key, value.toArray(new String[0]));
        return this;
    }

    /**
     * Get the underlying unmodifiable map of this body
     *
     * @return the underlying unmodifiable map of this body
     */
    Map<String, Object> getMap() {
        return Collections.unmodifiableMap(map);
    }

    /**
     * The string representation of the body
     *
     * @return the string representation of the body
     */
    @Override
    public String toString() {
        return map.toString();
    }

}
