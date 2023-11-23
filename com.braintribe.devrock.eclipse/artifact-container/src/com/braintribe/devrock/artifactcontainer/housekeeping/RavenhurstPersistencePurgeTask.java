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
package com.braintribe.devrock.artifactcontainer.housekeeping;

import java.util.List;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.registry.RavenhurstPersistenceHelper;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

/**
 * a {@link HouseKeepingTask} that cleans the .interrogation files from the updateinfo directories  
 * 
 * @author pit
 *
 */
public class RavenhurstPersistencePurgeTask implements HouseKeepingTask {

	@Override
	public void execute() {
	
		// do not purge if debug's active 
		if (ArtifactContainerPlugin.isDebugActive()) {
			return;
		}
		// get the days
		int days = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getRavenhurstPreferences().getPrunePeriod();
		// currently configured contract (a full one, even if not really required)
		ClasspathResolverContract contract = MalaclypseWirings.fullClasspathResolverContract().contract();
		try {
			List<RavenhurstBundle> bundles = contract.ravenhurstScope().getRavenhurstBundles();
			// purge for each bundle 
			for (RavenhurstBundle bundle : bundles) {
				List<String> msgs = RavenhurstPersistenceHelper.purge( contract.settingsReader(), bundle, days);
				// process errors 
				if (!msgs.isEmpty()) {
					for (String msg : msgs) {
						ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
						ArtifactContainerPlugin.getInstance().log(status);
					}
				}
				else {
					ArtifactContainerStatus status = new ArtifactContainerStatus( "sucessfully pruned interrogation data of repository [" + bundle.getRepositoryId() + "@" + bundle.getRepositoryUrl() + "] of [" + bundle.getProfileId() + "]", IStatus.INFO);
					ArtifactContainerPlugin.getInstance().log(status);
				}
			}
		} catch (Exception e) {
			String msg = "cannot run ravenhurst house keeping task";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			ArtifactContainerPlugin.getInstance().log(status);
		} 
	}
	
}
