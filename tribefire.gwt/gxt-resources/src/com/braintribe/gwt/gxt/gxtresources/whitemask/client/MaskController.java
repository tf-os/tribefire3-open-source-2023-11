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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;

/**
 * Controller for marking whether a mask should be done with the entire screen opaque.
 */
public class MaskController {
	
	public static boolean maskScreenOpaque = false;
	private static String progressBarImage;
	private static String progressBarTitle = "";
	public static Integer progressBarInitialValue;
	public static Integer progressBarMaxValue;
	
	public static void setProgressMask(boolean opaque, Integer initialValue, Integer maxValue) {
		maskScreenOpaque = opaque;
		progressBarInitialValue = initialValue;
		progressBarMaxValue = maxValue;
	}
	
	public static void setProgressBarTitle(String title) {
		progressBarTitle = title;
		
		Element titleElement = Document.get().getElementById("progressBarTitle");
		if (titleElement == null)
			return;
		
		titleElement.setInnerText(title);
	}
	
	public static void setProgressBarImage(String image) {
		progressBarImage = image;
		
		Element element = Document.get().getElementById("progressBarImage");
		if (element == null)
			return;
		
		ImageElement imageElement = element.cast();
		imageElement.setSrc(image);
	}
	
	public static String getProgressBarTitle() {
		return progressBarTitle;
	}
	
	public static String getProgressBarImage() {
		return progressBarImage;
	}

}
