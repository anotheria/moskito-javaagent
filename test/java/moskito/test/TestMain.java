package moskito.test;

import net.anotheria.moskito.core.producers.IStatsProducer;
import net.anotheria.moskito.core.registry.ProducerRegistryFactory;

import java.util.Arrays;

/**
 * TODO comment this class
 *
 * @author lrosenberg
 * @since 07.04.13 22:26
 */
public class TestMain {
    public static void main(String a[]) {
        System.out.println("Hello I am Test...");
        TestService service = new TestServiceImpl();

        for (int i = 0; i < 10; i++) {
            System.out.println("Echo " + i + " --> " + service.echo(i));
        }


        Class<TestServiceImpl> clazz = TestServiceImpl.class;
        System.out.println("Fields: " + Arrays.toString(clazz.getFields()));
        System.out.println("Methods: " + Arrays.toString(clazz.getMethods()));
        System.out.println("Should not be empty " + ProducerRegistryFactory.getProducerRegistryInstance().getProducers().isEmpty());
        IStatsProducer producer = ProducerRegistryFactory.getProducerRegistryInstance().getProducer("TestMain");
        System.out.println("Should be annotated category " + "annotated".equals(producer.getCategory()));
        System.out.println("Should be default subsystem " + "default".equals(producer.getSubsystem()));
    }

    public void test(int i) {
        System.out.println("Test " + i);
    }
}
