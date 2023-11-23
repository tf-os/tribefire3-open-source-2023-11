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
package com.braintribe.gwt.gxt.gxtresources.orangeflattab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.theme.blue.client.tabs.BluePlainTabPanelAppearance;

public class OrangeFlatTabPanelAppearance extends BluePlainTabPanelAppearance {
	
	public interface OrangeFlatTabPanelStyle extends BluePlainTabPanelStyle {
		//NOP
	}
	
	public interface OrangeFlatTabPanelResources extends BluePlainTabPanelResources {

		@Override
		@Source({"com/sencha/gxt/theme/base/client/tabs/TabPanel.gss", "com/sencha/gxt/theme/blue/client/tabs/BlueTabPanel.gss",
				"com/sencha/gxt/theme/base/client/tabs/PlainTabPanel.gss", "com/sencha/gxt/theme/blue/client/tabs/BluePlainTabPanel.gss",
				"OrangeFlatTabPanel.gss"})
		OrangeFlatTabPanelStyle style();
		
	}
	
	private final PlainTabPanelTemplates template;
	private final OrangeFlatTabPanelStyle style;
	private static final String ACTIVE_CLASS_NAME = "x-tab-strip-active";
	
	public OrangeFlatTabPanelAppearance() {
		this(GWT.<OrangeFlatTabPanelResources> create(OrangeFlatTabPanelResources.class), GWT.<PlainTabPanelTemplates> create(PlainTabPanelTemplates.class));
	}
	
	public OrangeFlatTabPanelAppearance(OrangeFlatTabPanelResources resources, PlainTabPanelTemplates template) {
		super(resources, template, GWT.<ItemTemplate> create(ItemTemplate.class));
	    this.style = resources.style();
	    this.template = template;
	}
	
	@Override
	public void render(SafeHtmlBuilder builder) {
		builder.append(template.renderPlain(style));
	}
	
	@Override
	public void onSelect(Element item) {
		super.onSelect(item);
		item.addClassName(ACTIVE_CLASS_NAME);
	}
	
	@Override
	public void onDeselect(Element item) {
		super.onDeselect(item);
		item.removeClassName(ACTIVE_CLASS_NAME);
	}

}
