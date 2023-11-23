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
package tribefire.platform.impl.bootstrapping;

/**
 * This is useful when threads in a cartridge need to know when they can actually start
 * their work. Sometimes, it is necessary to wait for the server startup before they should
 * start their work (e.g., because they need the tribefire-services up and running).
 * 
 * By invoking {@link #waitForWorkPermission()}, the caller is automatically blocked
 * until the worker should start its work.
 */
public interface TribefireWorkControl {

	public final static TribefireWorkControl instance = TribefireWorkControlImpl.instance;
	
	/**
	 * Blocks until the tribefire-services are available. When the worker should start its
	 * work, this method will return. If the services are already running, this method
	 * will return immediately.
	 * 
	 * @throws InterruptedException Thrown if there was an InterruptedException during waiting
	 * 	for the services.
	 */
	void waitForWorkPermission() throws InterruptedException;
	
}
