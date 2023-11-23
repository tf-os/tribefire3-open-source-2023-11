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
package com.braintribe.template.processing;

import com.braintribe.model.artifact.essential.PartIdentification;

public interface ArtifactTemplateConsts {
	
	final String DOT_FTL = ".ftl";
	final String GROOVY_EXTENSION = "groovy";
	
	final String DEPENDENCIES_SCRIPT = "dependencies." + GROOVY_EXTENSION;
	
	final String CONTENT_DIR = "content";
	
	final String STATIC_DIR = "static";
	final String STATIC_DIR_FULL = CONTENT_DIR + "/" + STATIC_DIR;
	final String DYNAMIC_DIR = "dynamic";
	final String DYNAMIC_DIR_FULL = CONTENT_DIR + "/" + DYNAMIC_DIR;
	final String PROJECTED_DIR = "projected";
	final String PROJECTED_DIR_FULL = DYNAMIC_DIR_FULL + "/" + PROJECTED_DIR;
	final String STATIC_TEMPLATE = "static" + DOT_FTL;
	final String STATIC_TEMPLATE_FULL = DYNAMIC_DIR_FULL + "/" + STATIC_TEMPLATE;

	PartIdentification ARCHIVE_ZIP_PART = PartIdentification.create("archive", "zip");

}
