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
package com.braintribe.model.processing.xmi.converter.experts;

import com.braintribe.model.meta.GmMetaModel;

/**
 * helper class to build the artifact binding information
 * 
 * supports gwt module name appended via an $ to the model name
 * 
 * @author pit
 *
 */
public class DeclaringModelBinding {
	private String name;
	private GmMetaModel model;
	
	public DeclaringModelBinding( GmMetaModel model) {
		this.model = model;
		this.name = model.getName();		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public GmMetaModel getModel() {
		return model;
	}
	public void setModel(GmMetaModel model) {
		this.model = model;
	}

	public String toString() {
		return name;
	}
	
	public boolean matchesArtifact(String modelName) {		
		if (model.getName().equals( modelName))
			return true;
		return false;
	}
	
	
	public boolean matchesArtifact(GmMetaModel suspect) {
		if (model == suspect)
			return true;
		if (model.getName().equals( name))
			return true;
		return false;
	}

	public boolean matchesArtifact(DeclaringModelBinding artifact) {
		if (name.equals( artifact.getName()))
			return true;
		return false;
	}
	

}
