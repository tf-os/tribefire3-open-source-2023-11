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
package com.braintribe.testing.category;

/**
 * Signals that the test can only be executed in a special environment. Main purpose of this category is to indicate that a (default) CI can't run the
 * test.<br>
 * Sub types of this category further specify the requirements.
 *
 * @author michael.lafite
 *
 * @see SpecialExternalSystems
 * @see SpecialResources
 */
public interface SpecialEnvironment {
	// no methods required
}
