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
package com.braintribe.devrock.preferences.contributer;

/**
 * the exposed registry, which is used by collaborating plugins to register and de-register their contribution 
 * 
 * @author Pit
 *
 */
public interface PreferencesContributerRegistry {
	/**
	 * register a declaration
	 * @param contributer - the {@link PreferencesContributionDeclaration} to register
	 */
	void addContributionDeclaration( PreferencesContributionDeclaration contributer);
	
	/**
	 * de-register a declaration 
	 * @param contributer - the {@link PreferencesContributionDeclaration} to deregister
	 */
	void removeContributionDeclaration( PreferencesContributionDeclaration contributer);
	
	/**
	 * register an implementation 
	 * @param contributer - the {@link PreferencesContributerImplementation} to register
	 */
	void addContributerImplementation( PreferencesContributerImplementation contributer);
	
	/**
	 * deregister an implement 
	 * @param contributer - the {@link PreferencesContributerImplementation} to deregister
	 */
	void removeContributerImplementation( PreferencesContributerImplementation contributer);
}
