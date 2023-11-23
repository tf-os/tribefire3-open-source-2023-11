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

import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.codec.marshaller.api.DecodingLenience;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.options.attributes.AbsentifyMissingPropertiesOption;
import com.braintribe.codec.marshaller.api.options.attributes.DecodingLenienceOption;
import com.braintribe.codec.marshaller.api.options.attributes.InferredRootTypeOption;
import com.braintribe.codec.marshaller.api.options.attributes.RequiredTypesRecieverOption;
import com.braintribe.codec.marshaller.api.options.attributes.SessionOption;
import com.braintribe.common.attribute.AttributeContextBuilder;
import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.session.GmSession;

/**
 * A builder for {@link GmDeserializationOptions}.
 * 
 * @author Neidhart.Orlich
 */
public interface GmDeserializationContextBuilder extends AttributeContextBuilder {

	default GmDeserializationContextBuilder absentifyMissingProperties(boolean shouldAbsentifyMissingProperties) {
		return set(AbsentifyMissingPropertiesOption.class, shouldAbsentifyMissingProperties);
	}

	default GmDeserializationContextBuilder setDecodingLenience(DecodingLenience decodingLenience) {
		return set(DecodingLenienceOption.class, decodingLenience);
	}

	default GmDeserializationContextBuilder setRequiredTypesReceiver(Consumer<Set<String>> requiredTypesReceiver) {
		return set(RequiredTypesRecieverOption.class, requiredTypesReceiver);
	}

	default GmDeserializationContextBuilder setSession(GmSession session) {
		return set(SessionOption.class, session);
	}

	default GmDeserializationContextBuilder setInferredRootType(GenericModelType inferredRootType) {
		return set(InferredRootTypeOption.class, inferredRootType);
	}

	@Override
	default <A extends TypeSafeAttribute<? super V>, V> GmDeserializationContextBuilder set(Class<A> option, V value) {
		setAttribute(option, value);
		return this;
	}

	/**
	 * Creates a new {@link GmDeserializationOptions} instance like specified before with this builder. After calling this
	 * method, this builder becomes immutable and shouldn't be used any more.
	 */
	@Override
	GmDeserializationOptions build();

}
