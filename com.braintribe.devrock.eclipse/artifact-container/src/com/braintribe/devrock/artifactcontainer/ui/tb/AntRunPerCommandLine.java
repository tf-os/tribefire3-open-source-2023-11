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
package com.braintribe.devrock.artifactcontainer.ui.tb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;

import com.braintribe.build.process.ProcessException;
import com.braintribe.build.process.ProcessExecution;
import com.braintribe.build.process.ProcessResults;
import com.braintribe.build.process.listener.ProcessNotificationListener;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.model.artifact.Artifact;

/**
 * ant runner <br/>
 * ant -Drange="[[groupId:]artifactId][+[groupId:]artifactId]+..]" [-Dskip=<index>] <br/>
 * 
 * @author pit
 *
 */
public class AntRunPerCommandLine {
	private static final String OS_NAME = "os.name";
	private static final String WIN_RUNNER_CMD = "ant.bat";
	private static final String NIX_RUNNER_CMD = "ant";
	private final String KEY_EXPRESSION = "@KeyExpression";
	protected IProject project = null;
	protected Artifact id = null;	
	protected Map<String, String> arguments = new HashMap<String, String>();	
	protected Map<String, String> properties = null;
	protected Map<String,String> combinedMap = new HashMap<String,String>();

	protected ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
	
	public AntRunPerCommandLine() {	
	}
		
	/**
	 * ant run for a remote project (or with additional settings) 
	 * @param arguments - the arguments of the ant run
	 * @param properties - the properties of the ant run 	
	 */
	public AntRunPerCommandLine( Map<String, String> arguments, Map<String, String> properties) {

		this.arguments = arguments;		
		this.properties = properties;
	}
	
	
	/**
	 * build a list of tokens to be issues to the process runner 
	 * @return - a list of the commands for the process runner 
	 */
	private List<String> prepareForRun(List<String> expressions) {		 
		List<String> result = new ArrayList<String>();
		String runner_cmd = isRunningOnWindows() ? WIN_RUNNER_CMD : NIX_RUNNER_CMD;
		result.add( runner_cmd);
		
		if (arguments != null)
			combinedMap.putAll( arguments);
		
		if (properties != null)
			combinedMap.putAll( properties);
		
		for (String key : combinedMap.keySet()) {
			result.add( combinedMap.get(key));
		}
		
		String rangeExpression = "-Drange=" + toString( expressions, '+');
		result.add(rangeExpression);
		
		
		combinedMap.put( KEY_EXPRESSION, toString( expressions, ','));
			
		return result;
	}
	
	
	/**
	 * @param expressions -
	 * @param skipExpression -  
	 * @param directory - the directory to run it 
	 * @param monitor - the {@link ProcessNotificationListener} to be notified 
	 * @throws TbRunException - arrgh
	 */
	public void run( List<String> expressions, String skipExpression, File directory, ProcessNotificationListener monitor) throws TbRunException{
		try {
			List<String> cmd = prepareForRun( expressions);
			
			
			// add any system properties 
			Map<String, String> propertyOverrides = VirtualEnvironmentPlugin.getPropertyOverrides();
			if (propertyOverrides != null) {
				for (Entry<String, String> entry : propertyOverrides.entrySet()) {
					cmd.add( "-D" + entry.getKey() + "=" + entry.getValue());
				}
			}
			
			// skip 
			if (skipExpression != null && skipExpression.length() > 0) {
				cmd.add( "-Dskip=" + skipExpression);
			}
			
			// environment variables  
			Map<String, String> environmentOverrides = VirtualEnvironmentPlugin.getEnvironmentOverrides();
			
			ProcessResults results = ProcessExecution.runCommand( cmd, directory, environmentOverrides, monitor);
			if (results.getRetVal() == 0) {				
				String msg = "tb has successfully run " + combinedMap.get( KEY_EXPRESSION) + "";			
				ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.OK);
				ArtifactContainerPlugin.getInstance().log(status);	
			} else {
				throw new ProcessException( results.getErrorText());
			}		
		} catch (ProcessException e) {
			//
			String msg = "Cannot run tb for [" + toString( expressions, ',') + "] target as " + e;			
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);					
			throw new TbRunException( e);
		}		
	}
	
	/**
	 * concat the passed targets into a comma delimited string 
	 * @param targets - the targets 
	 * @return - the string 
	 */
	private String toString( Collection<String> targets, char delimiter) {
		StringBuilder builder = new StringBuilder();
		for (String task: targets) {
			if (builder.length() > 0)
				builder.append( delimiter);
			builder.append( task);			
		}
		return builder.toString();
	}
	
	private String normalize(String value) {
		if (value == null) {
		    return "";
		}
		return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
	}
	
	private boolean isRunningOnWindows() {
		String osName = normalize(System.getProperty( OS_NAME));
		if (osName.startsWith( "windows")) {
			return true;
		}
		return false;
		
	}
}
