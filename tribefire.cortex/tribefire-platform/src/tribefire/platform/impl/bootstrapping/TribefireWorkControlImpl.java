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

import java.util.HashSet;
import java.util.Set;

import com.braintribe.utils.LongIdGenerator;

public class TribefireWorkControlImpl implements TribefireWorkControl {
	
	public static TribefireWorkControlImpl instance = new TribefireWorkControlImpl();

	protected Set<String> monitors = new HashSet<String>();
	protected volatile boolean permissionGranted = false;

	@Override
	public void waitForWorkPermission() throws InterruptedException {

		if (this.permissionGranted) {
			return;
		}

		String monitorObject = new String(Thread.currentThread().getName())+LongIdGenerator.provideLongId();

		synchronized(monitorObject) {
			
			synchronized(this) {
				if (this.permissionGranted) {
					return;
				}
				monitors.add(monitorObject);
			}

			monitorObject.wait();
		}
	}

	public void giveWorkPermission() {
		this.permissionGranted = true;

		Set<String> monitorsToNotify = null; 

		synchronized(this) {
			monitorsToNotify = monitors;
			monitors = new HashSet<String>();
		}

		for (String monitorObject : monitorsToNotify) {
			synchronized (monitorObject) {				
				monitorObject.notify();
			}
		}

	}

}
