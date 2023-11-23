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
package com.braintribe.model.processing.securityservice.basic;

import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.stream.NullOutputStream;

public class CredentialsHasher {
	// TODO: discuss fastest and fitting marshaller for this
	private YamlMarshaller marshaller;

	public CredentialsHasher() {
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}

	public String hash(Credentials credentials, Consumer<Map<String, Object>> enricher) {
		try {
			// TODO: which is the best algorithm here and is HASH good anyways?
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			Map<String, Object> credentialsEnvelope = new LinkedHashMap<>();
			credentialsEnvelope.put("credentials", credentials);
			enricher.accept(credentialsEnvelope);

			try (DigestOutputStream out = new DigestOutputStream(NullOutputStream.getInstance(), digest)) {
				marshaller.marshall(out, credentialsEnvelope);
			}

			return StringTools.toHex(digest.digest());
		} catch (Exception e) {
			throw Exceptions.unchecked(e);
		}
	}
}
