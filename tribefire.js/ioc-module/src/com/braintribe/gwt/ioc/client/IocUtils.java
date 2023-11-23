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
package com.braintribe.gwt.ioc.client;

import java.util.function.Supplier;



public class IocUtils {
	
	public static class IfNotNull<T> {
		private T value;
		
		public IfNotNull(T value) {
			this.value = value;
		}
		
		public T otherwise(T def) {
			if (value != null) return value;
			else return def;
		}

		public T otherwiseProvide(Supplier<? extends T> provider) throws RuntimeException {
			if (value != null) return value;
			else return provider.get();
		}
	}
	
	public static <T> IfNotNull<T> ifNotNull(T value) {
		return new IfNotNull<T>(value);
	}
	
	public static void main(String[] args) {
		ifNotNull(1).otherwise(10);
	}
}
