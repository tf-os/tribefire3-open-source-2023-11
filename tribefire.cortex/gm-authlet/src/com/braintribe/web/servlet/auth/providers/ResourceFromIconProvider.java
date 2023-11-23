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
package com.braintribe.web.servlet.auth.providers;

import java.util.Set;
import java.util.function.Function;

import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.SimpleIcon;
import com.braintribe.model.resource.specification.PixelDimensionSpecification;


public class ResourceFromIconProvider implements Function<Icon, Resource> {
	
	int minExpectedHeight = 17;
	int maxExpectedHeight = 32;
	
	public void setMinExpectedHeight(int minExpectedHeight) {
		this.minExpectedHeight = minExpectedHeight;
	}
	
	public void setMaxExpectedHeight(int maxExpectedHeight) {
		this.maxExpectedHeight = maxExpectedHeight;
	}
	
	@Override
	public Resource apply(Icon icon) throws RuntimeException {
		return getImageFromIcon(icon, minExpectedHeight, maxExpectedHeight);
	}
	
	public static Resource getImageFromIcon(Icon icon, int minExpectedHeight, int maxExpectedHeight) {
		if (icon instanceof SimpleIcon) {
			Resource representation = ((SimpleIcon) icon).getImage();
			if (minExpectedHeight <= 0 && maxExpectedHeight <= 0) {
				// no specification given return representation.
				return representation;
			}
			PixelDimensionSpecification imageSpecification = null;
			if (representation.getSpecification() instanceof PixelDimensionSpecification)
				imageSpecification = (PixelDimensionSpecification) representation.getSpecification();
			
			if (imageSpecification != null && minExpectedHeight <= imageSpecification.getPixelHeight() && maxExpectedHeight >= imageSpecification.getPixelHeight())
				return representation;
			
			return representation;
		} else if (icon instanceof AdaptiveIcon) {
			Set<Resource> representations = ((AdaptiveIcon) icon).getRepresentations();
			if (representations != null) {
				int currentHeight = 0;
				Resource currentResource = null;
				for (Resource representation : representations) {
					
					if (minExpectedHeight <= 0 && maxExpectedHeight <= 0) {
						// no specification given return first representation.
						return representation;
					}
					
					PixelDimensionSpecification imageSpecification = null;
					if (representation.getSpecification() instanceof PixelDimensionSpecification)
						imageSpecification = (PixelDimensionSpecification) representation.getSpecification();
					
					if (imageSpecification != null && minExpectedHeight <= imageSpecification.getPixelHeight() && maxExpectedHeight >= imageSpecification.getPixelHeight()) {
						if (currentResource == null || imageSpecification.getPixelHeight() > currentHeight) {
							currentResource = representation;
							currentHeight = imageSpecification.getPixelHeight();
						}
					}
				}
				
				return currentResource;
			}
		}
		
		return null;
	}

}
