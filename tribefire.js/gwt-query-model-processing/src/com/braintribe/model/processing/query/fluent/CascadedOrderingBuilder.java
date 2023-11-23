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

import java.util.List;
import java.util.function.Consumer;

import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SimpleOrdering;

public class CascadedOrderingBuilder<T> extends OperandBuilder<CascadedOrderingBuilder<T>> {
	private final List<SimpleOrdering> orderings = newList();
	private final Consumer<List<SimpleOrdering>> _receiver;
	private final T _backLink;

	public CascadedOrderingBuilder(AbstractQueryBuilder<?> sourceRegistry, T backLink, Consumer<List<SimpleOrdering>> receiver) {
		super(sourceRegistry);
		setBackLink(this);
		setReceiver(operand -> {
			SimpleOrdering simpleOrdering = SimpleOrdering.T.create();
			simpleOrdering.setDirection(OrderingDirection.ascending);
			simpleOrdering.setOrderBy(operand);
			orderings.add(simpleOrdering);
		});

		this._backLink = backLink;
		this._receiver = receiver;
	}

	public OperandBuilder<CascadedOrderingBuilder<T>> dir(final OrderingDirection direction) {
		return new OperandBuilder<CascadedOrderingBuilder<T>>(sourceRegistry, this, operand -> {
			SimpleOrdering simpleOrdering = SimpleOrdering.T.create();
			simpleOrdering.setDirection(direction);
			simpleOrdering.setOrderBy(operand);
			orderings.add(simpleOrdering);
		});
	}

	public T close() {
		_receiver.accept(orderings);
		return _backLink;
	}
}
