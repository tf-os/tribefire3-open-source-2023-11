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
package com.braintribe.testing.internal.suite.crud;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.Unique;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.lcd.CommonTools;

/**
 * Lets you state if you want to ignore a property (false) or not (true) during testing
 * 
 * @author Neidhart
 *
 */
@FunctionalInterface
public interface PropertyFilterPredicate {

	/**
	 * Lets you state if you want to ignore a property (false) or not (true) during testing
	 * 
	 * @return
	 */
	Boolean test(Property property, GenericEntity entity, PersistenceGmSession session);
	
//	public static PropertyFilterPredicate ignorePropertiesWithName(String ... names) {
//		
//		return (Property property, GenericEntity entity, PersistenceGmSession _session) -> {
//			return !CommonTools.getSet(names).contains(property.getName());
//		};
//	}

}
