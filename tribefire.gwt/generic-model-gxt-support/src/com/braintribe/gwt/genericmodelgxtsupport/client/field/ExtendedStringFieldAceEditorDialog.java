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
package com.braintribe.gwt.genericmodelgxtsupport.client.field;

import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Dialog with AceEditor to be displayed when using the {@link ExtendedStringDialog}
 *
 */
public class ExtendedStringFieldAceEditorDialog extends ExtendedStringDialog {
	
	private AceEditor aceEditor;
	private String codeFormat = AceEditorMode.TEXT.getName();
	private BorderLayoutContainer aceEditorContainer;
	
	public ExtendedStringFieldAceEditorDialog() {
		super();
		addStyleName("extendedStringFieldAceEditorDialog");
		
		aceEditor = new AceEditor();	
		aceEditor.startEditor(); 
		aceEditor.setTheme(AceEditorTheme.CHROME);
		aceEditor.setAutocompleteEnabled(true);
		setMode(codeFormat);
		
		aceEditorContainer = new BorderLayoutContainer();
		aceEditorContainer.setCenterWidget(aceEditor);	
		aceEditorContainer.setStyleName("ExtendedStringFieldAceEditorContainer");
		
		updateContainer();
	}
	
	private void setMode(String codeFormat) {
		for (AceEditorMode mode : AceEditorMode.values()) {
			if (!mode.getName().equals(codeFormat))
				continue;
			try  {
				aceEditor.setMode(mode);
			} catch (Exception e) {
				aceEditor.setMode(AceEditorMode.TEXT);  //RVE - on error show at default TEXT editor
			}
			break;
		}
	}	
	
	@Override
	public String getString() {
		return aceEditor.getText();
	}
	
	@Override
	public void setString(String string) {
		cancelChanges = false;
		aceEditor.setText(string);
	}
	
	@Override
	public void setReadOnly(Boolean readOnly) {
		super.setReadOnly(readOnly);
		aceEditor.setReadOnly(readOnly);
	}
	
	@Override
	public String getCodeFormat() {
		return codeFormat;
	}

	@Override
	public void setCodeFormat(String editorMode) {
		if (editorMode == null || editorMode.isEmpty())
			return;
		
		this.codeFormat = editorMode;
		setMode(this.codeFormat);
	}
		
	@Override
	public Widget getView() {
		return aceEditorContainer;
	}

	public void updateContainer() {
		container.setCenterWidget(aceEditorContainer);
	}
	
}
