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
package com.braintribe.doc.pages;

import java.util.Map;

import com.braintribe.doc.UniversalPath;

public class AllDocsPageRenderer extends AbstractPageRenderer {

	public AllDocsPageRenderer() {
		super("all-docs.ftlh");
	}

	@Override
	protected Map<String, Object> dataModel(PageRenderingContext context) {
		Map<String, Object> dataModel = context.newDataModelWithAssets("");
		dataModel.put("tags", context.getDocumentationContent().getUsedTags());
		
		return dataModel;
	}

	@Override
	protected UniversalPath targetFile(PageRenderingContext context) {
		return UniversalPath.start("all-docs.html"); 
	}
	
	
}
