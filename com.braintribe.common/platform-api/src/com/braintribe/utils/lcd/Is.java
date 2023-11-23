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
package com.braintribe.utils.lcd;

import java.util.Collection;
import java.util.Map;

import com.braintribe.common.lcd.AssertionException;

/**
 * Helper class used to conveniently assert that some condition is met. Examples:
 *
 * <pre>
 * Is.True(myBoolean);
 * Is.blank(myString);
 * Is.Null(myObject);
 * </pre>
 * <p>
 * Class and method names are intentionally very short so that method calls can be used inline without having too much impact on readability:
 *
 * <pre>
 * addElements(Is.empty(myCollection));
 * addEntries(Is.empty(myMap));
 * </pre>
 *
 * All methods throw an {@link AssertionException}, if the check fails.
 *
 * @author michael.lafite
 *
 * @see Not
 */
// if you add a method to this class, please consider adding a similar method to com.braintribe.utils.lcd.Not.
public class Is {

	/**
	 * Asserts the passed <code>value</code> is <code>true</code>.
	 */
	public static Boolean TRUE(Boolean value) {
		if (!value) {
			throw new AssertionException("The passed boolean must be true, but is " + value + "!");
		}
		return value;
	}

	/**
	 * Asserts the passed <code>value</code> is <code>true</code>.
	 */
	public static Boolean True(Boolean value) {
		if (!value) {
			throw new AssertionException("The passed boolean must be true, but is " + value + "!");
		}
		return value;
	}

	/**
	 * Asserts the passed <code>value</code> is <code>true</code>.
	 */
	public static Boolean true_(Boolean value) {
		if (!value) {
			throw new AssertionException("The passed boolean must be true, but is " + value + "!");
		}
		return value;
	}

	/**
	 * Asserts the passed <code>value</code> is <code>false</code>.
	 */
	public static Boolean FALSE(Boolean value) {
		if (value) {
			throw new AssertionException("The passed boolean must be false, but is " + value + "!");
		}
		return value;
	}

	/**
	 * Asserts the passed <code>value</code> is <code>false</code>.
	 */
	public static Boolean False(Boolean value) {
		if (value) {
			throw new AssertionException("The passed boolean must be false, but is " + value + "!");
		}
		return value;
	}

	/**
	 * Asserts the passed <code>value</code> is <code>false</code>.
	 */
	public static Boolean false_(Boolean value) {
		if (value) {
			throw new AssertionException("The passed boolean must be false, but is " + value + "!");
		}
		return value;
	}

	/**
	 * Asserts the passed <code>object</code> is <code>null</code>.
	 */
	public static <T> T NULL(T object) {
		if (object != null) {
			throw new AssertionException("The passed object must be null, but is " + object + "!");
		}
		return object;
	}

	/**
	 * Asserts the passed <code>object</code> is <code>null</code>.
	 */
	public static <T> T Null(T object) {
		if (object != null) {
			throw new AssertionException("The passed object must be null, but is " + object + "!");
		}
		return object;
	}

	/**
	 * Asserts the passed <code>object</code> is <code>null</code>.
	 */
	public static <T> T null_(T object) {
		if (object != null) {
			throw new AssertionException("The passed object must be null, but is " + object + "!");
		}
		return object;
	}

	/**
	 * Asserts the passed <code>string</code> is {@link CommonTools#isEmpty(String) empty}.
	 */
	public String empty(String string) {
		if (!CommonTools.isEmpty(string)) {
			throw new AssertionException("The passed string must be empty, but is '" + string + "'!");
		}
		return string;
	}

	/**
	 * Asserts the passed <code>string</code> is {@link CommonTools#isBlank(String) blank}.
	 */
	public String blank(String string) {
		if (!CommonTools.isBlank(string)) {
			throw new AssertionException("The passed string must be blank, but is '" + string + "'!");
		}
		return string;
	}

	/**
	 * Asserts the passed <code>collection</code> is {@link CommonTools#isEmpty(Collection) empty}.
	 */
	public <T extends Collection<?>> T empty(T collection) {
		if (!CommonTools.isEmpty(collection)) {
			throw new AssertionException("The passed collection must be empty, but has size " + collection.size() + ".");
		}
		return collection;
	}

	/**
	 * Asserts the passed <code>map</code> is {@link CommonTools#isEmpty(Map) empty}.
	 */
	public <T extends Map<?, ?>> T empty(T map) {
		if (!CommonTools.isEmpty(map)) {
			throw new AssertionException("The passed map must be empty, but has size " + map.size() + ".");
		}
		return map;
	}
}
