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
package com.braintribe.devrock.zed.ui.viewer.model;

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
import com.braintribe.devrock.zarathud.model.common.EntityNode;
import com.braintribe.devrock.zarathud.model.common.FieldNode;
import com.braintribe.devrock.zarathud.model.common.FingerPrintCoalescedNode;
import com.braintribe.devrock.zarathud.model.common.FingerPrintNode;
import com.braintribe.devrock.zarathud.model.common.FingerPrintRating;
import com.braintribe.devrock.zarathud.model.common.MethodNode;
import com.braintribe.devrock.zarathud.model.common.Node;
import com.braintribe.devrock.zarathud.model.model.EnumEntityNode;
import com.braintribe.devrock.zarathud.model.model.GenericEntityNode;
import com.braintribe.devrock.zarathud.model.model.PropertyNode;
import com.braintribe.utils.lcd.LazyInitialized;

public class ViewLabelProvider extends CellLabelProvider implements IStyledLabelProvider {
	private static final String PROPERTY_TO_TYPE_DELEMITER = " \u21E2 ";

	private static final String STYLER_STANDARD = "standard";
	private static final String STYLER_EMPHASIS = "emphasis";
	
	//private static final String METHOD_TO_ARGUMENT_TYPE_DELIMITER = "\u2192";
	private static final String METHOD_TO_RETURNTYPE_DELIMITER = "\u2190";
	
	private static final String PREFIX_FINGERPRINT_ERROR = "\u24BA" +" ";
	private static final String STYLER_ERROR = "error";

	private static final String PREFIX_FINGERPRINT_WARNING = "\u24CC" +" ";
	private static final String STYLER_WARNING = "warning";
	
	private static final String PREFIX_FINGERPRINT_INFO = "\u24BE" +" ";
	private static final String STYLER_INFO = "info";
	
	
	
	
	private static final String PREFIX_ENTITY = "\u24A0" +" ";
	private Image entityImage; 
	private static final String PREFIX_ENUM = "\u24A9="  +" ";
	private Image enumImage;
	private static final String PREFIX_METHOD = "\u24A8" +" ";
	private Image methodImage;
	private static final String PREFIX_FIELD = "\u24A1" +" ";;
	private Image fieldImage;
	private static final String PREFIX_PROPERTY = "\u24AB" +" ";
	private Image propertyImage;
	
	
	private UiSupport uiSupport;
	private String uiSupportStylersKey;
	
	private LazyInitialized<Map<String, Styler>> stylerMap = new LazyInitialized<>( this::setupUiSupport);
	private Image fingerPrintWarning;
	private Image fingerPrintError;
	private Image fingerPrintPassed;
	private Image fingerPrintInfo;
	
	private boolean showUnicodePrefixes = false;
	private StyledTextHandler styledStringStyler;
	{
		styledStringStyler = new StyledTextHandler();
		styledStringStyler.setStylerMapSupplier(stylerMap);
		styledStringStyler.setDelimiterStyleSupplier( () -> STYLER_STANDARD);
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
		
		//
		entityImage = uiSupport.images().addImage("ge_entity", ViewLabelProvider.class, "int_obj.png");
		enumImage = uiSupport.images().addImage("ge_enum", ViewLabelProvider.class, "enum_obj.png");
		propertyImage = uiSupport.images().addImage("ge_property", ViewLabelProvider.class, "property.png");
		fieldImage = uiSupport.images().addImage("ge_field", ViewLabelProvider.class, "field_public_obj.png");
		methodImage = uiSupport.images().addImage("ge_method", ViewLabelProvider.class, "methpub_obj.png"); 
		
		Stylers stylers = uiSupport.stylers(uiSupportStylersKey);
		stylers.addStandardStylers();
		
		Map<String,Styler> map = new HashMap<>();
		map.put(STYLER_EMPHASIS, stylers.standardStyler(Stylers.KEY_BOLD));
		map.put(STYLER_STANDARD, stylers.standardStyler(Stylers.KEY_DEFAULT));
		map.put(STYLER_ERROR, stylers.addStyler(STYLER_ERROR, null, SWT.COLOR_DARK_RED, null, null));
		map.put(STYLER_WARNING, stylers.addStyler(STYLER_WARNING, null, SWT.COLOR_DARK_YELLOW, null, null));
		map.put(STYLER_INFO, stylers.addStyler(STYLER_INFO, null, SWT.COLOR_DARK_GREEN, null, null));
		return map;
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
		default:
		case ok:
			return fingerPrintPassed;			
		}		
	}
	
	/**
	 * returns the prefix {@link String} associated with the {@link FingerPrintRating}
	 * @param rating
	 * @return
	 */
	private String getPrefixForRating( FingerPrintRating rating) {
		// trigger lazy if required - for the pictures 
		stylerMap.get();
		
		switch (rating) {
		case error:
			return PREFIX_FINGERPRINT_ERROR;				
		case warning:
			return PREFIX_FINGERPRINT_WARNING;
		case info:
		case ok:
		default:
			return PREFIX_FINGERPRINT_INFO;			
		}		
	}
	
	/**
	 * returns the styler id for the {@link FingerPrintRating}
	 * @param rating
	 * @return
	 */
	private String getStylerForRating( FingerPrintRating rating) {
		// trigger lazy if required - for the pictures 
		stylerMap.get();
		
		switch (rating) {
		case error:
			return STYLER_ERROR;				
		case warning:
			return STYLER_WARNING;
		case info:
		case ok:
		default:
			return STYLER_INFO;			
		}		
	}


	@Override
	public Image getImage(Object obj) {
		// trigger lazy if required - for the pictures 
		stylerMap.get();
		
		if (showUnicodePrefixes)
			return null;	
		
		if (obj instanceof FingerPrintNode) {
			FingerPrintNode fpn = (FingerPrintNode) obj;
			FingerPrintRating rating = fpn.getRating();
			if (rating != null) {
				return getImageForRating( rating);
			}
			else {
				return fingerPrintInfo;
			}
		}
		else if (obj instanceof FingerPrintCoalescedNode) {
			FingerPrintCoalescedNode fpn = (FingerPrintCoalescedNode) obj;
			FingerPrintRating overallRating = fpn.getWorstFingerPrintRating();
			if (overallRating != null) {
				return getImageForRating( overallRating);
			}
			else {
				return fingerPrintInfo;
			}
		}				
		else if (obj instanceof EntityNode) {
			Image ratingImage = determineRatingImage( obj);
			if (ratingImage != null)
				return ratingImage;
			return entityImage;
		}
		else if (obj instanceof GenericEntityNode) {
			Image ratingImage = determineRatingImage( obj);
			if (ratingImage != null)
				return ratingImage;
			return entityImage;
		}
		else if (obj instanceof EnumEntityNode) {
			Image ratingImage = determineRatingImage( obj);
			if (ratingImage != null)
				return ratingImage;
			return enumImage;
		}
		else if (obj instanceof PropertyNode) {
			Image ratingImage = determineRatingImage( obj);
			if (ratingImage != null)
				return ratingImage;
			return propertyImage;
		}
		else if (obj instanceof MethodNode) {
			Image ratingImage = determineRatingImage( obj);
			if (ratingImage != null)
				return ratingImage;
			return methodImage;
		}
		else if (obj instanceof FieldNode) {
			Image ratingImage = determineRatingImage( obj);
			if (ratingImage != null)
				return ratingImage;
			return fieldImage;
		}
		return null;
	}
	
	

	private Image determineRatingImage(Object obj) {
		Node node = (Node) obj;
		FingerPrintRating worstFingerPrintRating = node.getWorstFingerPrintRating();
		if (worstFingerPrintRating == null) { 
			return null;
		}
		return getImageForRating(worstFingerPrintRating);		
	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof GenericEntityNode) {
			GenericEntityNode gn = (GenericEntityNode) element;
			List<Node> children = gn.getChildren();
			String text = "analysis has found (" + children.size() + ") classes of issues for : " + gn.getName();
			return text;
		}
		else if (element instanceof EnumEntityNode) {
			EnumEntityNode gn = (EnumEntityNode) element;
			List<Node> children = gn.getChildren();
			String text = "analysis has found (" + children.size() + ") classes of issues for : " + gn.getName();
			return text;
		}
		else if (element instanceof FingerPrintCoalescedNode) {
			FingerPrintCoalescedNode fpcn = (FingerPrintCoalescedNode) element;
			FingerPrintRating overallRating = fpcn.getWorstFingerPrintRating();
			String text = "Issue " + fpcn.getMessage() + " is currently rated as :" + overallRating.name();
			return text;
		}
		else if (element instanceof EntityNode) {
			EntityNode en = (EntityNode) element;
			List<Node> children = en.getChildren();
			String text = "analysis has found (" + children.size() + ") classes of issues for : " + en.getName();
			return text;
		}
	 	
		return super.getToolTipText(element);
	}

	@Override
	public StyledString getStyledText(Object obj) {
		
		Pair<String, List<StyledTextSequenceData>> stylePair = null;
		if (obj instanceof GenericEntityNode) { 
			GenericEntityNode node = (GenericEntityNode) obj;
			stylePair = getStyledTextForGenericEntity(node);			
		}
		else if (obj instanceof PropertyNode) {
			PropertyNode node = (PropertyNode) obj;
			stylePair = getStyledTextForProperty(node);
		}
		else if (obj instanceof FingerPrintCoalescedNode) {
			FingerPrintCoalescedNode node = (FingerPrintCoalescedNode) obj;
			stylePair = getStyledTextForCoalescedFingerPrint(node);
		}		
		else if (obj instanceof MethodNode) {
			MethodNode node = (MethodNode) obj;
			stylePair = getStyledTextForMethod( node);
		}
		else if (obj instanceof EnumEntityNode) {
			EnumEntityNode node = (EnumEntityNode) obj;		
			stylePair = getStyledTextForEnum( node);			
		}
		else if (obj instanceof FieldNode) {
			FieldNode node = (FieldNode) obj;
			stylePair = getStyledTextForField( node);
		}
		else if (obj instanceof EntityNode) {
			 EntityNode node = (EntityNode) obj;
			 stylePair = getStyledTextForEntity( node);
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
	
	private Pair<String, List<StyledTextSequenceData>> getStyledTextForEntity(EntityNode node) {
		if (!showUnicodePrefixes) {
			return styledStringStyler.build( node.getName(), STYLER_EMPHASIS);
		}
		else {
			List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();
			if (showUnicodePrefixes) {
				pairs.add( styledStringStyler.build( PREFIX_ENTITY, STYLER_STANDARD));
			}
			pairs.add( styledStringStyler.build(node.getName(), STYLER_EMPHASIS));
			return styledStringStyler.merge(pairs);
		}				
	}

	private Pair<String, List<StyledTextSequenceData>> getStyledTextForEnum(EnumEntityNode node) {
		if (!showUnicodePrefixes) {
			return styledStringStyler.build( node.getName(), STYLER_EMPHASIS);
		}
		else {
			List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();
			if (showUnicodePrefixes) {
				pairs.add( styledStringStyler.build( PREFIX_ENUM, STYLER_STANDARD));
			}
			pairs.add( styledStringStyler.build(node.getName(), STYLER_EMPHASIS));
			return styledStringStyler.merge(pairs);
		}		
	}

	/**
	 * builds the styled text data for a {@link GenericEntityNode}
	 * @param node
	 * @return - a {@link Pair} of {@link String} with associated {@link StyledTextSequenceData}
	 */
	private Pair<String, List<StyledTextSequenceData>> getStyledTextForGenericEntity( GenericEntityNode node) {
		if (!showUnicodePrefixes) {
			return styledStringStyler.build( node.getName(), STYLER_EMPHASIS);
		}
		else {
			List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();
			if (showUnicodePrefixes) {
				pairs.add( styledStringStyler.build( PREFIX_ENTITY, STYLER_STANDARD));
			}
			pairs.add( styledStringStyler.build(node.getName(), STYLER_EMPHASIS));
			return styledStringStyler.merge(pairs);
		}				
	}
	
	/**
	 * @param node
	 * @return - a {@link Pair} of {@link String} with associated {@link StyledTextSequenceData}
	 */
	private Pair<String, List<StyledTextSequenceData>> getStyledTextForProperty( PropertyNode node) {
		List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();
		if (showUnicodePrefixes) {
			pairs.add( styledStringStyler.build( PREFIX_PROPERTY, STYLER_STANDARD));
		}
		pairs.add( styledStringStyler.build(node.getName(), STYLER_EMPHASIS));
		pairs.add( styledStringStyler.build(PROPERTY_TO_TYPE_DELEMITER, STYLER_STANDARD));
		pairs.add( getStyledTextForType( node.getType()));
		return styledStringStyler.merge(pairs);
	}
	
	private Pair<String, List<StyledTextSequenceData>> getStyledTextForField( FieldNode node) {
		List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();
		if (showUnicodePrefixes) {
			pairs.add( styledStringStyler.build( PREFIX_FIELD, STYLER_STANDARD));
		}
		pairs.add( styledStringStyler.build(node.getName(), STYLER_EMPHASIS));
		pairs.add( styledStringStyler.build(PROPERTY_TO_TYPE_DELEMITER, STYLER_STANDARD));
		pairs.add( getStyledTextForType( node.getType()));
		return styledStringStyler.merge(pairs);
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
	
	/**
	 * builds the styled text data for a {@link FingerPrintNode}
	 * @param node - the {@link FingerPrintNode}
	 * @return - a {@link Pair} of {@link String} with associated {@link StyledTextSequenceData}
	 */
	private Pair<String, List<StyledTextSequenceData>> getStyledTextForCoalescedFingerPrint( FingerPrintCoalescedNode node) {
		String txt = node.getMessage();
		if (!showUnicodePrefixes) {
			return styledStringStyler.build( Pair.of(txt, STYLER_STANDARD));
		}
		else {
			List<Pair<String, List<StyledTextSequenceData>>> pairs = new ArrayList<>();
			String prefix = getPrefixForRating( node.getWorstFingerPrintRating());
			String styler = getStylerForRating( node.getWorstFingerPrintRating());
			pairs.add( styledStringStyler.build(Pair.of(prefix, styler)));
			pairs.add(styledStringStyler.build( Pair.of(txt, STYLER_EMPHASIS)));
			return styledStringStyler.merge(pairs);
		}
	}
	/**
	 * builds the styled text data for a {@link MethodNode}
	 * @param node
	 * @return - a {@link Pair} of {@link String} with associated {@link StyledTextSequenceData}
	 */
	private Pair<String, List<StyledTextSequenceData>> getStyledTextForMethod(MethodNode node) {
		// java notation ?
		
		List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();
		
		if (showUnicodePrefixes) {
			pairs.add( styledStringStyler.build( Pair.of( PREFIX_METHOD, STYLER_STANDARD)));
		}
		
		Pair<String,List<StyledTextSequenceData>> styledTextReturnType = getStyledTextForType( node.getReturnType().getName());
		pairs.add(styledTextReturnType);
		
		
		
		List<Pair<String,String>> tokens = new ArrayList<>();
		tokens.add( Pair.of( " " + METHOD_TO_RETURNTYPE_DELIMITER + " ", STYLER_STANDARD));
		tokens.add( Pair.of(node.getName(), STYLER_EMPHASIS));
		
		List<EntityNode> parameterTypes = node.getParameterTypes();
		if (parameterTypes.size() > 0) {
			tokens.add( Pair.of( " " + METHOD_TO_RETURNTYPE_DELIMITER +" ", STYLER_STANDARD));
		}		
		Pair<String, List<StyledTextSequenceData>> styledTextName = styledStringStyler.build( tokens);
		pairs.add(styledTextName);
		
		if (parameterTypes.size() > 0) {
			List<Pair<String,List<StyledTextSequenceData>>> temp = new ArrayList<>();
			for (EntityNode en : parameterTypes) {
				Pair<String,List<StyledTextSequenceData>> styledTextParamType = getStyledTextForType( en.getName());
				temp.add(styledTextParamType);			
			}
			pairs.add( styledStringStyler.merge( temp, ","));
		}
		
		Pair<String, List<StyledTextSequenceData>> result = styledStringStyler.merge(pairs);				
		return result;
	}
	

	@Override
	public void update(ViewerCell arg0) {	
	}
	
	
}
