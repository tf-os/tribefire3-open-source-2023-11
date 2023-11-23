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
package com.braintribe.model.processing.print.experts;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.print.EntityPrinter;
import com.braintribe.model.processing.print.PrintingContext;

/**
 * @author peter.gazdik
 */
public class GenericRefPrinter implements EntityPrinter<GenericEntity> {

	private final boolean ignoreId;
	private final boolean ignorePartition;
	private final boolean ignoreGlobalId;

	public GenericRefPrinter(boolean ignoreId, boolean ignorePartition, boolean ignoreGlobalId) {
		if (ignoreId && ignoreGlobalId) {
			throw new IllegalArgumentException("Cannot ignore both id and globalId");
		}

		this.ignoreId = ignoreId;
		this.ignorePartition = ignorePartition;
		this.ignoreGlobalId = ignoreGlobalId;
	}

	@Override
	public void print(GenericEntity entity, PrintingContext context) {
		EntityType<GenericEntity> et = entity.entityType();

		context.print(et.getShortName());
		context.print("(");

		if (!ignoreId)
			printId(entity, context);

		if (!ignorePartition)
			printPartition(entity, context);

		if (!ignoreGlobalId)
			printGlobalId(entity, context);

		context.print(")");
	}

	private void printId(GenericEntity entity, PrintingContext context) {
		Object id = entity.getId();
		if (id != null)
			context.print(id);
		else {
			context.print("@");
			context.print(entity.runtimeId());
		}
	}

	private void printPartition(GenericEntity entity, PrintingContext context) {
		if (!ignoreId)
			context.print(", ");
		context.print("'");
		context.print(entity.getPartition());
		context.print("'");
	}

	private void printGlobalId(GenericEntity entity, PrintingContext context) {
		if (!ignoreId || !ignorePartition)
			context.print(", ");
		context.print(entity.getGlobalId());
	}

}
