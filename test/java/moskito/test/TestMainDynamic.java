package moskito.test;

import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:vzhovtiuk@anotheria.net">Vitaliy Zhovtiuk</a>
 *         Date: 10/17/13
 *         Time: 8:47 PM
 *         To change this template use File | Settings | File Templates.
 */
public class TestMainDynamic {
    static final Logger logger = Logger.getLogger(TestMainDynamic.class);

    static {
        MyJavaAgentLoader.loadAgent();
    }

    /**
     * Main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        logger.info("main method invoked with args: {}" +  Arrays.asList(args));
        logger.info("userName: {}");
    }

}
