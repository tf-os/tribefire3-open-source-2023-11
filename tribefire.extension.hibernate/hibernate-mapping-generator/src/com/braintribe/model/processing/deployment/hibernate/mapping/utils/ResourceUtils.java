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
package com.braintribe.model.processing.deployment.hibernate.mapping.utils;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;

public class ResourceUtils {

	private static Logger log = Logger.getLogger(ResourceUtils.class);
	private static final String classpathPrefix = "classpath:";

	private ResourceUtils() {
	}

	public static List<String> loadResourceToStrings(String xmlSnippetUrl) {
		try (BufferedReader reader = getReader(xmlSnippetUrl)) {
			List<String> result = newList();
			while (reader.ready()) {
				String line = reader.readLine();
				if (line.contains("<!DOCTYPE hibernate-mapping PUBLIC") || line.contains("Hibernate/Hibernate Mapping DTD")
						|| line.contains("http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd")) {
					continue;
				}
				result.add(line);
			}

			return result;

		} catch (IOException e) {
			throw new UncheckedIOException("XML could not be loaded: '" + xmlSnippetUrl + "'", e);
		}
	}

	public static String loadResourceToString(String xmlSnippetUrl) {

		String res = null;
		try (Reader newBufferedReader = getReader(xmlSnippetUrl)) {
			res = IOTools.slurp(newBufferedReader);
		} catch (IOException e) {
			throw new UncheckedIOException("XML could not be loaded: '" + xmlSnippetUrl + "'", e);
		}

		return res;
	}

	private static BufferedReader getReader(String xmlSnippetUrl) throws IOException {

		Objects.requireNonNull(xmlSnippetUrl, "URL must not be null");

		if (xmlSnippetUrl.startsWith(classpathPrefix)) {

			xmlSnippetUrl = xmlSnippetUrl.substring(classpathPrefix.length());

			// leading slashes must be removed when loading resources from the ClassLoader
			if (xmlSnippetUrl.startsWith("/") && xmlSnippetUrl.length() > 1) {
				xmlSnippetUrl = xmlSnippetUrl.substring(1);
			}

			InputStream stream = ResourceUtils.class.getClassLoader().getResourceAsStream(xmlSnippetUrl);

			if (stream == null) {
				throw new IOException("URL couldn't be loaded from classpath: '" + xmlSnippetUrl + "'");
			}

			if (log.isTraceEnabled())
				log.trace("Loaded from classpath: " + xmlSnippetUrl);

			return new BufferedReader(new InputStreamReader(stream, "UTF-8"));

		} else {

			Path path = Paths.get(xmlSnippetUrl);

			return Files.newBufferedReader(path);

		}

	}

}
