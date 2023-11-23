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
package tribefire.platform.impl.deployment;

/**
 * <p>
 * The possible activation states of a cartridge instance.
 * 
 */
public enum ActivationState {

	/**
	 * No activation occurred
	 */
	inactive,

	/**
	 * No activation occurred for the instance as it was unauthorized
	 */
	unauthorized,

	/**
	 * The instance is activated
	 */
	activated,

	/**
	 * The instance is currently being activated
	 */
	activating,

	/**
	 * The instance is deactivated
	 */
	deactivated,

	/**
	 * The instance is currently being deactivated
	 */
	deactivating;

}
