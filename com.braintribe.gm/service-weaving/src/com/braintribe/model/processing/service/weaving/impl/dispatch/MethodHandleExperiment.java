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
package com.braintribe.model.processing.service.weaving.impl.dispatch;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class MethodHandleExperiment {

	public static void main(String[] args) {
		try {
			Method method = MethodHandleExperiment.class.getDeclaredMethod("foo", String.class);
			MethodHandle handle = MethodHandles.lookup().unreflect(method);
			
			Object object = "Hallo Welt!";
			
			MethodHandleExperiment instance = new MethodHandleExperiment();
			
			handle.invoke(instance, object);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void foo(String s) {
		System.out.println(s);
	}
}
