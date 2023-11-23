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
package com.braintribe.gwt.browserfeatures.client;

import com.google.gwt.dom.client.Element;

public class ScrollUtil {
		
	public static native final void scrollIntoView(Element el) /*-{
		if(el){		
			bounding = el.getBoundingClientRect();		
			isVisible = (0 < bounding.top && bounding.top < (screen.availHeight)) ||
        		(0 < bounding.bottom && bounding.bottom < (screen.availHeight ));        	
	        if(!isVisible){	        	
				el.scrollIntoView({behavior: "auto", block: "start"});
			}
		}
	}-*/;
	
	public static native final void forceScrollIntoView(Element el) /*-{
		el.scrollIntoView({behavior: "auto", block: "start"});
	}-*/;
	
	public static native final boolean isInViewport(Element el) /*-{
		rect = el.getBoundingClientRect();
		elementHeight = el.offsetHeight;
		elementWidth = el.offsetWidth;
		
		return (rect.top >= elementHeight &&
	        rect.left >= elementWidth &&
	        rect.bottom <= ($wnd.innerHeight || $doc.documentElement.clientHeight) + elementHeight &&
	        rect.right <= ($wnd.innerWidth || $doc.documentElement.clientWidth) + elementWidth);
	}-*/;
	
	public static native final void scrollLeft(Element el, int diffX) /*-{
	if(el){		
		el.scrollLeft += diffX;
	}
	}-*/;	

	public static native final void scrollTop(Element el, int diffY) /*-{
	if(el){		
		el.scrollTop += diffY;
	}
	}-*/;			
}
