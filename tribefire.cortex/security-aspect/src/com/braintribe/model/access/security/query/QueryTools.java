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
package com.braintribe.model.access.security.query;

import static com.braintribe.model.access.security.query.ConditionTools.and;
import static com.braintribe.model.access.security.query.ConditionTools.not;
import static com.braintribe.model.access.security.query.ConditionTools.or;
import static com.braintribe.model.acl.Acl.accessibility;
import static com.braintribe.model.acl.HasAcl.acl;
import static com.braintribe.model.acl.HasAcl.owner;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.StandardTraversingContext;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.security.acl.AclTcs;

/**
 * 
 */
class QueryTools {

	static SourcesDescriptor findQuerySources(Query query) {
		QueryVisitor queryVisitor = new QueryVisitor();

		StandardTraversingContext traversingContext = new StandardTraversingContext();
		traversingContext.setTraversingVisitor(queryVisitor);

		EntityType<GenericEntity> queryEntityType = query.entityType();
		queryEntityType.traverse(traversingContext, query);

		return queryVisitor.resolveSourceTypes();
	}

	/** Crates a condition for given HasAcl source with given roles. */
	static Condition createAclCondition(Source source, String user, Set<String> roles) {
		return createAclCondition(newPropertyOperand(source, null), user, roles);
	}

	static Condition createAclCondition(PropertyOperand propertyOperand, String user, Set<String> roles) {
		Set<String> negativeRoles = negateAclRoles(roles);

		PropertyOperand ownerOperand = getPropOperand(propertyOperand, owner);
		PropertyOperand aclOperand = getPropOperand(propertyOperand, acl);
		PropertyOperand accessOperand = getPropOperand(aclOperand, accessibility);

		Condition isOwner = vcEqual(ownerOperand, user);
		Condition ownerIsNull = vcEqual(ownerOperand, null);
		Condition aclIsNull = vcEqual(aclOperand, null);

		Condition granted = containsOneOfRoles(accessOperand, roles);
		Condition notDenied = not(containsOneOfRoles(accessOperand, negativeRoles));

		return or( //
				isOwner, //
				and(ownerIsNull, aclIsNull), //
				and(granted, notDenied) //
		);
	}

	private static Set<String> negateAclRoles(Set<String> roles) {
		Set<String> result = newSet();

		for (String role : roles)
			result.add("!" + role);

		return result;
	}

	private static Condition containsOneOfRoles(PropertyOperand etilsPropertyOperand, Set<String> roles) {
		List<Condition> comparisons = new ArrayList<Condition>();

		for (String role : roles) {
			ValueComparison roleCheck = vcContains(etilsPropertyOperand, role);

			comparisons.add(roleCheck);
		}

		return or(comparisons);
	}

	private static PropertyOperand getPropOperand(PropertyOperand propOperand, String propName) {
		PropertyOperand result = PropertyOperand.T.create();
		result.setSource(propOperand.getSource());
		result.setPropertyName(enchain(propOperand.getPropertyName(), propName));

		return result;
	}

	private static String enchain(String propPath, String propName) {
		return propPath == null ? propName : propPath + "." + propName;
	}

	static void appendConditions(Query query, List<Condition> conditionsToAppend) {
		appendCondition(query, and(conditionsToAppend));
	}

	private static void appendCondition(Query query, Condition conditionToAppend) {
		Restriction restriction = query.getRestriction();
		if (restriction == null) {
			restriction = Restriction.T.create();
			query.setRestriction(restriction);
		}

		Condition oldCondition = restriction.getCondition();

		Condition newCondition = oldCondition == null ? conditionToAppend : and(oldCondition, conditionToAppend);

		restriction.setCondition(newCondition);
	}

	public static void appendAclLoadingTc(Query query) {
		query.setTraversingCriterion(ensureAclLoaded(query.getTraversingCriterion()));
	}
	
	private static TraversingCriterion ensureAclLoaded(TraversingCriterion queryTc) {
		return queryTc == null ? AclTcs.DEFAULT_TC_WITH_ACL : AclTcs.addAclEagerLoadingTo(queryTc);
	}

	static PropertyOperand newPropertyOperand(Source source, String propertyPath) {
		PropertyOperand operand = PropertyOperand.T.create();
		operand.setSource(source);
		operand.setPropertyName(propertyPath);

		return operand;
	}

	private static ValueComparison vcContains(PropertyOperand collectionOperand, Object value) {
		return valueComparison(Operator.contains, collectionOperand, value);
	}

	private static ValueComparison vcEqual(PropertyOperand propertyOperand, Object value) {
		return valueComparison(Operator.equal, propertyOperand, value);
	}

	private static ValueComparison valueComparison(Operator operator, Object left, Object right) {
		ValueComparison roleCheck = ValueComparison.T.create();
		roleCheck.setLeftOperand(left);
		roleCheck.setOperator(operator);
		roleCheck.setRightOperand(right);
		return roleCheck;
	}

	static Condition extractCondition(Query query) {
		Restriction r = query.getRestriction();

		return r != null ? r.getCondition() : null;
	}
}
