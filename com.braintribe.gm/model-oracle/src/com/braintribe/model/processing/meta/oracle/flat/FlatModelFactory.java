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
package com.braintribe.model.processing.meta.oracle.flat;

import static com.braintribe.utils.lcd.CollectionTools2.acquireLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmBooleanType;
import com.braintribe.model.meta.GmDateType;
import com.braintribe.model.meta.GmDecimalType;
import com.braintribe.model.meta.GmDoubleType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmFloatType;
import com.braintribe.model.meta.GmIntegerType;
import com.braintribe.model.meta.GmLongType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmStringType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.meta.override.GmCustomTypeOverride;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.meta.override.GmEnumTypeOverride;
import com.braintribe.model.processing.meta.oracle.hierarchy.GraphInliner;
import com.braintribe.model.processing.meta.oracle.hierarchy.InlinedGraph;

/**
 * @author peter.gazdik
 */
public class FlatModelFactory {

	private static final int NUMBER_OF_SIMPLE_TYPES_AND_BASE = 9;
	private static final Logger log = Logger.getLogger(FlatModelFactory.class);

	public static FlatModel buildFor(GmMetaModel model) {
		return new FlatModelFactory(model).build();
	}

	private final GmMetaModel model;
	private final FlatModel flatModel;

	FlatModelFactory(GmMetaModel model) {
		this.model = model;
		this.flatModel = new FlatModel(model);
	}

	private FlatModel build() {
		indexModel();
		return flatModel;
	}

	private void indexModel() {
		InlinedGraph<GmMetaModel> ig = GraphInliner.with(model, GmMetaModel::getDependencies).nullNeighborErrorHandler(this::onDependencyNull).inline();

		flatModel.allModels.addAll(ig.list);
		flatModel.modelIndex.putAll(ig.index);

		logTraceIfEnabled(ig);

		for (GmMetaModel gmMetaModel : ig.list)
			visitModel(gmMetaModel);
	}

	private void onDependencyNull(GmMetaModel modelWithNull) {
		throw new IllegalArgumentException("Error while indexing model " + model.getName() + ". It's dependency (" + modelWithNull.getName()
				+ ") has 'null' among it's dependencies. There seems to be a problem with how this model is retrieved.");
	}
	
	private void logTraceIfEnabled(InlinedGraph<GmMetaModel> ig) {
		if (log.isTraceEnabled()) {
			log.trace("[FlatModelFactory] If you see this message too many times, there is probably a lot of room for optimization.");
			log.trace("[FlatModelFactory] Indexing: " + model.getName());
			log.trace("[FlatModelFactory] Models:" + ig.list.stream().map(GmMetaModel::getName).collect(Collectors.toList()));
		}
	}

	private void visitModel(GmMetaModel model) {
		validateModel(model);

		for (GmType gmType : nullSafe(model.getTypes()))
			visitType(model, gmType);

		for (GmCustomTypeOverride gmCustomTypeOverride : nullSafe(model.getTypeOverrides()))
			visitTypeOverride(gmCustomTypeOverride);
	}

	private void validateModel(GmMetaModel gmMetaModel) {
		if (gmMetaModel.getName().equals(GenericModelTypeReflection.rootModelName)) {
			if (isEmpty(gmMetaModel.getTypes()))
				throw new GenericModelException("[FlatModelFactory] RootModel has no types");

			if (gmMetaModel.getTypes().size() <= NUMBER_OF_SIMPLE_TYPES_AND_BASE)
				throw new GenericModelException("[FlatModelFactory] RootModel does not have all the types. Found only: "
						+ gmMetaModel.getTypes().stream().map(GmType::getTypeSignature).collect(Collectors.joining(", ", "[", "]")));

		} else if (isEmpty(gmMetaModel.getDependencies())) {
			throw new GenericModelException("[FlatModelFactory] Model has no dependencies and is not a root-model: " + gmMetaModel.getName()
					+ ". Something is wrong with your data. This could be caused by a corrupt/missing model-declaration.xml of this model or one of it's dependencies, "
					+ "or if this model was retrieve per query, the property might not have been eager-loaded.");
		}
	}

	private void visitType(GmMetaModel model, GmType gmType) {
		validateType(model, gmType);
		
		indexTypeBySignature(gmType);

		switch (gmType.typeKind()) {
			case BASE:
				visitBaseType((GmBaseType) gmType);
				return;

			case BOOLEAN:
			case DATE:
			case DECIMAL:
			case DOUBLE:
			case FLOAT:
			case INTEGER:
			case LONG:
			case STRING:
				visitSimpleType((GmSimpleType) gmType);
				return;

			case ENTITY:
				visitEntityType((GmEntityType) gmType);
				return;

			case ENUM:
				visitEnumTypeInfo((GmEnumType) gmType);
				return;

			default:
				return;
		}
	}

	private void validateType(GmMetaModel model, GmType gmType) {
		if (gmType == null)
			throw new NullPointerException("Model " + model + " contains a 'null' type.");
	}

	private void visitBaseType(GmBaseType gmType) {
		flatModel.gmBaseType = gmType;
	}

	private void visitSimpleType(GmSimpleType gmType) {
		flatModel.simpleTypes.add(gmType);

		switch (gmType.typeKind()) {
			case BOOLEAN:
				flatModel.gmBooleanType = (GmBooleanType) gmType;
				break;
			case DATE:
				flatModel.gmDateType = (GmDateType) gmType;
				break;
			case DECIMAL:
				flatModel.gmDecimalType = (GmDecimalType) gmType;
				break;
			case DOUBLE:
				flatModel.gmDoubleType = (GmDoubleType) gmType;
				break;
			case FLOAT:
				flatModel.gmFloatType = (GmFloatType) gmType;
				break;
			case INTEGER:
				flatModel.gmIntegerType = (GmIntegerType) gmType;
				break;
			case LONG:
				flatModel.gmLongType = (GmLongType) gmType;
				break;
			case STRING:
				flatModel.gmStringType = (GmStringType) gmType;
				break;
			default:
				throw new IllegalStateException("Unknown simple type kind: " + gmType.typeKind());
		}
	}

	private void visitEntityType(GmEntityType gmType) {
		visitEntityTypeInfo(gmType);

		Set<GmEntityType> superTypeSet = acquireLinkedSet(flatModel.superTypes, gmType);

		for (GmEntityType superType : nullSafe(gmType.getSuperTypes())) {
			superTypeSet.add(superType);
			acquireSet(flatModel.subTypes, superType).add(gmType);
		}

		// makes sure collection types are resolvable by their signature
		for (GmProperty gmProperty : nullSafe(gmType.getProperties()))
			if (getTypeOf(gmType, gmProperty).isGmCollection())
				indexTypeBySignature(getTypeOf(gmType, gmProperty));
	}

	private GmType getTypeOf(GmEntityType gmType, GmProperty gmProperty) {
		if (gmProperty == null)
			throw new NullPointerException("Entity type " + gmType + " contains a 'null' property.");
		
		GmType type = gmProperty.getType();
		if (type == null )
			throw new NullPointerException("Property " + gmProperty + " of " + gmType + " has 'null' as it's type.");
		
		return type;
	}

	private void visitTypeOverride(GmCustomTypeOverride gmCustomTypeOverride) {
		if (gmCustomTypeOverride.isGmEntityOverride())
			visitEntityTypeInfo((GmEntityTypeOverride) gmCustomTypeOverride);
		else
			visitEnumTypeInfo((GmEnumTypeOverride) gmCustomTypeOverride);
	}

	private void visitEntityTypeInfo(GmEntityTypeInfo gmTypeInfo) {
		FlatEntityType flatEntityType = acquireFlatEntityType(gmTypeInfo);
		flatEntityType.infos.add(gmTypeInfo);
	}

	private void visitEnumTypeInfo(GmEnumTypeInfo gmTypeInfo) {
		FlatEnumType flatEnumType = acquireFlatEnumType(gmTypeInfo);
		flatEnumType.infos.add(gmTypeInfo);
	}

	private void indexTypeBySignature(GmType gmType) {
		flatModel.allTypes.put(gmType.getTypeSignature(), gmType);
	}

	private FlatEntityType acquireFlatEntityType(GmEntityTypeInfo gmEntityTypeInfo) {
		GmEntityType gmEntityType = gmEntityTypeInfo.addressedType();
		String typeSignature = gmEntityType.getTypeSignature();

		return (FlatEntityType) flatModel.flatCustomTypes.computeIfAbsent(typeSignature, s -> new FlatEntityType(gmEntityType, flatModel));
	}

	private FlatEnumType acquireFlatEnumType(GmEnumTypeInfo gmEnumTypeInfo) {
		GmEnumType gmEnumType = gmEnumTypeInfo.addressedType();
		String typeSignature = gmEnumType.getTypeSignature();

		return (FlatEnumType) flatModel.flatCustomTypes.computeIfAbsent(typeSignature, s -> new FlatEnumType(gmEnumType, flatModel));
	}

}
