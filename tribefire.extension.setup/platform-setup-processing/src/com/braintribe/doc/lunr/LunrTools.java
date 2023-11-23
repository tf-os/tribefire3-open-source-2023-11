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
package com.braintribe.doc.lunr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface LunrTools {
	static <T> List<T> splice(List<T> list, int start, int deleteCount, T... insertItems) {
		int size = list.size();

		if (start < 0) {
			start = size + start;
			
			if (start < 0)
				start = 0;
		}
		else {
			if (start > size)
				start = size;
		}
		
		int remaining = size - start;

		if (deleteCount > remaining) {
			deleteCount = remaining;
		}
		else if (deleteCount < 0) {
			deleteCount = 0;
		}
		
		List<T> subList = list.subList(start, start + deleteCount);
		
		List<T> result = new ArrayList<>(subList);
		
		if (deleteCount != 0)
			subList.clear();
		
		if (insertItems.length > 0)
			subList.addAll(Arrays.asList(insertItems));
		
		return result;
	}
}
