package org.moskito.javaagent.config;

import java.util.Arrays;

import net.anotheria.util.StringUtils;
import org.configureme.annotations.Configure;

/**
 * Defines classes scope, which should be monitored.
 *
 * @author <a href="mailto:vzhovtiuk@anotheria.net">Vitaliy Zhovtiuk</a>
 *         Date: 10/27/13
 *         Time: 11:56 AM
 *         To change this template use File | Settings | File Templates.
 */
public class MonitoringClassConfig {
	/**
     * Class/package name patterns which should be tracked with given 'producer,subsystem,category'...
     */
    @Configure
    private String[] patterns;
	/**
     * Producer identifier.
     */
    @Configure
    private String producerId;
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

    public boolean patternMatch(final String className) {
        if(StringUtils.isEmpty(className) || patterns==null)
            return false;

        for (final String classToExclude : patterns)
            if (className.replace("/", ".").matches(classToExclude))
                return true;

        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MonitoringClassConfig{");
        sb.append("patterns=").append(Arrays.toString(patterns));
        sb.append(", producerId='").append(producerId).append('\'');
        sb.append(", subsystem='").append(subsystem).append('\'');
        sb.append(", category='").append(category).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
