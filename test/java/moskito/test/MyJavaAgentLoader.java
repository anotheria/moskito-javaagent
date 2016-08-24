package moskito.test;

import com.sun.tools.attach.VirtualMachine;

import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:vzhovtiuk@anotheria.net">Vitaliy Zhovtiuk</a>
 *         Date: 10/17/13
 *         Time: 8:45 PM
 *         To change this template use File | Settings | File Templates.
 */
public class MyJavaAgentLoader {
    static final Logger logger = Logger.getLogger(MyJavaAgentLoader.class.getName());

    private static final String jarFilePath = System.getProperty("user.home") + "/.m2/repository/"
            + "org/moskito/javaagent/1.0.0-SNAPSHOT/"
            + "javaagent-1.0.0-SNAPSHOT.jar";

    public static void loadAgent() {
        logger.info("dynamically loading javaagent");
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);

        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(jarFilePath, "");
            // load agent into target VM
            // vm.loadAgent(agent, "com.sun.management.jmxremote.port=5000");
            vm.detach();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
