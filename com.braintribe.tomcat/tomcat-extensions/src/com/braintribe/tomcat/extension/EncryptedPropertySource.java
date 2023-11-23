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
package com.braintribe.tomcat.extension;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tomcat.util.IntrospectionUtils.PropertySource;

/**
 * This is a minimal tool for decrypting system property values. It allows to use encrypted values in server.xml or other files. <br>
 * The prefix <code>AES/CBC/PKCS5Padding:</code>indicates that the {@link #getProperty(String)} method should decode the value. If this prefix is not
 * present, the default action (get the actual system property) is performed.
 */
public class EncryptedPropertySource implements PropertySource {

	private static final Logger logger = Logger.getLogger(EncryptedPropertySource.class.getName());

	protected final static String PREFIX = "AES/CBC/PKCS5Padding";

	@Override
	public String getProperty(String key) {

		logger.fine("getProperty called for key " + key);

		if (key != null) {

			if (key.startsWith(PREFIX + ":")) {

				logger.fine("Key " + key + " seems to be an encrypted value.");
				try {
					String passwordEncodedAndEncryptedWithKeyLength = key.substring(PREFIX.length() + 1);

					String passwordEncodedAndEncrypted;
					int index = passwordEncodedAndEncryptedWithKeyLength.indexOf(':');
					int keyLength = 256;
					if (index != -1) {
						passwordEncodedAndEncrypted = passwordEncodedAndEncryptedWithKeyLength.substring(index + 1);
						keyLength = Integer.parseInt(passwordEncodedAndEncryptedWithKeyLength.substring(0, index));
					} else {
						passwordEncodedAndEncrypted = passwordEncodedAndEncryptedWithKeyLength;
					}

					String text = Crypt.decrypt(passwordEncodedAndEncrypted, keyLength);

					logger.fine("Key " + key + " decrypted");

					return text;
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Could not decrypt password: " + key, e);
				}

			} else if (key.startsWith(Cryptor.DECRYPTION_PREFIX)) {

				logger.fine("Key " + key + " seems to be an encrypted value (Jinni-encryption).");

				try {
					String passwordEncrypted = key.substring(Cryptor.DECRYPTION_PREFIX.length());

					String text = Cryptor.decrypt(null, null, null, null, passwordEncrypted);

					logger.fine("Key " + key + " decrypted");

					return text;
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Could not decrypt password: " + key, e);
				}

			} else if ((key.startsWith("decrypt('") && key.endsWith("')")) || (key.startsWith("decrypt(\"") && key.endsWith("\")"))) {

				logger.fine("Key " + key + " seems to be an encrypted value (Jinni-encryption).");

				try {
					String quote = key.startsWith("${decrypt('") ? "'" : "\"";
					int idx1 = key.indexOf(quote);
					int idx2 = key.lastIndexOf(quote);
					if (idx1 > 0 && idx2 > idx1) {
						key = key.substring(("decrypt(" + quote).length(), key.length() - 2);
					}

					String text = Cryptor.decrypt(null, null, null, null, key);

					logger.fine("Key " + key + " decrypted");

					return text;
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Could not decrypt password: " + key, e);
				}
			}
		}

		String value = System.getProperty(key);

		return value;
	}

}
