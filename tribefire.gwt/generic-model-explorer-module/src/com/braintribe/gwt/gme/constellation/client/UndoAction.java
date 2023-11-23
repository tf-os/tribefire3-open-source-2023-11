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

public class UndoAction extends Action implements TransactionFrameListener, KnownGlobalAction {
	
	private static final String KNOWN_NAME = "undo";
	private PersistenceGmSession gmSession;
	private List<UndoActionListener> undoActionListeners;
	private int currentUndoSize = 0;
	private Timer manipulationTimer;
	
	public void setGmSession(PersistenceGmSession gmSession) {
		if (this.gmSession != null)
			this.gmSession.getTransaction().removeTransactionFrameListener(this);
		
		this.gmSession = gmSession;
		
		gmSession.getTransaction().addTransactionFrameListener(this);
		gmSession.listeners().add((ManipulationListener) manipulation -> getManipulationTimer().schedule(200));
	}
	
	public UndoAction() {
		setName(LocalizedText.INSTANCE.undo());
		setTooltip(LocalizedText.INSTANCE.undo());
		setIcon(ConstellationResources.INSTANCE.undo());
		setHoverIcon(ConstellationResources.INSTANCE.undoSmall());
		setEnabled(false);
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		boolean undoAll = false;
		if (triggerInfo != null) {
			Boolean all = triggerInfo.get("UndoAll");
			undoAll = (all == null) ? false : all;			
		}
		
		if (undoAll) 
			undoAllManipulations();
		else	
			undoManipulation();
	}
	
	@Override
	public String getKnownName() {
		return KNOWN_NAME;
	}
	
	/**
	 * Undoes the current manipulation to undo on the given {@link PersistenceGmSession}.
	 */
	public static void undoManipulation(PersistenceGmSession gmSession) {
		undoManipulations(1, gmSession);
	}
	
	/**
	 * Undoes all manipulations on the given {@link PersistenceGmSession}.
	 */
	public static void undoAllManipulations(PersistenceGmSession gmSession) {
		undoManipulations(gmSession.getTransaction().getManipulationsDone().size(), gmSession);
	}
	
	public void undoAllManipulations() {
		undoManipulations(gmSession.getTransaction().getManipulationsDone().size(), gmSession);
	}
	
	@Override
	public void onDoUndoStateChanged(TransactionFrame transactionFrame) {
		boolean canUndo = transactionFrame.canUndo();
		setEnabled(canUndo);
		
		/*if (canUndo) {
			Manipulation manipulationToUndo = transactionFrame.getManipulationsDone().get(transactionFrame.getManipulationsDone().size() - 1);
			setTooltip(LocalizedText.INSTANCE.undo() + " - " + manipulationToUndo.getDescription());
		}*/
	}
	
	public void addUndoActionListener(UndoActionListener listener) {
		if (undoActionListeners == null)
			undoActionListeners = new ArrayList<>();
		
		undoActionListeners.add(listener);
	}
	
	public void removeUndoActionListener(UndoActionListener listener) {
		if (undoActionListeners != null) {
			undoActionListeners.remove(listener);
			if (undoActionListeners.isEmpty())
				undoActionListeners = null;
		}
	}
	
	private void undoManipulation() {
		undoManipulation(gmSession);
	}
	
	private static void undoManipulations(int steps, PersistenceGmSession gmSession) {
		Transaction transaction = gmSession.getTransaction();
		try {
			transaction.undo(steps);
		} catch (TransactionException e) {
			ErrorDialog.show(LocalizedText.INSTANCE.errorUndoing(), e);
			e.printStackTrace();
		}
	}
	
	private Timer getManipulationTimer() {
		if (manipulationTimer == null) {
			manipulationTimer = new Timer() {
				@Override
				public void run() {
					int undoSize = UndoAction.this.gmSession.getTransaction().getManipulationsDone().size();
					if (undoSize != currentUndoSize) {
						currentUndoSize = undoSize;
						fireUndoStateChanged(undoSize);
					}
				}
			};
		}
		
		return manipulationTimer;
	}
	
	private void fireUndoStateChanged(int manipulationsToUndo) {
		if (undoActionListeners != null) {
			for (UndoActionListener listener : undoActionListeners)
				listener.onUndoStateChanged(manipulationsToUndo);
		}
	}
	
	@FunctionalInterface
	public static interface UndoActionListener {
		void onUndoStateChanged(int manipulationsToUndo);
	}
	
}
