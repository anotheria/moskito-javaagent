package org.moskito.javaagent.instrumentations;

import static org.junit.Assert.*;
import org.junit.Test;
import org.moskito.javaagent.config.PatternCache;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Simple test of PatternCache class.
 *
 * @author dzhmud
 */
public class PatternCacheTest {

	@Test
	public void testCornerCases() {
		//check null argument
		try {
			PatternCache.getPattern(null);
			fail("expected NPE");
		} catch (NullPointerException npe) {
			//expected
		}
		//check empty pattern
		final Pattern empty = PatternCache.getPattern("");
		assertTrue(empty.matcher("").matches());
		assertFalse(empty.matcher(" ").matches());
		assertFalse(empty.matcher("fad").matches());
		//check not compilable pattern, should return match-nothing compiled pattern.
		final Pattern wrong = PatternCache.getPattern("([a-s]+");
		assertFalse(wrong.matcher("").matches());
		assertFalse(wrong.matcher(" ").matches());
		assertFalse(wrong.matcher("fad").matches());
	}

	@Test
	public void testCaching() {
		final Pattern empty = PatternCache.getPattern("");
		final Pattern otherCopy = PatternCache.getPattern("");
		if (empty != otherCopy) {
			LoggerFactory.getLogger(PatternCacheTest.class).warn("Maybe cache is not working, check please.");
		}
		//next line should put compiled pattern in cache using SoftReference
		final Pattern one = PatternCache.getPattern("[0-9A-Z]{,8}",false);
		final Pattern two = PatternCache.getPattern("[0-9A-Z]{,8}");
		assertSame("Should be same instance:", one, two);
	}

}
