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
package com.braintribe.devrock.zed.ui.viewer.extraction;


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
import com.braintribe.devrock.zarathud.model.extraction.AnnotationNode;
import com.braintribe.devrock.zarathud.model.extraction.ClassNode;
import com.braintribe.devrock.zarathud.model.extraction.EnumNode;
import com.braintribe.devrock.zarathud.model.extraction.EnumValueNode;
import com.braintribe.devrock.zarathud.model.extraction.ExtractionNode;
import com.braintribe.devrock.zarathud.model.extraction.FieldNode;
import com.braintribe.devrock.zarathud.model.extraction.InterfaceNode;
import com.braintribe.devrock.zarathud.model.extraction.MethodNode;
import com.braintribe.devrock.zarathud.model.extraction.subs.ContainerNode;
import com.braintribe.devrock.zarathud.model.extraction.subs.PackageNode;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.zarathud.model.data.AccessModifier;
import com.braintribe.zarathud.model.data.ClassEntity;
import com.braintribe.zarathud.model.data.EnumEntity;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.ScopeModifier;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;

public class ViewLabelProvider extends CellLabelProvider implements IStyledLabelProvider {
	private static final String STYLER_STANDARD = "standard";
	private static final String STYLER_EMPHASIS = "emphasis";
		
	
	private Image interfaceImage; 
	private Image enumImage;
	
	private Image methodPublicImage;
	private Image methodProtectedImage;
	private Image methodPrivateImage;
	
	private Image fieldPublicImage;
	private Image fieldProtectedImage;
	private Image fieldPrivateImage;
	
	private Image classImage;
	private Image annotationImage;
	private Image containerImage;
	private Image enumValueImage;
	private Image packageImage;
	
	private UiSupport uiSupport;
	private String uiSupportStylersKey;
	
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
		interfaceImage = uiSupport.images().addImage("ex_interface", ViewLabelProvider.class, "int_obj.png");
		classImage = uiSupport.images().addImage("ex_class", ViewLabelProvider.class, "class_ob.png");
		enumImage = uiSupport.images().addImage("ex_enum", ViewLabelProvider.class, "enum_obj.png");
		enumValueImage = uiSupport.images().addImage("ex_enumValue", ViewLabelProvider.class, "task-list.png");
		
		annotationImage = uiSupport.images().addImage("ex_annotation", ViewLabelProvider.class, "annotation.png");
		
		fieldPublicImage = uiSupport.images().addImage("ex_field_pb", ViewLabelProvider.class, "field_public.png");
		fieldProtectedImage = uiSupport.images().addImage("ex_field_pt", ViewLabelProvider.class, "field_protected.png");
		fieldPrivateImage = uiSupport.images().addImage("ex_field_pr", ViewLabelProvider.class, "field_private.png");
		
		methodPublicImage = uiSupport.images().addImage("ex_method_pb", ViewLabelProvider.class, "method_public.png");
		methodProtectedImage = uiSupport.images().addImage("ex_method_pt", ViewLabelProvider.class, "method_protected.png");
		methodPrivateImage = uiSupport.images().addImage("ex_method_pr", ViewLabelProvider.class, "method_private.png");
		
		containerImage = uiSupport.images().addImage("ex_container", ViewLabelProvider.class, "icons-legend.gif");
		
		packageImage = uiSupport.images().addImage("ex_package", ViewLabelProvider.class, "package_obj.png");
		
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
		
		if (obj instanceof ClassNode) {
			return classImage;
		}
		else if (obj instanceof InterfaceNode) {
			return interfaceImage;
		}
		else if (obj instanceof EnumNode) {
			return enumImage;
		}
		else if (obj instanceof FieldNode) {
			FieldNode fn = (FieldNode) obj;
			switch (fn.getFieldEntity().getAccessModifier()) {
				case PACKAGE_PRIVATE:			
				case PRIVATE:
					return fieldPrivateImage;				
				case PROTECTED:
					return fieldProtectedImage;				
				case PUBLIC:
				default:
					return fieldPublicImage;				
			}			
		}
		else if (obj instanceof MethodNode) {
			MethodNode mn = (MethodNode) obj;
			switch (mn.getMethodEntity().getAccessModifier()) {
				case PACKAGE_PRIVATE:			
				case PRIVATE:
					return methodPrivateImage;				
				case PROTECTED:
					return methodProtectedImage;				
				case PUBLIC:
				default:
					return methodPublicImage;
			}			
		}
		else if (obj instanceof AnnotationNode) {
			return annotationImage;
		}
		else if (obj instanceof ContainerNode) {
			return containerImage;
		}
		else if (obj instanceof EnumValueNode) {
			return enumValueImage;
		}						
		else if (obj instanceof PackageNode) {
			return packageImage;
		}
		return null;
	}
	
	

	@Override
	public String getToolTipText(Object obj) {
		//ExtractionNode en = (ExtractionNode) obj;
		if (obj instanceof ClassNode) {			
			ClassNode cn = (ClassNode) obj;
			ClassEntity e = cn.getEntity();
			String tt = buildTooltipPrefix(e.getAbstractNature(), e.getStaticNature(), false, e.getAccessModifier(), null);
			return tt + cn.getName();			
		}
		else if (obj instanceof InterfaceNode) {
			InterfaceNode in = (InterfaceNode) obj;
			InterfaceEntity ie = in.getInterfaceEntity();
			return ie.getName();			
		}
		else if (obj instanceof EnumNode) {
			EnumNode n = (EnumNode) obj;
			EnumEntity e = n.getEnumEntity();
			String tt = buildTooltipPrefix(e.getAbstractNature(), e.getStaticNature(), false, e.getAccessModifier(), null);
			return tt + n.getName();			
		}
		else if (obj instanceof FieldNode) {
			FieldNode n = (FieldNode) obj;
			FieldEntity e = n.getFieldEntity();
			String tt = buildTooltipPrefix( false, e.getStaticNature(), false, e.getAccessModifier(), e.getScopeModifier());
			return tt + n.getName();
		}
		else if (obj instanceof MethodNode) {			
			MethodNode n = (MethodNode) obj;
			MethodEntity e = n.getMethodEntity();
			String tt = buildTooltipPrefix( e.getAbstractNature(), e.getStaticNature(), e.getSynchronizedNature(), e.getAccessModifier(), null);
			return tt + n.getName();
		}
		else if (obj instanceof AnnotationNode) {			
		}
		else if (obj instanceof ContainerNode) {			
		}		
	 	
		return super.getToolTipText(obj);
	}
	
	private String buildTooltipPrefix( boolean isAbstract, boolean isStatic, boolean isSynchronized, AccessModifier accessModifier, ScopeModifier scopeModifier) {
		StringBuilder sb = new StringBuilder();
		if (isAbstract) {
			sb.append( "abstract ");
		}
		if (isStatic) {
			sb.append( "static ");
		}
		if (isSynchronized)  {
			sb.append( "synchronized ");
		}
		
		switch (accessModifier) {
			case PRIVATE:
				sb.append( "private ");
				break;
			case PROTECTED:
				sb.append( "protected ");
				break;
			case PUBLIC:
				sb.append( "public ");
				break;
			case PACKAGE_PRIVATE:
			default:			
				sb.append( "package-private ");
				break;		
		}
		
		if (scopeModifier != null) {
			switch (scopeModifier) {
			case FINAL:
				sb.append( "final ");
				break;
			case VOLATILE:
				sb.append( "volatile ");
				break;
			default:
			case DEFAULT:
				break;			
			}
		}
		return sb.toString();
	}

	@Override
	public StyledString getStyledText(Object obj) {
		
		Pair<String, List<StyledTextSequenceData>> stylePair = null;
		
		if (obj instanceof ClassNode) {
			ClassNode node = (ClassNode) obj;
			stylePair = getStyledTextForClass( node);
		}
		else if (obj instanceof InterfaceNode) {
			InterfaceNode node = (InterfaceNode) obj;
			stylePair = getStyledTextForInterface( node);
		}
		else if (obj instanceof EnumNode) {
			EnumNode node = (EnumNode) obj;
			stylePair = getStyledTextForEnum( node);
		}
		else if (obj instanceof FieldNode) {
			FieldNode node = (FieldNode) obj;
			stylePair = getStyledTextForField( node);
		}
		else if (obj instanceof MethodNode) {
			MethodNode node = (MethodNode) obj;
			stylePair = getStyledTextForMethod( node);
		}
		else if (obj instanceof AnnotationNode) {
			AnnotationNode node = (AnnotationNode) obj;
			stylePair = getStyledTextForAnnotation( node);
		}
		else if (obj instanceof ContainerNode) {			
			ContainerNode cn = (ContainerNode) obj;
			stylePair = styledStringStyler.build( cn.getName(), STYLER_STANDARD);
		}		
		else if (obj instanceof EnumValueNode) {
			EnumValueNode evn = (EnumValueNode) obj;
			stylePair = styledStringStyler.build(evn.getName(), STYLER_STANDARD);
		}
		else if (obj instanceof ExtractionNode) {
			ExtractionNode en = (ExtractionNode) obj;
			stylePair = styledStringStyler.build(en.getName(), STYLER_STANDARD);
		}		
		else {
			stylePair = styledStringStyler.build(obj.getClass().getName(), STYLER_STANDARD);
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
	
	
	

	private Pair<String, List<StyledTextSequenceData>> getStyledTextForClass(ClassNode node) {
		List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();		
		pairs.add( styledStringStyler.build(node.getName(), STYLER_EMPHASIS));
		return styledStringStyler.merge(pairs);		
	}

	private Pair<String, List<StyledTextSequenceData>> getStyledTextForInterface(InterfaceNode node) {
		List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();		
		pairs.add( styledStringStyler.build(node.getName(), STYLER_EMPHASIS));
		return styledStringStyler.merge(pairs);
	}

	private Pair<String, List<StyledTextSequenceData>> getStyledTextForEnum(EnumNode node) {
		List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();		
		pairs.add( styledStringStyler.build(node.getName(), STYLER_EMPHASIS));
		return styledStringStyler.merge(pairs);
	}

	private Pair<String, List<StyledTextSequenceData>> getStyledTextForField(FieldNode node) {
		List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();
		
		FieldEntity fieldEntity = node.getFieldEntity();
		AccessModifier accessModifier = fieldEntity.getAccessModifier();
		ScopeModifier scopeModifier = fieldEntity.getScopeModifier();
		boolean staticNature = fieldEntity.getStaticNature();
		TypeReferenceEntity type = fieldEntity.getType();
		
		pairs.add( styledStringStyler.build( accessModifier.name().toLowerCase(), STYLER_STANDARD));
		pairs.add( styledStringStyler.build( " ", STYLER_STANDARD));
		
		if (staticNature) {
			pairs.add( styledStringStyler.build( "static", STYLER_STANDARD));
			pairs.add( styledStringStyler.build( " ", STYLER_STANDARD));
		}
		
		if (scopeModifier != ScopeModifier.DEFAULT) {
			pairs.add( styledStringStyler.build( scopeModifier.name().toLowerCase(), STYLER_STANDARD));		
			pairs.add( styledStringStyler.build( " ", STYLER_STANDARD));
		}
		pairs.add( styledStringStyler.build( type.getReferencedType().getName(), STYLER_STANDARD));
		pairs.add( styledStringStyler.build( " ", STYLER_STANDARD));
		
		pairs.add( styledStringStyler.build(node.getName(), STYLER_EMPHASIS));
		
		return styledStringStyler.merge(pairs);
	}

	private Pair<String, List<StyledTextSequenceData>> getStyledTextForMethod(MethodNode node) {
		List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();
		MethodEntity methodEntity = node.getMethodEntity();
		
		AccessModifier accessModifier = methodEntity.getAccessModifier();
		pairs.add( styledStringStyler.build( accessModifier.name().toLowerCase(), STYLER_STANDARD));
		pairs.add( styledStringStyler.build( " ", STYLER_STANDARD));
		
		boolean abstractNature = methodEntity.getAbstractNature();
		if (abstractNature) {
			pairs.add( styledStringStyler.build( "abstract ", STYLER_STANDARD));
		}
		
		boolean staticNature = methodEntity.getStaticNature();		
		if (staticNature) {
			pairs.add( styledStringStyler.build( "static", STYLER_STANDARD));
			pairs.add( styledStringStyler.build( " ", STYLER_STANDARD));
		}
		
		
		boolean synchronizedNature = methodEntity.getSynchronizedNature();
		if (synchronizedNature) {
			pairs.add( styledStringStyler.build( "synchronized", STYLER_STANDARD));
			pairs.add( styledStringStyler.build( " ", STYLER_STANDARD));
		}
		
		TypeReferenceEntity returnType = methodEntity.getReturnType();
		pairs.add( styledStringStyler.build( returnType.getReferencedType().getName(), STYLER_STANDARD));
		pairs.add( styledStringStyler.build( " ", STYLER_STANDARD));
		
		pairs.add( styledStringStyler.build(node.getName(), STYLER_EMPHASIS));
		
		pairs.add( styledStringStyler.build( "(", STYLER_STANDARD));
		
		List<TypeReferenceEntity> argumentTypes = methodEntity.getArgumentTypes();
		boolean first = true;
		for (TypeReferenceEntity tre : argumentTypes) {
			if (!first) {
				pairs.add( styledStringStyler.build( ",", STYLER_STANDARD));
				first = false;
			}
			pairs.add( styledStringStyler.build( tre.getReferencedType().getName(), STYLER_STANDARD));
		}
		
		pairs.add( styledStringStyler.build( ")", STYLER_STANDARD));
		
		return styledStringStyler.merge(pairs);
	}

	private Pair<String, List<StyledTextSequenceData>> getStyledTextForAnnotation(AnnotationNode node) {
		List<Pair<String,List<StyledTextSequenceData>>> pairs = new ArrayList<>();		
		pairs.add( styledStringStyler.build(node.getName(), STYLER_EMPHASIS));
		return styledStringStyler.merge(pairs);
	}

	@Override
	public void update(ViewerCell arg0) {	
	}
	
	
}
