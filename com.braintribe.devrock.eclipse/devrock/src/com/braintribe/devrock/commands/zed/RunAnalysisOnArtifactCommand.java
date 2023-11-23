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
package com.braintribe.devrock.commands.zed;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

import com.braintribe.devrock.api.selection.EnhancedSelectionExtracter;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.bridge.eclipse.api.McBridge;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.devrock.zarathud.model.ClassesProcessingRunnerContext;
import com.braintribe.devrock.zarathud.runner.api.ZedWireRunner;
import com.braintribe.devrock.zarathud.runner.wire.ZedRunnerWireTerminalModule;
import com.braintribe.devrock.zarathud.runner.wire.contract.ZedRunnerContract;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * run a zed analysis on a resolved artifact
 *  
 * @author pit
 *
 */
public class RunAnalysisOnArtifactCommand extends AbstractHandler implements ZedRunnerTrait{
	
			
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ISelection selection = SelectionExtracter.currentSelection();
		List<VersionedArtifactIdentification> vais = new ArrayList<>();	
		if (selection!= null) {
			List<EnhancedCompiledArtifactIdentification> extractSelectedArtifacts = EnhancedSelectionExtracter.extractSelectedArtifacts(selection);
			if (extractSelectedArtifacts != null && extractSelectedArtifacts.size() > 0) {
				extractSelectedArtifacts.stream().map( ecai -> VersionedArtifactIdentification.parse( ecai.asString())).forEach( vais::add);
			}
		}
		
		// Dialog to select analysis target	
		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
		///
		ZedTargetDialog targetSelector = new ZedTargetDialog( shell);
		targetSelector.setInitialIdentifications(vais);
		
		int retval = targetSelector.open();
		if (retval == org.eclipse.jface.dialogs.Dialog.CANCEL) {
			return null;
		}
						
		Maybe<RepositoryConfiguration> customRepositoryConfigurationMaybe = targetSelector.getCustomRepositoryConfiguration();
		
		McBridge mcBridge = ZedRunnerTrait.produceMcBridgeForExtraction( customRepositoryConfigurationMaybe);
						
		Maybe<List<CompiledTerminal>> targetsMaybe = targetSelector.getSelection();
		if (targetsMaybe.isSatisfied()) {
			CompiledTerminal compiledTerminal = targetsMaybe.get().get(0);			
			processTerminal(compiledTerminal, mcBridge);			
		}
		else {
			DevrockPluginStatus status = new DevrockPluginStatus("no selection available", (Reason) targetsMaybe.whyUnsatisfied());
			DevrockPlugin.instance().log(status);
		}	
		 
		return null;
	}

	

	private void processTerminal( CompiledTerminal compiledTerminal, McBridge mcBridge) {
		Maybe<ClassesProcessingRunnerContext> contextMaybe = ZedRunnerTrait.produceContext(compiledTerminal, mcBridge);
		
		if (contextMaybe.isUnsatisfied()) {
			DevrockPluginStatus status = new DevrockPluginStatus("cannot process terminal : " + compiledTerminal.asString(), (Reason) contextMaybe.whyUnsatisfied());
			DevrockPlugin.instance().log(status);
			return ;
		}
		ClassesProcessingRunnerContext cprContext = contextMaybe.get();
		
		// c) run												
		try (WireContext<ZedRunnerContract> wireContext = Wire.context( ZedRunnerWireTerminalModule.INSTANCE)) {																
			ZedWireRunner zedWireRunner = wireContext.contract().classesRunner(cprContext);
			
			Job job = new WorkbenchJob("running zed's analysis") {						
				@Override
				public IStatus runInUIThread(IProgressMonitor arg0) {
					zedWireRunner.run();
					ZedRunnerTrait.displayZedAnalysisResult( compiledTerminal.asString(), zedWireRunner, null);														
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}											
	}
}

