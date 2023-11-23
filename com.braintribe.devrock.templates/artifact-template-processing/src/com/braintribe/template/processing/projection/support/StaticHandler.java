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
package com.braintribe.template.processing.projection.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Allows manipulation of static structure in the static handler template.
 * 
 */
public class StaticHandler {
	
	private List<String> dirsToCreate = new ArrayList<>();
	private List<String> ignoredFiles = new ArrayList<>();
	private Map<String, String> fileRelocations = new HashMap<>();
	
	/**
	 * Creates an empty directory in the installation directory.
	 */
	public void createDir(String dirPath) {
		dirsToCreate.add(dirPath);
	}
	
	/**
	 * Ignores a file or a directory projected into the installation directory.
	 */
	public void ignore(String filePath) {
		ignoredFiles.add(filePath);
	}
	
	/**
	 * Relocates a file or a directory projected in the installation directory.
	 */
	public void relocate(String source, String target) {
		fileRelocations.put(source, target);
	}
	
	public List<String> getDirsToCreate() {
		return dirsToCreate;
	}

	public List<String> getIgnoredFiles() {
		return ignoredFiles;
	}

	public Map<String, String> getFileRelocations() {
		return fileRelocations;
	}
	
}