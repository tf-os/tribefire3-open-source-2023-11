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
package com.braintribe.devrock.preferences.contributer.implementation;

import java.util.Set;

import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.devrock.preferences.contributer.PreferencesContributerImplementation;
import com.braintribe.devrock.preferences.contributer.PreferencesContributerRegistry;
import com.braintribe.devrock.preferences.contributer.PreferencesContributionDeclaration;

public class PreferencesContributerRegistryImpl implements PreferencesContributerRegistry {
	
	private Set<PreferencesContributionDeclaration> declarations = CodingSet.createHashSetBased(new PreferencesContributorDeclarationWrapperCodec());
	private Set<PreferencesContributerImplementation> implementations = CodingSet.createHashSetBased(new PreferencesContributorImplementationWrapperCodec());
	

	@Override
	public void addContributionDeclaration(PreferencesContributionDeclaration contributer) {
		declarations.add(contributer);

	}

	@Override
	public void removeContributionDeclaration(PreferencesContributionDeclaration contributer) {
		declarations.remove(contributer);

	}

	@Override
	public void addContributerImplementation(PreferencesContributerImplementation contributer) {
		implementations.add(contributer);

	}

	@Override
	public void removeContributerImplementation(PreferencesContributerImplementation contributer) {
		implementations.remove(contributer);
	}
	
	public Set<PreferencesContributionDeclaration> getDeclarations() {
		return declarations;
	}
	
	public Set<PreferencesContributerImplementation> getImplementations() {
		return implementations;
	}

}
