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
package com.braintribe.model.processing.platformsetup;

import java.io.Writer;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.manipulation.marshaller.ManMarshaller;
import com.braintribe.model.processing.manipulation.marshaller.ResultNaming;

public abstract class ManipulationRecording {

	private static ManMarshaller manMarshaller = new ManMarshaller();

	public static void stringify(Writer writer, GenericEntity entity, String instanceVar, String defaultInstanceTypeVar) {

		try {
			if (defaultInstanceTypeVar != null && isDefaultInstance(entity)) {
				writer.append(defaultInstanceTypeVar);
				writer.append(" = ");
				writer.append(entity.entityType().getTypeSignature());
				return;

			} else {
				manMarshaller.marshall(writer, entity, GmSerializationOptions.deriveDefaults().stabilizeOrder(true)
						.set(ResultNaming.class, instanceVar).outputPrettiness(OutputPrettiness.high).build());
			}

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while stringifying manipulations!");
		}

	}

	private static boolean isDefaultInstance(GenericEntity entity) {
		for (Property property : entity.entityType().getProperties()) {
			Object initializer = property.getInitializer();

			if (initializer != null) {
				if (!nullSafeEquals(property.get(entity), initializer))
					return false;
			} else {
				if (!nullSafeEquals(property.get(entity), property.getDefaultRawValue()))
					return false;
			}
		}

		return true;
	}

	private static boolean nullSafeEquals(Object o1, Object o2) {
		if (o1 == o2)
			return true;

		if (o1 == null || o2 == null)
			return false;

		return o1.equals(o2);
	}

}
