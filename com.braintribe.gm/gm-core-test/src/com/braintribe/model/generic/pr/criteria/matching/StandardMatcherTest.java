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
package com.braintribe.model.generic.pr.criteria.matching;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.common.attribute.common.UserInfo;
import com.braintribe.common.attribute.common.UserInfoAttribute;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.AclCriterion;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.JokerCriterion;
import com.braintribe.model.generic.pr.criteria.RootCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.AbstractProperty;
import com.braintribe.model.generic.reflection.StandardTraversingContext;
import com.braintribe.model.generic.reflection.cloning.model.AclTcEntity;
import com.braintribe.model.generic.reflection.cloning.model.City;
import com.braintribe.utils.collection.impl.AttributeContexts;

/**
 * Tests for {@link StandardMatcher}
 * 
 * @author peter.gazdik
 */
public class StandardMatcherTest {

	private final StandardMatcher matcher = newMatcher();

	private StandardMatcher newMatcher() {
		StandardMatcher result = new StandardMatcher();
		result.setCheckOnlyProperties(false);
		return result;
	}

	private final StandardTraversingContext context = new StandardTraversingContext();

	@Test
	public void testJoker() throws Exception {
		setMatcherTc(JokerCriterion.T.create());

		pushRoot("string");

		assertMatches(true);
	}

	@Test
	public void testAcl_NonAclEntity() throws Exception {
		setMatcherTc(aclWriteCriterion());

		City nonAcl = City.T.create();

		pushRoot(nonAcl);
		assertMatches(false);

		pushEntity(nonAcl);
		assertMatches(false);
	}

	@Test
	public void testAcl_NoContext() throws Exception {
		setMatcherTc(aclWriteCriterion());

		AclTcEntity aclEntity = AclTcEntity.T.create();
		aclEntity.setOwner("AclOwner");

		pushRoot(aclEntity);
		assertMatches(false);

		pushEntity(aclEntity);
		assertMatches(false);
	}

	@Test
	public void testAcl_Matches() throws Exception {
		setMatcherTc(aclWriteCriterion());

		AclTcEntity aclEntity = AclTcEntity.T.create();
		aclEntity.setOwner("AclOwner");

		UserInfo ui = UserInfo.of("AclOwner", null);
		AttributeContexts.push(AttributeContexts.derivePeek().set(UserInfoAttribute.class, ui).build());

		pushRoot(aclEntity);
		assertMatches(false);

		pushEntity(aclEntity);
		assertMatches(true); // HERE WE FINALLY MATCH

		pushProperty("name");
		assertMatches(false); // we don't match properties, even if on matching ACL entity
	}

	private AclCriterion aclWriteCriterion() {
		AclCriterion acl = AclCriterion.T.create();
		acl.setOperation("write");
		return acl;
	}

	private void setMatcherTc(TraversingCriterion tc) {
		matcher.setCriterion(tc);
	}

	private void pushRoot(Object o) {
		RootCriterion rootCriterion = RootCriterion.T.createPlain();
		rootCriterion.setTypeSignature(GMF.getTypeReflection().getType(o).getTypeSignature());
		context.pushTraversingCriterion(rootCriterion, o);

	}

	private void pushEntity(GenericEntity entity) {
		EntityCriterion ec = EntityCriterion.T.createPlainRaw();
		ec.setTypeSignature(entity.entityType().getTypeSignature());

		context.pushTraversingCriterion(ec, entity);
	}

	private void pushProperty(String propertyName) {
		GenericEntity entity = (GenericEntity) context.getObjectStack().peek();
		AbstractProperty p = (AbstractProperty) entity.entityType().getProperty(propertyName);

		context.pushTraversingCriterion(p.acquireCriterion(), p.get(entity));
	}

	private void assertMatches(boolean expected) {
		assertThat(matcher.matches(context)).isEqualTo(expected);
	}

}
