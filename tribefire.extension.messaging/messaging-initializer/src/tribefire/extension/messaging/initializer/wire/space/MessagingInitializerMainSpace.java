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
package tribefire.extension.messaging.initializer.wire.space;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.messaging.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.messaging.initializer.wire.contract.MessagingInitializerContract;
import tribefire.extension.messaging.initializer.wire.contract.MessagingInitializerMainContract;
import tribefire.extension.messaging.initializer.wire.contract.MessagingInitializerModuleDefaultsContract;
import tribefire.extension.messaging.initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.messaging.model.meta.MessagingProperty;
import tribefire.extension.messaging.model.meta.MessagingTypeSignature;
import tribefire.extension.messaging.model.meta.RelatedObjectType;
import tribefire.extension.messaging.templates.wire.contract.MessagingTemplatesContract;

@Managed
public class MessagingInitializerMainSpace implements MessagingInitializerMainContract {

	@Import
	private MessagingInitializerContract initializer;

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private RuntimePropertiesContract properties;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private MessagingTemplatesContract messagingTemplate;

	@Import
	private MessagingInitializerModuleDefaultsContract defaults;

	@Override
	public MessagingInitializerContract initializer() {
		return initializer;
	}

	@Override
	public ExistingInstancesContract existingInstances() {
		return existingInstances;
	}

	@Override
	public RuntimePropertiesContract properties() {
		return properties;
	}

	@Override
	public CoreInstancesContract coreInstances() {
		return coreInstances;
	}

	@Override
	public MessagingTemplatesContract messagingTemplate() {
		return messagingTemplate;
	}

	@Override
	public MessagingInitializerModuleDefaultsContract defaults() {
		return defaults;
	}

	@Override
	@Managed
	public MessagingTypeSignature testTypeSignatureMd(ManagedGmSession session) {
		MessagingTypeSignature bean = session.create(MessagingTypeSignature.T);
		bean.setIdObjectType(RelatedObjectType.REQUEST);
		return bean;
	}

	@Override
	@Managed
	public MessagingProperty diffPropertyMdRequest(ManagedGmSession session, EntityType<?> requestType, EntityType<?> loadedObjectType) {
		MessagingProperty md = session.create(MessagingProperty.T);
		md.setGlobalId("wire://AdxInitializerWireModule/AdxInitializerDataSpace/" + requestType.getTypeName() + "/" + loadedObjectType.getTypeName());
		md.setGetterEntityType(requestType.getTypeSignature());
		md.setLoadedObjectType(loadedObjectType.getTypeSignature());
		return md;
	}

}
