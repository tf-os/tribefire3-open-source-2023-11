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
package com.braintribe.model.generic.reflection;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;


@SuppressWarnings("unusable-by-js")
public class StandardCloningContext extends StandardTraversingContext implements CloningContext {

	private StrategyOnCriterionMatch strategyOnCriterionMatch = StrategyOnCriterionMatch.partialize;
	
	@Override
	public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
		return entityType.createRaw();
	}
	
	/**
	 * @deprecated use {@link #canTransferPropertyValue(EntityType, Property, GenericEntity, GenericEntity, AbsenceInformation)} instead
	 */
	@Deprecated
	@SuppressWarnings("unused")
	public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property, GenericEntity instanceToBeCloned) {
		return true;
	}

	@Override
	public boolean canTransferPropertyValue(
			EntityType<? extends GenericEntity> entityType, Property property,
			GenericEntity instanceToBeCloned, GenericEntity clonedInstance,
			AbsenceInformation sourceAbsenceInformation) {
		return canTransferPropertyValue(entityType, property, instanceToBeCloned);
	}

	@Override
	public Object postProcessCloneValue(GenericModelType propertyOrElementType, Object clonedValue) {
		return clonedValue;
	}
	
	/**
	 * @see com.braintribe.model.generic.reflection.CloningContext#createAbsenceInformation(com.braintribe.model.generic.reflection.GenericModelType, com.braintribe.model.generic.GenericEntity, com.braintribe.model.generic.reflection.Property)
	 */
	@Override
	public AbsenceInformation createAbsenceInformation(GenericModelType type, GenericEntity instanceToBeCloned, Property property) {
		return GMF.absenceInformation();
	}
	
	/**
	 * @see com.braintribe.model.generic.reflection.CloningContext#preProcessInstanceToBeCloned(com.braintribe.model.generic.GenericEntity)
	 */
	@Override
	public GenericEntity preProcessInstanceToBeCloned(GenericEntity instanceToBeCloned) {
		return instanceToBeCloned;
	}
	
	@Override
	public StrategyOnCriterionMatch getStrategyOnCriterionMatch() {
		return strategyOnCriterionMatch;
	}

	public void setStrategyOnCriterionMatch(StrategyOnCriterionMatch strategyOnCriterionMatch) {
		this.strategyOnCriterionMatch = strategyOnCriterionMatch;
	}

}
