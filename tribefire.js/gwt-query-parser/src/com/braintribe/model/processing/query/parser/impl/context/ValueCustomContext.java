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

/**
 * Parent of all the different variations of {@link CustomContext}. In the
 * parser listeners, most of the "value" tokens are cast to this type to provide
 * generality.
 * 
 * @param <R>
 *            Type of object that is wrapped
 */
public class ValueCustomContext<R extends Object> extends CustomContext<R> {

	public ValueCustomContext(R returnValue) {
		super(returnValue);
	}

}
