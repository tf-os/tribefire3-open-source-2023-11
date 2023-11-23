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
package com.braintribe.model.processing.cortex.service.access;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.cortexapi.access.ExplorerStyle;
import com.braintribe.model.cortexapi.access.SetupAccessResponse;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.cortex.service.ServiceBase;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.workbench.WorkbenchInstructionProcessor;
import com.braintribe.model.workbench.KnownWorkenchPerspective;
import com.braintribe.model.workbench.instruction.AddFolder;
import com.braintribe.model.workbench.instruction.WorkbenchInstruction;
import com.braintribe.utils.i18n.I18nTools;

public class WorkbenchConfiguration extends ServiceBase {

	private static final Logger logger = Logger.getLogger(WorkbenchSetup.class);

	protected PersistenceGmSessionFactory sessionFactory;
	protected IncrementalAccess access;
	protected List<WorkbenchInstruction> standardInstructions;
	private Map<ExplorerStyle, List<WorkbenchInstruction>> styleInstructions = new HashMap<>();

	public WorkbenchConfiguration(IncrementalAccess access, List<WorkbenchInstruction> instructions,
			Map<ExplorerStyle, List<WorkbenchInstruction>> styleInstructions,
			PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		this.access = access;
		this.standardInstructions = instructions;
		this.styleInstructions = styleInstructions;
	}

	public SetupAccessResponse run(boolean ensureExplorerPerspectives, ExplorerStyle explorerStyle,
			List<WorkbenchInstruction> additionalInstructions) {

		if (access == null) {
			return createConfirmationResponse("Please provide an access!", Level.ERROR, SetupAccessResponse.T);
		}

		IncrementalAccess workbenchAccess = access.getWorkbenchAccess();
		// TODO Setup the workbench automatically if wb access is missing - pending
		// approval from CORE
		// if (workbenchAccess == null) {
		// WorkbenchSetup setup = new WorkbenchSetup(persistenceGmSession, access,
		// baseWorkbenchModelName,
		// baseWorkbenchAccessId);
		// access = setup.runEmbedded(resetExistingAccess, true); //
		// resetExistingModel);
		// workbenchAccess = access.getWorkbenchAccess();
		//
		// logger.info("Workspace was not setup prior to configuration, performing the
		// setup automatically.");
		// }

		if (workbenchAccess == null) {
			return createConfirmationResponse(
					"Automated workbench setup was not successful. Please setup the workbench manually before proceeding.",
					Level.ERROR, SetupAccessResponse.T);
		}

		PersistenceGmSession workbenchSession = createWorkbenchSession(workbenchAccess);
		if (workbenchSession == null) {
			return createConfirmationResponse(
					"Could not create session to workbench access: " + workbenchAccess.getExternalId(), Level.ERROR,
					SetupAccessResponse.T);
		}

		List<WorkbenchInstruction> instructionsToExecute = new ArrayList<>();

		if (ensureExplorerPerspectives) {
			instructionsToExecute.addAll(standardInstructions);
			instructionsToExecute.add(createTopLevelEntryPointInstruction());
		}

		if (explorerStyle != null) {
			instructionsToExecute.addAll(getExplorerStyleInstructions(explorerStyle));
		}

		if (!additionalInstructions.isEmpty()) {
			instructionsToExecute.addAll(additionalInstructions);
		}

		WorkbenchInstructionProcessor processor = new WorkbenchInstructionProcessor();
		processor.setSession(workbenchSession);
		processor.setCommitAfterProcessing(true);
		processor.processInstructions(instructionsToExecute);

		return createResponse("Configured Workbench for access: " + access.getExternalId(), SetupAccessResponse.T);
	}

	private PersistenceGmSession createWorkbenchSession(IncrementalAccess workbenchAccess) {
		try {

			PersistenceGmSession workbenchSession = sessionFactory.newSession(workbenchAccess.getExternalId());
			return workbenchSession;

		} catch (Exception e) {
			notifyError("Could not create session to workbench access: " + workbenchAccess.getExternalId());
			logger.error("Could not create session to workbench access: " + workbenchAccess.getExternalId(), e);
		}
		return null;
	}

	private WorkbenchInstruction createTopLevelEntryPointInstruction() {
		Folder folderToAdd = Folder.T.create();
		folderToAdd.setName(access.getExternalId());
		folderToAdd.setDisplayName(I18nTools.createLs(access.getName()));

		AddFolder instruction = AddFolder.T.create();
		instruction.setPath(KnownWorkenchPerspective.root.name());
		instruction.setFolderToAdd(folderToAdd);
		return instruction;
	}

	private List<WorkbenchInstruction> getExplorerStyleInstructions(ExplorerStyle explorerStyle) {
		if (explorerStyle != null) {
			List<WorkbenchInstruction> styleInstructions = this.styleInstructions.get(explorerStyle);
			if (styleInstructions != null) {
				return styleInstructions;
			}
		}
		return Collections.emptyList();
	}

}
