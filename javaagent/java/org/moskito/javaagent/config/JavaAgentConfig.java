package org.moskito.javaagent.config;

import com.google.gson.annotations.SerializedName;
import net.anotheria.util.StringUtils;
import org.configureme.ConfigurationManager;
import org.configureme.annotations.AfterConfiguration;
import org.configureme.annotations.Configure;
import org.configureme.annotations.ConfigureMe;
import org.configureme.annotations.DontConfigure;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:vzhovtiuk@anotheria.net">Vitaliy Zhovtiuk</a>
 *         Date: 10/27/13
 *         Time: 11:56 AM
 *         To change this template use File | Settings | File Templates.
 */
@ConfigureMe (name = "moskito-javaagent-config")
public class JavaAgentConfig {
	/**
	 * Default config.
	 */
	@DontConfigure
	private static final MonitoringClassConfig DEFAULT_CONFIG = new MonitoringClassConfig(true);
	/**
	 * MonitoringClassConfig configurations.
	 */
	@Configure
	@SerializedName ("@monitoringClassConfig")
	private MonitoringClassConfig[] monitoringClassConfig;
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
	private int moskitoBackendPort = 11000;
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

	@AfterConfiguration
	public synchronized void init() {
		Map<String, MonitoringClassConfig> config = new HashMap<>();
		if (monitoringClassConfig == null || monitoringClassConfig.length <= 0)
			return;
		for (final MonitoringClassConfig cnf : monitoringClassConfig)
			if (cnf != null)
				for (final String pattern : cnf.getPatterns())
					if (!StringUtils.isEmpty(pattern))
						config.put(pattern, cnf);
		clazzConfig = config;
		classesToInclude = new HashSet<>(clazzConfig.keySet());
		clazzNameToConfigurationStorage.clear();
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

	/**
	 * Resolve {@link MonitoringClassConfig } for incoming type name.
	 *
	 * @param clazzName
	 * 		type name
	 * @return {@link MonitoringClassConfig}
	 */
	public MonitoringClassConfig getMonitoringConfig(final String clazzName) {
		if (clazzConfig == null || clazzConfig.isEmpty())
			return DEFAULT_CONFIG;

		final MonitoringClassConfig cached = clazzNameToConfigurationStorage.get(clazzName);
		if (cached != null)
			return cached;
		MonitoringClassConfig result = DEFAULT_CONFIG;
		try {
			for (final Map.Entry<String, MonitoringClassConfig> mcc : clazzConfig.entrySet())
				if (patternMatch(mcc.getKey(), clazzName)) {
					result = mcc.getValue();
					//first found!
					return result;
				}

			return result;
		} finally {
			clazzNameToConfigurationStorage.putIfAbsent(clazzName, result);
		}
	}

	/**
	 * Return {@code true} in case if weaving should be performed, {@code false} otherwise.
	 *
	 * @param clazzName
	 * 		name of the class
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
	 * @param pattern
	 * 		configured monitoring class pattern
	 * @param className
	 * 		name of the class
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
		sb.append("monitoringClassConfig=").append(monitoringClassConfig == null ? "null" : Arrays.asList(monitoringClassConfig).toString());
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
			} catch (final RuntimeException e) {
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

