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

/**
 * This is a little helper class that provides an alternative to suppressing certain warnings related to unused code via {@link SuppressWarnings}
 * annotation. Why do you need this? Well, you don't. You can always suppress via annotation, if needed. Some developers don't like that approach
 * though.<br>
 * In the following example, the created <code>URL</code> is not used, but this is not a bug. The code just makes sure that the constructor doesn't
 * throw an exception, which means that the <code>urlString</code> is valid.
 *
 * <pre>
 * try {
 * 	new URL(urlString);
 * } catch (MalformedURLException e) {
 * 	throw new IllegalArgumentException("The specified string '" + urlString + "' is not a valid URL!");
 * }
 * </pre>
 *
 * To be able to suppress the warning (just for that line) one has to declare a variable:
 *
 * <pre>
 * &#64;SuppressWarnings("unused")
 * URL url = new URL(urlString);
 * </pre>
 *
 * In addition, it probably makes sense to add a comment explaining this:
 *
 * <pre>
 * &#47;&#47; create URL to validate URL string
 * &#64;SuppressWarnings("unused")
 * URL url = new URL(urlString);
 * </pre>
 *
 * This class provides an alternative, where one just writes:
 *
 * <pre>
 * Unused.butConstructorMayThrowException(urlString);
 * </pre>
 *
 * With this approach the code briefly explains why something is unused. The Javadoc provides further information. Therefore this is more descriptive
 * than just <code>&#64;SuppressWarnings</code> and usually descriptive enough so that one doesn't have to further explain in a comment.<br>
 * On the other hand most developers, obviously, are not used to this pattern. Therefore the annotation might still be better choice. But this is an
 * alternative.
 *
 * @author michael.lafite
 */
@SuppressWarnings("unused")
public class Unused {

	/**
	 * Indicates that a method parameter is intended to be used by overriding methods. Example:
	 *
	 * <pre>
	 * public RequestResponse processRequest(Request request, RequestProcessingContext context) {
	 * 	Unused.butOverridingMethodsRequireParameter(context);
	 *  ...
	 * }
	 * </pre>
	 */
	public static void butOverridingMethodsRequireParameter(Object unusedParameter) {
		// empty
	}

	/**
	 * Indicates that a constructor may throw an exception and that the intention is actually not to instantiate a new object, but just to check, if
	 * the exception is thrown or not (and to handle the exception). Example:
	 *
	 * <pre>
	 * try {
	 * 	Unused.butConstructorMayThrowException(urlString);
	 * } catch (MalformedURLException e) {
	 * 	throw new IllegalArgumentException("The specified string '" + urlAsString + "' is not a valid URL!");
	 * }
	 * </pre>
	 */
	public static void butConstructorMayThrowException(Object unusedObject) {
		// empty
	}

	/**
	 * Indicates that a variable is (intentionally) unused, but just temporarily. This is an alternative to commenting out the respective code. It can
	 * e.g. be useful when commenting out would create an unused <code>import</code>.
	 */
	public static void butJustTemporarily(Object unusedVariable) {
		// empty
	}
}
