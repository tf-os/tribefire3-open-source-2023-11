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

import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.processing.async.api.AsyncCallback;

public interface MetaDataEditorExpert extends MetaDataEditorBaseExpert {

	void provide(GenericEntity value, CallbackMetaData callback);
    void provide(GenericEntity value, CallbackEntityType callback);
    void provide(GenericEntity value, GmMetaModel editingModel, GenericEntity entity, CallbackMetaData callback);
	void provide(GenericEntity value, CallbackExpertResultType callback);
    void provide(GenericEntity value, GmMetaModel editingModel, GenericEntity entity, CallbackExpertResultType callback);
	
	public static abstract class CallbackMetaData implements AsyncCallback<Collection<MetaData>> {
		@Override
		public void onFailure(Throwable t) {
			ErrorDialog.show("MetaDataEditorExpert-Failure", t);
		}
	}
	
	public static abstract class CallbackEntityType implements AsyncCallback<Collection<EntityType<?>>> {
		@Override
		public void onFailure(Throwable t) {
			ErrorDialog.show("MetaDataEditorExpert-Failure", t);
		}
	}

	public static abstract class CallbackExpertResultType implements AsyncCallback<Collection<MetaDataEditorExpertResultType>> {
		@Override
		public void onFailure(Throwable t) {
			ErrorDialog.show("MetaDataEditorExpert-Failure", t);
		}
	}

}
