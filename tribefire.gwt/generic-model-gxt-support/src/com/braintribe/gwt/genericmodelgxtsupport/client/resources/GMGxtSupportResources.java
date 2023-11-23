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
package com.braintribe.gwt.genericmodelgxtsupport.client.resources;

import com.braintribe.gwt.browserfeatures.client.GWTMetaPropertiesUtil;
import com.braintribe.gwt.browserfeatures.client.GWTMetaPropertiesUtil.IconSet;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface GMGxtSupportResources extends ClientBundle {
	
	public static final GMGxtSupportResources INSTANCE = (GMGxtSupportResources) (GWTMetaPropertiesUtil.getIconSet().equals(IconSet.coloured) ? GWT.create(ColouredGMGxtSupportResources.class) :
		GWT.create(GMGxtSupportResources.class));
	
	@Source ("New_32x32.png")
	public ImageResource add();
	public ImageResource addLocale();
	@Source("OK_32x32.png")
	public ImageResource apply();
	public ImageResource bar_blue_bl();
	public ImageResource bar_blue_br();
	public ImageResource bar_blue_tl();
	public ImageResource bar_blue_tr();
	public ImageResource bar_brightness();
	public ImageResource bar_green_bl();
	public ImageResource bar_green_br();
	public ImageResource bar_green_tl();
	public ImageResource bar_green_tr();
	public ImageResource bar_hue();
	public ImageResource bar_red_bl();
	public ImageResource bar_red_br();
	public ImageResource bar_red_tl();
	public ImageResource bar_red_tr();
	public ImageResource bar_saturation();
	public ImageResource bar_white();
	public ImageResource calendar();
	public ImageResource color();
	@Source ("Delete_32x32.png")
	public ImageResource delete();
	public ImageResource dropDown();
	@Source ("Hide_32x32.png")
	ImageResource hide();
	public ImageResource map_blue_max();
	public ImageResource map_blue_min();
	public ImageResource map_brightness();
	public ImageResource map_hue();
	public ImageResource map_red_max();
	public ImageResource map_red_min();
	public ImageResource map_green_max();
	public ImageResource map_green_min();
	public ImageResource map_saturation();
	public ImageResource map_saturation_overlay();
	public ImageResource map_white();
	public ImageResource mappoint();
	@Source("Multiline_16x16.png")
	ImageResource multiLine();
	public ImageResource rangearrows();
	@Source("Remove_32x32.png")
	public ImageResource remove();
	@Source ("Save_32x32.png")
	public ImageResource save();
	@Source ("Show_32x32.png")
	ImageResource show();

}
