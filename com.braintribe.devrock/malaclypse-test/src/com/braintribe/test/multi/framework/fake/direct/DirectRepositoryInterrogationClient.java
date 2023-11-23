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
package com.braintribe.test.multi.framework.fake.direct;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClient;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationException;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.model.ravenhurst.Part;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstRequest;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstResponse;

public class DirectRepositoryInterrogationClient extends AbstractDirectClientBase implements RepositoryInterrogationClient {

	public DirectRepositoryInterrogationClient(String key, boolean expansive, String... artifacts) {	
		super( key, expansive, artifacts);
	}
			
	@Override
	public RavenhurstResponse interrogate(RavenhurstRequest request) throws RepositoryInterrogationException {			
		RavenhurstResponse response = RavenhurstResponse.T.create();
		StringBuilder builder = new StringBuilder();
		for (String value : artifacts) {
			if (builder.length() > 0)
				builder.append("\n");
			builder.append( value);			

			int gp = value.indexOf(':');
			int vs = value.indexOf( '#');
			Artifact artifact = Artifact.T.create();
			artifact.setGroupId( value.substring(0, gp));
			artifact.setArtifactId( value.substring(gp+1, vs));
			artifact.setVersion( value.substring(vs+1));			 		
			response.getTouchedArtifacts().add( artifact);
		}
		
		
		response.setPayload(builder.toString());
		
		return response;
	}
	

	@Override
	public RavenhurstResponse extractIndex(RavenhurstRequest request) throws RepositoryInterrogationException {
		RavenhurstResponse response = RavenhurstResponse.T.create();
		return response;
	}

	@Override
	public List<Part> extractPartList(RavenhurstBundle bundle, Artifact artifact) throws RepositoryInterrogationException {
		url = bundle.getRepositoryUrl();
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}		
		if (expansive)
			return generatePartList(bundle, artifact, expansive_extensions);
		else
			return generatePartList(bundle, artifact, restricted_extensions);
	}
	
	
	@Override
	public List<String> extractVersionList(RavenhurstBundle bundle, Identification artifact) throws RepositoryInterrogationException {	
		String match = artifact.getGroupId()+":"+artifact.getArtifactId();
		List<String> versions = new ArrayList<String>();
		for (String suspect : artifacts) {
			if (suspect.startsWith(match)) {
				versions.add( suspect.substring( suspect.indexOf('#')+1));
			}
		}
		return versions;
	}


	private List<Part> generatePartList( RavenhurstBundle bundle, Artifact artifact, String [] exts) {
		List<Part> parts = new ArrayList<Part>();		
		String pathPrefix = url + "/" + artifact.getGroupId().replace('.', '/') + "/" + artifact.getArtifactId() + "/" + artifact.getVersion();
		String namePrefix = artifact.getArtifactId() + "-" + artifact.getVersion();
		
		String [] values = exts;
		if (expansive) {
			values = expansive_extensions;
		}
		for (String extension : values) {
			Part part = Part.T.create();
			part.setSource( pathPrefix + "/" + namePrefix + extension);
			part.setName( namePrefix + extension);
			parts.add(part);
			// hashes 
			part = Part.T.create();
			part.setSource( pathPrefix + "/" + namePrefix + extension + ".md5");
			part.setName( namePrefix + extension + ".md5");
			parts.add(part);
			part = Part.T.create();
			part.setSource( pathPrefix + "/" + namePrefix + extension + ".sha1");
			part.setName( namePrefix + extension + ".sha1");
			parts.add(part);		
		}
		// metadata 
		Part part = Part.T.create();
		part.setSource( pathPrefix + "/" + "maven-metadata.xml");
		part.setName( "maven-metadata.xml");
		parts.add(part);
		
		part = Part.T.create();
		part.setSource( pathPrefix + "/" + "maven-metadata.xml.md5");
		part.setName( "maven-metadata.xml.md5");
		parts.add(part);
		
		part = Part.T.create();
		part.setSource( pathPrefix + "/" + "maven-metadata.xml.sha1");
		part.setName( "maven-metadata.xml.sha1");
		parts.add(part);
		
		
		return parts;
	}
}
