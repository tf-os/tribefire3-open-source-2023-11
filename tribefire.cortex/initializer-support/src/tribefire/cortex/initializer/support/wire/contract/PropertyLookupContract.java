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
package tribefire.cortex.initializer.support.wire.contract;

import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.impl.properties.PropertyLookups;

/**
 * Marker interface for contracts dedicated to resolving properties / environment variables.
 * <p>
 * These contracts are implemented automatically.
 * <p>
 * Each method is assumed to be the name of a property and the implementation returns the corresponding value, resolved via {@link TribefireRuntime}.
 * <p>
 * Example:
 * 
 * <pre>
 * public interface XyzEnvContract extends PropertyLookupContract {
 * 	String EXTERNAL_SERVICE_URL();
 * 
 * 	&#64;Default("4")
 * 	Integer NUMBER_OF_THREADS_FOR_SOME_TASK();
 * 
 * 	&#64;Decrypt
 * 	String SOME_DB_PASSWORD();
 * }
 * </pre>
 * 
 * @see PropertyLookups
 * 
 * @author Dirk Scheffler
 */
public interface PropertyLookupContract extends WireSpace {
	// marker interface
}
