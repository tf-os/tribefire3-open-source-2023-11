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

import com.braintribe.model.processing.cmd.test.meta.entity.SimpleEntityMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.SimpleInheritedMetaData;
import com.braintribe.model.processing.cmd.test.model.Person;
import com.braintribe.model.processing.cmd.test.model.ServiceProvider;
import com.braintribe.model.processing.cmd.test.model.Teacher;

/**
 * Tests whether the caching is working OK, especially that after finding a static meta-data, the resolver does not try to look any further no matter
 * what.
 */
public class ExclusiveMdProvider extends AbstractModelSupplier {

	@Override
	protected void addMetaData() {
		addSimpleExclusiveMd();
		addInheritedExclusiveMd();
	}

	/**
	 * The purpose is to have two meta-data, the non-static one with lower priority.
	 */
	private void addSimpleExclusiveMd() {
		fullMdEditor.onEntityType(Teacher.T).addMetaData( //
				append(newMd(SimpleEntityMetaData.T, true), 10), //
				append(newMd(SimpleEntityMetaData.T, false), unreachable(), 0) //
		);

		fullMdEditor.onEntityType(Person.T) //
				.addMetaData(append(newMd(SimpleEntityMetaData.T, false), unreachable()));
	}

	/**
	 * The purpose is to have two meta-data, the non-static in first super-type, so that second super-type won't be visited. Note that first
	 * super-type of {@link Teacher} is {@link Person}, then comes {@link ServiceProvider}.
	 */
	private void addInheritedExclusiveMd() {
		fullMdEditor.onEntityType(Person.T) //
				.addMetaData(newMd(SimpleInheritedMetaData.T, true, Person.T));
		fullMdEditor.onEntityType(ServiceProvider.T) //
				.addMetaData(append(newMd(SimpleInheritedMetaData.T, false, ServiceProvider.T), unreachable()));
	}

}
