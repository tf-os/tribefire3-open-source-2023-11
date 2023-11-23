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
package com.braintribe.model.generic.annotation.meta.api.synthesis;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author peter.gazdik
 */
public class RepeatedAnnotationDescriptor extends AnnotationDescriptor {

	private final List<SingleAnnotationDescriptor> nestedAnnotations;

	public RepeatedAnnotationDescriptor(Class<? extends Annotation> annotationClass, List<SingleAnnotationDescriptor> nestedAnnotations) {
		super(annotationClass);
		this.nestedAnnotations = nestedAnnotations;
	}

	public List<SingleAnnotationDescriptor> getNestedAnnotations() {
		return nestedAnnotations;
	}

	@Override
	public void withSourceCode(Consumer<String> sourceCodeConsumer) {
		for (SingleAnnotationDescriptor nestedAnnotation : nestedAnnotations)
			nestedAnnotation.withSourceCode(sourceCodeConsumer);
	}

}
