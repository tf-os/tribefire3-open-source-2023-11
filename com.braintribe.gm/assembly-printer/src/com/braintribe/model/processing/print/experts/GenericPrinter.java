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

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.processing.print.EntityPrinter;
import com.braintribe.model.processing.print.PrintingContext;

/**
 * @author peter.gazdik
 */
public class GenericPrinter implements EntityPrinter<GenericEntity> {

	private final Map<Property, PropertyPrinter> propertyPrinters = newMap();
	private boolean doWriteRuntimeId;

	public void registerPropertyPrinter(Property property, PropertyPrinter printer) {
		propertyPrinters.put(property, printer);
	}

	public void setDoWriteRuntimeId(boolean doWriteRuntimeId) {
		this.doWriteRuntimeId = doWriteRuntimeId;
	}

	@Override
	public void print(GenericEntity entity, PrintingContext context) {
		EntityType<GenericEntity> et = entity.entityType();

		context.print(et.getShortName());

		if (doWriteRuntimeId) {
			context.print("(@");
			context.print(entity.runtimeId());
			context.print(")");
		}

		context.println(" (");
		context.levelUp();

		for (Property property : et.getProperties()) {
			if (context.ignoreProperty(property))
				continue;

			Object value = property.getDirectUnsafe(entity);

			if (value == null) {
				printNullProperty(property, context);

			} else if (VdHolder.isVdHolder(value)) {
				printVdHolder(property, (VdHolder) value, context);

			} else {
				printPropertyValue(entity, property, value, context);
			}
		}

		context.levelDown();
		context.print(")");

	}

	private void printNullProperty(Property property, PrintingContext context) {
		if (context.getWriteNulls()) {
			context.print(property.getName());
			context.println(": null");
		}
	}

	private void printVdHolder(Property property, VdHolder value, PrintingContext context) {
		context.print(property.getName());
		context.print(": ");
		if (value.isAbsenceInformation) {
			context.println(" <absent>");
		} else {
			context.println(" <VD>");
		}
	}

	private void printPropertyValue(GenericEntity entity, Property property, Object value, PrintingContext context) {
		context.print(property.getName());
		context.print(": ");

		PropertyPrinter propertyPrinter = propertyPrinters.get(property);
		if (propertyPrinter == null) {
			context.println(value);
		} else {
			propertyPrinter.print(entity, value, property, context);
			context.println("");
		}

	}

}
