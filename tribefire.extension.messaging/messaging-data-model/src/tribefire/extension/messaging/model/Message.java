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

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.MinLength;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

import tribefire.extension.messaging.service.reason.validation.MandatoryNotSatisfied;

/**
 * The actual message which should be sent. If the key is not set it will be enriched with a unique key by the
 * underlying implementation.
 * 
 */
public interface Message extends StandardStringIdentifiable, HasMessageInformation, HasResourceMapping {

	EntityType<Message> T = EntityTypes.T(Message.class);

	String resourceBinaryPersistence = "resourceBinaryPersistence";
	String values = "values";
	String key = "key";
	String topics = "topics";

	@Mandatory
	@Name("Resource Binary Persistence")
	@Description("Describes persistence rules for the attached resources")
	ResourceBinaryPersistence getResourceBinaryPersistence();
	void setResourceBinaryPersistence(ResourceBinaryPersistence resourceBinaryPersistence);

	@Mandatory
	@MinLength(1)
	@Name("Values")
	@Description("The actual message")
	Map<String, GenericEntity> getValues();
	void setValues(Map<String, GenericEntity> values);

	@Name("Key")
	@Description("The message key")
	GenericEntity getKey();
	void setKey(GenericEntity key);

	@Name("Topics")
	@Description("Topics the message will be sent")
	Set<String> getTopics();
	void setTopics(Set<String> topics);

	// -----------------------------------------------------------------------
	// DEFAULT METHODS
	// -----------------------------------------------------------------------
	default Resource getPersistedResourceSilent(Object id) {
		return this.getResourceMapping().get(id);
	}

	default Resource getPersistedResourceReasoned(Object id) {
		//@formatter:off
		return Optional.ofNullable(this.getResourceMapping().get(id))
				.orElseThrow(()-> new UnsatisfiedMaybeTunneling(Reasons.build(MandatoryNotSatisfied.T)
						                                      .text("No resource was mapped for resource '" + id + "'").toMaybe()));
		//@formatter:on
	}

	default boolean shouldPersist(Resource resource) {
		return this.getResourceBinaryPersistence().shouldPersist(resource.isTransient());
	}

	// Builder
	default Message topic(Set<String> topic) {
		this.setTopics(topic);
		return this;
	}

	default Message values(Map<String, GenericEntity> values) {
		this.setValues(values);
		return this;
	}
	default Message option(ResourceBinaryPersistence option) {
		this.setResourceBinaryPersistence(option);
		return this;
	}

	default void addValue(String key, GenericEntity value) {
		this.getValues().put(key, value);
	}
}
