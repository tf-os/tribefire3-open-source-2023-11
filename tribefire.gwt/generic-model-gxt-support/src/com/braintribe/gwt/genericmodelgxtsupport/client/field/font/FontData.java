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
package com.braintribe.gwt.genericmodelgxtsupport.client.field.font;

import java.util.ArrayList;
import java.util.List;

public class FontData {

	public static List<FontSelection> getFontFamily() {
	    List<FontSelection> fontFamily = new ArrayList<FontSelection>();
		fontFamily.add(new FontSelection("10", "Arial"));
		fontFamily.add(new FontSelection("20", "Arial Black"));
		fontFamily.add(new FontSelection("30", "Comic Sans MS"));
		fontFamily.add(new FontSelection("40", "Courier New"));
		fontFamily.add(new FontSelection("50", "Georgia"));
		fontFamily.add(new FontSelection("60", "Helvetica"));
		fontFamily.add(new FontSelection("70", "Impact"));
		fontFamily.add(new FontSelection("80", "Lucida Sans Unicode"));
		fontFamily.add(new FontSelection("90", "Lucida Console"));		
		fontFamily.add(new FontSelection("100", "Palatino Linotype"));
		fontFamily.add(new FontSelection("110", "Tahoma"));
		fontFamily.add(new FontSelection("120", "Times New Roman"));
		fontFamily.add(new FontSelection("130", "Trebuchet MS"));
		fontFamily.add(new FontSelection("140", "Verdana"));				
		return fontFamily;
	}
	
	public static List<FontSelection> getFontSize() {
	    List<FontSelection> fontSize = new ArrayList<FontSelection>();
		fontSize.add(new FontSelection("10", "10%"));
		fontSize.add(new FontSelection("20", "20%"));
		fontSize.add(new FontSelection("30", "30%"));				
		fontSize.add(new FontSelection("40", "40%"));				
		fontSize.add(new FontSelection("50", "50%"));				
		fontSize.add(new FontSelection("60", "60%"));				
		fontSize.add(new FontSelection("70", "70%"));				
		fontSize.add(new FontSelection("80", "80%"));				
		fontSize.add(new FontSelection("90", "90%"));				
		fontSize.add(new FontSelection("100", "100%"));				
		fontSize.add(new FontSelection("200", "200%"));				
		fontSize.add(new FontSelection("400", "400%"));				
		//fontSize.add(new FontSelection("800", "800%"));				
		fontSize.add(new FontSelection("1000", "xx-small"));
		fontSize.add(new FontSelection("1010", "x-small"));
		fontSize.add(new FontSelection("1020", "small"));
		fontSize.add(new FontSelection("1030", "smaller"));
		fontSize.add(new FontSelection("1040", "medium"));
		fontSize.add(new FontSelection("1050", "larger"));		
		fontSize.add(new FontSelection("1060", "large"));
		fontSize.add(new FontSelection("1070", "x-large"));
		fontSize.add(new FontSelection("1080", "xx-large"));
		//fontSize.add(new FontSelection("1090", "initial"));
		//fontSize.add(new FontSelection("1100", "inherit"));
		return fontSize;
	}	
	
	public static List<FontSelection> getFontStyle() {
	    List<FontSelection> fontStyle = new ArrayList<FontSelection>();
		fontStyle.add(new FontSelection("10", "normal"));
		fontStyle.add(new FontSelection("20", "italic"));
		fontStyle.add(new FontSelection("30", "oblique"));
		return fontStyle;
	}
	
	public static List<FontSelection> getFontWeight() {
	    List<FontSelection> fontWeight = new ArrayList<FontSelection>();
		fontWeight.add(new FontSelection("10", "normal"));
		fontWeight.add(new FontSelection("20", "bold"));
		//fontWeight.add(new FontSelection("100", "100"));
		//fontWeight.add(new FontSelection("200", "200"));
		//fontWeight.add(new FontSelection("300", "300"));
		//fontWeight.add(new FontSelection("400", "400"));
		//fontWeight.add(new FontSelection("500", "500"));
		//fontWeight.add(new FontSelection("600", "600"));
		//fontWeight.add(new FontSelection("700", "700"));
		//fontWeight.add(new FontSelection("800", "800"));
		//fontWeight.add(new FontSelection("900", "900"));		
		//fontWeight.add(new FontSelection("120", "bolder"));
		//fontWeight.add(new FontSelection("130", "lighter"));
		//fontWeight.add(new FontSelection("140", "initial"));				
		//fontWeight.add(new FontSelection("150", "inherit"));				
		return fontWeight;
	}
}
