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
package com.braintribe.devrock.zed.ui.viewer.dependencies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.fonts.StyledTextHandler;
import com.braintribe.devrock.api.ui.fonts.StyledTextSequenceData;
import com.braintribe.devrock.api.ui.stylers.Stylers;
import com.braintribe.devrock.zarathud.model.common.FingerPrintRating;
import com.braintribe.devrock.zarathud.model.common.ReferenceNode;
import com.braintribe.devrock.zarathud.model.dependency.DependencyAnalysisNode;
import com.braintribe.devrock.zarathud.model.dependency.DependencyKind;
import com.braintribe.utils.lcd.LazyInitialized;

public class ViewLabelProvider extends CellLabelProvider implements IStyledLabelProvider {
	private static final String STYLER_EMPHASIS = "emphasis";
	private static final String STYLER_STANDARD = "standard";
	private static final String STYLER_FORWARD = "forward";
	private static final String STYLER_EXCESS = "excess";
	private static final String STYLER_CONFIRMED = "confirmed";
	private static final String STYLER_MISSING = "missing";	
	private static final String STYLER_REDACTED = "redacted";
	
	private static final String STYLER_OVERRIDE_ERROR = "override_error";
	private static final String STYLER_OVERRIDE_WARN = "override_warn";
	private static final String STYLER_OVERRIDE_OK = "override_ok";
	private static final String STYLER_OVERRIDE_INFO = "override_info";
	private static final String STYLER_OVERRIDE_IGNORE = "override_ignore";
	
	private static final String IMAGE_KEY_EXCESS_REDACTED = "excess_redacted";
	private static final String IMAGE_KEY_MISSING_REDACTED = "missing_redacted";	
	
	private static final String REFERENCE_DELIMITER = " \u21E2 ";
	
	private static final String SYMBOL_ERROR = " \u24BA ";
	private static final String SYMBOL_WARNING = " \u24CC ";
	private static final String SYMBOL_INFO = " \u24BE ";
	private static final String SYMBOL_IGNORE = " \u24D8 ";
	private static final String SYMBOL_OK = " \u24C4 ";
	
	
	private UiSupport uiSupport;
	private String uiSupportStylersKey;
	
	private LazyInitialized<Map<String, Styler>> stylerMap = new LazyInitialized<>( this::setupUiSupport);
	
	private Image confirmedImage;
	private Image missingImage;
	private Image excessImage;
	private Image forwardImage;
	private Image redactedExcessImage;
	private Image redactedMissingImage;
	
	private StyledTextHandler styledStringStyler;
	{
		styledStringStyler = new StyledTextHandler();
		styledStringStyler.setStylerMapSupplier(stylerMap);
		styledStringStyler.setDelimiterStyleSupplier( () -> Stylers.KEY_DEFAULT);
	}
	
	@Configurable
	public void setUiSupport(UiSupport uiSupport) {
		this.uiSupport = uiSupport;					
	}
	
	@Configurable
	public void setUiSupportStylersKey(String uiSupportStylersKey) {
		this.uiSupportStylersKey = uiSupportStylersKey;
	}
		
	private Map<String,Styler> setupUiSupport() {		
		
		confirmedImage = uiSupport.images().addImage(STYLER_CONFIRMED, ViewLabelProvider.class, "passed.png");
		missingImage = uiSupport.images().addImage(STYLER_MISSING, ViewLabelProvider.class, "add.gif");
		excessImage = uiSupport.images().addImage(STYLER_EXCESS, ViewLabelProvider.class, "remove.gif");
		forwardImage = uiSupport.images().addImage(STYLER_FORWARD, ViewLabelProvider.class, "filter_obj.gif");
		redactedMissingImage = uiSupport.images().addImage(IMAGE_KEY_MISSING_REDACTED, ViewLabelProvider.class, "add_redacted.gif");
		redactedExcessImage = uiSupport.images().addImage(IMAGE_KEY_EXCESS_REDACTED, ViewLabelProvider.class, "remove_redacted.gif");
		
		
		Stylers stylers = uiSupport.stylers(uiSupportStylersKey);
		stylers.addStandardStylers();
		Map<String,Styler> map = new HashMap<>();
	
		map.put(STYLER_MISSING, stylers.standardStyler(Stylers.KEY_BOLD));
		map.put(STYLER_CONFIRMED, stylers.standardStyler(Stylers.KEY_DEFAULT));
		map.put(STYLER_EXCESS, stylers.addStyler(STYLER_EXCESS, null, SWT.COLOR_DARK_GRAY, null, null));
		map.put(STYLER_FORWARD, stylers.addStyler(STYLER_FORWARD, null, SWT.COLOR_GRAY, null, null));
		map.put(STYLER_STANDARD, stylers.standardStyler(Stylers.KEY_DEFAULT));
		map.put(STYLER_EMPHASIS, stylers.standardStyler(Stylers.KEY_BOLD));
		map.put(STYLER_REDACTED, stylers.addStyler(STYLER_REDACTED, SWT.BOLD, SWT.COLOR_GRAY, null, null));
		
		map.put(STYLER_OVERRIDE_ERROR, stylers.addStyler(STYLER_OVERRIDE_ERROR, SWT.BOLD, SWT.COLOR_DARK_RED, null, null));
		map.put(STYLER_OVERRIDE_WARN, stylers.addStyler(STYLER_OVERRIDE_WARN, SWT.BOLD, SWT.COLOR_DARK_YELLOW, null, null));
		map.put(STYLER_OVERRIDE_OK, stylers.addStyler(STYLER_OVERRIDE_OK, SWT.BOLD, SWT.COLOR_DARK_GREEN, null, null));
		map.put(STYLER_OVERRIDE_IGNORE, stylers.addStyler(STYLER_OVERRIDE_IGNORE, null, SWT.COLOR_DARK_GRAY, null, null));
		map.put(STYLER_OVERRIDE_INFO, stylers.addStyler(STYLER_OVERRIDE_INFO, null, SWT.COLOR_DARK_BLUE, null, null));
		
		
		return map;
	}

	@Override
	public Image getImage(Object obj) {
		if (obj instanceof DependencyAnalysisNode) { 
			DependencyAnalysisNode node = (DependencyAnalysisNode) obj;
			switch (node.getKind()) {
				case confirmed:
					return confirmedImage;				
				case excess:
					if (node.getRedacted()) {
						return redactedExcessImage;
					}
					else if (!testForRelevance(node)) {
						return redactedExcessImage;
					}
					return excessImage;
					
				case missing:
					if (node.getRedacted()) {
						return redactedMissingImage;
					}
					else if (!testForRelevance(node)) {
						return redactedMissingImage;								
					}
					return missingImage;
				case forward:
					return forwardImage;
				default:
					break;			
			}
		}
		return null;
	}
	
	

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof DependencyAnalysisNode) {
			DependencyAnalysisNode node = (DependencyAnalysisNode) element;
			
			String text;
			if (node.getRedacted()) {
				text = "redacted: " + node.getIdentification().asString();
			}
			else if (node.getOverridden()){
				FingerPrintRating rating = node.getRating();				
				text = "overridden to " + rating.name() + ":" + node.getIdentification().asString();  
			}
			else {
				text = node.getIdentification().asString();
			}
						
			String suffix = null;
			String referenceCount = null;
			switch (node.getKind()) {
				case confirmed:
					suffix = STYLER_CONFIRMED;
					break;
				case excess:
					suffix = STYLER_EXCESS;
					break;
				case missing:
					suffix = STYLER_MISSING;
					referenceCount = "" + node.getReferences().size();
					break;
				case forward:
					if (node.getIncompleteForwardReference()) {
						suffix = "forwarded (missing)";
					}
					else {
						suffix = "forwarded";
					}
				default:
					break;			
			}
			
			if (suffix != null) {
				if (referenceCount != null) {
					text = text + " (" + suffix + ", number of references : " + referenceCount + ")";
				}
				else {
					text = text + " (" + suffix + ")";
				}
				if (!testForRelevance(node)) {
					return text + "\n is accepted as is";
				}
				else if (node.getKind() == DependencyKind.missing || (node.getKind() == DependencyKind.forward && node.getIncompleteForwardReference())) {
					return text + "\n should be added to pom"; 
				}
				else if (node.getKind() == DependencyKind.excess) {
					return text + "\n can be removed from pom";
				}
				
				return text;
			}
			else {
				return text;
			}
			
		}
		else if (element instanceof ReferenceNode) {
			ReferenceNode node = (ReferenceNode) element;
			String text = node.getSource().getName() + REFERENCE_DELIMITER + node.getTarget().getName();
			int count = node.getCount();
			if (count > 1) {
				text += " (" + count + ")";
			}
			return text;
		}
		return super.getToolTipText(element);
	}

	@Override
	public StyledString getStyledText(Object obj) {
		if (obj instanceof DependencyAnalysisNode) { 
			DependencyAnalysisNode node = (DependencyAnalysisNode) obj;
			String stylerKey = null;
			if (node.getRedacted()) {
				stylerKey = STYLER_REDACTED;
			}
			else {
				switch (node.getKind()) {
					case confirmed:
						stylerKey = STYLER_CONFIRMED;
						break;
					case excess:					
						stylerKey = STYLER_EXCESS;
						break;
					case missing:
						stylerKey = STYLER_MISSING;
						break;
					case forward:
						if (node.getIncompleteForwardReference()) {
							stylerKey = STYLER_EMPHASIS;
						}
						else {
							stylerKey = STYLER_FORWARD;
						}
						break;
					default:
						stylerKey = STYLER_STANDARD;
						break;			
				}	
			}
			List<Pair<String, List<StyledTextSequenceData>>> stylePairs = new ArrayList<>();
			// override symbol in color 
			if (node.getOverridden()) {
				String prefix = null;
				FingerPrintRating rating = node.getRating();
				switch (rating) {
					case error:
						prefix = SYMBOL_ERROR;
						stylePairs.add( styledStringStyler.build(prefix, STYLER_OVERRIDE_ERROR));
						break;
					case info:
						prefix = SYMBOL_INFO;
						stylePairs.add( styledStringStyler.build(prefix, STYLER_OVERRIDE_INFO));
						break;
					case warning:
						prefix = SYMBOL_WARNING;
						stylePairs.add( styledStringStyler.build(prefix, STYLER_OVERRIDE_WARN));
						break;
					case ok:
						prefix = SYMBOL_OK;
						stylePairs.add( styledStringStyler.build(prefix, STYLER_OVERRIDE_OK));
						break;
					case ignore:
						prefix = SYMBOL_IGNORE;
						stylePairs.add( styledStringStyler.build(prefix, STYLER_OVERRIDE_IGNORE));
						break;
					default:
						break;				
				}
			}
			// dependency name 
			stylePairs.add( styledStringStyler.build( node.getIdentification().asString(), stylerKey));		
			Pair<String,List<StyledTextSequenceData>> merged = styledStringStyler.merge(stylePairs);
			return StyledTextHandler.applyRanges(merged); 			
						
		}
		else if (obj instanceof ReferenceNode){
			ReferenceNode node = (ReferenceNode) obj;
			int count = node.getCount();
			
			List<Pair<String, List<StyledTextSequenceData>>> pairs = new ArrayList<>();
			pairs.add(getStyledTextForType(node.getSource().getName()));	
			pairs.add( styledStringStyler.build( REFERENCE_DELIMITER, STYLER_STANDARD));
			pairs.add(getStyledTextForType(node.getTarget().getName()));
		
			if (count > 1) {
				pairs.add( styledStringStyler.build(" (" + count + ")", STYLER_STANDARD));
				
			}			
			Pair<String,List<StyledTextSequenceData>> merged = styledStringStyler.merge(pairs);
			return StyledTextHandler.applyRanges(merged);
		}
		return null;
	}

	@Override
	public void update(ViewerCell arg0) {	
	}
	
	/**
	 * builds the styled text data for a type (in form of a {@link String}
	 * @param type - the fully qualified type as a {@link String}
	 * @return - a {@link Pair} of {@link String} with associated {@link StyledTextSequenceData}
	 */
	private Pair<String, List<StyledTextSequenceData>> getStyledTextForType( String type) {
		String [] split = type.split("\\.");
		List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();
		for (int i = 0; i < split.length - 1; i++) {
			pairs.add( styledStringStyler.build(split[i], STYLER_STANDARD));
		}
		pairs.add( styledStringStyler.build( split[ split.length -1], STYLER_EMPHASIS));
		return styledStringStyler.merge(pairs, ".");
	}

	@Override
	public void dispose() {
		
		super.dispose();
	}
	
	private boolean testForRelevance(DependencyAnalysisNode node) {
		FingerPrintRating rating = node.getRating();
		boolean ignore = rating == FingerPrintRating.ok || rating == FingerPrintRating.ignore;
		return !ignore;		
	}

}
