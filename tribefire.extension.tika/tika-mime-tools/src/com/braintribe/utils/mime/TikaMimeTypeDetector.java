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
package com.braintribe.utils.mime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TemporaryResources;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;

import com.braintribe.logging.Logger;
import com.braintribe.mimetype.MimeTypeDetector;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

/**
 *
 */
public class TikaMimeTypeDetector implements MimeTypeDetector {

	protected static Logger logger = Logger.getLogger(TikaMimeTypeDetector.class);

	protected TikaConfig tikaConfig = null;
	protected Detector detector;

	private TikaConfig getConfig() {
		if (this.tikaConfig == null) {
			synchronized (this) {
				if (this.tikaConfig == null) {
					try {
						this.tikaConfig = new TikaConfig();
						this.detector = this.tikaConfig.getDetector();
					} catch (TikaException | IOException e) {
						logger.error(() -> "Failed to create Tika.", e);
						return null;
					}
				}
			}
		}
		return this.tikaConfig;
	}

	/**
	 * @see com.braintribe.mimetype.MimeTypeDetector#getMimeType(java.io.File, java.lang.String)
	 */
	@Override
	public String getMimeType(File file, String fileName) throws RuntimeException {
		try {
			boolean trace = logger.isTraceEnabled();

			if (null != getConfig()) {

				Metadata metadata = new Metadata();
				if (!StringTools.isEmpty(fileName)) {
					metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);
				}

				TikaInputStream tis = null;
				try (FileInputStream fis = new FileInputStream(file)) {
					tis = TikaInputStream.get(fis);
					org.apache.tika.mime.MediaType mediaType = this.detector.detect(tis, metadata);
					String mimeType = mediaType.toString();
					if (trace)
						logger.trace("Detected MIME type " + mimeType + " for file " + file.getAbsolutePath() + " with original name " + fileName);
					return mimeType;
				} finally {
					IOTools.closeCloseable(tis, logger);
				}
			} else {
				if (trace)
					logger.trace("Could not detect MIME type for file " + file.getAbsolutePath() + " with original name " + fileName);
			}
			return null;
		} catch (IOException e) {
			throw new RuntimeException("Tika exception for file " + file.getName() + " and original filename " + fileName + ".", e);
		}
	}

	@Override
	public String getMimeType(InputStream is, String fileName) {
		boolean trace = logger.isTraceEnabled();
		TemporaryResources temporaryResources = null;

		try {
			if (null != getConfig()) {

				Metadata metadata = new Metadata();
				if (!StringTools.isEmpty(fileName)) {
					metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);
				}

				temporaryResources = new TemporaryResources(); // responsible for temporary file(s)
				TikaInputStream tis = TikaInputStream.get(is, temporaryResources); // does not need no closing
				org.apache.tika.mime.MediaType mediaType = this.detector.detect(tis, metadata);
				String mimeType = mediaType.toString();
				if (trace)
					logger.trace("Detected MIME type " + mimeType + " from input stream with original name " + fileName);
				return mimeType;

			} else {
				if (trace)
					logger.trace("Could not detect MIME type from input stream with original name " + fileName);
			}
			return null;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} finally {
			IOTools.closeCloseable(temporaryResources, logger);
		}
	}

}
