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

import org.fest.assertions.Assert;
import org.fest.assertions.Assertions;
import org.fest.assertions.GenericAssert;

import com.braintribe.model.generic.GenericEntity;

/**
 * {@link Assert} used to check a <code>{@link GenericEntity}</code> property (specified via
 * {@link GenericEntityProperty}). Note that this <code>Assert</code> does not extend {@link GenericAssert} and thus
 * provides only a reduced set of assertion checks. If one wants to to check against the actual property value, one can
 * use {@link GenericEntityPropertyValueAssert} instead (see {@link #onValue()}).
 * 
 * @author michael.lafite
 */
public class GenericEntityPropertyAssert extends Assert {

	private final GenericEntityProperty genericEntityProperty;

	public GenericEntityPropertyAssert(final GenericEntityProperty genericEntityProperty) {
		this.genericEntityProperty = genericEntityProperty;
	}

	/**
	 * Works like {@link GenericAssert#as(String)}.
	 */
	public GenericEntityPropertyAssert as(final String description) {
		description(description);
		return this;
	}

	/**
	 * Verifies that the property is absent.
	 */
	public GenericEntityPropertyAssert isAbsent() {
		if (!this.genericEntityProperty.isAbsent()) {
			failIfCustomMessageIsSet();
			fail("Property '" + this.genericEntityProperty.getName() + "' of entity "
					+ this.genericEntityProperty.getEntity() + " is not absent!");
		}
		return this;
	}

	/**
	 * Verifies that the property is not absent.
	 */
	public GenericEntityPropertyAssert isNotAbsent() {
		if (this.genericEntityProperty.isAbsent()) {
			failIfCustomMessageIsSet();
			fail("Property '" + this.genericEntityProperty.getName() + "' of entity "
					+ this.genericEntityProperty.getEntity() + " is absent!");
		}
		return this;
	}

	/**
	 * Returns a {@link GenericEntityPropertyValueAssert} that can be used to check the property value. Note that it
	 * usually makes more sense to pass the property value directly to the respection {@link Assertions}'
	 * <code>assertThat</code> method.
	 */
	public GenericEntityPropertyValueAssert onValue() {
		final GenericEntityPropertyValueAssert genericEntityPropertyValueAssert = new GenericEntityPropertyValueAssert(
				this.genericEntityProperty);
		if (description() != null) {
			genericEntityPropertyValueAssert.as(description());
		}
		return genericEntityPropertyValueAssert;
	}
}
