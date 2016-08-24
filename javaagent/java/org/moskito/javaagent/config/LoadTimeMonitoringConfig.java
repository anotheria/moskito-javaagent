package org.moskito.javaagent.config;

import com.google.gson.annotations.SerializedName;
import org.configureme.ConfigurationManager;
import org.configureme.annotations.Configure;
import org.configureme.annotations.ConfigureMe;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:vzhovtiuk@anotheria.net">Vitaliy Zhovtiuk</a>
 *         Date: 10/27/13
 *         Time: 11:56 AM
 *         To change this template use File | Settings | File Templates.
 */
@ConfigureMe(name = "moskito-lt")
public class LoadTimeMonitoringConfig {
	/**
     * MonitoringClassConfig configurations.
     */
    @Configure
    @SerializedName("@monitoringClassConfig")
    private MonitoringClassConfig[] monitoringClassConfig;
	/**
     * Classes prefixes, which should be passed to weaver. E.G [test.*, javax.*, com.MyClass] - so on.
     */
    @Configure
    private String[] classesToInclude;
	/**
     * Work mode with default init.
     */
    @Configure
    private WorkMode mode = WorkMode.LOG_ONLY;
	/**
     * Allow to start moskito inspect backend.
     */
    @Configure
    private boolean startMoskitoBackend = false;
	/**
     * Moskito backend registry port.
     */
    @Configure
    private int moskitoBackendPort = 10000;

	/**
     * Return configured {@link LoadTimeMonitoringConfig} object.
     * @return {@link LoadTimeMonitoringConfig }
     */
    public static LoadTimeMonitoringConfig getInstance(){
        return InstanceProvider.CONFIG.getInstance();
    }

    public MonitoringClassConfig[] getMonitoringClassConfig() {
        return monitoringClassConfig;
    }

    public void setMonitoringClassConfig(MonitoringClassConfig[] monitoringClassConfig) {
        this.monitoringClassConfig = monitoringClassConfig;
    }

    public String[] getClassesToInclude() {
        return classesToInclude;
    }

    public void setClassesToInclude(String[] classesToInclude) {
        this.classesToInclude = classesToInclude;
    }

    public WorkMode getMode() {
        return mode==null ? WorkMode.LOG_ONLY : mode;
    }

    public void setMode(WorkMode mode) {
        this.mode = mode;
    }


    public void setStartMoskitoBackend(boolean startMoskitoBackend) {
        this.startMoskitoBackend = startMoskitoBackend;
    }

    public int getMoskitoBackendPort() {
        return moskitoBackendPort;
    }

    public void setMoskitoBackendPort(int moskitoBackendPort) {
        this.moskitoBackendPort = moskitoBackendPort;
    }

	/**
	 * Return {@code true} in case if moskito inspect backend should be enabled.
     * @return boolean value
     */
    public boolean startMoskitoBacked(){
        return startMoskitoBackend && WorkMode.PROFILING == mode;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("LoadTimeMonitoringConfig{");
        sb.append("monitoringClassConfig=").append(monitoringClassConfig == null ? "null" : Arrays.asList(monitoringClassConfig).toString());
        sb.append(", classesToInclude=").append(classesToInclude == null ? "null" : Arrays.asList(classesToInclude).toString());
        sb.append(", mode=").append(mode);
        sb.append(", startMoskitoBackend=").append(startMoskitoBackend);
        sb.append(", moskitoBackendPort=").append(moskitoBackendPort);
        sb.append('}');
        return sb.toString();
    }

	/**
	 * Instance holder.
     */
    private enum InstanceProvider {
		/**
         * Instance variable.
         */
        CONFIG;
		/**
         * LoadTimeMonitoringConfig instance.
         */
        private LoadTimeMonitoringConfig instance;

		/**
         * Constructor.
         */
        InstanceProvider() {
            instance = new LoadTimeMonitoringConfig();
            try {
                ConfigurationManager.INSTANCE.configure(instance);
            }catch (final RuntimeException e){
                LoggerFactory.getLogger(InstanceProvider.class).error(" failed to confiugre LoadTimeMonitoringConfig, defaults used");
            }
        }

        public LoadTimeMonitoringConfig getInstance() {
            return instance;
        }
    }

	/**
     * Working mode.
     */
    public enum WorkMode{
		/**
         * Log only mode.
         */
        LOG_ONLY,
		/**
         * Monitoring mode.
         */
        PROFILING
    }
}

