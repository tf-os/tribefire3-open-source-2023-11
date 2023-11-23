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
package com.braintribe.tribefire.jinni.support.request.completion;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newTreeMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;

/**
 * @author peter.gazdik
 */
/* package */ class EnumsRegistry {

	public final Map<String, GmEnumType> shortIdentifierToType = newTreeMap();

	private final Map<GmEnumType, String> typeToShortIdentifier = newMap();
	private final Map<String, Integer> shortNameToCount = newMap();

	public String acquireShortIdentifier(GmEnumType gmType) {
		return typeToShortIdentifier.computeIfAbsent(gmType, this::newShortIdentifier);
	}

	private String newShortIdentifier(GmEnumType gmType) {
		String result = resolveShortIdentifier(gmType);

		shortIdentifierToType.put(result, gmType);

		return result;
	}

	private String resolveShortIdentifier(GmEnumType gmType) {
		String shortName = gmType.<EnumType> reflectionType().getShortName();
		Integer c = shortNameToCount.compute(shortName, (name, count) -> (count == null ? 1 : count + 1));
		return c == 1 ? shortName : shortName + c;
	}

	public static List<String> listConstantsNames(GmEnumType gmEnumType) {
		return gmEnumType.getConstants().stream() //
				.map(GmEnumConstant::getName) //
				.sorted() //
				.collect(Collectors.toList());
	}

}
