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
package com.braintribe.utils.resource;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.braintribe.logging.Logger;
import com.braintribe.mimetype.MimeTypeDetector;
import com.braintribe.mimetype.PlatformMimeTypeDetector;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.ImageSpecification;
import com.braintribe.model.resource.specification.RasterImageSpecification;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

/**
 * @author peter.gazdik
 */
public class ResourceMetaDataTools {

	private static final Logger log = Logger.getLogger(ResourceMetaDataTools.class);

	private static Set<String> supportedRasterImageMimeTypes = asSet("image/gif", "image/png", "image/jpeg", "image/bmp");

	private static final MimeTypeDetector mimeTypeDetector = PlatformMimeTypeDetector.instance;

	public static Resource fileToResource(String creator, File file) {
		return fileToResource(creator, file, file.getName());
	}

	public static Resource fileToResource(String creator, File file, String fileName) {
		BasicFileAttributes attr = readFileAttributes(file);

		Resource resource = Resource.T.create();
		resource.setName(fileName);
		resource.setCreated(new Date(attr.creationTime().toMillis()));
		resource.setCreator(creator);
		resource.setMd5(computeMd5(file));
		resource.setFileSize(file.length());

		String mimeType = detectMimeType(file, fileName);

		resource.setMimeType(mimeType);
		resource.setSpecification(resolveSpecification(file, mimeType));

		return resource;
	}

	private static BasicFileAttributes readFileAttributes(File file) {
		try {
			return Files.readAttributes(file.getAbsoluteFile().toPath(), BasicFileAttributes.class);
		} catch (IOException e) {
			throw new UncheckedIOException("Error while reading attributes of: " + file.getAbsolutePath(), e);
		}
	}

	private static String computeMd5(File file) {
		try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			return computeMd5(is, "file: " + file.getAbsolutePath());
		} catch (IOException e) {
			throw new RuntimeException("", e);
		}
	}

	private static String computeMd5(InputStream is, String descriptor) {
		MessageDigest digest = newMessageDigest();
		DigestInputStream digestIn = new DigestInputStream(is, digest);

		try {
			IOTools.consume(digestIn);
		} catch (IOException e) {
			throw new RuntimeException("Error while consuming " + descriptor, e);
		}

		return StringTools.toHex(digest.digest());
	}

	private static MessageDigest newMessageDigest() {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 algorithm not found.", e);
		}
	}

	private static String detectMimeType(File file, String fileName) {
		try {
			return mimeTypeDetector.getMimeType(file, fileName);
		} catch (Exception e) {
			log.warn("Error while resolving mime type for file: " + file.getAbsolutePath(), e);
			return null;
		}
	}

	private static ResourceSpecification resolveSpecification(File file, String mimeType) {
		if (!supportedRasterImageMimeTypes.contains(mimeType))
			return null;

		try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			return getSpecification(is);

		} catch (Exception e) {
			log.warn("Error while resolving image specification for file: " + file.getAbsolutePath(), e);
			return null;
		}
	}

	private static ImageSpecification getSpecification(InputStream is) throws IOException {
		ImageInputStream iis = ImageIO.createImageInputStream(is);
		if (iis == null)
			return null;

		Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
		if (!readers.hasNext())
			return null;

		ImageReader imageReader = readers.next();
		try {
			imageReader.setInput(iis);

			int height = imageReader.getHeight(0);
			int width = imageReader.getWidth(0);
			int numberImages = imageReader.getNumImages(true);

			RasterImageSpecification specification = RasterImageSpecification.T.create();
			specification.setPixelWidth(width);
			specification.setPixelHeight(height);
			specification.setPageCount(numberImages);

			return specification;

		} finally {
			try {
				imageReader.dispose();
			} catch (Exception e) {
				log.warn("Error while disposing the image reader", e);
			}
		}

	}

}
