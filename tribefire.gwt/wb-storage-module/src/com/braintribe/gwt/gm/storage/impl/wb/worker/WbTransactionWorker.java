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
package com.braintribe.gwt.gm.storage.impl.wb.worker;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gm.storage.impl.wb.WbStorageHandle;
import com.braintribe.gwt.gm.storage.impl.wb.WbStorageRuntimeException;
import com.braintribe.gwt.gme.workbench.client.Workbench;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.Transaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.braintribe.processing.async.api.AsyncCallback;

public abstract class WbTransactionWorker<E> {
	private WbStorageHandle storageHandle = null;

	public WbStorageHandle getStorageHandle() {
		return this.storageHandle;
	}

	public void setStorageHandle(final WbStorageHandle storageHandle) {
		this.storageHandle = storageHandle;
	}

	/******************************** Transaction Methods ********************************/

	public void doTransaction(final PersistenceGmSession workbenchSession) {
		doTransaction(workbenchSession, null, null);
	}

	public void doTransaction(final PersistenceGmSession workbenchSession, final Future<E> future) {
		doTransaction(workbenchSession, null, future);
	}

	public void doTransaction(final PersistenceGmSession workbenchSession, final Workbench workbench) {
		doTransaction(workbenchSession, workbench, null);
	}

	public void doTransaction(final PersistenceGmSession workbenchSession, final Workbench workbench, final Future<E> future) {
		// Start nested transaction
		NestedTransaction nestedTransaction = workbenchSession.getTransaction().getCurrentTransactionFrame().beginNestedTransaction();
		if (workbench != null) {
			// Suspend received workbench
			workbench.suspendHistoryListener();
		}

		try {
			// Do your job
			transactionJob(workbenchSession);

			// Commit changes
			commitTransaction(nestedTransaction, workbench);
			nestedTransaction = null;
		} catch (final Exception e) {
			// Rollback changes
			rollbackTransaction(nestedTransaction, workbench);
			nestedTransaction = null;

			// Callback is defined?
			if (future != null) {
				// Call onFailure
				future.onFailure(e);
				return;
			} else {
				// No callback, so throw exception
				throw new WbStorageRuntimeException(e);
			}
		}

		// Commit changes to session
		commitToSession(workbenchSession, future);
	}

	/******************************** Transaction Helpers ********************************/

	private static void commitTransaction(final NestedTransaction nestedTransaction, final Workbench workbench) {
		// Commit changes
		nestedTransaction.commit();
		if (workbench != null) {
			// Resume received workbench
			workbench.resumeHistoryListener();
		}
	}

	private void rollbackTransaction(final NestedTransaction nestedTransaction, final Workbench workbench) {
		try {
			// Delete storage handle
			this.storageHandle = null;

			// Rollback changes
			nestedTransaction.rollback();
			if (workbench != null) {
				// Resume received workbench
				workbench.resumeHistoryListener();
			}
		} catch (final TransactionException ex) {
			throw new WbStorageRuntimeException(ex);
		}
	}

	private void commitToSession(final PersistenceGmSession workbenchSession, final Future<E> future) {
		final Transaction currentTransaction = workbenchSession.getTransaction();

		// Is manipulation list is done or is this nested transaction started from other nested transaction?
		if (currentTransaction.getManipulationsDone().isEmpty() || currentTransaction.getCurrentTransactionFrame() instanceof NestedTransaction) {
			if (future != null)
				future.onSuccess(getSuccessResult());
		} else {
			// No nested transaction left, so commit to workbench session
			workbenchSession.commit(AsyncCallback.of(response -> {
				if (future != null)
					future.onSuccess(getSuccessResult());
			}, e -> {
				if (future != null) {
					future.onFailure(e);
					return;
				} else {
					throw new WbStorageRuntimeException(e);
				}
			}));
		}
	}

	/********************************** Abstract Methods *********************************/

	protected abstract void transactionJob(final PersistenceGmSession transactionSession);

	protected E getSuccessResult() {
		return null;
	}
}
