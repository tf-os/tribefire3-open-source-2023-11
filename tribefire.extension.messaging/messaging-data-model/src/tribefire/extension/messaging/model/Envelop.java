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
package tribefire.extension.messaging.model;

import java.util.List;

import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.MinLength;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * The {@link Envelop} holds multiple {@link Message}s. It can globally set for all the attached {@link Message}s the
 * information from {@link HasMessageInformation}
 * 
 * <ul>
 * <li>{@link HasMessageInformation#getTimestamp()}</li>
 * <li>{@link HasMessageInformation#getNanoTimestamp()}</li>
 * <li>{@link HasMessageInformation#getContext()}</li>
 * </ul>
 * 
 * The {@link Envelop} itself won't be sent to the underlying messaging system but each {@link Message} itself - the
 * {@link Envelop} is only a container for easier sending multiple messages
 */
public interface Envelop extends StandardStringIdentifiable, HasMessageInformation {

	EntityType<Envelop> T = EntityTypes.T(Envelop.class);

	String messages = "messages";

	@Mandatory
	@MinLength(1)
	@Name("Messages")
	@Description("List of 'messages'")
	List<Message> getMessages();
	void setMessages(List<Message> messages);

	// -----------------------------------------------------------------------
	// DEFAULT METHODS
	// -----------------------------------------------------------------------

	static Envelop create(Message message) {
		Envelop envelop = Envelop.T.create();
		envelop.getMessages().add(message);
		return envelop;
	}
}
