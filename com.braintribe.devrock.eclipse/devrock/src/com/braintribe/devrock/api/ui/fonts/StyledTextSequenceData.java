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
package com.braintribe.devrock.api.ui.fonts;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;

/**
 * simple style handler for style string : contains data
 * @author pit
 *
 */
public class StyledTextSequenceData {
	public int start;
	public int end;
	public Styler styler;

	public StyledTextSequenceData( int s, int e, Styler styler) {
		this.start = s;
		this.end = e;
		this.styler = styler;
	}	
	
	public StyledString apply( StyledString ss) {		
		ss.setStyle( start, end - start, styler);
		return ss;
	}
}