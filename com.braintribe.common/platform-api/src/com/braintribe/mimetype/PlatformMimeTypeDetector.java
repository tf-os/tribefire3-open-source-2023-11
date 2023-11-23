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
package com.braintribe.mimetype;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.logging.Logger;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

public class PlatformMimeTypeDetector implements MimeTypeDetector {

	public static final PlatformMimeTypeDetector instance = new PlatformMimeTypeDetector();

	private static final String defaultMimeType = "application/octet-stream";
	private static final Logger log = Logger.getLogger(PlatformMimeTypeDetector.class);

	private Map<String, List<String>> extensionToMimeTypeMap = null;
	private Map<String, List<String>> mimeTypeToExtensionMap = null;

	private PlatformMimeTypeDetector() {
	}

	private void initialize() {
		if (extensionToMimeTypeMap == null) {
			synchronized (this) {
				if (extensionToMimeTypeMap == null) {
					extensionToMimeTypeMap = new ConcurrentHashMap<>();
					mimeTypeToExtensionMap = new ConcurrentHashMap<>();

					try (InputStream in = PlatformMimeTypeDetector.class.getClassLoader()
							.getResourceAsStream("com/braintribe/mimetype/mime-extensions.properties")) {
						List<String> lines = StringTools.readLinesFromInputStream(in, "UTF-8", false);
						for (String line : lines) {
							line = line.trim();
							if (line.startsWith("#") || line.length() == 0) {
								continue;
							}
							int index = line.indexOf("=");
							if (index <= 0 || index == line.length() - 1) {
								continue;
							}
							String mimeType = line.substring(0, index).trim().toLowerCase();
							String extension = line.substring(index + 1).trim().toLowerCase();

							extensionToMimeTypeMap.computeIfAbsent(extension, e -> new ArrayList<>()).add(mimeType);
							mimeTypeToExtensionMap.computeIfAbsent(mimeType, m -> new ArrayList<>()).add(extension);
						}

					} catch (Exception e) {
						log.warn("Error while trying to load the MIME type map.", e);
					}
				}
			}
		}
	}

	@Override
	public String getMimeType(File file, String fileName) {

		initialize();

		String mimeType = null;

		if (!StringTools.isBlank(fileName)) {
			String extension = FileTools.getExtension(fileName);
			List<String> candidates = extensionToMimeTypeMap.get(extension);
			mimeType = candidates != null && !candidates.isEmpty() ? candidates.get(0) : null;
			if (mimeType != null && !mimeType.equals(defaultMimeType)) {
				return mimeType;
			}
		}

		if (file != null) {
			mimeType = getMimeType(file.toPath());
		}

		return defaultIfNull(mimeType);

	}

	@Override
	public String getMimeType(InputStream input, String fileName) {

		initialize();

		String mimeType = null;

		try {
			Path path = tempPath(fileName);
			try (OutputStream out = Files.newOutputStream(path)) {
				IOTools.pump(input, out);
				mimeType = getMimeType(path);
			} finally {
				Files.deleteIfExists(path);
			}

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		return defaultIfNull(mimeType);

	}

	/**
	 * Returns a list of possible MIME types for a specific extension. Most likely, you are interested in the first entry of the list.
	 *
	 * @return A list of possible MIME types. It returns null, if no MIME type was found for the provided extension.
	 * @throws IllegalArgumentException
	 *             When the provided extension is null or empty.
	 */
	public List<String> getMimeTypesForExtension(String extensionWithoutDot) throws IllegalArgumentException {
		initialize();
		if (StringTools.isBlank(extensionWithoutDot)) {
			throw new IllegalArgumentException("The extension must not be null or empty.");
		}
		extensionWithoutDot = extensionWithoutDot.toLowerCase().trim();
		return extensionToMimeTypeMap.get(extensionWithoutDot);
	}

	/**
	 * Returns a list of extensions for a specific MIME type.
	 *
	 * @param mimeType
	 *            The MIME type. It may contain parameters (e.g., text/html; encoding=...) or capital characters.
	 * @return The list of possible MIME types (ordered by the occurence in the mapping file), or null, if no extension was found.
	 * @throws IllegalArgumentException
	 *             Thrown when the mimeType is null or empty.
	 */
	public List<String> getExtensionsForMimeType(String mimeType) throws IllegalArgumentException {
		initialize();
		if (StringTools.isBlank(mimeType)) {
			throw new IllegalArgumentException("The MIME type must not be null or empty.");
		}
		int index = mimeType.indexOf(';');
		if (index != -1) {
			mimeType = mimeType.substring(0, index).trim();
		}
		mimeType = mimeType.toLowerCase();
		mimeType = mimeType.replace(" ", "");
		return mimeTypeToExtensionMap.get(mimeType);
	}

	private String getMimeType(Path path) {
		String mimeType = null;

		try {
			FileNameMap fileNameMap = URLConnection.getFileNameMap();
			mimeType = fileNameMap.getContentTypeFor(path.getFileName().toString());
			if (mimeType != null && mimeType.equalsIgnoreCase(defaultMimeType)) {
				return mimeType;
			}
		} catch (Exception e) {
			log.debug(() -> "Error while trying to get the content type of path " + path);
		}

		try {
			mimeType = Files.probeContentType(path);
			if (mimeType != null && mimeType.equalsIgnoreCase(defaultMimeType)) {
				return mimeType;
			}
		} catch (Exception e) {
			log.debug(() -> "Error while trying to probe content of path " + path);
		}

		return mimeType;

	}

	private Path tempPath(String fileName) {
		Path path;
		try {
			path = Files.createTempFile("mime-type-detection-", fileName != null ? "-" + fileName : null);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return path;
	}

	private String defaultIfNull(String mimeType) {
		return mimeType == null ? defaultMimeType : mimeType;
	}

}
