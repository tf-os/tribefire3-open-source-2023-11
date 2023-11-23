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

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assert;
import org.assertj.core.api.WritableAssertionInfo;
import org.assertj.core.error.MessageFormatter;
import org.assertj.core.internal.Failures;

import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.ReflectionTools;
import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.NullSafe;

/**
 * Provides assertions that are shared by custom assertions and custom assertion extensions. This is used as a work-around, since Java doesn't support
 * multiple inheritance for classes.
 *
 * @author michael.lafite
 */
public interface SharedAssert<S extends AbstractAssert<S, A>, A> extends Assert<S, A> {

	/**
	 * Expected name of the field that holds the <code>actual</code> (see <code>AbstractAssert.actual</code>).
	 *
	 * @see #actual(AbstractAssert)
	 */
	String ACTUAL_FIELD = "actual";

	/**
	 * Expected name of the field that holds the <code>info</code> (see <code>AbstractAssert.info</code>).
	 *
	 * @see #info(AbstractAssert)
	 */
	String INFO_FIELD = "info";

	/**
	 * Expected name of the field that holds the {@link Logger}.
	 *
	 * @see #logger(AbstractAssert)
	 */
	String LOGGER_FIELD = "logger";

	/**
	 * Delegates to method {@link AbstractAssert#withFailMessage(String, Object...)} on the specified <code>assertInstance</code> passing the given
	 * <code>errorMessage</code> and <code>arguments</code>. The purpose of this method is to be able to call the delegate method from this interface.
	 * Otherwise there is no reason to use this method. Instead one should directly call the delegate.
	 */
	// this is not a default method, because the method is protected in AbstractAssert (and we'd make it public be
	// defining a default)
	static <S extends AbstractAssert<S, A>, A> void failWithMessage(S assertInstance, String errorMessage, Object... arguments) {
		WritableAssertionInfo info = assertInstance.getWritableAssertionInfo();
		AssertionError assertionError = Failures.instance().failureIfErrorMessageIsOverridden(info);
		if (assertionError == null) {
			// error message was not overridden, build it.
			String description = MessageFormatter.instance().format(info.description(), info.representation(), "");
			assertionError = new AssertionError(description + String.format(errorMessage, arguments));
		}
		Failures.instance().removeAssertJRelatedElementsFromStackTraceIfNeeded(assertionError);
		throw assertionError;
	}

	/**
	 * Returns the {@link #actual(AbstractAssert) actual} value of this instance.
	 */
	default A actual() {
		return actual((S) this);
	}

	/**
	 * Returns the actual value for the given <code>assertInstance</code>. The expected field name is {@value #ACTUAL_FIELD}.
	 */
	static <S extends AbstractAssert<S, A>, A> A actual(S assertInstance) {
		return (A) ReflectionTools.getFieldValue(ACTUAL_FIELD, assertInstance);
	}

	/**
	 * Returns the {@link #info(AbstractAssert) info} of this instance.
	 */
	default WritableAssertionInfo info() {
		return info((S) this);
	}

	/**
	 * Returns the info for the given <code>assertInstance</code>. The expected field name is {@value #INFO_FIELD}.
	 */
	static <S extends AbstractAssert<S, A>, A> WritableAssertionInfo info(S assertInstance) {
		return (WritableAssertionInfo) ReflectionTools.getFieldValue(INFO_FIELD, assertInstance);
	}

	/**
	 * Returns the {@link #logger(AbstractAssert) logger} of this instance.
	 */
	default Logger logger() {
		return logger((S) this);
	}

	/**
	 * Returns the {@link Logger} of the given <code>assertInstance</code>. The expected field name is {@value #LOGGER_FIELD}.
	 *
	 * @throws IllegalArgumentException
	 *             if the given <code>assertInstance</code> doesn't have a {@link Logger}.
	 */
	static <S extends AbstractAssert<S, A>, A> Logger logger(S assertInstance) {
		Arguments.notNullWithName("assertInstance", assertInstance);
		Object loggerFieldValue;
		try {
			loggerFieldValue = ReflectionTools.getFieldValue(LOGGER_FIELD, assertInstance);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("No Logger found for " + assertInstance.getClass().getName()
					+ "! For assertion methods that require a Logger just create a field named " + LOGGER_FIELD + ".", e);
		}

		if (loggerFieldValue == null) {
			throw new IllegalArgumentException("No Logger found for " + assertInstance.getClass().getName() + "! The class has a field named "
					+ LOGGER_FIELD + ", but for the given instances it is not set.");
		}
		if (!(loggerFieldValue instanceof Logger)) {
			throw new IllegalArgumentException("No Logger found for " + assertInstance.getClass().getName() + "! The class has a field named "
					+ LOGGER_FIELD + ", but the type is " + loggerFieldValue.getClass().getName() + " instead of " + Logger.class.getName() + ".");
		}
		return (Logger) loggerFieldValue;
	}

	/**
	 * Returns a string representation of the passed <code>object</code>. This is e.g. used to distinguish between <code>null</code> and String
	 * <code>"null"</code> or between a number and the same number as string.
	 */
	default String toString(Object object) {
		return CommonTools.getStringRepresentation(object);
	}

	/**
	 * Performs an <code>equals</code> check similar to {@link #isEqualTo(Object)}, but with a more verbose message in case of an assertion error.
	 *
	 * @see #isEqualToWithVerboseErrorMessageAndLogging(Object)
	 */
	default S isEqualToWithVerboseErrorMessage(A expected) {
		String errorMessage = createVerboseErrorMessageIfObjectsAreNotEqual(actual(), expected);
		if (errorMessage != null) {
			failWithMessage((S) this, errorMessage);
		}
		return (S) this;
	}

	/**
	 * Performs an <code>equals</code> check similar to {@link #isEqualTo(Object)}, but with a more verbose message in case of an assertion error.
	 * This is similar to {@link #isEqualToWithVerboseErrorMessage(Object)}, but it also logs the info on {@link LogLevel#ERROR ERROR} level using the
	 * <code>Assert</code>'s {@link #logger(AbstractAssert) Logger}.
	 */
	default S isEqualToWithVerboseErrorMessageAndLogging(A expected) {
		String errorMessage = createVerboseErrorMessageIfObjectsAreNotEqual(actual(), expected);
		if (errorMessage != null) {
			logger((S) this).error(errorMessage);
			failWithMessage((S) this, errorMessage);
		}
		return (S) this;
	}

	/**
	 * Returns a verbose error message, if the given objects are not equal (or both <code>null</code>). For example, if the not even the types are the
	 * same, their fully qualified names will be included in the message.
	 *
	 * @return the verbose error message or <code>null</code>, if the objects are equal.
	 */
	static <A> String createVerboseErrorMessageIfObjectsAreNotEqual(A actual, A expected) {
		String result = null;
		if (!CommonTools.equalsOrBothNull(actual, expected)) {

			boolean typesAreEqualOrOneObjectIsNull = (actual == null) || (expected == null) || actual.getClass().equals(expected.getClass());
			if (typesAreEqualOrOneObjectIsNull) {
				// types are the same, so we don't need much type info
				String typeName = (actual != null) ? actual.getClass().getSimpleName() : expected.getClass().getSimpleName();
				result = "Equals assertion failed for " + typeName + "s!\nActual:\n" + CommonTools.getStringRepresentation(actual) + "\nExpected:\n"
						+ CommonTools.getStringRepresentation(expected);
			} else {
				// types are different -> include fully qualified name
				result = "Equals assertion failed!\nActual (type=" + NullSafe.className(actual) + "):\n" + CommonTools.getStringRepresentation(actual)
						+ "\nExpected (type=" + NullSafe.className(expected) + "):\n" + CommonTools.getStringRepresentation(expected);
			}
		}
		return result;
	}
}
