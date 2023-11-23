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
package com.braintribe.devrock.api.ui.editors;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.devrock.api.ui.listeners.ModificationNotificationBroadcaster;
import com.braintribe.devrock.api.ui.listeners.ModificationNotificationListener;

public class AbstractEditor implements ModificationNotificationBroadcaster{
	protected Set<ModificationNotificationListener> listeners = new HashSet<ModificationNotificationListener>();

	@Override
	public void addListener(ModificationNotificationListener listener) {
		listeners.add( listener);
	}

	@Override
	public void removeListener(ModificationNotificationListener listener) {
		listeners.remove(listener);
	}

	protected void broadcast( String value) {
		for (ModificationNotificationListener listener : listeners) {
			listener.acknowledgeChange( this, value);
		}		
	}
	

}
