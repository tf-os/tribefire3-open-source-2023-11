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

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Hibernate mapping meta-data which specifies the mapping for the id property in case the corresponding table uses a composite key.
 * 
 * Note that this mapping is only considered if attached to an id property.
 * 
 * See HibernateAccess for more information regarding composite id mappins.
 */
public interface JpaCompositeId extends JpaPropertyMapping {

	EntityType<JpaCompositeId> T = EntityTypes.T(JpaCompositeId.class);

	List<JpaColumn> getColumns();
	void setColumns(List<JpaColumn> columns);

}
