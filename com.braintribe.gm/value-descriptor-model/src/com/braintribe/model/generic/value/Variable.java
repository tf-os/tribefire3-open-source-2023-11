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
package com.braintribe.model.generic.value;


import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.value.type.DynamicallyTypedDescriptor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("Variable ${name}")

public interface Variable extends DynamicallyTypedDescriptor {

	EntityType<Variable> T = EntityTypes.T(Variable.class);

	// @formatter:off
	@Mandatory
	String getName();
	void setName(String name);

	
	@Override
	@Initializer("'string'") //default typesignature for variables
	String getTypeSignature();
	@Override
	void setTypeSignature(String typeSignature);
	
	/**
	 * @deprecated this should be configured with meta data (Name)
	 */
	@Deprecated
	LocalizedString getLocalizedName();
	@Deprecated
	void setLocalizedName(LocalizedString localizedName);

	/**
	 * @deprecated this should be configured with meta data (Description)
	 */
	@Deprecated
	LocalizedString getDescription();
	@Deprecated
	void setDescription(LocalizedString description);

	void setDefaultValue(Object defaultValue);
	Object getDefaultValue();
	// @formatter:on

}
