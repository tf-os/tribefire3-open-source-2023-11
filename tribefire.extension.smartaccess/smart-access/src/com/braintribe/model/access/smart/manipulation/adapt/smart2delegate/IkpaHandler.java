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
package com.braintribe.model.access.smart.manipulation.adapt.smart2delegate;

import static com.braintribe.model.access.smart.manipulation.SmartManipulationProcessor.USE_CASE;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.owner;
import static com.braintribe.utils.lcd.CollectionTools2.acquireList;

import java.util.Collection;
import java.util.Set;

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.smart.manipulation.SmartManipulationContextVariables;
import com.braintribe.model.access.smart.manipulation.SmartManipulationProcessor;
import com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.meta.ConvertibleQualifiedProperty;
import com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.QualifiedProperty;
import com.braintribe.model.processing.smart.SmartAccessException;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping;

/**
 * Covers properties with {@link InverseKeyPropertyAssignment} mapping.
 */
public class IkpaHandler implements Smart2DelegateHandler<InverseKeyPropertyAssignment> {

	private final SmartManipulationProcessor smp;
	private final ModelExpert modelExpert;
	private final SmartManipulationContextVariables $;

	private InverseKeyPropertyAssignment currentIkpa;
	private Object ikpaKey;

	private EntityProperty currentInverseNewOwner;
	private EntityMapping currentInverseEntityMapping;

	public IkpaHandler(SmartManipulationProcessor smp) {
		this.smp = smp;
		this.modelExpert = smp.modelExpert();
		this.$ = smp.context();
	}

	@Override
	public void loadAssignment(InverseKeyPropertyAssignment assignment) {
		QualifiedProperty keyProperty = assignment.getKeyProperty();
		String propertyName = keyProperty.getProperty().getName();
		propertyName = modelExpert.findDelegatePropertyForKeyProperty(keyProperty.propertyOwner(), $.currentAccess, propertyName,
				$.currentSmartType);

		currentIkpa = assignment;
		ikpaKey = smp.propertyValueResolver().acquireDelegatePropertyValue($.currentSmartReference, propertyName);
	}

	/* This is used as local variable inside methods, but is declared here to make code nicer */
	protected Manipulation delegateManipulation;

	@Override
	public void convertToDelegate(ChangeValueManipulation smartManipulation) {
		Set<EntityReference> currentValueSmartReferences = queryCurrentValueReferences($.currentSmartReference,
				getChangedProperty(smartManipulation));

		for (EntityReference ref: currentValueSmartReferences) {
			removeInverseRelationship(ref);
		}

		Object currentValue = smartManipulation.getNewValue();

		if (currentValue instanceof Collection) {
			for (Object ref: (Collection<?>) currentValue) {
				addInverseRelationship(ref);
			}
		} else if (currentValue != null) {
			addInverseRelationship(currentValue);
		}
	}

	private String getChangedProperty(PropertyManipulation cvm) {
		return ((EntityProperty) cvm.getOwner()).getPropertyName();
	}

	@Override
	public void convertToDelegate(AddManipulation smartManipulation) throws ModelAccessException {
		for (Object inverseOwnerReference: smartManipulation.getItemsToAdd().keySet()) {
			addInverseRelationship(inverseOwnerReference);
		}
	}

	@Override
	public void convertToDelegate(RemoveManipulation smartManipulation) throws ModelAccessException {
		for (Object inverseOwnerReference: smartManipulation.getItemsToRemove().keySet()) {
			removeInverseRelationship(inverseOwnerReference);
		}
	}

	@Override
	public void convertToDelegate(ClearCollectionManipulation smartManipulation) throws ModelAccessException {
		Set<EntityReference> currentValueSmartReferences = queryCurrentValueReferences($.currentSmartReference,
				getChangedProperty(smartManipulation));

		for (EntityReference ref: currentValueSmartReferences) {
			removeInverseRelationship(ref);
		}
	}

	private Set<EntityReference> queryCurrentValueReferences(EntityReference smartReference, String smartProperty) {
		return smp.propertyValueResolver().acquireSmartPropertyValueReferences(smartReference, smartProperty,
				findJoinIdProperty(smartReference, smartProperty));
	}

	private void addInverseRelationship(Object inverseOwnerReference) {
		loadInverseOwner(inverseOwnerReference);

		if (currentIkpa.getProperty().getProperty().getType() instanceof GmCollectionType) {
			delegateManipulation = ManipulationBuilder.add(currentInverseNewOwner, ikpaKey, ikpaKey);
		} else {
			delegateManipulation = ManipulationBuilder.changeValue(currentInverseNewOwner, ikpaKey);
		}

		acquireList($.delegateManipulations, currentInverseEntityMapping.getAccess()).add(delegateManipulation);
	}

	private void removeInverseRelationship(Object inverseOwnerReference) {
		loadInverseOwner(inverseOwnerReference);

		ConvertibleQualifiedProperty qp = currentIkpa.getProperty();

		if (qp.getProperty().getType() instanceof GmCollectionType) {
			delegateManipulation = ManipulationBuilder.remove(currentInverseNewOwner, ikpaKey, ikpaKey);
		} else {
			delegateManipulation = ManipulationBuilder.changeValue(currentInverseNewOwner, null);
		}

		acquireList($.delegateManipulations, currentInverseEntityMapping.getAccess()).add(delegateManipulation);
	}

	private void loadInverseOwner(Object inverseOwner) {
		EntityReference referencedSmartEntityReference = (EntityReference) inverseOwner;
		String referencedSmartSignature = referencedSmartEntityReference.getTypeSignature();

		/* Note that this doesn't have to be the same as $.currentSmartReferencedEntityType, it can be a sub-type. */
		GmEntityType actualReferencedSmartType = modelExpert.resolveSmartEntityType(referencedSmartSignature);
		IncrementalAccess access = smp.accessResolver().resolveAccess(referencedSmartEntityReference);
		currentInverseEntityMapping = modelExpert.resolveEntityMapping(actualReferencedSmartType, access, USE_CASE);

		EntityReference currentInverseDelegateReference = smp.referenceManager().acquireDelegateReference(referencedSmartEntityReference,
				currentInverseEntityMapping);
		currentInverseNewOwner = owner(currentInverseDelegateReference, getDelegatePropertyName(referencedSmartSignature, access));
	}

	private String getDelegatePropertyName(String currentInverseSmartSignature, IncrementalAccess access) {
		String mappedProperty = currentIkpa.getProperty().getProperty().getName();
		if (!$.currentSmartPropertyReferencesUnmappedType) {
			return mappedProperty;
		}

		GmEntityType currentInverseSmartGmEntityType = modelExpert.resolveSmartEntityType(currentInverseSmartSignature);

		EntityPropertyMapping epm = modelExpert.resolveEntityPropertyMapping(currentInverseSmartGmEntityType, access, mappedProperty);
		return epm.getDelegatePropertyName();
	}

	private String findJoinIdProperty(EntityReference smartReference, String smartProperty) {
		String signature = smartReference.getTypeSignature();
		EntityType<GenericEntity> et = GMF.getTypeReflection().getType(signature);
		GenericModelType propertyType = et.getProperty(smartProperty).getType();

		if (propertyType.isEntity() || propertyType.isCollection()) {
			return GenericEntity.id;
		}

		throw new SmartAccessException("Property cannot be joined as it does not reference an entity. Property: '" + signature + "." + smartProperty + "'. Type: " + propertyType);
	}
}
