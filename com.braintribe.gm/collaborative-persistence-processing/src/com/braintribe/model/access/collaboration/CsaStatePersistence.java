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
package com.braintribe.model.access.collaboration;

import com.braintribe.model.csa.CollaborativeSmoodConfiguration;

/**
 * Component that is responsible for storing and loading CSA configuration and (in one case) it's state (see {@link #readMarker()}).
 * 
 * @author peter.gazdik
 */
public interface CsaStatePersistence {

	/**
	 * Loads the CSA configuration from config.json AND ALSO stores that configuration as the original configuration, in case no original
	 * configuration exists yet.
	 */
	CollaborativeSmoodConfiguration readConfiguration();
	void writeConfiguration(CollaborativeSmoodConfiguration value);

	/**
	 * Reads the original CSA configuration - this is used when the whole persistence is being reset, and the original configuration was (most likely)
	 * created as a copy of regular configuration when it was first accessed.
	 */
	CollaborativeSmoodConfiguration readOriginalConfiguration();
	void overwriteOriginalConfiguration(CollaborativeSmoodConfiguration value);

	/** Loads a marker used by the DCSA; marks which operations from the shared storage were already processed. */
	String readMarker();
	void writeMarker(String marker);

}
