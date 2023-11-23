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
package com.braintribe.gwt.genericmodel.client.itw;

import static java.util.Collections.emptyList;

import java.util.List;

import com.braintribe.gwt.genericmodel.client.reflect.GwtEntityType;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.TransientProperty;

/**
 * An extension for {@link EntityType} useful for implementation of {@link EnhancedEntity} in GWT.
 */
public class GwtScriptEntityType<T extends GenericEntity> extends GwtEntityType<T> {

	private EntityTypeBinding entityTypeBinding;
	private static int scriptTypeIds = -1;
	private final int typeId = scriptTypeIds--;

	/**
	 * This is called iff there are not transient properties, including within super-types.
	 * 
	 * @see GwtEntityType#setDeclaredTransientProperties(TransientProperty[])
	 */
	public void setNoTransientProperties() {
		super.setTransientProperties(emptyList());
	}

	public EntityTypeBinding getEntityTypeBinding() {
		return entityTypeBinding;
	}

	public void setEntityTypeBinding(EntityTypeBinding entityBinding) {
		this.entityTypeBinding = entityBinding;
	}

	public List<GwtScriptEntityType<?>> getGwtScriptSuperTypes() {
		return (List<GwtScriptEntityType<?>>) (List<?>) super.getSuperTypes();
	}

	public List<GwtScriptProperty> getGwtScriptProperties() {
		return (List<GwtScriptProperty>) (List<?>) super.getProperties();
	}

	public int getTypeId() {
		return typeId;
	}

	@Override
	public boolean isInstance(Object value) {
		CastableTypeMap typeMap = ScriptOnlyItwTools.getCastableTypeMap(value);
		return typeMap != null && typeMap.instanceOf(typeId);
	}
}
