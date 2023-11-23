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
package com.braintribe.model.meta.data.prompt;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.PropertyMetaData;

/**
 * Configures string properties which should be editable with a custom editor for WYSIWYG.
 */
public interface EditAsHtml extends PropertyMetaData {
	
	EntityType<EditAsHtml> T = EntityTypes.T(EditAsHtml.class);
	
	/**
	 * When configured to true, then the strong tag is used instead of b (default) for bold text.
	 */
	void setUseStrongTag(boolean useStrongTag);
	boolean getUseStrongTag();
	
	/**
	 * When configured to true, then the p tag is used instead of div (default) for new paragraphs.
	 */
	void setUsePTag(boolean usePTag);
	boolean getUsePTag();
	
	/**
	 * When configured to true, then the em tag is used instead of i (default) for italic text. 
	 */
	void setUseEmTag(boolean useEmTag);
	boolean getUseEmTag();

}
