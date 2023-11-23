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
package com.braintribe.codec.marshaller.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.html.model.HtmlEntityValue;
import com.braintribe.codec.marshaller.html.model.HtmlLinkValue;
import com.braintribe.codec.marshaller.html.model.HtmlListValue;
import com.braintribe.codec.marshaller.html.model.HtmlMapValue;
import com.braintribe.codec.marshaller.html.model.HtmlValue;
import com.braintribe.codec.marshaller.html.model.RenderType;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;

public class HtmlMarshaller implements CharacterMarshaller {
	FreemarkerRenderer freemarkerRenderer;

	public HtmlMarshaller() {
		freemarkerRenderer = new FreemarkerRenderer(null);
	}

	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		marshall(out, value, GmSerializationOptions.deriveDefaults().build());
	}

	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		try {
			Writer writer = new OutputStreamWriter(out, "UTF-8");
			marshall(writer, value, options);
			writer.flush();

		} catch (IOException e) {
			throw new UncheckedIOException("Error while marshalling", e);
		}

	}

	@Override
	public void marshall(Writer writer, Object value, GmSerializationOptions options) throws MarshallException {
		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put("toString", OneArgumentMethod.of(Object::toString));
		dataModel.put("size", OneArgumentMethod.<List<?>> of(List::size));
		dataModel.put("type", OneArgumentMethod.<HtmlValue> of(t -> t.getObjectTypeCode().name()));

		HtmlMarshallerFreemarkerSupport support = new HtmlMarshallerFreemarkerSupport();
		dataModel.put("support", support);

		HtmlValue htmlValue = createHtmlValue(value, new HtmlValueContext(options).expectingType(GMF.getTypeReflection().getType(value)));

		dataModel.put("value", htmlValue);

		freemarkerRenderer = new FreemarkerRenderer(null);
		freemarkerRenderer.writeFromTemplate("html-marshaller-result.ftlh", dataModel, writer);
	}

	private HtmlValue createHtmlValue(Object value, HtmlValueContext context) {
		HtmlValue htmlValue = null;

		HtmlValue visitedObject = context.getVisitedObject(value);
		if (visitedObject != null) {
			if (!visitedObject.linkable())
				return visitedObject;

			HtmlLinkValue linkValue = HtmlLinkValue.T.create();
			linkValue.setLinked(visitedObject);
			htmlValue = linkValue;
			HtmlValueContext.enrichHtmlValue(value, htmlValue, context);
			return htmlValue;
		}

		if (value instanceof Map) {
			HtmlMapValue mapValue = context.registerObject(value, HtmlMapValue.T);

			GenericModelType expectedType = context.getCurrentlyExpectedType();
			GenericModelType keyType = BaseType.INSTANCE;
			GenericModelType valueType = BaseType.INSTANCE;

			if (expectedType.isCollection()) {
				MapType mapType = (MapType) expectedType;
				keyType = mapType.getKeyType();
				valueType = mapType.getValueType();
			}

			final HtmlValueContext keyContext = context.expectingType(keyType);
			final HtmlValueContext valueContext = context.expectingType(valueType);
			;

			Map<Object, Object> map = (Map<Object, Object>) value;
			map.forEach((k, v) -> mapValue.getEntries().put(createHtmlValue(k, keyContext), createHtmlValue(v, valueContext)));

			mapValue.setKeyTypeSignature(keyType.getTypeSignature());
			mapValue.setValueTypeSignature(valueType.getTypeSignature());

			htmlValue = mapValue;
		} else if (value instanceof List<?> || value instanceof Set<?>) {
			HtmlListValue listValue = context.registerObject(value, HtmlListValue.T);
			Collection<Object> collection = (Collection<Object>) value;

			final HtmlValueContext elementContext;
			GenericModelType expectedType = context.getCurrentlyExpectedType();
			GenericModelType elementType;

			if (expectedType.isCollection()) {
				elementType = ((LinearCollectionType) expectedType).getCollectionElementType();
			} else {
				elementType = BaseType.INSTANCE;
			}

			elementContext = context.expectingType(elementType);

			List<HtmlValue> elements = collection.stream().map(e -> createHtmlValue(e, elementContext)).collect(Collectors.toList());
			listValue.setElements(elements);
			listValue.setElementTypeSignature(elementType.getTypeSignature());

			if (value instanceof List<?>)
				listValue.setRenderType(RenderType.list);
			else
				listValue.setRenderType(RenderType.set);

			htmlValue = listValue;
		} else if (value instanceof GenericEntity) {

			HtmlEntityValue entityValue = context.registerObject(value, HtmlEntityValue.T);
			GenericEntity entity = (GenericEntity) value;
			for (Property p : entity.entityType().getProperties()) {
				Object actualValue = p.get(entity);
				HtmlValue propertyValue = createHtmlValue(actualValue, context.expectingType(p.getType()));
				if (writeProperty(context.getOptions(), actualValue, p.getType()))
					entityValue.getProperties().put(p.getName(), propertyValue);
			}
			htmlValue = entityValue;
		} else {
			htmlValue = context.registerObject(value, HtmlValue.T);
		}

		return htmlValue;
	}

	private boolean writeProperty(GmSerializationOptions options, Object value, GenericModelType valueType) {
		if (options.writeEmptyProperties())
			return true;

		if (value == null)
			return false;

		if (valueType.isCollection()) {
			if (value instanceof Map<?, ?>) {
				return !((Map<?, ?>) value).isEmpty();
			}
			if (value instanceof Collection<?>)
				return !((Collection<?>) value).isEmpty();
		}

		return true;
	}

	// Unsupported
	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		throw new UnsupportedOperationException("Unmarshalling is not supported");
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		throw new UnsupportedOperationException("Unmarshalling is not supported");
	}

	@Override
	public Object unmarshall(Reader reader, GmDeserializationOptions options) throws MarshallException {
		throw new UnsupportedOperationException("Unmarshalling is not supported");
	}

	private static class DataModel {
		private Object value;

		public DataModel(Object value) {
			this.value = value;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
	}

}
