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
package com.braintribe.model.generic.processing.pr.fluent;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.function.Consumer;

import com.braintribe.model.generic.pr.criteria.PatternCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;

public class PatternBuilder<T> extends CriterionBuilder<PatternBuilder<T>> {
	@SuppressWarnings("hiding")
	private final T backLink;
	private final List<TraversingCriterion> criteria = newList();
	@SuppressWarnings("hiding")
	private final Consumer<? super PatternCriterion> receiver;

	public PatternBuilder(T backLink, Consumer<? super PatternCriterion> receiver) {
		this.receiver = receiver;
		this.backLink = backLink;
		setBackLink(this);
		setReceiver(criteria::add);
	}

	public T close() {
		PatternCriterion criterion = PatternCriterion.T.create();
		criterion.setCriteria(criteria);
		receiver.accept(criterion);
		return backLink;
	}

}
