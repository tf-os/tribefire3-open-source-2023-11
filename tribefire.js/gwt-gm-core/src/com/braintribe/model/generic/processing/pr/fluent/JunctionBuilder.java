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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.pr.criteria.JunctionCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.tc)
@SuppressWarnings("unusable-by-js")
public class JunctionBuilder<T> extends CriterionBuilder<JunctionBuilder<T>>{
	@SuppressWarnings("hiding")
	private final T backLink;
	private final List<TraversingCriterion> criteria = new ArrayList<TraversingCriterion>();
	@SuppressWarnings("hiding")
	private final Consumer<? super JunctionCriterion> receiver;
	private final EntityType<? extends JunctionCriterion> junctionType;
	
	@JsIgnore
	public JunctionBuilder(EntityType<? extends JunctionCriterion> junctionType, T backLink, Consumer<? super JunctionCriterion> receiver) {
		this.receiver = receiver;
		this.backLink = backLink;
		this.junctionType = junctionType;
		setBackLink(this);
		setReceiver(new Consumer<TraversingCriterion>() {
			@Override
			public void accept(TraversingCriterion criterion) throws RuntimeException {
				criteria.add(criterion);
			}
		});
	}
	
	public T close() throws TraversingCriteriaBuilderException {
		try {
			JunctionCriterion junction = junctionType.create();
			junction.setCriteria(criteria);
			receiver.accept(junction);
			return backLink;
		} catch (Exception e) {
			throw new TraversingCriteriaBuilderException(e);
		}
	}
	

}
