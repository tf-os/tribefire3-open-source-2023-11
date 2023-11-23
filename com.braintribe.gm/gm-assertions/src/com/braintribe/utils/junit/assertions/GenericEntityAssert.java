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

import org.fest.assertions.Assertions;
import org.fest.assertions.GenericAssert;

import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.model.generic.GenericEntity;

/**
 * Assertions for <code>{@link GenericEntity}</code>s.
 * <p>
 * To create a new instance of this class invoke <code>{@link GmAssertions#assertThat(GenericEntity)}</code>.
 * </p>
 * 
 * 
 */
public class GenericEntityAssert extends GenericAssert<GenericEntityAssert, GenericEntity> {

	public GenericEntityAssert(final GenericEntity actual) {
		super(GenericEntityAssert.class, actual);
	}

	// /**
	// * Verifies that the actual <code>{@link GenericEntity}</code> has the specified <code>property</code> absent.
	// */
	// public void propertyIsAbsent(final String propertyName) {
	//
	// if (GmTools.isAbsent(this.actual, propertyName)) {
	// return;
	// } else {
	// failIfCustomMessageIsSet();
	// fail("The property '" + propertyName + "' of the " + this.actual + " is not absent");
	// }
	// }

	/**
	 * Verifies that the actual <code>{@link GenericEntity}</code> has a session attached.
	 */
	public void hasSessionAttached() {

		if (this.actual.session() != null) {
			return;
		} else {
			failIfCustomMessageIsSet();
			fail("Entity " + this.actual + " has no session attached!");
		}
	}

	/**
	 * TODO: add doc
	 */
	public GenericEntityPropertyAssert onIdProperty() {
		// TODO implement
		throw new NotImplementedException();
	}

	/**
	 * Returns a {@link GenericEntityPropertyAssert} that can be used to check the specified <code>property</code>.
	 */
	public GenericEntityPropertyAssert onProperty(final String property) {
		isNotNull();
		final GenericEntityPropertyAssert genericEntityPropertyAssert = new GenericEntityPropertyAssert(
				new GenericEntityProperty(this.actual, property));
		if (description() != null) {
			genericEntityPropertyAssert.as(description());
		}
		return genericEntityPropertyAssert;
	}

	/**
	 * TODO: add doc
	 */
	public GenericEntityPropertyValueAssert onIdPropertyValue() {
		// TODO implement
		throw new NotImplementedException();
	}

	/**
	 * Returns a {@link GenericEntityPropertyValueAssert} that can be used to check the value of the specified
	 * <code>property</code>. Note that it usually makes more sense to pass the property value directly to the
	 * respection {@link Assertions}' <code>assertThat</code> method.
	 */
	public GenericEntityPropertyValueAssert onPropertyValue(final String property) {
		isNotNull();
		final GenericEntityPropertyValueAssert genericEntityPropertyValueAssert = new GenericEntityPropertyValueAssert(
				new GenericEntityProperty(this.actual, property));
		if (description() != null) {
			genericEntityPropertyValueAssert.as(description());
		}
		return genericEntityPropertyValueAssert;
	}
}
