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
package com.braintribe.model.accessdeployment.jpa.meta;

import java.util.Map;

import com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping;
import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Mapping for a property whose type is an embeddable entity type.
 * 
 * @see EntityMapping#getIsEmbeddable()
 */
public interface JpaEmbedded extends JpaPropertyMapping {

	EntityType<JpaEmbedded> T = EntityTypes.T(JpaEmbedded.class);

	/**
	 * For now, all the properties of the embedded entity type have to be mapped here. Also, only
	 * {@link PropertyMapping} is supported as a map value right now.
	 * 
	 * This can however inconvenient if we want to use the same embeddable type multiple times, but map one of it's
	 * properties differently while others stay the same - this forces us to define the whole map again. So in the
	 * future, the In the future, also want to support {@link JpaPropertyMapping}s on the embeddable type itself, to
	 * serve as a default.
	 * 
	 * Also, currently there is no support for default values of column name and type, so in case PropertyMapping is
	 * used, both have to be specified explicitly.
	 */
	Map<String, JpaPropertyMapping> getEmbeddedPropertyMappings();
	void setEmbeddedPropertyMappings(Map<String, JpaPropertyMapping> embeddedPropertyMappings);

}
