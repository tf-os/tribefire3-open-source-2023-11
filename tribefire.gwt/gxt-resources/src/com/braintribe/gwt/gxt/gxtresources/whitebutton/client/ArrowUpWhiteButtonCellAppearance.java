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
package com.braintribe.gwt.gxt.gxtresources.whitebutton.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.theme.base.client.button.ButtonCellDefaultAppearance;
import com.sencha.gxt.theme.base.client.frame.TableFrame;

public class ArrowUpWhiteButtonCellAppearance<C> extends ButtonCellDefaultAppearance<C> {
	
	public interface WhiteButtonCellStyle extends ButtonCellStyle {
		//NOP
	}
	
	public interface WhteButtonCellResources extends ButtonCellResources {
		
		@Override
		@Source({"com/sencha/gxt/theme/base/client/button/ButtonCell.gss", "WhiteButtonCell.gss", "ArrowUpWhiteButtonCell.gss"})
		WhiteButtonCellStyle style();
		
		@Override
		@Source("com/braintribe/gwt/gxt/gxtresources/images/arrowUp.gif")
		ImageResource arrow();
		
	}
	
	public ArrowUpWhiteButtonCellAppearance() {
		super(GWT.<WhteButtonCellResources>create(WhteButtonCellResources.class), GWT.<ButtonCellTemplates> create(ButtonCellTemplates.class),
				new TableFrame(GWT.<WhiteButtonTableFrameResources> create(WhiteButtonTableFrameResources.class)));
	}

}
