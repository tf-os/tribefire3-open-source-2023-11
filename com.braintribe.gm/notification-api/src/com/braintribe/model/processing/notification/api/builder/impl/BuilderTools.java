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
package com.braintribe.model.processing.notification.api.builder.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public abstract class BuilderTools {

	public static String createDetailedMessage(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw) {
			@Override
			public void println() {
				write('\n');
			}
		};
		e.printStackTrace(pw);
		String detailedMsg = sw.toString();
		detailedMsg = (detailedMsg == null || detailedMsg.trim().length() == 0) ? null : detailedMsg;
		return detailedMsg;
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
