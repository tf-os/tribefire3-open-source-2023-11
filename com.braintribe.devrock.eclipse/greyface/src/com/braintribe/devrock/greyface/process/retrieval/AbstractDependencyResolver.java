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
package com.braintribe.devrock.greyface.process.retrieval;

import java.util.Set;

import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.ResolvingException;
import com.braintribe.build.artifact.retrieval.multi.resolving.listener.DependencyResolverNotificationListener;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.version.Version;

public abstract class AbstractDependencyResolver implements DependencyResolver {

	@Override
	public void addListener(DependencyResolverNotificationListener arg0) {		
	}

	@Override
	public void removeListener(DependencyResolverNotificationListener arg0) {		
	}

	@Override
	public Set<Solution> resolveMatchingDependency(String arg0, Dependency arg1) throws ResolvingException {
		return null;
	}

	@Override
	public Part resolvePom(String contextId, Identification arg1, Version arg2) throws ResolvingException {
		return null;
	}

	@Override
	public Part resolvePomPart(String contextId, Part part) throws ResolvingException {	
		return null;
	}

		

	
}
