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
package com.braintribe.utils.junit.assertions;

import static java.lang.Math.abs;

import org.fest.assertions.Formatting;
import org.fest.assertions.LongAssert;

/**
 * An extended version of {@link LongAssert}.
 *
 * @see #isEqualTo(long, long)
 *
 *
 */
public class ExtendedLongAssert extends LongAssert {

	protected ExtendedLongAssert(final long actual) {
		super(actual);
	}

	protected ExtendedLongAssert(final Long actual) {
		super(actual);
	}

	/**
	 * Asserts the actual value is equal to an expected value, allowing a specified delta.
	 */
	public LongAssert isEqualTo(final long expected, final long deltaValue) {
		if (abs(expected - this.actual) <= deltaValue) {
			return this;
		}
		failIfCustomMessageIsSet();
		throw failure(unexpectedNotEqual(this.actual, expected) + Formatting.format(" using delta:<%s>", Double.valueOf(deltaValue)));
	}

	static String unexpectedNotEqual(final Object actual, final Object expected) {
		return Formatting.format("expected:<%s> but was:<%s>", expected, actual);
	}

}
