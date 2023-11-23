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
package com.braintribe.model.processing.deployment.hibernate.mapping.wrapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.EntityDescriptor;

/**
 * <p>
 * Wrapper class for {@link GmEntityType} that includes features to assist {@link EntityDescriptor} generation.
 * 
 */
public class HbmEntityType {
	
	public enum EntityTypeCategory {
		CLASS,
		SUBCLASS,
		UNMAPPED,
	}

	private GmEntityType type;
	private HbmEntityType superType;
	private List<HbmEntityType> subTypes;

	private EntityTypeCategory typeCategory;

	private Set<GmEntityType> flattenedSuperTypes;
	private Set<String> flattenedSuperTypesSignatures;
	private Set<String> flattenedSubTypesSignatures;

	public HbmEntityType(GmEntityType type) {
		this.type = type;
		this.subTypes = new ArrayList<HbmEntityType>();
		this.flattenedSuperTypes = new LinkedHashSet<GmEntityType>();
		this.flattenedSuperTypesSignatures = new LinkedHashSet<String>();
		this.flattenedSubTypesSignatures = new LinkedHashSet<String>();
		this.typeCategory = EntityTypeCategory.UNMAPPED;
	}
	
	public GmEntityType getType() {
		return type;
	}

	public void setType(GmEntityType type) {
		this.type = type;
	}
	
	/**
	 * Single super type to be used in the "extends" attribute for entities categorized as EntityTypeCategory.SUBCLASS 
	 */
	public HbmEntityType getSuperType() {
		return superType;
	}

	public void setSuperType(HbmEntityType superType) {
		this.superType = superType;
	}
	
	public List<HbmEntityType> getSubTypes() {
		return subTypes;
	}
	
	public void setSubTypes(List<HbmEntityType> subTypes) {
		this.subTypes = subTypes;
	}
	
	public Boolean getHasSubTypes() {
		return (!(this.subTypes == null) && !this.subTypes.isEmpty());
	}
	
	public Boolean getIsTopLevel() {
		return typeCategory == EntityTypeCategory.CLASS;
	}
	
	public Set<GmEntityType> getFlattenedSuperTypes() {
		return flattenedSuperTypes;
	}

	public void setFlattenedSuperTypes(Set<GmEntityType> flattenedSuperTypes) {
		this.flattenedSuperTypes = flattenedSuperTypes;
	}
	
	public Set<String> getFlattenedSuperTypesSignatures() {
		return flattenedSuperTypesSignatures;
	}

	public void setFlattenedSuperTypesSignatures(Set<String> flattenedSuperTypesSignatures) {
		this.flattenedSuperTypesSignatures = flattenedSuperTypesSignatures;
	}

	public Set<String> getFlattenedSubTypesSignatures() {
		return flattenedSubTypesSignatures;
	}

	public void setFlattenedSubTypesSignatures(Set<String> flattenedSubTypesSignatures) {
		this.flattenedSubTypesSignatures = flattenedSubTypesSignatures;
	}

	public boolean isSuperTypeOf(HbmEntityType other) {
		return other.getFlattenedSuperTypesSignatures().contains(this.getType().getTypeSignature());
	}
	
	public boolean isSubTypeOf(HbmEntityType other) {
		return other.getFlattenedSubTypesSignatures().contains(this.getType().getTypeSignature());
	}

	public EntityTypeCategory getTypeCategory() {
		return typeCategory;
	}

	public void setTypeCategory(EntityTypeCategory typeCategory) {
		this.typeCategory = typeCategory;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + type.getTypeSignature() + "]";
	}
}
