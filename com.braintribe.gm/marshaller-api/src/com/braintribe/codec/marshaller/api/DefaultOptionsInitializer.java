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
package com.braintribe.codec.marshaller.api;

import com.braintribe.codec.marshaller.api.options.GmDeserializationContext;
import com.braintribe.codec.marshaller.api.options.GmDeserializationContextBuilderImpl;
import com.braintribe.codec.marshaller.api.options.GmSerializationContext;
import com.braintribe.codec.marshaller.api.options.GmSerializationContextBuilder;
import com.braintribe.codec.marshaller.api.options.attributes.DecodingLenienceOption;
import com.braintribe.codec.marshaller.api.options.attributes.InferredRootTypeOption;
import com.braintribe.codec.marshaller.api.options.attributes.OutputPrettinessOption;
import com.braintribe.codec.marshaller.api.options.attributes.RequiredTypesRecieverOption;
import com.braintribe.codec.marshaller.api.options.attributes.SessionOption;
import com.braintribe.codec.marshaller.api.options.attributes.AbsentifyMissingPropertiesOption;
import com.braintribe.codec.marshaller.api.options.attributes.StabilizeOrderOption;
import com.braintribe.codec.marshaller.api.options.attributes.UseDirectPropertyAccessOption;
import com.braintribe.codec.marshaller.api.options.attributes.WriteAbsenceInformationOption;
import com.braintribe.codec.marshaller.api.options.attributes.WriteEmptyPropertiesOption;
import com.braintribe.common.attribute.AttributeContextBuilder;
import com.braintribe.model.generic.reflection.BaseType;

interface DefaultOptionsInitializer {
	// Set even obvious default values to avoid an exception when calling the respective getter
	static void initializeSerializationDefaults(AttributeContextBuilder builder) {
		builder.setAttribute(OutputPrettinessOption.class, OutputPrettiness.none);
		builder.setAttribute(InferredRootTypeOption.class, BaseType.INSTANCE);
		builder.setAttribute(WriteAbsenceInformationOption.class, true);
		
		builder.setAttribute(StabilizeOrderOption.class, false);
		builder.setAttribute(UseDirectPropertyAccessOption.class, false);
		builder.setAttribute(WriteEmptyPropertiesOption.class, false);
	}
	
	// Set even obvious default values to avoid an exception when calling the respective getter
	static void initializeDeserializationDefaults(AttributeContextBuilder builder) {
		builder.setAttribute(DecodingLenienceOption.class, null);
		builder.setAttribute(InferredRootTypeOption.class, null);
		builder.setAttribute(RequiredTypesRecieverOption.class, null);
		builder.setAttribute(SessionOption.class, null);
		builder.setAttribute(AbsentifyMissingPropertiesOption.class, false);
	}

	static GmSerializationOptions createDefaultSerializationOptions() {
		GmSerializationContextBuilder emptyContextBuilder = new GmSerializationContext().derive();
		initializeSerializationDefaults(emptyContextBuilder);
		return emptyContextBuilder.build();
	}
	
	static GmDeserializationOptions createDefaultDeserializationOptions() {
		GmDeserializationContextBuilderImpl emptyContextBuilder = new GmDeserializationContext().derive();
		initializeDeserializationDefaults(emptyContextBuilder);
		return emptyContextBuilder.build();
	}
}
