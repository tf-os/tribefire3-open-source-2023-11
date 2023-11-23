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
package com.braintribe.devrock.artifactcontainer.control.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.model.malaclypse.cfg.container.ArtifactContainerConfiguration;
import com.braintribe.model.malaclypse.cfg.container.ArtifactKind;
import com.braintribe.model.malaclypse.cfg.container.ContainerKind;
import com.braintribe.model.malaclypse.cfg.container.ResolverKind;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * @author pit
 *
 */
public class ArtifactContainerConfigurationPersistenceExpert {
	private StaxMarshaller marshaller = new StaxMarshaller();
	private static final String CONFIGURATION_FILE = ".container.cfg.xml";
	private static final String LEGACY_CONFIGURATION_FILE = "container.xml";
	
	/**
	 * generate and return a file to read/write the container's configuration 	
	 */
	private File deriveFile( IProject project) {
		String name = project.getLocation().toOSString() + File.separator + CONFIGURATION_FILE;
		return new File( name);
	}
	
	/**
	 * return a file to read the legacy information 
	 */
	private File deriveLegacyFile( IProject project) {
		String name = project.getLocation().toOSString() + File.separator + LEGACY_CONFIGURATION_FILE;
		return new File( name);
	}
	/**
	 * load the configuration or - if not found, or an error occurs - generate a default configuration
	 * @param project - the {@link IProject} the container is associated with 
	 * @return - a valid {@link ArtifactContainerConfiguration}, read or generated 
	 */
	public ArtifactContainerConfiguration getConfiguration(IProject project) {
		File file = deriveFile(project);
		if (file.exists() == false) {			
			return importLegacyContainer( project); 
		}
		InputStream stream = null;
		try {
			stream = new FileInputStream( file);
			ArtifactContainerConfiguration configuration = (ArtifactContainerConfiguration) marshaller.unmarshall( stream);
			if (configuration.getGlobalId() == null) {
				configuration.setGlobalId( UUID.randomUUID().toString());
				configuration.setModified( true);
			}
			return configuration;
		} catch (IOException e) {
			String msg = "cannot open stream from [" + file.getAbsolutePath() + "]";			
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		} catch (MarshallException e) {
			String msg = "cannot unmarshall container configuration from [" + file.getAbsolutePath() + "]";			
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		finally {
			IOTools.closeQuietly(stream);
		}
		return generateDefaultContainerConfiguration();
	}
	
	/**
	 * load an old configuration and convert it into the new format 	
	 */
	private ArtifactContainerConfiguration importLegacyContainer( IProject project) {
		File file = deriveLegacyFile(project);
		if (file == null || !file.exists())
			return null;
		try {
			Document document = DomParser.load().from(file);
			Element kindE = DomUtils.getElementByPath( document.getDocumentElement(), "kind", false);
			if (kindE != null) {
				String kind = kindE.getTextContent();
				
				ArtifactContainerConfiguration cfg = generateDefaultContainerConfiguration();
				cfg.setArtifactKind( extractMatchingArtifactKind(kind));
				return cfg;
			}
		} catch (DomParserException e) {
			String msg = "cannot read legacy container information file [" + file.getAbsolutePath() + "]";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		return null;	
	}
	
	/**
	 * extract the artifact kind in the current modeling enum  
	 * @param kind - the string as found in the old container 
	 * @return - the respective {@link ArtifactKind}
	 */
	private ArtifactKind extractMatchingArtifactKind( String kind) {
		if (kind.equalsIgnoreCase("MODEL")) {
			return ArtifactKind.model;
		}
		if (kind.equalsIgnoreCase("GWT_TERMINAL")) {
			return ArtifactKind.gwtTerminal;
		}
		if (kind.equalsIgnoreCase("GWT_LIBRARY")) {
			return ArtifactKind.gwtTerminal;
		}
		if (kind.equalsIgnoreCase("PLUGIN")) {
			return ArtifactKind.plugin;
		}
		// default: standard
		return ArtifactKind.standard;
	}
	
	/**
	 * save a configuration of a container
	 * @param project - the {@link IProject} the container is associated with 
	 * @param artifactContainerConfiguration - the {@link ArtifactContainerConfiguration} to store 
	 */
	public void saveConfiguration( IProject project, ArtifactContainerConfiguration artifactContainerConfiguration) {
		if (project == null) {
			return;
		}
		File file = deriveFile(project);
		OutputStream stream = null;
		try {
			artifactContainerConfiguration.setModified(false);
			stream = new FileOutputStream(file);
			marshaller.marshall(stream, artifactContainerConfiguration, GmSerializationOptions.defaults.outputPrettiness( OutputPrettiness.high).stabilizeOrder(true));					
		} catch (MarshallException e) {
			String msg="cannot marshall container configuration to [" + file.getAbsolutePath() + "]";			
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
			artifactContainerConfiguration.setModified( true);
		} catch (IOException e) {
			String msg="cannot open stream to write container configuration to [" + file.getAbsolutePath() + "]";			
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
			artifactContainerConfiguration.setModified( true);
		}		
		finally {
			IOTools.closeQuietly(stream);
		}
	}
	
	/**
	 * generates a default configuration 
	 * @return - a {@link ArtifactContainerConfiguration} initialized to default values
	 */
	public static ArtifactContainerConfiguration generateDefaultContainerConfiguration() {
		ArtifactContainerConfiguration containerConfiguration = ArtifactContainerConfiguration.T.create();
		containerConfiguration.setGlobalId( UUID.randomUUID().toString());
		containerConfiguration.setArtifactKind( ArtifactKind.standard);
		containerConfiguration.setResolverKind( ResolverKind.optimistic);
		containerConfiguration.setContainerKind( ContainerKind.dynamicContainer);	
		containerConfiguration.setModified( true);
		return containerConfiguration;
	}
	
}
