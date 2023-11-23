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
package com.braintribe.model.smartqueryplan.queryfunctions;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;

import com.braintribe.model.query.Source;
import com.braintribe.model.query.functions.QueryFunction;

/**
 * Function which enables querying of delegate properties directly on the smart level. This is useful for manipulations
 * of key-properties (see {@link KeyPropertyAssignment}), where we need a delegate-property value for given smart
 * reference.
 * 
 * Simple-type properties are retrieved without any conversion being applied, but note that enums are converted based on
 * the mappings, is present, but generally speaking this function does not guarantee a correct behavior for enums.
 * 
 * @author peter.gazdik
 */
public interface ResolveDelegateProperty extends QueryFunction {

	EntityType<ResolveDelegateProperty> T = EntityTypes.T(ResolveDelegateProperty.class);

	Source getSource();
	void setSource(Source source);

	String getDelegateProperty();
	void setDelegateProperty(String delegateProperty);

}
