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
package com.braintribe.gm.model.reason;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.braintribe.model.generic.reflection.EntityType;

public class ReasonBuilderImpl<R extends Reason> implements ReasonBuilder<R> {
	private final R reason;
	
	public ReasonBuilderImpl(EntityType<R> reasonType) {
		reason = reasonType.create();
	}
	
	@Override
	public <T> ReasonBuilder<R> assign(BiConsumer<R, T> assigner, T value) {
		assigner.accept(reason, value);
		return this;
	}

	@Override
	public ReasonBuilder<R> cause(Reason reason) {
		this.reason.causedBy(reason);
		return this;
	}

	@Override
	public ReasonBuilder<R> causes(Reason... reasons) {
		return causes(Arrays.asList(reasons));
	}

	@Override
	public ReasonBuilder<R> causes(Collection<Reason> reasons) {
		reasons.forEach(this::cause);
		return this;
	}

	@Override
	public R toReason() {
		if (reason.getText() == null) {
			String text = ReasonFormatter.buildPlainText(reason);
			reason.setText(text);
		}

		return reason;
	}
	
	@Override
	public <T> Maybe<T> toMaybe() {
		return Maybe.empty(toReason());
	}
	
	@Override
	public <T> Maybe<T> toMaybe(T value) {
		return Maybe.incomplete(value, toReason());
	}

	@Override
	public ReasonBuilder<R> text(String text) {
		reason.setText(text);
		return this;
	}

	@Override
	public ReasonBuilder<R> enrich(Consumer<R> enricher) {
		enricher.accept(reason);
		return this;
	}


}
