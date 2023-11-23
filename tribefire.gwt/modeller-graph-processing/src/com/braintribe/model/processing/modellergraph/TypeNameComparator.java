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
package com.braintribe.model.processing.modellergraph;

import java.util.Comparator;

import com.braintribe.model.meta.GmType;

public class TypeNameComparator implements Comparator<GmType> {
	@Override
	public int compare(GmType o1, GmType o2) {
		String t1 = o1.getTypeSignature();
		String t2 = o2.getTypeSignature();
		
		int packageDelimiter1 = t1.lastIndexOf('.');
		int packageDelimiter2 = t2.lastIndexOf('.');
		
		String package1 = t1.substring(0, packageDelimiter1);
		String package2 = t2.substring(0, packageDelimiter2);
		
		String simpleName1 = t1.substring(packageDelimiter1 + 1);
		String simpleName2 = t2.substring(packageDelimiter2 + 1);
		
		int res = simpleName1.compareToIgnoreCase(simpleName2);
		
		if (res != 0) 
			return res;
		else
			return package1.compareToIgnoreCase(package2);
	}
}
