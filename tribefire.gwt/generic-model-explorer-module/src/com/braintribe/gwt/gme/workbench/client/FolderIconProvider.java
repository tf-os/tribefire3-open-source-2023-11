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
package com.braintribe.gwt.gme.workbench.client;

import java.util.List;

import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.client.IconAndType;
import com.braintribe.gwt.gmview.client.IconProviderAdapter;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;


/**
 * Icon provider for the {@link Folder} entity.
 * @author michel.docouto
 *
 */
public class FolderIconProvider extends IconProviderAdapter {
	
	public enum IconSize {
		small, medium, large
	}
	
	private IconSize iconSize = IconSize.small;
	private List<IconSize> iconSizes;
	private PersistenceGmSession gmSession;
	
	/**
	 * Configures the desirable icon size to be returned. Defaults to small.
	 */
	@Configurable
	public void setIconSize(IconSize iconSize) {
		this.iconSize = iconSize;
	}
	
	/**
	 * Configures the desirable icon sizes to be returned. Defaults to null.
	 * Used for accepting more than one size. The icons are searched in the order they appear in the list.
	 */
	public void setIconSizes(List<IconSize> iconSizes) {
		this.iconSizes = iconSizes;
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}

	@Override
	public IconAndType apply(ModelPath modelPath) {
		if (modelPath == null)
			return null;
		
		ModelPathElement pathElement = modelPath.last();
		Object value = pathElement.getValue();
		if (!(value instanceof Folder))
			return null;
		
		Icon icon = ((Folder) value).getIcon();
		Resource imageResource = null;
		if (iconSizes != null && !iconSizes.isEmpty()) {
			for (IconSize iconSize : iconSizes) {
				imageResource = getIcon(iconSize, icon);
				if (imageResource != null)
					break;
			}
		} else
			imageResource = getIcon(iconSize, icon);
		
		if (imageResource != null)
			return new IconAndType(new GmImageResource(imageResource, gmSession.resources().url(imageResource).asString()), false);
		
		return null;
	}
	
	private Resource getIcon(IconSize iconSize, Icon icon) {
		Resource imageResource;
		switch (iconSize) {
		case small:
			imageResource = GMEIconUtil.getSmallImageFromIcon(icon);
			break;
		case medium:
			imageResource = GMEIconUtil.getMediumImageFromIcon(icon);
			break;
		case large:
		default:
			imageResource = GMEIconUtil.getLargeImageFromIcon(icon);
			break;
		}
		
		return imageResource;
	}

}
