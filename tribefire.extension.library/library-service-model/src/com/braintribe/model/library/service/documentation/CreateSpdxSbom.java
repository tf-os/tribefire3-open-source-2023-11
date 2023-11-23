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
package com.braintribe.model.library.service.documentation;

import java.util.Date;

import com.braintribe.model.accessapi.AccessDataRequest;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.library.service.ArtifactReferences;
import com.braintribe.model.library.service.LibraryBaseRequest;
import com.braintribe.model.service.api.ServiceRequest;

public interface CreateSpdxSbom extends LibraryBaseRequest, AccessDataRequest, ArtifactReferences, HasIgnoreMissing {

	final EntityType<CreateSpdxSbom> T = EntityTypes.T(CreateSpdxSbom.class);

	public final static String title = "title";
	public final static String timestamp = "timestamp";

	String getTitle();
	void setTitle(String title);

	Date getTimestamp();
	void setTimestamp(Date timestamp);

	String getDocumentUri();
	void setDocumentUri(String documentUri);

	@Initializer("'Library Module'")
	String getCreatorTool();
	void setCreatorTool(String creatorTool);

	@Initializer("'Braintribe Technology GmbH'")
	String getCreatorOrganization();
	void setCreatorOrganization(String creatorOrganization);

	String getPackageName();
	void setPackageName(String packageName);

	String getCopyrightText();
	void setCopyrightText(String copyrightText);

	String getPackageComment();
	void setPackageComment(String packageComment);

	String getHomepage();
	void setHomepage(String homepage);

	String getPackageDescription();
	void setPackageDescription(String packageDescription);

	String getPackageOriginator();
	void setPackageOriginator(String packageOriginator);

	String getDownloadLocation();
	void setDownloadLocation(String downloadLocation);

	@Override
	EvalContext<SpdxSbom> eval(Evaluator<ServiceRequest> evaluator);

}
