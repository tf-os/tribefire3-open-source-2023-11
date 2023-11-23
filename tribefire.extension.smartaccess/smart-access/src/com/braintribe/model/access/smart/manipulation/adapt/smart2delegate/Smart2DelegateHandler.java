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

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;

/**
 * 
 * @author peter.gazdik
 */
public interface Smart2DelegateHandler<T extends PropertyAssignment> {

	void loadAssignment(T assignment);

	void convertToDelegate(ChangeValueManipulation smartManipulation) throws ModelAccessException;

	void convertToDelegate(AddManipulation smartManipulation) throws ModelAccessException;

	void convertToDelegate(RemoveManipulation smartManipulation) throws ModelAccessException;

	void convertToDelegate(ClearCollectionManipulation smartManipulation) throws ModelAccessException;

}
