// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.model.processing.notification.api.builder.impl;

import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public abstract class BuilderTools {

	public static String createDetailedMessage(Throwable e) {
		return e.getMessage();
	}

	
	public static <T> void receive(T entity, Consumer<T> receiver) {
		try {
			receiver.accept(entity);
		} catch (Exception e) {
			throw new RuntimeException("Error building Notification.", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends GenericEntity> T createEntity (EntityType<? extends GenericEntity> entityType, Function<EntityType<? extends GenericEntity>, GenericEntity> entityFactory) { 
		try {
			return (T) entityFactory.apply(entityType);	
		} catch (Exception e) {
			throw new RuntimeException("Could not create entity for class: "+entityType,e);
		}
	}
	
}
