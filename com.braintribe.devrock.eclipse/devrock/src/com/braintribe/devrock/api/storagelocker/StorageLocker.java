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
package com.braintribe.devrock.api.storagelocker;

import java.util.Optional;

import com.braintribe.devrock.eclipse.model.storage.StorageLockerPayload;
import com.braintribe.devrock.plugin.DevrockPlugin;

/**
 * interface to the internal 'settings storage system' as implemented by the {@link DevrockPlugin} 
 * @author pit
 *
 */
public interface StorageLocker {

	
	/**
	 * @param <T> - the type of the slot's value
	 * @param slot - the name of the slot
	 * @return - an {@link Optional} of T, whatever that is  
	 */
	<T> Optional<T> getValue(String slot);
	
	
	/**
	 * @param <T> - the type of the slot's value
	 * @param slot - the name of the slot
	 * @param value - the default value to be returned if no such slot exists 
	 * @return - the T, whatever that is 
	 */
	<T> T getValue( String slot, T value);

	/**
	 * @param <T> - the type of the slot's value
	 * @param slot - the name of the slot
	 * @param value - an instance of T whatever that is  
	 */
	<T> void setValue(String slot, T value);

	
	/**
	 * @return - the full data currently stored
	 */
	StorageLockerPayload content();
}