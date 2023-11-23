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
package com.braintribe.gwt.ioc.gme.client.expert;

import java.util.function.Function;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;


public class FolderIconsRasterImageProvider implements Function<GenericEntity, Future<Resource>>{
	
	@Override
	public Future<Resource> apply(GenericEntity index) throws RuntimeException {
		if  (index instanceof Folder) {
			Folder folder = (Folder) index;
			if (folder.getIcon() != null) {
				Icon icon = folder.getIcon();
				
				Resource imageResource = GMEIconUtil.getLargeImageFromIcon(icon);
				if (imageResource == null) {
					imageResource = GMEIconUtil.getMediumImageFromIcon(icon);
					if (imageResource == null) {
						imageResource = GMEIconUtil.getSmallImageFromIcon(icon);
					}
				}
				
				return new Future<Resource>(imageResource);
			}
		}
		return new Future<Resource>(null);
	}

}
