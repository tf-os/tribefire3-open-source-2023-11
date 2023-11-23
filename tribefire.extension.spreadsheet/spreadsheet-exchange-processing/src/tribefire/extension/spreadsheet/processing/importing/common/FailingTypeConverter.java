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

import com.braintribe.common.potential.Potential;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;

public class FailingTypeConverter<S, T> implements TypeConverter<S, T> {
	private Reason reason;
	private Class<S> fromClass;
	private Class<T> toClass;
	
	public FailingTypeConverter(Class<S> fromClass, Class<T> toClass, Reason reason) {
		super();
		this.fromClass = fromClass;
		this.toClass = toClass;
		this.reason = reason;
	}

	@Override
	public Maybe<T> convert(S source) {
		return reason.asMaybe();
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
