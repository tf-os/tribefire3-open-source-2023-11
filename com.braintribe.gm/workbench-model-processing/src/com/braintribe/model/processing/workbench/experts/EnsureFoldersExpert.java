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
package com.braintribe.model.processing.workbench.experts;

import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.workbench.WorkbenchInstructionContext;
import com.braintribe.model.processing.workbench.WorkbenchInstructionExpert;
import com.braintribe.model.processing.workbench.WorkbenchInstructionProcessorException;
import com.braintribe.model.workbench.instruction.EnsureFolders;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.i18n.I18nTools;

/**
 * Ensures that all folders of given path are either exists or will be (shallow) created
 * with name (and optional displayName).
 */
public class EnsureFoldersExpert implements WorkbenchInstructionExpert<EnsureFolders> {

	private static final Logger logger = Logger.getLogger(EnsureFoldersExpert.class);
	
	@Override
	public void process(EnsureFolders instruction, WorkbenchInstructionContext context) throws WorkbenchInstructionProcessorException {
		
		for (String path : instruction.getPaths()) {

			List<String> pathElements = context.getPathElements(path);

			// We can return if no path is given.
			if (pathElements.isEmpty()) {
				logger.warn("No folder path specified.");
				continue;
			}
			
			PersistenceGmSession session = context.getSession();
			boolean withDisplayName = instruction.getWithDisplayName();
			
			Folder folder = context.findRootFolder(pathElements.get(0));
			if (folder == null) {
				// Couldn't find specified root folder. Create the full path (no parent)
				createFolders(null, pathElements, session, withDisplayName);
				continue;
			}
			
			// Start path iteration on second element.
			boolean stop = false;
			for (int i = 1; i < pathElements.size() && !stop; i++) {
				Folder parent = folder;
				folder = context.findSubFolder(pathElements.get(i), folder);
				if (folder == null) {
					// Found a non existing subFolder. Create the remaining path below last found folder
					createFolders(parent, pathElements.subList(i, pathElements.size()), session, withDisplayName);
					stop = true;
				}
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("All folders of specified path have been found.");
			}

		}
		
	}

	/**
	 * Creates a recursive folder structure based on given path starting below parent (if given). 
	 */
	private void createFolders(Folder parent, List<String> path, PersistenceGmSession session, boolean withDisplayName ) {
		for (String pathElement : path) {
			parent = createFolder(parent, pathElement, session, withDisplayName);
		}
	}

	/**
	 * Create a single folder with name (and displayName) and adds it to parent's subFolders if given.
	 */
	private Folder createFolder(Folder parent, String folderName, PersistenceGmSession session, boolean withDisplayName) {
		Folder folder = session.create(Folder.T);
		folder.setName(folderName);
		if (withDisplayName) {
			folder.setDisplayName(createDisplayName(folderName, session));
		}
		if (parent != null) {
			parent.getSubFolders().add(folder);
		}
		return folder;
	}


	protected LocalizedString createDisplayName(String folderName, final PersistenceGmSession session) {
		
		if (folderName.startsWith("$")) {
			folderName = folderName.substring(1);
		}

		String rawDisplayName = StringTools.prettifyCamelCase(folderName);
		return I18nTools
				.lsBuilder()
				.addDefault(rawDisplayName)
				.factory(() -> {
						return session.create(LocalizedString.T);
				})
				.build();
	}


	

}
