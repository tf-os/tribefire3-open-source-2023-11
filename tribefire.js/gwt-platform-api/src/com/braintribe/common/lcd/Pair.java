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
package com.braintribe.common.lcd;

import com.braintribe.utils.lcd.CommonTools;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * Simple helper class that holds two associated objects.
 *
 * @author michael.lafite
 *
 * @param <F>
 *            type of the first of the pair's two objects
 * @param <S>
 *            type of the second of the pair's two objects
 */
@JsType(namespace = "$tf.util")
public class Pair<F, S> {
	public final F first;
	public final S second;

	public Pair(F first, final S second) {
		this.first = first;
		this.second = second;
	}

	@JsIgnore
	public Pair(final Pair<F, S> otherPair) {
		this(otherPair.first(), otherPair.second());
	}
	
	public static <F,S> Pair<F,S> of(F first, S second) {
		return new Pair<F,S>(first, second);
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof Pair) {
			final Pair<?, ?> otherPair = (Pair<?, ?>) other;
			return CommonTools.equalsOrBothNull(getFirst(), otherPair.getFirst()) && CommonTools.equalsOrBothNull(getSecond(), otherPair.getSecond());

		}
		return false;
	}

	@Override
	public int hashCode() {
		return CommonTools.getHashCode(getFirst()) + CommonTools.getHashCode(getSecond());
	}

	public F getFirst() {
		return this.first;
	}

	public S getSecond() {
		return this.second;
	}

	@JsIgnore
	public F first() {
		return this.first;
	}

	@JsIgnore
	public S second() {
		return this.second;
	}

	@Override
	public String toString() {
		return "(" + first() + "," + second() + ")";
	}

}
