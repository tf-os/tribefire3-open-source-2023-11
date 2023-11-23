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
package com.braintribe.model.processing.meta.editor.empty;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.meta.editor.EntityTypeMetaDataEditor;

/**
 * @author peter.gazdik
 */
public class EmptyEntityTypeMetaDataEditor implements EntityTypeMetaDataEditor {

	public static final EmptyEntityTypeMetaDataEditor INSTANCE = new EmptyEntityTypeMetaDataEditor();

	private EmptyEntityTypeMetaDataEditor() {
	}

	@Override
	public GmEntityType getEntityType() {
		throw new UnsupportedOperationException("Method 'EmptyEntityTypeMetaDataEditor.getEntityType' is not supported!");
	}

	@Override
	public EntityTypeMetaDataEditor configure(Consumer<GmEntityTypeInfo> consumer) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor configure(String propertyName, Consumer<GmPropertyInfo> consumer) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor configure(Property property, Consumer<GmPropertyInfo> consumer) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor configure(GmPropertyInfo gmPropertyInfo, Consumer<GmPropertyInfo> consumer) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor addMetaData(MetaData... mds) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor addMetaData(Iterable<? extends MetaData> mds) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor removeMetaData(Predicate<? super MetaData> filter) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor addPropertyMetaData(MetaData... mds) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor addPropertyMetaData(Iterable<? extends MetaData> mds) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor removePropertyMetaData(Predicate<? super MetaData> filter) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor addPropertyMetaData(String propertyName, MetaData... mds) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor addPropertyMetaData(String propertyName, Iterable<? extends MetaData> mds) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor removePropertyMetaData(String propertyName, Predicate<? super MetaData> filter) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor addPropertyMetaData(Property property, MetaData... mds) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor addPropertyMetaData(Property property, Iterable<? extends MetaData> mds) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor removePropertyMetaData(Property property, Predicate<? super MetaData> filter) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor addPropertyMetaData(GmPropertyInfo gmPropertyInfo, MetaData... mds) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor addPropertyMetaData(GmPropertyInfo gmPropertyInfo, Iterable<? extends MetaData> mds) {
		return this;
	}

	@Override
	public EntityTypeMetaDataEditor removePropertyMetaData(GmPropertyInfo gmPropertyInfo, Predicate<? super MetaData> filter) {
		return this;
	}

}
