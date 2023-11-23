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
package com.braintribe.devrock.artifactcontainer.commands;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.VersionModificationAction;
import com.braintribe.plugin.commons.commands.AbstractDropdownCommandHandler;
import com.braintribe.plugin.commons.commands.ArtifactToClipboardExpert;
import com.braintribe.plugin.commons.selection.SelectionExtractor;

public class DependencyFromClipboardPasteCommand extends AbstractDropdownCommandHandler {	
	private String PARM_MSG = "com.braintribe.devrock.artifactcontainer.common.commands.command.param.paste";
	private String PARM_DIRECT = VersionModificationAction.untouched.name();//"direct";
	private String PARM_RANGIFY = VersionModificationAction.rangified.name();//"rangify";
	private String PARM_VARIABLE = VersionModificationAction.referenced.name();//"variable";
	
	private Object performInjection(VersionModificationAction mode) {

		// get contents from Clipoard
		boolean canPaste = false;
		
		// get pom file 
		IWorkbench iworkbench = PlatformUI.getWorkbench();
		IWorkbenchWindow iworkbenchwindow = iworkbench.getActiveWorkbenchWindow();
		IWorkbenchPage page =  iworkbenchwindow.getActivePage();
		ISelection selection = page.getSelection();		
		IProject project = SelectionExtractor.extractSelectedProject(selection);

		Clipboard clipboard = new Clipboard(Display.getCurrent());
		
		// see whether there are structured data of the copied artifacts 
		Artifact[] storedArtifactsOfCurrentClipboard = ArtifactToClipboardExpert.getStoredArtifactsOfCurrentClipboard(clipboard);
		
		if (storedArtifactsOfCurrentClipboard == null) {
			
			TextTransfer plainTextTransfer = TextTransfer.getInstance();
			TransferData[] td = clipboard.getAvailableTypes();
	  		for (int i = 0; i < td.length; ++i) {
	  			if (TextTransfer.getInstance().isSupportedType(td[i])) {
	  				canPaste = true;
	  				break;
	  			}
	  		}
	  		if (!canPaste)
	  			return null;
	  		
			String cliptxt = (String)clipboard.getContents(plainTextTransfer, DND.CLIPBOARD);
			if (cliptxt == null || cliptxt.length() == 0) {
				return null;
			}
			ArtifactToClipboardExpert.injectDependenciesIntoProject( project, mode, cliptxt);	
		}
		else {
			ArtifactToClipboardExpert.injectDependenciesIntoProject( project, mode, storedArtifactsOfCurrentClipboard);
		}
		
		
		// 
		return null;
	}

	@Override
	public void process(String param) {		
		VersionModificationAction pasteMode = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getQuickImportPreferences().getLastDependencyPasteMode();
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
		ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getQuickImportPreferences().setLastDependencyPasteMode(pasteMode);
	}

	@Override
	protected String getParamKey() {
		return PARM_MSG;
	}
	
	
			
}
