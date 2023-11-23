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
package com.braintribe.model.processing.query.parser.impl.context;

import com.braintribe.model.processing.query.parser.impl.listener.GmqlBasicParserListener;

/**
 * A wrapper object that holds partial or full results during parsing.
 * 
 * During query parsing, maintaining a parse tree would really be expensive.
 * Thus the solution, is to keep only the needed information from the parsing,
 * i.e. the partial/full results of the rules.
 * 
 * This construct is used extensively in {@link GmqlBasicParserListener}.
 * 
 * @param <R>
 *            Type of object that is wrapped
 */
public class CustomContext<R> {

	private final R returnValue;

	public CustomContext(R returnValue) {
		this.returnValue = returnValue;
	}

	public <C extends CustomContext<?>> C cast() {
		return (C) ((CustomContext<?>) this);
	}

	public R getReturnValue() {
		return returnValue;
	}

}
