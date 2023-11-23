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
package com.braintribe.codec.dom.genericmodel;

/**
 * Specifies how missing/unknown types, properties, etc. are handled during decoding process.
 * 
 * @author michael.lafite
 * @deprecated use {@link com.braintribe.codec.marshaller.api.DecodingLenience} instead
 */
@Deprecated
public class DecodingLenience extends com.braintribe.codec.marshaller.api.DecodingLenience {

	/**
	 * Creates a new <code>DecodingLenience</code> instance where all lenience propeerties are disabled.
	 */
	public DecodingLenience() {
		// nothing to do
	}

	/**
	 * Creates a new <code>DecodingLenience</code> instance and {@link #setLenient(boolean) sets the lenience} as
	 * specified.
	 */
	public DecodingLenience(boolean lenient) {
		setLenient(lenient);
	}


}
