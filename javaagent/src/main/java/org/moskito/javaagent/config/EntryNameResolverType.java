package org.moskito.javaagent.config;

import ch.qos.logback.classic.pattern.Abbreviator;
import ch.qos.logback.classic.pattern.LoggerConverter;
import ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.LoggerFactory;

/**
 * Defines common ways of producer id resolving.
 *
 * @author sshscp
 */
public enum EntryNameResolverType {
	/**
	 * Fully qualified class name as producer id, may contains "#methodName" in scope of {@link JavaAgentConfig.WorkMode#LOG_ONLY} mode.
	 */
	FULLY_QUALIFIED_CLASS_NAME {
		@Override
		public String resolveEntry(final Class<?> clazz, final String methodName, final JavaAgentConfig.WorkMode mode) {
			return getOriginal(clazz.getName(), methodName, mode);
		}
	},
	/**
	 * Just class name as producer id, may contains "#methodName" in scope of {@link JavaAgentConfig.WorkMode#LOG_ONLY} mode.
	 */
	CLASS_NAME {
		@Override
		public String resolveEntry(final Class<?> clazz, final String methodName, final JavaAgentConfig.WorkMode mode) {
			return getOriginal(clazz.getSimpleName(), methodName, mode);
		}
	},
	/**
	 * Logback-classic {@link TargetLengthBasedClassNameAbbreviator} usage, with 'abbreviationTargetLength' as target length.
	 * May contains "#methodName" in scope of {@link JavaAgentConfig.WorkMode#LOG_ONLY} mode.
	 */
	ABBREVIATION {
		/**
		 * Default 'abbreviationTargetLength'.
		 */
		private final int abbreviationTargetLength = 1;
		/**
		 * {@link Abbreviator} for class#method names compaction.
		 */
		private final Abbreviator nameAbbreviator = new TargetLengthBasedClassNameAbbreviator(abbreviationTargetLength);

		@Override
		public String resolveEntry(final Class<?> clazz, final String methodName, final JavaAgentConfig.WorkMode mode) {
			final String original = getOriginal(clazz.getName(), methodName, mode);
			if (net.anotheria.util.StringUtils.isEmpty(original)) {
				LoggerFactory.getLogger(EntryNameResolverType.class).warn("Resolve entry class[" + clazz + "] , method [" + methodName + "] returned result[" + original + "]");
				return original;
			}
			return nameAbbreviator.abbreviate(original);
		}
	};

	/**
	 * Resolve producer name or log entry name, depending on configuration passed as 'configuration' param.
	 *
	 * @param pjp
	 * 		{@link ProceedingJoinPoint}
	 * @param configuration
	 * 		{@link JavaAgentConfig.MonitoringClassConfig}
	 * @param mode
	 * 		{@link JavaAgentConfig.WorkMode}
	 * @return resolved entry name
	 */
	public static String resolve(final ProceedingJoinPoint pjp, final JavaAgentConfig.MonitoringClassConfig configuration, final JavaAgentConfig.WorkMode mode) {
		if (pjp == null)
			throw new IllegalArgumentException("pjp can't be null");
		if (configuration == null)
			throw new IllegalArgumentException("configuration can't be null");
		if (mode == null)
			throw new IllegalArgumentException("mode can't be null");

		return configuration.getNameResolverType().resolveEntry(pjp.getSignature().getDeclaringType(), pjp.getSignature().getName(), mode);
	}

	/**
	 * Build producer identifier.
	 *
	 * @param clazz
	 * 		{@link Class} of the entry
	 * @param methodName
	 * 		method name of the entry
	 * @param mode
	 * 		{@link JavaAgentConfig.WorkMode}
	 * @return producer identifier
	 */
	public abstract String resolveEntry(final Class<?> clazz, final String methodName, final JavaAgentConfig.WorkMode mode);


	/**
	 * Build original entry identifier, based on incoming params.
	 *
	 * @param clazz
	 * 		class name
	 * @param method
	 * 		method name
	 * @param mode
	 * 		work mode
	 * @return original entry
	 */
	protected String getOriginal(final String clazz, final String method, final JavaAgentConfig.WorkMode mode) {
		switch (mode) {
			case LOG_ONLY:
				return clazz + "#" + method;
			case PROFILING:
				return clazz;
			default:
				throw new AssertionError("mode " + mode + " not supported");
		}
	}
}
