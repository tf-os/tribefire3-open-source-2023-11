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
package com.braintribe.model.resource.specification;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("PDF Specification")
public interface PdfSpecification extends PhysicalDimensionSpecification, PageCountSpecification {

	final EntityType<PdfSpecification> T = EntityTypes.T(PdfSpecification.class);

	String imageAlignment = "imageAlignment";
	String detectPageRotation = "detectPageRotation";
	String scaleTiffByDpi = "scaleTiffByDpi";
	String ignoreInvalidImages = "ignoreInvalidImages";

	@Name("Image Alignment")
	@Description("The alignment of the image.")
	int getImageAlignment();
	void setImageAlignment(int imageAlignment);

	@Name("Page Rotation")
	@Description("The rotation of the image.")
	boolean getDetectPageRotation();
	void setDetectPageRotation(boolean detectPageRotation);

	@Name("Scale TIFF by DPI")
	@Description("Scale TIFF by DPI.")
	boolean getScaleTiffByDpi();
	void setScaleTiffByDpi(boolean scaleTiffByDpi);

	@Name("Ignore Invalid Images")
	@Description("Ignore Invalid Images.")
	boolean getIgnoreInvalidImages();
	void setIgnoreInvalidImages(boolean ignoreInvalidImages);

}
