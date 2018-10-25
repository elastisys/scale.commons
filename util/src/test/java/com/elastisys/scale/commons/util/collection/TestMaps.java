package com.elastisys.scale.commons.util.collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;

public class TestMaps {

    @Test
    public void zeroMappings() {
        Map<Object, Object> map = Maps.of();
        assertThat(map.isEmpty(), is(true));
    }

    @Test
    public void oneMapping() {
        Map<Integer, String> map = Maps.of(1, "1");
        assertThat(map.size(), is(1));
        assertThat(map.get(1), is("1"));
    }

    @Test
    public void twoMappings() {
        Map<Integer, String> map = Maps.of(1, "1", 2, "2");
        assertThat(map.size(), is(2));
        assertThat(map.get(1), is("1"));
        assertThat(map.get(2), is("2"));
    }

    @Test
    public void threeMappings() {
        Map<Integer, String> map = Maps.of(1, "1", 2, "2", 3, "3");
        assertThat(map.size(), is(3));
        assertThat(map.get(1), is("1"));
        assertThat(map.get(2), is("2"));
        assertThat(map.get(3), is("3"));
    }

    @Test
    public void fourMappings() {
        Map<Integer, String> map = Maps.of(1, "1", 2, "2", 3, "3", 4, "4");
        assertThat(map.size(), is(4));
        assertThat(map.get(1), is("1"));
        assertThat(map.get(2), is("2"));
        assertThat(map.get(3), is("3"));
        assertThat(map.get(4), is("4"));
    }

    @Test
    public void fiveMappings() {
        Map<Integer, String> map = Maps.of(1, "1", 2, "2", 3, "3", 4, "4", 5, "5");
        assertThat(map.size(), is(5));
        assertThat(map.get(1), is("1"));
        assertThat(map.get(2), is("2"));
        assertThat(map.get(3), is("3"));
        assertThat(map.get(4), is("4"));
        assertThat(map.get(5), is("5"));
    }

    @Test
    public void sixMappings() {
        Map<Integer, String> map = Maps.of(1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6");
        assertThat(map.size(), is(6));
        assertThat(map.get(1), is("1"));
        assertThat(map.get(2), is("2"));
        assertThat(map.get(3), is("3"));
        assertThat(map.get(4), is("4"));
        assertThat(map.get(5), is("5"));
        assertThat(map.get(6), is("6"));
    }

    @Test
    public void sevenMappings() {
        Map<Integer, String> map = Maps.of(1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6", 7, "7");
        assertThat(map.size(), is(7));
        assertThat(map.get(1), is("1"));
        assertThat(map.get(2), is("2"));
        assertThat(map.get(3), is("3"));
        assertThat(map.get(4), is("4"));
        assertThat(map.get(5), is("5"));
        assertThat(map.get(6), is("6"));
        assertThat(map.get(7), is("7"));
    }

    @Test
    public void eightMappings() {
        Map<Integer, String> map = Maps.of(1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6", 7, "7", 8, "8");
        assertThat(map.size(), is(8));
        assertThat(map.get(1), is("1"));
        assertThat(map.get(2), is("2"));
        assertThat(map.get(3), is("3"));
        assertThat(map.get(4), is("4"));
        assertThat(map.get(5), is("5"));
        assertThat(map.get(6), is("6"));
        assertThat(map.get(7), is("7"));
        assertThat(map.get(8), is("8"));
    }

    @Test
    public void nineMappings() {
        Map<Integer, String> map = Maps.of(1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6", 7, "7", 8, "8", 9, "9");
        assertThat(map.size(), is(9));
        assertThat(map.get(1), is("1"));
        assertThat(map.get(2), is("2"));
        assertThat(map.get(3), is("3"));
        assertThat(map.get(4), is("4"));
        assertThat(map.get(5), is("5"));
        assertThat(map.get(6), is("6"));
        assertThat(map.get(7), is("7"));
        assertThat(map.get(8), is("8"));
        assertThat(map.get(9), is("9"));
    }

    @Test
    public void tenMappings() {
        Map<Integer, String> map = Maps.of(1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6", 7, "7", 8, "8", 9, "9", 10,
                "10");
        assertThat(map.size(), is(10));
        assertThat(map.get(1), is("1"));
        assertThat(map.get(2), is("2"));
        assertThat(map.get(3), is("3"));
        assertThat(map.get(4), is("4"));
        assertThat(map.get(5), is("5"));
        assertThat(map.get(6), is("6"));
        assertThat(map.get(7), is("7"));
        assertThat(map.get(8), is("8"));
        assertThat(map.get(9), is("9"));
        assertThat(map.get(10), is("10"));
    }

}
