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
package com.braintribe.testing.junit.assertions.assertj.core.api;

import org.assertj.core.api.AbstractCharSequenceAssert;

import com.braintribe.logging.Logger;

/**
 * Provides custom {@link CharSequence} assertions.
 *
 * @author michael.lafite
 */
public class ExtendedCharSequenceAssert extends AbstractCharSequenceAssert<ExtendedCharSequenceAssert, CharSequence>
		implements SharedCharSequenceAssert<ExtendedCharSequenceAssert, CharSequence> {

	@SuppressWarnings("unused") // may be used by SharedAssert methods via reflection
	private static final Logger logger = Logger.getLogger(ExtendedCharSequenceAssert.class);

	public ExtendedCharSequenceAssert(CharSequence actual) {
		super(actual, ExtendedCharSequenceAssert.class);
	}
}
