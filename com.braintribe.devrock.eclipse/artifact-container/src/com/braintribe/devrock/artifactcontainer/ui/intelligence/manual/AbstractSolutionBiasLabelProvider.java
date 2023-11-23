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
package com.braintribe.devrock.artifactcontainer.ui.intelligence.manual;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflectionSupport;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.bias.ArtifactBias;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.artifact.Solution;

public abstract class AbstractSolutionBiasLabelProvider extends ColumnLabelProvider {
	private RepositoryReflectionSupport reflection;
	
	@Configurable @Required
	public void setReflection(RepositoryReflectionSupport reflection) {
		this.reflection = reflection;
	}
	
	protected ArtifactBias getBias( Object element) {
		Solution solution = (Solution) element;
		ArtifactBias artifactBias = reflection.getArtifactBias(solution);
		return artifactBias;
	}
}
