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
package tribefire.extension.okta.model;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface OktaError extends GenericEntity {

	EntityType<OktaError> T = EntityTypes.T(OktaError.class);

	String error = "error";
	String error_description = "error_description";

	String errorCode = "errorCode";
	String errorSummary = "errorSummary";
	String errorLink = "errorLink";
	String errorId = "errorId";
	String errorCauses = "errorCauses";

	String getErrorCode();
	void setErrorCode(String errorCode);

	String getErrorSummary();
	void setErrorSummary(String errorSummary);

	String getErrorLink();
	void setErrorLink(String errorLink);

	String getErrorId();
	void setErrorId(String errorId);

	List<OktaError> getErrorCauses();
	void setErrorCauses(List<OktaError> errorCauses);

	String getError();
	void setError(String error);

	String getError_description();
	void setError_description(String error_description);

	default String errorMessage() {
		StringBuilder sb = new StringBuilder();
		addErrorText(sb, "Error", getError());
		addErrorText(sb, "Error Description", getError_description());
		addErrorText(sb, "Error Code", getErrorCode());
		addErrorText(sb, "Error Summary", getErrorSummary());
		addErrorText(sb, "Error ID", getErrorId());
		return sb.toString();
	}

	private void addErrorText(StringBuilder sb, String label, String text) {
		if (text != null && text.trim().length() > 0) {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(label + ": " + text);
		}
	}
}
