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
package com.braintribe.devrock.greyface.process.scan;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.resolving.ResolvingException;
import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.devrock.greyface.GreyfaceStatus;
import com.braintribe.devrock.greyface.process.notification.ScanContext;
import com.braintribe.devrock.greyface.process.notification.ScanProcessListener;
import com.braintribe.devrock.greyface.process.retrieval.CompoundDependencyResolver;
import com.braintribe.devrock.greyface.scope.GreyfaceScope;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;

public class SynchronousScannerImpl extends AbstractScannerImpl {
	
	@Override
	protected void runScan(IProgressMonitor monitor, ScanContext context) {				
		List<RepositorySetting> sources = context.getSourceRepositories();
		List<Dependency> dependencies = extractDependenciesFromNameList(context.getCondensedNames());			
		SynchronousScanExpert scanExpert = new SynchronousScanExpert();
		
		CompoundDependencyResolver resolver = new CompoundDependencyResolver();
		resolver.setSources(sources);
		//scanExpert.setCompoundDependencyResolver(resolver);
		scanExpert.setSources(sources);
		scanExpert.setContext(context);		

		scanExpert.setProgressMonitor(monitor);
		scanExpert.setDependencyCache(dependencyCache);
		GreyfaceScope.getScope().resetScope( resolver);
		
		
		for (ScanProcessListener listener : listeners) {
			listener.acknowledgeStartScan();
			scanExpert.addScanProcessListener(listener);
		}
	
		for (Dependency dependency : dependencies) {
			if (monitor.isCanceled())
				break;
			// 
			try {
				scanExpert.scanDependency( dependency, 0, 0, null);
			} catch (ResolvingException e) {
				String msg = "cannot scan [" + NameParser.buildName(dependency) + "] as [" + e.getMessage() + "]";
				GreyfaceStatus status = new GreyfaceStatus( msg, e);
				GreyfacePlugin.getInstance().getLog().log(status);
			}
		}				
		
		for (ScanProcessListener listener : listeners) {
			listener.acknowledgeStopScan();
		}							
	}
}
