package com.elastisys.scale.commons.util.collection;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convenience class for constructing maps. The returned maps preserves the
 * insertion order on iteration.
 *
 */
public class Maps {

    /**
     * Returns a map containing zero mappings.
     *
     * @return
     */
    public static <K, V> Map<K, V> of() {
        return new LinkedHashMap<>();
    }

    /**
     * Returns a map containing one mapping.
     *
     * @return
     */
    public static <K, V> Map<K, V> of(K key1, V value1) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(key1, value1);
        return map;
    }

    /**
     * Returns a map containing two mappings.
     *
     * @return
     */
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    /**
     * Returns a map containing three mappings.
     *
     * @return
     */
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    /**
     * Returns a map containing four mappings.
     *
     * @return
     */
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        return map;
    }

    /**
     * Returns a map containing five mappings.
     *
     * @return
     */
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5,
            V value5) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        return map;
    }

    /**
     * Returns a map containing six mappings.
     *
     * @return
     */
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5,
            V value5, K key6, V value6) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        return map;
    }

    /**
     * Returns a map containing seven mappings.
     *
     * @return
     */
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5,
            V value5, K key6, V value6, K key7, V value7) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        map.put(key7, value7);
        return map;
    }

    /**
     * Returns a map containing eight mappings.
     *
     * @return
     */
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5,
            V value5, K key6, V value6, K key7, V value7, K key8, V value8) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        map.put(key7, value7);
        map.put(key8, value8);
        return map;
    }

    /**
     * Returns a map containing nine mappings.
     *
     * @return
     */
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5,
            V value5, K key6, V value6, K key7, V value7, K key8, V value8, K key9, V value9) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        map.put(key7, value7);
        map.put(key8, value8);
        map.put(key9, value9);
        return map;
    }

    /**
     * Returns a map containing ten mappings.
     *
     * @return
     */
    public static <K, V> Map<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5,
            V value5, K key6, V value6, K key7, V value7, K key8, V value8, K key9, V value9, K key10, V value10) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        map.put(key7, value7);
        map.put(key8, value8);
        map.put(key9, value9);
        map.put(key10, value10);
        return map;
    }

}
