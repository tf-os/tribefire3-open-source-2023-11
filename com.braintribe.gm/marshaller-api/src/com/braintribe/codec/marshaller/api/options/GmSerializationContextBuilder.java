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
package com.braintribe.codec.marshaller.api.options;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.api.options.attributes.InferredRootTypeOption;
import com.braintribe.codec.marshaller.api.options.attributes.OutputPrettinessOption;
import com.braintribe.codec.marshaller.api.options.attributes.StabilizeOrderOption;
import com.braintribe.codec.marshaller.api.options.attributes.UseDirectPropertyAccessOption;
import com.braintribe.codec.marshaller.api.options.attributes.WriteAbsenceInformationOption;
import com.braintribe.codec.marshaller.api.options.attributes.WriteEmptyPropertiesOption;
import com.braintribe.common.attribute.AttributeContextBuilder;
import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.model.generic.reflection.GenericModelType;

/**
 * A builder for {@link GmSerializationOptions}.
 * 
 * @author Neidhart.Orlich
 */
public interface GmSerializationContextBuilder extends AttributeContextBuilder {
	default GmSerializationContextBuilder setOutputPrettiness(OutputPrettiness outputPrettiness) {
		return set(OutputPrettinessOption.class, outputPrettiness);
	}

	default GmSerializationContextBuilder outputPrettiness(OutputPrettiness outputPrettiness) {
		return set(OutputPrettinessOption.class, outputPrettiness);
	}

	default GmSerializationContextBuilder useDirectPropertyAccess(boolean useDirectPropertyAccess) {
		return set(UseDirectPropertyAccessOption.class, useDirectPropertyAccess);
	}

	default GmSerializationContextBuilder writeEmptyProperties(boolean writeEmptyProperties) {
		return set(WriteEmptyPropertiesOption.class, writeEmptyProperties);
	}

	default GmSerializationContextBuilder stabilizeOrder(boolean stabilizeOrder) {
		return set(StabilizeOrderOption.class, stabilizeOrder);
	}

	default GmSerializationContextBuilder writeAbsenceInformation(boolean writeAbsenceInformation) {
		return set(WriteAbsenceInformationOption.class, writeAbsenceInformation);
	}

	default GmSerializationContextBuilder inferredRootType(GenericModelType inferredRootType) {
		return set(InferredRootTypeOption.class, inferredRootType);
	}

	default GmSerializationContextBuilder setInferredRootType(GenericModelType inferredRootType) {
		return set(InferredRootTypeOption.class, inferredRootType);
	}

	@Override
	default <A extends TypeSafeAttribute<? super V>, V> GmSerializationContextBuilder set(Class<A> option, V value) {
		AttributeContextBuilder.super.set(option, value);
		return this;
	}

	/**
	 * Creates a new {@link GmSerializationOptions} instance like specified before with this builder. After calling this
	 * method, this builder becomes immutable and shouldn't be used any more.
	 */
	@Override
	GmSerializationOptions build();

}
