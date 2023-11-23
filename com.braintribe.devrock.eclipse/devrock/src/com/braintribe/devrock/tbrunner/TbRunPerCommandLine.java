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
package com.braintribe.devrock.tbrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;

import com.braintribe.devrock.api.process.ProcessException;
import com.braintribe.devrock.api.process.ProcessExecution;
import com.braintribe.devrock.api.process.ProcessResults;
import com.braintribe.devrock.api.process.listener.ProcessNotificationListener;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;

/**
 * tb runner <br/>
 * tb [[groupId:]artifactId] <br/>
 * 
 * @author pit
 *
 */
@Deprecated
public class TbRunPerCommandLine {
	private static final String RUNNER_CMD = "tb.bat";
	private final String KEY_EXPRESSION = "@KeyExpression";
	protected IProject project = null;
	
	protected Map<String, String> arguments = new HashMap<String, String>();	
	protected Map<String, String> properties = null;
	protected Map<String,String> combinedMap = new HashMap<String,String>();

	
	public TbRunPerCommandLine() {	
	}
		
	/**
	 * ant run for a remote project (or with additional settings) 
	 * @param arguments - the arguments of the ant run
	 * @param properties - the properties of the ant run 	
	 */
	public TbRunPerCommandLine( Map<String, String> arguments, Map<String, String> properties) {

		this.arguments = arguments;		
		this.properties = properties;
	}
	
	
	/**
	 * build a list of tokens to be issues to the process runner 
	 * @return - a list of the commands for the process runner 
	 */
	private List<String> prepareForRun(List<String> expressions) {		 
		List<String> result = new ArrayList<String>();		
		result.add( RUNNER_CMD);
		
		if (arguments != null)
			combinedMap.putAll( arguments);
		
		if (properties != null)
			combinedMap.putAll( properties);
		
		for (String key : combinedMap.keySet()) {
			result.add( combinedMap.get(key));
		}
		
		for (String expression : expressions) {
			result.add( expression);
		}
		
		combinedMap.put( KEY_EXPRESSION, toString( expressions));
			
		return result;
	}
	
	
	/**
	 * @param targets - the targets to call 
	 * @param directory - the directory to run it 
	 * @param monitor - the {@link ProcessNotificationListener} to be notified 
	 * @throws TbRunException - arrgh
	 */
	public void run( List<String> expressions, File directory, ProcessNotificationListener monitor) throws TbRunException{
		try {
			List<String> cmd = prepareForRun( expressions);
			
			
			// add any system properties 
			Map<String, String> propertyOverrides = DevrockPlugin.instance().virtualEnviroment().getPropertyOverrrides();
			if (propertyOverrides != null) {
				for (Entry<String, String> entry : propertyOverrides.entrySet()) {
					cmd.add( "-D" + entry.getKey() + "=" + entry.getValue());
				}
			}
			
			// environment variables  
			Map<String, String> environmentOverrides = DevrockPlugin.instance().virtualEnviroment().getEnvironmentOverrrides();
			
			ProcessResults results = ProcessExecution.runCommand( cmd, directory, environmentOverrides, monitor);
			if (results.getRetVal() == 0) {				
				String msg = "tb has successfully run " + combinedMap.get( KEY_EXPRESSION) + "";			
				DevrockPluginStatus status = new DevrockPluginStatus(msg, IStatus.OK);
				DevrockPlugin.instance().log(status);	
			} else {
				throw new ProcessException( results.getErrorText());
			}		
		} catch (ProcessException e) {
			//
			String msg = "Cannot run tb for [" + toString( expressions) + "] target as " + e;			
			DevrockPluginStatus status = new DevrockPluginStatus(msg, e);
			DevrockPlugin.instance().log(status);					
			throw new TbRunException( e);
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
