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
package com.braintribe.model.email.service;

import com.braintribe.model.email.service.reason.MailServerConnectionError;
import com.braintribe.model.email.service.reason.MailServerError;
import com.braintribe.model.email.service.reason.PostProcessingError;
import com.braintribe.model.email.service.reason.RetrieveConnectorMissing;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.UnsatisfiedBy;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

@Description("Gets unread emails from the email server in .eml format.")
@UnsatisfiedBy(RetrieveConnectorMissing.class)
@UnsatisfiedBy(MailServerError.class)
@UnsatisfiedBy(PostProcessingError.class)
@UnsatisfiedBy(MailServerConnectionError.class)
public interface ReceiveEmails extends EmailServiceRequest {

	EntityType<ReceiveEmails> T = EntityTypes.T(ReceiveEmails.class);

	@Name("Connector ID")
	@Description("The external ID of the connection that should be used. When this is not set, the first matching connector will be used.")
	String getConnectorId();
	void setConnectorId(String connectorId);

	@Name("Folder")
	@Description("The folder that should be used for retrieving emails. This is only used for IMAP connections. The default value is 'Inbox'.")
	@Initializer("'Inbox'")
	String getFolder();
	void setFolder(String folder);

	@Name("Maximum Emails")
	@Description("The maximum number of emails that should be retrieved.")
	@Initializer("50")
	Integer getMaxEmailCount();
	void setMaxEmailCount(Integer maxEmailCount);

	@Name("Post-processing")
	@Description("Determines what should be done with the email after retrieval. By default, mails are marked as read.")
	@Initializer("enum(com.braintribe.model.email.service.ReceivedEmailPostProcessing,MARK_READ)")
	ReceivedEmailPostProcessing getPostProcessing();
	void setPostProcessing(ReceivedEmailPostProcessing prostProcessing);

	@Name("Get Unread Mails Only")
	@Description("When set to true, only new (unread) emails will be retrieved. If false, all emails available will be retrieved.")
	@Initializer("true")
	boolean getUnreadOnly();
	void setUnreadOnly(boolean unreadOnly);

	@Name("Search Expression")
	@Description("Limited search expression to select specific mails (e.g., 'from office@example.com'")
	String getSearchExpression();
	void setSearchExpression(String searchExpression);

	@Override
	EvalContext<? extends ReceivedEmails> eval(Evaluator<ServiceRequest> evaluator);

}
