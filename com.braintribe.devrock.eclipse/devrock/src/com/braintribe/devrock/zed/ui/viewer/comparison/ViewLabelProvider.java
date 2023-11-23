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
package com.braintribe.devrock.zed.ui.viewer.comparison;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.fonts.StyledTextHandler;
import com.braintribe.devrock.api.ui.fonts.StyledTextSequenceData;
import com.braintribe.devrock.api.ui.stylers.Stylers;
import com.braintribe.devrock.zarathud.model.common.FingerPrintNode;
import com.braintribe.devrock.zarathud.model.common.FingerPrintRating;
import com.braintribe.devrock.zarathud.model.extraction.subs.ContainerNode;
import com.braintribe.devrock.zed.forensics.fingerprint.HasFingerPrintTokens;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.ClassEntity;
import com.braintribe.zarathud.model.data.EnumEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.forensics.FingerPrint;

public class ViewLabelProvider extends CellLabelProvider implements IStyledLabelProvider, HasFingerPrintTokens {
	private static final String STYLER_STANDARD = "standard";
	private static final String STYLER_EMPHASIS = "emphasis";
	
	private UiSupport uiSupport;
	private String uiSupportStylersKey;
	
	private Image fingerPrintWarning;
	private Image fingerPrintError;
	private Image fingerPrintOk;
	private Image fingerPrintIgnored;	
	
	private Image annotationImg;
	private Image classImg;
	private Image enumImg;
	private Image interfaceImg;
	
	private LazyInitialized<Map<String, Styler>> stylerMap = new LazyInitialized<>( this::setupUiSupport);
	
	private StyledTextHandler styledStringStyler;
	{
		styledStringStyler = new StyledTextHandler();
		styledStringStyler.setStylerMapSupplier(stylerMap);	
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
					
		//
		fingerPrintWarning = uiSupport.images().addImage("cmp_warn", ViewLabelProvider.class, "testwarn.png");
		fingerPrintError = uiSupport.images().addImage("cmp_error", ViewLabelProvider.class, "testerr.png");
		fingerPrintOk = uiSupport.images().addImage("cmp_ok", ViewLabelProvider.class, "testok.png");
		fingerPrintIgnored = uiSupport.images().addImage("cmp_ignore", ViewLabelProvider.class, "testignored.png");	
		
		annotationImg = uiSupport.images().addImage("cmp_anno", ViewLabelProvider.class, "annotation.png");
		classImg = uiSupport.images().addImage("cmp_class", ViewLabelProvider.class, "class_ob.png");
		interfaceImg = uiSupport.images().addImage("cmp_int", ViewLabelProvider.class, "int_obj.png");
		enumImg = uiSupport.images().addImage("cmp_enum", ViewLabelProvider.class, "enum_obj.png");
		
		
		Stylers stylers = uiSupport.stylers(uiSupportStylersKey);
		stylers.addStandardStylers();
		
		Map<String,Styler> map = new HashMap<>();
		map.put(STYLER_EMPHASIS, stylers.standardStyler(Stylers.KEY_BOLD));
		map.put(STYLER_STANDARD, stylers.standardStyler(Stylers.KEY_DEFAULT));
		return map;
	}
		
	
	


	@Override
	public Image getImage(Object obj) {
		// trigger lazy if required - for the pictures 
		stylerMap.get();
		
		if (obj instanceof FingerPrintNode) {
		
			FingerPrintNode node = (FingerPrintNode) obj;
			
			switch (node.getRepresentation()) {
				case owner:
					return getImageForTopEntity( node.getBaseTopEntity());				
				default:
				case container:
					FingerPrintRating rating = node.getRating();
					if (rating == null) {
						return fingerPrintIgnored;
					}					
					switch (rating) {
						case error:
							return fingerPrintError;			
						case warning:
							return fingerPrintWarning;			
						default:
						case ignore:
							return fingerPrintIgnored;
						case info:
						case ok:
							return fingerPrintOk;	
					}			
			}
			
			
		}
		else if (obj instanceof ContainerNode) {
			ContainerNode cnn = (ContainerNode) obj;
			switch ( cnn.getRepresentation()) {
			case owner:
				ZedEntity owner = cnn.getOwner();
				if (owner != null)  {
					return getImageForTopEntity( owner);	
				}			
				return fingerPrintIgnored;				
			default:
			case container:
				FingerPrintRating rating = cnn.getRating();
				switch (rating) {
				case error:
					return fingerPrintError;			
				case warning:
					return fingerPrintWarning;			
				default:
				case ignore:
					return fingerPrintIgnored;
				case info:
				case ok:
					return fingerPrintOk;	
			}					
			}			
		}
		else 
			return null;
	}
	
	private Image getImageForTopEntity( ZedEntity z) {
		if (z instanceof AnnotationEntity) {
			return annotationImg;
		}
		else if (z instanceof ClassEntity) {
			return classImg;
		}
		else if (z instanceof InterfaceEntity) {
			return interfaceImg;
		}
		else if (z instanceof EnumEntity) {
			return enumImg;
		}
		return null;
	}

	@Override
	public String getToolTipText(Object obj) {
		
		if (obj instanceof FingerPrintNode) {		
			FingerPrintNode fpn = (FingerPrintNode) obj;
			
			FingerPrint fp = fpn.getFingerPrint();
			String issue = fp.getSlots().get( ISSUE);
			
			StringBuilder sb = new StringBuilder();
			sb.append( "found issue '" + issue + "'");
			
			ZedEntity topEntity = fpn.getBaseTopEntity();
			if (topEntity != null) {
				sb.append( " in : " + topEntity.getName());
			}
			return sb.toString();
		}
				 	
		return super.getToolTipText(obj);
	}
		
	@Override
	public StyledString getStyledText(Object obj) {
		
		Pair<String, List<StyledTextSequenceData>> stylePair = null;
		
		if (obj instanceof FingerPrintNode) {		
			FingerPrintNode fingerPrintNode = (FingerPrintNode) obj;			
					
			stylePair = getStyledTextForFingerPrint( fingerPrintNode);				
			
		}
		else if (obj instanceof ContainerNode) {
			ContainerNode cn = (ContainerNode) obj;
			stylePair = getStyledTextForContainer( cn);			
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
	
		
	private Pair<String, List<StyledTextSequenceData>> getStyledTextForContainer(ContainerNode cn) {
		List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();
		pairs.add( styledStringStyler.build( cn.getName(), STYLER_EMPHASIS));
		pairs.add( styledStringStyler.build( " (" + cn.getChildren().size() + ")", STYLER_STANDARD));		
		return styledStringStyler.merge(pairs);
	}

	private Pair<String, List<StyledTextSequenceData>> getStyledTextForFingerPrint(FingerPrintNode fpn) {
		List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();
		FingerPrint fp = fpn.getFingerPrint();
		String issue = fp.getSlots().get( ISSUE);
		pairs.add( styledStringStyler.build(issue, STYLER_EMPHASIS));		
		pairs.add( styledStringStyler.build(" ", STYLER_STANDARD));
		
		ZedEntity topEntity = fpn.getBaseTopEntity();
		if (topEntity != null) {
			pairs.add( styledStringStyler.build( topEntity.getName(), STYLER_STANDARD));
		}
		return styledStringStyler.merge(pairs);		
	}
	
	@Override
	public void update(ViewerCell arg0) {	
	}
	
	
}
