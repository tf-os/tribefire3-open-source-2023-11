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
package com.braintribe.devrock.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.FileDialog;

import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryConfigurationLoaded;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.model.reason.essential.ParseError;


/**
 * loads a persisted repository configuration and shows it
 * @author pit
 *
 */
public class RepositoryConfigurationViewCommand extends AbstractRepositoryConfigurationViewCommand {
	private static final YamlMarshaller yamlMarshaller = new YamlMarshaller();
	private static final StaxMarshaller staxMarshaller = new StaxMarshaller();

	@Override
	protected Maybe<Container> retrieveRepositoryMaybe() {

		FileDialog fd = new FileDialog( getShell());
		fd.setFilterExtensions( new String[] {"*.yaml", "*.xml"});
		
		String selectedFile = fd.open();
		
		if (selectedFile == null) {
			return null;
		}
		
		File file = new File( selectedFile);
		if (!file.exists()) {
			String msg = "File [" + file.getAbsolutePath() + "] doesn't exist";
			DevrockPluginStatus status = new DevrockPluginStatus(msg, IStatus.ERROR);
			DevrockPlugin.instance().log(status);
			return Maybe.empty( Reasons.build(NotFound.T).text( msg).toReason());
		}
		RepositoryConfiguration repositoryConfiguration;
		try ( InputStream in = new FileInputStream( file)) {
			if (selectedFile.endsWith(".yaml")) {
				long before = System.nanoTime();				
				repositoryConfiguration = (RepositoryConfiguration) yamlMarshaller.unmarshall( in);				
				long after = System.nanoTime();
				double lastProcessingTime = (after - before) / 1E6;				
				return Maybe.complete( postProcess( repositoryConfiguration, file, lastProcessingTime));
			}
			else if (selectedFile.endsWith( ".xml")) {
				long before = System.nanoTime();
				repositoryConfiguration = (RepositoryConfiguration) staxMarshaller.unmarshall( in);
				long after = System.nanoTime();
				double lastProcessingTime = (after - before) / 1E6;				
				return Maybe.complete( postProcess( repositoryConfiguration, file, lastProcessingTime));
			}
			else {
				String msg = "File [" + file.getAbsolutePath() + "] is not of a supported format (yaml/xml)";
				DevrockPluginStatus status = new DevrockPluginStatus(msg, IStatus.ERROR);
				DevrockPlugin.instance().log(status);
				return Maybe.empty( Reasons.build(ParseError.T).text( msg).toReason());
			}
			
		} catch (Exception e) {		
			String msg = "File [" + file.getAbsolutePath() + "] cannot be read";
			DevrockPluginStatus status = new DevrockPluginStatus(msg, e);
			DevrockPlugin.instance().log(status);
			return Maybe.empty( Reasons.build(ParseError.T).text( msg + ": " + e.getMessage()).toReason());
		}			
	}

	private Container postProcess(RepositoryConfiguration repositoryConfiguration, File selectedFile, double lastProcessingTime) {
		Reason umbrella = Reasons.build( RepositoryConfigurationLoaded.T)
				.text("persisted repository configuration from :" + selectedFile.getName())
				.enrich( r -> r.setUrl( selectedFile.getAbsolutePath()))
				.enrich( r -> r.getReasons().add( repositoryConfiguration.getOrigination()))
				.toReason();
		repositoryConfiguration.setOrigination( umbrella);
		
		// build a container return value
		Container container = new Container();
		container.rfcg = repositoryConfiguration;
		container.processingTime = lastProcessingTime;
		container.timestamp = null; // will be interpreted as not being a 'instant compiled' repository, but loaded from a file
		container.file = selectedFile; 
		return container;
		
		
	}
	
	


	

}
