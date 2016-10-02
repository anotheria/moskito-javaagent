package org.moskito.javaagent.instrumentations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.moskito.javaagent.instrumentations.sample.EchoTestWithoutMonitoring;
import net.anotheria.moskito.core.producers.IStatsProducer;
import net.anotheria.moskito.core.registry.ProducerRegistryFactory;
import org.junit.Assert;
import org.junit.Test;
import org.moskito.javaagent.AspectTransformationAgent;
import org.moskito.javaagent.config.JavaAgentConfig;
import org.moskito.javaagent.config.JavaAgentConfig.MonitoringClassConfig;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:vzhovtiuk@anotheria.net">Vitaliy Zhovtiuk</a>
 *         Date: 10/19/13
 *         Time: 7:12 PM
 *         To change this template use File | Settings | File Templates.
 *         // Dump mode package and class wildcards classname
 */

public class AspectJTransformationTest {
	@Test
	public void profilingTest() throws Exception {
		// enable monitoring!
		final JavaAgentConfig monitoringConfig = JavaAgentConfig.getInstance();
		monitoringConfig.setMode(JavaAgentConfig.WorkMode.PROFILING);
		final MonitoringClassConfig clazzConfig = new MonitoringClassConfig();
		clazzConfig.setPatterns(new String[] {EchoTestWithoutMonitoring.class.getName()});
		clazzConfig.setCategory("1");
		clazzConfig.setSubsystem("1");
		List<MonitoringClassConfig> allClassConfigs = new ArrayList<>(Arrays.asList(monitoringConfig.getMonitoringClassConfig()));
		allClassConfigs.add(clazzConfig);
		monitoringConfig.setMonitoringClassConfig(allClassConfigs.toArray(new MonitoringClassConfig[allClassConfigs.size()]));
		//reinit
		monitoringConfig.init();

		final Class classToTransform = EchoTestWithoutMonitoring.class;
		ClassFileTransformer classFileTransformer = new AspectTransformationAgent();
		System.setProperty("org.aspectj.weaver.loadtime.configuration", "aop.xml");
		class ByteArrayClassLoader extends ClassLoader {
			public Class defineClass(String name, byte[] b) {
				return defineClass(name, b, 0, b.length);
			}
		}
		ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader();


		final byte[] transformedBytes = classFileTransformer.transform(byteArrayClassLoader, classToTransform.getName(), null, null, getByteArrayFromClass(classToTransform));


		final Class transformedClass = byteArrayClassLoader.defineClass(classToTransform.getName(), transformedBytes);
		final Object echoService = transformedClass.newInstance();
		try {
			@SuppressWarnings ("unchecked")
			Method method = transformedClass.getMethod("echo", Long.TYPE);
			Long value = (Long) method.invoke(echoService, 12L);
			assertEquals("Result of called invoked ", (Long) 18L, value);
		} catch (SecurityException | NoSuchMethodException e) {
			fail(e.getMessage());
		}
		assertFalse("Should not be empty", ProducerRegistryFactory.getProducerRegistryInstance().getProducers().isEmpty());
		final String producerId = clazzConfig.getNameResolverType().resolveEntry(EchoTestWithoutMonitoring.class, "echo", monitoringConfig.getMode());
		IStatsProducer producer = ProducerRegistryFactory.getProducerRegistryInstance().getProducer(producerId);
		Assert.assertNotNull("Producer is NULL", producer);
		assertEquals("Should be annotated category ", clazzConfig.getCategory(), producer.getCategory());
		assertEquals("Should be default subsystem ", clazzConfig.getSubsystem(), producer.getSubsystem());

	}

	@Test
	public void loggingTest() throws Exception {
		// enable logging!
		JavaAgentConfig cnf = JavaAgentConfig.getInstance();
		cnf.setMode(JavaAgentConfig.WorkMode.LOG_ONLY);
		final Class classToTransform = EchoTestWithoutMonitoring.class;
		ClassFileTransformer classFileTransformer = new AspectTransformationAgent();
		System.setProperty("org.aspectj.weaver.loadtime.configuration", "aop.xml");
		class ByteArrayClassLoader extends ClassLoader {
			public Class defineClass(String name, byte[] b) {
				return defineClass(name, b, 0, b.length);
			}
		}
		ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader();


		final byte[] transformedBytes = classFileTransformer.transform(byteArrayClassLoader, classToTransform.getName(), null, null, getByteArrayFromClass(classToTransform));

		final Class transformedClass = byteArrayClassLoader.defineClass(classToTransform.getName(), transformedBytes);
		final Object echoService = transformedClass.newInstance();
		try {
			@SuppressWarnings ("unchecked")
			Method method = transformedClass.getMethod("echo", Long.TYPE);
			Long value = (Long) method.invoke(echoService, 12L);
			assertEquals("Result of called invoked ", (Long) 18L, value);
		} catch (SecurityException | NoSuchMethodException e) {
			fail(e.getMessage());
		}
		assertFalse("Should not be empty", ProducerRegistryFactory.getProducerRegistryInstance().getProducers().isEmpty());
		IStatsProducer producer = ProducerRegistryFactory.getProducerRegistryInstance().getProducer("EchoTestWithoutMonitoring");
		Assert.assertNull("Strange - should be NULL!", producer);

	}


	private String getBytecode(Class aClass) throws Exception {
		final byte[] b1 = getByteArrayFromClass(aClass);
		return getBytecodeFromByteArray(b1);
	}

	private String getBytecodeFromByteArray(byte[] bytes) throws Exception {
		final ClassReader cr = new ClassReader(bytes);
		StringWriter sw = new StringWriter();
		final TraceClassVisitor traceClassVisitor = new TraceClassVisitor(new PrintWriter(sw));
		cr.accept(traceClassVisitor, 0);
		return sw.toString();
	}

	private Class getClassFromByteArray(String name, byte[] bytes) {
		class ByteArrayClassLoader extends ClassLoader {
			public Class defineClass(String name, byte[] b) {
				return defineClass(name, b, 0, b.length);
			}
		}
		ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader();
		return byteArrayClassLoader.defineClass(name, bytes);
	}

	private byte[] getByteArrayFromClass(Class aClass) throws IOException {
		String className = aClass.getName();
		String classAsPath = className.replace('.', '/') + ".class";
		InputStream stream = aClass.getClassLoader().getResourceAsStream(classAsPath);
		return toByteArray(stream);
	}


	private static byte[] toByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();

		return buffer.toByteArray();
	}
}
