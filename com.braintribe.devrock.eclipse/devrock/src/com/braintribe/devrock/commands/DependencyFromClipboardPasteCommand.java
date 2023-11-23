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
package com.braintribe.devrock.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;

import com.braintribe.devrock.api.clipboard.ArtifactToClipboardExpert;
import com.braintribe.devrock.api.clipboard.ClipboardEntry;
import com.braintribe.devrock.api.commands.AbstractDropdownCommandHandler;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.eclipse.model.actions.VersionModificationAction;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;


/**
 * command the inject dependencies from the clipboard into the selected projects
 * @author pit
 *
 */
public class DependencyFromClipboardPasteCommand extends AbstractDropdownCommandHandler {	
	private static Logger log = Logger.getLogger(DependencyFromClipboardPasteCommand.class);
	private String PARM_DIRECT = VersionModificationAction.untouched.name();//"direct";
	private String PARM_RANGIFY = VersionModificationAction.rangified.name();//"rangify";
	private String PARM_VARIABLE = VersionModificationAction.referenced.name();//"variable";	
	private VersionModificationAction lastUsedPasteMode = VersionModificationAction.referenced;
	private IProject target;
	
	// declare the param here
	{
		PARM_MSG = "com.braintribe.devrock.artifactcontainer.common.commands.command.param.paste";		
	}
	
	
	public DependencyFromClipboardPasteCommand() {	
	}
	
	public DependencyFromClipboardPasteCommand(IProject target) {
		this.target = target;
		
	}
	
	private void performInjection(VersionModificationAction mode) {

		// get contents from Clipoard
		boolean canPaste = false;
		
		// get pom file
		
		if (target == null) {
				
			target = SelectionExtracter.currentProject();
			if (target == null) {
				log.debug("no project currently selected");
				return;
			}
		}

		Clipboard clipboard = new Clipboard(Display.getCurrent());
		
		// see whether there are structured data of the copied artifacts 
		ClipboardEntry[] storedArtifactsOfCurrentClipboard = ArtifactToClipboardExpert.getStoredArtifactsOfCurrentClipboard(clipboard);
		
		if (storedArtifactsOfCurrentClipboard == null) {
			// no real artifacts attached to the clipoard, use the text-insert feature.. 
			TextTransfer plainTextTransfer = TextTransfer.getInstance();
			TransferData[] td = clipboard.getAvailableTypes();
	  		for (int i = 0; i < td.length; ++i) {
	  			if (TextTransfer.getInstance().isSupportedType(td[i])) {
	  				canPaste = true;
	  				break;
	  			}
	  		}
	  		if (!canPaste)
	  			return;
	  		
			String cliptxt = (String)clipboard.getContents(plainTextTransfer, DND.CLIPBOARD);
			if (cliptxt == null || cliptxt.length() == 0) {
				return;
			}
			ArtifactToClipboardExpert.injectDependenciesIntoProject( target, mode, cliptxt);	
		}
		else {
			// actual artifacts in the clipboard..
			List<CompiledDependencyIdentification> ecais = new ArrayList<>( storedArtifactsOfCurrentClipboard.length);
			for (ClipboardEntry entry : storedArtifactsOfCurrentClipboard) {
				ecais.add( entry.getIdentification());
			}
			ArtifactToClipboardExpert.injectDependenciesIntoProject( target, mode, ecais);
		}			
	}
	
	public void process( VersionModificationAction mode) {
		performInjection(mode);
	}

	@Override
	public void process(String param) {		
		VersionModificationAction pasteMode = lastUsedPasteMode;
		if (param == null) {
			;
		}
		else if (param.equalsIgnoreCase( PARM_DIRECT)) {
			pasteMode = VersionModificationAction.untouched;
		}
		else if (param.equalsIgnoreCase( PARM_RANGIFY)) {
			pasteMode = VersionModificationAction.rangified;
		}
		else if (param.equalsIgnoreCase( PARM_VARIABLE)) {
			pasteMode = VersionModificationAction.referenced;	
		}
		performInjection(pasteMode);		
	}


}
