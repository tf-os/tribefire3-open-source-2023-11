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
package com.braintribe.wire.impl.compile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.braintribe.cfg.Required;

public class RequiredMethodCollector {
	public List<Method> scanRequiredMethods(Class<?> instanceClass) {
		List<Method> methods = null;
		for (Method method: instanceClass.getMethods()) {
			if (method.isAnnotationPresent(Required.class)) {
				if (methods == null)
					methods = new ArrayList<>();
			}
		}
		
		return methods != null? methods: Collections.emptyList();
	}
}
