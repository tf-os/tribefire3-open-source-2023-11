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
package tribefire.extension.messaging.initializer.wire.contract;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.messaging.model.meta.MessagingProperty;
import tribefire.extension.messaging.model.meta.MessagingTypeSignature;
import tribefire.extension.messaging.templates.wire.contract.MessagingTemplatesContract;

public interface MessagingInitializerMainContract extends WireSpace {

	MessagingInitializerContract initializer();

	ExistingInstancesContract existingInstances();

	RuntimePropertiesContract properties();

	CoreInstancesContract coreInstances();

	MessagingInitializerModuleDefaultsContract defaults();

	MessagingTemplatesContract messagingTemplate();

	MessagingTypeSignature testTypeSignatureMd(ManagedGmSession session);

	MessagingProperty diffPropertyMdRequest(ManagedGmSession session, EntityType<?> requestType, EntityType<?> loadedObjectType);
}
