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
package com.braintribe.testing.tools.gm.meta;

import com.braintribe.model.meta.GmMetaModel;

/**
 * Helper class to build a {@link GmMetaModel} and related types.
 */
public class MetaModelBuilder {

//	public static final String[] SIMPLE_TYPE_SIGNATURES = new String[] { "boolean", "integer", "long", "decimal", "float", "double", "string",
//			"date" };
//
//	private static Map<String, GmType> typeRegistry = new HashMap<>();
//	private static Map<String, GmMetaModel> modelRegistry = new HashMap<>();
//
//	private MetaModelBuilder() {
//	}
//
//	private static void registerType(GmType type) {
//		if (type != null) {
//			typeRegistry.put(type.getTypeSignature(), type);
//		}
//	}
//
//	private static GmType getRegisteredType(String id) {
//		if (!typeRegistry.containsKey(id)) {
//			throw new RuntimeException("no GmType '" + id + "' registered");
//		}
//		return typeRegistry.get(id);
//	}
//
//	private static void registerModel(GmMetaModel model) {
//		if (model != null) {
//			modelRegistry.put(model.getName(), model);
//		}
//	}
//
//	private static GmMetaModel getRegisteredModel(String id) {
//		if (!modelRegistry.containsKey(id)) {
//			throw new RuntimeException("no GmMetaModel '" + id + "' registered");
//		}
//		return modelRegistry.get(id);
//	}
//
//	public static GmMetaModelBuilder<NoopBuilder> model() {
//		return new GmMetaModelBuilder<>(new NoopBuilder(), new WrappingBuilderSetter<GmMetaModel>() {
//			@Override
//			public void set(GmMetaModel o) {
//				/* noop it is */
//			}
//		});
//	}
//
//	public static class NoopBuilder implements Builder<Object> {
//		@Override
//		public Object build() {
//			return null;
//		}
//	}
//
//	public static GmEntityTypeBuilder<NoopBuilder> entityType() {
//		return new GmEntityTypeBuilder<>(new NoopBuilder(), new WrappingBuilderSetter<GmEntityType>() {
//			@Override
//			public void set(GmEntityType o) {
//				/* noop it is */
//			}
//		});
//	}
//
//	public static BindingArtifact artifactBinding(String groupId, String artifactId, String version) {
//		BindingArtifact ab = GMF.createEntity(BindingArtifact.class);
//		ab.setGroupId(groupId);
//		ab.setArtifactId(artifactId);
//		ab.setVersion(version);
//		return ab;
//	}
//
//	public static class GmMetaModelBuilder<B> extends DefaultWrappableBuilder<B, GmMetaModel> {
//
//		private GmMetaModel mm = GMF.createEntity(GmMetaModel.class);
//
//		GmMetaModelBuilder(B wrappingBuilder, WrappingBuilderSetter<GmMetaModel> setter) {
//			super(wrappingBuilder, setter);
//		}
//
//		public GmMetaModelBuilder<B> withName(String name) {
//			mm.setName(name);
//			registerModel(mm);
//			return this;
//		}
//
//		public GmMetaModelBuilder<B> withModelExposure(ModelExposure modelExposure) {
//			mm.setModelExposure(modelExposure);
//			return this;
//		}
//
//		public GmMetaModelBuilder<B> withDependencyModel(GmMetaModel model) {
//			if (mm.getDependencies() == null) {
//				mm.setDependencies(new HashSet<GmMetaModel>());
//			}
//			mm.getDependencies().add(model);
//			return this;
//		}
//
//		public GmMetaModelBuilder<B> withDependencyModelLookedup(String modelId) {
//			GmMetaModel m = getRegisteredModel(modelId);
//			return withDependencyModel(m);
//		}
//
//		public GmMetaModelBuilder<GmMetaModelBuilder<B>> withDependencyModelFromBuilder() {
//			return new GmMetaModelBuilder<>(this, new WrappingBuilderSetter<GmMetaModel>() {
//				@Override
//				public void set(GmMetaModel o) {
//					withDependencyModel(o);
//				}
//			});
//		}
//
//		public GmMetaModelBuilder<B> withArtifactBinding(BindingArtifact artifactBinding) {
//			mm.setArtifactBinding(artifactBinding);
//			return this;
//		}
//
//		public GmMetaModelBuilder<B> withBaseType(GmBaseType baseType) {
//			mm.setBaseType(baseType);
//			registerType(baseType);
//			return this;
//		}
//
//		public GmMetaModelBuilder<B> withBaseTypeSignature(String baseTypeSignature) {
//			GmBaseType baseType = GMF.createEntity(GmBaseType.class);
//			baseType.setTypeSignature(baseTypeSignature);
//			mm.setBaseType(baseType);
//			registerType(baseType);
//			return this;
//		}
//
//		public GmMetaModelBuilder<B> withStandardBaseType() {
//			withBaseTypeSignature("object");
//			return this;
//		}
//
//		public GmMetaModelBuilder<B> withSimpleType(GmSimpleType simpleType) {
//			if (mm.getSimpleTypes() == null) {
//				mm.setSimpleTypes(new HashSet<GmSimpleType>());
//			}
//			mm.getSimpleTypes().add(simpleType);
//			registerType(simpleType);
//			return this;
//		}
//
//		public GmMetaModelBuilder<B> withSimpleTypeSignature(String simpleTypeSignature) {
//			GmSimpleType st = GMF.createEntity(GmSimpleType.class);
//			st.setTypeSignature(simpleTypeSignature);
//			withSimpleType(st);
//			registerType(st);
//			return this;
//		}
//
//		public GmMetaModelBuilder<B> withStandardSimpleTypes() {
//			for (String s : SIMPLE_TYPE_SIGNATURES) {
//				withSimpleTypeSignature(s);
//			}
//			return this;
//		}
//
//		public GmMetaModelBuilder<B> withEntityType(GmEntityType entityType) {
//			if (mm.getEntityTypes() == null) {
//				mm.setEntityTypes(new HashSet<GmEntityType>());
//			}
//			mm.getEntityTypes().add(entityType);
//			return this;
//		}
//
//		public GmMetaModelBuilder<B> withEntityTypes(GmEntityType... entityTypes) {
//			if (entityTypes != null) {
//				for (GmEntityType entityType : entityTypes) {
//					withEntityType(entityType);
//				}
//			}
//			return this;
//		}
//
//		public GmEntityTypeBuilder<GmMetaModelBuilder<B>> withEntityTypeFromBuilder() {
//			return new GmEntityTypeBuilder<>(this, new WrappingBuilderSetter<GmEntityType>() {
//				@Override
//				public void set(GmEntityType o) {
//					withEntityType(o);
//				}
//			});
//		}
//
//		public GmMetaModelBuilder<B> withEnumType(GmEnumType enumType) {
//			if (mm.getEnumTypes() == null) {
//				mm.setEnumTypes(new HashSet<GmEnumType>());
//			}
//			mm.getEnumTypes().add(enumType);
//			return this;
//		}
//
//		public GmEnumTypeBuilder<GmMetaModelBuilder<B>> withEnumTypeFromBuilder() {
//			return new GmEnumTypeBuilder<>(this, new WrappingBuilderSetter<GmEnumType>() {
//				@Override
//				public void set(GmEnumType o) {
//					withEnumType(o);
//				}
//			});
//		}
//
//		public GmMetaModelBuilder<B> withModelMetaData(ModelMetaData modelMetaData) {
//			if (mm.getMetaData() == null) {
//				mm.setMetaData(new HashSet<ModelMetaData>());
//			}
//			mm.getMetaData().add(modelMetaData);
//			return this;
//		}
//
//		@Override
//		public GmMetaModel build() {
//			return mm;
//		}
//	}
//
//	public static class GmEntityTypeBuilder<B> extends DefaultWrappableBuilder<B, GmEntityType> {
//		private GmEntityType et = GMF.createEntity(GmEntityType.class);
//
//		public GmEntityTypeBuilder(B wrappingBuilder, WrappingBuilderSetter<GmEntityType> setter) {
//			super(wrappingBuilder, setter);
//		}
//
//		public GmEntityTypeBuilder<B> withTypeSignature(String typeSignature) {
//			et.setTypeSignature(typeSignature);
//			registerType(et);
//			return this;
//		}
//
//		public GmEntityTypeBuilder<B> withArtifactBinding(BindingArtifact artifactBinding) {
//			et.setArtifactBinding(artifactBinding);
//			return this;
//		}
//
//		public GmEntityTypeBuilder<B> withIsAbstract(boolean isAbstract) {
//			et.setIsAbstract(isAbstract);
//			return this;
//		}
//
//		public GmEntityTypeBuilder<B> withDeclaringModel(GmMetaModel metaModel) {
//			et.setDeclaringModel(metaModel);
//			return this;
//		}
//
//		public GmEntityTypeBuilder<B> withDeclaringModelLookedup(String id) {
//			GmMetaModel m = modelRegistry.get(id);
//			et.setDeclaringModel(m);
//			return this;
//		}
//
//		public GmEntityTypeBuilder<B> withSuperType(GmEntityType superType) {
//			if (et.getSuperTypes() == null) {
//				et.setSuperTypes(new ArrayList<GmEntityType>());
//			}
//			et.getSuperTypes().add(superType);
//			return this;
//		}
//
//		public GmEntityTypeBuilder<GmEntityTypeBuilder<B>> withSuperTypeFromBuilder() {
//			return new GmEntityTypeBuilder<>(this, new WrappingBuilderSetter<GmEntityType>() {
//				@Override
//				public void set(GmEntityType o) {
//					withSuperType(o);
//				}
//			});
//		}
//
//		public GmEntityTypeBuilder<B> withSuperTypeLookedup(String id) {
//			GmType superType = getRegisteredType(id);
//			withSuperType((GmEntityType) superType);
//			return this;
//		}
//
//		public GmEntityTypeBuilder<B> withProperty(GmProperty property) {
//			if (et.getProperties() == null) {
//				et.setProperties(new ArrayList<GmProperty>());
//			}
//			et.getProperties().add(property);
//			return this;
//		}
//
//		public GmPropertyBuilder<GmEntityTypeBuilder<B>> withPropertyFromBuilder() {
//			GmPropertyBuilder<GmEntityTypeBuilder<B>> gmPropertyBuilder = new GmPropertyBuilder<>(this, new WrappingBuilderSetter<GmProperty>() {
//				@Override
//				public void set(GmProperty o) {
//					withProperty(o);
//				}
//			});
//			gmPropertyBuilder.withEntityType(et);
//			return gmPropertyBuilder;
//		}
//
//		@Override
//		public GmEntityType build() {
//			return et;
//		}
//
//		public GmEntityTypeBuilder<B> withMetaData(EntityTypeMetaData entityTypeMetaData) {
//			if (et.getMetaData() == null) {
//				et.setMetaData(new HashSet<EntityTypeMetaData>());
//			}
//			et.getMetaData().add(entityTypeMetaData);
//			return this;
//		}
//	}
//
//	public static class GmPropertyBuilder<B> extends DefaultWrappableBuilder<B, GmProperty> {
//		private GmProperty p = GMF.createEntity(GmProperty.class);
//
//		GmPropertyBuilder(B wrappingBuilder, WrappingBuilderSetter<GmProperty> setter) {
//			super(wrappingBuilder, setter);
//		}
//
//		@Override
//		public GmProperty build() {
//			return p;
//		}
//
//		public GmPropertyBuilder<B> withName(String name) {
//			p.setName(name);
//			return this;
//		}
//
//		public GmPropertyBuilder<B> withIsOverlay(boolean isOverlay) {
//			p.setIsOverlay(isOverlay);
//			return this;
//		}
//
//		public GmPropertyBuilder<B> withType(GmType type) {
//			p.setType(type);
//			return this;
//		}
//
//		public GmPropertyBuilder<B> withTypeLookedup(String id) {
//			p.setType(getRegisteredType(id));
//			return this;
//		}
//
//		public GmPropertyBuilder<B> withSetOfElementTypeLookedup(String id) {
//			GmType elementType = getRegisteredType(id);
//			GmSetType set = GMF.createEntity(GmSetType.class);
//			set.setElementType(elementType);
//			p.setType(set);
//			return this;
//		}
//
//		public GmPropertyBuilder<B> withSetOfElementType(GmType elementType) {
//			GmSetType set = GMF.createEntity(GmSetType.class);
//			set.setElementType(elementType);
//			p.setType(set);
//			return this;
//		}
//
//		public GmPropertyBuilder<B> withListOfElementTypeLookedup(String id) {
//			GmType elementType = getRegisteredType(id);
//			GmListType list = GMF.createEntity(GmListType.class);
//			list.setElementType(elementType);
//			p.setType(list);
//			return this;
//		}
//
//		public GmPropertyBuilder<B> withListOfElementType(GmType elementType) {
//			GmListType list = GMF.createEntity(GmListType.class);
//			list.setElementType(elementType);
//			p.setType(list);
//			return this;
//		}
//
//		public GmPropertyBuilder<B> withMapOfElementTypesLookedup(String keySig, String valSig) {
//			GmType keyType = getRegisteredType(keySig);
//			GmType valType = getRegisteredType(valSig);
//			GmMapType map = GMF.createEntity(GmMapType.class);
//			map.setKeyType(keyType);
//			map.setValueType(valType);
//			p.setType(map);
//			return this;
//		}
//
//		public GmPropertyBuilder<B> withMapOfElementTypes(GmType keyType, GmType valType) {
//			GmMapType map = GMF.createEntity(GmMapType.class);
//			map.setKeyType(keyType);
//			map.setValueType(valType);
//			p.setType(map);
//			return this;
//		}
//
//		public GmPropertyBuilder<B> withEntityTypeLookedup(String id) {
//			p.setEntityType((GmEntityType) getRegisteredType(id));
//			return this;
//		}
//
//		public GmPropertyBuilder<B> withEntityType(GmEntityType entityType) {
//			p.setEntityType(entityType);
//			return this;
//		}
//
//		public GmPropertyBuilder<B> withIsId(boolean isId) {
//			p.setIsId(isId);
//			return this;
//		}
//
//		public GmPropertyBuilder<B> withNullable(boolean nullable) {
//			p.setNullable(nullable);
//			return this;
//		}
//
//		public GmPropertyBuilder<B> withMetaData(PropertyMetaData propertyMetaData) {
//			if (p.getMetaData() == null) {
//				p.setMetaData(new HashSet<PropertyMetaData>());
//			}
//			p.getMetaData().add(propertyMetaData);
//			return this;
//		}
//	}
//
//	public static class GmEnumTypeBuilder<B> extends DefaultWrappableBuilder<B, GmEnumType> {
//		private GmEnumType et = GMF.createEntity(GmEnumType.class);
//
//		public GmEnumTypeBuilder(B wrappingBuilder, WrappingBuilderSetter<GmEnumType> setter) {
//			super(wrappingBuilder, setter);
//		}
//
//		public GmEnumTypeBuilder<B> withTypeSignature(String typeSignature) {
//			et.setTypeSignature(typeSignature);
//			registerType(et);
//			return this;
//		}
//
//		public GmEnumTypeBuilder<B> withArtifactBinding(BindingArtifact artifactBinding) {
//			et.setArtifactBinding(artifactBinding);
//			return this;
//		}
//
//		public GmEnumTypeBuilder<B> withConstant(GmEnumConstant constant) {
//			if (et.getConstants() == null) {
//				et.setConstants(new ArrayList<GmEnumConstant>());
//			}
//			et.getConstants().add(constant);
//			return this;
//		}
//
//		public GmEnumTypeBuilder<B> withConstantWithName(String name) {
//			GmEnumConstant c = GMF.createEntity(GmEnumConstant.class);
//			c.setName(name);
//			c.setEnumType(et);
//			withConstant(c);
//			return this;
//		}
//
//		public GmEnumTypeBuilder<B> withConstantWithNameAndBackReference(String name, GmEnumType backReference) {
//			GmEnumConstant c = GMF.createEntity(GmEnumConstant.class);
//			c.setName(name);
//			c.setEnumType(backReference);
//			withConstant(c);
//			return this;
//		}
//
//		public GmEnumTypeBuilder<B> withConstantWithNameAndBackReferenceLookedup(String name, String lookupId) {
//			GmEnumConstant c = GMF.createEntity(GmEnumConstant.class);
//			c.setName(name);
//			c.setEnumType((GmEnumType) getRegisteredType(lookupId));
//			withConstant(c);
//			return this;
//		}
//
//		public GmEnumTypeBuilder<B> withDeclaringModel(GmMetaModel metaModel) {
//			et.setDeclaringModel(metaModel);
//			return this;
//		}
//
//		public GmEnumTypeBuilder<B> withDeclaringModelLookedup(String id) {
//			GmMetaModel m = modelRegistry.get(id);
//			et.setDeclaringModel(m);
//			return this;
//		}
//
//		@Override
//		public GmEnumType build() {
//			return et;
//		}
//	}
//
//	private static abstract class DefaultWrappableBuilder<B, T> implements WrappableBuilder<B>, Builder<T> {
//		private B wrappingBuilder;
//		private WrappingBuilderSetter<T> setter;
//
//		public DefaultWrappableBuilder(B wrappingBuilder, WrappingBuilderSetter<T> setter) {
//			this.wrappingBuilder = wrappingBuilder;
//			this.setter = setter;
//		}
//
//		@Override
//		public B done() {
//			T res = build();
//			setter.set(res);
//			return wrappingBuilder;
//		}
//	}
//
//	private static interface WrappingBuilderSetter<T> {
//		public void set(T o);
//	}
//
//	private static interface WrappableBuilder<B> {
//		public B done();
//	}
//
//	private static interface Builder<T> {
//		public T build();
//	}
}
