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
package com.braintribe.model.io.metamodel.render.context;

import static com.braintribe.model.io.metamodel.render.context.SourceWriterTools.getGlobalIdAnnotationSource;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.GlobalId;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.TypeRestriction;
import com.braintribe.model.generic.annotation.meta.api.synthesis.AnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.synthesis.MdaSynthesis;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.io.metamodel.render.SourceWriterContext;
import com.braintribe.model.io.metamodel.render.info.MetaModelInfo;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.restriction.GmTypeRestriction;
import com.braintribe.model.processing.itw.InitializerTools;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * NOTE: All superclasses/interfaces names are fullNames (including package prefix), except when these are inside same package as given EntityType.
 * Annotations are simple names only.
 */
public class EntityTypeContextBuilder {

	private static final Logger log = Logger.getLogger(EntityTypeContextBuilder.class);

	private final SourceWriterContext context;
	private final GmEntityType gmEntityType;
	private final TypeSignatureResolver typeSignatureResolver;

	private final ImportManager im;
	private final EntityTypeContext result = new EntityTypeContext();

	public EntityTypeContextBuilder(SourceWriterContext context, TypeSignatureResolver typeSignatureResolver, GmEntityType gmEntityType,
			MetaModelInfo metaModelInfo) {

		this.context = context;
		this.gmEntityType = gmEntityType;
		this.typeSignatureResolver = typeSignatureResolver;

		this.result.typeInfo = metaModelInfo.getInfoForEntityType(gmEntityType);
		this.result.importManager = im = new ImportManager(result.typeInfo.packageName);
	}

	public EntityTypeContext build() {
		importTypesForTypeLiteral();

		setHeaderInfo();
		setProperties();
		setAnnotations();
		setEvaluatesToIfEligible();

		return result;
	}

	private void importTypesForTypeLiteral() {
		im.useType(EntityType.class);
		im.useType(EntityTypes.class);
	}

	private void setHeaderInfo() {
		result.superInterfaces = getDependedTypeNames(result.typeInfo.superTypes);
		result.isAbstract = Boolean.TRUE.equals(gmEntityType.getIsAbstract());
	}

	private void setAnnotations() {
		List<String> annotations = newList();

		if (Boolean.TRUE.equals(gmEntityType.getIsAbstract())) {
			addAnnotationImport(Abstract.class);
			annotations.add(Abstract.class.getSimpleName());
		}

		if (SourceWriterTools.elmentNeedsGlobalId(gmEntityType)) {
			addAnnotationImport(GlobalId.class);
			annotations.add(getGlobalIdAnnotationSource(gmEntityType.getGlobalId()));
		}

		for (AnnotationDescriptor annotationDescriptor : MdaSynthesis.synthesizeMetaDataAnnotations(gmEntityType)) {
			addAnnotationImport(annotationDescriptor.getAnnotationClass());
			annotationDescriptor.withSourceCode(annotations::add);
		}

		result.annotations = annotations;
	}

	private void setProperties() {
		List<GmProperty> gmProperties = context.modelOracle.getEntityTypeOracle(gmEntityType) //
				.getProperties() //
				.asGmProperties() //
				.collect(Collectors.toList());

		if (gmProperties.isEmpty())
			return;

		List<PropertyDescriptor> properties = newList();

		for (GmProperty gmProperty : gmProperties) {
			GmEntityType declaringSuperType = PropertyFilteringUtils.findDeclaringTypeIfInherited(gmEntityType, gmProperty);

			if (!PropertyFilteringUtils.propertyNeedsToBeImplemented(gmProperty, gmEntityType, declaringSuperType))
				continue;

			JavaType propertyType = getPropertyTypeSafe(gmProperty);
			if (propertyType == null)
				continue;

			List<String> annotations = preparePropertyAnnotations(gmProperty);

			properties.add(new PropertyDescriptor(gmProperty, propertyType, declaringSuperType != null, annotations));
		}

		Collections.sort(properties);

		result.properties = properties;
	}

	private JavaType getPropertyTypeSafe(GmProperty gmProperty) {
		try {
			JavaType propertyType = getPropertyType(gmProperty);

			// if property's nullable's not set, and if propertyType's one of the basic simple string, AND there's an
			// internal, non-nullable
			// type, use that.
			if (gmProperty.getNullable() == false)
				propertyType = getNonNullableType(propertyType);

			return propertyType;

		} catch (Exception e) {
			log.warn("Error occured while preparing property ' " + gmProperty.getName() + "' for rendering as: " + e.getMessage());

			return null;
		}
	}

	private List<String> preparePropertyAnnotations(GmProperty gmProperty) {
		List<String> result = newList();

		if (gmProperty.getInitializer() != null) {
			addAnnotationImport(Initializer.class);
			result.add("Initializer(\"" + stringifyInitializer(gmProperty) + "\")");
		}

		if (SourceWriterTools.elmentNeedsGlobalId(gmProperty)) {
			addAnnotationImport(GlobalId.class);
			result.add(getGlobalIdAnnotationSource(gmProperty.getGlobalId()));
		}

		if (gmProperty.getTypeRestriction() != null) {
			addAnnotationImport(TypeRestriction.class);
			result.add(stringfyTypeRestriction(gmProperty.getTypeRestriction()));
		}

		MdaSynthesis.synthesizeMetaDataAnnotations(gmProperty).forEach((AnnotationDescriptor ad) -> {
			addAnnotationImport(ad.getAnnotationClass());
			ad.withSourceCode(result::add);
		});

		return result;
	}

	private String stringifyInitializer(GmProperty gmProperty) {
		Object initializer = gmProperty.getInitializer();

		if (gmProperty.getType().isGmEnum() && initializer instanceof EnumReference)
			return ((EnumReference) initializer).getConstant();
		else
			return InitializerTools.stringifyInitializer(initializer);
	}

	// E.g.: TypeRestriction(value={A.class, B.class}, key={C.class, D.class}, allowVd=false, allowKeyVd=true)
	private static String stringfyTypeRestriction(GmTypeRestriction typeRestriction) {
		// @formatter:off
		return new StringJoiner(",", "TypeRestriction(", ")")
			.add(param("value", classArray(typeRestriction.getTypes())))
			.add(param("key", classArray(typeRestriction.getKeyTypes())))
			.add(param("allowVd", "" + typeRestriction.getAllowVd()))
			.add(param("allowKeyVd", "" + typeRestriction.getAllowKeyVd()))
			.toString();
		// @formatter:on
	}

	// E.g.: {A.class, B.class}
	private static String classArray(List<GmType> types) {
		StringJoiner stringJoiner = new StringJoiner(",", "{", "}");
		for (GmType gmType : types)
			stringJoiner.add(getJavaTypeSignature(gmType) + ".class");

		return stringJoiner.toString();
	}

	private static String getJavaTypeSignature(GmType type) {
		return type.isGmCustom() ? type.getTypeSignature() : type.reflectionType().getJavaType().getName();
	}

	private static String param(String name, String value) {
		return name + "=" + value;
	}

	/**
	 * find the non-nullable (i.e. primitive) representation for this type<br/>
	 * java.lang.Boolean -> boolean java.lang.Integer -> int
	 * 
	 * @param type
	 *            - the type as String (fully qualified, as set via the type reflection)
	 * @return - the non-nullable representation
	 */
	private JavaType getNonNullableType(JavaType type) {
		switch (type.rawType) {
			case "java.lang.Boolean":
				return JavaType.booleanType;
			case "java.lang.Integer":
				return JavaType.intType;
			case "java.lang.Double":
				return JavaType.doubleType;
			case "java.lang.Float":
				return JavaType.floatType;
			case "java.lang.Long":
				return JavaType.longType;

			// not supported in models, but anyway here
			case "java.lang.Short":
				return JavaType.shortType;
			case "java.lang.Byte":
				return JavaType.byteType;
			case "java.lang.Char":
				return JavaType.charType;
		}

		return type;
	}

	private JavaType getPropertyType(GmProperty gmProperty) {
		GmType propertyType = gmProperty.getType();

		if (propertyType == null)
			throw new IllegalStateException("Property type is nulle. Property: " + gmProperty);

		return getJavaType(propertyType);
	}

	private void addAnnotationImport(Class<? extends Annotation> annotationClass) {
		im.useType(annotationClass.getName());
	}

	private void setEvaluatesToIfEligible() {
		GmType evaluatesTo = gmEntityType.getEvaluatesTo();

		if (evaluatesTo == null)
			return;

		im.useType(Override.class);
		im.useType(EvalContext.class);
		im.useType(Evaluator.class);
		im.useType(ServiceRequest.class);

		result.evaluatesTo = getJavaType(evaluatesTo);
	}

	private List<JavaType> getDependedTypeNames(Collection<? extends GmType> gmTypes) {
		if (gmTypes == null)
			return null;

		List<JavaType> result = newList(gmTypes.size());

		for (GmType gmType : gmTypes)
			result.add(getJavaType(gmType));

		return result;
	}

	private JavaType getJavaType(GmType gmType) {
		JavaType javaType = typeSignatureResolver.resolveJavaType(gmType);

		im.useType(javaType.rawType);
		im.useType(javaType.keyType);
		im.useType(javaType.valueType);

		return javaType;
	}

}
