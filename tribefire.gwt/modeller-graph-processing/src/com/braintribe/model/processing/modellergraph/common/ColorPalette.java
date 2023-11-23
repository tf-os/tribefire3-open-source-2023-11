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
package com.braintribe.model.processing.modellergraph.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.model.modellergraph.graphics.AggregationKind;
import com.braintribe.model.modellergraph.graphics.Color;

public class ColorPalette {
	
	private static List<String> colorPalette = new ArrayList<String>();
	
	private static Map<AggregationKind, String> colorPerAggregationKind = new HashMap<AggregationKind, String>();
	private static Map<AggregationKind, String> greyColorPerAggregationKind = new HashMap<AggregationKind, String>();
	
	static{
		colorPalette.add("#f8e03e");
		colorPalette.add("#a45e60");
		colorPalette.add("#541f0f");
		colorPalette.add("#cd8b34");
		colorPalette.add("#e7d2b7");
		
		colorPalette.add("#446455");
		colorPalette.add("#fdd262");
		colorPalette.add("#d3dddc");
		colorPalette.add("#c7b19c");
		
		colorPalette.add("#85d4e3");
		colorPalette.add("#f4b5bd");
		colorPalette.add("#9c964a");
		colorPalette.add("#cdc08c");
		colorPalette.add("#fad77b");
		
		colorPalette.add("#e6a0c4");
		colorPalette.add("#c6cdf7");
		colorPalette.add("#d8a499");
		colorPalette.add("#7294d4");
		
		colorPalette.add("#9a8822");
		colorPalette.add("#f5cdb4");
		colorPalette.add("#f8afa8");
		colorPalette.add("#fddda0");
		colorPalette.add("#74a089");
		
		colorPalette.add("#d8b70a");
		colorPalette.add("#02401b");
		colorPalette.add("#a2a475");
		colorPalette.add("#81a88d");
		colorPalette.add("#972d15");
		
		colorPalette.add("#89b151");
		colorPalette.add("#f4ddbe");
		colorPalette.add("#715a38");
		colorPalette.add("#201a02");
		
		colorPalette.add("#798e87");
		colorPalette.add("#c27d38");
		colorPalette.add("#ccc591");
		colorPalette.add("#29211f");
		
		colorPalette.add("#899da4");
		colorPalette.add("#c93312");
		colorPalette.add("#faefd1");
		colorPalette.add("#dc863b");
		
		colorPalette.add("#f3df6c");
		colorPalette.add("#ceab07");
		colorPalette.add("#d5d5d3");
		colorPalette.add("#24281a");
		
		colorPalette.add("#f1bb7b");
		colorPalette.add("#fd6467");
		colorPalette.add("#5b1a18");
		colorPalette.add("#d67236");
		
		colorPalette.add("#8d9876");
		colorPalette.add("#8d9876");
		colorPalette.add("#609f80");
		colorPalette.add("#4b574d");
		colorPalette.add("#af420a");
		
		colorPalette.add("#01abe9");
		colorPalette.add("#1b346c");
		colorPalette.add("#f54b1a");
		colorPalette.add("#e5c39e");
		colorPalette.add("#c3ced0");
		
		colorPalette.add("#aea8a8");
		colorPalette.add("#cc9e02");
		colorPalette.add("#95796d");
		colorPalette.add("#ad6e45");
		
		colorPerAggregationKind.put(AggregationKind.none, "#417cf4");
//		colorPerAggregationKind.put(AggregationKind.collection_aggregation, "#9b59ff");
		colorPerAggregationKind.put(AggregationKind.simple_aggregation, "#f4b942");
		colorPerAggregationKind.put(AggregationKind.unordered_aggregation, "#ac41f4");
		colorPerAggregationKind.put(AggregationKind.ordered_aggregation, "#2bc645");
		colorPerAggregationKind.put(AggregationKind.multiple_aggregation, "#ad6e45");
		colorPerAggregationKind.put(AggregationKind.value_association, "#2bc645");
		colorPerAggregationKind.put(AggregationKind.key_association, "#ad6e45");
		
		greyColorPerAggregationKind.put(AggregationKind.none, "#e0e0e0");
//		greyColorPerAggregationKind.put(AggregationKind.collection_aggregation, "#cccccc");
		greyColorPerAggregationKind.put(AggregationKind.simple_aggregation, "#bfbfbf");
		greyColorPerAggregationKind.put(AggregationKind.unordered_aggregation, "#a3a3a3");
		greyColorPerAggregationKind.put(AggregationKind.ordered_aggregation, "#939393");
		greyColorPerAggregationKind.put(AggregationKind.multiple_aggregation, "#7a7a7a");
		greyColorPerAggregationKind.put(AggregationKind.value_association, "#565656");
		greyColorPerAggregationKind.put(AggregationKind.key_association, "#3a3a3a");
		
	}
	
	public static Color getColor(AggregationKind aggregationKind, boolean grey){
		if(grey)
			return getColor(greyColorPerAggregationKind.get(aggregationKind));
		else
			return getColor(colorPerAggregationKind.get(aggregationKind));
	}
	
	public static Color getColor(String hexString){
		Color color = Color.T.create();
		color.setRed(Integer.valueOf( hexString.substring( 1, 3 ), 16 ));
		color.setGreen(Integer.valueOf( hexString.substring( 3, 5 ), 16 ));
		color.setBlue(Integer.valueOf( hexString.substring( 5, 7 ), 16 ));
		return color;
	}
	
	public static String getColor(Color color){
//		return PathRendering.INSTANCE.rgba((int)color.getRed(), (int)color.getGreen(), (int)color.getBlue(), color.getAlpha());
		return toHex(color).toString();
	}
	
	public static String toHex(Color color) {
	    //String alpha = pad(Integer.toHexString((int)color.getAlpha()));
	    String red = pad(Integer.toHexString((int)color.getRed()));
	    String green = pad(Integer.toHexString((int)color.getGreen()));
	    String blue = pad(Integer.toHexString((int)color.getBlue()));
	    //String hex = "0x" + alpha + red + green + blue;
	    return "#" + red + green + blue;
	}

	private static final String pad(String s) {
	    return (s.length() == 1) ? "0" + s : s;
	}

}
