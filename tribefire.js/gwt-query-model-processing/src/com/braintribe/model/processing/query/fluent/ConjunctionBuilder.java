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
package com.braintribe.model.processing.query.fluent;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.function.Consumer;

import com.braintribe.model.query.conditions.Conjunction;

public class ConjunctionBuilder<T> {
	private final T backLink;
	private final Conjunction conjunction = Conjunction.T.create();
	private final Consumer<? super Conjunction> receiver;
	private final AbstractQueryBuilder<?> queryBuilder;

	protected ConjunctionBuilder(AbstractQueryBuilder<?> queryBuilder, T backLink, Consumer<? super Conjunction> receiver) {
		this.backLink = backLink;
		this.receiver = receiver;
		this.conjunction.setOperands(newList());
		this.queryBuilder = queryBuilder;
	}

	public ConditionBuilder<ConjunctionBuilder<T>> add() {
		return new ConditionBuilder<ConjunctionBuilder<T>>(queryBuilder, this, conjunction.getOperands()::add);
	}

	public T endConjunction() throws RuntimeException {
		receiver.accept(conjunction);

		return backLink;
	}
}
