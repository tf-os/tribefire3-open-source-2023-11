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
package com.braintribe.utils.template;

import java.io.File;
import java.io.Reader;
import java.util.Map;

public interface TemplateEngine {

	public String applyTemplate( File template
			, String templateEncoding
			, Map<String, Object> data
			, boolean escapeHTMLEntities) throws Exception;

	public String applyTemplate( String buffer
			, Map<String, Object> data) throws Exception;

	public String applyTemplate( Reader templateReader
			, Map<String, Object> data) throws Exception;
	
	public String applyTemplate( Reader templateReader
			, Map<String, Object> data
			, boolean escapeHTMLEntities) throws Exception;

	public void applyTemplateToFile( File template
			, String templateEncoding
			, File targetFile
			, Map<String, Object> data
			, boolean escapeHTMLEntities) throws Exception;

}
