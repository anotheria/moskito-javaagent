package org.moskito.javaagent.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.gson.annotations.SerializedName;
import net.anotheria.util.StringUtils;
import org.configureme.ConfigurationManager;
import org.configureme.annotations.AfterConfiguration;
import org.configureme.annotations.Configure;
import org.configureme.annotations.ConfigureMe;
import org.configureme.annotations.DontConfigure;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:vzhovtiuk@anotheria.net">Vitaliy Zhovtiuk</a>
 *         Date: 10/27/13
 *         Time: 11:56 AM
 *         To change this template use File | Settings | File Templates.
 */
@ConfigureMe(name = "moskito-javaagent-config")
public class JavaAgentConfig {
	/**
	 * App packages property.
	 */
	@DontConfigure
	private static final String APP_PACKAGES_PROPERTY = "applicationPackages";
	/**
	 * Default config.
	 */
	@DontConfigure
	private static final MonitoringClassConfig DEFAULT_CONFIG = new MonitoringClassConfig(true);

	/**
	 * MonitoringDefaultClassConfig configurations.
	 */
	@Configure
	@SerializedName("@monitoringDefaultClassConfig")
	private MonitoringClassConfig[] monitoringDefaultClassConfig;

	/**
	 * MonitoringClassConfig configurations.
	 */
	@Configure
	@SerializedName("@monitoringClassConfig")
	private MonitoringClassConfig[] monitoringClassConfig;

	/**
	 * Classes to be monitored, contains default and configured.
	 */
	@DontConfigure
	private MonitoringClassConfig[] monitoredClasses;
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
	private int moskitoBackendPort = 9450;
	/**
	 * Class config inner clazzNameToConfigurationStorage.
	 */
	@DontConfigure
	private volatile Map<String, MonitoringClassConfig> clazzConfig = new HashMap<>();
	/**
	 * Classes which should be passed to weaving.
	 */
	@DontConfigure
	private volatile Set<String> classesToInclude = new HashSet<>();
	/**
	 * Map class name to {@link MonitoringClassConfig}, for performance improvement.
	 */
	@DontConfigure
	private final ConcurrentMap<String, MonitoringClassConfig> clazzNameToConfigurationStorage = new ConcurrentHashMap<>();

	/**
	 * Configuration point.
	 */
	@AfterConfiguration
	public synchronized void init() {
		Map<String, MonitoringClassConfig> config = new HashMap<>();
		initDefaultMonitoredClasses();
		copyConfiguredMonitoredClasses();
		if (monitoredClasses.length < 1)
			return;
		for (final MonitoringClassConfig cnf : monitoredClasses)
			if (cnf != null)
				for (final String pattern : cnf.getPatterns())
					if (!StringUtils.isEmpty(pattern))
						config.put(pattern, cnf);
		clazzConfig = config;
		classesToInclude = new HashSet<>(clazzConfig.keySet());
		clazzNameToConfigurationStorage.clear();
	}

	/**
	 * Init default monitored classes.
	 */
	private void initDefaultMonitoredClasses() {
		String appPackages = System.getProperty(APP_PACKAGES_PROPERTY);
		if (StringUtils.isEmpty(appPackages) || monitoringDefaultClassConfig == null || monitoringDefaultClassConfig.length < 1){
			monitoredClasses = new MonitoringClassConfig[0];
			return;
		}
		String[] splitted = appPackages.split(",");
		monitoredClasses = new MonitoringClassConfig[splitted.length * monitoringDefaultClassConfig.length];
		int i = 0;
		for (String appPackage : splitted)
			for (MonitoringClassConfig classNamePatternConfig : monitoringDefaultClassConfig) {
				String[] patterns = getDefaultPackagePatterns(appPackage, classNamePatternConfig.getPatterns());
				monitoredClasses[i] = createMonitoringClassConfig(classNamePatternConfig.getSubsystem(), classNamePatternConfig.getCategory(), patterns);
				i++;
			}
	}

	/**
	 * Get default package patterns to be monitored.
	 */
	private String[] getDefaultPackagePatterns(String appPackage, String[] classNamePatterns) {
		String[] patterns = new String[classNamePatterns.length];
		int i = 0;
		for (String pattern: classNamePatterns){
			patterns[i] = appPackage + pattern;
			i++;
		}
		return patterns;
	}

	/**
	 * Copy configured monitored classes.
	 */
	private void copyConfiguredMonitoredClasses() {
		if (monitoringClassConfig == null || monitoringClassConfig.length < 1)
			return;
		MonitoringClassConfig[] oldMonitoredClass = monitoredClasses;
		monitoredClasses = new MonitoringClassConfig[oldMonitoredClass.length + monitoringClassConfig.length];
		System.arraycopy(oldMonitoredClass, 0, monitoredClasses, 0, oldMonitoredClass.length);
		System.arraycopy(monitoringClassConfig, 0, monitoredClasses, oldMonitoredClass.length, monitoringClassConfig.length);
	}

	/**
	 * Return configured {@link JavaAgentConfig} object.
	 *
	 * @return {@link JavaAgentConfig }
	 */
	public static JavaAgentConfig getInstance() {
		return InstanceProvider.CONFIG.getInstance();
	}

	public MonitoringClassConfig[] getMonitoringClassConfig() {
		return monitoringClassConfig;
	}

	public void setMonitoringClassConfig(MonitoringClassConfig[] monitoringClassConfig) {
		this.monitoringClassConfig = monitoringClassConfig;
	}

	public WorkMode getMode() {
		return mode == null ? WorkMode.LOG_ONLY : mode;
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

	public MonitoringClassConfig[] getMonitoringDefaultClassConfig() {
		return monitoringDefaultClassConfig;
	}

	public void setMonitoringDefaultClassConfig(MonitoringClassConfig[] monitoringDefaultClassConfig) {
		this.monitoringDefaultClassConfig = monitoringDefaultClassConfig;
	}

	/**
	 * Resolve {@link MonitoringClassConfig } for incoming type name.
	 *
	 * @param clazzName type name
	 * @return {@link MonitoringClassConfig}
	 */
	public MonitoringClassConfig getMonitoringConfig(final String clazzName) {
		if (clazzConfig == null || clazzConfig.isEmpty())
			return DEFAULT_CONFIG;

		final MonitoringClassConfig cached = clazzNameToConfigurationStorage.get(clazzName);
		if (cached != null)
			return cached;
		MonitoringClassConfig result = DEFAULT_CONFIG;
		for (final Map.Entry<String, MonitoringClassConfig> mcc : clazzConfig.entrySet())
			if (patternMatch(mcc.getKey(), clazzName)) {
				result = mcc.getValue();
				//first found!
				return result;
			}
		clazzNameToConfigurationStorage.put(clazzName, result);
		return result;
	}

	/**
	 * Return {@code true} in case if weaving should be performed, {@code false} otherwise.
	 *
	 * @param clazzName name of the class
	 * @return boolean
	 */
	public boolean shouldPerformWeaving(final String clazzName) {
		if (StringUtils.isEmpty(clazzName))
			return false;
		for (final String data : classesToInclude)
			if (clazzName.matches(data))
				return true;

		return false;
	}


	/**
	 * Return {@code true} in case if class name matches pattern, {@code false} otherwise.
	 *
	 * @param pattern   configured monitoring class pattern
	 * @param className name of the class
	 * @return boolean value
	 */
	private static boolean patternMatch(final String pattern, final String className) {
		return !(StringUtils.isEmpty(className) || StringUtils.isEmpty(pattern)) && className.replace("/", ".").matches(pattern);
	}

	/**
	 * Return {@code true} in case if moskito inspect backend should be enabled.
	 *
	 * @return boolean value
	 */
	public boolean startMoskitoBacked() {
		return startMoskitoBackend && WorkMode.PROFILING == mode;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("LoadTimeMonitoringConfig{");
		sb.append("monitoringDefaultClassConfig=").append(monitoringDefaultClassConfig == null ? "null" : Arrays.asList(monitoringDefaultClassConfig).toString());
		sb.append(", monitoringClassConfig=").append(monitoringClassConfig == null ? "null" : Arrays.asList(monitoringClassConfig).toString());
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
		private JavaAgentConfig instance;

		/**
		 * Constructor.
		 */
		InstanceProvider() {
			instance = new JavaAgentConfig();
			try {
				ConfigurationManager.INSTANCE.configure(instance);
				//CHECKSTYLE:OFF
			} catch (final RuntimeException e) {
				//CHECKSTYLE:ON
				LoggerFactory.getLogger(InstanceProvider.class).error(" failed to confiugre LoadTimeMonitoringConfig, defaults used");
			}
		}

		public JavaAgentConfig getInstance() {
			return instance;
		}
	}


	/**
	 * Monitoring class configuration.
	 */
	public static class MonitoringClassConfig {
		/**
		 * Class/package name patterns which should be tracked with given 'producer,subsystem,category'...
		 */
		@Configure
		private String[] patterns;
		/**
		 * Producer subsystem.
		 */
		@Configure
		private String subsystem;
		/**
		 * Producer category.
		 */
		@Configure
		private String category;
		/**
		 * Defines whether current configuration default, or not.
		 */
		@DontConfigure
		private final boolean defaultConfig;

		/**
		 * Constructor.
		 */
		public MonitoringClassConfig() {
			this(false);
		}

		/**
		 * Constructor.
		 */
		public MonitoringClassConfig(final boolean defaultCnf) {
			this.defaultConfig = defaultCnf;
		}

		public String getSubsystem() {
			return subsystem;
		}

		public void setSubsystem(String subsystem) {
			this.subsystem = subsystem;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public String[] getPatterns() {
			return patterns;
		}

		public void setPatterns(String[] patterns) {
			this.patterns = patterns;
		}

		public boolean isDefaultConfig() {
			return defaultConfig;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("MonitoringClassConfig{");
			sb.append("patterns=").append(Arrays.toString(patterns));
			sb.append(", subsystem='").append(subsystem).append('\'');
			sb.append(", category='").append(category).append('\'');
			sb.append(", defaultConfig='").append(defaultConfig).append('\'');
			sb.append('}');
			return sb.toString();
		}
	}

	/**
	 * Creates MonitoringClassConfig.
	 *
	 * @param subsystem subsystem
	 * @param category  category
	 * @param patterns  patterns
	 * @return MonitoringClassConfig instance
	 */
	private MonitoringClassConfig createMonitoringClassConfig(String subsystem, String category, String[] patterns) {
		MonitoringClassConfig config = new MonitoringClassConfig();
		config.setSubsystem(subsystem);
		config.setCategory(category);
		config.setPatterns(patterns);
		return config;
	}

	/**
	 * Working mode.
	 */
	public enum WorkMode {
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

