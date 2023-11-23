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

import java.util.List;
import java.util.function.Consumer;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.value.AsString;
import com.braintribe.model.query.functions.value.Concatenation;

public class ConditionBuilder<T> extends AbstractOperandBuilder<T, ValueComparisonBuilder<T>, Condition> {

	protected ConditionBuilder(SourceRegistry sourceRegistry, T backLink, Consumer<Condition> receiver) {
		super(sourceRegistry, backLink, receiver);
	}

	protected ConditionBuilder(SourceRegistry sourceRegistry) {
		super(sourceRegistry);
	}

	public JunctionBuilder<T> conjunction() {
		return new JunctionBuilder<T>(sourceRegistry, Conjunction.T, backLink, receiver);
	}

	public JunctionBuilder<T> disjunction() {
		return new JunctionBuilder<T>(sourceRegistry, Disjunction.T, backLink, receiver);
	}

	public ConditionBuilder<T> negation() {
		return new ConditionBuilder<>(sourceRegistry, backLink, condition -> receiver.accept(newNegation(condition)));
	}

	private Negation newNegation(Condition condition) {
		Negation negation = sourceRegistry.newGe(Negation.T);
		negation.setOperand(condition);
		return negation;
	}

	public T condition(Condition condition) {
		receiver.accept(condition);
		return backLink;
	}

	public T fullText(String alias, String text) {
		Source source = sourceRegistry.acquireSource(alias);
		FulltextComparison fulltextComparison = sourceRegistry.newGe(FulltextComparison.T);
		fulltextComparison.setText(text);
		fulltextComparison.setSource(source);
		receiver.accept(fulltextComparison);
		return backLink;
	}

	// These methods have to stay due to binary compatibility
	
	@Override
	public ValueComparisonBuilder<T> entity(String alias) {
		return super.entity(alias);
	}

	@Override
	public ValueComparisonBuilder<T> entity(GenericEntity entity) {
		return super.entity(entity);
	}

	@Override
	public ValueComparisonBuilder<T> entityReference(PersistentEntityReference reference) {
		return super.entityReference(reference);
	}

	@Override
	public ValueComparisonBuilder<T> value(Object object) {
		return super.value(object);
	}

	@Override
	public ValueComparisonBuilder<T> property(String name) {
		return super.property(name);
	}

	@Override
	public ValueComparisonBuilder<T> property(String alias, String name) {
		return super.property(alias, name);
	}

	@Override
	public ValueComparisonBuilder<T> listIndex(String joinAlias) {
		return super.listIndex(joinAlias);
	}

	@Override
	public ValueComparisonBuilder<T> mapKey(String joinAlias) {
		return super.mapKey(joinAlias);
	}

	@Override
	public ValueComparisonBuilder<T> localize(Object operand, String locale) {
		return super.localize(operand, locale);
	}

	@Override
	public OperandBuilder<ValueComparisonBuilder<T>> localize(String locale) {
		return super.localize(locale);
	}

	public ValueComparisonBuilder<T> entitySignature(String alias) {
		// Always creating a PropertyOperand prevents a problem in EntityQuery, where the source would be null
		return entitySignature(alias, null);
	}

	public ValueComparisonBuilder<T> entitySignature(String alias, String propertyPath) {
		PropertyOperand propertyOperand = sourceRegistry.newGe(PropertyOperand.T);
		propertyOperand.setPropertyName(propertyPath);
		propertyOperand.setSource(sourceRegistry.acquireSource(alias));

		EntitySignature signatureFunction = sourceRegistry.newGe(EntitySignature.T);
		signatureFunction.setOperand(propertyOperand);
		return operand(signatureFunction);
	}

	@Override
	public OperandBuilder<ValueComparisonBuilder<T>> entitySignature() {
		return super.entitySignature();
	}

	@Override
	public ValueComparisonBuilder<T> operand(Object object) {
		return new ValueComparisonBuilder<>(sourceRegistry, object, backLink, receiver);
	}

	public OperandListBuilder<ValueComparisonBuilder<T>> concatenate() {
		ValueComparisonBuilder<T> vcb = operand(null);

		return new OperandListBuilder<>(sourceRegistry, vcb, operandList -> vcb.setLeftOperand(newConcatenation(operandList)));
	}

	private Concatenation newConcatenation(List<Object> operandList) {
		Concatenation concatenate = sourceRegistry.newGe(Concatenation.T);
		concatenate.setOperands(operandList);
		return concatenate;
	}

	@Override
	public OperandBuilder<ValueComparisonBuilder<T>> asString() {
		ValueComparisonBuilder<T> vcb = operand(null);

		return new OperandBuilder<>(sourceRegistry, vcb, value -> vcb.setLeftOperand(newAsString(value)));
	}

	private AsString newAsString(Object value) {
		AsString asString = sourceRegistry.newGe(AsString.T);
		asString.setOperand(value);
		return asString;
	}
}
