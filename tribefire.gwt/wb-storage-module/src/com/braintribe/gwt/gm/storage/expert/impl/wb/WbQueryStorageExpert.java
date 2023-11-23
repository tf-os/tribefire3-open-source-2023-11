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
package com.braintribe.gwt.gm.storage.expert.impl.wb;

import com.braintribe.gwt.gm.storage.api.ColumnData;
import com.braintribe.gwt.gm.storage.api.StorageHandle;
import com.braintribe.gwt.gm.storage.expert.api.QueryStorageExpert;
import com.braintribe.gwt.gm.storage.expert.api.QueryStorageInput;
import com.braintribe.gwt.gm.storage.impl.wb.WbStorageHandle;
import com.braintribe.gwt.gm.storage.impl.wb.WbStorageRuntimeException;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.query.Query;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.meta.TemplateMetaData;
import com.braintribe.model.workbench.TemplateQueryAction;
import com.braintribe.model.workbench.meta.QueryString;

public class WbQueryStorageExpert implements QueryStorageExpert {

	@Override
	public QueryStorageInput prepareStorageInput(Query query, String queryString, ColumnData columnData) {
		WbQueryStorageInput storageInput = WbQueryStorageInput.T.create();
		storageInput.setQueryString(queryString);
		storageInput.setQuery(query);
		storageInput.setColumnData(columnData);
		return storageInput;
	}

	@Override
	public StorageHandle prepareStorageHandle(GenericEntity entity) {
		if (entity instanceof Folder) {
			WbStorageHandle storageHandle = new WbStorageHandle();
			storageHandle.setQueryFolder((Folder) entity);
			return storageHandle;
		}

		return null;
	}

	@Override
	public String getQueryString(StorageHandle handle) {
		if (handle instanceof WbStorageHandle) {
			final WbStorageHandle storageHandle = (WbStorageHandle) handle;
			final Folder queryFolder = storageHandle.getQueryFolder();

			if (queryFolder != null) {
				// Check for Template Query-Action.
				if (queryFolder.getContent() instanceof TemplateQueryAction) {
					final TemplateQueryAction templateQueryAction = (TemplateQueryAction) queryFolder.getContent();
					Template queryTemplate = templateQueryAction.getTemplate();

					// Check template for query
					if (queryTemplate != null) {
						// Try to find the QueryString
						for (final TemplateMetaData metaData : queryTemplate.getMetaData()) {
							if (metaData instanceof QueryString) {
								// QueryString Meta-Data found
								QueryString queryString = (QueryString) metaData;
								return queryString.getValue();
							}
						}
					}
				}

				return null;
			}
		}

		// Throw not found exception
		throw new WbStorageRuntimeException("Could not get Query-String. Invalid handle received.");
	}
}
