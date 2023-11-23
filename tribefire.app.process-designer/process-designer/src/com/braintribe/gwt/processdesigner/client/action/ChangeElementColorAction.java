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
package com.braintribe.gwt.processdesigner.client.action;

import java.util.List;

import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGPoint;
import org.vectomatic.dom.svg.OMSVGRectElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;

import com.braintribe.gwt.genericmodelgxtsupport.client.field.color.ColorPicker;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.processdefrep.HasColor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.sencha.gxt.widget.core.client.ColorPalette;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ChangeElementColorAction extends ProcessDesignerActionMenuElement{
	
	private Dialog dialog;
	private ColorPicker colorPicker;
	private ColorPalette colorPalette;
	private OMSVGSVGElement svg;
	private List<ModelPath> selection;
	private TextButton ok;
	private TextButton cancel;
	private OMSVGGElement colorElement;
	private OMSVGRectElement rectElement;
	private int colorElementWidth = 32;
	
	public ChangeElementColorAction() {
		setUseIcon(false);
	}
	
	public void setSvg(OMSVGSVGElement svg) {
		this.svg = svg;
	}
	
	public Dialog getDialog() {
		if(dialog == null){
			dialog = new Dialog();
			dialog.setShadow(false);
			dialog.setClosable(false);
			dialog.setDraggable(false);
			dialog.setBodyBorder(false);
			dialog.setBorders(false);
			dialog.setBodyStyle("background: #e5e5e5");
			dialog.setSize("400px", "350px");
			
			BorderLayoutContainer container = new BorderLayoutContainer();
			container.setCenterWidget(getColorPicker());
			
			ToolBar toolBar = new ToolBar();
//			toolBar.setStyleAttribute("background", "#e5e5e5");
//			toolBar.setAlignment(HorizontalAlignment.RIGHT);
			toolBar.add(getOk());
			toolBar.add(getCancel());			
			
			BorderLayoutData south = new BorderLayoutData(35);
			
			container.setSouthWidget(toolBar, south);
			
			dialog.add(container);
			
			dialog.setFocusWidget(getColorPicker());
		}
		return dialog;
	}
	
	public ColorPicker getColorPicker() {
		if(colorPicker == null){
			colorPicker = new ColorPicker(){
				@Override
				public void onChange(ChangeEvent event) {
					super.onChange(event);
					for(ModelPath modelPath : selection){
						if(modelPath.last() != null && modelPath.last().getValue() instanceof HasColor){
							HasColor hasColor = modelPath.last().getValue();
							hasColor.setColor(colorPicker.getHexColor());
						}	
					}
				}
			};
		}
		return colorPicker;
	}
	
	public ColorPalette getColorPalette() {
		if(colorPalette == null){
			colorPalette = new ColorPalette();
			colorPalette.addSelectionHandler(event -> {
				for(ModelPath modelPath : selection){
					if(modelPath.last() != null && modelPath.last().getValue() instanceof HasColor){
						HasColor hasColor = modelPath.last().getValue();
						hasColor.setColor("#" + event.getSelectedItem());
					}	
				}
			});
		}
		return colorPalette;
	}
	
	public TextButton getOk() {
		if(ok == null){
			ok = new TextButton("Ok");
			ok.addSelectHandler(event -> {
				for(ModelPath modelPath : selection){
					if(modelPath.last() != null && modelPath.last().getValue() instanceof HasColor){
						HasColor hasColor = modelPath.last().getValue();
						hasColor.setColor("#" + colorPicker.getHexColor());
						rectElement.setAttribute("style", "fill:" + hasColor.getColor() + ";stroke:black;stroke-width:1");
					}	
				}
				getDialog().hide();
			});
		}
		return ok;
	}
	
	public TextButton getCancel() {
		if(cancel == null){
			cancel = new TextButton("Cancel");
			cancel.addSelectHandler(event -> getDialog().hide());
		}
		return cancel;
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		//NOP
	}

	@Override
	public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
		setEnabled(false);
		selection = gmSelectionSupport.getCurrentSelection();
		rectElement.setAttribute("style", "fill:white;stroke:black;stroke-width:1");
		for(ModelPath modelPath : selection){
			if(modelPath.last() != null && modelPath.last().getValue() instanceof HasColor){
				HasColor hasColor = (HasColor)modelPath.last().getValue();
				rectElement.setAttribute("style", "fill:" + hasColor.getColor() + ";stroke:black;stroke-width:1");
				setEnabled(true);
				try {
					getColorPicker().setHex(hasColor.getColor().substring(1, hasColor.getColor().length()));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}	
		}
		getDialog().hide();
	}

	@Override
	public void handleDipose() {
		//NOP
	}

	@Override
	public void configure() {
		//NOP
	}

	@Override
	public void perform() {
		getDialog().show();
		
		float x = getBBox().getX();
		float y = getBBox().getY() - getDialog().getOffsetHeight();//+ getBBox().getHeight();
		OMSVGPoint point = svg.createSVGPoint(x, y).matrixTransform(svg.getScreenCTM());
		
		getDialog().setPosition((int)point.getX(),(int)point.getY());
	}
	
	@Override
	public OMSVGGElement prepareIconElement() {
		if(colorElement == null){
			colorElement = new OMSVGGElement();
			rectElement = new OMSVGRectElement(0, 0, 0, 0, 0, 0);
			rectElement.setAttribute("width", colorElementWidth+"");
			rectElement.setAttribute("height", colorElementWidth+"");
			rectElement.setAttribute("x",((getX()+(width/2)) - (colorElementWidth / 2))+"");
			rectElement.setAttribute("y",((getY()+(height/2)) - (colorElementWidth / 2))+"");
			rectElement.setAttribute("style", "fill:white;stroke:black;stroke-width:1");
			colorElement.appendChild(rectElement);
		}
		return colorElement;
	}
	
	@Override
	public void init() {
		super.init();
		rectElement.setAttribute("width", colorElementWidth+"");
		rectElement.setAttribute("height", colorElementWidth+"");
		rectElement.setAttribute("x",((getX()+(width/2)) - (colorElementWidth / 2))+"");
		rectElement.setAttribute("y",((getY()+(height/2)) - (colorElementWidth / 2))+"");
	}

}
