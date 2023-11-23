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

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.createInverse;

import java.util.ArrayList;

import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;

public class BasicNestedTransaction extends AbstractTransactionFrame implements NestedTransaction {
	private BasicTransaction transaction;
	private AbstractTransactionFrame parentFrame;
	
	public BasicNestedTransaction(ManagedGmSession session, BasicTransaction transaction, AbstractTransactionFrame parentFrame) {
		super(session);
		this.transaction = transaction;
		this.parentFrame = parentFrame;
	}

	@Override
	public AbstractTransactionFrame getParentFrame() {
		return parentFrame;
	}
	
	@Override
	protected void pushHibernation() {
		transaction.pushHibernation();
	}
	
	@Override
	protected void popHibernation() {
		transaction.popHibernation();
	}
	
	@Override
	public NestedTransaction beginNestedTransaction() {
		return transaction.beginNestedTransaction();
	}
	
	@Override
	protected void onManipulationUndone(Manipulation manipulation) {
		transaction.onManipulationUndone(manipulation);
	}
	
	@Override
	protected void onManipulationDone(Manipulation manipulation) {
		transaction.onManipulationDone(manipulation);
	}
	
	@Override
	public void commit() {
		// first commit child frame recursively
		if (childFrame != null)
			childFrame.commit();
		
		// commit this frame then by placing a resulting manipulation if there is one
		switch (doneManipulations.size()) {
		case 0:
			break;
		case 1:
			parentFrame.appendManipulation(doneManipulations.getFirst());
			break;
		default:
			CompoundManipulation compoundManipulation = compound(new ArrayList<Manipulation>(doneManipulations));
			compoundManipulation.linkInverse(createInverse(compoundManipulation));

			parentFrame.appendManipulation(compoundManipulation);
		}
		
		transaction.endNestedTransaction();
		
		clear();
	}
	
	@Override
	public void rollback() throws TransactionException {
		// first undo childFrames recursively
		if (childFrame != null) {
			childFrame.rollback();
		}
		
		// really rollback the manipulations
		undo(doneManipulations.size());
		
		transaction.endNestedTransaction();
		
		clear();
	}
}
