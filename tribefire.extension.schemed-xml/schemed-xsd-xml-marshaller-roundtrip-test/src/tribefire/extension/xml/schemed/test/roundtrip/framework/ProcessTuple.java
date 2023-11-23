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
package tribefire.extension.xml.schemed.test.roundtrip.framework;

import java.io.File;

public class ProcessTuple {
	
	private File skeletonModelAsJar;
	private File mappingModelAsXml;

	public File getSkeletonModelAsJar() {
		return skeletonModelAsJar;
	}

	public void setSkeletonModelAsJar(File skeletonModelAsJar) {
		this.skeletonModelAsJar = skeletonModelAsJar;
	}

	public File getMappingModelAsXml() {
		return mappingModelAsXml;
	}

	public void setMappingModelAsXml(File mappingModelAsXml) {
		this.mappingModelAsXml = mappingModelAsXml;
	}

	public ProcessTuple() {
	}
	
	public ProcessTuple( File jar, File xml) {
		this.skeletonModelAsJar = jar;
		this.mappingModelAsXml = xml;
	}
}
