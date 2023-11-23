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
package com.braintribe.devrock.zed.ui.viewer.classpath;

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
import com.braintribe.devrock.zarathud.model.classpath.ClasspathDuplicateNode;
import com.braintribe.devrock.zarathud.model.common.ArtifactNode;
import com.braintribe.devrock.zarathud.model.common.FingerPrintRating;
import com.braintribe.devrock.zarathud.model.common.Node;
import com.braintribe.utils.lcd.LazyInitialized;

public class ViewLabelProvider extends CellLabelProvider implements IStyledLabelProvider {
	
	// link to unicode : https://unicode-explorer.com/lists
	
	//private boolean showUnicodePrefixes = false;
	
	private static final String STYLER_STANDARD = "standard";
	private static final String STYLER_EMPHASIS = "emphasis";
		
	private static final String STYLER_ERROR = "error";
	
	private static final String REFERENCE_DELIMITER = " \u21E2 ";
	
	private Image artifactImage;
	

	private UiSupport uiSupport;
	private String uiSupportStylersKey;
	
	private LazyInitialized<Map<String, Styler>> stylerMap = new LazyInitialized<>( this::setupUiSupport);

	private Image fingerPrintError;
	
	private StyledTextHandler styledStringStyler;

	private Image fingerPrintWarning;

	private Image fingerPrintPassed;

	private Image fingerPrintInfo;
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
		
	/**
	 * lazy initializer for the ui stuff 
	 * @return
	 */
	private Map<String,Styler> setupUiSupport() {		
						
		fingerPrintWarning = uiSupport.images().addImage("fp_warning", ViewLabelProvider.class, "testwarn.png");
		fingerPrintError = uiSupport.images().addImage("fp_error", ViewLabelProvider.class, "testerr.png");
		fingerPrintPassed = uiSupport.images().addImage("fp_passed", ViewLabelProvider.class, "testok.png");
		fingerPrintInfo = uiSupport.images().addImage("fp_info", ViewLabelProvider.class, "testignored.png");
		
		artifactImage = uiSupport.images().addImage("ge_entity", ViewLabelProvider.class, "solution.jar.gif");
		
		Stylers stylers = uiSupport.stylers(uiSupportStylersKey);
		stylers.addStandardStylers();
		
		Map<String,Styler> map = new HashMap<>();
		map.put(STYLER_EMPHASIS, stylers.standardStyler(Stylers.KEY_BOLD));
		map.put(STYLER_STANDARD, stylers.standardStyler(Stylers.KEY_DEFAULT));
		map.put(STYLER_ERROR, stylers.addStyler(STYLER_ERROR, null, SWT.COLOR_DARK_RED, null, null));		
		return map;
	}
	
	@Override
	public Image getImage(Object obj) {
		// trigger lazy if required - for the pictures 
		stylerMap.get();
		
		if (obj instanceof ArtifactNode) {
			return artifactImage;
		}
		else if (obj instanceof ClasspathDuplicateNode) {
			ClasspathDuplicateNode node = (ClasspathDuplicateNode) obj;
			FingerPrintRating rating = node.getRating();			
			return getImageForRating(rating);
		}
		return null;
	}
	
	/**
	 * returns the image associated with the {@link FingerPrintRating}
	 * @param rating
	 * @return
	 */
	private Image getImageForRating( FingerPrintRating rating) {
		// trigger lazy if required - for the pictures 
		stylerMap.get();
		
		switch (rating) {
		case error:
			return fingerPrintError;				
		case warning:
			return fingerPrintWarning;
		case info:
			return fingerPrintInfo;
		case ok:
		default:
			return fingerPrintPassed;			
		}		
	}
	
	@Override
	public StyledString getStyledText(Object arg0) {
		Node node = (Node) arg0;
		Pair<String, List<StyledTextSequenceData>> stylePair = null;
		
		if (node instanceof ArtifactNode) {
			ArtifactNode artifactNode = (ArtifactNode) node;
			stylePair = getStyledTextForArtifact( artifactNode);
		}
		else if (node instanceof ClasspathDuplicateNode) {
			ClasspathDuplicateNode entityNode = (ClasspathDuplicateNode) node;
			stylePair = getStyledTextForClasspathDuplicateNode( entityNode);
		}		
		if (stylePair != null) {
			List<StyledTextSequenceData> sequences = stylePair.second;			
			StyledString st = new StyledString( stylePair.first);
			if (sequences != null) {
				StyledTextHandler.applyRanges(st, sequences);
			}
			return st;
		}		
		return new StyledString();		
	}	

	private Pair<String, List<StyledTextSequenceData>> getStyledTextForClasspathDuplicateNode(ClasspathDuplicateNode cpDuplicateNode) {
		List<Pair<String, List<StyledTextSequenceData>>> pairs = new ArrayList<>();
				
		
		pairs.add( getStyledTextForType( cpDuplicateNode.getReferencingType().getName()));
		pairs.add( styledStringStyler.build(REFERENCE_DELIMITER, STYLER_STANDARD));
		pairs.add( getStyledTextForType( cpDuplicateNode.getDuplicateType().getName()));
				
		return styledStringStyler.merge(pairs);					
	}

	private Pair<String, List<StyledTextSequenceData>> getStyledTextForArtifact(ArtifactNode artifactNode) {
		String txt = artifactNode.getIdentification().asString();
		return styledStringStyler.build( Pair.of(txt, STYLER_STANDARD));				
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
	public void update(ViewerCell arg0) {	
	}
	
	
}
