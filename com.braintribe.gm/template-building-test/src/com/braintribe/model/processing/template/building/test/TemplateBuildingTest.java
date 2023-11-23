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
package com.braintribe.model.processing.template.building.test;

import java.util.Date;

import org.junit.Test;

import com.braintribe.model.bvd.math.Subtract;
import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.template.building.impl.Templates;
import com.braintribe.model.processing.template.building.test.model.Person;
import com.braintribe.model.processing.vde.builder.api.VdBuilder;
import com.braintribe.model.processing.vde.evaluator.VDE;
import com.braintribe.model.processing.vde.evaluator.api.aspects.DateAspect;
import com.braintribe.model.template.Template;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.utils.i18n.I18nTools;

/**
 * 
 * @author Dirk Scheffler
 *
 */
public class TemplateBuildingTest {

	@Test
	public void testTemplateBuilding() throws Exception {
		Template template = Templates
		.template(I18nTools.createLs("template"))
		.prototype(c -> {
			return c.create(Person.T);
		})
		.record(c -> {
			Person prototype = c.getPrototype();

			Name name = Name.T.create();
			name.setName(I18nTools.createLs("template"));
			
			c.pushVariable("name").addMetaData(name);
			prototype.setName("unkown");
			
			VdBuilder $ = VDE.builder();
			Subtract sub = $.subtract(Now.T.create(), $.timeSpan(1, TimeUnit.hour));
			
			c.pushVd(sub);
			prototype.setBirthday(null);
		})
		.build();
		
		Object prototype = template.getPrototype();
		Manipulation script = template.getScript();
		
		System.out.println(prototype);
		
	
		
		
	}
	
	@Test
	public void testVd() {
		VdBuilder $ = VDE.builder();
		Subtract sub = $.subtract($.now(), $.timeSpan(1, TimeUnit.hour), $.timeSpan(3, TimeUnit.hour));
		
		Object value = VDE.evaluate(sub);
		
		System.out.println(value);
	}


}
