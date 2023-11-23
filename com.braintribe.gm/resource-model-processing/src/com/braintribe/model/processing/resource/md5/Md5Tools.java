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
package com.braintribe.model.processing.resource.md5;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;

public class Md5Tools {
	public static Md5 getMd5(InputStream in) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		byte md[] = new byte[8192];
		for (int n = 0; (n = in.read(md)) > -1;) {
			digest.update(md, 0, n);
		}
		return new Md5(digest.digest());
	}
	
	public static Md5 getMd5(File file) throws Exception {
		FileInputStream in = new FileInputStream(file);
		
		try {
			return getMd5(in);
		}
		finally {
			in.close();
		}
	}
	
	public static Md5 getMd5(URL url) throws Exception {
		InputStream in = url.openStream();
		
		try {
			return getMd5(in);
		}
		finally {
			in.close();
		}
	}
}
