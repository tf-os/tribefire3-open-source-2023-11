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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.pr.AbsenceInformation;

import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
@SuppressWarnings("unusable-by-js")
public interface CloningContext extends TraversingContext {

	GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned);

	/**
	 * Checks whether the property on original entity should even be handled. This is the very first check, before the property value is even looked
	 * at.
	 * <p>
	 * If this method returns
	 * <ul>
	 * <li><tt>false</tt>, the property is ignored and the next one is considered
	 * <li><tt>true</tt>, the property value is retrieved and another check is made with {@link #isTraversionContextMatching()}
	 * </ul>
	 * 
	 * @see #isAbsenceResolvable
	 * @see #isTraversionContextMatching()
	 */
	boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property, GenericEntity instanceToBeCloned,
			GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation);

	/**
	 * This method is called after a property value or a collection element are cloned to allow additional adjustments.
	 */
	Object postProcessCloneValue(GenericModelType propertyOrElementType, Object clonedValue);

	AbsenceInformation createAbsenceInformation(GenericModelType type, GenericEntity instanceToBeCloned, Property property);

	GenericEntity preProcessInstanceToBeCloned(GenericEntity instanceToBeCloned);

	StrategyOnCriterionMatch getStrategyOnCriterionMatch();

}
