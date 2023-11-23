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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * trigger both the 'repository import' and the 'quick import' controllers to rescan..
 * repository controller : it scans the 'remote', 'local' repositories and the sources (just to get the {@link VersionedArtifactIdentification}) 
 * quick controller : scans only the 'sources' - as it needs the .project files
 *  
 * sources   
 * 
 * @author pit
 *
 */
public class RunQuickImportScanCommand extends AbstractHandler  {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {		
		DevrockPlugin.instance().quickImportController().scheduleRescan();
		DevrockPlugin.instance().repositoryImportController().scheduleRescan();
		return null;
	}
	

}
