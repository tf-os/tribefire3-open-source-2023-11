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
package tribefire.extension.etcd.etcd_vitals.wire.space;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.plugin.etcd.EtcdPlugableLeadershipManager;
import com.braintribe.model.plugin.etcd.EtcdPlugableLockManager;
import com.braintribe.utils.lcd.NullSafe;

import tribefire.extension.etcd.vitals.model.deployment.EtcdLeadershipManager;
import tribefire.extension.etcd.vitals.model.deployment.EtcdLockManager;
import tribefire.extension.etcd.vitals.model.deployment.EtcdMessaging;
import tribefire.module.api.DenotationTransformationContext;
import tribefire.module.api.DenotationTransformerRegistry;

/**
 * @author peter.gazdik
 */
public class EtcdPluggablesEdr2ccMorphers {

	// TODO currently we only know this is edr2cc because there is no other use-case for DenoTrans.
	public static final String globalIdPrefix = "edr2cc:etcd:";

	public static void bindMorphers(DenotationTransformerRegistry tegistry) {
		tegistry.registerStandardMorpher(EtcdPlugableLeadershipManager.T, EtcdLeadershipManager.T, EtcdPluggablesEdr2ccMorphers::leadership);

		tegistry.registerStandardMorpher(EtcdPlugableLockManager.T, EtcdLockManager.T, EtcdPluggablesEdr2ccMorphers::locking);

		tegistry.registerStandardMorpher(com.braintribe.model.messaging.etcd.EtcdMessaging.T, EtcdMessaging.T,
				EtcdPluggablesEdr2ccMorphers::messaging);
	}

	private static Maybe<EtcdLeadershipManager> leadership(DenotationTransformationContext context, EtcdPlugableLeadershipManager pluggable) {
		EtcdLeadershipManager deployable = context.create(EtcdLeadershipManager.T);
		deployable.setGlobalId(globalIdPrefix + context.denotationId());

		deployable.setEndpointUrls(pluggable.getEndpointUrls());
		deployable.setExternalId(deployable.getGlobalId());
		deployable.setName("etcd Leadership Manager");

		deployable.setUsername(pluggable.getUsername());
		deployable.setPassword(pluggable.getPassword());
		deployable.setProject(pluggable.getProject());

		deployable.setDefaultLeadershipTimeout(pluggable.getDefaultLeadershipTimeout());
		deployable.setDefaultCandidateTimeout(pluggable.getDefaultCandidateTimeout());
		deployable.setCheckInterval(pluggable.getCheckInterval());

		return Maybe.complete(deployable);
	}

	private static Maybe<EtcdLockManager> locking(DenotationTransformationContext context, EtcdPlugableLockManager pluggable) {
		EtcdLockManager deployable = context.create(EtcdLockManager.T);
		deployable.setGlobalId(globalIdPrefix + context.denotationId());

		deployable.setEndpointUrls(pluggable.getEndpointUrls());
		deployable.setExternalId(deployable.getGlobalId());
		deployable.setName("etcd Lock Manager");

		deployable.setUsername(pluggable.getUsername());
		deployable.setPassword(pluggable.getPassword());
		deployable.setProject(pluggable.getProject());
		
		return Maybe.complete(deployable);
	}

	private static Maybe<EtcdMessaging> messaging(DenotationTransformationContext context,
			com.braintribe.model.messaging.etcd.EtcdMessaging pluggable) {

		EtcdMessaging deployable = context.create(EtcdMessaging.T);
		deployable.setGlobalId(globalIdPrefix + context.denotationId());

		deployable.setExternalId(deployable.getGlobalId());
		deployable.setName(NullSafe.get(pluggable.getName(), "etcd Message Queue"));

		deployable.setEndpointUrls(pluggable.getEndpointUrls());
		deployable.setProject(pluggable.getProject());
		deployable.setUsername(pluggable.getUsername());
		deployable.setPassword(pluggable.getPassword());

		return Maybe.complete(deployable);
	}
}
