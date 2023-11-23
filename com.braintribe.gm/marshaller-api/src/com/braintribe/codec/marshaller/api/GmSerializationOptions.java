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

import com.braintribe.codec.marshaller.api.options.GmSerializationContextBuilder;
import com.braintribe.codec.marshaller.api.options.attributes.OutputPrettinessOption;
import com.braintribe.codec.marshaller.api.options.attributes.StabilizeOrderOption;
import com.braintribe.codec.marshaller.api.options.attributes.UseDirectPropertyAccessOption;
import com.braintribe.codec.marshaller.api.options.attributes.WriteAbsenceInformationOption;
import com.braintribe.codec.marshaller.api.options.attributes.WriteEmptyPropertiesOption;
import com.braintribe.common.attribute.AttributeContext;

/**
 * Immutable {@link AttributeContext} with convenience methods for serialization related {@link MarshallerOption}s. Call
 * {@link #derive()} on it to create a new instance starting with identical settings that can be adapted to your needs
 * with the builder-like API .
 * <p>
 * To create a new instance from scratch, use {@link #defaultOptions}.
 */
public interface GmSerializationOptions extends GmMarshallingOptions {

	/**
	 * General-purpose {@link GmSerializationOptions}, already preinitialized with common default values. Call
	 * {@link #derive()} on it for a builder-like API to create your custom options.
	 */
	GmSerializationOptions defaultOptions = DefaultOptionsInitializer.createDefaultSerializationOptions();

	static GmSerializationContextBuilder deriveDefaults() {
		return defaultOptions.derive();
	}

	default OutputPrettiness outputPrettiness() {
		return getAttribute(OutputPrettinessOption.class);
	}

	default boolean writeEmptyProperties() {
		return getAttribute(WriteEmptyPropertiesOption.class);
	}

	default boolean useDirectPropertyAccess() {
		return getAttribute(UseDirectPropertyAccessOption.class);
	}

	default boolean stabilizeOrder() {
		return getAttribute(StabilizeOrderOption.class);
	}

	default boolean writeAbsenceInformation() {
		return getAttribute(WriteAbsenceInformationOption.class);
	}

	@Override
	GmSerializationContextBuilder derive();
}
