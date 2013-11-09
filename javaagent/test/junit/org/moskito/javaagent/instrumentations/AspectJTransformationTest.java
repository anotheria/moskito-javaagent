package org.moskito.javaagent.instrumentations;

import net.anotheria.moskito.core.producers.IStatsProducer;
import net.anotheria.moskito.core.registry.ProducerRegistryFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.moskito.javaagent.instrumentations.sample.EchoTestWithoutMonitoring;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

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
    @Ignore
    @Test
    public void shouldCallAdviceAdapter() throws Exception {
        final Class classToTransform = EchoTestWithoutMonitoring.class;
        ClassFileTransformer classFileTransformer = new org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter();
        System.setProperty("org.aspectj.weaver.loadtime.configuration", "aop.xml");
        class ByteArrayClassLoader extends ClassLoader {
            public Class defineClass(String name, byte[] b) {
                return defineClass(name, b, 0, b.length);
            }
        }
        ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader();


        final byte[] transformedBytes = classFileTransformer.transform(byteArrayClassLoader, classToTransform.getName() , null, null,  getByteArrayFromClass(classToTransform));


        final Class transformedClass = byteArrayClassLoader.defineClass(classToTransform.getName(), transformedBytes);
        Object echoService = transformedClass.newInstance();
        try {
            Method method = transformedClass.getMethod("echo", Long.TYPE);
            Long value = (Long) method.invoke(echoService, 12L);
            assertEquals("Result of called invoked ", (Long) 18L, value);
        } catch (SecurityException e) {
            fail("No method given " + e.getMessage());
        } catch (NoSuchMethodException e) {
            fail("No method given " + e.getMessage());
        }
        assertFalse("Should not be empty", ProducerRegistryFactory.getProducerRegistryInstance().getProducers().isEmpty());
        IStatsProducer producer = ProducerRegistryFactory.getProducerRegistryInstance().getProducer("EchoTestWithoutMonitoring");
        assertEquals("Should be annotated category ", "annotated", producer.getCategory());
        assertEquals("Should be default subsystem ", "default", producer.getSubsystem());

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
