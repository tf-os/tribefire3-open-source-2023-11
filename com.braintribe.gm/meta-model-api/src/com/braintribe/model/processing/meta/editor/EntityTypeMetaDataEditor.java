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
package com.braintribe.model.processing.meta.editor;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;

/**
 * @author peter.gazdik
 */
public interface EntityTypeMetaDataEditor {

	/** return the underlying {@link GmEntityType} */
	GmEntityType getEntityType();

	/** Configures MD for current entity type. */
	EntityTypeMetaDataEditor configure(Consumer<GmEntityTypeInfo> consumer);

	/** Configures MD for given property. */
	EntityTypeMetaDataEditor configure(String propertyName, Consumer<GmPropertyInfo> consumer);

	/** Configures MD for given property. */
	EntityTypeMetaDataEditor configure(Property property, Consumer<GmPropertyInfo> consumer);

	/** Configures MD for given property. */
	EntityTypeMetaDataEditor configure(GmPropertyInfo gmPropertyInfo, Consumer<GmPropertyInfo> consumer);

	/** Adds MD to {@link GmEntityTypeInfo#getMetaData()} */
	EntityTypeMetaDataEditor addMetaData(MetaData... mds);

	/** Adds MD to {@link GmEntityTypeInfo#getMetaData()} */
	EntityTypeMetaDataEditor addMetaData(Iterable<? extends MetaData> mds);

	/** Removes MD from {@link GmEntityTypeInfo#getMetaData()} */
	EntityTypeMetaDataEditor removeMetaData(Predicate<? super MetaData> filter);

	/** Adds MD to {@link GmEntityTypeInfo#getPropertyMetaData()} */
	EntityTypeMetaDataEditor addPropertyMetaData(MetaData... mds);

	/** Adds MD to {@link GmEntityTypeInfo#getPropertyMetaData()} */
	EntityTypeMetaDataEditor addPropertyMetaData(Iterable<? extends MetaData> mds);

	/** Removes MD from {@link GmEntityTypeInfo#getPropertyMetaData()} */
	EntityTypeMetaDataEditor removePropertyMetaData(Predicate<? super MetaData> filter);

	/** Adds MD to {@link GmPropertyInfo#getMetaData()}, for property/propertyOverride of this entity. */
	EntityTypeMetaDataEditor addPropertyMetaData(String propertyName, MetaData... mds);

	/** Adds MD to {@link GmPropertyInfo#getMetaData()}, for property/propertyOverride of this entity. */
	EntityTypeMetaDataEditor addPropertyMetaData(String propertyName, Iterable<? extends MetaData> mds);

	/** Removes MD from {@link GmPropertyInfo#getMetaData()}, for property/propertyOverride of this entity. */
	EntityTypeMetaDataEditor removePropertyMetaData(String propertyName, Predicate<? super MetaData> filter);

	/** Similar to {@link #addPropertyMetaData(String, MetaData...)} */
	EntityTypeMetaDataEditor addPropertyMetaData(Property property, MetaData... mds);

	/** Similar to {@link #addPropertyMetaData(String, MetaData...)} */
	EntityTypeMetaDataEditor addPropertyMetaData(Property property, Iterable<? extends MetaData> mds);

	/** Similar to {@link #removePropertyMetaData(String, Predicate)} */
	EntityTypeMetaDataEditor removePropertyMetaData(Property property, Predicate<? super MetaData> filter);

	/** Similar to {@link #addPropertyMetaData(String, MetaData...)} */
	EntityTypeMetaDataEditor addPropertyMetaData(GmPropertyInfo gmPropertyInfo, MetaData... mds);

	/** Similar to {@link #addPropertyMetaData(String, MetaData...)} */
	EntityTypeMetaDataEditor addPropertyMetaData(GmPropertyInfo gmPropertyInfo, Iterable<? extends MetaData> mds);

	/** Similar to {@link #removePropertyMetaData(String, Predicate)} */
	EntityTypeMetaDataEditor removePropertyMetaData(GmPropertyInfo gmPropertyInfo, Predicate<? super MetaData> filter);

}