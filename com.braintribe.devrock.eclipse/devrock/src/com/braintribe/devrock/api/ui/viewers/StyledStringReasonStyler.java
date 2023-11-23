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
package com.braintribe.devrock.api.ui.viewers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.stringtemplate.v4.compiler.STParser.mapExpr_return;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.fonts.StyledTextHandler;
import com.braintribe.devrock.api.ui.fonts.StyledTextSequenceData;
import com.braintribe.devrock.api.ui.stylers.Stylers;
import com.braintribe.devrock.eclipse.model.reason.devrock.ProjectNonPerfectMatch;
import com.braintribe.devrock.model.mc.cfg.origination.Origination;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.wire.api.util.Lists;

/**
 * actually implementation for Eclipse - takes the data from the {@link ReasonStyler} and applies StyledStringStyler to it  
 * 
 * @author pit
 *
 */
public class StyledStringReasonStyler {
	public static final int COLOR_ERROR = SWT.COLOR_DARK_RED;
	public static final int COLOR_WARNING = SWT.COLOR_DARK_YELLOW;
	
	public static String infoMark = "ⓘ"; //"⒤"; // "ⅈ";
	public static String warnMark = "ⓦ";
	public static String errorMark = "Ⓔ";
	
	public static final String REASON_STYLERS = "reason_stylers";
	
	private ReasonStyler reasonStyler = new ReasonStyler();
	private UiSupport uiSupport;
	private LazyInitialized<Map<String,String>> coalescedStyles = new LazyInitialized<>(this::buildStyleMap);
	private String defaultStylerId = "standard";
	
	private enum ReasonLevel {info, error, warning};
	private boolean suppressTextPrefix = true;
	
	@Configurable @Required
	public void setUiSupport(UiSupport uiSupport) {
		this.uiSupport = uiSupport;
	}
	
	@Configurable
	public void setSuppressTextPrefix(boolean suppressTextPrefix) {
		this.suppressTextPrefix = suppressTextPrefix;
	}
			
	/**
	 * process a {@link Reason} into a {@link StyledString}
	 * @param reason - the {@link Reason}
	 * @return - the resulting {@link StyledString}
	 */
	public StyledString process( Reason reason) {
		List<Pair<String, String>> stylingData = reasonStyler.process(reason);
		Stylers stylers = uiSupport.stylers(REASON_STYLERS);
		
		// scan for 'undefined' styler
		boolean undefinedValueInExpression = false;
		for (Pair<String,String> styledData : stylingData) {
			if (styledData.second.equals( ReasonStyler.STYLER_UNDEFINED)) {
				undefinedValueInExpression = true;
				break;
			}
		}
		if (undefinedValueInExpression && reason.getText() != null) {
			String txt = reason.getText();
			Styler styler = stylers.styler(ReasonStyler.STYLER_STANDARD);
			StyledString styledString = new StyledString(txt);
			styledString.setStyle(0, styledString.length(), styler);
			return styledString;
		}
		
		List<StyledTextSequenceData> sequences = new ArrayList<>( stylingData.size());
		StringBuilder sb = new StringBuilder();
		int s = 0,e = 0;
	
		for (Pair<String,String> data : stylingData) {
		
			// get the text
			String text = data.first;
			sb.append( text);
			
			// specify end of text
			e = e+text.length();
		
			// get the styler lazyly 
			String stylerId = data.second;					
			Styler styler = stylers.styler(stylerId);
			if (styler == null) {	
				styler = createStyler(stylerId, null);
			}
			StyledTextSequenceData sequence = new StyledTextSequenceData(s, e, styler);
			sequences.add(sequence);
			// reposition 
			s = e;
		}		

		String text = sb.toString();
		if (suppressTextPrefix) {
			StyledString styledString = new StyledString(text);
			StyledTextHandler.applyRanges(styledString, sequences);
			return styledString;
		}
		else {
			if (reason instanceof Origination) {
				String extendedInfomark = infoMark + " ";		
				StyledString styledString = mergeStyledStringWithPrefix(stylers, sequences, text, extendedInfomark, ReasonLevel.info);
				return styledString;	
			}
			else if (reason instanceof ProjectNonPerfectMatch) {
				String extendedInfomark = warnMark + " ";		
				StyledString styledString = mergeStyledStringWithPrefix(stylers, sequences, text, extendedInfomark, ReasonLevel.warning);
				return styledString;
			}
			else {
				String extendedInfomark = errorMark + " ";		
				StyledString styledString = mergeStyledStringWithPrefix(stylers, sequences, text, extendedInfomark, ReasonLevel.error);			
				return styledString;
			}
		}
	}

	private StyledString mergeStyledStringWithPrefix(Stylers stylers, List<StyledTextSequenceData> sequences, String text, String prefix, ReasonLevel level) {
		text = prefix + text;
		StyledString styledString = new StyledString(text);
		
		sequences.stream().forEach( ss -> {
			ss.start = ss.start + prefix.length();
			ss.end = ss.end + prefix.length();				
		});
		
		List<StyledTextSequenceData> mergedSequences = new ArrayList<>( sequences.size() + 1);
		Styler styler;
		
		switch (level) {
			case error:
				styler = stylers.styler( ReasonStyler.STYLER_ERROR);
				if (styler == null) {	
					styler = createStyler( ReasonStyler.STYLER_ERROR, COLOR_ERROR);
				}
				break;
			case warning:
				styler = stylers.styler( ReasonStyler.STYLER_WARNING);
				if (styler == null) {	
					styler = createStyler( ReasonStyler.STYLER_WARNING, COLOR_WARNING);
				}
				break;
			default: {
				styler = stylers.styler( ReasonStyler.STYLER_EMPHASIS);
				if (styler == null) {	
					styler = createStyler( ReasonStyler.STYLER_EMPHASIS, null);
				}		
				break;
			}
		}
				
		mergedSequences.add( new StyledTextSequenceData(0, prefix.length(), styler));
		mergedSequences.addAll(sequences);
		StyledTextHandler.applyRanges(styledString, mergedSequences);
		return styledString;
	}

	/**
	 * @return - a {@link mapExpr_return} of style-id (from {@link ReasonStyler} and the actual styler
	 */
	private Map<String,String> buildStyleMap() {
		Map<String,String> map = new HashMap<>();
		
		Lists.list( //
				ReasonStyler.STYLER_EMPHASIS, //
				ReasonStyler.STYLER_ARTIFACTID, //
				ReasonStyler.STYLER_GROUPID, //
				ReasonStyler.STYLER_VERSION, // 
				ReasonStyler.STYLER_PART_CLASSIFIER, //
				ReasonStyler.STYLER_PART_TYPE, //
				ReasonStyler.STYLER_ERROR, //
				ReasonStyler.STYLER_WARNING //
 			) 
			.stream() //
				.forEach( s -> map.put(s, "bold"));
		
		return map;
	}

	/**
	 * acquire a {@link Styler} of the given id
	 * @param id - the id of the {@link Styler}
	 * @return
	 */
	private Styler createStyler(String id, Integer color) {		
		String stylerId = coalescedStyles.get().get(id);
		if (stylerId == null)
			stylerId = defaultStylerId;
				
		Stylers stylers = uiSupport.stylers( REASON_STYLERS);		
		switch (stylerId) {
			case "bold":			
				return stylers.addStyler( id, SWT.BOLD, color, null, null);	
			default:			
				return stylers.addStyler( id, null, color, null, null);				
		}		
	}
}
