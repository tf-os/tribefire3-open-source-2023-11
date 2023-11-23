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
package com.braintribe.model.processing.meta.oracle;

import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.meta.GmEnumConstant;

/**
 * @author peter.gazdik
 */
public class BasicEnumTypeConstants implements EnumTypeConstants {

	private final BasicEnumTypeOracle enumTypeOracle;

	private Predicate<? super GmEnumConstant> filter;

	public BasicEnumTypeConstants(BasicEnumTypeOracle enumTypeOracle) {
		this.enumTypeOracle = enumTypeOracle;
	}

	@Override
	public EnumTypeConstants filter(Predicate<? super GmEnumConstant> filter) {
		this.filter = filter;
		return this;
	}

	@Override
	public Stream<Enum<?>> asEnums() {
		EnumType et = BasicModelOracle.typeReflection.getType(enumTypeOracle.flatEnumType.type.getTypeSignature());
		return asGmEnumConstants().map(gmConstant -> et.getEnumValue(gmConstant.getName()));
	}

	@Override
	public Stream<EnumConstantOracle> asEnumConstantOracles() {
		return asGmEnumConstants().map(gmConstant -> enumTypeOracle.getConstant(gmConstant));
	}

	@Override
	public Stream<GmEnumConstant> asGmEnumConstants() {
		Stream<GmEnumConstant> result = getDeclaredConstants();

		if (filter != null) {
			result = result.filter(filter);
		}

		return result;
	}

	private Stream<GmEnumConstant> getDeclaredConstants() {
		return nullSafe(enumTypeOracle.flatEnumType.type.getConstants()).stream();
	}

}
