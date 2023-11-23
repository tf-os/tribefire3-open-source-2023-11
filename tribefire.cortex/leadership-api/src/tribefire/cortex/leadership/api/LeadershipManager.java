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
package tribefire.cortex.leadership.api;

public interface LeadershipManager {

	/**
	 * Adds a {@link LeadershipListener} for given "domainId".
	 * <p>
	 * Given "listenerId" is used for removal of the listener.
	 * <p>
	 * * IMPORTANT: One can only {@link #removeLeadershipListener(String, LeadershipListener) remove a listener} after the
	 * {@link #addLeadershipListener} method has returned. Otherwise there is no guarantee on the behavior.
	 */
	void addLeadershipListener(String domainId, LeadershipListener listener);

	/** Removes a {@link LeadershipListener} for given "domainId" using the "listenerId" used when adding the listener. */
	void removeLeadershipListener(String domainId, LeadershipListener listener);

	/** Helpful description used for logging. */
	String description();

}

