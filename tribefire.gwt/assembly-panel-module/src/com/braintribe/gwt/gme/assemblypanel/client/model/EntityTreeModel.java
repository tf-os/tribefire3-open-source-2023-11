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
package com.braintribe.gwt.gme.assemblypanel.client.model;

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.gme.assemblypanel.client.AssemblyUtil;
import com.braintribe.gwt.gme.assemblypanel.client.model.factory.ModelFactory;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.prompt.Embedded;
import com.braintribe.model.meta.data.prompt.EntityCompoundViewing;
import com.braintribe.model.meta.data.prompt.ShowAsList;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.sencha.gxt.core.shared.FastMap;

public class EntityTreeModel extends AbstractGenericTreeModel {

	private static Comparator<PropertyEntryTreeModel> priorityComparator;
	private static final String TEMP_DISPLAY_PROPERTY = "tempDisplayProperty";
	public static final String SPECIAL_STYLE_PROPERTY = "specialStyleProperty";

	private EntityType<GenericEntity> elementType;
	private ModelFactory modelFactory;
	private boolean displayAllPropertiesInMultiplex;
	private EntityType<?> baseTypeForProperties;
	private String condensedPropertyName;
	private final Map<String, TreePropertyModel> treePropertyModels = new LinkedHashMap<>();
	private final Map<String, TreePropertyModel> normalizedTreePropertyModels = new FastMap<>();
	private final Map<String, TreePropertyModel> hiddenTreePropertyModels = new FastMap<>();
	private final List<PropertyEntryTreeModel> propertyEntryTreeModels = new ArrayList<>();
	private String condensedPropertyAfterUpdate;
	private boolean completeDueToCondensation = false;
	private boolean expandPending = false;

	/**
	 * Instantiates a new EntityTreeModel.
	 * 
	 * @param objectAndType
	 *            Contains the parent GenericEntity and its type.
	 * @param displayAllPropertiesInMultiplex
	 *            Configures whether we should prepare all properties as children. If false is passed, only complex and object typed properties are prepared.
	 * @param baseTypeForProperties
	 *            If not null, we prepare {@link TreePropertyModel} for this entity.
	 * @param condensedPropertyName
	 *            If not null, we prepare a child only for that given property, ignoring all the others.
	 */
	public EntityTreeModel(ObjectAndType objectAndType, ModelFactory modelFactory, boolean displayAllPropertiesInMultiplex,
			EntityType<?> baseTypeForProperties, String condensedPropertyName) throws GenericModelException {
		this.elementType = objectAndType.getType();
		this.modelFactory = modelFactory;
		this.displayAllPropertiesInMultiplex = displayAllPropertiesInMultiplex;
		this.baseTypeForProperties = baseTypeForProperties;
		this.condensedPropertyName = condensedPropertyName;
		setModelObject(objectAndType.getObject(), objectAndType.getDepth());
	}

	/**
	 * Builds a new EntityTreeModel cloned from the given one.
	 */
	public EntityTreeModel(ObjectAndType objectAndType, EntityTreeModel model) throws GenericModelException {
		this.modelFactory = model.modelFactory;
		this.displayAllPropertiesInMultiplex = model.displayAllPropertiesInMultiplex;
		this.baseTypeForProperties = model.baseTypeForProperties;
		this.condensedPropertyName = model.condensedPropertyName;
		if (objectAndType.getObject() != null)
			this.elementType = ((GenericEntity) objectAndType.getObject()).entityType();
		else
			this.elementType = objectAndType.getType();

		if (model.getModelObject() == null) {
			setModelObject(objectAndType.getObject(), objectAndType.getDepth());
			return;
		}

		cloneProperties(objectAndType, model);
	}

	@Override
	public void setModelObject(Object modelObject, int depth) {
		setModelObject((GenericEntity) modelObject, depth, null);
	}

	@Override
	public EntityTreeModel getEntityTreeModel() {
		return this;
	}

	@Override
	public AbstractGenericTreeModel getDelegate() {
		return this;
	}

	@Override
	public EntityType<GenericEntity> getElementType() {
		return elementType;
	}
	
	/**
	 * Returns the list of {@link TreePropertyModel} for this entity model.
	 */
	public Collection<TreePropertyModel> getTreePropertyModels() {
		return treePropertyModels.values();
	}
	
	@Override
	public TreePropertyModel getTreePropertyModel(Property property) {
		return treePropertyModels.get(getPropertyKey(property));
	}
	
	public TreePropertyModel getTreePropertyModel(String propertyName) {
		return this.normalizedTreePropertyModels.get(propertyName);
	}
	
	public TreePropertyModel getHiddenTreePropertyModel(String propertyName) {
		return this.hiddenTreePropertyModels.get(propertyName);
	}
	
	public EntityType<?> getBaseTypeForProperties() {
		return baseTypeForProperties;
	}

	protected void setModelObject(GenericEntity modelObject, int depth, String currentlyCondensedProperty) {
		prepareProperties(modelObject, depth, currentlyCondensedProperty, null);
	}

	private void prepareProperties(GenericEntity entity, int depth, String currentlyCondensedProperty, EntityTreeModel entityTreeModelToClone) {
		super.setModelObject(entity, depth);

		ModelMdResolver metaDataResolver;
		if (entity == null)
			metaDataResolver = modelFactory.getGmSession().getModelAccessory().getMetaData().useCase(modelFactory.getUseCase()).lenient(true);
		else {
			metaDataResolver = getMetaData(entity).useCase(modelFactory.getUseCase());
			this.elementType = entity.entityType();
			if (condensedPropertyName == null) {
				condensedPropertyName = modelFactory.getCondensedProperty(entity, elementType);
				condensedPropertyAfterUpdate = condensedPropertyName;
			}
		}

		if (baseTypeForProperties != null) {
			if (entityTreeModelToClone != null && entity != null)
				cloneTreePropertyModels(entity, entityTreeModelToClone);
			else {
				Map<String, EntityCompoundViewing> entityCompoundViewings = GMEMetadataUtil
						.getEntityCompoundViewingsMap(metaDataResolver.entityType(elementType));
				prepareTreePropertyModels(entityCompoundViewings, metaDataResolver);
			}
		}
		
		boolean hasPropertyEntryModels = entity != null ? hasPropertyEntryModels(entity, metaDataResolver) : false;

		if (hasPropertyEntryModels && entity != null && depth >= ModelFactory.MAX_DEPTH) {
			notCompleted = true;
			return;
		}
		
		if (notCompleted && currentlyCondensedProperty != null)
			completeDueToCondensation = true;

		notCompleted = false;
		propertyEntryTreeModels.clear();
		this.clear();

		if (entity == null || !hasPropertyEntryModels)
			return;

		preparePropertyEntryModels(depth, currentlyCondensedProperty, entity, metaDataResolver/*, entityCompoundViewings*/);
		Collections.sort(propertyEntryTreeModels, getPriorityComparator(false));

		propertyEntryTreeModels.forEach(c -> add(c));
	}

	private void cloneProperties(ObjectAndType objectAndType, EntityTreeModel model) {
		prepareProperties((GenericEntity) objectAndType.getObject(), objectAndType.getDepth(), null, model);
	}

	private void cloneTreePropertyModels(GenericEntity parentEntity, EntityTreeModel entityTreeModel) {
		for (Map.Entry<String, TreePropertyModel> entry : entityTreeModel.treePropertyModels.entrySet()) {
			TreePropertyModel clonedPropertyModel = TreePropertyModel.createClone(parentEntity, entry.getValue());
			if (clonedPropertyModel != null) {
				treePropertyModels.put(entry.getKey(), clonedPropertyModel);
				normalizedTreePropertyModels.put(clonedPropertyModel.getNormalizedPropertyName(), clonedPropertyModel);
			}
		}

		for (Map.Entry<String, TreePropertyModel> entry : entityTreeModel.hiddenTreePropertyModels.entrySet()) {
			TreePropertyModel clonedPropertyModel = TreePropertyModel.createClone(parentEntity, entry.getValue());
			if (clonedPropertyModel != null)
				hiddenTreePropertyModels.put(clonedPropertyModel.getNormalizedPropertyName(), clonedPropertyModel);
		}
	}

	private void prepareTreePropertyModels(Map<String, EntityCompoundViewing> entityCompoundViewings, ModelMdResolver modelMdResolver) {
		treePropertyModels.clear();
		normalizedTreePropertyModels.clear();
		hiddenTreePropertyModels.clear();

		GenericEntity entity = (GenericEntity) modelObject;
		if (entity == null)
			return;

		String useCase = modelFactory.getUseCase();

		List<TreePropertyModel> treePropertyModelList = new ArrayList<>();
		baseTypeForProperties.getProperties().forEach(p -> prepareTreePropertyModel(entityCompoundViewings, entity, useCase, modelMdResolver, treePropertyModelList, p, null));
		
		if (modelFactory.getColumnData() != null) {
			modelFactory.getColumnData().getDisplayPaths().stream().filter(column -> column.getDeclaringTypeSignature() != null).forEach(column -> {
				EntityType<?> declaringType = GMF.getTypeReflection().getEntityType(column.getDeclaringTypeSignature());
				Property property = declaringType.getProperty(column.getPath());
				prepareTreePropertyModel(entityCompoundViewings, entity, useCase, modelMdResolver, treePropertyModelList, property, null);
			});
		}
		
		Collections.sort(treePropertyModelList, TreePropertyModel.getPriorityComparator(false));

		for (TreePropertyModel treePropertyModel : treePropertyModelList) {
			treePropertyModels.put(getPropertyKey(treePropertyModel.getNormalizedProperty()), treePropertyModel);
			normalizedTreePropertyModels.put(treePropertyModel.getNormalizedPropertyName(), treePropertyModel);
		}
	}

	private void prepareTreePropertyModel(Map<String, EntityCompoundViewing> entityCompoundViewings, GenericEntity entity, String useCase,
			ModelMdResolver modelMdResolver, List<TreePropertyModel> treePropertyModelList, Property property, Property embeddedProperty) {
		GenericModelType propertyType = property.getType();

		if (propertyType.isCollection())
			return;
		
		Property instanceProperty = entity.entityType().findProperty(property.getName());
		if (instanceProperty == null || !property.getType().equals(instanceProperty.getType()))
			return;
		
		property = instanceProperty;

		boolean isSimpleOrSimplified = true;

		Object propertyValue = null;
		if (propertyType.isBase() || propertyType.isEntity())
			propertyValue = property.get(entity);

		if (propertyType.isBase()) {
			BaseType baseType = (BaseType) propertyType;
			if (propertyValue != null)
				propertyType = baseType.getActualType(propertyValue);
			if (propertyType == null)
				propertyType = property.getType();
		}

		String propertyName = property.getName();
		EntityMdResolver entityMdResolver = modelMdResolver.entity(entity);
		PropertyMdResolver propertyMdResolver = entityMdResolver.useCase(useCase).property(propertyName);
		
		boolean isCompound = isCompound(entityCompoundViewings, propertyName);
		if (propertyType.isEntity() && !isCompound)
			isSimpleOrSimplified = AssemblyUtil.isInline(property, entity, null, modelMdResolver, modelFactory);
		else
			isSimpleOrSimplified = !AssemblyUtil.hasOutline(property, entity, null, modelMdResolver, modelFactory);
		
		if (propertyType.isEntity()) {
			GenericEntity embeddedEntity = property.get(entity);
			if (embeddedEntity != null) {
				Embedded embedded = propertyMdResolver.meta(Embedded.T).exclusive();
				if (embedded == null)
					embedded = modelMdResolver.entity(embeddedEntity).useCase(useCase).meta(Embedded.T).exclusive();
				
				if (embedded != null)
					handleEmbeddedProperty(entity, useCase, modelMdResolver, treePropertyModelList, property, embedded, embeddedEntity);
			}
		}

		if (!isSimpleOrSimplified)
			return;

		Double priority = GMEMetadataUtil.getPropertyPriority(propertyMdResolver);
		boolean isAbsent = GMEUtil.isPropertyAbsent(entity, property);
		boolean visible = propertyMdResolver.is(Visible.T);
		VirtualEnum virtualEnum = propertyMdResolver.meta(VirtualEnum.T).exclusive();

		TreePropertyModel treePropertyModel;
		if (!isCompound) {
			boolean isPassword = GMEMetadataUtil.isPropertyPassword(propertyMdResolver);
			boolean editable = GMEMetadataUtil.isPropertyEditable(propertyMdResolver, entity);
			String propertyDisplay = GMEMetadataUtil.getPropertyDisplay(propertyName, propertyMdResolver);

			treePropertyModel = new TreePropertyModel(property, entity, priority, !editable, isPassword, isAbsent, propertyDisplay,
					virtualEnum/* , baseTyped */);
		} else {
			treePropertyModel = new CompoundTreePropertyModel(entityCompoundViewings.get(propertyName), property, entity, priority, isAbsent,
					virtualEnum, modelMdResolver, useCase);
		}
		treePropertyModel.setEmbeddedProperty(embeddedProperty);

		if (visible)
			treePropertyModelList.add(treePropertyModel);
		else
			hiddenTreePropertyModels.put(treePropertyModel.getNormalizedPropertyName(), treePropertyModel);
	}

	private void handleEmbeddedProperty(GenericEntity entity, String useCase, ModelMdResolver modelMdResolver, List<TreePropertyModel> treePropertyModelList,
			Property property, Embedded embedded, GenericEntity embeddedParentEntity) {
		EntityType<?> propertyEntityType = embeddedParentEntity.entityType();
		List<String> embeddedProperties = new ArrayList<>();
		if (embedded.getIncludes().isEmpty())
			propertyEntityType.getProperties().forEach(subProperty -> embeddedProperties.add(subProperty.getName()));
		else
			embeddedProperties.addAll(embedded.getIncludes());
		
		embeddedProperties.removeAll(embedded.getExcludes());
		
		for (String embeddedPropertyName : embeddedProperties) {
			Property embeddedProperty = propertyEntityType.getProperty(embeddedPropertyName);
			GenericEntity parentEntity = property.get(entity);
			prepareTreePropertyModel(null, parentEntity, useCase, modelMdResolver, treePropertyModelList, embeddedProperty, property);
			
			GenericModelType embeddedPropertyType = embeddedProperty.getType();
			if (embeddedPropertyType.isEntity()) {
				GenericEntity embeddedEntity = embeddedProperty.get(parentEntity);
				if (embeddedEntity != null) {
					Embedded subEmbedded = modelMdResolver.entity(parentEntity).property(embeddedProperty).useCase(useCase).meta(Embedded.T).exclusive();
					if (subEmbedded == null)
						subEmbedded = modelMdResolver.entity(embeddedEntity).useCase(useCase).meta(Embedded.T).exclusive();
					
					if (subEmbedded != null) {
						handleEmbeddedProperty(parentEntity, useCase, modelMdResolver, treePropertyModelList, embeddedProperty, subEmbedded,
								embeddedEntity);
					}
				}
			}
			
		}
	}

	private boolean isCompound(Map<String, EntityCompoundViewing> entityCompoundViewings, String propertyName) {
		return entityCompoundViewings != null && entityCompoundViewings.containsKey(propertyName);
	}
	
	private boolean hasPropertyEntryModels(GenericEntity entity, ModelMdResolver metaDataResolver) {
		EntityMdResolver entityMdResolver = metaDataResolver.entity(entity);
		for (Property property : elementType.getProperties()) {
			String propertyName = property.getName();
			PropertyMdResolver propertyMdResolver = entityMdResolver.property(propertyName);
			boolean visible = propertyMdResolver.is(Visible.T);
			if (!visible)
				continue;
			
			if (displayAllPropertiesInMultiplex)
				return true;
			
			GenericModelType propertyType = property.getType();
			Object propertyValue = null;
			if (propertyType.isBase() || propertyType.isEntity())
				propertyValue = property.get(entity);
			
			boolean baseTyped = false;
			if (propertyType.isBase()) {
				baseTyped = true;
				BaseType baseType = (BaseType) propertyType;
				if (propertyValue != null)
					propertyType = baseType.getActualType(propertyValue);
				if (propertyType == null)
					propertyType = property.getType();
			}
			
			if (propertyType.isCollection())
				return true;
			
			boolean isSimpleOrSimplified = true;
			
			if (propertyType.isEntity() || baseTyped)
				isSimpleOrSimplified = AssemblyUtil.isInline(property, entity, elementType, metaDataResolver, modelFactory);
			else
				isSimpleOrSimplified = !AssemblyUtil.hasOutline(property, entity, elementType, metaDataResolver, modelFactory);
			
			if (!isSimpleOrSimplified)
				return true;
		}
		
		return false;
	}

	private void preparePropertyEntryModels(int depth, String currentlyCondensedProperty, GenericEntity entity, ModelMdResolver metaDataResolver/*,
			Map<String, EntityCompoundViewing> entityCompoundViewings*/) {
		EntityMdResolver entityMdResolver = metaDataResolver.entity(entity);
		for (Property property : elementType.getProperties()) {
			String propertyName = property.getName();
			if (condensedPropertyName != null && (!condensedPropertyName.equals(propertyName)))
				continue;
			
			PropertyMdResolver propertyMdResolver = entityMdResolver.property(propertyName);
			boolean visible = propertyMdResolver.is(Visible.T);
			if (!visible)
				continue;

			GenericModelType propertyType = property.getType();
			Object propertyValue = null;
			if (propertyType.isBase() || propertyType.isEntity())
				propertyValue = property.get(entity);

			boolean baseTyped = false;
			if (propertyType.isBase()) {
				baseTyped = true;
				BaseType baseType = (BaseType) propertyType;
				if (propertyValue != null)
					propertyType = baseType.getActualType(propertyValue);
				if (propertyType == null)
					propertyType = property.getType();
			}

			boolean editable = GMEMetadataUtil.isPropertyEditable(propertyMdResolver, entity);
			Double priority = GMEMetadataUtil.getPropertyPriority(propertyMdResolver);
			String propertyDisplay = GMEMetadataUtil.getPropertyDisplay(propertyName, propertyMdResolver);
			boolean isAbsent = GMEUtil.isPropertyAbsent(entity, property);

			if (!propertyType.isCollection()) {
				boolean isSimpleOrSimplified = true;
				
				Embedded embedded = null;
				if (propertyType.isEntity()) {
					embedded = propertyMdResolver.meta(Embedded.T).exclusive();
					if (embedded == null) {
						embedded = metaDataResolver.entityType((EntityType<?>) propertyType).useCase(modelFactory.getUseCase()).meta(Embedded.T)
								.exclusive();
					}
				}
				
				//boolean isInCompound = embedded != null || isCompound(entityCompoundViewings, propertyName);

				if (propertyType.isEntity() || baseTyped)
					isSimpleOrSimplified = AssemblyUtil.isInline(property, entity, elementType, metaDataResolver, modelFactory);
				else
					isSimpleOrSimplified = !AssemblyUtil.hasOutline(property, entity, elementType, metaDataResolver, modelFactory);

				if (isSimpleOrSimplified) {
					if (!displayAllPropertiesInMultiplex)
						continue;
				}/* else if (isInCompound && !visible)
					continue;*/
			}

			boolean mandatory = propertyMdResolver.is(Mandatory.T);

			int propertyDepth = depth;
			if (condensedPropertyName == null && !propertyName.equals(currentlyCondensedProperty))
				propertyDepth = depth + 1;

			PropertyEntry propertyEntry = new PropertyEntry(entity, elementType, propertyName, isAbsent,
					isMapAsList(entity, elementType, propertyName, metaDataResolver, modelFactory.getUseCase()),
					getMaxSize(entity, elementType, property, metaDataResolver, modelFactory.getUseCase()), property, propertyType, propertyDepth,
					baseTyped);
			ObjectAndType propertyObjectAndType = new ObjectAndType();
			propertyObjectAndType.setObject(propertyEntry);
			propertyObjectAndType.setType(propertyType);
			propertyObjectAndType.setDepth(propertyDepth);
			PropertyEntryTreeModel propertyEntryTreeModel = new PropertyEntryTreeModel(propertyObjectAndType, modelFactory);
			propertyEntryTreeModel.set(TEMP_DISPLAY_PROPERTY, propertyDisplay);
			propertyEntryTreeModel.setMandatory(mandatory);
			propertyEntryTreeModel.setPriority(priority != null ? priority : (Double.NEGATIVE_INFINITY));
			propertyEntryTreeModel.setEditable(editable);
			if (visible)
				propertyEntryTreeModels.add(propertyEntryTreeModel);

			if (condensedPropertyName != null)
				break;
		}
	}
	
	public String getCondensedPropertyAfterUpdate() {
		if (expandPending)
			return null;
		String result = condensedPropertyAfterUpdate;
		condensedPropertyAfterUpdate = null;
		return result;
	}
	
	public void restoreNotCompleteDueToCondensation() {
		condensedPropertyName = null;
		condensedPropertyAfterUpdate = null;
		if (completeDueToCondensation) {
			completeDueToCondensation = false;
			notCompleted = true;
			expandPending = true;
		}
	}
	
	public boolean isExpandPending() {
		return expandPending;
	}
	
	public void clearExpandPending() {
		expandPending = false;
	}

	/**
	 * Returns true if the given property for the given type is to be shown as list.
	 */
	protected static boolean isMapAsList(GenericEntity entity, EntityType<?> entityType, String propertyName, ModelMdResolver metaDataResolver, String useCase) {
		EntityMdResolver entityMdResolver = getEntityMdResolver(entity, entityType, metaDataResolver, useCase);
		return entityMdResolver.property(propertyName).is(ShowAsList.T);
	}
	
	protected static Integer getMaxSize(GenericEntity entity, EntityType<?> entityType, Property property, ModelMdResolver metaDataResolver, String useCase) {
		GenericModelType type = property.getType();
		boolean isSet = type.isCollection() && ((CollectionType) type).getCollectionKind().equals(CollectionKind.set);
		if (!isSet)
			return null;
		
		EntityMdResolver entityMdResolver = getEntityMdResolver(entity, entityType, metaDataResolver, useCase);
		
		return GMEMetadataUtil.getMaxLimit(entityMdResolver, property, AssemblyUtil.MAX_SET_ENTRIES);
	}
	
	private static EntityMdResolver getEntityMdResolver(GenericEntity entity, EntityType<?> entityType, ModelMdResolver metaDataResolver, String useCase) {
		EntityMdResolver entityMdResolver;
		if (entity == null)
			entityMdResolver = metaDataResolver.entityType(entityType).useCase(useCase);
		else
			entityMdResolver = getMetaData(entity).entity(entity).useCase(useCase);
		
		return entityMdResolver;
	}
	
	private String getPropertyKey(Property property) {
		return property.getName() + "." + property.getFirstDeclaringType().getTypeSignature();
	}

	private static Comparator<PropertyEntryTreeModel> getPriorityComparator(final boolean priorityReverse) {
		if (priorityComparator != null)
			return priorityComparator;
		
		priorityComparator = (o1, o2) -> {
			int priorityComparison;
			if (priorityReverse)
				priorityComparison = o1.getPriority().compareTo(o2.getPriority());
			else
				priorityComparison = o2.getPriority().compareTo(o1.getPriority());
			
			if (priorityComparison == 0) {
				String d1 = o1.get(TEMP_DISPLAY_PROPERTY);
				String d2 = o2.get(TEMP_DISPLAY_PROPERTY);
				return d1.compareToIgnoreCase(d2);
			}
			
			return priorityComparison;
		};

		return priorityComparator;
	}

}
