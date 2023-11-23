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
package com.braintribe.gwt.utils.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.NativeEvent;

/**
 * Expert for adding listeners for shortcuts globally.
 * @author michel.docouto
 *
 */
public class RootKeyNavExpert {
	
	private static List<RootKeyNavListener> listeners;
	
	protected RootKeyNavExpert() {
		//Not instantiable
	}
	
	public static void addRootKeyNavListener(RootKeyNavListener listener) {
		if (listener == null)
			return;
		
		if (listeners == null)
			listeners = new ArrayList<>();
		
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public static void removeRootKeyNavListener(RootKeyNavListener listener) {
		if (listener == null && listeners == null)
			return;
		
		if (listeners.contains(listener))
			listeners.remove(listener);
		
		if (listeners.isEmpty())
			listeners = null;
	}
	
	public static List<RootKeyNavListener> getListeners() {
		return listeners;
	}
	
	public interface RootKeyNavListener {
		public void onRootKeyPress(NativeEvent evt);
	}

}
