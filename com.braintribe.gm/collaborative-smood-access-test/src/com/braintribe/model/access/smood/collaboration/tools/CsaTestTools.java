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
package com.braintribe.model.access.smood.collaboration.tools;

import java.io.File;
import java.io.IOException;

import com.braintribe.utils.FileTools;

/**
 * @author peter.gazdik
 */
public class CsaTestTools {

	public static File createWorkingFolder(File prototypFolder) throws IOException {
		File workingFolder = new File(resWork(), prototypFolder.getName());

		if (workingFolder.exists())
			FileTools.deleteDirectoryRecursively(workingFolder);

		FileTools.copyDirectory(prototypFolder, workingFolder);
		
		return workingFolder;
	}

	private static File resWork() throws IOException {
		File res_work = new File("res-work");
		FileTools.ensureDirectoryExists(res_work);
		
		return res_work;
	}

}
