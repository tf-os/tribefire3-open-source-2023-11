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
package com.braintribe.model.processing.traversing.engine.impl.clone.legacy;

import java.util.Stack;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.api.ModelPathElementType;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.BasicCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.pr.criteria.RootCriterion;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingPropertyModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingPropertyRelatedModelPathElement;
import com.braintribe.model.processing.traversing.engine.api.customize.ClonerCustomization;
import com.braintribe.model.processing.traversing.engine.api.customize.PropertyTransferContext;
import com.braintribe.model.processing.traversing.engine.impl.clone.BasicPropertyTransferExpert;

/**
 * @author peter.gazdik
 */
public class CloningContextBasedClonerCustomization implements ClonerCustomization {

	private final CloningContext cc;
	private final RootCriterion rootCriterion = RootCriterion.T.create();

	/**
	 * @param cc
	 *            a CloningContext which is compatible with GMT, most likely something that is explicitly marked as
	 *            {@link GmtCompatibleCloningContext}, or at least follows the same limitations.
	 */
	public CloningContextBasedClonerCustomization(CloningContext cc) {
		this.cc = cc;
	}

	@Override
	public void transferProperty(GenericEntity clonedEntity, Property property, Object clonedValue, PropertyTransferContext context) {
		BasicPropertyTransferExpert.INSTANCE.transferProperty(clonedEntity, property, clonedValue, context);
	}

	@Override
	public <T extends GenericEntity> T supplyRawClone(T instanceToBeCloned, GmTraversingContext context, TraversingModelPathElement pathElement,
			EntityType<T> entityType) {
		return (T) cc.supplyRawClone(entityType, instanceToBeCloned);
	}

	@Override
	public boolean isAbsenceResolvable(GenericEntity instanceToBeCloned, GmTraversingContext context,
			TraversingPropertyModelPathElement propertyPathElement, AbsenceInformation absenceInformation) {
		return cc.isAbsenceResolvable(propertyPathElement.getProperty(), instanceToBeCloned, absenceInformation);
	}

	@Override
	public AbsenceInformation createAbsenceInformation(GenericEntity instanceToBeCloned, GmTraversingContext context,
			TraversingPropertyModelPathElement propertyPathElement) {
		Property property = propertyPathElement.getProperty();
		return cc.createAbsenceInformation(property.getType(), instanceToBeCloned, property);
	}

	@Override
	public Object postProcessClonedPropertyRelatedValue( //
			Object clonedValue, GmTraversingContext context, TraversingPropertyRelatedModelPathElement pathElement) {

		BasicCriterion tc = resolveTcToPutOnStack(pathElement);
		Stack<BasicCriterion> traversingStack = cc.getTraversingStack();

		traversingStack.push(tc);

		try {
			return cc.postProcessCloneValue(pathElement.getType(), clonedValue);

		} finally {
			traversingStack.pop();

		}
	}

	private BasicCriterion resolveTcToPutOnStack(TraversingPropertyRelatedModelPathElement pathElement) {
		if (pathElement.getElementType() != ModelPathElementType.Property)
			return rootCriterion;

		Property property = pathElement.getProperty();

		PropertyCriterion result = PropertyCriterion.T.create();
		result.setPropertyName(property.getName());
		result.setTypeSignature(property.getType().getTypeSignature());
		return result;
	}

}
