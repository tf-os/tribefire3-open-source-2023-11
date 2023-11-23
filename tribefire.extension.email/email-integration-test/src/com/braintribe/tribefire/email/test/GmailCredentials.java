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
package com.braintribe.tribefire.email.test;

import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.utils.encryption.Cryptor;
import com.braintribe.utils.lcd.StringTools;

public class GmailCredentials {

	private static String user = "email.cartridge@gmail.com";

	public static String getEmail() {
		String plainValue = TribefireRuntime.getProperty("GMAIL_EMAIL");
		if (!StringTools.isBlank(plainValue)) {
			return plainValue;
		}
		return user;
	}

	public static String getPassword() {
		String plainValue = TribefireRuntime.getProperty("GMAIL_PASSWORD");
		if (!StringTools.isBlank(plainValue)) {
			return plainValue;
		}
		String encryptedValue = TribefireRuntime.getProperty("GMAIL_PASSWORD_ENCRYPTED");
		if (StringTools.isBlank(encryptedValue)) {
			throw new IllegalStateException("Neither the property GMAIL_PASSWORD nor GMAIL_PASSWORD_ENCRYPTED are set.");
		}

		return Cryptor.decrypt(TribefireRuntime.DEFAULT_TRIBEFIRE_DECRYPTION_SECRET, null, null, null, encryptedValue);
	}
}
