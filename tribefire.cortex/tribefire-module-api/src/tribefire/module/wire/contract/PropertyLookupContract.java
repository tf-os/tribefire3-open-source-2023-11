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
package tribefire.module.wire.contract;

import com.braintribe.wire.api.space.WireSpace;

/**
 * Marker interface for custom property-lookup contracts. Extending contracts (which also have to be interfaces, not classes) will automatically be
 * proxied to resolve properties (System, Environment Variables, platform-specific configuration).
 * 
 * <h3>Example:</h3>
 * 
 * <pre>
 * public interface MyPropertiesContract extends PropertyLookupContract {
 * 
 * 	String MAX_NUMBER_OF_FOOBAR_THREADS();
 * 
 * }
 * </pre>
 */
public interface PropertyLookupContract extends WireSpace {
	// Empty
}
