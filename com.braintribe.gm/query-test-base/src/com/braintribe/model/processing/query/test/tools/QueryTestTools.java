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
package com.braintribe.model.processing.query.test.tools;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.braintribe.model.record.ListRecord;

public class QueryTestTools {

	public static Map<Object, Integer> processResult(List<?> results) {
		if (results.isEmpty())
			return Collections.emptyMap();

		boolean areRecords = results.get(0) instanceof ListRecord;

		if (areRecords)
			return fill(newMap(), copyListRecords(results));
		else
			return fill(newMap(), results);
	}

	private static List<?> copyListRecords(List<?> records) {
		List<List<?>> result = newList();

		for (Object o : records) {
			List<Object> values = ((ListRecord) o).getValues();
			result.add(values);
		}

		return result;
	}

	public static List<?> asList(Object o, Object[] os) {
		List<Object> values = newList();
		values.add(o);
		values.addAll(Arrays.asList(os));

		return values;
	}

	private static Map<Object, Integer> fill(Map<?, Integer> _map, List<?> results) {
		Map<Object, Integer> map = (Map<Object, Integer>) _map;

		for (Object o : results) {
			int val = 1;
			if (map.containsKey(o))
				val = map.get(o) + 1;

			map.put(o, val);
		}

		return map;
	}

}
