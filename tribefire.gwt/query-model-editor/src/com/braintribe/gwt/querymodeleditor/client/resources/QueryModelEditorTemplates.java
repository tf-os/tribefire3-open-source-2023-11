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
package com.braintribe.gwt.querymodeleditor.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.core.client.XTemplates;

public interface QueryModelEditorTemplates extends XTemplates {

	public static final QueryModelEditorTemplates INSTANCE = GWT.create(QueryModelEditorTemplates.class);

	@XTemplate(source = "qmePanel.html")
	SafeHtml qmePanel(TemplateConfigurationBean bean);

	@XTemplate(source = "qmeAdvancedPanel.html")
	SafeHtml qmeAdvancedPanel(TemplateConfigurationBean bean);

	@XTemplate(source = "pagenationControl.html")
	SafeHtml pagenationControl(TemplateConfigurationBean bean);

	@XTemplate(source = "dropDownControl.html")
	SafeHtml dropDownControl(TemplateConfigurationBean bean);

	@XTemplate(source = "qacDialogPanel.html")
	SafeHtml qacDialogPanel(TemplateConfigurationBean bean);
}
