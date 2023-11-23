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
package com.braintribe.model.processing.query.stringifier.experts.basic;

import com.braintribe.model.processing.query.api.stringifier.QueryStringifierRuntimeException;
import com.braintribe.model.processing.query.api.stringifier.experts.Stringifier;
import com.braintribe.model.processing.query.api.stringifier.experts.StringifierContext;

public class NumberStringifier<N extends Number> implements Stringifier<N, StringifierContext> {
	private String appendix = null;

	public NumberStringifier() {
		// Nothing
	}

	public NumberStringifier(String appendix) {
		this();
		this.appendix = appendix;
	}

	public void setAppendix(String appendix) {
		this.appendix = appendix;
	}

	@Override
	public String stringify(N number, StringifierContext operand) throws QueryStringifierRuntimeException {
		StringBuilder queryString = new StringBuilder();
		
		queryString.append(number.toString());
		if (this.appendix != null) {
			queryString.append(this.appendix);
		}
		
		return queryString.toString();
	}
}
