package org.moskito.javaagent.instrumentations;

import org.configureme.ConfigurationManager;
import org.junit.Test;
import org.moskito.javaagent.config.LoadTimeMonitoringConfig;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:vzhovtiuk@anotheria.net">Vitaliy Zhovtiuk</a>
 *         Date: 11/3/13
 *         Time: 11:21 AM
 *         To change this template use File | Settings | File Templates.
 */
public class LoadTimeMonitoringConfigTest {
	@Test
	public void shouldConfigureMoskitoConfig() throws Exception {
		//given
		final LoadTimeMonitoringConfig loadTimeMonitoringConfig = LoadTimeMonitoringConfig.getInstance();


		//then
		assertNotNull("Should not be null", loadTimeMonitoringConfig);
		assertFalse("Should have excluded classes", loadTimeMonitoringConfig.getClassesToInclude().length == 0);
		assertTrue("Should have included classes config", loadTimeMonitoringConfig.getMonitoringClassConfig().length > 0);
	}

}
