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
package com.braintribe.model.generic.manipulation.accessor;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.value.EntityReference;

public abstract class OwnerAccessor<T extends Owner> {
	
	protected GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	public abstract <T1> T1 get(T owner);
	
	/**
	 * @return if the replace change the identification a new reference is returned otherwise null
	 */
	public abstract EntityReference replace(T owner, Object newValue);
	public abstract void markAsAbsent(T owner, AbsenceInformation absenceInformation, String propertyName);

}
