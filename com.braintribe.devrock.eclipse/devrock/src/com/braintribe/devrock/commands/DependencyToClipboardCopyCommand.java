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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;

import com.braintribe.devrock.api.clipboard.ArtifactToClipboardExpert;
import com.braintribe.devrock.api.commands.AbstractDropdownCommandHandler;
import com.braintribe.devrock.api.selection.EnhancedSelectionExtracter;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.eclipse.model.actions.VersionModificationAction;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.eclipse.model.identification.RemoteCompiledDependencyIdentification;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;

/**
 * command to copy workspace projects into the clipboard as dependencies 
 * @author pit
 *
 */
public class DependencyToClipboardCopyCommand extends AbstractDropdownCommandHandler {	
	private String PARM_DIRECT = VersionModificationAction.untouched.name();//"direct";
	private String PARM_RANGIFY = VersionModificationAction.rangified.name();//"rangify";
	private String PARM_VARIABLE = VersionModificationAction.referenced.name();//"variable";
	
	private Clipboard clipboard;
	private VersionModificationAction lastUsedCopyMode = VersionModificationAction.referenced;
	
	// must declare the parameter that this commands wants
	{
		PARM_MSG = "com.braintribe.devrock.artifactcontainer.common.commands.command.param.copy";
	}
	
	
	@Override
	public void process(String param) {
				
		VersionModificationAction copyMode = lastUsedCopyMode;
		if (param == null) {
			;
		}
		else if (param.equalsIgnoreCase( PARM_DIRECT)) {
			copyMode = VersionModificationAction.untouched;
		}
		else if (param.equalsIgnoreCase( PARM_RANGIFY)) {
			copyMode = VersionModificationAction.rangified;
		}
		else if (param.equalsIgnoreCase( PARM_VARIABLE)) {
			copyMode = VersionModificationAction.referenced;	
		}		
		performCopy(copyMode);
		
	}

	
	private void performCopy( VersionModificationAction action) {
		ISelection selection = SelectionExtracter.currentSelection();
		// get selected container entry 
		List<EnhancedCompiledArtifactIdentification> identifiedArtifacts = EnhancedSelectionExtracter.extractEitherJarEntriesOrOwnerArtifacts(selection);
		
		if (identifiedArtifacts.size() > 0) {
			if (clipboard != null) {
				clipboard.dispose();
			}
			List<RemoteCompiledDependencyIdentification> rcdis = identifiedArtifacts.stream().map( ecai -> RemoteCompiledDependencyIdentification.from( ecai)).collect(Collectors.toList());
			clipboard = ArtifactToClipboardExpert.copyToClipboard( action, rcdis);
		}
		else {
			DevrockPluginStatus status = new DevrockPluginStatus( "cannot identify any artifacts from selection", IStatus.WARNING);
			DevrockPlugin.instance().log(status);	
		}
		
	}
	
}
