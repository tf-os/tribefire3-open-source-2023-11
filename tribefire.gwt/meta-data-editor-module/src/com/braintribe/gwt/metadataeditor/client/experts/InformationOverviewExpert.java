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

import java.util.ArrayList;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.override.GmEntityTypeOverride;

public class InformationOverviewExpert extends AbstractBaseEditorOverviewExpert {

	@Override
	public void provide(GenericEntity value, Set<String> listUseCase, Set<String> listRoles, Set<String> listAccess,	Callback callback) {
		try {	
			if (value instanceof GmEntityType) {
				callback.onSuccess(new ArrayList<>(((GmEntityType) value).getPropertyMetaData()));
			}
			if (value instanceof GmEntityTypeOverride) {
				callback.onSuccess(new ArrayList<>(((GmEntityTypeOverride) value).getPropertyMetaData()));
			}
			//if (value instanceof GmMetaModel) {
			//	callback.onSuccess(new ArrayList<GmEntityType>(((GmMetaModel)value).getEntityTypes()));
			//}
		} catch (Exception e) {
			callback.onFailure(e);
		}
		
	}

}
