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

import java.util.function.Consumer;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.pr.criteria.ComparisonOperator;
import com.braintribe.model.generic.pr.criteria.ConjunctionCriterion;
import com.braintribe.model.generic.pr.criteria.DisjunctionCriterion;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.JokerCriterion;
import com.braintribe.model.generic.pr.criteria.JunctionCriterion;
import com.braintribe.model.generic.pr.criteria.ListElementCriterion;
import com.braintribe.model.generic.pr.criteria.MapCriterion;
import com.braintribe.model.generic.pr.criteria.MapKeyCriterion;
import com.braintribe.model.generic.pr.criteria.MapValueCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.pr.criteria.RecursionCriterion;
import com.braintribe.model.generic.pr.criteria.RootCriterion;
import com.braintribe.model.generic.pr.criteria.SetElementCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.ValueConditionCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.typecondition.TypeCondition;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.tc)
@SuppressWarnings("unusable-by-js")
public class CriterionBuilder<T> {
	protected T backLink;
	protected Consumer<? super TraversingCriterion> receiver;
	
	@JsIgnore
	public CriterionBuilder(T backLink, Consumer<? super TraversingCriterion> receiver) {
		this.backLink = backLink;
		this.receiver = receiver;
	}
	
	@JsIgnore
	protected CriterionBuilder() {
		
	}
	
	@JsIgnore
	protected JunctionBuilder<T> getJunctionBuilder(EntityType<? extends JunctionCriterion> junctionType){
		return new JunctionBuilder<>(junctionType, backLink, receiver);
	}
	
	@JsIgnore
	protected PatternBuilder<T> getPatternBuilder(){
		return new PatternBuilder<>(backLink, receiver);
	}
	
	@JsIgnore
	protected CriterionBuilder<T> getCriterionBuilder(T t, Consumer<? super TraversingCriterion> receiver){
		return new CriterionBuilder<>(t,receiver);
	}
	
	@JsIgnore
	protected void setBackLink(T backLink) {
		this.backLink = backLink;
	}
	
	@JsIgnore
	protected void setReceiver(Consumer<? super TraversingCriterion> receiver) {
		this.receiver = receiver;
	}
	
	public PatternBuilder<T> pattern() {
		PatternBuilder<T> patternBuilder = getPatternBuilder();
		return patternBuilder;
	}
	
	public T placeholder(String name) {
		return criterion(TC.placeholder(name));
	}
	
	public JunctionBuilder<T> conjunction() {
		return getJunctionBuilder(ConjunctionCriterion.T);
	}
	
	public JunctionBuilder<T> disjunction() {
		return getJunctionBuilder(DisjunctionCriterion.T);
	}
	
	public CriterionBuilder<T> negation() {
		CriterionBuilder<T> criterionBuilder = getCriterionBuilder(backLink,  criterion -> receiver.accept(TC.negation(criterion)));
		return criterionBuilder;
	}
	
	public T typeCondition(TypeCondition typeCondition) {
		try {
			receiver.accept(TC.typeCondition(typeCondition));
			return backLink;
		} catch (Exception e) {
			throw new TraversingCriteriaBuilderException(e);
		}
	}

	@JsMethod(name="emptyEntity")
	public T entity() {
		return entity((String)null);
	}
	
	@JsMethod(name="entityViaClass")
	public T entity(Class<? extends GenericEntity> entityClass) {
		return entity(entityClass.getName());
	}
	
	@JsMethod(name="entityViaType")
	public T entity(EntityType<?> entityType) {
		return entity(entityType.getTypeSignature());
	}
	
	public T entity(String typeSignature) {
		EntityCriterion criterion = EntityCriterion.T.create();
		criterion.setTypeSignature(typeSignature);
		return criterion(criterion);
	}
	
	@JsMethod(name="emptyProperty")
	public T property() {
		return propertyWithType(null, (String)null);
	}
	
	public T property(String name) {
		return propertyWithType(name, (String)null);
	}
	
	@JsMethod(name="propertyTypeViaType")
	public T propertyType(GenericModelType type) {
		return propertyWithType(null, type.getTypeSignature());
	}
	
	@JsMethod(name="propertyTypeViaTypeSignature")
	public T propertyType(String typeSignature) {
		return propertyWithType(null, typeSignature);
	}
	
	@JsMethod(name="propertyTypeViaClass")
	public T propertyType(Class<? extends GenericEntity> entityClass) {
		return propertyWithType(null, entityClass.getName());
	}
	
	
	public T propertyWithType(String name, String typeSignature) {
		PropertyCriterion criterion = PropertyCriterion.T.create();
		criterion.setPropertyName(name);
		criterion.setTypeSignature(typeSignature);
		return criterion(criterion);
	}
	
	@JsMethod(name="propertyWithTypeViaClass")
	public T propertyWithType(String name, Class<? extends GenericEntity> entityClass){
		return propertyWithType(name, entityClass.getName());
	}
	
	@JsMethod(name="propertyWithTypeViaType")
	public T propertyWithType(String name, GenericModelType type){
		return propertyWithType(name, type.getTypeSignature());
	}
	
	public T valueCondition(String propertyPath, ComparisonOperator operator, Object operand) {
		ValueConditionCriterion criterion = ValueConditionCriterion.T.create();
		criterion.setPropertyPath(propertyPath);
		criterion.setOperator(operator);
		criterion.setOperand(operand);
		return criterion(criterion);
	}

	public T joker() {
		return criterion(JokerCriterion.T.create());
	}
	
	public T root() {
		return criterion(RootCriterion.T.create());
	}
	
	@JsMethod(name="rootTypeSignature")
	public T root(String typeSignature) {
		RootCriterion criterion = RootCriterion.T.create();
		criterion.setTypeSignature(typeSignature);
		return criterion(criterion);
	}
	
	@JsMethod(name="rootViaClass")
	public T root(Class<? extends GenericEntity> entityClass) {
		return root(entityClass.getName());
	}
	
	@JsMethod(name="rootViaType")
	public T root(GenericModelType type) {
		return root(type.getTypeSignature());
	}
	
	public T criterion(TraversingCriterion criterion) {
		try {
			receiver.accept(criterion);
			return backLink;
		} catch (Exception e) {
			throw new TraversingCriteriaBuilderException(e);
		}
	}
	
	public CriterionBuilder<T> recursion(final int min, final int max) {
		CriterionBuilder<T> criterionBuilder = getCriterionBuilder(backLink, new Consumer<TraversingCriterion>() {
			@Override
			public void accept(TraversingCriterion criterion) throws RuntimeException {
				RecursionCriterion recursionCriterion = RecursionCriterion.T.create();
				recursionCriterion.setCriterion(criterion);
				recursionCriterion.setMinRecursion(min);
				recursionCriterion.setMaxRecursion(max);
				receiver.accept(recursionCriterion);
			}
		});
		return criterionBuilder;
	}
	
	public T setElement(){
		SetElementCriterion criterion = SetElementCriterion.T.create();
		return criterion(criterion);
	}
	
	@JsMethod(name="setElementViaSignature")
	public T setElement(String typeSignature) {
		SetElementCriterion criterion = SetElementCriterion.T.create();
		criterion.setTypeSignature(typeSignature);
		return criterion(criterion);
	}
	
	@JsMethod(name="setElementViaType")
	public T setElement(GenericModelType type) {
		return setElement(type.getTypeSignature());
	}
	
	@JsMethod(name="setElementViaClass")
	public T setElement(Class<? extends GenericEntity> entityClass) {
		return setElement(entityClass.getName());
	}
	
	@JsMethod(name="emptylistElement")
	public T listElement()  {
		ListElementCriterion criterion = ListElementCriterion.T.create();
		return criterion(criterion);
	}
	
	public T map()  {
		MapCriterion criterion = MapCriterion.T.create();
		return criterion(criterion);
	}
	
	@JsMethod(name="mapViaType")
	public T map(GenericModelType type) {
		return map(type.getTypeSignature());
	}
	
	@JsMethod(name="mapViaTypeSignature")
	public T map(String typeSignature) {
		MapCriterion criterion = MapCriterion.T.create();
		criterion.setTypeSignature(typeSignature);
		return criterion(criterion);
	}	
	
	public T mapValue()  {
		MapValueCriterion criterion = MapValueCriterion.T.create();
		return criterion(criterion);
	}
	
	@JsMethod(name="mapValueViaTypeSignature")
	public T mapValue(String typeSignature) {
		MapValueCriterion criterion = MapValueCriterion.T.create();
		criterion.setTypeSignature(typeSignature);
		return criterion(criterion);
	}
	
	@JsMethod(name="mapValueViaType")
	public T mapValue(GenericModelType type) {
		return mapValue(type.getTypeSignature());
	}
	
	@JsMethod(name="mapValueViaClass")
	public T mapValue(Class<? extends GenericEntity> entityClass) {
		return mapValue(entityClass.getName());
	}
	
	public T mapKey()  {
		MapKeyCriterion criterion = MapKeyCriterion.T.create();
		return criterion(criterion);
	}
	
	@JsMethod(name="mapKeyViaTypeSignature")
	public T mapKey(String typeSignature) {
		MapKeyCriterion criterion = MapKeyCriterion.T.create();
		criterion.setTypeSignature(typeSignature);
		return criterion(criterion);
	}
	
	@JsMethod(name="mapKeyViaType")
	public T mapKey(GenericModelType type) {
		return mapKey(type.getTypeSignature());
	}	
	
	@JsMethod(name="mapKeyViaClass")
	public T mapKey(Class<? extends GenericEntity> entityClass) {
		return mapKey(entityClass.getName());
	}	
	
	public T listElement(String typeSignature) {
		ListElementCriterion criterion = ListElementCriterion.T.create();
		criterion.setTypeSignature(typeSignature);
		return criterion(criterion);
	}
	
	@JsMethod(name="listElementViaType")
	public T listElement(GenericModelType type) {
		return listElement(type.getTypeSignature());
	}
	
	@JsMethod(name="listElementViaClass")
	public T listElement(Class<? extends GenericEntity> entityClass) {
		return listElement(entityClass.getName());
	}
	
}
