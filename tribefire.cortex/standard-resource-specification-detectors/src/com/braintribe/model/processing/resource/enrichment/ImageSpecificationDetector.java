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
package com.braintribe.model.processing.resource.enrichment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.resource.specification.ImageSpecification;
import com.braintribe.model.resource.specification.RasterImageSpecification;
import com.braintribe.utils.IOTools;

public class ImageSpecificationDetector extends AbstractSpecificationDetector implements ResourceSpecificationDetector<ImageSpecification> {

	private final static Logger logger = Logger.getLogger(ImageSpecificationDetector.class);

	@Override
	public ImageSpecification getSpecification(InputStream in, String mimeType, GmSession session) throws IOException {

		ImageInputStream iis = null;
		ImageReader imageReader = null;

		try {
			iis = ImageIO.createImageInputStream(in);
			if (iis == null)
				return null;

			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			if (!readers.hasNext())
				return null;

			imageReader = readers.next();
			imageReader.setInput(iis);

			return createSpecificationIsPossible(session, imageReader);

		} catch (Exception e) {
			logger.error("Error while detecting image specification.", e);
			return null;

		} finally {
			if (imageReader != null) {
				try {
					imageReader.dispose();
				} catch (Exception e) {
					logger.error("Error while disposing the image reader", e);
				}
			}
			IOTools.closeCloseable(iis, logger);

		}
	}

	private ImageSpecification createSpecificationIsPossible(GmSession session, ImageReader imageReader) {
		Integer height = readHeight(imageReader);
		Integer width = readWidth(imageReader);
		Integer numberImages = readNumImages(imageReader);

		boolean hasDimensions = height != null && width != null;
		boolean hasNumImages = numberImages != null;

		if (!hasDimensions && !hasNumImages)
			return null;

		ImageSpecification result;
		if (hasDimensions) {
			RasterImageSpecification rasterSpecification = contextualCreate(RasterImageSpecification.T, session);
			rasterSpecification.setPixelWidth(width);
			rasterSpecification.setPixelHeight(height);

			result = rasterSpecification;

		} else {
			result = contextualCreate(ImageSpecification.T, session);
		}

		if (hasNumImages)
			result.setPageCount(numberImages);

		return result;
	}

	private Integer readHeight(ImageReader imageReader) {
		try {
			return imageReader.getHeight(0);
		} catch (IOException e) {
			return null;
		}
	}

	private Integer readWidth(ImageReader imageReader) {
		try {
			return imageReader.getWidth(0);
		} catch (IOException e) {
			return null;
		}
	}

	private Integer readNumImages(ImageReader imageReader) {
		try {
			return imageReader.getNumImages(true);
		} catch (IOException e) {
			return null;
		}
	}

}
