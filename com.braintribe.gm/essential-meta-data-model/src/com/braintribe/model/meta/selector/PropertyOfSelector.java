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
package com.braintribe.model.meta.selector;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.prompt.Hidden;

/**
 * Evaluates to true iff the resolution context property is also a property of the configured {@link #getEntityType()
 * entityType}. This means we we check if the configured entity type has a property with same name and type. We do not
 * check if it is the exact same property, due to possible multiple inheritance ambiguity.
 * 
 * Example:
 * 
 * Say we have an abstract type X, and we want to make all the properties declared on it's sub-types hidden. We can
 * configure a {@link Hidden} MD on X directly, with {@link NegationSelector negation} of this selector for the type X.
 */
public interface PropertyOfSelector extends MetaDataSelector {

	EntityType<PropertyOfSelector> T = EntityTypes.T(PropertyOfSelector.class);

	GmEntityType getEntityType();
	void setEntityType(GmEntityType entityType);

	boolean getOnlyDeclared();
	void setOnlyDeclared(boolean OnlyDeclared);
	
}
