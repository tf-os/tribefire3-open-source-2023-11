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
package com.braintribe.tribefire.jinni;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import com.braintribe.exception.Exceptions;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

public class JinniEnvironment extends OverridingEnvironment {

	public static final String JINNI_LINE_WIDTH_VAR_NAME = "BT__JINNI_LINE_WIDTH";

	public JinniEnvironment(File file) {
		super(new StandardEnvironment());
		final Properties properties = new Properties();
		if (file != null && file.exists()) {
			try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
				properties.load(reader);
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while loading environment properties file: " + file.getAbsolutePath());
			}
		}
		properties.forEach((name, value) -> {
			setProperty((String) name, (String) value);
		});
	}
}
