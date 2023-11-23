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
package com.braintribe.gwt.gm.storage.impl.wb.form.setting.controls;

import java.util.function.Supplier;

import com.braintribe.gwt.gm.storage.impl.wb.WbStorageRuntimeException;
import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.qc.api.client.EntityFieldListener;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class GimaEntityFieldListener implements EntityFieldListener {
	private Supplier<GIMADialog> gimaDialogProvider = null;

	public GimaEntityFieldListener(final Supplier<GIMADialog> gimaDialogProvider) {
		this.gimaDialogProvider = gimaDialogProvider;
	}

	protected void displayGIMA(final PersistenceGmSession workbenchSession, final GenericEntity entity, final AsyncCallback<Boolean> callback) {
		try {
			// Provide GIMA and set our session to it
			final GIMADialog gimaDialog = this.gimaDialogProvider.get();
			gimaDialog.setGmSession(workbenchSession);

			// Create model path for the criterion
			final ModelPath modelPath = new ModelPath();
			modelPath.add(new RootPathElement(entity.entityType(), entity));

			// Display GIMA for the defined TraversingCriterion
			gimaDialog.showForModelPathElement(modelPath).get(callback);
		} catch (final Exception e) {
			// Throw GIMADialog providing exception
			throw new WbStorageRuntimeException("Error while providing GIMADialog.", e);
		}
	}
}
