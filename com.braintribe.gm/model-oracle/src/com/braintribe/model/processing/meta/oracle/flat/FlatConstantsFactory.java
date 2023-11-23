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
package com.braintribe.model.processing.meta.oracle.flat;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.Map;

import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.meta.override.GmEnumConstantOverride;
import com.braintribe.model.meta.override.GmEnumTypeOverride;

/**
 * Factory for flat local constants.
 * 
 * @author peter.gazdik
 */
public class FlatConstantsFactory {

	private final FlatEnumType flatEnumType;
	private final Map<String, FlatEnumConstant> flatConstants;

	public static Map<String, FlatEnumConstant> buildFor(FlatEnumType flatEnumType) {
		return new FlatConstantsFactory(flatEnumType).build();
	}

	FlatConstantsFactory(FlatEnumType flatEnumType) {
		this.flatEnumType = flatEnumType;
		this.flatConstants = newMap();
	}

	private Map<String, FlatEnumConstant> build() {
		for (GmEnumTypeInfo entityTypeInfo : flatEnumType.infos)
			visit(entityTypeInfo);

		return flatConstants;
	}

	private void visit(GmEnumTypeInfo gmEnumTypeInfo) {
		if (gmEnumTypeInfo instanceof GmEnumTypeOverride) {
			GmEnumTypeOverride gmEnumTypeOverride = (GmEnumTypeOverride) gmEnumTypeInfo;
			for (GmEnumConstantOverride gmEnumConstantOverride : nullSafe(gmEnumTypeOverride.getConstantOverrides()))
				visitEnumConstant(gmEnumConstantOverride);

		} else {
			GmEnumType gmEnumType = (GmEnumType) gmEnumTypeInfo;
			for (GmEnumConstantInfo gmEnumConstantInfo : nullSafe(gmEnumType.getConstants()))
				visitEnumConstant(gmEnumConstantInfo);
		}
	}

	private void visitEnumConstant(GmEnumConstantInfo gmEnumConstantInfo) {
		FlatEnumConstant flatConstant = acquireFlatConstant(gmEnumConstantInfo);
		flatConstant.infos.add(gmEnumConstantInfo);
	}

	private FlatEnumConstant acquireFlatConstant(GmEnumConstantInfo info) {
		GmEnumConstant gmEnumConstant = info.relatedConstant();
		String constantName = gmEnumConstant.getName();

		return flatConstants.computeIfAbsent(constantName, n -> new FlatEnumConstant(gmEnumConstant));
	}

}
