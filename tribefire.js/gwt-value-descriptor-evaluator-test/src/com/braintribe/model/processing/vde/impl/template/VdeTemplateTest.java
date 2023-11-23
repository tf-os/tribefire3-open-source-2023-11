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
package com.braintribe.model.processing.vde.impl.template;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.bvd.string.Concatenation;
import com.braintribe.model.bvd.string.Lower;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.Escape;
import com.braintribe.model.processing.vde.impl.misc.VdeTestTemplate;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Template is a regular (non-VDE) entity which has VDEs as it's properties.
 */
public class VdeTemplateTest extends VdeTest {

	private static final EntityType<VdeTestTemplate> et = VdeTestTemplate.T;
	private static final Property stringParamP = et.getProperty("stringParam");

	private final VdeTestTemplate template = et.create();

	@Test
	public void testEvaluatesVdParams() throws Exception {
		String operand = "GOGOPLATA";

		Lower vd = $.lower(operand);

		stringParamP.setVd(template, vd);
		template.setObjectParam(vd);

		VdeTestTemplate result = evalTemplate();

		assertThat(result.getStringParam()).isEqualTo(operand.toLowerCase());
		assertThat(result.getObjectParam()).isEqualTo(operand.toLowerCase());
	}

	@Test
	public void testEvaluatesCollections() throws Exception {
		runWithCollection(asList(template("CO"), template("VID")));
		runWithCollection(asSet(template("BUNNY")));
	}

	private void runWithCollection(Collection<VdeTestTemplate> c) {
		Collection<VdeTestTemplate> e = (Collection<VdeTestTemplate>) evaluate(c);
		checkEvaledCollection(c, e);
	}

	@Test
	public void testEvaluatesMap() throws Exception {
		Map<VdeTestTemplate, VdeTestTemplate> map = asMap(template("CO"), template("VID"));
		Map<VdeTestTemplate, VdeTestTemplate> evaledMap = (Map<VdeTestTemplate, VdeTestTemplate>) evaluate(map);

		checkEvaledCollection(map.keySet(), evaledMap.keySet());
		checkEvaledCollection(map.values(), evaledMap.values());
	}

	private void checkEvaledCollection(Collection<VdeTestTemplate> c, Collection<VdeTestTemplate> e) {
		assertThat(e instanceof List).isEqualTo(c instanceof List);
		assertThat(e instanceof Set).isEqualTo(c instanceof Set);
		assertThat(e).hasSameSizeAs(c);

		Iterator<VdeTestTemplate> cit = c.iterator();
		Iterator<VdeTestTemplate> eit = e.iterator();

		while (cit.hasNext())
			checkEvaled(cit.next(), eit.next());
	}

	private void checkEvaled(VdeTestTemplate orig, VdeTestTemplate evaled) {
		Lower lower = (Lower) orig.getObjectParam();
		String lowerOperand = (String) lower.getOperand();
		assertThat(evaled.getObjectParam()).isEqualTo(lowerOperand.toLowerCase());
	}

	private VdeTestTemplate template(String paramValue) {
		VdeTestTemplate result = VdeTestTemplate.T.create();
		result.setObjectParam($.lower(paramValue));

		return result;
	}

	/** Escape is evaluated by simply cloning it's operand */
	@Test
	public void testEvaluatesEscapeAsVdParam() throws Exception {
		String operand = "GOGOPLATA";

		Lower escapedLower = $.lower(operand);
		Escape escape = $.escape(escapedLower);

		template.setObjectParam(escape);

		VdeTestTemplate result = evalTemplate();

		assertThat(result.getObjectParam()).isInstanceOf(Lower.class).isNotSameAs(escapedLower);
	}

	/** Escape's operand, when cloned, is not evaluated, but it's VDs remain VDs */
	@Test
	public void testVdOfEscapedVd() throws Exception {
		Concatenation concat = $.concatenation();
		Lower escapedLower = $.lower(concat);
		Escape escape = $.escape(escapedLower);

		template.setObjectParam(escape);

		VdeTestTemplate result = evalTemplate();

		Object objectParam = result.getObjectParam();
		assertThat(objectParam).isInstanceOf(Lower.class);

		Lower _lower = (Lower) objectParam;
		assertThat(_lower.getOperand()).isInstanceOf(Concatenation.class).isNotSameAs(concat);
	}

	@Test
	public void testVdHolderOfEscapedVd() throws Exception {
		Property lowerOperandP = Lower.T.getProperty("operand");

		Concatenation concat = $.concatenation();
		Lower escapedLower = $.lower();
		lowerOperandP.setVd(escapedLower, concat);

		Escape escape = $.escape(escapedLower);

		template.setObjectParam(escape);

		VdeTestTemplate result = evalTemplate();

		Object objectParam = result.getObjectParam();
		assertThat(objectParam).isInstanceOf(Lower.class);

		Lower _lower = (Lower) objectParam;
		assertThat((Object) lowerOperandP.getVd(_lower)).isInstanceOf(Concatenation.class).isNotSameAs(concat);
	}

	private VdeTestTemplate evalTemplate() {
		VdeTestTemplate result = (VdeTestTemplate) evaluate(template);
		assertThat(result).isNotNull().isNotSameAs(template);
		return result;
	}

}
