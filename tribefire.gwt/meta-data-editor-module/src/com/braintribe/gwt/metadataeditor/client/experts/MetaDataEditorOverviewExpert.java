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
package com.braintribe.gwt.metadataeditor.client.experts;

import java.util.Collection;
import java.util.Set;

import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.processing.async.api.AsyncCallback;

public interface MetaDataEditorOverviewExpert extends MetaDataEditorBaseExpert {

	void provide(GenericEntity value, Set<String> listUseCase, Set<String> listRoles, Set<String> listAccess, Callback callback);

	public static abstract class Callback implements AsyncCallback<Collection<MetaData>> {
		@Override
		public void onFailure(Throwable t) {
			ErrorDialog.show("MetaDataEditorExpert-Failure", t);
		}
	}
}
