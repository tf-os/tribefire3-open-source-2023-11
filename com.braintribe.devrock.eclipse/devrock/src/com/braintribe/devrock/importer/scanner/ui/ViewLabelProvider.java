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
package com.braintribe.devrock.importer.scanner.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.stylers.Stylers;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.eclipse.model.identification.SelectableEnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.eclipse.model.scan.SourceRepositoryEntry;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.utils.lcd.LazyInitialized;

public class ViewLabelProvider extends CellLabelProvider implements IStyledLabelProvider {
	
	private static final String KEY_GRP = "grp";
	private static final String KEY_PATH = "path";
	private static final String KEY_IDENTIFICATION = "identification";

	private final String propertyName;
	
	private UiSupport uisSupport = DevrockPlugin.instance().uiSupport();
	
	private static Styler validNameStyler;
	private static Styler validButInWsNameStyler;
	private static Styler invalidEntryStyler;
	private static Styler groupEntryStyler;
	private static Styler pathKeyStyler;
	private static Styler pathStyler;
		
	private final LazyInitialized<List<SourceRepositoryEntry>> scanEntries = new LazyInitialized<>( this::initScanDirectories);
	private QuickImportAction action;
	
	
	private List<SourceRepositoryEntry> initScanDirectories() {
		List<SourceRepositoryEntry> entries = DevrockPlugin.envBridge().getScanRepositories();
		return entries;		
	}

	public ViewLabelProvider(String propertyName, Font initialFont, QuickImportAction action) {
		this.propertyName = propertyName;
		this.action = action;
		
		Stylers stylers = uisSupport.stylers("qi-dialog");
		stylers.setInitialFont( initialFont);
		stylers.addStandardStylers();
		
		validNameStyler = stylers.styler("bold");//new ParametricStyler( initialFont, SWT.BOLD, null, null);
		validButInWsNameStyler = stylers.addStyler( "bold-italic", SWT.BOLD | SWT.ITALIC); //new ParametricStyler(initialFont, SWT.BOLD | SWT.ITALIC, null, null);		
		invalidEntryStyler = stylers.styler( "italic"); //new ParametricStyler(initialFont, SWT.ITALIC, null, null);
		groupEntryStyler = stylers.styler("default"); // new ParametricStyler(initialFont, null, null, null);
		pathKeyStyler = stylers.styler( "default"); //new ParametricStyler(initialFont, null, null, null);
		pathStyler = stylers.styler( "default"); //new ParametricStyler(initialFont, null, null, null);
	}

	@Override
	public StyledString getStyledText(Object object) {
		if (object instanceof String) {
			if (propertyName.equals( KEY_IDENTIFICATION))
				return createStyledString( (String) object, KEY_GRP);			
		}
		if (object instanceof EnhancedCompiledArtifactIdentification == false)		
			return createStyledString( (String) object, null);	
		
		SelectableEnhancedCompiledArtifactIdentification ecai = (SelectableEnhancedCompiledArtifactIdentification) object;		
		
		//
		switch (propertyName) {
			case KEY_IDENTIFICATION:								
				return createStyledString( ecai, "id");
			case KEY_PATH: 
				return createStyledString( ecai, KEY_PATH);			
			default:
				break;					
		}
		
		return new StyledString("");
	}
	
	/**
	 * create a {@link StyledString} for a String (which is either group or nothing) 
	 * @param grp - the String's value
	 * @param key - the key (if any)
	 * @return - the {@link StyledString}
	 */
	private StyledString createStyledString( String grp, String key) {
		if (key != null && key.equals(KEY_GRP))
			return new StyledString( grp, groupEntryStyler);
		return new StyledString(); 
	}
	
	
	/**
	 * create a {@link StyledString} for a {@link SelectableEnhancedCompiledArtifactIdentification}
	 * @param ecai - the {@link SelectableEnhancedCompiledArtifactIdentification}
	 * @param key - the key 
	 * @return - the {@link StyledString}
	 */
	private StyledString createStyledString( SelectableEnhancedCompiledArtifactIdentification ecai, String key) {
		// validity : always ok if dependency import requested, otherwise depends on current state 
		boolean isValid = action == QuickImportAction.importDependency || ecai.getCanBeImportedWithCurrentChoices();
		switch (key) {
			// the identification is the name of the artifact 
			case "id" :
				if (isValid) {
					Styler style = ecai.getExistsInWorkspace() ? validButInWsNameStyler : validNameStyler;

					// valid artifact (can be imported) 
					// artifact id normal, version emphasis
					StyledString ss = new StyledString( ecai.getArtifactId() + " " + ecai.getVersion().asString());
					ss.setStyle(0, ecai.getArtifactId().length(), style);
					return ss;
				}
				else {
					// invalid artifact (cannot be imported) 
					return new StyledString( ecai.getArtifactId() + " " + ecai.getVersion().asString(), invalidEntryStyler);
				}
			// the path
			case KEY_PATH :
				String origin = ecai.getOrigin();
				SourceRepositoryEntry entry = scanEntries.get().stream().filter( se -> origin.startsWith( se.getActualFile())).findFirst().orElse( null);
				StyledString text = null;
				if (entry != null) {
					File actualFile = new File(entry.getActualFile());
					File originFile = new File( origin);
					Path path = actualFile.toPath().relativize( originFile.toPath());
					// format path here... 
					if (isValid) {						
						text = new StyledString( entry.getKey() + ":" + path.toString());		
						int keyLength = entry.getKey().length();
						text.setStyle(0, keyLength, pathKeyStyler);
						text.setStyle(keyLength, text.length() - keyLength, pathStyler);
					} else {
						text = new StyledString( entry.getKey() + ":" + path.toString(), invalidEntryStyler);
					}
				}
				else {
					text = new StyledString( origin);
				}
				return text;
			default:
				// standard
				return new StyledString();
		}		
	}

	
	
	@Override
	public String getToolTipText(Object element) {
		if (element instanceof String) {
			if (propertyName.equals( KEY_IDENTIFICATION))
				return "group id";
			return null;
		}
		else if (element instanceof SelectableEnhancedCompiledArtifactIdentification) {
			SelectableEnhancedCompiledArtifactIdentification secai = (SelectableEnhancedCompiledArtifactIdentification) element;
			boolean canBeImported = secai.getCanBeImportedWithCurrentChoices();
			
			switch (propertyName) {
				case KEY_IDENTIFICATION:
					if (action != QuickImportAction.importDependency) {
						return !canBeImported ? secai.asString() + " cannot be imported with the current import choices" : secai.asString() + "can be imported";
					}
					else {
						return secai.asString() + " can be injected as a dependency";	
					}
				case KEY_PATH:
					if (action != QuickImportAction.importDependency) {
						return !canBeImported ? "cannot be imported with the current import choices from " + secai.getOrigin() : "can be imported from " + secai.getOrigin();
					}
					else {
						return secai.asString() + " can be injected as a dependency (from " + secai.getOrigin() + ")";	
					}
				default:
					break;					
			}
		}

		return super.getToolTipText(element);
	}


	@Override
	public void update(ViewerCell arg0) {
		System.out.println();
	}

	@Override
	public void dispose() {		
	}

	@Override
	public Image getImage(Object arg0) {
		return null;
	}
	
	

}
