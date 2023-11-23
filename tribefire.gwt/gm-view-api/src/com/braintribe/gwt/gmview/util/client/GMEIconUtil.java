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
package com.braintribe.gwt.gmview.util.client;

import java.util.Set;
import java.util.function.BiPredicate;

import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.meta.data.display.GroupAssignment;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.SimpleIcon;
import com.braintribe.model.resource.specification.PixelDimensionSpecification;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.provider.Holder;
import com.google.gwt.resources.client.ImageResource;

/**
 * Util class containing Icon related code to be used throughout GME Components.
 *
 */
public class GMEIconUtil {
	
	private static final Logger logger = new Logger(GMEIconUtil.class);

	public static String getGroupIcon(GroupAssignment groupAssignment, ManagedGmSession gmSession) {
		if (gmSession == null || groupAssignment.getGroup() == null || groupAssignment.getGroup().getIcon() == null)
			return null;
		
		try {
			com.braintribe.model.meta.data.display.Icon icon = groupAssignment.getGroup().getIcon();
			com.braintribe.model.resource.Icon i = icon.getIcon();
			Resource r = null;
			if (i instanceof SimpleIcon)
				r = ((SimpleIcon) i).getImage();
			else if (i instanceof AdaptiveIcon)
				r = GMEIconUtil.getLargeImageFromIcon(i);
			
			if (r != null)
				return gmSession.getModelAccessory().getModelSession().resources().url(r).asString();
		} catch(Exception ex) {
			//NOP
		}
	
		return null;
	}
	
	/**
	 * Create a {@link GmImageResource} from the given {@link Resource} by streaming from the resource session.
	 */
	public static ImageResource transform(Resource resource) {
		if (resource == null || resource.session() == null)
			return null;
		
		return new GmImageResource(resource, ((ManagedGmSession) resource.session()).resources().url(resource).asString());
	}

	/**
	 * Create a {@link GmImageResource} from the given {@link Resource} by streaming from the resource session.
	 */
	public static ImageResource transform(Resource resource, ManagedGmSession gmSession) {
		if (resource == null || gmSession == null)
			return null;
		
		return new GmImageResource(resource, gmSession.resources().url(resource).asString());
	}
	
	/**
	 * Create a {@link GmImageResource} from the given {@link Resource} by streaming from the resource session.
	 */
	public static ImageResource transform(Resource resource, ManagedGmSession gmSession, String accessId) {
		if (resource == null || gmSession == null)
			return null;
		
		return new GmImageResource(resource, gmSession.resources().url(resource).accessId(accessId).asString());
	}
	
	/**
	 * Gets a {@link Resource} from an icon, with the height being less than 17 pixels.
	 */
	public static Resource getSmallImageFromIcon(Icon icon) {
		return GMEIconUtil.getImageFromIcon(icon, 0, 16);
	}

	/**
	 * Gets a {@link Resource} from an icon, with the height being between 17 and 32 pixels.
	 */
	public static Resource getMediumImageFromIcon(Icon icon) {
		return GMEIconUtil.getImageFromIcon(icon, 17, 32);
	}

	/**
	 * Gets a {@link Resource} from an icon, with the height being greater than 32 pixels.
	 */
	public static Resource getLargeImageFromIcon(Icon icon) {
		return GMEIconUtil.getImageFromIcon(icon, 33, Integer.MAX_VALUE);
	}

	public static Resource getSmallestImageFromIcon(Icon icon) {
		return getImageFromIcon(icon, GMEIconUtil::isSmaller);
	}

	public static Resource getLargestImageFromIcon(Icon icon) {
		return getImageFromIcon(icon, GMEIconUtil::isLarger);
	}

	/**
	 * Gets a {@link Resource} from an icon, with the larges height available.
	 */
	private static Resource getImageFromIcon(Icon icon, BiPredicate<Resource, Resource> comparator) {
		if (icon instanceof SimpleIcon)
			return ((SimpleIcon) icon).getImage();
		
		if (!(icon instanceof AdaptiveIcon))
			return null;
		
		Holder<Resource> largest = new Holder<>();
		Set<Resource> representations = ((AdaptiveIcon) icon).getRepresentations();
		if (representations.isEmpty())
			return null;
		
		//if (logger.isDebugEnabled())
			//logger.debug("Comparing: "+representations.size()+" resources for icon: "+icon.getId());
		//@formatter:off
		representations
			.stream()
			.filter(r -> comparator.test(r,largest.get()))
			.forEach(largest);
		//@formatter:on
		Resource r = largest.get();
		
		//if (logger.isDebugEnabled())
			//logger.debug("Determined resource: "+r.getName()+" as the representation of icon: "+icon.getId());
		
		return r;
	}
	
	private static boolean isSmaller(Resource r1, Resource r2) {
		return compare(r1, r2) < 0;
	}
	
	private static boolean isLarger(Resource r1, Resource r2) {
		return compare(r1, r2) > 0;
	}
	
	private static int compare(Resource r1, Resource r2) {
		if (r1 == null && r2 == null)
			return 0;
		
		if (r1 == null)
			return -1;
		
		if (r2 == null)
			return 1;

		ResourceSpecification s1 = r1.getSpecification();
		ResourceSpecification s2 = r2.getSpecification();
		
		if (!(s1 instanceof PixelDimensionSpecification)) { 
			logger.warn("No pixel specification provided for resource: "+r1.getName()+". Spec: "+s2);
			return -1;
		}
		
		if (!(s2 instanceof PixelDimensionSpecification)) {
			logger.warn("No pixel specification provided for resource: "+r2.getName()+". Spec: "+s2);
			return 1;
		}
		
		PixelDimensionSpecification is1 = (PixelDimensionSpecification) s1;
		PixelDimensionSpecification is2 = (PixelDimensionSpecification) s2;
		
		int compareTo = Integer.valueOf(is1.getPixelHeight()).compareTo(is2.getPixelHeight());
		return compareTo;
	}

	/**
	 * Returns a {@link Resource} from an {@link Icon}, with the height being between minExpectedHeight and maxExpectedHeight.
	 * @param minExpectedHeight - the minimum height, in pixels.
	 * @param maxExpectedHeight - the maximum height, in pixels.
	 */
	public static Resource getImageFromIcon(Icon icon, int minExpectedHeight, int maxExpectedHeight) {
		if (icon instanceof SimpleIcon) {
			Resource representation = ((SimpleIcon) icon).getImage();
			if (representation == null)
				return null;
			
			PixelDimensionSpecification imageSpecification = null;
			if (representation.getSpecification() instanceof PixelDimensionSpecification)
				imageSpecification = (PixelDimensionSpecification) representation.getSpecification();
			if (imageSpecification != null) {
				int pixelHeight = imageSpecification.getPixelHeight();
				if (minExpectedHeight <= pixelHeight && maxExpectedHeight >= pixelHeight)
					return representation;
			}
			
			return null;
		}
		
		if (icon instanceof AdaptiveIcon) {
			Set<Resource> representations = ((AdaptiveIcon) icon).getRepresentations();
			if (representations != null) {
				for (Resource representation : representations) {
					if (representation != null) {
						PixelDimensionSpecification imageSpecification = null;
						if (representation.getSpecification() instanceof PixelDimensionSpecification)
							imageSpecification = (PixelDimensionSpecification) representation.getSpecification();
						
						if (imageSpecification != null) {
							int pixelHeight = imageSpecification.getPixelHeight();
							if (minExpectedHeight <= pixelHeight && maxExpectedHeight >= pixelHeight)
								return representation;
						}
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * Returns an {@link Icon} from the given {@link Resource}.
	 */
	public static Icon getIconFromImage(Resource image) {
		if (image != null) {
			SimpleIcon simpleIcon = SimpleIcon.T.create();
			simpleIcon.setImage(image);
			return simpleIcon;
		}
		
		return null;
	}

	/**
	 * Returns an small {@link ImageResource} from the given context, if available.
	 */
	public static ImageResource getSmallIcon(WorkbenchActionContext<?> context, ManagedGmSession gmSession) {
		Icon icon = context.getWorkbenchAction().getIcon();
		if (icon == null) {
			Folder folder = context.getFolder();
			if (folder != null)
				icon = folder.getIcon();
		}
		
		if (icon == null)
			return null;
		
		Resource resource = getSmallImageFromIcon(icon);
		if (resource == null)
			return null;
		
		if (resource.session() == null)
			return transform(resource, gmSession);
		
		return transform(resource);		
	}
	
	/**
	 * Returns an small {@link ImageResource} from the given context, if available.
	 */
	public static ImageResource getSmallIcon(WorkbenchActionContext<?> context) {
		return getSmallIcon(context, null);
	}

}
