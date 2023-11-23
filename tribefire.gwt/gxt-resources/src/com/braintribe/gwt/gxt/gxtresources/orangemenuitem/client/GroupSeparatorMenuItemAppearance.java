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
package com.braintribe.gwt.gxt.gxtresources.orangemenuitem.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.theme.blue.client.menu.BlueSeparatorMenuItemAppearance;

public class GroupSeparatorMenuItemAppearance extends BlueSeparatorMenuItemAppearance {
	
  private GroupSeparatorMenuItemStyle style;
  private SafeHtml content;
  private String iconUrl;
	
  public interface GroupSeparatorMenuItemStyle extends BlueSeparatorMenuItemStyle {
		//NOP
  }
	
  public interface GroupSeparatorMenuItemResources extends BlueSeparatorMenuItemResources {
		
		@Override
		@Source({"com/sencha/gxt/theme/base/client/menu/Item.gss", "com/sencha/gxt/theme/blue/client/menu/BlueItem.gss", "com/sencha/gxt/theme/base/client/menu/SeparatorMenuItem.gss",
				"com/sencha/gxt/theme/blue/client/menu/BlueSeparatorMenuItem.gss", "GroupSeparatorMenuItem.gss"})
		GroupSeparatorMenuItemStyle style();
  }
	  
  public interface GroupSeparatorMenuItemTemplate extends SeparatorMenuItemTemplate {

	    @XTemplate(source = "GroupSeparatorMenuItem.html")
	    SafeHtml render(SeparatorMenuItemStyle style, SafeHtml content);
	    
	    @XTemplate(source = "GroupIconSeparatorMenuItem.html")
	    SafeHtml render(SeparatorMenuItemStyle style, SafeHtml content, String iconUrl);	    
  }

  public GroupSeparatorMenuItemAppearance() {
	    this(SafeHtmlUtils.fromString(""), null);
  }
  
  public GroupSeparatorMenuItemAppearance(SafeHtml content, String iconUrl) {
    this(GWT.<GroupSeparatorMenuItemResources> create(GroupSeparatorMenuItemResources.class),
        GWT.<SeparatorMenuItemTemplate> create(GroupSeparatorMenuItemTemplate.class), content, iconUrl);
  }

  public GroupSeparatorMenuItemAppearance(GroupSeparatorMenuItemResources resources,
		  SeparatorMenuItemTemplate template, SafeHtml content, String iconUrl) {
    super(resources, template);
    this.style = resources.style();
    this.content = content;
    this.iconUrl = iconUrl;
  }

  @Override
  public void render(SafeHtmlBuilder result) {
	    //result.append(template.template(style));
	    if (iconUrl == null || iconUrl.isEmpty())
	    	result.append(((GroupSeparatorMenuItemTemplate) template).render(style, content));	    	
	    else	
	    	result.append(((GroupSeparatorMenuItemTemplate) template).render(style, content, iconUrl));
  }
}
