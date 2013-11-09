package org.moskito.javaagent.config;

import org.configureme.annotations.Configure;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:vzhovtiuk@anotheria.net">Vitaliy Zhovtiuk</a>
 *         Date: 10/27/13
 *         Time: 11:56 AM
 *         To change this template use File | Settings | File Templates.
 */
public class MonitoringClassConfig {
    @Configure
    private String[] patterns;
    @Configure
    private String producerId;
    @Configure
    private String subsystem;
    @Configure
    private String category;

    public String getProducerId() {
        return producerId;
    }

    public void setProducerId(String producerId) {
        this.producerId = producerId;
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

    public boolean patternMatch(String className) {
        for (String classToExclude : patterns) {
            if (className.replace("/", ".").matches(classToExclude)) {
                return true;
            }
        }
        return false;
    }
}
