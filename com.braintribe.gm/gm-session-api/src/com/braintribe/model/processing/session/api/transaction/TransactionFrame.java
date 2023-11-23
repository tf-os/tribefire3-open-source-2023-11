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
package com.braintribe.model.processing.session.api.transaction;

import java.util.List;

import com.braintribe.model.generic.manipulation.Manipulation;

public interface TransactionFrame {
	public boolean canRedo();
	public boolean canUndo();
	
	public void redo(int steps) throws TransactionException;
	public void undo(int steps) throws TransactionException;

	public List<Manipulation> getManipulationsDone();
	public List<Manipulation> getManipulationsUndone();
	
	public NestedTransaction beginNestedTransaction();
	
	public void addTransactionFrameListener(TransactionFrameListener listener);	
	public void removeTransactionFrameListener(TransactionFrameListener listener);
}
