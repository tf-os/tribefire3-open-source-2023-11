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
package com.braintribe.model.resource;

import java.util.Set;
import java.util.function.BiPredicate;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.specification.PixelDimensionSpecification;
import com.braintribe.model.resource.specification.ResourceSpecification;

public interface AdaptiveIcon extends Icon {

	EntityType<AdaptiveIcon> T = EntityTypes.T(AdaptiveIcon.class);
	
	public static final String representations = "representations";

	public Set<Resource> getRepresentations();	
	public void setRepresentations(Set<Resource> representations);
	
	@Override
	default Resource image() {
		return largestImage();
	}

	default Resource image(BiPredicate<Resource, Resource> comparator) {
		Set<Resource> representations = getRepresentations();
		if (representations.isEmpty())
			return null;
		
		Resource largest = null;
		
		for (Resource image : representations) {
			if (comparator.test(image, largest)) {
				largest = image;
			}
		}
		
		return largest;
	}

	default Resource largestImage() {
		return image(AdaptiveIcon::isLarger);
	}

	default Resource smallestImage() {
		return image(AdaptiveIcon::isSmaller);
	}

	static boolean isLarger(Resource r1, Resource r2) {
		return compare(r1, r2) > 0;
	}

	static boolean isSmaller(Resource r1, Resource r2) {
		return compare(r1, r2) < 0;
	}
	
	static int compare(Resource r1, Resource r2) {
		if (r1 == null && r2 == null)
			return 0;
		
		if (r1 == null)
			return -1;
		
		if (r2 == null)
			return 1;

		ResourceSpecification s1 = r1.getSpecification();
		ResourceSpecification s2 = r2.getSpecification();
		
		if (!(s1 instanceof PixelDimensionSpecification)) { 
			return -1;
		}
		
		if (!(s2 instanceof PixelDimensionSpecification)) {
			return 1;
		}
		
		PixelDimensionSpecification is1 = (PixelDimensionSpecification) s1;
		PixelDimensionSpecification is2 = (PixelDimensionSpecification) s2;
		
		int compareTo = Integer.valueOf(is1.getPixelHeight()).compareTo(is2.getPixelHeight());
		return compareTo;
	}

}
