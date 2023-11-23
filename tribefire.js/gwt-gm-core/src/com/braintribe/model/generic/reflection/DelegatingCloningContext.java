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

import java.util.Collection;
import java.util.Stack;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.BasicCriterion;
import com.braintribe.model.generic.pr.criteria.CriterionType;

public class DelegatingCloningContext implements CloningContext {
	private final TraversingContext traversingContextDelegate;
	
	@Override
	public Stack<BasicCriterion> getTraversingStack() {
		return traversingContextDelegate.getTraversingStack();
	}

	@Override
	public Stack<Object> getObjectStack() {
		return traversingContextDelegate.getObjectStack();
	}

	@Override
	public void pushTraversingCriterion(BasicCriterion criterion, Object object) {
		traversingContextDelegate.pushTraversingCriterion(criterion, object);
	}

	@Override
	public BasicCriterion popTraversingCriterion() {
		return traversingContextDelegate.popTraversingCriterion();
	}

	@Override
	public boolean isTraversionContextMatching() {
		return traversingContextDelegate.isTraversionContextMatching();
	}

	@Override
	public void registerAsVisited(GenericEntity entity, Object associate) {
		traversingContextDelegate.registerAsVisited(entity, associate);
	}

	@Override
	public <T> T getAssociated(GenericEntity entity) {
		return (T)traversingContextDelegate.getAssociated(entity);
	}

	@Override
	public Collection<GenericEntity> getVisitedObjects() {
		return traversingContextDelegate.getVisitedObjects();
	}

	@Override
	public <T> Collection<T> getAssociatedObjects() {
		return traversingContextDelegate.getAssociatedObjects();
	}

	@Override
	public boolean isVisited(GenericEntity entity) {
		return traversingContextDelegate.isVisited(entity);
	}

	public DelegatingCloningContext(TraversingContext traversingContextDelegate) {
		this.traversingContextDelegate = traversingContextDelegate;
	}
	
	@Override
	public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
		return entityType.create();
	}
	
	@Override
	public boolean canTransferPropertyValue(
			EntityType<? extends GenericEntity> entityType, Property property,
			GenericEntity instanceToBeCloned, GenericEntity clonedInstance,
			AbsenceInformation sourceAbsenceInformation) {
		return true;
	}
	
	@Override
	public Object postProcessCloneValue(GenericModelType propertyType,
			Object clonedValue) {
		return clonedValue;
	}
	
	@Override
	public AbsenceInformation createAbsenceInformation(GenericModelType type, GenericEntity instanceToBeCloned, Property property) {
		return GMF.absenceInformation();
	}
	
	@Override
	public GenericEntity preProcessInstanceToBeCloned(GenericEntity instanceToBeCloned) {
		return instanceToBeCloned;
	}
	
	/**
	 * @see com.braintribe.model.generic.reflection.CloningContext#isAbsenceResolvable(com.braintribe.model.generic.reflection.Property,
	 *      com.braintribe.model.generic.GenericEntity, com.braintribe.model.generic.pr.AbsenceInformation)
	 */
	@Override
	public boolean isAbsenceResolvable(Property property, GenericEntity instanceToBeCloned, AbsenceInformation absenceInformation) {
		return false;
	}
	
	@Override
	public boolean isPropertyValueUsedForMatching(EntityType<?> type, GenericEntity entity, Property property) {
		return true;
	}

	@Override
	public CriterionType getCurrentCriterionType() {
		return traversingContextDelegate.getCurrentCriterionType();
	}

	@Override
	public StrategyOnCriterionMatch getStrategyOnCriterionMatch() {
		return StrategyOnCriterionMatch.partialize;
	}
	
}
