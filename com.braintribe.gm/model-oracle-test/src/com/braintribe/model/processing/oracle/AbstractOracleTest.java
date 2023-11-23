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
package com.braintribe.model.processing.oracle;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.EnumTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;
import com.braintribe.model.processing.oracle.model.ModelNames;
import com.braintribe.model.processing.oracle.model.ModelOracleModelProvider;
import com.braintribe.model.processing.oracle.model.meta.DataOrigin;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @author peter.gazdik
 */
abstract class AbstractOracleTest implements ModelNames {

	// protected static GmMetaModel farmModel = ModelOracleModelProvider.basicRawModel_Deprecated();
	protected static GmMetaModel farmModel = ModelOracleModelProvider.farmModel();
	protected static ModelOracle oracle = new BasicModelOracle(farmModel);

	protected static EntityTypeOracle getEntityOracle(EntityType<?> et) {
		return oracle.getEntityTypeOracle(et);
	}

	protected static EnumTypeOracle getEnumOracle(Class<? extends Enum<?>> enumClass) {
		return oracle.getEnumTypeOracle(enumClass);
	}

	protected static void assertOrigins(Stream<MetaData> metaData, String... expected) {
		List<String> actualOrigins = extractOrigins(metaData);
		Assertions.assertThat(actualOrigins).containsExactly(expected);
	}

	protected static List<String> extractOrigins(Stream<MetaData> metaData) {
		return metaData.map(md -> ((DataOrigin) md).getOriginName()).collect(Collectors.toList());
	}

	protected static void assertQualifiedOrigins(Stream<QualifiedMetaData> qualifiedMetaData, String... expected) {
		assertOrigins(qualifiedMetaData.map(QualifiedMetaData::metaData), expected);
	}

	protected static void assertQualifiedOwnerIds(Stream<QualifiedMetaData> qualifiedMetaData, String... expectedOwnerIds) {
		List<String> actualOrigins = extractOwnerIds(qualifiedMetaData);
		Assertions.assertThat(actualOrigins).containsExactly(expectedOwnerIds);
	}

	protected static List<String> extractOwnerIds(Stream<QualifiedMetaData> metaData) {
		return metaData.map(qmd -> qmd.ownerElement().getGlobalId()).collect(Collectors.toList());
	}

	protected static String entityId(EntityType<?> et) {
		return typeId(et.getTypeSignature());
	}

	protected static String entityOId(EntityType<?> et, String modelName) {
		return "typeO:" + modelName + "/" + entityId(et);
	}

	protected String propertyId(EntityType<?> et, String propertyName) {
		return JavaTypeAnalysis.propertyGlobalId(et.getTypeSignature(), propertyName);
	}

	protected static String propertyOId(EntityType<?> et, String propertyName, String modelName) {
		return "propertyO:" + entityOId(et, modelName) + "/" + propertyName;
	}

	protected static String enumOId(Class<? extends Enum<?>> enumClass, String modelName) {
		return "typeO:" + modelName + "/" + enumId(enumClass);
	}

	protected static String enumId(Class<? extends Enum<?>> enumClass) {
		return typeId(enumClass.getName());
	}

	protected String constantId(Enum<?> constant) {
		return JavaTypeAnalysis.constantGlobalId(constant.getClass().getName(), constant.name());
	}

	protected static String constantOId(Enum<?> constant, String modelName) {
		return "constantO:" + enumOId((Class<? extends Enum<?>>) constant.getClass(), modelName) + "/" + constant.name();
	}

	private static String typeId(String typeSignature) {
		return JavaTypeAnalysis.typeGlobalId(typeSignature);
	}

}
