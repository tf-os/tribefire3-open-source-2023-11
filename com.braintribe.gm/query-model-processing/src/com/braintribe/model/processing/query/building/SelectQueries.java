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
package com.braintribe.model.processing.query.building;

import java.util.Objects;
import java.util.function.Supplier;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;

public abstract class SelectQueries extends Queries implements Supplier<SelectQuery> {
	protected SelectQuery supply;
	
	public static PropertyOperand property(Source source, String name) {
		return source.property(name);
	}
	
	public static PropertyOperand property(Source source, Property property) {
		return source.property(property);
	}
	
	public static From source(EntityType<?> entityType) {
		return source(entityType.getTypeSignature(), entityType.getShortName());
	}
	
	public static From source(EntityType<?> entityType, String name) {
		return source(entityType.getTypeSignature(), name);
	}
	
	public static From source(String entityTypeSignature, String name) {
		From from = From.T.create();
		from.setEntityTypeSignature(entityTypeSignature);
		from.setName(name);
		
		return from;
	}
	
	public static SelectQuery from(From... froms) {
		return SelectQuery.create(froms);
	}

	public static Join join(Source source, String propertyName) {
		return source.join(propertyName);
	}
	
	@Override
	public SelectQuery get() {
		Objects.requireNonNull(supply, "Not allowed to call get() before setting supply field");
		return supply;
	}

}