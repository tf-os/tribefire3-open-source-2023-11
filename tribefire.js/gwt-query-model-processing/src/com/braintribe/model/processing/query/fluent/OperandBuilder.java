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

import java.util.function.Consumer;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.query.functions.value.AsString;
import com.braintribe.model.query.functions.value.Concatenation;
import com.braintribe.model.query.functions.value.Lower;
import com.braintribe.model.query.functions.value.Upper;

public class OperandBuilder<T> extends AbstractOperandBuilder<T, T, Object> {

	public OperandBuilder(SourceRegistry sourceRegistry, T backLink, Consumer<Object> receiver) {
		super(sourceRegistry, backLink, receiver);
	}

	protected OperandBuilder(SourceRegistry sourceRegistry) {
		super(sourceRegistry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T operand(Object value) {
		receiver.accept(value);
		return backLink;
	}

	@Override
	public T value(Object value) {
		return super.value(value);
	}

	@Override
	public T property(String name) {
		return super.property(name);
	}

	@Override
	public T property(String alias, String name) {
		return super.property(alias, name);
	}

	@Override
	public T entity(String alias) {
		return super.entity(alias);
	}

	@Override
	public T entity(GenericEntity entity) {
		return super.entity(entity);
	}

	@Override
	public T entityReference(PersistentEntityReference reference) {
		return super.entityReference(reference);
	}

	public T enumConstant(Enum<?> enumConstant) {
		return enumReference(EnumReference.of(enumConstant));
	}

	public T enumReference(EnumReference reference) {
		return operand(reference);
	}
	
	@Override
	public T listIndex(String joinAlias) {
		return super.listIndex(joinAlias);
	}

	public T listIndex() {
		return listIndex(null);
	}

	public T mapKey() {
		return mapKey(null);
	}

	@Override
	public T mapKey(String joinAlias) {
		return super.mapKey(joinAlias);
	}

	@Override
	public T localize(Object operand, String locale) {
		return super.localize(operand, locale);
	}

	@Override
	public OperandBuilder<T> localize(final String locale) {
		return super.localize(locale);
	}

	@Override
	public OperandBuilder<T> entitySignature() {
		return super.entitySignature();
	}

	@Override
	public T count() {
		return super.count();
	}

	@Override
	public T count(String alias) {
		return super.count(alias);
	}

	@Override
	public T count(String alias, String propertyName) {
		return super.count(alias, propertyName);
	}

	@Override
	public T count(String alias, String propertyName, boolean distinct) {
		return super.count(alias, propertyName, distinct);
	}

	@Override
	public T max(String alias, String propertyName) {
		return super.max(alias, propertyName);
	}

	@Override
	public T min(String alias, String propertyName) {
		return super.min(alias, propertyName);
	}

	@Override
	public T sum(String alias, String propertyName) {
		return super.sum(alias, propertyName);
	}

	@Override
	public T avg(String alias, String propertyName) {
		return super.avg(alias, propertyName);
	}

	public OperandListBuilder<T> concatenate() {
		return new OperandListBuilder<T>(sourceRegistry, backLink, operandList -> {
			Concatenation concatenate = Concatenation.T.create();
			concatenate.setOperands(operandList);

			receiver.accept(concatenate);
		});
	}

	public OperandBuilder<T> lower() {
		return new OperandBuilder<T>(sourceRegistry, backLink, value -> {
			Lower lower = Lower.T.create();
			lower.setOperand(value);
			receiver.accept(lower);
		});
	}

	public OperandBuilder<T> upper() {
		return new OperandBuilder<T>(sourceRegistry, backLink, value -> {
			Upper upper = Upper.T.create();
			upper.setOperand(value);
			receiver.accept(upper);
		});
	}

	@Override
	public OperandBuilder<T> asString() {
		return new OperandBuilder<T>(sourceRegistry, backLink, value -> {
			AsString asString = AsString.T.create();
			asString.setOperand(value);
			receiver.accept(asString);
		});
	}
}
