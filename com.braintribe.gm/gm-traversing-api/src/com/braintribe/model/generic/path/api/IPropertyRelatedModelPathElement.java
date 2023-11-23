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
package com.braintribe.model.generic.path.api;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;

/**
 * This {@link IModelPathElement} collects common properties of all property based elements such as:
 * 
 * <ul>
 * <li>{@link IPropertyModelPathElement}</li>
 * <li>{@link IListItemModelPathElement}</li>
 * <li>{@link ISetItemModelPathElement}</li>
 * <li>{@link IMapKeyModelPathElement}</li>
 * <li>{@link IMapValueModelPathElement}</li>
 * <li></li>
 * </ul>
 * 
 * @author dirk.scheffler
 * @author pit.steinlin
 * @author peter.gazdik
 */
public interface IPropertyRelatedModelPathElement extends IModelPathElement, HasEntityPropertyInfo {

	/**
	 * 
	 * @return the {@link Property} which holds the value returned by {@link #getValue()}
	 */
	@Override
	Property getProperty();

	/**
	 * 
	 * @return the {@link GenericEntity} which is the owner of the property value returned by {@link #getValue()}
	 */
	@Override
	<T extends GenericEntity> T getEntity();

	/**
	 * 
	 * @return the {@link GenericEntity} which is the owner of the property value returned by {@link #getValue()}
	 */
	@Override
	<T extends GenericEntity> EntityType<T> getEntityType();

	@Override
	default boolean isPropertyRelated() {
		return true;
	}

}
