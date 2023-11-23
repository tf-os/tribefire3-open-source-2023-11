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
package tribefire.module.wire.contract;

import com.braintribe.model.dcsadeployment.DcsaSharedStorage;
import com.braintribe.model.leadershipdeployment.LeadershipManager;
import com.braintribe.model.lockingdeployment.Locking;
import com.braintribe.model.messagingdeployment.Messaging;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.wire.api.space.WireSpace;

/**
 * @author peter.gazdik
 */
public interface ClusterBindersContract extends WireSpace {

	ComponentBinder<Messaging, MessagingConnectionProvider<?>> messaging();

	ComponentBinder<Locking, com.braintribe.model.processing.lock.api.Locking> locking();

	ComponentBinder<LeadershipManager, tribefire.cortex.leadership.api.LeadershipManager> leadershipManager();

	ComponentBinder<DcsaSharedStorage, com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage> dcsaSharedStorage();

}
