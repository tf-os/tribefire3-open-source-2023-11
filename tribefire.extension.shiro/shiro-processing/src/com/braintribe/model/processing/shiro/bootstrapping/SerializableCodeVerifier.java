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
package com.braintribe.model.processing.shiro.bootstrapping;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;

import com.braintribe.exception.Exceptions;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;

public class SerializableCodeVerifier extends CodeVerifier implements Serializable {

	private static final long serialVersionUID = -8223681020765931679L;

	public SerializableCodeVerifier(String value) {
		super(value);
	}

	public static SerializableCodeVerifier create(CodeVerifier source) {
		return new SerializableCodeVerifier(source.getValue());
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int len = in.readInt();
		byte[] valueBytes = new byte[len];
		in.readFully(valueBytes);
		setValue(valueBytes);

		long expDateTime = in.readLong();
		if (expDateTime != -1) {
			Date expDate = new Date(expDateTime);
			setExpirationDate(expDate);
		}
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		byte[] valueBytes = super.getValueBytes();
		int len = valueBytes.length;

		out.writeInt(len);
		out.write(valueBytes);

		Date expirationDate = super.getExpirationDate();
		if (expirationDate == null) {
			out.writeLong(-1L);
		} else {
			out.writeLong(expirationDate.getTime());
		}
	}

	private void setValue(byte[] newValue) {
		try {
			Field field = Secret.class.getDeclaredField("value");
			field.setAccessible(true);
			field.set(this, newValue);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not deserialize a CodeVerifier");
		}
	}
	private void setExpirationDate(Date newValue) {
		try {
			Field field = Secret.class.getDeclaredField("expDate");
			field.setAccessible(true);
			field.set(this, newValue);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not deserialize a CodeVerifier");
		}
	}
}
