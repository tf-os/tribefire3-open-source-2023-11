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
package com.braintribe.devrock.api.ui.viewers.reason;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.stylers.Stylers;
import com.braintribe.gm.model.reason.Reason;

public class ReasonViewLabelProvider extends CellLabelProvider implements IStyledLabelProvider {
	
	public static final String KEY_TYPE = "grp";
	public static final String KEY_MESSAGE = "path";


	private String propertyName; 
	
	private UiSupport uiSupport;
	private String uiSupportStylersKey;
	
	private static Styler typeStyler;
	private static Styler messageStyler;
	
	@Configurable
	public void setUiSupport(UiSupport uiSupport) {
		this.uiSupport = uiSupport;				
		setupUiSupport();
	}
	
	@Configurable
	public void setUiSupportStylersKey(String uiSupportStylersKey) {
		this.uiSupportStylersKey = uiSupportStylersKey;
	}
		
	private void setupUiSupport() {		
		Stylers stylers = uiSupport.stylers(uiSupportStylersKey);
		stylers.addStandardStylers();
		typeStyler = stylers.standardStyler(Stylers.KEY_BOLD);
		messageStyler = stylers.standardStyler(Stylers.KEY_DEFAULT);
		
	}

		
	public ReasonViewLabelProvider(String propertyName, UiSupport uiSupport, String uiSupportStylersKey) {
		this.uiSupport = uiSupport;
		this.propertyName = propertyName;		
		this.uiSupportStylersKey = uiSupportStylersKey;
	}
	


	@Override
	public StyledString getStyledText(Object object) {
		Reason r = (Reason) object;
			
		if (propertyName.equals(KEY_TYPE)) {
			return new StyledString( r.entityType().getShortName(), typeStyler);	
		}
		if (propertyName.equals( KEY_MESSAGE)) {
			return new StyledString( r.getText(), messageStyler);
		}
	
		return new StyledString("");
	}
	

	
	
	@Override
	public String getToolTipText(Object element) {
		Reason reason = (Reason) element;
		
		if (propertyName.equals( KEY_TYPE))
			return "Type of the Reason is " + reason.entityType().getShortName();
		
		if (propertyName.equals( KEY_MESSAGE))
			return "Message of the Reason " + reason.getText();
		
		return super.getToolTipText(element);
	}


	@Override
	public void update(ViewerCell arg0) {
		System.out.println();
		
	}

	@Override
	public void dispose() {				
	}

	@Override
	public Image getImage(Object arg0) {
		
		return null;
	}
	
	

}
