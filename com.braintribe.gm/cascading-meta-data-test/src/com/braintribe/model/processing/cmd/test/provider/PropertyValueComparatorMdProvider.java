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

import com.braintribe.model.meta.selector.Operator;
import com.braintribe.model.meta.selector.PropertyValueComparator;
import com.braintribe.model.processing.cmd.test.meta.property.SimplePropertyMetaData;
import com.braintribe.model.processing.cmd.test.model.Person;

/**
 * 
 */
public class PropertyValueComparatorMdProvider extends AbstractModelSupplier {

	@Override
	protected void addMetaData() {
		addPropertyMdWithStringEqualComparator();
		addPropertyMdWithIntGreaterComparator();
		addPropertyMdWithCollectionFirstElementEqualComparator();
		addPropertyMdWithListSizeComparator();
		addPropertyMdWithSetSizeComparator();
		addPropertyMdWithMapSizeComparator();
	}

	private void addPropertyMdWithStringEqualComparator() {
		PropertyValueComparator comparator = PropertyValueComparator.T.create();
		comparator.setOperator(Operator.equal);
		comparator.setValue("foo");

		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("name", append(newMd(SimplePropertyMetaData.T, true), comparator));
	}

	private void addPropertyMdWithIntGreaterComparator() {
		PropertyValueComparator comparator = PropertyValueComparator.T.create();
		comparator.setPropertyPath("age");
		comparator.setOperator(Operator.greater);
		comparator.setValue(0);

		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("name", append(newMd(SimplePropertyMetaData.T, true), comparator));
	}

	private void addPropertyMdWithCollectionFirstElementEqualComparator() {
		PropertyValueComparator comparator = PropertyValueComparator.T.create();
		comparator.setPropertyPath("friends.0.name");
		comparator.setOperator(Operator.equal);
		comparator.setValue("foo");

		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("name", append(newMd(SimplePropertyMetaData.T, true), and(comparator, useCase("firstElement"))));
	}

	private void addPropertyMdWithListSizeComparator() {
		PropertyValueComparator comparator = PropertyValueComparator.T.create();
		comparator.setPropertyPath("friends.size");
		comparator.setOperator(Operator.equal);
		comparator.setValue(2);

		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("name", append(newMd(SimplePropertyMetaData.T, true), and(comparator, useCase("size-list"))));
	}

	private void addPropertyMdWithSetSizeComparator() {
		PropertyValueComparator comparator = PropertyValueComparator.T.create();
		comparator.setPropertyPath("otherFriends.size");
		comparator.setOperator(Operator.equal);
		comparator.setValue(2);

		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("name", append(newMd(SimplePropertyMetaData.T, true), and(comparator, useCase("size-set"))));
	}

	private void addPropertyMdWithMapSizeComparator() {
		PropertyValueComparator comparator = PropertyValueComparator.T.create();
		comparator.setPropertyPath("properties.size");
		comparator.setOperator(Operator.greater);
		comparator.setValue(1);

		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("name", append(newMd(SimplePropertyMetaData.T, true), and(comparator, useCase("size-map"))));
	}

}
