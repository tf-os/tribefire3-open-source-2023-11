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
package com.braintribe.codec.marshaller.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.dom.coder.DeferredDecoder;
import com.braintribe.codec.marshaller.dom.coder.DeferredEncoder;
import com.braintribe.codec.marshaller.dom.coder.DomCoders;
import com.braintribe.codec.marshaller.dom.coder.entity.EntityDomCodingPreparation;
import com.braintribe.codec.marshaller.dom.coder.entity.EntityDomDeferredDecoder;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.TypeCode;

public class GmDomCodec<T> implements GmCodec<T, Document> {
	private final GenericModelType type;
	private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	private final EntityDomCodingPreparations entityDomCodingPreparations = new EntityDomCodingPreparations();
	private boolean shouldWriteAbsenceInformation = true;
	private Consumer<Set<String>> requiredTypesReceiver;

	public GmDomCodec(Class<T> valueClass) {
		this.type = GMF.getTypeReflection().getType(valueClass);
	}

	public GmDomCodec(GenericModelType type) {
		this.type = type;
	}

	public GmDomCodec() {
		type = BaseType.INSTANCE;
	}

	@Configurable
	public void setShouldWriteAbsenceInformation(boolean shouldWriteAbsenceInformation) {
		this.shouldWriteAbsenceInformation = shouldWriteAbsenceInformation;
	}

	@Override
	public T decode(Document encodedValue) throws CodecException {
		return decode(encodedValue, GmDeserializationOptions.deriveDefaults().build());
	}

	@Override
	public Document encode(T value) throws CodecException {
		return encode(value, GmSerializationOptions.deriveDefaults().build());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getValueClass() {
		return (Class<T>) type.getJavaType();
	}

	@Override
	@SuppressWarnings("unchecked")
	public T decode(Document encodedValue, GmDeserializationOptions options) throws CodecException {
		DomDecodingContextImpl context = new DomDecodingContextImpl(encodedValue, options, entityDomCodingPreparations);
		return (T) decodeGmData(context, encodedValue.getDocumentElement());
	}

	@Override
	public Document encode(T value, GmSerializationOptions options) throws CodecException {
		try {
			Document document = documentBuilderFactory.newDocumentBuilder().newDocument();

			ProcessingInstruction gmXmlPi = document.createProcessingInstruction("gm-xml", "version=\"4\"");
			document.appendChild(gmXmlPi);

			DomEncodingContextImpl context = new DomEncodingContextImpl(document, entityDomCodingPreparations, options);
			context.setWriteAbsenceInformation(shouldWriteAbsenceInformation);
			document.appendChild(encodeGmData(context, value));

			return document;
		} catch (ParserConfigurationException e) {
			throw new CodecException("error while creating new empty document", e);
		}
	}

	private Object decodeGmData(DomDecodingContext context, Element element) throws CodecException {
		Node node = element.getFirstChild();

		Object rootValue = null;

		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;

				switch (childElement.getTagName()) {
					case "required-types":
						decodeRequiredTypes(context, childElement);
						break;
					case "root-value":
						rootValue = decodeRootValue(context, childElement);
						break;
					case "pool":
						decodePool(context, childElement);
						break;
				}
			}
			node = node.getNextSibling();
		}

		runDeferredDecoders(context);

		return rootValue;
	}

	private static Element encodeGmData(DomEncodingContext context, Object value) throws CodecException {
		Element rootValueElement = encodeRootValue(context, value);

		Element poolElement = context.getDocument().createElement("pool");
		context.setPool(poolElement);

		runDeferredEncoders(context);

		Element requiredTypesElement = encodeRequiredTypes(context);

		Element gmDataElement = context.getDocument().createElement("gm-data");

		gmDataElement.appendChild(requiredTypesElement);
		gmDataElement.appendChild(rootValueElement);
		gmDataElement.appendChild(poolElement);
		return gmDataElement;
	}

	private void decodeRequiredTypes(DomDecodingContext context, Element element) throws CodecException {
		Node node = element.getFirstChild();
		GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;

				String typeSignature = childElement.getTextContent();
				GenericModelType _type = typeReflection.getType(typeSignature);

				TypeInfo4Read typeInfo = new TypeInfo4Read();
				typeInfo.type = _type;
				typeInfo.as = childElement.getAttribute("as");
				typeInfo.alias = childElement.getAttribute("alias");

				if (_type.getTypeCode() == TypeCode.entityType) {
					EntityType<?> entityType = (EntityType<?>) _type;
					typeInfo.preparation = context.getEntityDomCodingPreparation(entityType);
					typeInfo.setCount(Integer.parseInt(childElement.getAttribute("num")));
				}

				context.registerTypeInfo(typeInfo);
			}

			node = node.getNextSibling();
		}

		if (requiredTypesReceiver != null) {
			try {
				requiredTypesReceiver.accept(context.getRequiredTypes());
			} catch (Exception e) {
				throw new CodecException("error while informing required types receiver", e);
			}
		}
	}

	private static Element encodeRequiredTypes(DomEncodingContext context) {
		Element element = context.getDocument().createElement("required-types");

		List<TypeInfo> typeInfos = new ArrayList<>(context.getRequiredTypeInfos());
		Collections.sort(typeInfos);

		for (TypeInfo typeInfo : typeInfos) {
			Element typeElement = context.getDocument().createElement("t");
			typeElement.setAttribute("as", typeInfo.as);
			typeElement.setAttribute("alias", typeInfo.alias);

			if (typeInfo.type.getTypeCode() == TypeCode.entityType)
				typeElement.setAttribute("num", Integer.toString(typeInfo.getCount()));
			typeElement.setTextContent(typeInfo.type.getTypeSignature());

			element.appendChild(typeElement);
		}

		return element;
	}

	private static Object decodeRootValue(DomDecodingContext context, Element element) throws CodecException {
		Node node = element.getFirstChild();

		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				return DomCoders.objectCoder.decode(context, childElement);
			}
			node = node.getNextSibling();
		}

		throw new CodecException("missing root value");
	}

	private static Element encodeRootValue(DomEncodingContext context, Object value) throws CodecException {
		Element element = context.getDocument().createElement("root-value");
		Element valueElement = DomCoders.objectCoder.encode(context, value);
		element.appendChild(valueElement);
		return element;
	}

	private static void decodePool(DomDecodingContext context, final Element element) throws CodecException {

		final int maxDeferred = context.getMaxDeferred();

		if (maxDeferred != -1) {
			context.appendDeferredDecoder(new DeferredDecoder() {
				private Node node = element.getFirstChild();

				@Override
				public boolean continueDecode(DomDecodingContext _context) throws CodecException {
					node = decodePool(_context, node, maxDeferred);
					return node != null;
				}
			});
		} else {
			Node node = element.getFirstChild();
			decodePool(context, node, -1);
		}
	}

	private static Node decodePool(DomDecodingContext context, Node node, int maxDecode) throws CodecException {
		int i = 0;
		while (node != null) {
			if (maxDecode != -1 && i == maxDecode)
				break;

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;

				String id = childElement.getAttribute("id");
				EntityRegistration entityRegistration = context.acquireEntity(id);

				TypeInfo4Read typeInfo = entityRegistration.typeInfo;

				EntityType<?> entityType = (EntityType<?>) typeInfo.type;
				EntityDomCodingPreparation preparation = typeInfo.preparation;

				EntityDomDeferredDecoder decoder = new EntityDomDeferredDecoder(entityType, preparation, childElement);

				context.appendDeferredDecoder(decoder);
			}
			node = node.getNextSibling();
		}

		return node;
	}

	private static void runDeferredEncoders(DomEncodingContext context) throws CodecException {
		DeferredEncoder deferredEncoder = context.getFirstDeferredEncoder();

		while (deferredEncoder != null) {
			while (deferredEncoder.continueEncode(context)) {
				/* Nothing to do here */ }
			deferredEncoder = deferredEncoder.next;
		}
	}

	private static void runDeferredDecoders(DomDecodingContext context) throws CodecException {
		DeferredDecoder deferredDecoder = context.getFirstDeferredDecoder();

		while (deferredDecoder != null) {
			while (deferredDecoder.continueDecode(context)) {
				/* Nothing to do here */ }
			deferredDecoder = deferredDecoder.next;
		}
	}

	public void setRequiredTypesReceiver(Consumer<Set<String>> requiredTypesReceiver) {
		this.requiredTypesReceiver = requiredTypesReceiver;
	}
}
