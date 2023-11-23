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
package com.braintribe.model.processing.meta.cmd.index;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.Set;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.data.EnumTypeMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.ModelMetaData;
import com.braintribe.model.meta.data.PropertyMetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.index.ConcurrentCachedIndex;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.context.ResolutionContext;
import com.braintribe.model.processing.meta.cmd.resolvers.ModelMdAggregator;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataBox;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataTools;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.EnumConstantOracle;
import com.braintribe.model.processing.meta.oracle.EnumTypeOracle;
import com.braintribe.model.processing.meta.oracle.PropertyOracle;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;
import com.braintribe.model.processing.meta.oracle.TypeHierarchy.Order;

/**
 * A structure containing model meta-data indexed according to it's type, ignoring any meta-data from super-types (see
 * not at the bottom of this javadoc).
 * <p>
 * Naming convention: There are two types on indices, ones that map to GM objects like entities and properties, and ones
 * that map to {@link MetaData} of these objects. The letter always contain the "Md" as part of their name, e.g.
 * {@link EntityMdIndex}.
 * <p>
 * Structure: Top class is {@link ModelMdIndex}, which not only maps to {@link ModelMetaData}, but also to entities and
 * enums of the model.
 * <p>
 * {@link EntityIndex} is index of all entities of the model. For given type signature returns the {@link EntityMdIndex}
 * - an index for {@link EntityTypeMetaData} of given entity. This {@linkplain EntityMdIndex} also maps to
 * {@linkplain PropertyIndex}.
 * <p>
 * {@link EnumIndex} is index of all enums of the model. For given type signature returns the {@link EnumMdIndex} - an
 * index for {@link EnumTypeMetaData} of given enum.
 * <p>
 * {@link PropertyIndex} is index of all properties of given entity. For given property name returns the
 * {@link PropertyMdIndex} - an index for {@link PropertyMetaData} corresponding to given property.
 * <p>
 * Note that all of the MD indices correspond 1:1 to what the {@linkplain MetaData} was defined in the model, i.e. there
 * is no merging with supertypes done here. This only serves as an access to the various {@linkplain MetaData} (although
 * some minor optimization is being done here - removing some obviously inactive {@linkplain MetaData}). To see the
 * "real" cache for meta data check {@link ModelMdAggregator} .
 */
public class MetaDataIndexStructure {

	public static ModelMdIndex newModelMdIndex(ResolutionContext resolutionContext) {
		return new ModelMdIndex(resolutionContext);
	}

	public static abstract class MdIndex extends ConcurrentCachedIndex<MetaDataIndexKey, MetaDataBox> {
		protected ResolutionContext resolutionContext;

		MdIndex(ResolutionContext resolutionContext) {
			this.resolutionContext = resolutionContext;
		}

		public MetaDataBox acquireMetaData(MetaDataIndexKey indexKey) {
			return acquireFor(indexKey);
		}

		@Override
		protected MetaDataBox provideValueFor(MetaDataIndexKey indexKey) {
			MetaDataBox allMetaData = getAllMetaData();

			if (allMetaData == null) {
				return MetaDataBox.EMPTY_BOX;
			}

			if (indexKey.inheritableOnly()) {
				return filterInheritableOnly(acquireMetaData(indexKey.indexKeyForAll()));

			} else {
				List<QualifiedMetaData> normalMetaData = filterMdByTypeAndStaticSelector(allMetaData.normalMetaData, indexKey);
				List<QualifiedMetaData> importantMetaData = filterMdByTypeAndStaticSelector(allMetaData.importantMetaData, indexKey);

				return new MetaDataBox(normalMetaData, importantMetaData);
			}
		}

		private List<QualifiedMetaData> filterMdByTypeAndStaticSelector(List<QualifiedMetaData> metaData, MetaDataIndexKey indexKey) {
			return resolutionContext.filterByStaticSelectors(MetaDataTools.prepareMetaDataForType(metaData, indexKey.mdType()));
		}

		private MetaDataBox filterInheritableOnly(MetaDataBox allMd) {
			List<QualifiedMetaData> normalMetaData = filterInheritableOnly(allMd.normalMetaData);
			List<QualifiedMetaData> importantMetaData = filterInheritableOnly(allMd.importantMetaData);

			return new MetaDataBox(normalMetaData, importantMetaData);
		}

		private List<QualifiedMetaData> filterInheritableOnly(List<QualifiedMetaData> allMd) {
			List<QualifiedMetaData> filteredMd = newList();
			boolean areSame = true;

			for (QualifiedMetaData qmd : allMd) {
				if (qmd.metaData().getInherited()) {
					filteredMd.add(qmd);
				} else {
					areSame = false;
				}
			}

			return areSame ? allMd : filteredMd;
		}

		protected abstract MetaDataBox getAllMetaData();
	}

	/** Index: {@code Class<? extends ModelMetaData>} -> {@code List<ModelMetaData> } */
	public static class ModelMdIndex extends MdIndex {
		public final EntityIndex entityIndex;
		public final EnumIndex enumIndex;
		public final ModelEnumMdIndex modelEnumMdIndex;
		public final ModelConstantMdIndex modelConstantMdIndex;

		public ModelMdIndex(ResolutionContext resolutionContext) {
			super(resolutionContext);

			this.entityIndex = new EntityIndex(resolutionContext);
			this.enumIndex = new EnumIndex(this, resolutionContext);
			this.modelEnumMdIndex = new ModelEnumMdIndex(resolutionContext);
			this.modelConstantMdIndex = new ModelConstantMdIndex(resolutionContext);
		}

		@Override
		protected MetaDataBox getAllMetaData() {
			return MetaDataBox.forNormalMdOnly(resolutionContext.modelOracle.getQualifiedMetaData());
		}

		public EntityMdIndex acquireEntityMdIndex(String typeSignature) {
			return entityIndex.acquireFor(typeSignature);
		}

		public EnumMdIndex acquireEnumMdIndex(String typeSignature) {
			return enumIndex.acquireFor(typeSignature);
		}

	}

	/** Index: for {@link MetaData} defined by {@link GmMetaModel#getEnumTypeMetaData()}. */
	public static class ModelEnumMdIndex extends MdIndex {

		public ModelEnumMdIndex(ResolutionContext resolutionContext) {
			super(resolutionContext);
		}

		@Override
		protected MetaDataBox getAllMetaData() {
			return MetaDataBox.forPrioritizable(resolutionContext.modelOracle.getQualifiedEnumMetaData());
		}
	}

	/** Index: for {@link MetaData} defined by {@link GmMetaModel#getEnumConstantMetaData()}. */
	public static class ModelConstantMdIndex extends MdIndex {

		public ModelConstantMdIndex(ResolutionContext resolutionContext) {
			super(resolutionContext);
		}

		@Override
		protected MetaDataBox getAllMetaData() {
			return MetaDataBox.forPrioritizable(resolutionContext.modelOracle.getQualifiedEnumConstantMetaData());
		}
	}

	/** Index: enumSignature -> {@link EnumMdIndex} */
	private static class EnumIndex extends ConcurrentCachedIndex<String, EnumMdIndex> {
		public final ResolutionContext resolutionContext;
		public final ModelMdIndex modelMdIndex;

		public EnumIndex(ModelMdIndex modelMdIndex, ResolutionContext resolutionContext) {
			this.modelMdIndex = modelMdIndex;
			this.resolutionContext = resolutionContext;
		}

		@Override
		protected EnumMdIndex provideValueFor(String typeSignature) {
			if (isEmpty(typeSignature)) {
				throw new CascadingMetaDataException("Type signature cannot be empty!");
			}

			EnumTypeOracle enumOracle = resolutionContext.modelOracle.getEnumTypeOracle(typeSignature);

			return new EnumMdIndex(modelMdIndex, enumOracle, resolutionContext);
		}
	}

	/** Index: for {@link MetaData} of {@link GmEntityTypeInfo}. */
	public static class EnumMdIndex extends MdIndex {
		public final ModelMdIndex modelMdIndex;
		public final EnumTypeOracle enumOracle;
		public final EnumConstantIndex enumConstantIndex;
		public final GlobalEnumConstantMdIndex globalEnumConstantMdIndex;

		public EnumMdIndex(ModelMdIndex modelMdIndex, EnumTypeOracle enumOracle, ResolutionContext resolutionContext) {
			super(resolutionContext);

			this.modelMdIndex = modelMdIndex;
			this.enumOracle = enumOracle;
			this.enumConstantIndex = new EnumConstantIndex(this, enumOracle, resolutionContext);
			this.globalEnumConstantMdIndex = new GlobalEnumConstantMdIndex(enumOracle, resolutionContext);
		}

		@Override
		protected MetaDataBox getAllMetaData() {
			return MetaDataBox.forNormalMdOnly(enumOracle.getQualifiedMetaData());
		}

		public EnumConstantMdIndex acquireEnumConstantMdIndex(String constant) {
			return enumConstantIndex.acquireFor(constant);
		}

		public GmEnumType getGmEnumType() {
			return enumOracle.asGmType();
		}
	}

	/** Index: constantName -> {@link EnumConstantMdIndex} */
	private static class EnumConstantIndex extends ConcurrentCachedIndex<String, EnumConstantMdIndex> {
		public final EnumMdIndex enumMdIndex;
		public final EnumTypeOracle enumOracle;
		public final ResolutionContext resolutionContext;

		public EnumConstantIndex(EnumMdIndex enumMdIndex, EnumTypeOracle enumOracle, ResolutionContext resolutionContext) {
			this.enumMdIndex = enumMdIndex;
			this.enumOracle = enumOracle;
			this.resolutionContext = resolutionContext;
		}

		@Override
		protected EnumConstantMdIndex provideValueFor(String constant) {
			if (isEmpty(constant)) {
				throw new CascadingMetaDataException("Property name cannot be empty!");
			}

			EnumConstantOracle constantOracle = enumOracle.getConstant(constant);

			return new EnumConstantMdIndex(enumMdIndex, constantOracle, resolutionContext);
		}
	}

	/** Index: for {@link MetaData} defined by {@link GmEnumType#getEnumConstantMetaData()}. */
	public static class GlobalEnumConstantMdIndex extends MdIndex {
		public final EnumTypeOracle enumOracle;

		public GlobalEnumConstantMdIndex(EnumTypeOracle enumOracle, ResolutionContext resolutionContext) {
			super(resolutionContext);
			this.enumOracle = enumOracle;
		}

		@Override
		protected MetaDataBox getAllMetaData() {
			return MetaDataBox.forPrioritizable(enumOracle.getQualifiedConstantMetaData());
		}

		public GmEnumType getGmEnumType() {
			return enumOracle.asGmType();
		}

	}

	/** Index: for {@link MetaData} of {@link GmEnumConstantInfo}. */
	public static class EnumConstantMdIndex extends MdIndex {
		public final EnumMdIndex enumMdIndex;
		public final EnumConstantOracle constantOracle;

		public EnumConstantMdIndex(EnumMdIndex enumMdIndex, EnumConstantOracle constantOracle, ResolutionContext resolutionContext) {
			super(resolutionContext);

			this.enumMdIndex = enumMdIndex;
			this.constantOracle = constantOracle;
		}

		@Override
		protected MetaDataBox getAllMetaData() {
			return MetaDataBox.forPrioritizable(constantOracle.getQualifiedMetaData());
		}

		public GmEnumConstant getGmEnumConstant() {
			return constantOracle.asGmEnumConstant();
		}
	}

	/** Index: entitySignature -> {@link EntityMdIndex} */
	private static class EntityIndex extends ConcurrentCachedIndex<String, EntityMdIndex> {
		public final ResolutionContext resolutionContext;

		public EntityIndex(ResolutionContext resolutionContext) {
			this.resolutionContext = resolutionContext;
		}

		@Override
		protected EntityMdIndex provideValueFor(String typeSignature) {
			if (isEmpty(typeSignature)) {
				throw new CascadingMetaDataException("Type signature cannot be empty!");
			}

			EntityTypeOracle entityOracle = resolutionContext.modelOracle.getEntityTypeOracle(typeSignature);

			return new EntityMdIndex(this, entityOracle, resolutionContext);
		}
	}

	/** Index: for {@link MetaData} of {@link GmEntityTypeInfo}. */
	public static class EntityMdIndex extends MdIndex {
		public final EntityIndex entityIndex;
		public final EntityTypeOracle entityOracle;
		public final PropertyIndex propertyIndex;
		public final GlobalPropertyMdIndex globalPropertyMdIndex;

		public EntityMdIndex(EntityIndex entityIndex, EntityTypeOracle entityOracle, ResolutionContext resolutionContext) {
			super(resolutionContext);
			this.entityIndex = entityIndex;
			this.entityOracle = entityOracle;
			this.propertyIndex = new PropertyIndex(this, entityOracle, resolutionContext);
			this.globalPropertyMdIndex = new GlobalPropertyMdIndex(entityOracle, resolutionContext);
		}

		@Override
		protected MetaDataBox getAllMetaData() {
			return MetaDataBox.forPrioritizable(entityOracle.getQualifiedMetaData());
		}

		public GmEntityType getGmEntityType() {
			return entityOracle.asGmType();
		}

		public Set<GmEntityType> getAllSuperTypes() {
			return entityOracle.getSuperTypes().transitive().sorted(Order.subFirst).asGmTypes();
		}

		public boolean hasProperty(String propertyName) {
			return entityOracle.hasProperty(propertyName);
		}

		public PropertyMdIndex acquirePropertyMdIndex(String propertyName) {
			return propertyIndex.acquireFor(propertyName);
		}

		public EntityMdIndex acquireOtherEntityIndex(String typeSignature) {
			return entityIndex.acquireFor(typeSignature);
		}
	}

	/** Index: propertyName -> {@link PropertyMdIndex} */
	private static class PropertyIndex extends ConcurrentCachedIndex<String, PropertyMdIndex> {
		public final EntityMdIndex entityMdIndex;
		public final EntityTypeOracle entityOracle;
		public final ResolutionContext resolutionContext;

		public PropertyIndex(EntityMdIndex entityMdIndex, EntityTypeOracle entityOracle, ResolutionContext resolutionContext) {
			this.entityMdIndex = entityMdIndex;
			this.entityOracle = entityOracle;
			this.resolutionContext = resolutionContext;
		}

		@Override
		protected PropertyMdIndex provideValueFor(String propertyName) {
			if (isEmpty(propertyName)) {
				throw new CascadingMetaDataException("Property name cannot be empty!");
			}

			PropertyOracle propertyOracle = entityOracle.getProperty(propertyName);

			return new PropertyMdIndex(entityMdIndex, propertyOracle, resolutionContext);
		}
	}

	/** Index: for {@link MetaData} defined by {@link GmEntityType#getPropertyMetaData()}. */
	public static class GlobalPropertyMdIndex extends MdIndex {
		public final EntityTypeOracle entityOracle;

		public GlobalPropertyMdIndex(EntityTypeOracle entityOracle, ResolutionContext resolutionContext) {
			super(resolutionContext);
			this.entityOracle = entityOracle;
		}

		@Override
		protected MetaDataBox getAllMetaData() {
			return MetaDataBox.forPrioritizable(entityOracle.getQualifiedPropertyMetaData());
		}

		public GmEntityType getGmEntityType() {
			return entityOracle.asGmType();
		}
	}

	/** Index: for {@link MetaData} of {@link GmPropertyInfo}. */
	public static class PropertyMdIndex extends MdIndex {
		public final EntityMdIndex entityMdIndex;
		public final PropertyOracle propertyOracle;

		public PropertyMdIndex(EntityMdIndex entityMdIndex, PropertyOracle propertyOracle, ResolutionContext resolutionContext) {
			super(resolutionContext);

			this.entityMdIndex = entityMdIndex;
			this.propertyOracle = propertyOracle;
		}

		@Override
		protected MetaDataBox getAllMetaData() {
			return MetaDataBox.forPrioritizable(propertyOracle.getQualifiedMetaData());
		}

		public String getPropertyName() {
			return propertyOracle.getName();
		}

		public GmProperty getGmProperty() {
			return propertyOracle.asGmProperty();
		}
	}

	static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}
}
