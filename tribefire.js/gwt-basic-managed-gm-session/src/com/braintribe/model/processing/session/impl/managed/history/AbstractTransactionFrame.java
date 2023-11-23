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
package com.braintribe.model.processing.session.impl.managed.history;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.braintribe.model.processing.session.api.transaction.TransactionFrame;
import com.braintribe.model.processing.session.api.transaction.TransactionFrameListener;

public abstract class AbstractTransactionFrame implements TransactionFrame {
	private List<TransactionFrameListener> listeners = new ArrayList<TransactionFrameListener>();
	protected LinkedList<Manipulation> doneManipulations = new LinkedList<Manipulation>();
	protected LinkedList<Manipulation> undoneManipulations = new LinkedList<Manipulation>();
	
	protected boolean lastCheckDoneManipulationsEmpty = true;
	protected boolean lastCheckUndoneManipulationsEmpty = true;
	
	protected ManagedGmSession session;
	
	protected NestedTransaction childFrame;
	 
	public AbstractTransactionFrame(ManagedGmSession session) {
		this.session = session;
	}
	
	public void setChildFrame(NestedTransaction childFrame) {
		this.childFrame = childFrame;
	}
	
	public NestedTransaction getChildFrame() {
		return childFrame;
	}

	@Override
	public void addTransactionFrameListener(TransactionFrameListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeTransactionFrameListener(TransactionFrameListener listener) {
		listeners.remove(listener);
	}
	
	protected void appendManipulation(Manipulation manipulation) {
		doneManipulations.addLast(manipulation);
		
		if (!undoneManipulations.isEmpty()) {
			undoneManipulations.clear();
		}

		checkForStateChange();
	}
	
	protected void checkForStateChange() {
		boolean doneEmpty = doneManipulations.isEmpty();
		boolean undoneEmpty = undoneManipulations.isEmpty();
		
		boolean fireEvent = doneEmpty != lastCheckDoneManipulationsEmpty || undoneEmpty != lastCheckUndoneManipulationsEmpty;
		
		lastCheckDoneManipulationsEmpty = doneEmpty;
		lastCheckUndoneManipulationsEmpty = undoneEmpty;
		
		if (fireEvent)
			fireDoUndoStateChanged();
	}
	
	protected void fireDoUndoStateChanged() {
		for (TransactionFrameListener listener: listeners) {
			listener.onDoUndoStateChanged(this);
		}
	}

	protected abstract void onManipulationUndone(Manipulation manipulation);
	protected abstract void onManipulationDone(Manipulation manipulation);
	
	protected abstract void pushHibernation();
	protected abstract void popHibernation();

	@Override
	public boolean canRedo() {
		return !undoneManipulations.isEmpty();
	}

	@Override
	public boolean canUndo() {
		return !doneManipulations.isEmpty();
	}

	@Override
	public void redo(int number) throws TransactionException {
		int size = undoneManipulations.size();
		
		if (size == 0)
			return; 
		
		if (number > size)
			throw new TransactionException("should undo " + number + " manipulations but have only " + size);
		
		pushHibernation();
		
		try {
			Iterator<Manipulation> it = undoneManipulations.iterator();
			
			for (int i = 0; i < number; i++) {
				Manipulation manipulation = it.next();
				session.manipulate().mode(ManipulationMode.LOCAL).apply(manipulation);
				onManipulationDone(manipulation);
				it.remove();
				doneManipulations.addLast(manipulation);
			}
			
			// care for eventing
			checkForStateChange();
		}
		catch (Exception e) {
			throw new TransactionException("error while redoing manipulation");
		}
		finally {
			popHibernation();
		}
	}

	@Override
	public void undo(int number) throws TransactionException {
		int size = doneManipulations.size();
		
		if (size == 0)
			return; 
		
		if (number > size)
			throw new TransactionException("should undo " + number + " manipulations but have only " + size);
		
		pushHibernation();
		
		try {
			ListIterator<Manipulation> it = doneManipulations.listIterator(doneManipulations.size());
			
			for (int i = 0; i < number; i++) {
				Manipulation manipulation = it.previous();
				session.manipulate().mode(ManipulationMode.LOCAL).apply(manipulation.getInverseManipulation());
				onManipulationUndone(manipulation);
				it.remove();
				undoneManipulations.addFirst(manipulation);
			}
			
			// care for eventing
			checkForStateChange();
		}
		catch (Exception e) {
			throw new TransactionException("error while redoing manipulation", e);
		}
		finally {
			popHibernation();
		}
	}

	@Override
	public List<Manipulation> getManipulationsDone() {
		return doneManipulations;
	}

	@Override
	public List<Manipulation> getManipulationsUndone() {
		return undoneManipulations;
	}

	public void clear() {
		undoneManipulations.clear();
		doneManipulations.clear();
		checkForStateChange(); 
	}
}
