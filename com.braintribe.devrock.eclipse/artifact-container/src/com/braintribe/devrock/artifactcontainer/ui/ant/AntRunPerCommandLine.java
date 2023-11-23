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
package com.braintribe.devrock.artifactcontainer.ui.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.process.ProcessException;
import com.braintribe.build.process.ProcessExecution;
import com.braintribe.build.process.ProcessResults;
import com.braintribe.build.process.listener.ProcessNotificationListener;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.malaclypse.cfg.preferences.ac.AntRunnerPreferences;

public class AntRunPerCommandLine {
	
	protected IProject project = null;
	protected Artifact id = null;	
	protected Map<String, String> arguments = new HashMap<String, String>();	
	protected Map<String, String> properties = null;
	protected Map<String,String> combinedMap = new HashMap<String,String>();
	protected String buildFileLocation = null;
	protected ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
	
	public AntRunPerCommandLine( Artifact artifact, Map<String, String> properties, String location) {
		buildFileLocation = location;		
		this.properties = properties;
		String artifactAsString = NameParser.buildName(artifact);
		arguments.put("artifact", artifactAsString);
	}
	
	public AntRunPerCommandLine( String artifactAsString, Map<String, String> properties, String location) {
		buildFileLocation = location;		
		this.properties = properties;	
		arguments.put("artifact", artifactAsString);
	}
	
	/**
	 * ant run for a remote project (or with additional settings) 
	 * @param arguments - the arguments of the ant run
	 * @param properties - the properties of the ant run 
	 * @param location - the location of the build file 
	 */
	public AntRunPerCommandLine( Map<String, String> arguments, Map<String, String> properties, String location) {
		buildFileLocation = location;
		this.arguments = arguments;		
		this.properties = properties;
	}
	
	/**
	 * build a list of tokens to be issues to the process runner 
	 * @return - a list of the commands for the process runner 
	 */
	private List<String> prepareForRun() {		 
		List<String> result = new ArrayList<String>();
		AntRunnerPreferences preferences = plugin.getArtifactContainerPreferences(false).getAntRunnerPreferences();
		String home = preferences.getAntHome();
		String runner = preferences.getAntRunner(); 
		String cmd = home + File.separator + runner;
		cmd = cmd.replace( '\\', File.separatorChar);
		result.add( cmd);
		result.add("-f");
		result.add(buildFileLocation + File.separator + "build.xml");
			
		combinedMap.putAll( arguments);
		if (properties != null)
			combinedMap.putAll( properties);
		
		for (String key : combinedMap.keySet()) {
			result.add( "-D" + key + "=" + combinedMap.get(key));
		}							
			
		return result;
	}
	
	/**
	 * @param target - name of the target 
	 * @param directory - directory to run in 
	 * @param monitor - {@link ProcessNotificationListener} to be notified 
	 * @throws AntRunException - arrgh
	 */
	public void run( String target, File directory, ProcessNotificationListener monitor) throws AntRunException{
		List<String> tasks = new ArrayList<String>(1);
		tasks.add( target);
		run( tasks, directory, monitor);
	}
	
	/**
	 * @param targets - the targets to call 
	 * @param directory - the directory to run it 
	 * @param monitor - the {@link ProcessNotificationListener} to be notified 
	 * @throws AntRunException - arrgh
	 */
	public void run( Collection<String> targets, File directory, ProcessNotificationListener monitor) throws AntRunException{
		try {
			List<String> cmd = prepareForRun();
			cmd.addAll( targets);
			
			// add any system properties 
			Map<String, String> propertyOverrides = VirtualEnvironmentPlugin.getPropertyOverrides();
			if (propertyOverrides != null) {
				for (Entry<String, String> entry : propertyOverrides.entrySet()) {
					cmd.add( "-D" + entry.getKey() + "=" + entry.getValue());
				}
			}
			
			// environment variables  
			Map<String, String> environmentOverrides = VirtualEnvironmentPlugin.getEnvironmentOverrides();
			
			ProcessResults results = ProcessExecution.runCommand( cmd, directory, environmentOverrides, monitor);
			if (results.getRetVal() == 0) {				
				String msg = "Ant task [" + toString(targets) + "] has successfully run on project [" + combinedMap.get( "artifact") + "]";			
				ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.OK);
				ArtifactContainerPlugin.getInstance().log(status);	
			} else {
				throw new ProcessException( results.getErrorText());
			}		
		} catch (ProcessException e) {
			//
			String msg = "Cannot run ANT task [" + toString(targets) + "] target as " + e;			
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);					
			throw new AntRunException( e);
		}		
	}
	
	/**
	 * concat the passed targets into a comma delimited string 
	 * @param targets - the targets 
	 * @return - the string 
	 */
	private String toString( Collection<String> targets) {
		StringBuilder builder = new StringBuilder();
		for (String task: targets) {
			if (builder.length() > 0)
				builder.append(",");
			builder.append( task);			
		}
		return builder.toString();
	}
}
