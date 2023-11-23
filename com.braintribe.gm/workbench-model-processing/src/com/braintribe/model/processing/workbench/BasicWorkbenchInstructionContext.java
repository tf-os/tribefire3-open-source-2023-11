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
package com.braintribe.model.processing.workbench;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.workbench.instruction.WorkbenchInstruction;
import com.braintribe.utils.StringTools;

public class BasicWorkbenchInstructionContext implements WorkbenchInstructionContext {

	private PersistenceGmSession session;
	private GmExpertRegistry expertRegistry;
	
	public BasicWorkbenchInstructionContext(PersistenceGmSession session, GmExpertRegistry expertRegistry) {
		this.session = session;
		this.expertRegistry = expertRegistry;
	}
	
	@Override
	public PersistenceGmSession getSession() {
		return session;
	}

	/**
	 * Recursively searches folders based on given path. 
	 * The first element determines the root folder (query for folder name and parent is null).
	 * Based on the root folder a subfolder with name of next path element is searched.  
	 */
	@Override
	public Folder findFolderByPath(String path) throws WorkbenchInstructionProcessorException {
		
		// Split the path into it's elements
		List<String> pathElements = getPathElements(path);
		
		// Return null if no path elements are given.
		if (pathElements.isEmpty()) {
			return null;
		}
		
		// Find the root folder. 
		Folder folder = findRootFolder(pathElements.get(0));
		
		// Return null if no root found.
		if (folder == null) {
			return null;
		}
		
		// Start path iteration with second element (if available)
		for (int i = 1; i < pathElements.size(); i++) {
			String pathElement = pathElements.get(i);
			folder = findSubFolder(pathElement, folder);
			if (folder == null) {
				// No folder found on current level. Return null
				return null;
			}
		}
		
		// We return the last found sub folder. 
		return folder;
	}

	/**
	 * Internally calls {@link #findFolderByPath(String)} and throws a {@link FolderNotFoundException} if no folder was found.
	 */
	@Override
	public Folder getFolderByPath(String path) throws WorkbenchInstructionProcessorException, FolderNotFoundException{
		Folder folder = findFolderByPath(path);
		if (folder == null) {
			throw new FolderNotFoundException("Can't find a folder with path: "+path);
		}
		return folder;
	}
	
	@Override
	public Folder findSubFolder(String folderName, Folder parentFolder) throws WorkbenchInstructionProcessorException {
		
		for (Folder subFolder : parentFolder.getSubFolders()) {
			if (folderName.equals(subFolder.getName())) {
				return subFolder;
			}
		}
		return null;
	
	}
	
	@Override
	public Folder findRootFolder(String folderName) throws WorkbenchInstructionProcessorException {
		//@formatter:off
		EntityQuery folderQuery = 
				EntityQueryBuilder
					.from(Folder.class)
					.where()
						.conjunction()
							.property("name").eq(folderName)
							.property("parent").eq(null)
						.close()
					.done();
		//@formatter:on
		try {
			//TODO: should we be lenient in case multiple root folders with same name exists?
			Folder folder = session.queryCache().entities(folderQuery).unique();
			if (folder == null) {
				folder = session.query().entities(folderQuery).unique(); 
			}
			return folder;
		} catch (Exception e) {
			throw new WorkbenchInstructionProcessorException("Error while searching for root folder: "+folderName,e);
		}
	}
	
	@Override
	public List<String> getPathElements(String path) {
		// Return empty list if no or empty path is given.
		if (path == null || path.isEmpty()) {
			return Collections.emptyList();
		}

		return Arrays.asList(StringTools.splitString(path, "/"));
	}
	
	@Override
	public WorkbenchInstructionExpert<WorkbenchInstruction> getExpertForInstruction(WorkbenchInstruction instruction) {
		return expertRegistry
				.getExpert(WorkbenchInstructionExpert.class)
				.forInstance(instruction);
	}
	
}
