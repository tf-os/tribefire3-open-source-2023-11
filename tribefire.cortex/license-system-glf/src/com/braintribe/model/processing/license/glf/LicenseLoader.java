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
package com.braintribe.model.processing.license.glf;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.auxilii.glf.client.exception.SystemException;
import com.auxilii.glf.client.loader.XMLLoader;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.utils.StringTools;

public class LicenseLoader extends XMLLoader {
	private InputStreamProvider inputStreamProvider;
	private MessageDigest digest;

	public LicenseLoader(InputStreamProvider inputStreamProvider) throws SystemException {
		super();
		this.inputStreamProvider = inputStreamProvider;
		
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public InputStream openLicenseStream() throws Exception {
		return new DigestInputStream(inputStreamProvider.openInputStream(), digest);
	}
	
	public String getMd5() {
		return StringTools.toHex(digest.digest());
	}
	
	@Override
	public boolean stateChanged() {
		return false;
	}
}
