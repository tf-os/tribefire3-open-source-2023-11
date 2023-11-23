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
package com.braintribe.artifact.processing.core.test.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.processing.ResolvedArtifact;

public class ResolvedArtifactWriter extends WriterCommons{
	private static Logger log = Logger.getLogger(ResolvedArtifactWriter.class);
	
	private Writer writer;
	private Set<ResolvedArtifact> dumped = new HashSet<>();

	public ResolvedArtifactWriter(Writer writer) {
		this.writer = writer;					
	}

	public void dump( Collection<ResolvedArtifact> artifacts){
		artifacts.stream().forEach( ra -> {
			try {
				dump( ra);
			} catch (IOException e) {
				log.error( "cannot dump [" + getKey(ra) + "]");
			}
		});
	}
	
	public void dump( ResolvedArtifact artifact) throws IOException{
		dump( artifact, 0, true);
	}
	
	private void dump( ResolvedArtifact artifact, int index, boolean full) throws IOException {
		if (full) {
			writer.write( tab( index) + getKey( artifact) + "\t<-" + dump( artifact.getRepositoryOrigins()) + "\n");			
			artifact.getDependencies().stream()			
				.forEach( d -> {
					boolean explicit = false;
					if (!dumped.contains( d)) {
						dumped.add(d);
						explicit = true;
					}
					try {
						dump( d, index+1, explicit);
					} catch (IOException e) {
						log.error( "cannot dump [" + getKey(d) + "]");					
					}							
				});
		}
		else {
			writer.write( tab( index) + "[" + getKey( artifact) + "]\n");
		}
		
	}
	
	private String getKey( ResolvedArtifact artifact) {
		return artifact.getGroupId() + ":" + artifact.getArtifactId() + "#" + artifact.getVersion();		
	}
	
	
}
