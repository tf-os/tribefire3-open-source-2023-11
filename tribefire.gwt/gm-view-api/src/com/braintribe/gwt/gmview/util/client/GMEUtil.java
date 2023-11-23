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
package com.braintribe.gwt.gmview.util.client;

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.uiaction.ActionFolderContent;
import com.braintribe.gwt.action.adapter.gxt.client.ButtonActionAdapter;
import com.braintribe.gwt.action.adapter.gxt.client.MenuItemActionAdapter;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.async.client.MultiLoader;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.TrackableChangesAction;
import com.braintribe.gwt.gmview.client.AlternativeGmSessionHandler;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmMapView;
import com.braintribe.gwt.gmview.client.GmSessionHandler;
import com.braintribe.gwt.gmview.client.InstantiatedEntityListener;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.ModelPathNavigationListener;
import com.braintribe.gwt.gmview.client.PropertyBean;
import com.braintribe.gwt.gmview.client.UseCaseHandler;
import com.braintribe.gwt.gmview.client.WorkWithEntityActionListener;
import com.braintribe.gwt.gmview.codec.client.PropertyRelatedCodec;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution.RequestExecutionData;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.model.acl.Acl;
import com.braintribe.model.acl.AclOperation;
import com.braintribe.model.acl.HasAcl;
import com.braintribe.model.dataediting.api.GenericSnapshot;
import com.braintribe.model.dataediting.api.OnEditFired;
import com.braintribe.model.extensiondeployment.RequestProcessing;
import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.extensiondeployment.meta.OnEdit;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.folder.FolderContent;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.CollectionManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.path.ListItemPathElement;
import com.braintribe.model.generic.path.MapKeyPathElement;
import com.braintribe.model.generic.path.MapValuePathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.path.SetItemPathElement;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.meta.data.display.DefaultSort;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.manipulation.basic.tools.ManipulationRemotifier;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.notification.api.NotificationFactory;
import com.braintribe.model.processing.query.api.stringifier.QuerySelection;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.query.selection.BasicQuerySelectionResolver;
import com.braintribe.model.processing.session.api.managed.PropertyQueryResultConvenience;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedCollection;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedList;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedSet;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;
import com.braintribe.model.security.acl.AclTcs;
import com.braintribe.model.security.acl.AclTools;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.form.IsField;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

/**
 * Class containing utility methods, used throughout the GME components.
 *
 */
public class GMEUtil {
	private static final Logger logger = new Logger(GMEUtil.class);
	private static final String PASSWORD_DISPLAY = "****";
	public static final String PROPERTY_NAME_CSS = "gmePropertyName"; 
	public static final String PROPERTY_VALUE_CSS = "gmePropertyValue"; 
	
	/**
	 * Checks if the edition was valid (if there was an actual change in the value).
	 */
	public static boolean isEditionValid(Object value, Object startValue, IsField<?> field) {
		if (field instanceof TrackableChangesAction)
			return ((TrackableChangesAction) field).hasChanges();
			
		if (value instanceof String || startValue instanceof String) {
			String stringValue = (String) value;
			String stringStartValue = (String) startValue;
			if (stringValue == null || stringValue.isEmpty()) {
				if (stringStartValue == null || stringStartValue.isEmpty())
					return false;
				
				//editor was returning null. Which should be treated as "".
				if (value == null) {
					value = "";
				}
			} else if (stringValue.equals(stringStartValue))
				return false;
		} else if ((value != null && value.equals(startValue)) || (value == startValue))
			return false;
		
		return true;
	}
	
	/**
	 * Checks if the given property, for the given entity, is absent.
	 */
	public static boolean isPropertyAbsent(GenericEntity entity, Property property) {
		if (entity != null && entity.isEnhanced()) {
			if (property.isAbsent(entity))
				return true;

			if (property.getType().isCollection()) {
				Object collection = property.get(entity);
				if (collection instanceof EnhancedCollection)
					return !((EnhancedCollection) collection).isLoaded();
			}
		}
		
		return false;
	}

	/**
	 * Resolves the reference.
	 */
	public static GenericEntity resolveReference(EntityReference entityReference, PersistenceGmSession gmSession) {
		if (entityReference == null || gmSession == null)	
			return null;
		
		try {
			return gmSession.queryCache().entity(entityReference).require();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Prepares a new PropertyQuery object
	 * @param pageSize - not null add a paging restriction.
	 * @param modelMdResolver - if prepareSort is true, then this should be not null, otherwise prepareSort is assumed false.
	 */
	public static PropertyQuery getPropertyQuery(PersistentEntityReference reference, String propertyName, Integer pageSize,
			TraversingCriterion traversingCriterion, boolean prepareSort, ModelMdResolver modelMdResolver, String useCase) {
		PropertyQueryBuilder propertyQueryBuilder = PropertyQueryBuilder.forProperty(reference, propertyName);
		if (traversingCriterion != null)
			propertyQueryBuilder = propertyQueryBuilder.tc(traversingCriterion);
		if (pageSize != null)
			propertyQueryBuilder = propertyQueryBuilder.paging(pageSize, 0);
		
		if (!prepareSort || modelMdResolver == null)
			return propertyQueryBuilder.done();
		
		EntityType<?> entityType = GMF.getTypeReflection().getEntityType(reference.getTypeSignature());
		Property property = entityType.getProperty(propertyName);
		GenericModelType propertyType = property.getType();
		if (!propertyType.isCollection())
			return propertyQueryBuilder.done();
			
		CollectionType collectionType = (CollectionType) propertyType;
		GenericModelType collectionElementType = collectionType.getCollectionElementType();
		if (!collectionElementType.isEntity())
			return propertyQueryBuilder.done();
		
		propertyQueryBuilder = prepareOrdering(propertyQueryBuilder,
				modelMdResolver.entityType((EntityType<?>) collectionElementType).useCase(useCase),
				modelMdResolver.entityType(entityType).property(propertyName).useCase(useCase));
		
		return propertyQueryBuilder.done();
	}
	
	private static PropertyQueryBuilder prepareOrdering(PropertyQueryBuilder currentBuilder, EntityMdResolver entityMdResolver, PropertyMdResolver propertyMdResolver) {
		DefaultSort defaultSort = propertyMdResolver.meta(DefaultSort.T).exclusive();
		if (defaultSort == null)
			defaultSort = entityMdResolver.meta(DefaultSort.T).exclusive();
		
		if (defaultSort == null)
			return currentBuilder.orderBy(GenericEntity.id);
		
		OrderingDirection orderingDirection;
		switch (defaultSort.getDirection()) {
		case descending:
			orderingDirection = OrderingDirection.descending;
			break;
		default:
			orderingDirection = OrderingDirection.ascending;
		}
		
		return currentBuilder.orderBy(defaultSort.getProperty().getName(), orderingDirection);
	}

	/**
	 * prepare loader for Absent Property at Entity
	 */
	public static Loader<Void> loadAbsentProperty(GenericEntity entity, EntityType<GenericEntity> entityType, Property property,
			PersistenceGmSession gmSession, String useCase, CodecRegistry<String> codecRegistry,
			Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion) {
		return new Loader<Void>() {
			@Override
			public void load(final AsyncCallback<Void> asyncCallback) {
				EntityReference entityReference = entity.reference();
				if (!(entityReference instanceof PersistentEntityReference)) {
					asyncCallback.onSuccess(null);
					return;
				}

				final PropertyQuery propertyQuery = GMEUtil.getPropertyQuery((PersistentEntityReference) entityReference, property.getName(),
					null, getSpecialTraversingCriterion(property.getType().getJavaType(), specialEntityTraversingCriterion), false,
					gmSession.getModelAccessory().getMetaData(), useCase);
										
				final ProfilingHandle ph = Profiling.start(getClass(), "Querying for absent property '" + property.getName() + "'", true);
				gmSession.query().property(propertyQuery).result(new com.braintribe.processing.async.api.AsyncCallback<PropertyQueryResultConvenience>() {
					@Override
					public void onSuccess(PropertyQueryResultConvenience propertyQueryResult) {												
						ph.stop();						
						ProfilingHandle ph1 = Profiling.start(getClass(), "Handling absenty property '" + property.getName() + "' query", false);
						GmSessionException exception = null;
						try {
							PropertyQueryResult result = propertyQueryResult.result();
							gmSession.suspendHistory();								
							Object value = result != null ? result.getPropertyValue() : null;
							value = GMEUtil.transformIfSet(value, property.getName(), entityType);
							
							if ((value != null) && (value instanceof EnhancedCollection)) {
								if (result != null)
									((EnhancedCollection) value).setIncomplete(result.getHasMore());
							}
							
							ProfilingHandle ph2 = Profiling.start(getClass(), "Setting the loaded abent property value into the entity", false);
							property.set(entity, GMEUtil.sortIfSet(value, propertyQuery,	gmSession, useCase, codecRegistry));
							ph2.stop();
						} catch (GmSessionException e) {
							exception = e;
						} finally {
							gmSession.resumeHistory();
							
							ph1.stop();
							if (exception == null)
								asyncCallback.onSuccess(null);
							else
								onFailure(exception);
						}
					}
					
					@Override
					public void onFailure(Throwable t) {
						ph.stop();
						asyncCallback.onFailure(t);
					}
				});
			}
		};
	}
	
	private static TraversingCriterion getSpecialTraversingCriterion(Class<?> clazz, Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion) {
		if (specialEntityTraversingCriterion != null) {
			return specialEntityTraversingCriterion.get(clazz);
		}
		return null;
	}		
	
	/**
	 * Encodes the string for password displaying.
	 */
	public static String preparePasswordString(String value) {
		return value == null || value.isEmpty() ? value : PASSWORD_DISPLAY;
	}
	
	/**
	 * Prepares a {@link EntityProperty} based on the given parameters.
	 */
	public static EntityProperty newEntityProperty(GenericEntity parentEntity, String propertyName) {
		EntityProperty entityProperty = EntityProperty.T.create();
		entityProperty.setPropertyName(propertyName);
		if (parentEntity != null)
			entityProperty.setReference(parentEntity.reference());
		
		return entityProperty;
	}
	
	/**
	 * Prepares the {@link EntityProperty} based on the given instanceProperty and its root modelPathElement
	 */
	public static EntityProperty prepareQueryEntityProperty(GenericEntity parentEntity, Property property) {
		return newEntityProperty(parentEntity, property.getName());
	}
	
	/**
	 * Inserts to a collection based on the given parameters.
	 */
	public static void insertToListOrSet(PropertyRelatedModelPathElement collectionElement, List<GMTypeInstanceBean> valueBeans, int index) {
		boolean isList;
		if (collectionElement.getType() instanceof CollectionType)
			isList = ((CollectionType) collectionElement.getType()).getCollectionKind().equals(CollectionKind.list);
		else if (collectionElement.getEntity() instanceof GmProperty) { //special handling for the GmProperty initializer
			GmProperty gmProperty = ((GmProperty) collectionElement.getEntity());
			GmType gmType = gmProperty.getType();
			if (gmType != null)
				isList = gmType.typeKind().equals(GmTypeKind.LIST);
			else
				throw new RuntimeException("Invalid element.");
		} else
			throw new RuntimeException("Invalid element.");
		
		Collection<?> collection = (Collection<?>) collectionElement.getValue();
		
		List<KeyAndValueGMTypeInstanceBean> keyAndValueBeans;
		
		if (valueBeans.size() == 1) {
			KeyAndValueGMTypeInstanceBean bean;
			GMTypeInstanceBean valueBean = valueBeans.get(0);
			if (!isList)
				bean = new KeyAndValueGMTypeInstanceBean(valueBean);
			else {
				GMTypeInstanceBean key;
				if (index != -1)
					key = new GMTypeInstanceBean(index);
				else
					key = new GMTypeInstanceBean(collection == null ? 0 : collection.size());
				
				bean = new KeyAndValueGMTypeInstanceBean(key, valueBean);
			}
			
			keyAndValueBeans = Arrays.asList(bean);
		} else {
			keyAndValueBeans = new ArrayList<>();
			int listIndex = index == -1 ? (collection == null ? 0 : collection.size()) : index;
			for (GMTypeInstanceBean newGMTypeInstanceBean : valueBeans) {
				if (isList) {
					GMTypeInstanceBean key = new GMTypeInstanceBean(listIndex++);
					keyAndValueBeans.add(new KeyAndValueGMTypeInstanceBean(key, newGMTypeInstanceBean));
				} else
					keyAndValueBeans.add(new KeyAndValueGMTypeInstanceBean(newGMTypeInstanceBean));
			}
		}
		
		insertOrRemoveToCollection(collectionElement, keyAndValueBeans, true);
	}
	
	/**
	 * This method prepares the KeyAndValueGMTypeInstanceBean object populated with both key and value information, except if MapKeyPathElement is selected. In
	 * such case, only the key is set.
	 */
	public static KeyAndValueGMTypeInstanceBean prepareKeyAndValueGMTypeInstanceBean(PropertyRelatedModelPathElement collectionElement, ModelPathElement collectionItemElement) {
		GMTypeInstanceBean key = null;
		GMTypeInstanceBean value = null;
		Object collectionItemElementValue = collectionItemElement.getValue();
		GenericModelType collectionItemElementType = collectionItemElement.getType();
		if (collectionItemElement instanceof ListItemPathElement || collectionItemElement instanceof SetItemPathElement) {
			value = new GMTypeInstanceBean(collectionItemElementType, collectionItemElementValue);
			key = value;
			if (collectionItemElement instanceof ListItemPathElement)
				key = new GMTypeInstanceBean(((ListItemPathElement) collectionItemElement).getIndex());
		} else { //map
			if (collectionItemElement instanceof MapKeyPathElement)
				key = new GMTypeInstanceBean(collectionItemElementType, collectionItemElementValue);
			else if (collectionItemElement instanceof MapValuePathElement) {
				Object mapKey = ((MapValuePathElement) collectionItemElement).getKey();
				GenericModelType keyType;
				if (mapKey instanceof GenericEntity)
					keyType = ((GenericEntity) mapKey).entityType();
				else
					keyType = ((CollectionType) collectionElement.getType()).getParameterization()[0];
				
				key = new GMTypeInstanceBean(keyType, mapKey); 
				value = new GMTypeInstanceBean(collectionItemElementType, collectionItemElementValue);
			}
		}
		
		return new KeyAndValueGMTypeInstanceBean(key, value);
	}
	
	/**
	 * Inserts or removes the given keys and values to the given collection.
	 */
	public static void insertOrRemoveToCollection(PropertyRelatedModelPathElement collectionElement,
			List<KeyAndValueGMTypeInstanceBean> keysAndValues, boolean isInsert) {
		Object collection = collectionElement.getValue();
		if (collection == null && collectionElement.getEntity() instanceof GmProperty)
			collection = getCollectionInstanceFromGmProperty(collectionElement);
		
		if (collection instanceof List) {
			if (!isInsert) {
				List<Integer> keysToRemove = new ArrayList<>();
				for (KeyAndValueGMTypeInstanceBean keyAndValue : keysAndValues)
					keysToRemove.add((Integer) keyAndValue.getKey().getInstance());
				Collections.sort(keysToRemove);
				Collections.reverse(keysToRemove);
				for (Integer index : keysToRemove)
					((List<?>) collection).remove((int) index);
			} else {
				for (KeyAndValueGMTypeInstanceBean keyAndValue : keysAndValues)
					((List<Object>) collection).add((Integer) keyAndValue.getKey().getInstance(), keyAndValue.getValue().getInstance());
			}
		} else if (collection instanceof Set) {
			List<Object> keys = new ArrayList<>();
			for (KeyAndValueGMTypeInstanceBean keyAndValue : keysAndValues)
				keys.add(keyAndValue.getKey().getInstance());
			
			if (isInsert)
				((Set<Object>) collection).addAll(keys);
			else
				((Set<Object>) collection).removeAll(keys);
		} else if (collection instanceof Map) {
			if (isInsert) {
				Map<Object, Object> auxMap = new LinkedHashMap<>();
				for (KeyAndValueGMTypeInstanceBean keyAndValue : keysAndValues)
					auxMap.put(keyAndValue.getKey().getInstance(), keyAndValue.getValue().getInstance());
				
				((Map<Object, Object>) collection).putAll(auxMap);
			} else {
				for (KeyAndValueGMTypeInstanceBean keyAndValue : keysAndValues)
					((Map<?,?>) collection).remove(keyAndValue.getKey().getInstance());
			}
		}
	}

	private static Object getCollectionInstanceFromGmProperty(PropertyRelatedModelPathElement collectionElement) {
		GmProperty gmProperty = ((GmProperty) collectionElement.getEntity());
		
		GmType collectionType = gmProperty.getType();
		GmType collectionElementType = ((GmCollectionType) collectionType).collectionElementType();
		GenericModelType elementType;
		if (collectionElementType.isGmEnum())
			elementType = EnumReference.T;
		else {
			elementType = GMF.getTypeReflection().findType(collectionElementType.getTypeSignature());
			if (elementType == null)
				elementType = BaseType.INSTANCE;
		}
		
		Object collection;
		boolean isList = collectionType.typeKind().equals(GmTypeKind.LIST);
		if (isList) {
			ListType listType = GMF.getTypeReflection().getListType(elementType);
			collection = new EnhancedList<>(listType);
		} else {
			SetType setType = GMF.getTypeReflection().getSetType(elementType);
			collection = new EnhancedSet<>(setType);
		}
		
		changeEntityPropertyValue(gmProperty, collectionElement.getProperty(), collection);
		
		return collection;
	}
	
	/**
	 * Sets the given new value to the given property.
	 */
	public static void changeEntityPropertyValue(GenericEntity parentEntity, Property property, Object newPropertyValue) {
		property.set(parentEntity, newPropertyValue);
	}
	
	/**
	 * Replace the newValue in the collectionElement, for the given collectionEntry.
	 */
	public static void replaceInCollection(PropertyRelatedModelPathElement collectionElement, PropertyRelatedModelPathElement collectionEntryElement, Object newValue) {
		if (collectionEntryElement instanceof ListItemPathElement) {
			List<Object> list = (List<Object>) collectionElement.getValue();
			list.set(((ListItemPathElement) collectionEntryElement).getIndex(), newValue);
		} else if (collectionEntryElement instanceof SetItemPathElement) {
			Set<Object> set = (Set<Object>) collectionElement.getValue();
			set.remove(collectionEntryElement.getValue());
			set.add(newValue);
		} else if (collectionEntryElement instanceof MapKeyPathElement) {
			Map<Object, Object> map = (Map<Object, Object>) collectionElement.getValue();
			Object value = map.get(collectionEntryElement.getValue());
			map.remove(collectionEntryElement.getValue());
			map.put(newValue, value);
		} else if (collectionEntryElement instanceof MapValuePathElement) {
			Map<Object, Object> map = (Map<Object, Object>) collectionElement.getValue();
			map.remove(((MapValuePathElement) collectionEntryElement).getKey());
			map.put(((MapValuePathElement) collectionEntryElement).getKey(), newValue);
		}
	}
	
	/**
	 * Sets the content to the correct instance of the {@link GmContentView} ({@link GmListView}, {@link GmMapView} or {@link GmEntityView}).
	 */
	public static void setContent(ModelPath modelPath, GmContentView contentView) {
		if (modelPath == null) {
			contentView.setContent(null);
			return;
		}
		
		GenericModelType type = modelPath.last().getType();
		CollectionKind collectionKind = type.isCollection() ? ((CollectionType) type).getCollectionKind() : null;
		if (collectionKind == null)
			contentView.setContent(modelPath);
		else {
			switch (collectionKind) {
				case list:
				case set:
					((GmListView) contentView).setContent(modelPath);
					break;
				case map:
					((GmMapView) contentView).setContent(modelPath);
					break;
			}
		}
	}
	
	public static Object transformIfSet(Object value, String propertyName, EntityType<?> entityType) {
		if (value == null || value instanceof Set) //nothing to do
			return value;
		
		Object changedValue = value;
		Property property = entityType.getProperty(propertyName);
		GenericModelType propertyType = property.getType();
		if (propertyType.isCollection() && ((CollectionType) propertyType).getCollectionKind().equals(CollectionKind.set) && value instanceof List) {
			List<?> list = (List<?>) value;
			
			EnhancedSet<Object> set = new EnhancedSet<>((SetType) propertyType, new LinkedHashSet<>());
			set.addAll(list);
			changedValue = set;
		}
		
		return changedValue;
	}
	
	/**
	 * Sorts the given value if it is a set, and if the given {@link PropertyQuery} has no ordering.
	 */
	public static Object sortIfSet(Object value, PropertyQuery propertyQuery, final PersistenceGmSession gmSession, final String useCase,
			CodecRegistry<String> codecRegistry) {
		if (value instanceof Set && !((Set<Object>) value).isEmpty() && propertyQuery.getOrdering() == null) {
			Set<Object> set = (Set<Object>) value;
			List<Object> list = new ArrayList<>();
			for (Object object : set)
				list.add(object);
			
			Collections.sort(list, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					String o1String = o1 == null ? "" : prepareDisplayValue(gmSession, useCase, o1, GMF.getTypeReflection().getType(o1),
							codecRegistry, null);
					String o2String = o2 == null ? "" : prepareDisplayValue(gmSession, useCase, o2, GMF.getTypeReflection().getType(o2),
							codecRegistry, null);
					return o1String.compareTo(o2String);
				}
			});
			
			LinkedHashSet<Object> sortedSet = new LinkedHashSet<>();
			sortedSet.addAll(list);
			return sortedSet;
		}
		
		return value;
	}
	
	public static Codec<Object, String> getRendererCodec(CodecRegistry<String> codecRegistry,
			GenericModelType genericModelType, PersistenceGmSession gmSession, String useCase, PropertyBean propertyBean) {
		if (codecRegistry == null)
			return null;
		
		Codec<Object, String> rendererCodec = codecRegistry.getCodec(genericModelType.getJavaType());
		if (rendererCodec instanceof GmSessionHandler)
			((GmSessionHandler) rendererCodec).configureGmSession(gmSession);
		if (rendererCodec instanceof UseCaseHandler)
			((UseCaseHandler) rendererCodec).configureUseCase(useCase);
		if (rendererCodec instanceof PropertyRelatedCodec) {
			PropertyRelatedCodec propertyRelatedCodec = (PropertyRelatedCodec) rendererCodec;
			propertyRelatedCodec.configureModelMdResolver(gmSession.getModelAccessory().getMetaData());
			propertyRelatedCodec.configureUseCase(useCase);
			propertyRelatedCodec.configurePropertyBean(propertyBean);
		}
		
		return rendererCodec;
	}
	
	/**
	 * Prepares a new {@link GMTypeInstanceBean} for the given type and session.
	 */
	public static GMTypeInstanceBean getTypeInstanceBean(GenericModelType type, PersistenceGmSession gmSession) {
		Object instance;
		if (type.isEntity())
			instance = gmSession.create((EntityType<?>) type);
		else if (type.isCollection())
			instance = ((CollectionType) type).createPlain();
		else
			instance = null;
		
		return new GMTypeInstanceBean(type, instance);
	}
	
	/**
	 * Returns the {@link WorkWithEntityActionListener} related to the given Widget, or null if none.
	 */
	public static WorkWithEntityActionListener getWorkWithEntityActionListener(Object view) {
		if (view instanceof WorkWithEntityActionListener)
			return (WorkWithEntityActionListener) view;
		
		if (view instanceof Widget)
			return getWorkWithEntityActionListener(((Widget) view).getParent());
		
		return null;
	}
	
	/**
	 * Returns the {@link ModelPathNavigationListener} related to the given Widget, or null if none.
	 */
	public static ModelPathNavigationListener getModelPathNavigationListener(Widget widget) {
		if (widget instanceof ModelPathNavigationListener)
			return (ModelPathNavigationListener) widget;
		
		if (widget != null && widget.getParent() != null)
			return getModelPathNavigationListener(widget.getParent());
		
		return null;
	}
	
	/**
	 * Returns the {@link InstantiatedEntityListener} related to the given Widget, or null if none.
	 */
	public static InstantiatedEntityListener getInstantiatedEntityListener(Object view) {
		if (view instanceof InstantiatedEntityListener)
			return (InstantiatedEntityListener) view;
		
		if (view instanceof Widget)
			return getInstantiatedEntityListener(((Widget) view).getParent());
		
		return null;
	}
	
	/**
	 * Returns the parent object, if any, of the given manipulation.
	 */
	public static Object getParentObject(Manipulation manipulation) {
		Object parentObject = null;
		
		if (manipulation instanceof CollectionManipulation) {
			parentObject = getOwnerObject((CollectionManipulation) manipulation);
		} else if (manipulation instanceof PropertyManipulation) {
			PropertyManipulation propertyManipulation = (PropertyManipulation) manipulation;
			Owner owner = propertyManipulation.getOwner();
			parentObject = ((LocalEntityProperty) owner).getEntity();
		}
		
		return parentObject;
	}
	
	/**
	 * Returns the instantiable sub types of the given entityType which are also defined in the MetaModel.
	 */
	public static Set<EntityType<?>> getInstantiableSubTypesDefinedInMetaModel(EntityType<?> entityType, PersistenceGmSession gmSession) {
		return gmSession.getModelAccessory().getOracle().getEntityTypeOracle(entityType).getSubTypes().onlyInstantiable().transitive().includeSelf().asTypes();
	}
	
	/**
	 * Prepares {@link TextButton}s based on the given {@link ModelAction}s.
	 */
	public static List<Pair<String, TextButton>> prepareExternalActionButtons(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		if (externalActions == null)
			return null;
		
		List<Pair<String, TextButton>> buttons = new ArrayList<>();
		for (Pair<ActionTypeAndName, ModelAction> entry : externalActions) {
			ModelAction action = entry.getSecond();
			if (ModelAction.actionBelongsToPosition(action, ModelActionPosition.ActionBar)) {
				TextButton button = new TextButton();
				button.setWidth(75);
				button.setIconAlign(IconAlign.TOP);
				button.setScale(ButtonScale.LARGE);
				if (action.getHoverIcon() != null)
					button.setIcon(action.getHoverIcon());
				ButtonActionAdapter.linkActionToButton(true, action, button);
				buttons.add(new Pair<>(entry.getFirst().getActionName(), button));
			}
		}
		
		return buttons;
	}
	
	/**
	 * Prepares {@link MenuItem}s based on the given {@link ModelAction}s.
	 */
	public static List<Pair<String, MenuItem>> prepareExternalMenuItems(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		if (externalActions == null)
			return null;
		
		List<Pair<String, MenuItem>> menuItems = new ArrayList<>();
		for (Pair<ActionTypeAndName, ModelAction> entry : externalActions) {
			ModelAction action = entry.getSecond();
			if (ModelAction.actionBelongsToPosition(action, ModelActionPosition.ContextMenu)) {
				MenuItem item = new MenuItem();
				menuItems.add(new Pair<>(entry.getFirst().getActionName(), item));
				MenuItemActionAdapter.linkActionToMenuItem(action, item);
			}
		}
		
		return menuItems;
	}
	
	/**
	 * Returns the collection type name for the given collection type.
	 */
	public static String getCollectionTypeName(GmCollectionType collectionType) {
		switch (collectionType.typeKind()) {
			case LIST:
				return CollectionKind.list.toString();
			case SET:
				return CollectionKind.set.toString();
			case MAP:
				return CollectionKind.map.toString();
			default:
				return null;
		}
	}
	
	/**
	 * Gets the short name for the given GmType.
	 */
	public static String getShortName(GmType type) {
		return type.getTypeSignature().substring(type.getTypeSignature().lastIndexOf(".") + 1);
	}
	
	/**
	 * Checks if the given cssClasses definitions contains the given classContained. It doesn't use the contains method, because that will fail in case you are
	 * looking for abcd and have another class defined as abcde, for example.
	 */
	public static boolean containsCssDefinition(String cssClasses, String classContained) {
		if (cssClasses != null) {
			for (String cssClass : cssClasses.split(" ")) {
				if (cssClass.equals(classContained))
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if the given cssClasses definitions contains one ore more of the given classContained. It doesn't use the contains method, because that will fail
	 * in case you are looking for abcd and have another class defined as abcde, for example.
	 */
	public static boolean containsCssDefinitions(String cssClasses, List<String> classesContained) {
		if (classesContained != null) {
			for (String classContained : classesContained)
				if (containsCssDefinition(cssClasses, classContained))
					return true;
		}
		
		return false;
	}
	
	/**
	 * Helper method for preparing a {@link TypeCondition}.
	 */
	public static TypeCondition prepareTypeCondition(GenericModelType modelType) {
		switch (modelType.getTypeCode()) {
			case entityType:
				return TypeConditions.isAssignableTo(modelType);
			case enumType:
				return TypeConditions.isType(modelType);
			case listType:
			case setType:
				LinearCollectionType collectionType = (LinearCollectionType) modelType;
				return TypeConditions.hasCollectionElement(prepareTypeCondition(collectionType.getCollectionElementType()));
			case mapType:
				MapType mapType = (MapType) modelType;
				
				return TypeConditions.or(TypeConditions.hasMapKey(prepareTypeCondition(mapType.getKeyType())),
						TypeConditions.hasMapValue(prepareTypeCondition(mapType.getValueType())));
			case booleanType:
				return TypeConditions.isKind(TypeKind.booleanType);
			case dateType:
				return TypeConditions.isKind(TypeKind.dateType);
			case decimalType:
				return TypeConditions.isKind(TypeKind.decimalType);
			case doubleType:
				return TypeConditions.isKind(TypeKind.doubleType);
			case floatType:
				return TypeConditions.isKind(TypeKind.floatType);
			case integerType:
				return TypeConditions.isKind(TypeKind.integerType);
			case longType:
				return TypeConditions.isKind(TypeKind.longType);
			case stringType:
				return TypeConditions.isKind(TypeKind.stringType);
			default:
				return null;
		}
	}
	
	/**
	 * Helper method for preparing a {@link TypeCondition}.
	 */
	public static TypeCondition prepareTypeCondition(GmType modelType) {
		return prepareTypeCondition(modelType, false);
	}
	
	/**
	 * Helper method for preparing a {@link TypeCondition}.
	 * @param excludeTypeIfAbstract true for excluding the type, when the entity is abstract.
	 */
	public static TypeCondition prepareTypeCondition(GmType modelType, boolean excludeTypeIfAbstract) {
		String typeSignature = modelType.getTypeSignature();
		switch (modelType.typeKind()) {
		case ENTITY:
			GmEntityType entityType = (GmEntityType) modelType;
			
			if (!entityType.getIsAbstract() || !excludeTypeIfAbstract)
				return TypeConditions.isAssignableTo(typeSignature);
			
			// @formatter:off
			return TypeConditions.and(
						TypeConditions.isAssignableTo(typeSignature),
						TypeConditions.not(
							TypeConditions.isType(typeSignature)
						)
					);
			// @formatter:on
		case ENUM:
			return TypeConditions.isType(typeSignature);
		case BOOLEAN:
			return TypeConditions.isKind(TypeKind.booleanType);
		case DATE:
			return TypeConditions.isKind(TypeKind.dateType);
		case DECIMAL:
			return TypeConditions.isKind(TypeKind.decimalType);
		case DOUBLE:
			return TypeConditions.isKind(TypeKind.doubleType);
		case FLOAT:
			return TypeConditions.isKind(TypeKind.floatType);
		case LONG:
			return TypeConditions.isKind(TypeKind.longType);
		case STRING:
			return TypeConditions.isKind(TypeKind.stringType);
		case INTEGER:
			return TypeConditions.isKind(TypeKind.integerType);
		case LIST:
		case SET:
			GmLinearCollectionType collectionType = (GmLinearCollectionType) modelType;
			return TypeConditions.hasCollectionElement(prepareTypeCondition(collectionType.getElementType()));
		case MAP:
			GmMapType mapType = (GmMapType) modelType;
			
			return TypeConditions.or(TypeConditions.hasMapKey(prepareTypeCondition(mapType.getKeyType())),
					TypeConditions.hasMapValue(prepareTypeCondition(mapType.getValueType())));
		default:
			return null;
			
		}
	}
	
	/**
	 * Returns an entity type signature that will be in the result of the given {@link SelectQuery}, in case only one
	 * column is supposed to be returned.
	 */
	public static String getSingleEntityTypeSignatureFromSelectQuery(SelectQuery selectQuery) {
		BasicQuerySelectionResolver resolver = BasicQuerySelectionResolver.create()
				.aliasMode()
				.simple()
				.shorteningMode()
				.simplified();
		
		return getSingleEntityTypeSignatureFromSelectQuery(resolver.resolve(selectQuery));
	}
	
	/**
	 * Returns an entity type signature that will be in the result of the given selections, which came from a
	 * {@link SelectQuery}, in case only one column is supposed to be returned.
	 */
	public static String getSingleEntityTypeSignatureFromSelectQuery(List<QuerySelection> selections) {
		if (selections.size() == 1) {
			QuerySelection selection = selections.get(0);
			Object operand = selection.getOperand();
			if (operand instanceof PropertyOperand) {
				PropertyOperand propertyOperand = (PropertyOperand) operand;
				Source source = propertyOperand.getSource();
				if (source instanceof From)
					return ((From) source).getEntityTypeSignature();
			}
		}
		
		return null;
	}
	
	/**
	 * Returns an {@link EntityType} if the given selection list should contain a unique entityType as result, but only if the {@link PropertyOperand} is an entity.
	 */
	public static EntityType<?> getSingleEntityTypeFromSelections(List<QuerySelection> selections, GenericModelTypeReflection typeReflection) {
		if (selections.size() != 1)
			return null;
		
		QuerySelection selection = selections.get(0);
		Object operand = selection.getOperand();
		if (!(operand instanceof PropertyOperand))
			return null;
		
		PropertyOperand propertyOperand = (PropertyOperand) operand;
		Source source = propertyOperand.getSource();

		EntityType<?> entityType = null;
		if (source instanceof From)
			entityType = typeReflection.getEntityType(((From) source).getEntityTypeSignature());
		else if (source instanceof Join)
			entityType = getSingleEntityTypeFromJoin((Join) source, typeReflection);
		
		if (entityType == null)
			return null;
		
		if (propertyOperand.getPropertyName() == null)
			return entityType;
		
		Property property = entityType.findProperty(propertyOperand.getPropertyName());
		if (property == null || !property.getType().isEntity())
			return null;
		
		return (EntityType<?>) property.getType();
	}
	
	private static EntityType<?> getSingleEntityTypeFromJoin(Join join, GenericModelTypeReflection typeReflection) {
		if (join.getSource() instanceof From) {
			From joinSource = (From) join.getSource();
			EntityType<?> sourceEntityType = typeReflection.getEntityType(joinSource.getEntityTypeSignature());
			if (sourceEntityType == null)
				return null;
			
			return getEntityTypeFromJoin(sourceEntityType, join);
		}
		
		if (join.getSource() instanceof Join) {
			EntityType<?> entityType = getSingleEntityTypeFromJoin((Join) join.getSource(), typeReflection);
			if (entityType == null)
				return null;
			
			return getEntityTypeFromJoin(entityType, join);
		}
		
		return null;
	}
	
	private static EntityType<?> getEntityTypeFromJoin(EntityType<?> entityType, Join join) {
		Property property = entityType.findProperty(join.getProperty());
		if (property == null)
			return null;
		
		GenericModelType propertyType = property.getType();
		if (propertyType.isEntity())
			return (EntityType<?>) propertyType;
		else if (propertyType.isCollection()) {
			CollectionType collectionType = (CollectionType) propertyType;
			GenericModelType collectionElementType = collectionType.getCollectionElementType();
			if (collectionElementType.isEntity())
				return (EntityType<?>) collectionElementType;
		}
		
		return null;
	}
	
	/**
	 * Checks whether the given operation is granted for the given entity.
	 * Notice that if the session attached to it is not {@link PersistenceGmSession}, then it will return true by default.
	 */
	public static boolean isOperationGranted(GenericEntity entity, AclOperation operation) {
		if (entity == null || !(entity instanceof HasAcl || entity instanceof Acl))
			return true;

		GmSession session = entity.session();
		if (!(session instanceof PersistenceGmSession))
			return true;
		
		PersistenceGmSession persistenceGmSession = ((PersistenceGmSession) session);
		SessionAuthorization sa = persistenceGmSession.getSessionAuthorization();
		if (sa == null)
			return true;
		
		boolean isAcl = entity instanceof Acl;
		
		boolean isAdministrable = isAcl ? AclTools.isAclAdministrable(persistenceGmSession) : AclTools.isHasAclAdministrable(persistenceGmSession);
		if (isAdministrable)
			return true;
		
		if (isAcl)
			return ((Acl) entity).isOperationGranted(operation, sa.getUserRoles());
		else
			return ((HasAcl) entity).isOperationGranted(operation, sa.getUserName(), sa.getUserRoles());
	}
	
	/**
	 * Expands the given TC with some more required info, such as {@link HasAcl}.
	 */
	public static TraversingCriterion expandTc(TraversingCriterion currentTc) {
		return AclTcs.addAclEagerLoadingTo(currentTc);
	}
	
	/**
	 * HTML-Escapes a string.
	 *
	 * @param s the string to be escaped
	 * @return the input string, with all occurrences of HTML meta-characters
	 *         replaced with their corresponding HTML Entity References
	 */
	public static String htmlEscape(String s) {
	   return SafeHtmlUtils.htmlEscape(s);
	}
	
	/**
	 * HTML-Re-Escapes a string.
	 *
	 * @param s the string to be ReEscaped
	 * @return the input string, with all occurrences of HTML Entity References
	 *         replaced with their corresponding characters
	 */
	public static String htmlReEscape(String s) {
	      if (s.indexOf("&amp;") != -1)
	        s = s.replaceAll("&amp;", "&");
	      if (s.indexOf("&lt;") != -1)
	        s = s.replaceAll("&lt;", "<");
	      if (s.indexOf("&gt;") != -1)
	        s = s.replaceAll("&gt;", ">");
	      if (s.indexOf("&quot;") != -1)
	        s = s.replaceAll("&quot;", "\"");
	      if (s.indexOf("&#39;") != -1)
	        s = s.replaceAll("&#39;", "'");
	      
	      return s;
	}
	
	/**
	 * Returns true if the given action denotationType name is available within the given {@link Folder} and its sub folders.
	 * False, otherwise.
	 * If the denotationType is not found, then the same check is done by name.
	 * @param actionsRootFolder - if this is null, then true is returned.
	 */
	public static boolean isActionAvailable(ActionTypeAndName actionTypeAndName, Folder actionsRootFolder) {
		if (actionsRootFolder != null) {
			List<Folder> subFolders = actionsRootFolder.getSubFolders();
			if (subFolders != null)
				return isActionAvailable(actionTypeAndName, subFolders);
		}
		
		return true;
	}
	
	/**
	 * Returns the session based on the view. If the given type is unknown, then it returns the alternative session.
	 * If no alternative session is configured, then the "normal" one is returned anyway.
	 */
	public static PersistenceGmSession getSessionOrAlternativeSessionFromViewBasedOnType(GmContentView view, GenericModelType gmType) {
		PersistenceGmSession session = view.getGmSession();
		if (session.getModelAccessory().getOracle().findGmType(gmType) == null && view instanceof AlternativeGmSessionHandler) {
			PersistenceGmSession alternativeSession = ((AlternativeGmSessionHandler) view).getAlternativeGmSession();
			if (alternativeSession != null)
				session = alternativeSession;
		}
		
		return session;
	}
	
	/**
	 * Triggers the OnEditFired request.
	 */
	public static Future<Void> fireOnEditRequest(GenericEntity editedEntity, Manipulation triggerManipulation, PropertyMdResolver propertyMdResolver,
			PersistenceGmSession dataSession, TransientPersistenceGmSession transientSession, ModelPathNavigationListener navigationListener,
			Supplier<? extends TransientPersistenceGmSession> transientSessionProvider,
			Supplier<? extends NotificationFactory> notificationFactorySupplier) {
		List<OnEdit> onEditList = propertyMdResolver.meta(OnEdit.T).list();
		
		if (onEditList.isEmpty())
			return null;
		
		Future<Void> future = new Future<>();
		MultiLoader multiLoader = new MultiLoader();
		
		GenericEntity cloneEntity = editedEntity;
		if (!(editedEntity instanceof GenericSnapshot))
			cloneEntity = makeShallowScalarCopy(editedEntity);
		
		int i = 0;
		for (OnEdit onEdit : onEditList) {
			RequestProcessing requestProcessing = onEdit.getRequestProcessing();
			OnEditFired request = OnEditFired.T.create();
			request.setDomainId(getDomainId(requestProcessing, dataSession));
			request.setServiceId(getServiceId(requestProcessing));
			request.setSnapshot(cloneEntity);
			request.setTriggerManipulation(ManipulationRemotifier.remotify(triggerManipulation));
			
			RequestExecutionData red = new RequestExecutionData(request, dataSession, transientSession, navigationListener, transientSessionProvider,
					notificationFactorySupplier);
			multiLoader.add(Integer.toString(i++), DdsaRequestExecution.executeRequest(red));
		}
		
		multiLoader.load(AsyncCallbacks.of(result -> future.onSuccess(null), future::onFailure));
		
		return future;
	}
	
	/**
	 * Returns a copy of the given entities, with only the scalar properties being copied.
	 * The identifier property is always copied.
	 */
	public static <T extends GenericEntity> T makeShallowScalarCopy(T entity) {
		EntityType<T> et = entity.entityType();

		T result = et.create();

		for (Property p: et.getProperties()) {
			GenericModelType type = p.getType();
			if (!type.isScalar() && !p.isIdentifier())
				continue;
			Object value = p.get(entity);
			p.set(result, value);
		}

		return result;
	}
	
	/**
	 * Returns the domainId for the given {@link RequestProcessing}.
	 * If the request is null, or the request' service domain is null, then the session's accessId is returned.
	 * Otherwise, the externalId from the request' service domain is returned. 
	 */
	public static String getDomainId(RequestProcessing requestProcessing, PersistenceGmSession session) {
		if (requestProcessing == null)
			return session.getAccessId();
		
		ServiceDomain serviceDomain = requestProcessing.getServiceDomain();
		if (serviceDomain != null)
			return serviceDomain.getExternalId();
		else
			return session.getAccessId();
	}
	
	/**
	 * Returns the serviceId for the given {@link RequestProcessing}.
	 * If the request is null or the request' service processor is null, then null is returned.
	 * Otherwise, the externalId from the request' service processor is returned.
	 */
	public static String getServiceId(RequestProcessing requestProcessing) {
		if (requestProcessing == null)
			return null;
		
		ServiceProcessor serviceProcess = requestProcessing.getServiceProcessor();
		if (serviceProcess != null)
			return serviceProcess.getExternalId();
		else
			return null;
	}
	
	/**
	 * Returns the number as string, with the given number of decimal Digits.
	 */
	public static String formatNumber(Number number, int decimalDigits) {
		if (decimalDigits <= 0)
			return number.toString();
		
		String format = ".";
		for (int i = 0; i < decimalDigits; i++)
			format += "0";
		
		return NumberFormat.getFormat(format).format(number);
	}
	
	/**
	 * Prepares the string value based on the instance and the type (used only for Enums or Entities).
	 * @param codecRegistry - codes for the simplified values.
	 * @param propertyBean - if preparing display of a property, pass the property info.
	 */
	private static String prepareDisplayValue(PersistenceGmSession gmSession, String useCase, Object instance, GenericModelType genericModelType,
			CodecRegistry<String> codecRegistry, PropertyBean propertyBean) {
		String value = null;
		
		Codec<Object, String> rendererCodec = getRendererCodec(codecRegistry, genericModelType, gmSession, useCase, propertyBean);
		if (rendererCodec != null) {
			try {
				return SafeHtmlUtils.htmlEscape(rendererCodec.encode(instance));
			} catch (CodecException e) {
				logger.error("Error while encoding renderer value.", e);
				e.printStackTrace();
			}
		}
		
		boolean isEntity = genericModelType.isEntity();
		if (isEntity || genericModelType.isEnum()) {
			ModelMdResolver modelMdResolver;
			if (instance instanceof GenericEntity)
				modelMdResolver = getMetaData((GenericEntity) instance);
			else
				modelMdResolver = gmSession.getModelAccessory().getMetaData();
			if (isEntity) {
				String selectiveInformation = SelectiveInformationResolver.resolve((EntityType<?>) genericModelType, (GenericEntity) instance, modelMdResolver,
						useCase, true/* , null */);
				if (selectiveInformation != null && !selectiveInformation.trim().equals("")) {
					value = selectiveInformation;
				}
			} else {
				if (instance instanceof Enum) {
					Name name = modelMdResolver.enumConstant((Enum<?>) instance).useCase(useCase).meta(Name.T).exclusive();
					if (name != null && name.getName() != null)
						value = I18nTools.getLocalized(name.getName());
					else
						value = instance.toString();
				} else
					value = "";
			}
			
			if (value == null || value.isEmpty()) {
				Pair<Name, Description> pair = GMEMetadataUtil.getNameAndDescription(instance, genericModelType, modelMdResolver, useCase);
				if (pair != null && pair.first() != null && pair.first().getName() != null)
					value = I18nTools.getLocalized(pair.first().getName());
				else {
					if (isEntity)
						value = ((EntityType<?>) genericModelType).getShortName();
					else
						value = genericModelType.getTypeName();
				}
			}
		} else
			value = (instance != null ? instance.toString() : "");
		
		value = SafeHtmlUtils.htmlEscape(value);
		
		return value;
	}
	
	private static boolean isActionAvailable(ActionTypeAndName actionTypeAndName, List<Folder> subFolders) {
		for (Folder folder : subFolders) {
			String actionName = actionTypeAndName.getActionName();
			if (actionName != null && (actionName.equals(folder.getName()) || ("$" + actionName).equals(folder.getName())))
				return true;
			
			EntityType<? extends ActionFolderContent> denotationType = actionTypeAndName.getDenotationType();
			if (denotationType != null) {
				FolderContent folderContent = folder.getContent();
				if (folderContent != null && folderContent.entityType().equals(denotationType))
					return true;
			}
			
			List<Folder> childFolders = folder.getSubFolders();
			if (childFolders != null && !childFolders.isEmpty()) {
				if (isActionAvailable(actionTypeAndName, childFolders))
					return true;
			}
		}
		
		return false;
	}
	
	private static Object getOwnerObject(PropertyManipulation manipulation) {
		LocalEntityProperty owner = (LocalEntityProperty) manipulation.getOwner();
		
		GenericEntity entity = owner.getEntity();
		return entity.entityType().getProperty(owner.getPropertyName()).get(entity);
	}
	
	public static class KeyAndValueGMTypeInstanceBean {
		private GMTypeInstanceBean key;
		private GMTypeInstanceBean value;
		
		public KeyAndValueGMTypeInstanceBean() {
		}
		
		public KeyAndValueGMTypeInstanceBean(GMTypeInstanceBean key, GMTypeInstanceBean value) {
			setKey(key);
			setValue(value);
		}
		
		public KeyAndValueGMTypeInstanceBean(GMTypeInstanceBean bean) {
			setKey(bean);
			setValue(bean);
		}
		
		public GMTypeInstanceBean getKey() {
			return key;
		}
		
		public void setKey(GMTypeInstanceBean key) {
			this.key = key;
		}
		
		public GMTypeInstanceBean getValue() {
			return value;
		}
		
		public void setValue(GMTypeInstanceBean value) {
			this.value = value;
		}
	}

	public static String getTabName(ModelPathElement element, String dataText) {
		if (!element.getType().isCollection()) {
			return dataText;
		}
		
		CollectionType collectionType = element.getType();
		String kind;
		switch (collectionType.getCollectionKind()) {
			case list:
				kind = " List";
				break;
			case set:
				kind = " Set";
				break;
			default:
				kind = " Map";
		}
		
		GenericModelType collectionElementType = collectionType.getCollectionElementType();
		if (collectionElementType.isBase()) {
			Object singleElement = null;
			Object value = element.getValue();
			if (value instanceof List) {
				if (((List<?>) value).size() == 1) {
					singleElement = ((List<?>) value).get(0);
				}
			} else if (value instanceof Set) {
				if (((Set<?>) value).size() == 1) {
					for (Object setEntry : (Set<?>) value)
						singleElement = setEntry;
				}
			}
			
			if (singleElement != null)
				collectionElementType = collectionElementType.getActualType(singleElement);
		}
		
		String typeName = collectionElementType.getTypeName();
		if (collectionElementType.isEntity() || collectionElementType.isEnum())
			typeName = ((CustomType) collectionElementType).getShortName();
		
		return typeName + kind;
	}
	
	/**
	 * Checks whether the dataTransfer contains a folder.
	 */
	public static native boolean isUploadingFolder(DataTransfer dataTransfer) /*-{
		try {
			if (dataTransfer.items && dataTransfer.items.length) {
				var dataList = dataTransfer.items;
				for (var i = 0; i < dataList.length; ++i) {
					var item = dataList[i];
					var entry = item.webkitGetAsEntry();
					if (entry)
						return entry.isDirectory;
			      	
			      	entry = item.getAsEntry();
			      	if (entry)
			         	return entry.isDirectory;
				}
			}
		} catch (e) {
			return false;
		}
		
		return false;
	}-*/;
}
