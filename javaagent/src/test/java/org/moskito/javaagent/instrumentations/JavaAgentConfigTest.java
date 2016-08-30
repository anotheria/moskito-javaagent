package org.moskito.javaagent.instrumentations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.moskito.javaagent.config.JavaAgentConfig;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:vzhovtiuk@anotheria.net">Vitaliy Zhovtiuk</a>
 *         Date: 11/3/13
 *         Time: 11:21 AM
 *         To change this template use File | Settings | File Templates.
 */
public class JavaAgentConfigTest {
	@Test
	public void shouldConfigureMoskitoConfig() throws Exception {
		//given
		final org.moskito.javaagent.config.JavaAgentConfig config = org.moskito.javaagent.config.JavaAgentConfig.getInstance();


		//then
		assertNotNull("Should not be null", config);
		assertTrue("Should have included classes config", config.getMonitoringClassConfig().length > 0);
		final JavaAgentConfig.MonitoringClassConfig[] cConf  = config.getMonitoringClassConfig();

		for(final JavaAgentConfig.MonitoringClassConfig config1 : cConf){
			for(final String str : config1.getPatterns()) {
				org.junit.Assert.assertTrue(config.shouldPerformWeaving(str));
				org.junit.Assert.assertEquals(config1, config.getMonitoringConfig(str));
			}
		}

	}

}
