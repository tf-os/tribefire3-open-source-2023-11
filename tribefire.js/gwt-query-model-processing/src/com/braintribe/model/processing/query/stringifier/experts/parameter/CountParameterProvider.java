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
package com.braintribe.model.processing.query.stringifier.experts.parameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.braintribe.model.processing.query.api.stringifier.experts.paramter.MultipleParameterProvider;
import com.braintribe.model.query.functions.aggregate.Count;

public class CountParameterProvider implements MultipleParameterProvider<Count> {
	@Override
	public Collection<?> provideParameters(Count function) {
		List<Object> parameters = new ArrayList<Object>();

		parameters.add(function.getOperand());
		if (function.getDistinct()) {
			parameters.add(true);
		}

		return parameters;
	}
}
