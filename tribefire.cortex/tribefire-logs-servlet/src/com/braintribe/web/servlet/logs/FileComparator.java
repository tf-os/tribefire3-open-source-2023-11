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
package com.braintribe.web.servlet.logs;

import java.io.File;
import java.util.Comparator;
import java.util.Date;

public class FileComparator implements Comparator<File> {
	@Override
	public int compare(File file1, File file2) {
		int res = new Date(file2.lastModified()).compareTo(new Date(file1.lastModified()));
		if (res == 0) {
			return file1.compareTo(file2);
		}

		return res;
	}
}
