package org.moskito.javaagent.config;

import com.google.gson.annotations.SerializedName;
import org.configureme.annotations.Configure;
import org.configureme.annotations.ConfigureMe;

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
    @Configure
    @SerializedName("@monitoringClassConfig")
    private MonitoringClassConfig[] monitoringClassConfig;
    @Configure
    private String[] classesToInclude;

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

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("LoadTimeMonitoringConfig{");
        sb.append("monitoringClassConfig=").append(monitoringClassConfig == null ? "null" : Arrays.asList(monitoringClassConfig).toString());
        sb.append(", classesToInclude=").append(classesToInclude == null ? "null" : Arrays.asList(classesToInclude).toString());
        sb.append('}');
        return sb.toString();
    }
}

