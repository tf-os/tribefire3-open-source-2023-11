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

import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.meta.selector.AccessTypeSelector;
import com.braintribe.model.meta.selector.AccessTypeSignatureSelector;
import com.braintribe.model.processing.cmd.test.meta.property.SimplePropertyMetaData;
import com.braintribe.model.processing.cmd.test.model.Access;
import com.braintribe.model.processing.cmd.test.model.IncrementalAccess;
import com.braintribe.model.processing.cmd.test.model.Person;

/**
 * 
 */
public class SpecialSelectorMdProvider extends AbstractModelSupplier {

	public static final Access accessSelector = Access.T.create();
	public static final AccessTypeSelector accessTypeSelector = AccessTypeSelector.T.create();
	public static final AccessTypeSignatureSelector accessTypeSignatureSelector = AccessTypeSignatureSelector.T.create();

	public static final String NO_UC = "NO_UC";
	public static final String JUST_UC = "UCE-1";
	public static final String NOT_UC = "NOT-UC";
	public static final String X_AND_UC = "X_AND_UC";
	public static final String X_OR_UC = "X_OR_UC";

	static {
		accessSelector.setExternalId("theAccess");

		// matches every access
		accessTypeSelector.setAccessType(MetaModelBuilder.entityType(IncrementalAccess.T.getTypeSignature()));
		accessTypeSelector.setAssignable(true);
		accessTypeSignatureSelector.setDenotationTypeSignature(IncrementalAccess.T.getTypeSignature());
		accessTypeSignatureSelector.setAssignable(true);
	}

	@Override
	protected void addMetaData() {
		addSimplePropertyMdWithAccessSelector();
		addSimplePropertyMdWithAccessTypeSelector();
		addSimplePropertyMdWithAccessTypeSignatureSelector();
		addSimplePropertyMdForIgnoreSelectors();
	}

	private void addSimplePropertyMdWithAccessSelector() {
		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("name", append(newMd(SimplePropertyMetaData.T, true), accessSelector));
	}

	private void addSimplePropertyMdWithAccessTypeSelector() {
		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("age", append(newMd(SimplePropertyMetaData.T, true), accessTypeSelector));
	}

	private void addSimplePropertyMdWithAccessTypeSignatureSelector() {
		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("friend", append(newMd(SimplePropertyMetaData.T, true), accessTypeSignatureSelector));
	}

	private void addSimplePropertyMdForIgnoreSelectors() {
		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("friends", //
						append(newMd(SimplePropertyMetaData.T, JUST_UC), useCase(JUST_UC), 4), //
						append(newMd(SimplePropertyMetaData.T, X_AND_UC), and(FALSE_SELECTOR, useCase(X_AND_UC)), 3), //
						append(newMd(SimplePropertyMetaData.T, X_OR_UC), or(FALSE_SELECTOR, useCase(X_OR_UC)), 2), //
						append(newMd(SimplePropertyMetaData.T, NO_UC), staticContextSelector("NO_UC_VALUE"), 1) //
				);
		fullMdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("otherFriends", //
						append(newMd(SimplePropertyMetaData.T, NOT_UC), not(useCase(NOT_UC))));

	}

}
