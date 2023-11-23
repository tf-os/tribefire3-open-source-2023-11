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
package com.braintribe.model.processing.xmi.converter.tagdefinition;

import com.braintribe.model.processing.xmi.converter.experts.DeclaringModelBinding;

public class TagDefinition {

	private String xmiId;
	private String name;

	private DeclaringModelBinding artifactBinding;

	public String getXmiId() {
		return xmiId;
	}

	public void setXmiId(String xmiId) {
		this.xmiId = xmiId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DeclaringModelBinding getArtifactBinding() {
		return artifactBinding;
	}

	public void setArtifactBinding(DeclaringModelBinding artifactBinding) {
		this.artifactBinding = artifactBinding;
	}

}
