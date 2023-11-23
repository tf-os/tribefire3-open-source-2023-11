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
import com.braintribe.model.processing.cmd.test.meta.enumeration.SimpleEnumConstantMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.SimpleEnumMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.SimpleModelMetaData;
import com.braintribe.model.processing.cmd.test.meta.property.SimplePropertyMetaData;
import com.braintribe.model.processing.cmd.test.meta.selector.AspectCheckingSelector;
import com.braintribe.model.processing.cmd.test.model.Color;
import com.braintribe.model.processing.cmd.test.model.Person;

/**
 * 
 */
public class ResolutionContextMdProvider extends AbstractModelSupplier {

	private static final AspectCheckingSelector selector = AspectCheckingSelector.T.create();

	@Override
	protected void addMetaData() {
		// Model MD
		fullMdEditor.addModelMetaData(append(newMd(SimpleModelMetaData.T, true), selector));

		// Entity + Property MD
		fullMdEditor.onEntityType(Person.T) //
				.addMetaData(append(newMd(SimpleEntityMetaData.T, true), selector)) //
				.addPropertyMetaData("name", append(newMd(SimplePropertyMetaData.T, true), selector));

		fullMdEditor.onEnumType(Color.class) //
				.addMetaData(append(newMd(SimpleEnumMetaData.T, true), selector)) //
				.addConstantMetaData(Color.GREEN, append(newMd(SimpleEnumConstantMetaData.T, true), selector));
	}

}
