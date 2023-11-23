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
package com.braintribe.gwt.gxt.gxtresources.whitewindow.client;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.theme.base.client.frame.NestedDivFrame;
import com.sencha.gxt.theme.base.client.panel.FramedPanelBaseAppearance;
import com.sencha.gxt.theme.base.client.widget.HeaderDefaultAppearance;
import com.sencha.gxt.theme.blue.client.window.BlueWindowAppearance.BlueHeaderResources;
import com.sencha.gxt.theme.blue.client.window.BlueWindowAppearance.BlueHeaderStyle;
import com.sencha.gxt.theme.blue.client.window.BlueWindowAppearance.BlueWindowDivFrameResources;
import com.sencha.gxt.theme.blue.client.window.BlueWindowAppearance.BlueWindowDivFrameStyle;
import com.sencha.gxt.theme.blue.client.window.BlueWindowAppearance.BlueWindowResources;
import com.sencha.gxt.theme.blue.client.window.BlueWindowAppearance.BlueWindowStyle;
import com.sencha.gxt.widget.core.client.Window.WindowAppearance;

public class WhiteWindowAppearance extends FramedPanelBaseAppearance implements WindowAppearance {
	
	public interface WhiteWindowDivFrameStyle extends BlueWindowDivFrameStyle {
		//NOP
	}
	
	public interface WhiteWindowDivFrameResources extends BlueWindowDivFrameResources {
		
		@Override
        @Source({"com/sencha/gxt/theme/base/client/frame/NestedDivFrame.gss", "com/sencha/gxt/theme/blue/client/window/BlueWindowDivFrame.gss", "WhiteWindowDivFrame.gss"})
        WhiteWindowDivFrameStyle style();
		
	}
	
	public interface WhiteHeaderResources extends BlueHeaderResources {
	    @Override
		@Source({"com/sencha/gxt/theme/blue/client/window/BlueWindowHeader.gss", "WhiteWindowHeader.gss"})
	    BlueHeaderStyle style();
	  }
	
	private BlueWindowStyle blueStyle;
	
	public WhiteWindowAppearance() {
		this((BlueWindowResources) GWT.create(BlueWindowResources.class));
	}

	public WhiteWindowAppearance(BlueWindowResources resources) {
		super(resources, GWT.<FramedPanelTemplate> create(FramedPanelTemplate.class), new NestedDivFrame(
				GWT.<WhiteWindowDivFrameResources> create(WhiteWindowDivFrameResources.class)));

		blueStyle = resources.style();
	}
	
	@Override
	public String ghostClass() {
		return blueStyle.ghost();
	}
	
	@Override
	public HeaderDefaultAppearance getHeaderAppearance() {
		return new HeaderDefaultAppearance(GWT.<WhiteHeaderResources> create(WhiteHeaderResources.class));
	}
	
}
