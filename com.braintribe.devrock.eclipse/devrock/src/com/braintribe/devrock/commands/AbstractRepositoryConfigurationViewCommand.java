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
import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.devrock.ui.cfg.repository.RepositoryConfigurationInfoDialog;
import com.braintribe.gm.model.reason.Maybe;

/**
 * abstract base for 
 * @author pit
 *
 */
public abstract class AbstractRepositoryConfigurationViewCommand extends AbstractHandler{
	
	private Shell shell;
	
	protected abstract Maybe<Container> retrieveRepositoryMaybe();
	
	protected Shell getShell() {
		if (shell == null) {
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		}
		return shell;
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
					
		RepositoryConfigurationInfoDialog dlg = new RepositoryConfigurationInfoDialog( getShell());
	
		Maybe<Container> repositoryMaybe = retrieveRepositoryMaybe();
		if (repositoryMaybe.isUnsatisfied()) {
			DevrockPluginStatus status = new DevrockPluginStatus( repositoryMaybe.whyUnsatisfied());
			DevrockPlugin.instance().log(status);
			return null;
		}
		Container loaded = repositoryMaybe.get(); 
		
		dlg.setRepositoryConfiguration( loaded.rfcg);
		dlg.setLastProcessingTime( loaded.processingTime);
		dlg.setTimestamp( loaded.timestamp);
		dlg.setOrigin( loaded.file);
		dlg.open();
		return null;
	}
	
	/**
	 * a helper to transfer complex data 
	 * @author pit
	 *
	 */
	protected class Container {
		public RepositoryConfiguration rfcg;
		public Date timestamp;
		public double processingTime;
		public File file;
		
	}
}
