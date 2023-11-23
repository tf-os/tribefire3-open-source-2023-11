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
package com.braintribe.model.processing.cmd.test.provider;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.EnumConstantMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.cmd.test.meta.ActivableMetaData;
import com.braintribe.model.processing.cmd.test.meta.EntityRelatedMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.SimpleEntityMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.SimpleEnumConstantMetaData;
import com.braintribe.model.processing.cmd.test.meta.property.SimplePropertyMetaData;
import com.braintribe.model.processing.cmd.test.model.Color;
import com.braintribe.model.processing.cmd.test.model.Person;
import com.braintribe.model.processing.cmd.test.model.Teacher;

/**
 * 
 */
public class ImportantMdProvider extends AbstractModelSupplier {

	@Override
	protected void addMetaData() {
		addImportantEntityMd();
		addImportantPropertyMd();
		addImportantPropertyMdWithLowPrio();
		addImportantEnumConstantMd();
	}

	/**
	 * The important MD defined for super-type has higher conflictPriority than the lower one, so it should be taken (if it was not important, the
	 * priority would not matter.)
	 */
	private void addImportantEntityMd() {
		fullMdEditor.onEntityType(Person.T) //
				.addMetaData(newMd(SimpleEntityMetaData.T, Person.T, 10.0d, true));
		fullMdEditor.onEntityType(Teacher.T) //
				.addMetaData(newMd(SimpleEntityMetaData.T, Teacher.T, 0.0d, false));
	}

	/**
	 * Same as {@link #addImportantEntityMd()}
	 */
	private void addImportantPropertyMd() {
		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("age", newMd(SimplePropertyMetaData.T, Person.T, 10.0d, true));
		fullMdEditor.onEntityType(Teacher.T) //
				.addPropertyMetaData("age", newMd(SimplePropertyMetaData.T, Teacher.T, 0.0d, false));
	}

	/**
	 * Similar to {@link #addImportantPropertyMd()}, but the important MD has lower priority than the local one.
	 */
	private void addImportantPropertyMdWithLowPrio() {
		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("name", newMd(SimplePropertyMetaData.T, Person.T, -10.0d, true));
		fullMdEditor.onEntityType(Teacher.T) //
				.addPropertyMetaData("name", newMd(SimplePropertyMetaData.T, Teacher.T, 0.0d, false));
	}

	public static <M extends MetaData & ActivableMetaData & EntityRelatedMetaData> M newMd(EntityType<M> mdEt, EntityType<?> entityType,
			double priority, boolean important) {
		M amd = newMd(mdEt, true, entityType);
		amd.setConflictPriority(priority);
		amd.setImportant(important);

		return amd;
	}

	/**
	 * Similar to {@link #addImportantPropertyMd()}, but the important MD has lower priority than the local one.
	 */
	private void addImportantEnumConstantMd() {
		fullMdEditor //
				.addConstantMetaData(Color.class, newMd(SimpleEnumConstantMetaData.T, 10.0d, true)) //
				.addConstantMetaData(Color.GREEN, newMd(SimpleEnumConstantMetaData.T, 0.0d, false));
	}

	private static <M extends EnumConstantMetaData & ActivableMetaData> M newMd(EntityType<M> mdEt, double priority, boolean important) {

		M amd = newMd(mdEt, important);
		amd.setConflictPriority(priority);
		amd.setImportant(important);

		return amd;
	}
}
