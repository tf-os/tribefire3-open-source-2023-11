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

import com.braintribe.model.query.conditions.Disjunction;

public class DisjunctionBuilder<T> {
	private final AbstractQueryBuilder<?> queryBuilder;
	private final T backLink;
	private final Disjunction disjunction;
	private final Consumer<? super Disjunction> receiver;

	protected DisjunctionBuilder(AbstractQueryBuilder<?> queryBuilder, T backLink, Consumer<? super Disjunction> receiver) {
		this.backLink = backLink;
		this.receiver = receiver;
		this.disjunction = queryBuilder.newGe(Disjunction.T);
		this.disjunction.setOperands(newList());
		this.queryBuilder = queryBuilder;
	}

	public ConditionBuilder<DisjunctionBuilder<T>> add() {
		return new ConditionBuilder<DisjunctionBuilder<T>>(queryBuilder, this, disjunction.getOperands()::add);
	}

	public T endDisjunction() throws RuntimeException {
		receiver.accept(disjunction);

		return backLink;
	}
}
