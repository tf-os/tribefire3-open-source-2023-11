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

import java.util.function.Function;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.InternalError;

public class BasicTypeConverter<S, T> implements TypeConverter<S, T> {
	private Function<S,T> converterFunction;
	private Class<S> fromClass;
	private Class<T> toClass;
	
	public BasicTypeConverter(Class<S> fromClass, Class<T> toClass, Function<S, T> converterFunction) {
		super();
		this.fromClass = fromClass;
		this.toClass = toClass;
		this.converterFunction = converterFunction;
	}

	@Override
	public Maybe<T> convert(S source) {
		try {
			return Maybe.complete(converterFunction.apply(source));
		}
		catch (Exception e) {
			return InternalError.from(e).asMaybe();
		}
	}
	
	@Override
	public Class<S> getFromClass() {
		return fromClass;
	}
	
	@Override
	public Class<T> getToClass() {
		return toClass;
	}
}
