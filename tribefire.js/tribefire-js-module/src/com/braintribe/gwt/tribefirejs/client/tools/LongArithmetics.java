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
package com.braintribe.gwt.tribefirejs.client.tools;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.UnsafeNativeLong;

public class LongArithmetics 
{
	
	@UnsafeNativeLong
	public static native long toLong(JavaScriptObject b)/*-{
		if(typeof b == "number"){
			var d = @java.lang.Double::new(D)(b);
			b = d.@java.lang.Double::longValue()();
		}else{
			b = b.@java.lang.Long::longValue()();
		}
			return b;
	}-*/;
	
	public static Long add(Long a, JavaScriptObject b){
		return a + toLong(b);
	}
	
	public static Long sub(Long a, JavaScriptObject b){
		return a - toLong(b);
	}
	
	public static Long mul(Long a, JavaScriptObject b){
		return a * toLong(b);
	}
	
	public static Long div(Long a, JavaScriptObject b){
		return a / toLong(b);
	}
	
	public static Long pow(Long a, JavaScriptObject b){
		return (long) Math.pow(a, toLong(b));
	}
	
	public static Long sqrt(Long a){
		return (long) Math.sqrt(a);
	}
	
	public static Long lsh(Long a, int i){
		return (long) (a << i);
	}
	
	public static Long rsh(Long a, int i){
		return (long) (a >> i);
	}
	
}
