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
package com.braintribe.common.lcd;

/**
 * Interface for classes with a configuration that can be changed at runtime. The interface is used to observe these configuration changes.
 *
 * @author michael.lafite
 */
public interface ConfigurationModifiable {

	/**
	 * Adds the passed <code>observer</code> to the set of observers.
	 *
	 * @param observer
	 *            the observer to add.
	 */
	void addConfigurationObserver(ConfigurationObserver observer);

	/**
	 * Removes the passed <code>observer</code> from the set of observers.
	 *
	 * @param observer
	 *            to observer to remove.
	 * @return whether or not the observer has been removed.
	 */
	boolean removeConfigurationObserver(ConfigurationObserver observer);

}
