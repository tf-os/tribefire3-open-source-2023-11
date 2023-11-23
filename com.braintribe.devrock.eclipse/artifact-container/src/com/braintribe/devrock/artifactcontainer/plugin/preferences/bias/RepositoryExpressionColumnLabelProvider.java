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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.bias;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.bias.ArtifactBias;

public class RepositoryExpressionColumnLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		ArtifactBias pairing = (ArtifactBias) element;
		StringBuilder builder = new StringBuilder();
		
		for (String repo : pairing.getActiveRepositories()) {
			if (builder.length() > 0) {
				builder.append(',');
			}
			builder.append( repo);
		}
		for (String repo : pairing.getInactiveRepositories()) {
			if (builder.length() > 0) {
				builder.append(',');
			}
			builder.append( "!"+ repo);
		}
		
		return builder.toString();
	}

	@Override
	public String getToolTipText(Object element) {		
		return "display the repository information"; 
	}

}
