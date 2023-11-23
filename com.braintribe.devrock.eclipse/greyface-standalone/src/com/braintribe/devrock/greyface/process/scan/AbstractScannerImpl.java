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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.name.NameParserException;
import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.devrock.greyface.GreyfaceStatus;
import com.braintribe.devrock.greyface.process.notification.ScanContext;
import com.braintribe.devrock.greyface.process.notification.ScanProcessListener;
import com.braintribe.devrock.greyface.process.notification.ScanProcessNotificator;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;

public abstract class AbstractScannerImpl implements ScanProcessNotificator, Scanner {
	protected List<ScanProcessListener> listeners = new ArrayList<ScanProcessListener>();
	protected DependencyCache dependencyCache = new DependencyCache();
	
	
	@Override
	public void addScanProcessListener(ScanProcessListener listener) {			
		listeners.add(listener);
	}
	
	@Override
	public void removeScanProcessListener(ScanProcessListener listener) {
		listeners.remove(listener);		
	}
	
	
	protected List<Dependency> extractDependenciesFromNameList( List<String> condensedNames) {
		List<Dependency> result = new ArrayList<Dependency>( condensedNames.size());
		for (String name : condensedNames) {
			Dependency dependency = null;
			try {
				if (name.contains("#")) {
					 dependency = NameParser.parseCondensedDependencyName( name);
				}
				else if (name.contains( ":")){
					String [] tokens = name.split(":");
					dependency = Dependency.T.create();
					dependency.setGroupId( tokens[0]);
					dependency.setArtifactId( tokens[1]);
					dependency.setType( tokens[2]);
					dependency.setVersionRange( VersionRangeProcessor.createFromString( tokens[3]));					
				}
				else {
					name = name.replace('\\', '/');
					String [] tokens = name.split("/");
					dependency = Dependency.T.create();
					dependency.setVersionRange( VersionRangeProcessor.createFromString( tokens[tokens.length-1]));
					dependency.setArtifactId( tokens[ tokens.length-2]);
					// 
					StringBuffer group = new StringBuffer();
					for (int i = 0; i < tokens.length-2; i++) {
						if (group.length() > 0)
							group.append('.');
						group.append( tokens[i]);
					}
					dependency.setGroupId( group.toString());					
				}
			} catch (NameParserException e) {				
				String msg = "cannot parse dependency from [" + name + "] as [" + e.getMessage() +"]";
				GreyfaceStatus status = new GreyfaceStatus( msg, e);
				GreyfacePlugin.getInstance().getLog().log(status);
				continue;			
			} catch (VersionProcessingException e) {
				String msg = "cannot parse version range from [" + name + "] as [" + e.getMessage() +"]";
				GreyfaceStatus status = new GreyfaceStatus( msg, e);
				GreyfacePlugin.getInstance().getLog().log(status);
				continue;	
			}
			catch (Exception e) {
				String msg =  "cannot parse tokens from [" + name + "] as [" + e.getMessage() +"]";
				GreyfaceStatus status = new GreyfaceStatus( msg, e);
				GreyfacePlugin.getInstance().getLog().log(status);
				continue;	
			}
			
			result.add(dependency);
		}		
		return result;
	}

	@Override
	public void scan(IProgressMonitor progressMonitor, ScanContext context) {	 
		RemoteRepositoryExpert expert = new RemoteRepositoryExpert();
		runScan(progressMonitor, context);
		expert.closeHttpAccessContext();
	}
	
	protected abstract void runScan( IProgressMonitor progressMonitor, ScanContext context);
}
