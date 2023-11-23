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
package com.braintribe.model.processing.template.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.bvd.conditional.Coalesce;
import com.braintribe.model.bvd.convert.ToSet;
import com.braintribe.model.bvd.convert.ToString;
import com.braintribe.model.bvd.math.Subtract;
import com.braintribe.model.bvd.query.ResultConvenience;
import com.braintribe.model.bvd.string.Concatenation;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.value.Escape;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.processing.template.building.impl.Templates;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.vd.ResolveVariables;
import com.braintribe.model.user.User;

/**
 * Provides tests for {@link TemplateEvaluation}.
 * 
 */
public class TemplateEvaluationTest {

	
	@Test
	public void testVariableWithDefaultValueEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createQueryTemplateWithVariablesEvaluation("Foo", false);
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		assertThat(queryString).isEqualTo("select * from com.braintribe.model.user.User f where f.firstName like 'Foo'");
	}

	@Test
	public void testVariableWithDefaultValueAndScriptEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createQueryTemplateWithVariablesAndScriptEvaluation("Foo", false);
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		assertThat(queryString).isEqualTo("select * from com.braintribe.model.user.User f where f.firstName like 'Foo'");
	}
	
	@Test
	public void testVariableWithDefaultNullValueEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createQueryTemplateWithVariablesEvaluation(null, false);
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		assertThat(queryString).isEqualTo("select * from com.braintribe.model.user.User f where f.firstName like null");
	}
	
	@Test
	public void testVariableWithDefaultNullValueAndScriptEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createQueryTemplateWithVariablesAndScriptEvaluation(null, false);
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		assertThat(queryString).isEqualTo("select * from com.braintribe.model.user.User f where f.firstName like null");
	}

	@Test
	public void testVariableWithExplicitValueEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createQueryTemplateWithVariablesEvaluation("Foo", true);
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		assertThat(queryString).isEqualTo("select * from com.braintribe.model.user.User f where f.firstName like 'Foo'");
	}

	@Test
	public void testVariableWithExplicitValueAndScriptEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createQueryTemplateWithVariablesAndScriptEvaluation("Foo", true);
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		assertThat(queryString).isEqualTo("select * from com.braintribe.model.user.User f where f.firstName like 'Foo'");
	}

	@Test
	public void testVariableWithExplicitNullValueEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createQueryTemplateWithVariablesEvaluation(null, true);
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		assertThat(queryString).isEqualTo("select * from com.braintribe.model.user.User f where f.firstName like null");
	}

	@Test
	public void testVariableWithExplicitNullValueAndScriptEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createQueryTemplateWithVariablesAndScriptEvaluation(null, true);
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		assertThat(queryString).isEqualTo("select * from com.braintribe.model.user.User f where f.firstName like null");
	}

	
	@Test
	public void testComplexFulltextQueryTemplateWithVariablesAndScriptEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createComplexFulltextQueryTemplateWithVariablesAndScriptEvaluation("foo", "bar", false);
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		assertThat(queryString).isEqualTo("select * from com.braintribe.model.user.User f where ((f.firstName like 'foo' and fullText(null, 'bar')) or (f.lastName like 'foo' and fullText(null, 'bar')))");
	}

	@Test
	public void testComplexFulltextQueryTemplateWithExplicitVariablesAndScriptEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createComplexFulltextQueryTemplateWithVariablesAndScriptEvaluation("foo", "bar", true);
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		assertThat(queryString).isEqualTo("select * from com.braintribe.model.user.User f where ((f.firstName like 'foo' and fullText(null, 'bar')) or (f.lastName like 'foo' and fullText(null, 'bar')))");
	}
	
	
	@Test
	public void testQueryEscapedSelectionEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createSelectionQuery();
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		assertThat(queryString).isEqualTo("select coalesce(f, null) from com.braintribe.model.user.User f where f = coalesce(f, null)");
	}
	
	
	@Test
	public void testRecursiveEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createSelectionQueryWithStaticRecursiveEvaluation();
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		assertThat(queryString).isEqualTo("select 'foo.1' from com.braintribe.model.user.User f");
	}
	
	@Test
	public void testEscapedRecursiveEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createSelectionQueryWithStaticEscapedRecursiveEvaluation();
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		assertThat(queryString).isEqualTo("select concatenation(coalesce('foo', null), '.', toString(null, subtract(3, 2))) from com.braintribe.model.user.User f");
	}
	
	@Test
	public void testNestedEscapedWithExplicitVariableResolvementEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createQueryTemplateWithSubQueryAndVariablesEvaluation();
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		
		assertThat(queryString).isEqualTo("select user from com.braintribe.model.user.User user where (user.name like 'Foo' or user in toSet(query(select * from com.braintribe.model.user.User f where f.firstName like 'Foo', enum(com.braintribe.model.bvd.query.ResultConvenience, list)))) order by user.name desc");
	}

	@Test
	public void testEscapedSelectionWithExplicitVariableResolvementEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createQueryTemplateWithSelectionAndVariablesEvaluation();
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(SelectQuery.class);
		
		assertThat(queryString).isEqualTo("select concatenation(toString(null, user), '.', 'Foo') from com.braintribe.model.user.User user where user.name like 'Foo'");
	}
	

	@Test
	public void testPropertyQueryWithEntityReferenceEvaluation() throws Exception {

		TemplateEvaluation templateEvaluation = createPropertyQueryTemplateWithEntityReferenceEvaluation();
		Query query = templateEvaluation.evaluateTemplate(false);
		
		String queryString = query.stringify();
		System.out.println("Successfully evaluated template query to: " + queryString);
		
		// validate output
		assertThat(query).isNotNull();
		assertThat(query).isInstanceOf(PropertyQuery.class);
		assertThat(((PropertyQuery)query).getEntityReference()).isNotNull();
		assertThat(queryString).isEqualTo("property roles of reference(com.braintribe.model.user.User, '12345')");
	}
	
	private TemplateEvaluation createQueryTemplateWithSubQueryAndVariablesEvaluation() {

		Variable v = Variable.T.create();
		v.setDefaultValue("Foo");
		v.setName("Name");
		
		SelectQuery q =  new SelectQueryBuilder()
				.from(User.T,"f")
				.where()
					.property("f", "firstName").comparison(Operator.like).value(v)
				.done();
		
		
		com.braintribe.model.bvd.query.Query sqVd = com.braintribe.model.bvd.query.Query.T.create();
		sqVd.setQuery(q);
		sqVd.setResultConvenience(ResultConvenience.list);
		
		ToSet toSet = ToSet.T.create();
		toSet.setOperand(sqVd);
		
		Escape escape = Escape.T.create();
		escape.setValue(toSet);
		
		ResolveVariables resolveVariables = ResolveVariables.T.create();
		resolveVariables.setValue(escape);
		
		SelectQuery tq =  new SelectQueryBuilder()
				.from(User.T,"user")
				.where()
					.disjunction()
							.property("user",User.name).comparison(com.braintribe.model.query.Operator.like).value(v)
							.entity("user").in().value(escape)
					.close()
				.select()
					.entity("user")
				.orderBy(OrderingDirection.descending)
					.property("user", User.name)
				.done();
		
		TemplateEvaluation result = createTemplateFor(tq);

		return result;
	}
	
	private TemplateEvaluation createQueryTemplateWithSelectionAndVariablesEvaluation() {
		Variable v = Variable.T.create();
		v.setName("Name");
		v.setDefaultValue("Foo");
		
		SelectQuery q =  new SelectQueryBuilder()
				.from(User.T,"user")
				.where()
					.property("user","name").comparison(Operator.like).value(v)
				.select()
					.entity("user")
					
				.done();
		
		Object selection = q.getSelections().get(0);
		
		ToString toString = ToString.T.create();
		toString.setOperand(q.getFroms().get(0));
		
		Concatenation concat = Concatenation.T.create();
		concat.getOperands().add(toString);
		concat.getOperands().add(".");
		concat.getOperands().add(v);
		
		Escape escape = Escape.T.create();
		escape.setValue(concat);
		
		ResolveVariables resolveVariables = ResolveVariables.T.create();
		resolveVariables.setValue(escape);
		
		
		q.getSelections().remove(selection);
		q.getSelections().add(escape);
		
		return createTemplateFor(q);
	}
	
	
	private TemplateEvaluation createPropertyQueryTemplateWithEntityReferenceEvaluation() {
		
		PropertyQuery pq = PropertyQueryBuilder.forProperty(User.T, "12345", "roles").done();
		return createTemplateFor(pq);
		
	}
	
	private TemplateEvaluation createQueryTemplateWithVariablesEvaluation(Object value, boolean isExplicit) {
		
		Variable v = Variable.T.create();
		v.setName("Name");
		
		Map<String, Object> variableMap = new HashMap<>();
		if (isExplicit) {
			variableMap.put("Name", value);
		} else {
			v.setDefaultValue(value);
		}
		
		SelectQuery q =  new SelectQueryBuilder()
				.from(User.T,"f")
				.where()
					.property("f", "firstName").comparison(Operator.like).value(v)
				.done();
		
		return createTemplateFor(q, variableMap);
		
	}
	
	private TemplateEvaluation createQueryTemplateWithVariablesAndScriptEvaluation(Object value, boolean isExplicit) {
		
		SelectQuery q =  new SelectQueryBuilder()
				.from(User.T,"f")
				.where()
					.property("f", "firstName").comparison(Operator.like).value(null)
				.done();

		//@formatter:off
		Template template = 
				Templates
				.template(LocalizedString.create("Query Template"))
				.prototype(c -> {
					return (SelectQuery) q.clone(new StandardCloningContext() {
						@Override
						public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
							return c.create(entityType);
						}
					});
				})
				.record(c -> {
					SelectQuery query = c.getPrototype();
					ValueComparison vc = (ValueComparison) query.getRestriction().getCondition();
					c.pushVariable("Name");
					vc.setRightOperand((isExplicit ? null : value));
				})
				.build();
		//@formatter:on

		Map<String, Object> variableMap = new HashMap<>();
		if (isExplicit) {
			variableMap.put("Name", value);
		}

		return createTemplateFor(template, variableMap);
		
	}
	
	private TemplateEvaluation createSelectionQuery() {
		
		SelectQuery q =  new SelectQueryBuilder()
				.from(User.T,"f")
				.where()
					.entity("f").eq(null)
				.select()
					.entity("f")
					
				.done();
		
		Object selection = q.getSelections().get(0);
		
		Coalesce coalesce = Coalesce.T.create();
		coalesce.setOperand(selection);
		
		Escape escape = Escape.T.create();
		escape.setValue(coalesce);
		
		q.getSelections().remove(selection);
		q.getSelections().add(escape);
		
		ValueComparison condition = (ValueComparison) q.getRestriction().getCondition();
		condition.setRightOperand(escape);
		
		return createTemplateFor(q);
		
	}
	
	private TemplateEvaluation createSelectionQueryWithStaticRecursiveEvaluation() {
		
		SelectQuery q =  new SelectQueryBuilder()
				.from(User.T,"f")
				.select()
					.entity("f")
				.done();
		
		
		Coalesce coalesce = Coalesce.T.create();
		coalesce.setOperand("foo");
		
		Subtract subtract = Subtract.T.create();
		subtract.getOperands().add(3);
		subtract.getOperands().add(2);
		
		ToString toString = ToString.T.create();
		toString.setOperand(subtract);
		
		
		Concatenation concat = Concatenation.T.create();
		concat.getOperands().add(coalesce);
		concat.getOperands().add(".");
		concat.getOperands().add(toString);
		
		Object selection = q.getSelections().get(0);
		q.getSelections().remove(selection);
		q.getSelections().add(concat);
		
		return createTemplateFor(q);
		
	}

	private TemplateEvaluation createSelectionQueryWithStaticEscapedRecursiveEvaluation() {
		
		SelectQuery q =  new SelectQueryBuilder()
				.from(User.T,"f")
				.select()
					.entity("f")
				.done();
		
		
		Coalesce coalesce = Coalesce.T.create();
		coalesce.setOperand("foo");
		
		Subtract subtract = Subtract.T.create();
		subtract.getOperands().add(3);
		subtract.getOperands().add(2);
		
		ToString toString = ToString.T.create();
		toString.setOperand(subtract);
		
		
		Concatenation concat = Concatenation.T.create();
		concat.getOperands().add(coalesce);
		concat.getOperands().add(".");
		concat.getOperands().add(toString);
		
		Escape escape = Escape.T.create();
		escape.setValue(concat);
		
		Object selection = q.getSelections().get(0);
		q.getSelections().remove(selection);
		q.getSelections().add(escape);
		
		return createTemplateFor(q);
		
	}

	private TemplateEvaluation createComplexFulltextQueryTemplateWithVariablesAndScriptEvaluation(String nameValue, String fulltextValue, boolean isExplicit) {
		
		SelectQuery q =  new SelectQueryBuilder()
				.from(User.T,"f")
				.where()
					.disjunction()
						.conjunction()
							.property("f", "firstName").comparison(Operator.like).value(null)
							.fullText(null, null)
						.close()
						.conjunction()
							.property("f", "lastName").comparison(Operator.like).value(null)
							.fullText(null, null)
						.close()
					.close()
				.done();

		//@formatter:off
		Template template = 
				Templates
				.template(LocalizedString.create("Query Template"))
				.prototype(c -> {
					return (SelectQuery) q.clone(new StandardCloningContext() {
						@Override
						public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
							return c.create(entityType);
						}
					});
				})
				.record(c -> {
					SelectQuery query = c.getPrototype();
					
					Disjunction dc = (Disjunction) query.getRestriction().getCondition();
					Conjunction cc1 = (Conjunction ) dc.getOperands().get(0);
					Conjunction cc2 = (Conjunction ) dc.getOperands().get(1);
					
					ValueComparison vc1 = (ValueComparison) cc1.getOperands().get(0);
					ValueComparison vc2 = (ValueComparison) cc2.getOperands().get(0);
					
					FulltextComparison fc1 = (FulltextComparison) cc1.getOperands().get(1);
					FulltextComparison fc2 = (FulltextComparison) cc2.getOperands().get(1);
					
					c.pushVariable("Name");
					vc1.setRightOperand(isExplicit ? null : nameValue);
					c.pushVariable("Name");
					vc2.setRightOperand(isExplicit ? null : nameValue);
					
					c.pushVariable("searchText");
					fc1.setText(isExplicit ? null : fulltextValue);
					c.pushVariable("searchText");
					fc2.setText(isExplicit ? null : fulltextValue);
					
					
				})
				.build();
		//@formatter:on

		Map<String, Object> variableMap = new HashMap<>();
		if (isExplicit) {
			variableMap.put("Name", nameValue);
			variableMap.put("searchText", fulltextValue);
		}

		return createTemplateFor(template, variableMap);
		
	}
	

	private TemplateEvaluation createTemplateFor(Object prototype) {
		return createTemplateFor(prototype, Collections.emptyMap());
	}
	
	private TemplateEvaluation createTemplateFor(Object prototype, Map<String, Object> variableMap) {
		Template template = null;
		if (prototype instanceof Template) {
			template = (Template) prototype;
		} else {
			template = Template.T.create();
			template.setPrototype(prototype);
		}
		
		Smood persistence = new Smood(EmptyReadWriteLock.INSTANCE);
		BasicPersistenceGmSession session = new BasicPersistenceGmSession(persistence);
		
		Map<String, Variable> variableHarmonization = new HashMap<>();
		
		Template sessionTemplate = BaseType.INSTANCE.clone(new StandardCloningContext() {
			
				@Override
				public <T> T getAssociated(GenericEntity entity) {
					if (entity instanceof Variable) {
						Variable v = (Variable) entity;
						Variable harmonizedV = variableHarmonization.get(v.getName());
						if (harmonizedV != null) {
							return (T) harmonizedV;
						}
					}
					return super.getAssociated(entity);
				}
			
				@Override
				public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
					GenericEntity clone = session.create(entityType);
					clone.setId(UUID.randomUUID().toString());
					
					if (instanceToBeCloned instanceof Variable) {
						Variable v = (Variable) instanceToBeCloned;
						Variable cloneV = (Variable) clone;
						variableHarmonization.put(v.getName(), cloneV);
					}
					return clone;
				}
				
				@Override
				public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
					GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
					return !property.isIdentifier();
				}
				
		}, template, null);
		
		TemplateEvaluation result = new TemplateEvaluation();
		result.setTemplate(sessionTemplate);
		result.setValueDescriptorValues(variableMap);
		result.setTargetSession(session);
		
		return result;
	}


}
