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
package com.braintribe.gwt.gme.constellation.client;

import com.braintribe.gwt.action.client.Action;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;

/**
 * Prepares an {@link HTML} which works like a button which has two sections.
 * @author michel.docouto
 *
 */
public class DualSectionButton extends HTML {
	
	//private List<DualSectionButtonListener> listeners;
	private boolean dualSectionVisible = true;
	private ImageResource icon;
	private ImageResource dualIcon;
	private String description;
	private String toolTip;
	private String name;
	private IconAlign iconAlign;
	private ButtonScale buttonScale;
	private boolean useButtonText = true;
	private Action primaryAction;
	private Action secondaryAction;
	
	public DualSectionButton(ImageResource icon, ImageResource dualIcon, String description, String toolTip) {
		this.icon = icon;
		this.dualIcon = dualIcon;
		this.description = description;
		this.toolTip = toolTip;
		//this.name = name;
		prepareHtml();
		
		addDomHandler(event -> {
			EventTarget target = event.getNativeEvent().getEventTarget();
			if (Element.is(target)) {
				Element actionFolderElement = getActionElement(Element.as(target), 2, "dual-section-stack");
				fireSectionClicked(/*event, */actionFolderElement == null);
			}
		}, ClickEvent.getType());
	}
	
	/*
	public void addDualSectionButtonListener(DualSectionButtonListener listener) {
		if (listeners == null)
			listeners = new ArrayList<DualSectionButtonListener>();
		listeners.add(listener);
	}
	
	public void removeDualSectionButtonListener(DualSectionButtonListener listener) {
		if (listener != null) {
			listeners.remove(listener);
			
			if (listeners.isEmpty())
				listeners = null;
		}
	}
	*/
	
    public void setIconAlign(IconAlign iconAlign) {
    	this.iconAlign = iconAlign;
		prepareHtml();
	}
    
    public void setScale(ButtonScale buttonScale) {
    	this.buttonScale = buttonScale;
        prepareHtml();
    }    
	
    public void setUseButtonText(boolean useButtonText) {
    	this.useButtonText = useButtonText;
        prepareHtml();
    }        
    
	public void setDualSectionVisible(boolean dualSectionVisible) {
		if (this.dualSectionVisible != dualSectionVisible) {
			this.dualSectionVisible = dualSectionVisible;
			prepareHtml();
		}
	}
	
	private void prepareHtml() {
		StringBuilder builder = new StringBuilder();
		
		String buttonScaleClass = "";
		if (buttonScale == ButtonScale.LARGE)
			buttonScaleClass = " dual-section-button-scale-large";
		if (buttonScale == ButtonScale.MEDIUM)
			buttonScaleClass = " dual-section-button-scale-medium";
    	if (buttonScale == ButtonScale.SMALL)
			buttonScaleClass = " dual-section-button-scale-small";
	    if (buttonScale == ButtonScale.NONE)
			buttonScaleClass = " dual-section-button-scale-none";		
		
		String style = "";
		String textClass = "";
		if (iconAlign == IconAlign.LEFT || iconAlign == IconAlign.RIGHT) {
			style = " display: flex; justify-content: flex-start;";
			textClass = " actionbar-text-horizontal";
		} else {
			textClass = " actionbar-text-vertical";
		}
				
		StringBuilder img = new StringBuilder();
		img.append("<img src='").append(icon.getSafeUri().asString()).append("' class='dual-section-image'/>");
		StringBuilder text = new StringBuilder();
		text.append("<div class='actionbar-text").append(textClass).append("'");		
		if (toolTip != null)
			text.append(" qtip='").append(toolTip).append("'");		
		text.append(">");
		if (useButtonText)
			text.append(description);
		text.append("</div>");		
		
		builder.append("<div class='dual-section-button enabled").append(buttonScaleClass).append("' style='position: relative;").append(style).append("'>");
		if (iconAlign == IconAlign.LEFT || iconAlign == IconAlign.TOP) {
			builder.append(img.toString());
			builder.append(text.toString());
		} else {
			builder.append(text.toString());			
			builder.append(img.toString());
		}
		
		if (dualSectionVisible) {
			builder.append("<div class='dual-section-stack' style='position: absolute; width: 100%; bottom: 0px; box-shadow: 2px 2px 5px #888; background-image: url(");
			builder.append(dualIcon.getSafeUri().asString());
			builder.append(");'/></div>");
		}
		builder.append("</div>");
		setHTML(builder.toString());
	}
	
	private void fireSectionClicked(/*ClickEvent event, */boolean upper) {		
		/*
		if (listeners != null) {
			for (DualSectionButtonListener listener : listeners) {
				if (upper)
					listener.onUpperSectionClicked(event);
				else
					listener.onLowerSectionClicked(event);
			}
		}
		*/
		
		if (upper)
			primaryAction.perform(null);
		else
			secondaryAction.perform(null);
	}
	
	private Element getActionElement(Element clickedElement, int depth, String className) {
		if (depth > 0 && clickedElement != null) {
			if (className.equals(clickedElement.getClassName()))
				return clickedElement;
			
			return getActionElement(clickedElement.getParentElement(), --depth, className);
		}
		
		return null;
	}
	
	public void setIcon(ImageResource icon) {
		this.icon = icon;
		prepareHtml();
	}

	public void setDualIcon(ImageResource dualIcon) {
		this.dualIcon = dualIcon;
		prepareHtml();
	}
	
	public ImageResource getIcon() {
		return this.icon;
	}

	public ImageResource getDualIcon() {
		return this.dualIcon;
	}

	
	public void setDescription(String description) {
		 this.description = description;
	}
	
	public String getDescription() {
		return this.description;
	}

	public void setToolTip(String toolTip) {
		 this.toolTip = toolTip;
	}

	public String getToolTip() {
		return this.toolTip;
	}

	public void setName(String name) {
		 this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	public Action getPrimaryAction() {
		return primaryAction;
	}

	public void setPrimaryAction(Action primaryAction) {
		this.primaryAction = primaryAction;		
	}

	public Action getSecondaryAction() {
		return secondaryAction;		
	}

	public void setSecondaryAction(Action secondaryAction) {
		this.secondaryAction = secondaryAction;
	}

	/*
	public static interface DualSectionButtonListener {
		void onUpperSectionClicked(ClickEvent event);
		void onLowerSectionClicked(ClickEvent event);
	}
	*/	
}
