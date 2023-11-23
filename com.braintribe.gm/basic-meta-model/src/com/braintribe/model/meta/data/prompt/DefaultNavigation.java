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
package com.braintribe.model.meta.data.prompt;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.EntityTypeMetaData;

/**
 * This metadata defines a default property to be used when navigating an entity. For example, when using the Open
 * action, or double clicking an entity, currently we navigate to that entity, showing all its properties. By defining
 * this metadata, we, instead, navigate not to the entity itself, but to the defined property. Of course, the property
 * then must be configured to a non-simple property.
 * 
 * @author michel.docouto
 */
public interface DefaultNavigation extends EntityTypeMetaData {

	EntityType<DefaultNavigation> T = EntityTypes.T(DefaultNavigation.class);

	void setProperty(GmProperty property);
	GmProperty getProperty();

}
