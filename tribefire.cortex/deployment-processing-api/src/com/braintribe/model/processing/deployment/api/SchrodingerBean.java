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
package com.braintribe.model.processing.deployment.api;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.cortex.deployment.EnvironmentDenotationRegistry;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

/**
 * SchrodingerBean (SB) represents a deployable that is deployed from the cortex but is also available in Application's wire configuration. This dual
 * nature (hence the name) is necessary as on the one hand, we want for hardwired components in the wire world to have access to these instance, on
 * the other hand, we want to have them configurable in a normalized way (i.e. via cortex).
 * 
 * <h2>Working mechanism</h2>
 * 
 * Typically, a Schrodinger bean consists of two components:
 * <ol>
 * <li>Actual expert bean, which is really a proxy that will delegate to the actual expert once its deployed from the cortex
 * <li>SchrodingerBean instance that describes this expert
 * </ol>
 * 
 * Example:
 * 
 * <pre>
 * {@code @Managed}
 * public LockManager lockManager() {
 * 	return deployment.proxyingDeployedComponentResolver().resolve(SchrodingerBean.schrodingerBeanId("LockManager"), commonComponents.lockingManager());
 * }
 *  
 * public SchrodingerBean<LockManager> lockManagerSchrodingerBean() {
 * 	return SchrodingerBean.of("LockManager", this::lockManager, CortexConfiguration::getLockManager);
 * }
 * </pre>
 * 
 * Each proxy delegates to an actual implementation based on "externalId". Initially, each proxy uses an invalid (something like
 * SchrodingerBean:LockManager), and any attempt to use the proxy would fail.
 * <p>
 * This is later "fixed" as cortex contains a special post initializer (called SchrodingerBeanCoupler) which sets the correct external id. This
 * initializer is configured with a list of all known {@link SchrodingerBean} instances, for each it resolves it's
 * {@link #deployable(CortexConfiguration, ManagedGmSession)} (probably from {@link CortexConfiguration}), and sets the deployable's externalId on the
 * {@link #proxyDelegation() proxy}.
 *
 * 
 * <h2>Notes on {@link CortexConfiguration}(CC)</h2>
 * 
 * Currently all SchrodingerBeans are resolved from CortexConfiguration, e.g. {@link CortexConfiguration#getLockManager()}.
 * <p>
 * <b>But who sets these deployables on CC?</b><br/>
 * While any initializer might set a deployable on CC, typically it's based on {@link EnvironmentDenotationRegistry} via following mechanism:
 * <ol>
 * <li>There is an Edr2ccPostInitializer, a post initializer on cortext.
 * <li>Edr2ccPostInitializer knows all the SBs, where they should be configured on CC (which property) and their bindId in EDR.
 * <li>When it runs, it finds all SBs where the corresponding CC property is still <tt>null</tt>
 * <li>Using the SB's bindId, it looks a denotation instance up in the EDR.
 * <li>If it finds one, it transforms its into its final form and sets it on CC - see DenotationTransformer for more details on the transformation
 * process.
 * <li>If it doesn't find one, there is a follow-up post-initializer, that sets the default deployable for every relevant CC property.
 * </ol>
 * 
 * <h2>Used shortcuts</h2>
 * <ul>
 * <li>CC: Cortex Configuration
 * <li>EDR: Environment Denotation Registry
 * <li>SB: SchrodingerBean
 * </ul>
 * 
 * @param <T>
 *            type of given bean, e.g. IncrementalAcess
 * 
 * @author peter.gazdik
 */
public interface SchrodingerBean<T> {

	/** Just for identification purposes. */
	String name();

	T proxy();

	ConfigurableDcProxyDelegation proxyDelegation();

	Deployable deployable(CortexConfiguration cortexConfiguration, ManagedGmSession cortexSession);

	static boolean isSchrodingerBeanId(String externalId) {
		return externalId.startsWith("SchrodingerBean:");
	}

	static String schrodingerBeanId(String simpleName) {
		return "SchrodingerBean:" + simpleName;
	}

	// #####################################################
	// ## . . . . . . . . Static builders . . . . . . . . ##
	// #####################################################

	/** Builds a {@link SchrodingerBean} for a deployable configured via {@link CortexConfiguration}. */
	static <T> SchrodingerBean<T> of(String name, T proxy, Function<CortexConfiguration, ? extends Deployable> deployableResolver) {
		return of(name, proxy, (cc, session) -> deployableResolver.apply(cc));
	}

	/** Builds a {@link SchrodingerBean} for given parameters. */
	static <T> SchrodingerBean<T> of(String name, T proxy,
			BiFunction<CortexConfiguration, ManagedGmSession, Deployable> deployableResolver) {

		return new SchrodingerBean<T>() {

			@Override
			public String name() {
				return name;
			}

			@Override
			public T proxy() {
				return proxy;
			}
			
			@Override
			public ConfigurableDcProxyDelegation proxyDelegation() {
				return DcProxy.getConfigurableDelegateManager(proxy);
			}

			@Override
			public Deployable deployable(CortexConfiguration cortexConfiguration, ManagedGmSession cortexSession) {
				return deployableResolver.apply(cortexConfiguration, cortexSession);
			}

		};
	}
}
