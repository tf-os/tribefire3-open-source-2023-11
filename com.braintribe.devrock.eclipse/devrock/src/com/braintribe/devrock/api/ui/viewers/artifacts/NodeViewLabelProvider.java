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
package com.braintribe.devrock.api.ui.viewers.artifacts;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;

import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.eclipse.model.storage.ViewerContext;

public interface NodeViewLabelProvider extends IStyledLabelProvider {

	void setUiSupport(UiSupport uiSupport);
	void setUiSupportStylersKey(String uiSupportStylersKey);
	void setViewerContext(ViewerContext viewContext);
	void setRequestHander(DetailRequestHandler requestHander);
}
