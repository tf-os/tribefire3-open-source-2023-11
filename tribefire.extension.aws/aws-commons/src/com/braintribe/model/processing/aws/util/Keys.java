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
package com.braintribe.model.processing.aws.util;

import java.io.StringWriter;
import java.util.Base64;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import com.braintribe.exception.Exceptions;

public class Keys {

	private String publicKeyBase64;
	private String privateKeyBase64;

	public Keys(String publicKeyBase64, String privateKeyBase64) {
		this.publicKeyBase64 = publicKeyBase64;
		this.privateKeyBase64 = privateKeyBase64;
	}

	public String getPublicKeyBase64() {
		return publicKeyBase64;
	}

	public void setPublicKeyBase64(String publicKeyBase64) {
		this.publicKeyBase64 = publicKeyBase64;
	}

	public String getPrivateKeyBase64() {
		return privateKeyBase64;
	}

	public void setPrivateKeyBase64(String privateKeyBase64) {
		this.privateKeyBase64 = privateKeyBase64;
	}

	public String getPublicKeyPem() {
		try {
			byte[] publicKey = Base64.getDecoder().decode(publicKeyBase64);
			StringWriter writer = new StringWriter();
			PemWriter pemWriter = new PemWriter(writer);
			pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKey));
			pemWriter.flush();
			pemWriter.close();

			return writer.toString();
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not create PEM output of public key");
		}
	}
}
