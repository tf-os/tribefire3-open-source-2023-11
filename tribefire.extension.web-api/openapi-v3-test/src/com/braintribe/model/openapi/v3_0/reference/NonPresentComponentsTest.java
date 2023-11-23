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
package com.braintribe.model.openapi.v3_0.reference;

import org.junit.Test;

import com.braintribe.model.openapi.v3_0.OpenapiComponents;
import com.braintribe.model.openapi.v3_0.OpenapiSchema;
import com.braintribe.model.openapi.v3_0.reference.utils.AbstractComponentsTest;
import com.braintribe.model.openapi.v3_0.reference.utils.TestApiContext;
import com.braintribe.model.user.User;

public class NonPresentComponentsTest extends AbstractComponentsTest {

	@Test(expected = CantBuildReferenceException.class)
	public void simpleFail() {
		// only one context; only one level
		schemaRef(rootContext, User.T);
	}
	
	@Test(expected = CantBuildReferenceException.class)
	public void failTwoTypesOneContext() {
		// only one context; two levels
		schemaRef(rootContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, User.T);
			
			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);
			return comp;
		});
		
		///////////////
	}
	
	@Test(expected = CantBuildReferenceException.class)
	public void failThreeContextsOneType() {
		
		TestApiContext childContext = rootContext.childContext("CHILD");
		TestApiContext grandchildContext = childContext.childContext("CHILD");
		
		// three contexts; only one level
		schemaRef(grandchildContext, User.T);

	}
	
	@Test(expected = CantBuildReferenceException.class)
	public void failThreeContextsTwoTypes() {
		TestApiContext childContext = rootContext.childContext("CHILD");
		TestApiContext grandchildContext = childContext.childContext("CHILD");
		
		// three contexts; two levels
			schemaRef(grandchildContext, OpenapiComponents.T, c -> {
				OpenapiSchema ref = schemaRef(c, User.T);
				
				OpenapiSchema comp = OpenapiSchema.T.create();
				comp.setItems(ref);
				return comp;
			});
		
		///////////////
	}
	
	@Test
	public void succeedThreeContextsTwoModels1() {
		
		TestApiContext childContext = rootContext.childContext("CHILD", User.T::equals); // only middle context supports User type
		TestApiContext grandchildContext = childContext.childContext("GRANDCHILD", AbstractComponentsTest::isOpenapiEntity); // again no User type
		
		// three contexts; only one level; MIDDLE context supports User
		OpenapiSchema schemaRef = schemaRef(grandchildContext, User.T);
		getSchemaComponent(User.T, childContext, schemaRef); // So it got resolved with the middle context

	}

	@Test
	public void succeedThreeContextsTwoModels2() {
		TestApiContext childContext = rootContext.childContext("CHILD", User.T::equals); // only middle context supports User type
		TestApiContext grandchildContext = childContext.childContext("GRANDCHILD", AbstractComponentsTest::isOpenapiEntity); // again no User type

		// three contexts; two levels; MIDDLE context supports User
		OpenapiSchema schemaRef = schemaRef(grandchildContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, User.T);
			
			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);
			return comp;
		});
		
		// Only middle context supports user - so clearly the User type scheme gets created by that context
		// Only the outer contexts support the OpenapiComponents type
		// So the root context supports the OpenapiComponents type. But the schema references the User type which is not supported
		// Thus also the OpenapiComponent schema can't be created and gets the "notPresent" status.
		// Luckily the grandchild context also supports the OpenapiComponent type. Again the User type is not supported BUT the one from the parent context can be used 
		OpenapiSchema schemaComponent = getSchemaComponent(OpenapiComponents.T, grandchildContext, schemaRef);
		OpenapiSchema parameterRef = schemaComponent.getItems();
		OpenapiSchema childComponent = getSchemaComponent(User.T, childContext, parameterRef);
	}
	
	@Test(expected = CantBuildReferenceException.class)
	public void failThreeContextsTwoModelsBecauseCycle() {
		TestApiContext childContext = rootContext.childContext("CHILD", User.T::equals); // only middle context supports User type
		TestApiContext grandchildContext = childContext.childContext("GRANDCHILD", AbstractComponentsTest::isOpenapiEntity); // again no User type
		
		// three contexts; two levels; MIDDLE context supports User; CYCLE between mutually exclusive types (types which don't share a context in which they are both valid)
		schemaRef(grandchildContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, User.T, c2 -> {
				OpenapiSchema componentsRef = alreadyPresentSchemaRef(c2, OpenapiComponents.T);
				// fails already here
				// the following lines are just to complete the example
				OpenapiSchema cycleSchema = OpenapiSchema.T.create();
				cycleSchema.setItems(componentsRef);
				return cycleSchema;
			});
			
			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);
			
			
			return comp;
		});
		
	}
	
	@Test
	public void succeedThreeContextsTwoModels3() {
		
		TestApiContext childContext = rootContext.childContext("-CHILD"); 
		TestApiContext grandchildContext = childContext.childContext("GRANDCHILD", User.T::equals); // only this context supports User type
		
		// three contexts; only one level; GRANDCHILD context supports User
		OpenapiSchema schemaRef = schemaRef(grandchildContext, User.T);
		getSchemaComponent(User.T, grandchildContext, schemaRef); // So it got resolved with the grandchild context

	}
	
	@Test(expected = CantBuildReferenceException.class)
	public void failThreeContextsTwoModelsBecauseDependency() {
		TestApiContext childContext = rootContext.childContext("-CHILD"); 
		TestApiContext grandchildContext = childContext.childContext("GRANDCHILD", User.T::equals); // only this context supports User type

		// three contexts; two levels; GRANDCHILD context supports User
		OpenapiSchema schemaRef = schemaRef(grandchildContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, User.T);
			
			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);
			return comp;
		});
		
		// Only the grandchild context supports the User type
		// Unfortunately the OpenapiComponent scheme depends on it
		// None of the contexts that support the OpenapiComponent type have a parent context that supports the User type
		// Thus it can't be built
	}
	
	@Test
	public void succeedThreeContextsTwoModels4() {
		
		
		TestApiContext childContext = rootContext.childContext("CHILD", User.T::equals); // this context supports User type 
		TestApiContext grandchildContext = childContext.childContext("GRANDCHILD"); // this context supports User type as well
		
		// three contexts; only one level; only root context does not support user
		OpenapiSchema schemaRef = schemaRef(grandchildContext, User.T);
		getSchemaComponent(User.T, childContext, schemaRef); // So it got resolved with the child context

	}
	
	@Test(expected = CantBuildReferenceException.class)
	public void failThreeContextsTwoModelsBecauseDependency2() {
		TestApiContext childContext = rootContext.childContext("CHILD", User.T::equals); // this context supports User type 
		TestApiContext grandchildContext = childContext.childContext("GRANDCHILD"); // this context supports User type as well		// three contexts; two levels; only root context does not support user
		
		schemaRef(grandchildContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, User.T);
			
			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);
			return comp;
		});
		
		// Only the root context supports the OpenapiComponent type. However it does not support the User type
		// Unfortunately the OpenapiComponent scheme depends on it
		// None of the contexts that support the OpenapiComponent type have a parent context that supports the User type
		// Thus it can't be built
	}

	@Test
	public void succeedThreeContextsTwoModels5() {
		rootContext = TestApiContext.create("ROOT", testComponents, User.T::equals); // redefine root context so that it supports the User type
		TestApiContext childContext = rootContext.childContext("CHILD"); // also middle context supports User type
		TestApiContext grandchildContext = childContext.childContext("GRANDCHILD", AbstractComponentsTest::isOpenapiEntity); // again no User type
		
		// three contexts; only one level; ROOT and MIDDLE context support User
		OpenapiSchema schemaRef = schemaRef(grandchildContext, User.T);
		getSchemaComponent(User.T, rootContext, schemaRef); // So it got resolved with the middle context

	}
	
	@Test
	public void succeedThreeContextsTwoModels6() {
		rootContext = TestApiContext.create("ROOT", testComponents, User.T::equals); // redefine root context so that it supports the User type
		TestApiContext childContext = rootContext.childContext("CHILD"); // also middle context supports User type
		TestApiContext grandchildContext = childContext.childContext("GRANDCHILD", AbstractComponentsTest::isOpenapiEntity); // again no User type

		// three contexts; two levels; ROOT and MIDDLE context support User
		OpenapiSchema schemaRef = schemaRef(grandchildContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, User.T);
			
			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);
			return comp;
		});
		
		// The root context supports the User type so it is resolved by it.
		// Only the grandchild context supports the OpenapiComponents type so it is resolved by it.
		// The User type is not supported by the grandchild context BUT the one from the parent context can be used
		OpenapiSchema schemaComponent = getSchemaComponent(OpenapiComponents.T, grandchildContext, schemaRef);
		OpenapiSchema parameterRef = schemaComponent.getItems();
		OpenapiSchema childComponent = getSchemaComponent(User.T, rootContext, parameterRef);
	}
}
