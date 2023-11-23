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
package com.braintribe.cc.lcd;

import java.util.Iterator;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

/**
 * an iterator to work with the coding set<br/>
 * <br/>
 * if you need any concrete examples, have a look that the test cases in com.braintribe:PlatformApiTest#1.0, package com.braintribe.coding<br/>
 * <br/>
 *
 * @param <WE>
 *            - wrapper element
 * @param <E>
 *            - element
 *
 * @author pit
 */
public class CodingIterator<WE, E> implements Iterator<E> {

	private final Codec<E, WE> codec;
	private final Iterator<WE> delegate;

	public CodingIterator(final Iterator<WE> delegate, final Codec<E, WE> codec) {
		this.delegate = delegate;
		this.codec = codec;
	}

	@Override
	public boolean hasNext() {
		return this.delegate.hasNext();
	}

	@Override
	public E next() {
		try {
			return this.codec.decode(this.delegate.next());
		} catch (final CodecException e) {
			final String msg = "cannot retrieve next object as " + e;
			throw new IllegalStateException(msg, e);
		}
	}

	@Override
	public void remove() {
		this.delegate.remove();

	}

}
