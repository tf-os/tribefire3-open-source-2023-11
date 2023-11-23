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
package tribefire.extension.xml.schemed.xsd.api.analyzer;

import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.SchemaEntity;

public class SchemaLookupResult <T extends SchemaEntity>{
	private T found;
	private Namespace targetNamespace;
	
	public SchemaLookupResult(Namespace targetNamespace, T found) {
		this.targetNamespace = targetNamespace;
		this.found = found;		
	}

	public T getFound() {
		return found;
	}

	public void setFound(T found) {
		this.found = found;
	}

	public Namespace getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(Namespace targetNamespace) {
		this.targetNamespace = targetNamespace;
	}
	
	
	

}
