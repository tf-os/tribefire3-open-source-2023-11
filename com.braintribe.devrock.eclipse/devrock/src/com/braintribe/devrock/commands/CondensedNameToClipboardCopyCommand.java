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

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.api.selection.EnhancedSelectionExtracter;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;

/**
 * copies condensed names of all selected entries in the package explorer 
 * @author pit
 *
 */
public class CondensedNameToClipboardCopyCommand extends AbstractHandler{
	
	private Clipboard clipboard;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = SelectionExtracter.currentSelection();
		// direct project selection 
		List<EnhancedCompiledArtifactIdentification> selected = EnhancedSelectionExtracter.extractSelectedArtifacts(selection);
		
		if (selected.size() > 0) {
			copyToClipboard( selected);
		}
		else {
			DevrockPluginStatus status = new DevrockPluginStatus( "cannot identify any artifacts from selection", IStatus.WARNING);
			DevrockPlugin.instance().log(status);	
		}
		return null;
	}
	
	
			
	private void copyToClipboard( List<EnhancedCompiledArtifactIdentification> artifacts ) {		
		if (clipboard != null) {
			clipboard.dispose();
		}
		Display display = PlatformUI.getWorkbench().getDisplay();
	
		String text = artifacts.stream().map( vai -> vai.asString()).collect( Collectors.joining("\n"));		
		clipboard = new Clipboard( display);
		clipboard.setContents( new Object [] {text}, new Transfer[] { TextTransfer.getInstance() });
		
	}
			
}
