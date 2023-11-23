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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.custom.StyledText;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;

/**
 * handler for style string sequences..
 * based around {@link StyledTextSequenceData}..  
 * 
 * @author pit
 *
 */
public class StyledTextHandler {
	private Supplier<Map<String, Styler>> stylerMapSupplier;
	private Supplier<String> delimiterStyleSupplier;

	/*
	 * 		instance functions  functions 
	 */

	
	/**	
	 * @param stylerMapSupplier - supplies the Map<String,Styler> 
	 */
	@Configurable @Required
	public void setStylerMapSupplier(Supplier<Map<String, Styler>> stylerMapSupplier) {
		this.stylerMapSupplier = stylerMapSupplier;
	}
	
	/**
	 * @param delimiterStyleSupplier - a supplier for the styler-id for the delimiter
	 */
	@Configurable @Required
	public void setDelimiterStyleSupplier(Supplier<String> delimiterStyleSupplier) {
		this.delimiterStyleSupplier = delimiterStyleSupplier;
	}
	
	
	/**
	 * builds the styled text data for a tuple of display-text & styler-id
	 * @param pair - two {@link String} tuple, display-text & styler-id
	 * @return - a {@link Pair} of {@link String} with associated {@link StyledTextSequenceData}
	 */
	public Pair<String, List<StyledTextSequenceData>> build( Pair<String,String> pair) {
		return build( pair.first, pair.second);
	}
	
	
	/**
	 * build a styled text data from the two passed {@link String}
	 * @param text - the text
	 * @param stylerId - the id of the styler 
	 * @return - a {@link Pair} of {@link String} with associated {@link StyledTextSequenceData}
	 */
	public Pair<String, List<StyledTextSequenceData>> build( String text,String stylerId) {		
		int e = text.length();
		StyledTextSequenceData sd = new StyledTextSequenceData(0, e, stylerMapSupplier.get().get( stylerId));		
		return Pair.of(text, Collections.singletonList(sd));
	}
	
	/**
	 * build a styled text data from all passed sequences 
	 * @param map - a {@link List} of {@link Pair} of {@link String} with {@link StyledTextSequenceData}
	 * @return - a {@link Pair} of {@link String} with associated {@link StyledTextSequenceData}
	 */
	public Pair<String, List<StyledTextSequenceData>> build( List<Pair<String,String>> map) {
		List<StyledTextSequenceData> sds = new ArrayList<>( map.size());
		StringBuilder sb = new StringBuilder();
		for (Pair<String, String> entry : map) {
			int s = sb.length();
			sb.append( entry.first);
			int e = sb.length();
			StyledTextSequenceData sd = new StyledTextSequenceData(s, e, stylerMapSupplier.get().get( entry.second));
			sds.add(sd);
		}
		return Pair.of( sb.toString(), sds);
	}
	
	/**
	 * merges all paired styled text data into a single one 
	 * @param pairs - a {@link List} of {@link Pair} of {@link String} with {@link StyledTextSequenceData}
	 * @return - a {@link Pair} of {@link String} with associated {@link StyledTextSequenceData}
	 */
	public Pair<String, List<StyledTextSequenceData>> merge(List<Pair<String, List<StyledTextSequenceData>>> pairs) {		
		return merge( pairs, null);
	}
	
	/**
	 * merges all passed sequences into a single one while optionally inserting delimiter characters between the text parts 
	 * @param pairs  - a {@link List} of {@link Pair} of {@link String} with {@link StyledTextSequenceData}
	 * @param delimiter - an optional delimiter to insert between two text parts 
	 * @return - a {@link Pair} of {@link String} with associated {@link StyledTextSequenceData}
	 */
	public Pair<String, List<StyledTextSequenceData>> merge(List<Pair<String, List<StyledTextSequenceData>>> pairs, String delimiter) {
		
		List<Pair<String, List<StyledTextSequenceData>>>  enhancedPairs;
		if (delimiter == null) {
			enhancedPairs = pairs;
		}
		else {
			enhancedPairs = new ArrayList<>();
			for (Pair<String, List<StyledTextSequenceData>> pair : pairs) {
				if (enhancedPairs.size() > 0) {
					enhancedPairs.add( build( Collections.singletonList( Pair.of(delimiter, delimiterStyleSupplier.get()))));
				}
				enhancedPairs.add( pair);				
			}
		}
		StringBuilder sb = new StringBuilder();
		List<StyledTextSequenceData> sequences = new ArrayList<>();
		for (Pair<String, List<StyledTextSequenceData>> pair : enhancedPairs) {			
			sb.append( pair.first);
			if (sequences.isEmpty()) {
				sequences.addAll( pair.second);
			}
			else {
				sequences = StyledTextHandler.append(sequences, pair.second);
			}
		}
		Pair<String, List<StyledTextSequenceData>> result = Pair.of( sb.toString(), sequences);
		return result;
	}
	
	/*
	 * 		static functions 
	 */
	
	/**
	 * applies the passed {@link StyledTextSequenceData}s to the {@link StyledString}
	 * @param styledText - the {@link StyledString} to adorn
	 * @param sequences - a {@link List} of the {@link StyledTextSequenceData} to apply  
	 * @return - the 'decorated' (adorned) {@link StyledString}
	 */
	public static StyledString applyRanges( StyledString styledText, List<StyledTextSequenceData> sequences) {
		for (StyledTextSequenceData seq : sequences) {
			seq.apply(styledText);
		}
		return styledText;
	}
	
	/**
	 * applies the ranges to the string in the pair
	 * @param pair - a {@link Pair} of {@link String} and {@link List} of {@link StyledTextSequenceData}
	 * @return - the styled {@link StyledString}
	 */
	public static StyledString applyRanges(Pair<String, List<StyledTextSequenceData>> pair) {
		StyledString styledString = new StyledString( pair.first);
		applyRanges(styledString, pair.second);
		return styledString;
	}
	
	/**
	 * merges two (partial) {@link StyledText} fragments into one
	 * @param prefix - the {@link Pair} of {@link String} and {@link List} of {@link StyledTextSequenceData} that comes first
	 * @param suffix -the {@link Pair} of {@link String} and {@link List} of {@link StyledTextSequenceData} that comes second, attached
	 * @return
	 */
	public static StyledString merge( Pair<String, List<StyledTextSequenceData>> prefix, Pair<String, List<StyledTextSequenceData>> suffix) {
		if (prefix == null) {
			StyledString stxt = new StyledString(suffix.first);
			return StyledTextHandler.applyRanges(stxt, suffix.second);
		}
		else {
			StyledString stxt = new StyledString(prefix.first + suffix.first);
			List<StyledTextSequenceData> sequences = new ArrayList<>( prefix.second.size() + suffix.second.size());
			sequences.addAll(prefix.second);
			
			suffix.second.stream().forEach( s -> {
				s.start = s.start + prefix.first.length();
				s.end = s.end + prefix.first.length();				
			});			
			sequences.addAll( suffix.second);
			return StyledTextHandler.applyRanges(stxt, sequences);									
		}		
	}

	/**
	 * appends two {@link List} of {@link StyledTextSequenceData} while adapting the sequences' place  
	 * @param prefix - the 'left' side 
	 * @param suffix - the 'right' side 
	 * @return - the {@link List}
	 */
	public static List<StyledTextSequenceData> append( List<StyledTextSequenceData> prefix, List<StyledTextSequenceData> suffix) {
		List<StyledTextSequenceData> result = new ArrayList<>( prefix.size() + suffix.size());
		result.addAll(prefix);
		// get the last index of the prefix
		int ss = prefix.get( prefix.size() -1).end;
		for (StyledTextSequenceData sd : suffix) {
			sd.start += ss;
			sd.end += ss;
			result.add(sd);
		}						
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static List<StyledTextSequenceData> append( List<StyledTextSequenceData> prefix, List<StyledTextSequenceData> ... toAdd ) {
		List<List<StyledTextSequenceData>> suffices = new ArrayList<>();		
		suffices.addAll( Arrays.asList( toAdd));		
		
		List<StyledTextSequenceData> result = new ArrayList<>();
		result.addAll( prefix);
	
		for (List<StyledTextSequenceData> sds : suffices) {
			append( result, sds);
		}							
		return result;
	}
	
	
	
}
