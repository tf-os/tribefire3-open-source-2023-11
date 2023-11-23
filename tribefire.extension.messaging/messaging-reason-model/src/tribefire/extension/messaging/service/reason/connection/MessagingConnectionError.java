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
package tribefire.extension.messaging.service.reason.connection;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.messaging.service.reason.MessagingReason;

/**
 * Base for all errors related to the attached messaging system
 * 
 * Indicates that an error related to the attached messaging system appears.
 */
@Abstract
public interface MessagingConnectionError extends MessagingReason {

	EntityType<MessagingConnectionError> T = EntityTypes.T(MessagingConnectionError.class);

	String type = "type";
	String details = "details";

	@Mandatory
	@Name("Type")
	@Description("The type of the error that appeared")
	String getType();
	void setType(String type);

	@Mandatory
	@Name("Details")
	@Description("Detailed information")
	String getDetails();
	void setDetails(String details);

	// -----------------------------------------------------------------------
	// DEFAULT METHODS
	// -----------------------------------------------------------------------

	default void addThrowableInformation(Throwable t) {
		String name = t.getClass().getName();
		String message = t.getMessage();
		setType(name);
		setDetails(message);
	}
}