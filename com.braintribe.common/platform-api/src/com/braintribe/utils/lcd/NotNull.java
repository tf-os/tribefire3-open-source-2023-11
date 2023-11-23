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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Provides methods that ensure the returned value is not <code>null</code>. <b>Attention:</b>: The methods use Java assertions for their
 * <code>null</code> checks, which means the checks won't be executed in many development environments and almost never in production
 * environments.<br>
 * The initial purpose of this class was to be used as a helper when working with null annotations and advanced null analysis (which is e.g. supported
 * by Eclipse) in combination with libraries that don't use these null annotations. In that case one could pass results of library methods to
 * <code>NotNull.get</code> to tell the IDE that the result cannot be <code>null</code>. Example:
 *
 * <pre>
 * String path = NotNull.get(new File("relative/path/to/file").getAbsolutePath());
 * // The IDE knows that path cannot be null
 * ...
 * </pre>
 *
 * The class was intended to be used primarily for null analysis and also only for methods where it's clear they won't return <code>null</code>, e.g.
 * <code>File.getAbsolutePath</code>. Therefore it also made sense to just Java ssertions for the checks. However, Since we decided not to use null
 * analysis for now (mainly due to these problems with 3rd party libraries), this class is deprecated now (see deprecation comment for more info),
 * also because some developers apparently used it in places where <code>null</code> values can occur.
 *
 * @author michael.lafite
 *
 * @deprecated Either use {@link Not#Null(Object)}, which throws exceptions instead of relying on assertions, or completely remove the
 *             <code>NotNull</code> call, if the passed value cannot be <code>null</code> anyway.
 */
@Deprecated
public final class NotNull {

	private NotNull() {
		// no instantiation required
	}

	public static <T> T get(final T object) {
		assert (object != null) : "the passed object is null!";
		return object;
	}

	@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
	public static <T> String toString(final T object) {
		final String result = get(object).toString();
		assert (result != null) : "The string representation of the passed object is null! "
				+ CommonTools.getParametersString("object type", object.getClass().getName());
		return result;
	}
}
