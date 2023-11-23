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
package com.braintribe.common.db;

import com.braintribe.processing.test.db.derby.DerbyServerControl;

/**
 * @author peter.gazdik
 */
public class DbTestSupport {

	private static DerbyServerControl derbyServerControl;

	public static void startDerby() {
		if (derbyServerControl != null) {
			throw new IllegalStateException("Cannot start Derby as it is already running.");
		}

		derbyServerControl = new DerbyServerControl();
		try {
			derbyServerControl.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void shutdownDerby() {
		if (derbyServerControl != null) {
			try {
				derbyServerControl.stop();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			derbyServerControl = null;
		}
	}

}
