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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.PropertyNameSelector;
import com.braintribe.model.meta.selector.PropertyRegexSelector;
import com.braintribe.model.meta.selector.PropertyTypeSelector;
import com.braintribe.model.processing.cmd.test.meta.property.GlobalPropertyMetaData;
import com.braintribe.model.processing.cmd.test.meta.property.GlobalSelectorPMetaData;
import com.braintribe.model.processing.cmd.test.meta.property.SimplePropertyMetaData;
import com.braintribe.model.processing.cmd.test.model.Person;
import com.braintribe.model.processing.cmd.test.model.Teacher;

/**
 * 
 */
public class PropertyMdProvider extends AbstractModelSupplier {

	@Override
	protected void addMetaData() {
		addSimplePropertyMd();
		addMdForExtendedInfo();
		addGlobalPropertyMd();
		addGlobalPropertyMdByName();
		addGlobalPropertyMdByRegex();
		addGlobalPropertyMdByType();
	}

	private void addSimplePropertyMd() {
		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("age", newMd(SimplePropertyMetaData.T, true));
	}

	private void addMdForExtendedInfo() {
		fullMdEditor.onEntityType(Teacher.T) //
				.addPropertyMetaData("name", newMd(SimplePropertyMetaData.T, true));

		baseMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("name", newMd(SimplePropertyMetaData.T, true));
	}

	private void addGlobalPropertyMd() {
		fullMdEditor.onEntityType(GenericEntity.T) //
				.addPropertyMetaData(globalMd("GLOBAL"));

		baseMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("name", globalMd("PROPERTY"));
	}

	private void addGlobalPropertyMdByName() {
		PropertyNameSelector selector = PropertyNameSelector.T.create();
		selector.setPropertyName("color");

		fullMdEditor.onEntityType(GenericEntity.T) //
				.addPropertyMetaData(globalMd("P_NAME", selector));
	}

	private void addGlobalPropertyMdByRegex() {
		PropertyRegexSelector selector = PropertyRegexSelector.T.create();
		selector.setRegex(".*nds");

		fullMdEditor.onEntityType(GenericEntity.T) //
				.addPropertyMetaData(globalMd("P_REGEX", selector));
	}

	private void addGlobalPropertyMdByType() {
		IsAssignableTo condition = IsAssignableTo.T.create();
		condition.setTypeSignature(Person.T.getTypeSignature());

		PropertyTypeSelector selector = PropertyTypeSelector.T.create();
		selector.setTypeCondition(condition);

		fullMdEditor.onEntityType(GenericEntity.T) //
				.addPropertyMetaData(globalMd("P_TYPE", selector));
	}

	private GlobalSelectorPMetaData globalMd(String activeString, MetaDataSelector selector) {
		return append(newMd(GlobalSelectorPMetaData.T, activeString), selector);
	}

	private GlobalPropertyMetaData globalMd(String activeString) {
		return newMd(GlobalPropertyMetaData.T, activeString);
	}

}
