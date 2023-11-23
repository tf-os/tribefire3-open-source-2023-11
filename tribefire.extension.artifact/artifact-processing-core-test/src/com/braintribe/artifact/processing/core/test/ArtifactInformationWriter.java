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
package com.braintribe.artifact.processing.core.test;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.braintribe.model.artifact.info.ArtifactInformation;
import com.braintribe.model.artifact.info.LocalRepositoryInformation;
import com.braintribe.model.artifact.info.PartInformation;
import com.braintribe.model.artifact.info.RemoteRepositoryInformation;

/**
 * simple dumper 
 * 
 * @author pit
 *
 */
public class ArtifactInformationWriter {

	private Writer writer;
	
	public ArtifactInformationWriter( Writer writer) {
		this.writer = writer; 		
	}
	

	/**
	 * d
	 * @param information
	 */
	public void dump(ArtifactInformation information) throws IOException{
		if (information == null) {			
			return;
		}
		
		writer.write( "retrieved information about : " + information.getGroupId() + ":" + information.getArtifactId() + "#" + information.getVersion() + "\n");
		LocalRepositoryInformation localInformation = information.getLocalInformation();
		if (localInformation != null) {
			writer.write( "local information:");
			writer.write( "\t@ " + localInformation.getUrl() + "\n");			
			for (PartInformation partInformation : localInformation.getPartInformation()) {
				String classifier = partInformation.getClassifier();
				if (classifier != null)
					writer.write( "\t\t" + classifier + ":" + partInformation.getType() + "\t" + partInformation.getUrl() + "\n");
				else
					writer.write( "\t\t" + partInformation.getType() + "\t" + partInformation.getUrl() + "\n");
			}
		}
		List<RemoteRepositoryInformation> remoteInformation = information.getRemoteInformation();
		writer.write( "remote information:");
		for (RemoteRepositoryInformation remoteArtifactInformation : remoteInformation) {
			writer.write(  "\t" + remoteArtifactInformation.getName() + " @ " + remoteArtifactInformation.getUrl() + "\n");
			for (PartInformation partInformation : remoteArtifactInformation.getPartInformation()) {
				String classifier = partInformation.getClassifier();
				if (classifier != null)
					writer.write(  "\t\t" + classifier + ":" + partInformation.getType() + "\t" + partInformation.getUrl() + "\n");
				else
					writer.write(  "\t\t" + partInformation.getType() + "\t" + partInformation.getUrl() + "\n");
			}
		}
	}
	
}
