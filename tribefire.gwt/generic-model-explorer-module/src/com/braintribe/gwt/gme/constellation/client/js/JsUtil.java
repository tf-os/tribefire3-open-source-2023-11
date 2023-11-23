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
package com.braintribe.gwt.gme.constellation.client.js;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.gwt.genericmodel.client.codec.dom4.GmXmlCodec;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.js.interop.InteropConstants;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.EntryPointPathElement;
import com.braintribe.model.generic.path.ListItemPathElement;
import com.braintribe.model.generic.path.MapKeyPathElement;
import com.braintribe.model.generic.path.MapValuePathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.path.SetItemPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.session.api.managed.ManipulationLenience;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.processing.session.api.managed.ManipulationReport;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Utility class with methods to be used in the JS side, via JsInterop.
 * It is used for being able to manipulate Java objects within JS code via the exposed methods.
 *
 */
@JsType(namespace = InteropConstants.JS_UTIL_NAMESPACE)
@SuppressWarnings("unusable-by-js")
public class JsUtil {
	
	private static Map<PreliminaryEntityReference, GenericEntity> instantiations;
	
	@JsConstructor
	public JsUtil() {
		super();
	}
	
	/**
	 * Prepares a new List.
	 */
	@JsMethod
	public List<?> prepareList() {
		return new ArrayList<>();
	}
	
	/**
	 * Prepares a new Set.
	 */
	@JsMethod
	public Set<?> prepareSet() {
		return new HashSet<>();
	}
	
	/**
	 * Adds the given object to the given collection. If first checks if collection is an instance of {@link Collection}.
	 */
	@JsMethod
	public void addToCollection(Object object, Object collection) {
		if (collection instanceof Collection)
			((Collection<Object>) collection).add(object);
	}
	
	/**
	 * Returns an array for the given collection.
	 * If not a collection, then null is returned.
	 */
	@JsMethod
	public Object[] getCollectionAsArray(Object collection) {
		if (collection instanceof List)
			return ((List<?>) collection).toArray();
		else if (collection instanceof Set)
			return ((Set<?>) collection).toArray();
		
		return null;
	}
	
	
	/**
	 * Returns the type signature of the given object.
	 */
	@JsMethod
	public String getObjectTypeSignature(Object object) {
		if (object == null)
			return null;
		
		GenericModelType type = GMF.getTypeReflection().getType(object);
		return type != null ? type.getTypeSignature() : null;
	}
	
	/**
	 * Returns the {@link EntityType} of the given object, if it is a {@link GenericEntity}.
	 */
	@JsMethod
	public EntityType<?> getObjectEntityType(Object object) {
		if (object == null)
			return null;
		
		return object instanceof GenericEntity ? ((GenericEntity) object).entityType() : null;
	}
	
	
	/**
	 * Prepares a {@link ModelPath}.
	 */
	@JsMethod
	public ModelPath prepareModelPath() {
		return new ModelPath();
	}
	
	/**
	 * Prepares a {@link RootPathElement} with the given object.
	 */
	@JsMethod
	public RootPathElement prepareRootPathElement(Object object) {
		GenericModelType type = GMF.getTypeReflection().getType(object);
		return new RootPathElement(type , object);
	}
	
	/**
	 * Adds the given {@link ModelPathElement} to the given {@link ModelPath}.
	 */
	@JsMethod
	public void addElementToModelPath(ModelPath modelPath, ModelPathElement modelPathElement) {
		modelPath.add(modelPathElement);
	}
	
	/**
	 * Prepares a new {@link PropertyPathElement} with the given values.
	 */
	@JsMethod
	public PropertyPathElement preparePropertyPathElement(GenericEntity parentEntity, String propertyName, Object propertyValue) {
		Property property = parentEntity.entityType().getProperty(propertyName);
		return new PropertyPathElement(parentEntity, property, propertyValue);
	}
	
	/**
	 * Prepares a new {@link ListItemPathElement} with the given values.
	 */
	@JsMethod
	public ListItemPathElement prepareNewListItemElement(GenericEntity parentEntity, String propertyName, int index, Object entry) {
		Property property = parentEntity.entityType().getProperty(propertyName);
		return new ListItemPathElement(parentEntity, property, index, GMF.getTypeReflection().getType(entry), entry);
	}
	
	/**
	 * Prepares a new {@link SetItemPathElement} with the given values.
	 */
	@JsMethod
	public SetItemPathElement prepareNewSetItemPathElement(GenericEntity parentEntity, String propertyName, Object entry) {
		Property property = parentEntity.entityType().getProperty(propertyName);
		return new SetItemPathElement(parentEntity, property, GMF.getTypeReflection().getType(entry), entry);
	}
	
	/**
	 * Prepares a new {@link MapKeyPathElement} with the given values.
	 */
	@JsMethod
	public MapKeyPathElement prepareNewMapKeyPathElement(GenericEntity parentEntity, String propertyName, Object key, Object value) {
		Property property = parentEntity.entityType().getProperty(propertyName);
		return new MapKeyPathElement(parentEntity, property, GMF.getTypeReflection().getType(key), key, GMF.getTypeReflection().getType(value),
				value);
	}
	
	/**
	 * Prepares a new {@link MapValuePathElement} with the given values.
	 */
	@JsMethod
	public MapValuePathElement prepareNewMapValuePathElement(GenericEntity parentEntity, String propertyName, Object key, Object value) {
		Property property = parentEntity.entityType().getProperty(propertyName);
		return new MapValuePathElement(parentEntity, property, GMF.getTypeReflection().getType(key), key, GMF.getTypeReflection().getType(value),
				value, prepareNewMapKeyPathElement(parentEntity, propertyName, key, value));
	}
	
	/**
	 * Returns the first element of the given {@link ModelPath}.
	 */
	@JsMethod
	public ModelPathElement getFirstElement(ModelPath modelPath) {
		return modelPath.first();
	}
	
	/**
	 * Returns the last element of the given {@link ModelPath}.
	 */
	@JsMethod
	public ModelPathElement getLastElement(ModelPath modelPath) {
		return modelPath.last();
	}
	
	/**
	 * Returns the element of the given {@link ModelPath} for the given index.
	 */
	@JsMethod
	public ModelPathElement getElementByIndex(ModelPath modelPath, int index) {
		return modelPath.get(index);
	}
	
	/**
	 * Returns the next element of the given {@link ModelPathElement}.
	 * Null is returned in case there is no next element.
	 */
	@JsMethod
	public ModelPathElement getNextElement(ModelPathElement element) {
		return element.getNext();
	}
	
	/**
	 * Returns the previous element of the given {@link ModelPathElement}.
	 * Null is returned in case there is no previous element.
	 */
	@JsMethod
	public ModelPathElement getPreviousElement(ModelPathElement element) {
		return element.getPrevious();
	}
	
	/**
	 * Returns the value of the given {@link ModelPathElement}.
	 */
	@JsMethod
	public Object getElementValue(ModelPathElement element) {
		return element.getValue();
	}
	
	/**
	 * Returns the type of the given {@link ModelPathElement}.
	 */
	@JsMethod
	public GenericModelType getElementType(ModelPathElement element) {
		return element.getType();
	}
	
	/**
	 * Returns the type signature of the value of the given {@link ModelPathElement}.
	 */
	@JsMethod
	public String getElementTypeSignature(ModelPathElement element) {
		return element.getType().getTypeSignature();
	}
	
	@JsMethod
	public boolean isCollectionElementRelated(ModelPathElement element) {
		return element.isCollectionElementRelated();
	}
	
	@JsMethod
	public boolean isPropertyRelated(ModelPathElement element) {
		return element.isPropertyRelated();
	}
	
	@JsMethod
	public boolean isEntryPoint(ModelPathElement element) {
		return element instanceof EntryPointPathElement;
	}
	
	@JsMethod
	public boolean isRootPath(ModelPathElement element) {
		return element instanceof RootPathElement;
	}
	
	@JsMethod
	public boolean isListItem(ModelPathElement element) {
		return element instanceof ListItemPathElement;
	}
	
	@JsMethod
	public boolean isSetItem(ModelPathElement element) {
		return element instanceof SetItemPathElement;
	}
	
	@JsMethod
	public boolean isMapKey(ModelPathElement element) {
		return element instanceof MapKeyPathElement;
	}
	
	@JsMethod
	public boolean isMapValue(ModelPathElement element) {
		return element instanceof MapValuePathElement;
	}
	
	@JsMethod
	public boolean isPropertyPathElement(ModelPathElement element) {
		return element instanceof PropertyPathElement;
	}
	
	@JsMethod
	public <T> String encodeData(T data) {
		GmXmlCodec<T> codec = new GmXmlCodec<>();
		return codec.encode(data);
	}
	
	@JsMethod
	public <T> T decodeData(String encodedData) {
		GmXmlCodec<T> codec = new GmXmlCodec<>();
		return codec.decode(encodedData);
	}
	
	/**
	 * This method receives a String containing a list of manipulations, decodes them, and then apply them to the given session.
	 * It then takes the ManipulationReport received, and prepare a list of PreliminaryEntityReferences which were created by those manipulations. 
	 */
	@JsMethod
	public List<PreliminaryEntityReference> decodeAndApplyManipulations(String encodedManipulations, PersistenceGmSession gmSession) {
		List<Manipulation> manipulations = decodeData(encodedManipulations);
		
		CompoundManipulation compound = CompoundManipulation.create(manipulations);
		NestedTransaction nestedTransaction = gmSession.getTransaction().beginNestedTransaction();
		ManipulationReport report = gmSession.manipulate().mode(ManipulationMode.REMOTE).lenience(ManipulationLenience.manifestOnUnknownEntity)
				.instantiations(instantiations).apply(compound);
		
		Map<PreliminaryEntityReference, GenericEntity> map = report.getInstantiations();
		if (map != null) {
			if (instantiations == null)
				instantiations = map;
			else
				instantiations.putAll(instantiations);
		}
		
		nestedTransaction.commit();
		List<PreliminaryEntityReference> instantiatedReferencesList = new ArrayList<>();
		for (GenericEntity instantiatedEntity : report.getInstantiations().values()) {
			EntityReference reference = instantiatedEntity.reference();
			if (reference.referenceType().equals(EntityReferenceType.preliminary))
				instantiatedReferencesList.add((PreliminaryEntityReference) reference);
		}
		
		return instantiatedReferencesList;
	}
	
	/**
	 * Returns the entity from the session cache by type and id (or refId).
	 */
	@JsMethod
	public GenericEntity findEntityFromCache(PersistenceGmSession session, Object entity, Object refId) {
		Object entityId = getEntityId(entity);
		String typeSignature = getEntityTypeSignature(entity);
		if (entityId != null)
			return session.queryCache().entity(typeSignature, entityId).find();
		
		PreliminaryEntityReference reference = PreliminaryEntityReference.T.create();
		reference.setTypeSignature(typeSignature);
		reference.setRefId(refId != null ? refId : getRefId(entity));
		reference.setRefPartition(getRefPartition(entity));
		
		return session.queryCache().entity(reference).find();
	}
	
	/**
	 * Returns the entity from the session by globalId.
	 */
	@JsMethod
	public GenericEntity findEntityByGlobalId(PersistenceGmSession session, String globalId) {
		return session.findEntityByGlobalId(globalId);
	}
	
	@JsMethod
	public boolean isEntity(Object entity) {
		return entity instanceof GenericEntity;
	}
	
	/**
	 * Clones the given externalActionProviderConfiguration, from another GWT space, to this one.
	 */
	@JsMethod
	public ActionProviderConfiguration prepareActionProviderConfiguration(GmContentView view, Object externalActionProviderConfiguration) {
		return JsActionUtil.prepareActionProviderConfiguration(view, externalActionProviderConfiguration);
	}
	
	private native String getEntityTypeSignature(Object externalEntity) /*-{
		return externalEntity.EntityType().getTypeSignature();
	}-*/;
	
	private native String getRefId(Object entity) /*-{
		return entity.Reference().refId;
	}-*/;
	
	private native String getRefPartition(Object entity) /*-{
		return entity.Reference().refPartition;
	}-*/;
	
	private native Object getEntityId(Object entity) /*-{
		return entity.id;
	}-*/;

}
