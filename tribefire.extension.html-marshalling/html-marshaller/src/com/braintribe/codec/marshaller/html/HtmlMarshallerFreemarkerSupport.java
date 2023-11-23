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
package com.braintribe.codec.marshaller.html;

public class HtmlMarshallerFreemarkerSupport {

	public String toString(Object object) {
		return object != null ? "\"" + object.toString() + "\"": "(null)";
	}
	
	public String getPackageName(String typeSignature) {
		int lastDotIndex = typeSignature.lastIndexOf('.');
		
		if (lastDotIndex == -1) {
			return null;
		}
		
		return typeSignature.substring(0, lastDotIndex);
	}
	
	public String getShortName(String typeSignature) {
		int lastDotIndex = typeSignature.lastIndexOf('.');
		
		if (lastDotIndex == -1)
			return typeSignature;
		
		return typeSignature.substring(lastDotIndex + 1);
	}
	
}
