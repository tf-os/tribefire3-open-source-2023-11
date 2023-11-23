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

import static com.braintribe.utils.junit.assertions.BtAssertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

/**
 *
 */
public class ClassAssertTests {

	@Test
	public void demo() {
		assertThat(List.class).isAssignableFrom(ArrayList.class).isAssignableTo(Collection.class);
	}

	// ###################################
	// ## . . . . ACTUAL TESTS . . . . .##
	// ###################################

	@Test(expected = AssertionError.class)
	public void testIsAssignableFrom() {
		assertThat(List.class).isAssignableFrom(Integer.class);
	}

	@Test(expected = AssertionError.class)
	public void testIsAssignableTo() {
		assertThat(List.class).isAssignableTo(Integer.class);
	}

}
