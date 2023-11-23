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
package com.braintribe.devrock.mungojerry.preferences;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.braintribe.codec.CodecException;
import com.braintribe.commons.preferences.StringEditor;
import com.braintribe.devrock.mungojerry.plugin.Mungojerry;
import com.braintribe.model.malaclypse.cfg.preferences.gwt.GwtPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.mj.MungojerryPreferences;



public class MungojerryPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
protected Font bigFont;
	
		
	private StringEditor gwtFileEditor;
	
	@Override
	public void init(IWorkbench arg0) {
		setPreferenceStore(Mungojerry.getInstance().getPreferenceStore());
		setDescription("Mungojerry's preferences page");
		
	}

	@Override
	protected Control createContents(Composite parent) {
		Font initialFont = parent.getFont();
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( getShell().getDisplay(), fontDataBig);
		
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout( layout);
		
				
		
				
		Composite gwtGroup = new Composite( composite, SWT.NONE);
		gwtGroup.setLayout( layout);
		gwtGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		Label gwtLabel = new Label( gwtGroup, SWT.NONE);
		gwtLabel.setText( "Settings for GWT");
		gwtLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
		gwtLabel.setFont(bigFont);
		
		String gwtFile = Mungojerry.getInstance().getMungojerryPreferences(false).getGwtPreferences().getAutoInjectLibrary();
		gwtFileEditor = new StringEditor();
		gwtFileEditor.setSelection( gwtFile);
		Composite gwtComposite = gwtFileEditor.createControl(gwtGroup, "&GWT model auto inject gwt-user jar");
		gwtComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
				        	
    	composite.pack();
		return composite;
	}

	@Override
	protected void performApply() {
		MungojerryPreferences preferences = Mungojerry.getInstance().getMungojerryPreferences(false);
		GwtPreferences gwtPreferences = preferences.getGwtPreferences();
		gwtPreferences.setAutoInjectLibrary( gwtFileEditor.getSelection());
		try {
			new GwtPreferencesCodec( Mungojerry.getInstance().getPreferenceStore()).decode(gwtPreferences);
		} catch (CodecException e) {
			Mungojerry.log(IStatus.ERROR, "cannot write preferences to IPreferencesStore as " + e.getLocalizedMessage());
			e.printStackTrace();
		}		
	}

	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}

	@Override
	public boolean performCancel() {
		MungojerryPreferences preferences = Mungojerry.getInstance().getMungojerryPreferences(false);
		GwtPreferences gwtPreferences = preferences.getGwtPreferences();
		gwtFileEditor.setSelection(gwtPreferences.getAutoInjectLibrary());
		return super.performCancel();
	}

	

}
