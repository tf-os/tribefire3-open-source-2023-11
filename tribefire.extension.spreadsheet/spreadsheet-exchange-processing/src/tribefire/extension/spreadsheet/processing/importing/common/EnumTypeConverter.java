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
package tribefire.extension.spreadsheet.processing.importing.common;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.model.generic.reflection.EnumType;

import tribefire.extension.spreadsheet.model.reason.ConversionFailed;

/**
 * @author peter.gazdik
 */
public class EnumTypeConverter implements TypeConverter<String, Enum<?>> {
	private EnumType enumType;
	
	public EnumTypeConverter(EnumType enumType) {
		super();
		this.enumType = enumType;
	}

	@Override
	public Maybe<Enum<?>> convert(String source) {
		Enum<?> enumValue = enumType.findEnumValue(source);
		
		if (enumValue == null)
			return Reasons.build(ConversionFailed.T) //
					.text("EnumType [" + enumType.getTypeSignature() + "] has no enum constant [" + source + "]") //
					.toMaybe();

		return Maybe.complete(enumValue);
	}

	@Override
	public Class<String> getFromClass() {
		return String.class;
	}

	@Override
	public Class<Enum<?>> getToClass() {
		return (Class<Enum<?>>) enumType.getJavaType();
	}

}
