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
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;

import jsinterop.annotations.JsType;

/**
 * Cloning parameter that controls what to do in case of a "match" on a property. That is important, for collection
 * elements a match always means we skip the element, i.e. the cloned collection will not contain it. Only for
 * properties we have the following three options.
 * 
 * @see Matcher
 * @see CloningContext#isTraversionContextMatching()
 * @see TraversingCriterion
 * @see GenericModelType#clone(Object, Matcher, StrategyOnCriterionMatch)
 * @see GenericModelType#clone(CloningContext, Object, StrategyOnCriterionMatch)
 */
@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
public enum StrategyOnCriterionMatch {

	/**
	 * Specifies that property of the cloned entity will be set as absent, i.e. an {@link AbsenceInformation} value
	 * descriptor will be set. The actual {@linkplain AbsenceInformation} instance will be taken from
	 * {@link CloningContext#createAbsenceInformation(GenericModelType, GenericEntity, Property)}.
	 */
	partialize,

	/**
	 * Specifies that property value of the original entity will be taken "as is" to the cloned entity, i.e. no cloning
	 * of the property value itself will take place.
	 */
	reference,

	/** Specifies that the matching property will be completely ignored by the cloning algorithm. */
	skip

}
