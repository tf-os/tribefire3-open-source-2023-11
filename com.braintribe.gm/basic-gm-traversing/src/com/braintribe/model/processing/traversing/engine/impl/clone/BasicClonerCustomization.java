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

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingPropertyModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingPropertyRelatedModelPathElement;
import com.braintribe.model.processing.traversing.engine.api.customize.ClonerCustomization;
import com.braintribe.model.processing.traversing.engine.api.customize.PropertyTransferContext;
import com.braintribe.model.processing.traversing.engine.api.customize.PropertyTransferExpert;

public class BasicClonerCustomization implements ClonerCustomization {

	protected GmSession session;
	protected AbsenceInformation absenceInformation = GMF.absenceInformation();
	protected boolean absenceResolvable = false;
	protected PropertyTransferExpert propertyTransferExpert = BasicPropertyTransferExpert.INSTANCE;

	public void setSession(GmSession session) {
		this.session = session;
	}

	public void setPropertyTransferExpert(PropertyTransferExpert propertyTransferExpert) {
		this.propertyTransferExpert = propertyTransferExpert;
	}

	@Override
	public AbsenceInformation createAbsenceInformation(GenericEntity instanceToBeCloned, GmTraversingContext context,
			TraversingPropertyModelPathElement propertyPathElement) {
		return absenceInformation;
	}

	@Override
	public <T extends GenericEntity> T supplyRawClone(T instanceToBeCloned, GmTraversingContext context,
			TraversingModelPathElement pathElement, EntityType<T> entityType) {
		if (session != null)
			return session.create(entityType);
		else
			return entityType.create();
	}

	@Override
	public Object postProcessClonedPropertyRelatedValue(Object clonedValue, GmTraversingContext context,
			TraversingPropertyRelatedModelPathElement pathElement) {
		return clonedValue;
	}

	public void setAbsenceResolvable(boolean absenceResolvable) {
		this.absenceResolvable = absenceResolvable;
	}

	@Override
	public boolean isAbsenceResolvable(GenericEntity instanceToBeCloned, GmTraversingContext context,
			TraversingPropertyModelPathElement propertyPathElement, AbsenceInformation absenceInformation) {
		return absenceResolvable;
	}

	@Override
	public void transferProperty(GenericEntity clonedEntity, Property property, Object clonedValue, PropertyTransferContext context) {
		propertyTransferExpert.transferProperty(clonedEntity, property, clonedValue, context);
	}

}
