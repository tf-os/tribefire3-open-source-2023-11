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
package tribefire.platform.wire.space.common;
// ============================================================================

// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;

import com.braintribe.model.processing.resource.enrichment.DelegatingSpecificationDetector;
import com.braintribe.model.processing.resource.enrichment.ImageSpecificationDetector;
import com.braintribe.model.processing.resource.enrichment.PdfSpecificationDetector;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class SpecificationsSpace implements WireSpace {

	@Managed
	public DelegatingSpecificationDetector standardSpecificationDetector() {
		DelegatingSpecificationDetector bean = new DelegatingSpecificationDetector();

		bean.setDetectorMap(map( //
				entry("image/png", imageSpecificationDetector()), //
				entry("image/jpeg", imageSpecificationDetector()), //
				entry("image/bmp", imageSpecificationDetector()), //
				entry("image/gif", imageSpecificationDetector()), //
				entry("image/pjpeg", imageSpecificationDetector()), //
				entry("image/tiff", imageSpecificationDetector()), //
				entry("application/pdf", pdfSpecificationDetector()) //
		));

		return bean;
	}

	@Managed
	public ImageSpecificationDetector imageSpecificationDetector() {
		ImageSpecificationDetector bean = new ImageSpecificationDetector();
		return bean;
	}

	@Managed
	public PdfSpecificationDetector pdfSpecificationDetector() {
		PdfSpecificationDetector bean = new PdfSpecificationDetector();
		return bean;
	}

}
