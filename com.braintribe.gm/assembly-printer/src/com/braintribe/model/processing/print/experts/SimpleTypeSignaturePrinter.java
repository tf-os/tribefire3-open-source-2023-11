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
public class SimpleTypeSignaturePrinter implements EntityPrinter<GenericEntity> {

	public static final SimpleTypeSignaturePrinter INSTANCE = new SimpleTypeSignaturePrinter();

	private SimpleTypeSignaturePrinter() {
	}

	@Override
	public void print(GenericEntity entity, PrintingContext context) {
		EntityType<GenericEntity> et = entity.entityType();

		context.print(et.getShortName());
	}

}
