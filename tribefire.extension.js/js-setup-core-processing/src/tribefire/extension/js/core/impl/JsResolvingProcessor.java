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
package tribefire.extension.js.core.impl;


import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.extension.js.core.api.JsResolver;
import tribefire.extension.js.core.wire.JsResolverTerminalModule;
import tribefire.extension.js.core.wire.contract.JsResolverContract;
import tribefire.extension.js.core.wire.space.JsResolverConfigurationSpace;

/**
 * core processor for js module resolving support
 * @author pit
 *
 */
public class JsResolvingProcessor {	
	
	private static final String USER_HOME = "user.home";
	private static final String JS_LIBRARIES_DEFAULT = "/.devrock/js-libraries";
	public static final String JS_LIBRARIES = "DEVROCK_JS_LIBRARIES";
	private boolean preferMinOverPretty;
	private boolean supportLocalProjects;	
	private boolean useSymbolicLink = true;
		
	@Configurable
	public void setPreferMinOverPretty(boolean preferMinOverPretty) {
		this.preferMinOverPretty = preferMinOverPretty;
	}
	@Configurable
	public void setSupportLocalProjects(boolean supportLocalProjects) {
		this.supportLocalProjects = supportLocalProjects;
	}
	
	@Configurable
	public void setUseSymbolicLink(boolean useSymbolicLink) {
		this.useSymbolicLink = useSymbolicLink;
	}
	
	public JsResolverResult resolve(File workingDirectory, Map<File, String> linkFolders, VirtualEnvironment ves) {
		if (workingDirectory == null) {
			workingDirectory = new File( ".");
		}
										
		// create a specific configuration space 
		JsResolverConfigurationSpace configurationSpace = new JsResolverConfigurationSpace();
		configurationSpace.setWorkingDirectory(workingDirectory.getParentFile());
		configurationSpace.setResolutionDirectory(workingDirectory.getParentFile());
		configurationSpace.setRelevantPartTuples( Arrays.asList( PartTupleProcessor.createPomPartTuple()));
		configurationSpace.setVirtualEnvironment(ves);
		configurationSpace.setPreferMinOverPretty(preferMinOverPretty);
		configurationSpace.setSupportLocalProjects(supportLocalProjects);		
		configurationSpace.setUseSymbolicLink( useSymbolicLink);
				
		try (
				WireContext<JsResolverContract> resolverContext = Wire.context( new JsResolverTerminalModule( configurationSpace))
		) {
			JsResolver resolver = resolverContext.contract().jsResolver();
			File jsRepository = determineJsRepository(ves);
			
			if (linkFolders != null)
				return resolver.resolve(workingDirectory, jsRepository, linkFolders);
			else
				return resolver.resolve(workingDirectory, jsRepository);			
						
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot execute js resolving", IllegalStateException::new);
		} 						

	}
		
	/**
	 * @param workingDirectoryPath - the directory of the project to instrument
	 * @param settingsPath - the path to the settings.xml file to use (null if standard Maven style resolving should happen) 
	 * @param m2RepositoryPath - the path to Maven local repository (null if to be taken from the settings.xml)
	 */
	public JsResolverResult resolve(File workingDirectory, VirtualEnvironment ves) {
		return resolve(workingDirectory, null, ves);
	}
	
	private File determineJsRepository(VirtualEnvironment ves) {
		String jsRepositoryPath = ves.getEnv(JS_LIBRARIES);
		if (jsRepositoryPath == null) {
			String userhome = ves.getProperty(USER_HOME);
			jsRepositoryPath = userhome + JS_LIBRARIES_DEFAULT;
		}
		
		File jsRepository = new File( jsRepositoryPath);
		jsRepository.mkdirs();
		return jsRepository;
	}
	
	/**
	 * @param workingDirectoryPath - the directory of the project to instrument
	 * @param settingsPath - the path to the settings.xml file to use (null if standard Maven style resolving should happen) 
	 * @param m2RepositoryPath - the path to Maven local repository (null if to be taken from the settings.xml)
	 */

	public void resolve(Collection<String> terminals, File targetDirectory, File projectsDirectory, VirtualEnvironment ves) {							
		// create a specific configuration space 
		JsResolverConfigurationSpace configurationSpace = new JsResolverConfigurationSpace();
		configurationSpace.setWorkingDirectory( targetDirectory);	
		configurationSpace.setResolutionDirectory(projectsDirectory);
		configurationSpace.setRelevantPartTuples( Arrays.asList( PartTupleProcessor.createPomPartTuple()));
		configurationSpace.setVirtualEnvironment(ves);
		configurationSpace.setPreferMinOverPretty(preferMinOverPretty);
		configurationSpace.setSupportLocalProjects(supportLocalProjects);
		configurationSpace.setUseSymbolicLink(useSymbolicLink);
		
		try (
			WireContext<JsResolverContract> resolverContext = Wire.context( new JsResolverTerminalModule( configurationSpace))
		) {
			
			File jsRepository = determineJsRepository(ves);			
			JsResolver resolver = resolverContext.contract().jsResolver();	
			jsRepository.mkdirs();			
			resolver.resolve(terminals, jsRepository, targetDirectory, projectsDirectory);			
						
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot execute js resolving", IllegalStateException::new);
		} 						
	}	
}
