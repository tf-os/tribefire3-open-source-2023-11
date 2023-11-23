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
 * Signals that the test requires (running) tribefire services accessible through default URLs. Currently these are
 * <code>http://localhost:8080/tribefire-services</code> and <code>https://localhost:8443/tribefire-services</code>.
 *
 * @author michael.lafite
 *
 * @deprecated This category was defined before the introduction of Platform Assets. Now one can use Platform Assets to
 *             create an integration test setup, which includes all the components (e.g. tribefire Services, tribefire
 *             Modeler, some cartridge ...) and also configuration (e.g. users, permissions, deployables) and test data,
 *             which the tests requires. URLs may depend on where the test is run, but these can easily be provided by
 *             the CI (e.g. via system properties). Therefore this category is no longer needed.
 */
@Deprecated
public interface TribefireServices extends SpecialEnvironment {
	// no methods required
}
