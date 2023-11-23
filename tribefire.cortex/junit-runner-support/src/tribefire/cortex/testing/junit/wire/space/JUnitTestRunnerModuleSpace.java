// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.cortex.testing.junit.wire.space;

import java.net.URLClassLoader;

import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.testing.junit.impl.JUnitModuleTestRunner;
import tribefire.cortex.testrunner.api.ModuleTestRunner;
import tribefire.cortex.testrunner.wire.TestRunningContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefirePlatformContract;

/**
 * Let your module's main wire space extend this class to create and register a {@link JUnitModuleTestRunner} for all tests in your module's
 * classpath. No further wiring needed - the wire space's class body can be empty.
 * <p>
 * To get the tests on your module's classpath add a dependency to them in the <tt>pom.xml</tt>.
 * <p>
 * IMPORTANT: For the {@link JUnitModuleTestRunner} to work <b>you must declare all the dependencies of your module as private</b>, by specifying the
 * <code>privateDeps</code> property in the <tt>asset.man</tt>. Not doing so will probably lead to a runtime exception, as the junit runner assumes it
 * was loaded by a {@link URLClassLoader}. Thus your <tt>asset.man</tt> will most likely look like this:
 *
 * <pre>
 * $nature = (TribefireModule=com.braintribe.model.asset.natures.TribefireModule)()
 * .privateDeps=['.*']
 * </pre>
 *
 * @author Neidhart.Orlich
 */
@Managed
public abstract class JUnitTestRunnerModuleSpace implements TribefireModuleContract {

	@Import
	private TestRunningContract testRunning;

	@Import
	private TribefirePlatformContract tfPlatform;

	@Managed
	public JUnitModuleTestRunner testRunner() {
		JUnitModuleTestRunner bean = new JUnitModuleTestRunner();
		bean.setModuleDescriptor(moduleName());
		bean.setTimeoutInMs(testRunnerTimeOut());
		bean.setThreadContextScoping(tfPlatform.threading().contextScoping());
		return bean;
	}

	@Override
	public void onBeforeBinding() {
		ModuleTestRunner testRunner = testRunner();
		testRunning.registry().register(testRunner);
	}

	/**
	 * Set timeout for all tests combined in milliseconds. Disabled per default
	 *
	 * @see JUnitModuleTestRunner#setTimeoutInMs(long)
	 */
	protected long testRunnerTimeOut() {
		return 0;
	}

	public String moduleName() {
		String simpleName = getClass().getSimpleName();
		return StringTools.removeSuffixIfEligible(simpleName, "Space");
	}

}
