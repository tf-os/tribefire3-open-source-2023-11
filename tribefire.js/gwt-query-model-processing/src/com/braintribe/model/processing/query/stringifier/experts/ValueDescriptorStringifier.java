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
package com.braintribe.model.processing.query.stringifier.experts;

import java.util.Collection;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.processing.query.api.stringifier.experts.paramter.MultipleParameterProvider;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.utils.lcd.CommonTools;

public class ValueDescriptorStringifier<V extends ValueDescriptor> extends FunctionStringifier<V> {

	public ValueDescriptorStringifier() {
		super(ValueDescriptorStringifier::functionName, new ValueDescriptorParameterProvider());
	}
	
	
	protected static String functionName(ValueDescriptor valueDescriptor) {
		String typeSignature = valueDescriptor.entityType().getTypeSignature();
		String simpleTypName = CommonTools.getSimpleNameFromFullyQualifiedClassName(typeSignature);
		String functionName = StringTools.uncapitalize(simpleTypName);
		return functionName;
	}
	
	protected static class ValueDescriptorParameterProvider implements MultipleParameterProvider<ValueDescriptor> {
		@Override
		public Collection<?> provideParameters(ValueDescriptor valueDescriptor) {
			
			EntityType<GenericEntity> entityType = valueDescriptor.entityType();
			//@formatter:off
			return entityType
				.getProperties() //This list is already sorted by name!
				.stream()
				.filter(ValueDescriptorStringifier::isVdProperty)
				.map(p -> p.get(valueDescriptor))
				.collect(Collectors.toList());
			//@formatter:on
		}
	}
	
	protected static boolean isVdProperty(Property property) {
		return !(property.isIdentifying() || property.isGlobalId());
	}
}
