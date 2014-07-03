package com.elastisys.scale.commons.util.strings;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

/**
 * Verifies the behavior of the {@link StringUtils} class.
 * 
 * 
 */
public class TestStringUtils {

	@Test
	public void testReplaceAll() {
		// no replacements
		Map<String, String> substitutions = ImmutableMap.of();
		String replaced = StringUtils.replaceAll(
				"a string with ${var1} missing ${var2}", substitutions);
		assertThat(replaced, is("a string with ${var1} missing ${var2}"));

		// one matching replacement
		substitutions = ImmutableMap.of(//
				"${var1}", "several");
		replaced = StringUtils.replaceAll(
				"a string with ${var1} missing ${var2}", substitutions);
		assertThat(replaced, is("a string with several missing ${var2}"));

		// one substitution matching, one substitution not matching
		substitutions = ImmutableMap.of(//
				"${var1}", "several", //
				"${var3}", "no match");
		replaced = StringUtils.replaceAll(
				"a string with ${var1} missing ${var2}", substitutions);
		assertThat(replaced, is("a string with several missing ${var2}"));

		// two substitutions matching
		substitutions = ImmutableMap.of(//
				"${var1}", "several", //
				"${var2}", "values");
		replaced = StringUtils.replaceAll(
				"a string with ${var1} missing ${var2}", substitutions);
		assertThat(replaced, is("a string with several missing values"));
	}

	/**
	 * Verifies the behavior of the {@link StringUtils#prepend(String)}
	 * {@link Function}.
	 */
	@Test
	public void testStringPrependFunction() {
		assertThat(StringUtils.prepend("abc").apply("def"), is("abcdef"));
		assertThat(StringUtils.prepend("").apply("def"), is("def"));
		assertThat(StringUtils.prepend("abc").apply(""), is("abc"));

		try {
			StringUtils.prepend(null).apply("def");
			fail("should not be able to prepend null");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
}
