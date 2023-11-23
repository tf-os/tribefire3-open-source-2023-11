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
package tribefire.extension.xml.schemed.test.commons.xsd.test.util;

import java.io.File;

public class TestUtil {
	
	/**
	 * recursive file delete 
	 * @param file - top directory to be deleted
	 */
	public static void delete( File file) {
		if (file == null || file.exists() == false)
			return;
		for (File child : file.listFiles()) {
			if (child.isDirectory()) {
				delete( child);
			} 
			child.delete();			
		}
	}

	/**
	 * make sure that the directory exists AND IS EMPTY
	 * @param output - the directory to ensure
	 */
	public static void ensure(File output) {	
		if (output.exists())
			delete( output);
		output.mkdirs();
	}
}
