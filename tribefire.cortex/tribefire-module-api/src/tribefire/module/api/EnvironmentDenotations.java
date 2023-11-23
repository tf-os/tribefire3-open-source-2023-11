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
package tribefire.module.api;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;

/**
 * Interface for EnvironmentDenotationRegistry.
 * 
 * @author peter.gazdik
 */
public interface EnvironmentDenotations {

	void register(String bindId, GenericEntity denotationInstance);

	/**
	 * Returns a denotation instance for given bindId as registered via {@link #register}, or <tt>null</tt> if no entry is found.
	 */
	<T extends GenericEntity> T lookup(String bindId);

	/**
	 * Returns a sub-set of {@link #entries()}, where the key (bindId) matches given pattern using {@link String#matches(String)} check.
	 */
	<T extends GenericEntity> Map<String, T> find(String pattern);

	/**
	 * Returns a map where keys are bindIds and values are corresponding denotation instances, as registered via {@link #register} method.
	 */
	Map<String, GenericEntity> entries();

}
