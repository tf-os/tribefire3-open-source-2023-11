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
package tribefire.extension.modelling.processing.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;

import com.braintribe.model.artifact.processing.ResolvedArtifact;
import com.braintribe.model.artifact.processing.ResolvedPlatformAsset;
import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeInitializers;
import com.braintribe.model.csa.SmoodInitializer;
import com.braintribe.model.processing.core.commons.EntityHashingComparator;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;

/**
 * <h3>Naming</h3>
 * 
 * This is one of the modelling helper classes, whose idea is to provide an API
 * for the most common tasks associated with modelling-processing.
 *
 */
public interface ModellingTools {
	
	EntityHashingComparator<ResolvedPlatformAsset> aiComparator = EntityHashingComparator //
			.build(ResolvedPlatformAsset.T) //
			.addField(ResolvedPlatformAsset.groupId)
			.addField(ResolvedPlatformAsset.artifactId) //
			.addField(ResolvedPlatformAsset.version) //
			.done();
	
	EntityHashingComparator<ResolvedArtifact> artifactComparator = EntityHashingComparator //
			.build(ResolvedArtifact.T) //
			.addField(ResolvedArtifact.groupId)
			.addField(ResolvedArtifact.artifactId) //
			.addField(ResolvedArtifact.version) //
			.done();
	
	
	static boolean trunkExists(String accessId, PersistenceGmSession session) {
		GetCollaborativeInitializers getStages = GetCollaborativeInitializers.T.create();
		getStages.setServiceId(accessId);
		
		List<SmoodInitializer> stages = getStages.eval(session).get();
		SmoodInitializer trunk = stages.stream().filter(s -> s.getName().equals("trunk")).findFirst().orElse(null);

		return trunk != null;
	}
	
	
	static void transferResource(Resource resource, File to) {
		try(InputStream is = resource.openStream(); OutputStream os = new FileOutputStream(to)) {
			IOTools.transferBytes(is, os, IOTools.BUFFER_SUPPLIER_64K);
			
		} catch (IOException e) {
			throw new UncheckedIOException(
					"Error while transferring resource " + resource + " to " + to.getAbsolutePath(), e);
		}
	}

}
