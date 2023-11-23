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
package com.braintribe.common;

/**
 * This exists only to allow dumb copying of code for JVM (i.e. copying with this being used doesn't lead to compilation errors).
 * <p>
 * This cannot be implemented in GWT as it needs access to current thread's stacktrace.
 * 
 * @author peter.gazdik
 */
public class DeprecatedCode {

	public static void logWarn() {
		// NO OP
	}

	public static void printWarn() {
		// NO OP
	}

}
