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
package com.braintribe.model.generic.value;


import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface UnsatisfiedValue extends ValueDescriptor {
	
	EntityType<UnsatisfiedValue> T = EntityTypes.T(UnsatisfiedValue.class);
	
	String why = "why";
	String value = "value";
	String hasValue = "hasValue";
	
	Reason getWhy();
	void setWhy(Reason whyUnsatisfied);
	
	Object getValue();
	void setValue(Object value);
	
	boolean getHasValue();
	void setHasValue(boolean hasValue);

	
	static UnsatisfiedValue create(Reason why) {
		UnsatisfiedValue unsatisfiedVd = UnsatisfiedValue.T.create();
		unsatisfiedVd.setWhy(why);
		return unsatisfiedVd;
	}
	
	static UnsatisfiedValue create(Reason why, Object value) {
		UnsatisfiedValue unsatisfiedVd = UnsatisfiedValue.T.create();
		unsatisfiedVd.setWhy(why);
		unsatisfiedVd.setValue(value);
		unsatisfiedVd.setHasValue(true);
		return unsatisfiedVd;
	}
}
