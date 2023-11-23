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

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newTreeMap;
import static java.util.Collections.emptyMap;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GenericEntity;

/**
 * @author peter.gazdik
 */
public class SingleAnnotationDescriptor extends AnnotationDescriptor {

	private Map<String, Object> annotationValues;

	public SingleAnnotationDescriptor(Class<? extends Annotation> annotationClass) {
		super(annotationClass);
	}

	public void addAnnotationValue(String name, Object value) {
		if (annotationValues == null)
			annotationValues = newTreeMap();

		annotationValues.put(name, value);
	}

	public Map<String, Object> getAnnotationValues() {
		return annotationValues != null ? annotationValues : emptyMap();
	}

	public void setGlobalId(String globalId) {
		addAnnotationValue(GenericEntity.globalId, globalId);
	}

	@Override
	public void withSourceCode(Consumer<String> sourceCodeConsumer) {
		sourceCodeConsumer.accept(toSourceCode());
	}

	private String toSourceCode() {
		String simpleName = annotationClass.getSimpleName();

		if (isEmpty(annotationValues))
			return simpleName;

		List<Entry<String, ?>> entries = annotationValues.entrySet().stream() //
				.filter(e -> e.getValue() != null) //
				.collect(Collectors.toList());

		if (entries.isEmpty())
			return simpleName;

		StringBuilder sb = new StringBuilder(simpleName);
		sb.append('(');

		boolean first = true;
		for (Entry<String, ?> entry : entries) {
			if (first)
				first = false;
			else
				sb.append(", ");

			appendElementWithValue(sb, entry);

		}

		sb.append(')');

		return sb.toString();
	}

	private void appendElementWithValue(StringBuilder sb, Entry<String, ?> entry) {
		String elementName = entry.getKey();
		Object value = entry.getValue();

		sb.append(elementName);
		sb.append('=');

		appendValue(sb, value);
	}

	private void appendValue(StringBuilder sb, Object value) {
		if (value != null && value.getClass().isArray()) {
			sb.append('{');
			boolean first = true;
			for (Object o : (Object[]) value) {
				if (first)
					first = false;
				else
					sb.append(", ");
				appendValue(sb, o);
			}

			sb.append('}');

		} else if (value instanceof String) {
			sb.append('"');
			sb.append(value);
			sb.append('"');

		} else if (value instanceof ClassReference) {
			ClassReference castedValue = (ClassReference) value;
			sb.append(castedValue.className);
			sb.append(".class");

		} else {
			sb.append(value);
		}
	}

}
