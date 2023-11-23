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
package com.braintribe.gwt.gme.workbench.client;

import java.util.List;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.UndoAction;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.persistence.CommitListener;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.user.client.Timer;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.MessageBox;

/**
 * Expert with Save related operations for the {@link Workbench}.
 * @author michel.docouto
 *
 */
public class WorkbenchSaveExpert {
	
	private Workbench workbench;
	private Timer manipulationTimer;
	
	public void configureWorkbench(Workbench workbench) {
		this.workbench = workbench;
		
		workbench.getWorkbenchSession().listeners().add((ManipulationListener) manipulation -> {
			if (!workbench.isSessionListenerSuspended)
				getManipulationTimer().schedule(50);
		});
		
		workbench.getWorkbenchSession().listeners().add(new CommitListener() {
			@Override
			public void onBeforeCommit(PersistenceGmSession session, Manipulation manipulation) {
				//NOP
			}
			
			@Override
			public void onAfterCommit(PersistenceGmSession session, Manipulation manipulation, Manipulation inducedManipluation) {
				refreshFoldersAfterCommit();
			}
		});
	}
	
	public void saveManipulations() {
		final List<Manipulation> manipulationsDone = workbench.getWorkbenchSession().getTransaction().getManipulationsDone();
		if (manipulationsDone == null || manipulationsDone.isEmpty())
			return;
		
		confirmSave(manipulationsDone).andThen(proceed -> saveManipulations(proceed, manipulationsDone));
	}
	
	private Timer getManipulationTimer() {
		if (manipulationTimer == null) {
			manipulationTimer = new Timer() {
				@Override
				public void run() {
					saveManipulations();
				}
			};
		}
		
		return manipulationTimer;
	}
	
	private Future<Boolean> confirmSave(List<Manipulation> manipulations) {
		final Future<Boolean> future = new Future<>();
		if (needsConfirmation(manipulations)) {
			MessageBox box = new MessageBox(LocalizedText.INSTANCE.save(), LocalizedText.INSTANCE.saveConfirmation());
			box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO);
			box.setIcon(MessageBox.ICONS.question());
			box.addDialogHideHandler(event -> future.onSuccess(event.getHideButton().equals(PredefinedButton.YES)));
			box.show();
		} else
			future.onSuccess(true);
		
		return future;
	}
	
	private boolean needsConfirmation(List<Manipulation> manipulations) {
		for (Manipulation manipulation : manipulations) {
			if (needsConfirmation(manipulation))
				return true;
		}
		
		return false;
	}
	
	private boolean needsConfirmation(Manipulation manipulation) {
		if (manipulation instanceof RemoveManipulation || manipulation instanceof ClearCollectionManipulation || manipulation instanceof DeleteManipulation)
			return true;
		
		return false;
	}
	
	private void saveManipulations(Boolean proceed, List<Manipulation> manipulationsDone) {
		ModelEnvironmentDrivenGmSession workbenchSession = workbench.getWorkbenchSession();
		if (!proceed) {
			for (int i = 0; i < manipulationsDone.size(); i++)
				UndoAction.undoManipulation(workbenchSession);
			return;
		}
		
		List<Folder> workbenchFolders = workbench.getRootFolders();
		
		for (Manipulation manipulationDone : manipulationsDone) {
			if (!(manipulationDone instanceof RemoveManipulation))
				continue;
			
			Folder folderRemoved = null;
			for (Object value : ((RemoveManipulation) manipulationDone).getItemsToRemove().values()) {
				if (value instanceof Folder) {
					folderRemoved = (Folder) value;
					break;
				}
			}
			
			if (folderRemoved != null && workbenchFolders.contains(folderRemoved))
				workbench.removeRootFolder(folderRemoved);
		}
		
		GlobalState.mask(LocalizedText.INSTANCE.savingChanges());
		workbenchSession.commit(AsyncCallback.of( //
				response -> GlobalState.unmask(), //
				e -> {
					GlobalState.unmask();
					ErrorDialog.show(LocalizedText.INSTANCE.errorApplyingManipulations(), e);
					e.printStackTrace();
				}));
	}
	
	private void refreshFoldersAfterCommit() {
		List<WorkbenchPerspective> perspectives = workbench.dataSession.getModelEnvironment().getPerspectives();
		for (WorkbenchPerspective perspective : perspectives) {
			if (!Workbench.WORKBENCH_PERSPECTIVE_NAME.equals(perspective.getName()))
				continue;
			
			List<Folder> folders = perspective.getFolders();
			Folder folder = folders.isEmpty() ? null : folders.get(0);
			if (folder == null)
				break;
			
			try {
				workbench.suspendHistoryListener();
				workbench.getWorkbenchSession().merge().adoptUnexposed(true).suspendHistory(true).doFor(folder,
						AsyncCallback.of(mergedFolder -> workbench.resumeHistoryListener(), t -> {
							t.printStackTrace();
							workbench.resumeHistoryListener();
						}));
			} catch (GmSessionException e) {
				e.printStackTrace();
				workbench.resumeHistoryListener();
			}
			
			break;
		}
	}

}
