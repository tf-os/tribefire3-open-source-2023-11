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
package com.braintribe.gwt.gxt.gxtresources.whitemask.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Timer;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.dom.Mask.MaskDefaultAppearance;
import com.sencha.gxt.core.client.dom.Mask.MessageTemplates;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.core.client.dom.XElement;

public class WhiteMaskAppearance extends MaskDefaultAppearance {
	
	public interface WhiteMaskStyle extends MaskStyle {
		@ClassName("gmeMaskText")
		@Override
		String text();
    }
	
	public interface WhiteMaskResources extends MaskResources {
        @Override
        @Source({"com/sencha/gxt/core/client/dom/Mask.gss", "WhiteMask.gss"})
        WhiteMaskStyle css();
        
    	ImageResource info();
    }
	
	private MessageTemplates originalTemplate;
	private NotificationMessageTemplates notificationMessageTemplates;
	private Timer progressTimer;
	
	public WhiteMaskAppearance() {
		super(GWT.<MessageTemplates> create(NotificationMessageTemplates.class), GWT.<WhiteMaskResources> create(WhiteMaskResources.class));
		notificationMessageTemplates = (NotificationMessageTemplates) getTemplate();
		
		originalTemplate = GWT.create(MessageTemplates.class);
	}
	
	/*
	 * Overriding to export a new mask class.
	 */
	@Override
	public void mask(XElement parent, String message) {
		StringBuilder additionalClass = new StringBuilder();
		additionalClass.append(" gmeWhiteMask");
		if (message != null && !message.isEmpty())
			additionalClass.append(" gmeWhiteMaskWithMessage");
		
		XElement mask = XElement.createElement("div");
		mask.setClassName(getResources().css().mask() + additionalClass.toString());
		parent.appendChild(mask);

		XElement box = null;
		if (message != null) {
			if (MaskController.progressBarInitialValue != null) {
				String title = MaskController.getProgressBarTitle();
				if (title == null)
					title = "";
				
				box = XDOM.create(notificationMessageTemplates.progressTemplate(getResources().css(), SafeHtmlUtils.htmlEscape(message),
						MaskController.getProgressBarImage(), title, MaskController.progressBarInitialValue)).cast();
				box.addClassName("gmeOpaqueBox");
				mask.addClassName("gmeOpaqueMask");
				Scheduler.get().scheduleDeferred(this::updateProgress);
			} else {
				if (!MaskController.maskScreenOpaque)
					box = XDOM.create(getTemplate().template(getResources().css(), SafeHtmlUtils.htmlEscape(message))).cast();
				else {
					box = XDOM.create(originalTemplate.template(getResources().css(), SafeHtmlUtils.htmlEscape(message))).cast();
					box.addClassName("gmeOpaqueBox");
					mask.addClassName("gmeOpaqueMask");
				}
			}
			
			parent.appendChild(box);
		}

		if (GXT.isIE() && "auto".equals(parent.getStyle().getHeight()))
			mask.setSize(parent.getOffsetWidth(), parent.getOffsetHeight());

		if (box != null) {
			box.updateZIndex(0);
			box.center(parent);
		}
	}
	
	private void updateProgress() {
		if (progressTimer != null) {
			progressTimer.schedule(100);
			return;
		}
		
		progressTimer = new Timer() {
			@Override
			public void run() {
				Element progressBar = Document.get().getElementById("loadingProgressBarMask");
				if (progressBar == null || MaskController.progressBarInitialValue == null)
					return;
				
				MaskController.progressBarInitialValue++;
				progressBar.getStyle().setWidth(MaskController.progressBarInitialValue, Unit.PCT);
				
				if (!MaskController.progressBarInitialValue.equals(MaskController.progressBarMaxValue))
					progressTimer.schedule(100);
			}
		};
		
		progressTimer.schedule(100);
	}
	
	private native MaskResources getResources() /*-{
		return this.@com.sencha.gxt.core.client.dom.Mask.MaskDefaultAppearance::resources;
	}-*/;
	
	private native MessageTemplates getTemplate() /*-{
		return this.@com.sencha.gxt.core.client.dom.Mask.MaskDefaultAppearance::template;
	}-*/;

}
