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
package com.braintribe.model.processing.vde.impl.bvd.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Test;

import com.braintribe.model.bvd.context.ModelPath;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.processing.vde.evaluator.api.aspects.RootModelPathAspect;
import com.braintribe.model.processing.vde.evaluator.api.aspects.SelectedModelPathsAspect;
import com.braintribe.model.processing.vde.impl.VDGenerator;
import com.braintribe.model.processing.vde.test.VdeTest;
import com.braintribe.model.user.User;

public class ModelPathVdeTest extends VdeTest {

	public static VDGenerator $ = new VDGenerator();

	@Test
	public void testModelPathFirstElement() throws Exception {
		ModelPath modelPathVd = $.modelPathFirstElement();

		User u1 = user("Foo1");
		User u2 = user("Foo2");

		Object result = evaluateWith(
				RootModelPathAspect.class, buildModelPath(u1,u2), modelPathVd);
		
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(User.class);
		assertThat(result).isEqualTo(u1);
	}
	
	@Test
	public void testModelPathLastElement() throws Exception {
		ModelPath modelPathVd = $.modelPathLastElement();

		User u1 = user("Foo1");
		User u2 = user("Foo2");

		Object result = evaluateWith(
				RootModelPathAspect.class, buildModelPath(u1,u2), modelPathVd);
		
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(User.class);
		assertThat(result).isEqualTo(u2);
	}
	
	@Test
	public void testModelPathFirstOffsetElement() throws Exception {
		ModelPath modelPathVd = $.modelPathFirstElementOffset(1);

		User u1 = user("Foo1");
		User u2 = user("Foo2");

		Object result = evaluateWith(
				RootModelPathAspect.class, buildModelPath(u1,u2), modelPathVd);
		
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(User.class);
		assertThat(result).isEqualTo(u2);
	}

	@Test
	public void testModelPathLastOffsetElement() throws Exception {
		ModelPath modelPathVd = $.modelPathLastElementOffset(1);

		User u1 = user("Foo1");
		User u2 = user("Foo2");

		Object result = evaluateWith(
				RootModelPathAspect.class, buildModelPath(u1,u2), modelPathVd);
		
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(User.class);
		assertThat(result).isEqualTo(u1);
	}

	@Test
	public void testModelPathFirstElementSelection() throws Exception {
		ModelPath modelPathVd = $.modelPathFirstElementForSelection();

		User u1 = user("Foo1");
		User u2 = user("Foo2");


		Object result = evaluateWith(
				SelectedModelPathsAspect.class, Collections.singletonList(buildModelPath(u1,u2)), modelPathVd);
		
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(User.class);
		assertThat(result).isEqualTo(u1);
	}

	
	@Test
	public void testModelPathLastElementSelection() throws Exception {
		ModelPath modelPathVd = $.modelPathLastElementForSelection();

		User u1 = user("Foo1");
		User u2 = user("Foo2");


		Object result = evaluateWith(
				SelectedModelPathsAspect.class, Collections.singletonList(buildModelPath(u1,u2)), modelPathVd);
		
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(User.class);
		assertThat(result).isEqualTo(u2);
	}
	
	private com.braintribe.model.generic.path.ModelPath buildModelPath(GenericEntity... arr) {
		com.braintribe.model.generic.path.ModelPath modelPath = new com.braintribe.model.generic.path.ModelPath();
		for (GenericEntity e : arr) {
			ModelPathElement element = new RootPathElement(e);
			modelPath.add(element);
		}
		return modelPath;
	}

	private User user(String name) {
		User u = User.T.create();
		u.setName(name);
		return u;
	}

}
