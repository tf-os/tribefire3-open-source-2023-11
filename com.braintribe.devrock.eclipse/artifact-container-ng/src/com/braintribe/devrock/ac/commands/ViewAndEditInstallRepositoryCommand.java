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
package com.braintribe.devrock.ac.commands;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.ac.container.repository.FileRepositoryCollectingScanner;
import com.braintribe.devrock.ac.container.repository.viewer.InstallRepositoryViewer;
import com.braintribe.devrock.ac.container.updater.WorkspaceUpdater;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;

/**
 * reads the install repository, transposes the contents and calls up a viewer
 * 
 * @author pit
 *
 */
public class ViewAndEditInstallRepositoryCommand extends AbstractHandler {
	

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {	
		
		Repository installRepository;
		long before = System.nanoTime();
		Maybe<RepositoryReflection> repositoryReflectionMaybe = DevrockPlugin.mcBridge().reflectRepositoryConfiguration();
		long after = System.nanoTime();
		double lastProcessingTime = (after - before) / 1E6;	

		if (repositoryReflectionMaybe.isSatisfied()) {
			RepositoryReflection reflection = repositoryReflectionMaybe.get();
			RepositoryConfiguration repositoryConfiguration = reflection.getRepositoryConfiguration();
			installRepository = repositoryConfiguration.getInstallRepository();
			if (installRepository == null) {
				ArtifactContainerStatus status = new ArtifactContainerStatus( "no install repository configured", IStatus.INFO);
				ArtifactContainerPlugin.instance().log(status);
				return null;
			}
			System.out.println("getting rcfg took: " + lastProcessingTime + " ms");
		}
		else {
			Reason reason = repositoryReflectionMaybe.whyUnsatisfied();
			ArtifactContainerStatus status = new ArtifactContainerStatus( "cannot access install repository:", reason);
			ArtifactContainerPlugin.instance().log(status);
			return null;
		}
		
		if (installRepository instanceof MavenFileSystemRepository == false) {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "install repository isn't supported: " + installRepository.getName(), IStatus.INFO);
			ArtifactContainerPlugin.instance().log(status);
			return null;
		}
		MavenFileSystemRepository mavenRepository = (MavenFileSystemRepository) installRepository;

		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());

		// enumerate install repository, turn into nodes 
		InstallRepositoryViewer viewer = new InstallRepositoryViewer(shell);
		viewer.setRepository( mavenRepository);
		
		BusyIndicator.showWhile(shell.getDisplay(), () -> {	
			Map<File, AnalysisArtifact> scanResult = FileRepositoryCollectingScanner.scanRepository(mavenRepository, null);		
			Collection<AnalysisArtifact> population = scanResult.values();				
			viewer.setInitialPopulation( population);
			viewer.primeViewer();
		});
		
		
		viewer.open();
		if (viewer.hasPurged()) {					
			WorkspaceUpdater wu = new WorkspaceUpdater();
			wu.runAsJob();
		}
		
		return null;
	}

	
}
