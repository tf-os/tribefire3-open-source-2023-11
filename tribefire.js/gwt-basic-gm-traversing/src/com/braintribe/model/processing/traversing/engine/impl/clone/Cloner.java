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
package com.braintribe.model.processing.traversing.engine.impl.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.api.ModelPathElementType;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedList;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedMap;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedSet;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.GmTraversingException;
import com.braintribe.model.processing.traversing.api.GmTraversingVisitor;
import com.braintribe.model.processing.traversing.api.SkipUseCase;
import com.braintribe.model.processing.traversing.api.path.TraversingMapKeyModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingMapValueModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingPropertyModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingPropertyRelatedModelPathElement;
import com.braintribe.model.processing.traversing.engine.api.customize.ClonerCustomization;
import com.braintribe.model.processing.traversing.engine.api.usecase.AbsentifySkipUseCase;
import com.braintribe.model.processing.traversing.engine.api.usecase.DefaultSkipUseCase;
import com.braintribe.model.processing.traversing.engine.api.usecase.ReferenceSkipUseCase;

public class Cloner implements GmTraversingVisitor {

	private ClonerCustomization customizer = new BasicClonerCustomization();
	private Object clonedRootValue;
	private final Map<GenericEntity, GenericEntity> clonedEntitiesMap = new HashMap<>();
	private final PropertyTransferContextImpl propertyTransferContext = new PropertyTransferContextImpl();

	@Override
	public void onElementEnter(GmTraversingContext context, TraversingModelPathElement pathElement) throws GmTraversingException {

		// get clonedValue
		Object clonedValue = null;
		// cloned value is only needed if skipUseCase is null, otherwise we either skip, make absent or set a reference based on old value
		SkipUseCase skipUseCase = context.getSkipUseCase();
		if (skipUseCase == null) {
			clonedValue = clonePathElementValue(context, pathElement);
		}

		switch (pathElement.getElementType()) {
			case Root:
			case EntryPoint:
				// just add the root
				this.clonedRootValue = clonedValue;
				break;
			case Property: {
				
				// in case we want to skip this property, we do not do anything here
				if (skipUseCase == DefaultSkipUseCase.INSTANCE) {
					/* PGA TODO check with Dirk. I changed this so in the default skip case the original property is kept. The original
					 * implementation of the cloner would replace it with null, cause it was not doing this check */
					break;
				}

				TraversingPropertyModelPathElement propertyPathElement = (TraversingPropertyModelPathElement) pathElement;

				// get currentProperty
				Property property = propertyPathElement.getProperty();

				// get value according to skipUseCase
				Object propertyValue = getNewPropertyValue(clonedValue, skipUseCase, context, propertyPathElement);

				// in this case we do not want to set the property
				if (skipUseCase == AbsentifySkipUseCase.INSTANCE) {
					break;
				}

				// standard post process clonedValue
				Object postProcessingClonedValue = customizer.postProcessClonedPropertyRelatedValue(propertyValue, context,
						propertyPathElement);

				// get parent, i.e. cloned instance
				GenericEntity clonedEntity = context.getSharedCustomValue(pathElement.getPrevious());

				propertyTransferContext.traversingContext = context;
				propertyTransferContext.propertyPathElement = propertyPathElement;
				propertyTransferContext.clonedEntity = clonedEntity;
				customizer.transferProperty(clonedEntity, property, postProcessingClonedValue, propertyTransferContext);

				break;
			}
			case ListItem:
			case SetItem: {
				// get parent (which should be the new set created)
				Collection<Object> parentCollection = context.getSharedCustomValue(pathElement.getPrevious());
				// get value according to the skipUseCase
				Object collectionItemValue = getCollectionItemValue(clonedValue, skipUseCase, pathElement);

				// keep null values when the original was null and it is not the case that there should be a skip
				boolean keepNullValue = (clonedValue == null && !skipCollectionItemEvaluation(skipUseCase));

				if (collectionItemValue != null || keepNullValue) {
					// standard post process clonedValue
					Object postProcesseingClonedValue = customizer.postProcessClonedPropertyRelatedValue(collectionItemValue, context,
							(TraversingPropertyRelatedModelPathElement) pathElement);
					// add value to list
					parentCollection.add(postProcesseingClonedValue);
				}
				break;
			}
			case MapKey: {
				// get value according to the skipUseCase
				Object useCaseClonedValueMapKey = getCollectionItemValue(clonedValue, skipUseCase, pathElement);

				// add the pre processing value so that it is used in the if check prior to inserting values in the map
				context.setVisitorSpecificCustomValue(pathElement, useCaseClonedValueMapKey);

				Object postProcesseingClonedValue = customizer.postProcessClonedPropertyRelatedValue(useCaseClonedValueMapKey, context,
						(TraversingPropertyRelatedModelPathElement) pathElement);

				// store it as it will be added to the map, via the MapValue
				context.setSharedCustomValue(pathElement, postProcesseingClonedValue);
				break;
			}
			case MapValue: {
				// get map value
				TraversingMapValueModelPathElement mapValue = (TraversingMapValueModelPathElement) pathElement;
				// get parent (previous)
				Map<Object, Object> parentMap = context.getSharedCustomValue(mapValue.getPrevious());
				// get key
				TraversingMapKeyModelPathElement keyElement = mapValue.getKeyElement();
				// get the value of the key according to previous computation
				Object mapKey = keyElement.getSharedCustomValue();
				// verify
				// get value according to the skipUseCase
				Object useCaseClonedValueMapValue = getCollectionItemValue(clonedValue, skipUseCase, pathElement);
				// do not add null, null to map if both are from pre-processing
				if (useCaseClonedValueMapValue != null && context.getVisitorSpecificCustomValue(keyElement) != null) {
					Object postProcesseingClonedValue = customizer.postProcessClonedPropertyRelatedValue(useCaseClonedValueMapValue,
							context, (TraversingPropertyRelatedModelPathElement) pathElement);

					// add entry to Map
					parentMap.put(mapKey, postProcesseingClonedValue);
				}
				break;
			}
		}

	}

	/**
	 * Retrieves the value of the collection item according to the {@link SkipUseCase}
	 * 
	 * @return The clonedValue input parameter if there is no SkipUseCase, the original item reference if the SkipUseCase is of type
	 *         {@link ReferenceSkipUseCase} and null otherwise
	 */
	private static Object getCollectionItemValue(Object clonedValue, SkipUseCase skipUseCase, TraversingModelPathElement pathElement) {
		if (!skipCollectionItemEvaluation(skipUseCase)) {
			if (skipUseCase != null) {
				if (skipUseCase == ReferenceSkipUseCase.INSTANCE) {
					return pathElement.getValue();
				}
			} else {
				return clonedValue;
			}
		}

		return null;
	}

	/**
	 * Checks if a collection item requires evaluation. Skipping evaluation is due to the existence of a {@link SkipUseCase} that is of type
	 * {@link AbsentifySkipUseCase} or {@link DefaultSkipUseCase} as both are treated as a skip in collections
	 * 
	 * @return true if there is a skip Use Case of type {@link AbsentifySkipUseCase} or {@link DefaultSkipUseCase}
	 */
	private static boolean skipCollectionItemEvaluation(SkipUseCase skipUseCase) {
		return skipUseCase == AbsentifySkipUseCase.INSTANCE || skipUseCase == DefaultSkipUseCase.INSTANCE;
	}

	/**
	 * Evaluate a property value according to the {@link SkipUseCase}. 
	 */
	private Object getNewPropertyValue(Object clonedValue, SkipUseCase skipUseCase, GmTraversingContext context,
			TraversingPropertyModelPathElement propertyPathElement) {

		// cloned entity, i.e. property owner
		GenericEntity clonedEntity = (GenericEntity) context.getSharedCustomValue(propertyPathElement.getPrevious());

		// instance to be cloned
		GenericEntity entity = (GenericEntity) propertyPathElement.getPrevious().getValue();
		// get currentProperty
		Property property = propertyPathElement.getProperty();
		AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);
		Object oldPropertyValue = propertyPathElement.getValue();

		if (shouldResolveAbscenceInformation(entity, skipUseCase, context, absenceInformation, propertyPathElement)) {
			oldPropertyValue = property.get(entity);
			absenceInformation = null;
		}

		Object result = null;

		// if there is valid skipUseCase
		if (skipUseCase == null) {
			return clonedValue;
		}
		
		if (skipUseCase == ReferenceSkipUseCase.INSTANCE) {
			// here the cloning is stopped and original reference is transferred
			// if original value is a collection, or a map, then copy the whole thing
			return getPropertyReferenceValue(oldPropertyValue, propertyPathElement);
		}
		
		if (skipUseCase == AbsentifySkipUseCase.INSTANCE) {
			if (absenceInformation == null) {
				// here the cloning is stopped and an absence information is placed instead
				absenceInformation = customizer.createAbsenceInformation(entity, context, propertyPathElement);
			}
			
			if (absenceInformation != null) {
				property.setAbsenceInformation(clonedEntity, absenceInformation);
			}
			return null;
		}

		return result;
	}

	/**
	 * @return propertyValue as it is in case of non-collections. Otherwise, a new collection is created which is filled up with the
	 *         original items
	 */
	private static Object getPropertyReferenceValue(Object propertyValue, TraversingPropertyModelPathElement propertyPathElement) {
		Object referenceValue = propertyValue;

		if (propertyValue != null) {
			switch (propertyPathElement.getType().getTypeCode()) {
				case listType: {
					List<?> originalList = (List<?>) propertyValue;
					if (originalList.size() > 0) {
						List<Object> tempList = new ArrayList<Object>();
						for (Object item: originalList) {
							tempList.add(item);
						}
						referenceValue = tempList;
					}
					break;
				}
				case setType: {
					Set<?> originalSet = (Set<?>) propertyValue;
					Set<Object> tempSet = new HashSet<Object>();
					if (originalSet.size() > 0) {
						for (Object item: originalSet) {
							tempSet.add(item);
						}
						referenceValue = tempSet;
					}
					break;
				}
				case mapType:
					Map<Object, Object> currentMap = (Map<Object, Object>) propertyValue;
					if (currentMap.size() > 0) {
						Map<Object, Object> tempMap = new HashMap<Object, Object>();
						Set<Object> mapKeys = currentMap.keySet();
						for (Object key: mapKeys) {
							tempMap.put(key, currentMap.get(key));
						}
						referenceValue = tempMap;
					}
					break;
				default:// anything else leave the reference as it is
					break;
			}
		}
		return referenceValue;

	}

	/**
	 * Checks if absence information should be resolved or not. It is only resolved if the @
	 * ClonerCustomization#isAbsenceResolvable(GenericEntity, GmTraversingContext, TraversingPropertyModelPathElement, AbsenceInformation)}
	 * returns true and a {@link AbsentifySkipUseCase} is not used.
	 */
	private boolean shouldResolveAbscenceInformation(GenericEntity instanceToBeCloned, SkipUseCase skipUseCase, GmTraversingContext context,
			AbsenceInformation absenceInformation, TraversingPropertyModelPathElement propertyPathElement) {

		if (absenceInformation == null) {
			return false;
		}
		
		// it only makes sense to resolve AI if SkipUseCase doesn't say we want to set AI in the clone as well
		if (skipUseCase == AbsentifySkipUseCase.INSTANCE) {
			return false;
		}
		
		return customizer.isAbsenceResolvable(instanceToBeCloned, context, propertyPathElement, absenceInformation);
	}

	/**
	 * @return If there is no absence information involved, then the value of the path element is returned directly, otherwise resolve
	 *         absence information if needed
	 */
	private Object getPathValue(GmTraversingContext context, TraversingModelPathElement pathElement) {

		if (pathElement.getElementType() == ModelPathElementType.Property) {

			TraversingPropertyModelPathElement propertyPathElement = (TraversingPropertyModelPathElement) pathElement;
			// instance to be cloned
			GenericEntity entity = (GenericEntity) propertyPathElement.getPrevious().getValue();
			// get currentProperty
			Property property = propertyPathElement.getProperty();
			AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);
			Object propertyValue = propertyPathElement.getValue();

			if (shouldResolveAbscenceInformation(entity, null, context, absenceInformation, propertyPathElement)) {
				propertyValue = property.get(entity);
			}
			return propertyValue;

		}
		return pathElement.getValue();

	}

	/**
	 * Clones the pathElement value. Cloning is performed on the resolved value if absence information is present and has to be resolved.
	 * 
	 * If {@link GenericModelType} is a simple type, then the clone would be the value itself. Otherwise, for complex types, a new
	 * Entity/Collection is created.
	 */
	private Object clonePathElementValue(GmTraversingContext context, TraversingModelPathElement pathElement) {

		// simple types are just cloned by getting the value. If there is absence information, check if value needs
		// to be resolved as well
		Object clonedValue = getPathValue(context, pathElement);

		if (clonedValue != null) {
			GenericModelType type = pathElement.getType();
			switch (type.getTypeCode()) {
				case listType:
					clonedValue = new EnhancedList<>((ListType) type);
					context.setSharedCustomValue(pathElement, clonedValue);
					break;
				case mapType:
					clonedValue = new EnhancedMap<>((MapType)type);
					context.setSharedCustomValue(pathElement, clonedValue);
					break;
				case setType:
					clonedValue = new EnhancedSet<>((SetType)type);
					context.setSharedCustomValue(pathElement, clonedValue);
					break;
				case entityType: {
					GenericEntity entityToBeCloned = (GenericEntity) clonedValue;

					// check the map to see if there was a hit
					GenericEntity clonedEntity = clonedEntitiesMap.get(entityToBeCloned);

					// there was no match in the map
					if (clonedEntity == null) {
						// clone the entity
						clonedEntity = customizer.supplyRawClone(entityToBeCloned, context, pathElement, (EntityType<GenericEntity>) type);

						// clone entities one time only, keep it in a map
						clonedEntitiesMap.put(entityToBeCloned, clonedEntity);
					}
					clonedValue = clonedEntity;
					context.setSharedCustomValue(pathElement, clonedEntity);
					break;
				}
				default:
					break;
			}
		}
		return clonedValue;

	}

	@Override
	public void onElementLeave(GmTraversingContext context, TraversingModelPathElement pathElement) throws GmTraversingException {
		// All the work is done by onElementEnter
	}

	public void setCustomizer(ClonerCustomization customizer) {
		this.customizer = customizer;
	}

	public <V> V getClonedValue() {
		return (V) clonedRootValue;
	}

}
