package org.moskito.javaagent.config;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Utility cache for regex patterns used to match against class names.
 * Patterns are wrapped by default in WeakReferences due to javaagent usage specific: biggest part of calls
 * expected soon after javaagent start, and after some time the other cache,
 * {@link JavaAgentConfig#clazzNameToConfigurationStorage}, gets filled, so compiled patterns become useless.
 */
public final class PatternCache {
    /** Patterns cache. */
    private static final Map<String, Reference<Pattern>> cache = new ConcurrentHashMap<>();
    /**
     * Get cached or compile new Pattern.
     * @param pattern string representation of pattern.
     * @return Pattern from cache or newly compiled Pattern.
     */
    public static Pattern getPattern(final String pattern) {
        return getPattern(pattern, true);
    }
    /**
     * Get cached or compile new Pattern.
     * @param pattern string representation of pattern.
     * @param useWeakRefs switches type of reference to use when caching: Weak/SoftReference.
     * @return Pattern from cache or newly compiled Pattern.
     */
    public static Pattern getPattern(final String pattern, final boolean useWeakRefs) {
	    Objects.requireNonNull(pattern, "Pattern should not be null!");
	    final Reference<Pattern> ref = cache.get(pattern);
        Pattern result = (ref == null) ? null : ref.get();
        if (result == null) {
            try {
                result = Pattern.compile(pattern);
            } catch (PatternSyntaxException e) {
                //this pattern does not match any text. Just to prevent further attempts to compile wrong pattern.
                result = Pattern.compile("(?!.).");
            } finally {
                cache.put(pattern, useWeakRefs ? new WeakReference<>(result) : new SoftReference<>(result));
            }
        }
        return result;
    }

    private PatternCache(){}

}
