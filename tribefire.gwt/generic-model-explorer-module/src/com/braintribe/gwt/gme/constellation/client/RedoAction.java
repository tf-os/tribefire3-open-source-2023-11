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
package com.braintribe.gwt.gme.constellation.client;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.action.client.KnownGlobalAction;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.Transaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.braintribe.model.processing.session.api.transaction.TransactionFrame;
import com.braintribe.model.processing.session.api.transaction.TransactionFrameListener;
import com.google.gwt.user.client.Timer;

/**
 * Button responsible for performing redo manipulations.
 * @author michel.docouto
 *
 */
public class RedoAction extends Action implements TransactionFrameListener, KnownGlobalAction {
	
	private static final String KNOWN_NAME = "redo";
	private PersistenceGmSession gmSession;
	private List<RedoActionListener> redoActionListeners;
	private int currentRedoSize = 0;
	private Timer manipulationTimer;
	
	public void setGmSession(PersistenceGmSession gmSession) {
		if (this.gmSession != null)
			this.gmSession.getTransaction().removeTransactionFrameListener(this);
		
		this.gmSession = gmSession;
		
		gmSession.getTransaction().addTransactionFrameListener(this);
		gmSession.listeners().add((ManipulationListener) manipulation -> getManipulationTimer().schedule(200));
	}
	
	public RedoAction() {
		setName(LocalizedText.INSTANCE.redo());
		setTooltip(LocalizedText.INSTANCE.redo());
		setEnabled(false);
		setIcon(ConstellationResources.INSTANCE.redo());
		setHoverIcon(ConstellationResources.INSTANCE.redoSmall());
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		redoManipulation();
	}
	
	@Override
	public String getKnownName() {
		return KNOWN_NAME;
	}
	
	public void redoAllManipulations() {
		redoManipulations(gmSession.getTransaction().getManipulationsUndone().size());
	}
	
	@Override
	public void onDoUndoStateChanged(TransactionFrame transactionFrame) {
		boolean canRedo = transactionFrame.canRedo();
		setEnabled(canRedo);
		
		/*if (canRedo) {
			Manipulation manipulationToRedo = transactionFrame.getManipulationsUndone().get(0);
			setTooltip(LocalizedText.INSTANCE.redo() + " - " + manipulationToRedo.getDescription());
		}*/
	}
	
	public void addRedoActionListener(RedoActionListener listener) {
		if (redoActionListeners == null)
			redoActionListeners = new ArrayList<>();
		
		redoActionListeners.add(listener);
	}
	
	public void removeRedoActionListener(RedoActionListener listener) {
		if (redoActionListeners != null) {
			redoActionListeners.remove(listener);
			if (redoActionListeners.isEmpty())
				redoActionListeners = null;
		}
	}
	
	private void redoManipulation() {
		redoManipulations(1);
	}
	
	private void redoManipulations(int steps) {
		Transaction transaction = gmSession.getTransaction();
		try {
			transaction.redo(steps);
		} catch (TransactionException e) {
			ErrorDialog.show(LocalizedText.INSTANCE.errorRedoing(), e);
			e.printStackTrace();
		}
	}
	
	private Timer getManipulationTimer() {
		if (manipulationTimer != null)
			return manipulationTimer;
		
		manipulationTimer = new Timer() {
			@Override
			public void run() {
				int redoSize = RedoAction.this.gmSession.getTransaction().getManipulationsUndone().size();
				if (redoSize != currentRedoSize) {
					currentRedoSize = redoSize;
					fireRedoStateChanged(redoSize);
				}
			}
		};
		
		return manipulationTimer;
	}
	
	private void fireRedoStateChanged(int manipulationsToRedo) {
		if (redoActionListeners != null) {
			for (RedoActionListener listener : redoActionListeners)
				listener.onRedoStateChanged(manipulationsToRedo);
		}
	}

	@FunctionalInterface
	public static interface RedoActionListener {
		void onRedoStateChanged(int manipulationsToRedo);
	}
	
}
