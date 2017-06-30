package org.moskito.javaagent.instrumentations;

import static org.junit.Assert.*;
import org.junit.Test;
import org.moskito.javaagent.config.EntryNameResolverType;
import org.moskito.javaagent.config.JavaAgentConfig;

/**
 * Simple test of EntryNameResolverType enum.
 *
 * @author dzhmud
 */
public class EntryNameResolverTypeTest {

	@Test
	public void resolveEntryTest() {
		final Class testClass = EntryNameResolverTypeTest.class;
		final String methodName = "resolveEntryTest";
		EntryNameResolverType resolver = EntryNameResolverType.FULLY_QUALIFIED_CLASS_NAME;
		assertEquals("org.moskito.javaagent.instrumentations.EntryNameResolverTypeTest",
				resolver.resolveEntry(testClass, methodName, JavaAgentConfig.WorkMode.PROFILING));
		assertEquals("org.moskito.javaagent.instrumentations.EntryNameResolverTypeTest#" + methodName,
				resolver.resolveEntry(testClass, methodName, JavaAgentConfig.WorkMode.LOG_ONLY));

		resolver = EntryNameResolverType.CLASS_NAME;
		assertEquals("EntryNameResolverTypeTest",
				resolver.resolveEntry(testClass, methodName, JavaAgentConfig.WorkMode.PROFILING));
		assertEquals("EntryNameResolverTypeTest#" + methodName,
				resolver.resolveEntry(testClass, methodName, JavaAgentConfig.WorkMode.LOG_ONLY));

		resolver = EntryNameResolverType.ABBREVIATION;
		assertEquals("o.m.j.i.EntryNameResolverTypeTest",
				resolver.resolveEntry(testClass, methodName, JavaAgentConfig.WorkMode.PROFILING));
		assertEquals("o.m.j.i.EntryNameResolverTypeTest#" + methodName,
				resolver.resolveEntry(testClass, methodName, JavaAgentConfig.WorkMode.LOG_ONLY));
	}

}
