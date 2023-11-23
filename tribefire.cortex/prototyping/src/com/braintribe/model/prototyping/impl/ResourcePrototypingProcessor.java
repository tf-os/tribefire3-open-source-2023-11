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
package com.braintribe.model.prototyping.impl;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.options.GmDeserializationContextBuilder;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.prototyping.api.ResourcePrototyping;

public class ResourcePrototypingProcessor extends PrototypingProcessor<ResourcePrototyping> {
	private MarshallerRegistry marshallerRegistry;

	@Configurable
	@Required
	public void setMarshallerRegistry(MarshallerRegistry marshallerRegistry) {
		this.marshallerRegistry = marshallerRegistry;
	}

	@Override
	public GenericEntity process(ServiceRequestContext requestContext, ResourcePrototyping request) {
		String mimeType = request.getPrototypeResource().getMimeType();
		Marshaller marshaller = marshallerRegistry.getMarshaller(mimeType);

		if (marshaller == null) {
			throw new IllegalArgumentException("Can't unmarshall prototype: No marshaller available for mime-type: " + mimeType);
		}

		GmDeserializationContextBuilder optionsBuilder = GmDeserializationOptions.deriveDefaults();

		GmEntityType prototypeEntityType = request.getPrototypeEntityType();

		if (prototypeEntityType != null) {
			String typeSignature = prototypeEntityType.getTypeSignature();
			if (typeSignature == null) {
				throw new IllegalArgumentException("Could not create prototype: prototypeEntityType parameter has no typeSignature.");
			}

			EntityType<GenericEntity> prototypeType = GMF.getTypeReflection().getEntityType(typeSignature);
			optionsBuilder.setInferredRootType(prototypeType);
		}

		Object unmarshalledPrototype = marshaller.unmarshall(request.getPrototypeResource().openStream(), optionsBuilder.build());

		if (!(unmarshalledPrototype instanceof GenericEntity)) {
			throw new IllegalArgumentException(
					"Unmarshalled prototype is no GenericEntity but '" + unmarshalledPrototype.getClass().getName() + "'.");
		}

		GenericEntity unmarshalledEntity = (GenericEntity) unmarshalledPrototype;

		return unmarshalledEntity;
	}

}
