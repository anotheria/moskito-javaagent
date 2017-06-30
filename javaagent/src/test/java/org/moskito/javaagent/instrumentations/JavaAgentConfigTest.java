package org.moskito.javaagent.instrumentations;

import ch.qos.logback.classic.pattern.Abbreviator;
import ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator;
import net.anotheria.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.moskito.javaagent.config.JavaAgentConfig;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:vzhovtiuk@anotheria.net">Vitaliy Zhovtiuk</a>
 *         Date: 11/3/13
 *         Time: 11:21 AM
 *         To change this template use File | Settings | File Templates.
 */
public class JavaAgentConfigTest {
	@Test
	public void shouldConfigureMoskitoConfig() throws Exception {
		//given
		final JavaAgentConfig config = JavaAgentConfig.getInstance();

		//then
		assertNotNull("Should not be null", config);
		assertTrue("Should have included classes config", config.getMonitoringClassConfig().length > 0);
		final JavaAgentConfig.MonitoringClassConfig[] cConf = config.getMonitoringClassConfig();

		for (final JavaAgentConfig.MonitoringClassConfig config1 : cConf) {
			for (final String str : config1.getPatterns()) {
				assertTrue(config.shouldPerformWeaving(str));
				assertEquals(config1, config.getMonitoringConfig(str));
			}
		}
	}


	@Test
	public void logbackAbbreviationStabilityTest() {
		final List<String> someDataStrings = Arrays.asList(
				"com.some.maybe.not.some.or.almost.some.DataClass",
				"com.some.maybe.not.some.or.almost.some.DataClass$SubDataClass",
				"com.some.maybe.not.some.or.almost.some.DataClass$SubDataClass$SubSubSub",
				"com.some.maybe.not.some.or.almost.some.DataClass$SubDataClass$SubSubSub#methodName()",
				"com.some.maybe.not.some.or.almost.some.DataClass#methodName()",
				"com.some.maybe.not.some.or.almost.some.DataClass#methodName(....)"
		);
		final int retriesAmount = 1000;
		for (int limit = 0; limit < 10; limit++) {
			final Abbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(limit);
			for (final String data : someDataStrings) {
				final String originallyAbbreviated = abbreviator.abbreviate(data);
				for (int iteration = 0; iteration < retriesAmount; iteration++) {
					final String nextAbbreviation = abbreviator.abbreviate(data);
					assertEquals("not same string on iteration " + iteration, originallyAbbreviated, nextAbbreviation);
				}
			}
		}


	}

	@Test
	public void checkGettersAndSetters() {
		final JavaAgentConfig config = JavaAgentConfig.getInstance();
		final int newPort = 1 + config.getMoskitoBackendPort();
		config.setMoskitoBackendPort(newPort);
		assertEquals(newPort, config.getMoskitoBackendPort());

		for (final JavaAgentConfig.WorkMode mode : JavaAgentConfig.WorkMode.values()) {
			config.setMode(mode);
			assertEquals(mode, config.getMode());
			for (final boolean startBackend : new boolean[]{true, false}) {
				config.setStartMoskitoBackend(startBackend);
				final boolean expectedResult = startBackend && mode == JavaAgentConfig.WorkMode.PROFILING;
				assertEquals(expectedResult, config.startMoskitoBackend());
			}
		}
	}

}
