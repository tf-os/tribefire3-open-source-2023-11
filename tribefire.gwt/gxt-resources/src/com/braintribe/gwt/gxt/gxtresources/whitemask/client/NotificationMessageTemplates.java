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
package com.braintribe.gwt.gxt.gxtresources.whitemask.client;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.core.client.dom.Mask.MaskDefaultAppearance.MaskStyle;
import com.sencha.gxt.core.client.dom.Mask.MessageTemplates;

public interface NotificationMessageTemplates extends MessageTemplates {
	
	@XTemplate("<div class=\"{style.box} notificationBar-hintTextBig\"><div class=\"{style.text}\">{message}</div></div>")
	@Override
    SafeHtml template(MaskStyle style, String message);
	
	@XTemplate("<div class=\"{style.box} loadingParent\"><div class='loadingLogoParent'><img class='loadingLogo' id='progressBarImage' src='{imageSource}'/></div>" +
            "<div id ='progressBarTitle' class='loadingTitle'>{title}</div>" +
            "<div class='loadingText'>{message}</div><div class='progress-grey'><div id='loadingProgressBarMask' class='loadingProgressBar progress-blue' style='height: 2px; width:" +
			"{initialValue}%;'></div></div></div>")
    SafeHtml progressTemplate(MaskStyle style, String message, String imageSource, String title, int initialValue);

}
