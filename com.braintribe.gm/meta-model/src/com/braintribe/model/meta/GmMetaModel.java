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
package com.braintribe.model.meta;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.TypeRestriction;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Unique;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.data.EnumConstantMetaData;
import com.braintribe.model.meta.data.EnumTypeMetaData;
import com.braintribe.model.meta.data.HasMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.ModelMetaData;
import com.braintribe.model.meta.data.UniversalMetaData;
import com.braintribe.model.meta.override.GmCustomTypeOverride;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.meta.override.GmEnumTypeOverride;
import com.braintribe.model.weaving.ProtoGmMetaModel;

/**
 * This represents a model, and should thus have been called GmModel.
 * <p>
 * NOTE all {@link GmModelElement}s have semantic globalIds, this one in the form <tt>model:${this.getName()}</tt>, where {@link #getName()} is in the
 * form <tt>$groupId:$artifactId</tt> - see {@link Model#modelGlobalId(String)}.
 * 
 * @see Model
 * @see GmModelElement
 */
@SelectiveInformation(value = "${name}")
public interface GmMetaModel extends ProtoGmMetaModel, HasMetaData, GmModelElement, IsDeclaredInModel {

	EntityType<GmMetaModel> T = EntityTypes.T(GmMetaModel.class);

	final static String name = "name";
	final static String version = "version";
	final static String types = "types";
	final static String typeOverrides = "typeOverrides";
	final static String metaData = "metaData";
	final static String enumTypeMetaData = "enumTypeMetaData";
	final static String enumConstantMetaData = "enumConstantMetaData";
	final static String dependencies = "dependencies";

	@Override
	@Mandatory
	@Unique
	String getName();
	@Override
	void setName(String name);

	@Override
	String getVersion();
	@Override
	void setVersion(String version);

	@Override
	Set<GmType> getTypes();
	void setTypes(Set<GmType> types);

	@Override
	Set<GmCustomTypeOverride> getTypeOverrides();
	void setTypeOverrides(Set<GmCustomTypeOverride> typeOverrides);

	@Override
	@TypeRestriction({ ModelMetaData.class, UniversalMetaData.class })
	Set<MetaData> getMetaData();

	@TypeRestriction({ EnumTypeMetaData.class, UniversalMetaData.class })
	Set<MetaData> getEnumTypeMetaData();
	void setEnumTypeMetaData(Set<MetaData> enumTypeMetaData);

	@TypeRestriction({ EnumConstantMetaData.class, UniversalMetaData.class })
	Set<MetaData> getEnumConstantMetaData();
	void setEnumConstantMetaData(Set<MetaData> enumConstantMetaData);

	@Override
	List<GmMetaModel> getDependencies();
	void setDependencies(List<GmMetaModel> dependencies);

	/**
	 * This might be removed later, but to adjust code more easily, we have these here.
	 */
	default Stream<GmEntityType> entityTypes() {
		return (Stream<GmEntityType>) (Object) getTypes().stream().filter(GmType::isGmEntity);
	}

	/** See comment for {@link #entityTypes()} */
	default Stream<GmEnumType> enumTypes() {
		return (Stream<GmEnumType>) (Object) getTypes().stream().filter(GmType::isGmEnum);
	}

	/** See comment for {@link #entityTypes()} */
	default Stream<GmEntityTypeOverride> entityOverrides() {
		return (Stream<GmEntityTypeOverride>) (Object) getTypeOverrides().stream().filter(GmCustomTypeOverride::isGmEntityOverride);
	}

	/** See comment for {@link #entityTypes()} */
	default Stream<GmEnumTypeOverride> enumOverrides() {
		return (Stream<GmEnumTypeOverride>) (Object) getTypeOverrides().stream().filter(GmCustomTypeOverride::isGmEnumOverride);
	}

	/** See comment for {@link #entityTypes()} */
	default Set<GmEntityType> entityTypeSet() {
		return entityTypes().collect(Collectors.toSet());
	}

	/** See comment for {@link #entityTypes()} */
	default Set<GmEnumType> enumTypeSet() {
		return enumTypes().collect(Collectors.toSet());
	}

	/** See comment for {@link #entityTypes()} */
	default Set<GmEntityTypeOverride> entityOverrideSet() {
		return entityOverrides().collect(Collectors.toSet());
	}

	/** See comment for {@link #entityTypes()} */
	default Set<GmEnumTypeOverride> enumOverrideSet() {
		return enumOverrides().collect(Collectors.toSet());
	}

	@Override
	default GmMetaModel declaringModel() {
		return this;
	}
}
