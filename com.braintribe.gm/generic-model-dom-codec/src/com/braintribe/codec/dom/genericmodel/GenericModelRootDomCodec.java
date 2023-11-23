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
package com.braintribe.codec.dom.genericmodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.context.CodingContext;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.utils.xml.XmlTools;

public class GenericModelRootDomCodec<T> implements GmCodec<T, Document> {
	protected GenericModelDomCodecRegistry codecRegistry;
	private GenericModelType type;
	protected GenericModelTypeReflection genericModelTypeReflection = GMF.getTypeReflection();
	private static DocumentBuilder documentBuilder = null;
	private Consumer<Set<String>> requiredTypesReceiver;
	private com.braintribe.codec.marshaller.api.DecodingLenience decodingLenience;
	private boolean assignAbsenceInformationForMissingProperties = false;

	public GenericModelRootDomCodec() {
		// TODO: should the codec also be type and enum constant lenient by default?
		// (so far only property lenience is enabled, because that used to be the default)
		decodingLenience = new com.braintribe.codec.marshaller.api.DecodingLenience();
		decodingLenience.setPropertyLenient(true);
		decodingLenience.setEnumConstantLenient(true);
	}

	protected static DocumentBuilder getDocumentBuilder() throws CodecException {
		if (documentBuilder == null) {
			try {
				documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new CodecException("error while retrieving DocumentBuilder", e);
			}
		}

		return documentBuilder;
	}

	@Configurable
	public void setAssignAbsenceInformationForMissingProperties(boolean assignAbsenceInformationForMissingProperties) {
		this.assignAbsenceInformationForMissingProperties = assignAbsenceInformationForMissingProperties;
	}

	@Configurable
	public void setRequiredTypesReceiver(Consumer<Set<String>> requiredTypesReceiver) {
		this.requiredTypesReceiver = requiredTypesReceiver;
	}

	@Configurable
	public void setGenericModelTypeReflection(GenericModelTypeReflection genericModelTypeReflection) {
		this.genericModelTypeReflection = genericModelTypeReflection;
	}

	@Configurable
	public void setType(GenericModelType type) {
		this.type = type;
	}

	@Configurable
	public void setCodecRegistry(GenericModelDomCodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	public GenericModelDomCodecRegistry getCodecRegistry() {
		if (codecRegistry == null) {
			codecRegistry = GenericModelDomCodecRegistry.getDefaultInstance();
		}

		return codecRegistry;
	}

	public GenericModelType getType() {
		if (type == null) {
			type = genericModelTypeReflection.getBaseType();
		}

		return type;
	}

	protected void decodeRequiredTypes(Element gmDataElement) throws CodecException {
		if (this.requiredTypesReceiver == null) {
			return;
		}

		Set<String> typeSignatures = new HashSet<>();

		Element requiredTypesElement = XmlTools.getElementByPath(gmDataElement, "required-types");
		if (requiredTypesElement != null) {
			typeSignatures = new HashSet<>();
			Node child = requiredTypesElement.getFirstChild();

			while (child != null) {
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					Element childElement = (Element) child;
					if (childElement.getTagName().equals("type")) {
						String typeSignature = childElement.getTextContent();
						typeSignatures.add(typeSignature);
					}
				}

				child = child.getNextSibling();
			}
		} else {
			NodeList entityElements = gmDataElement.getElementsByTagName("entity");

			for (int i = 0; i < entityElements.getLength(); i++) {
				Element element = (Element) entityElements.item(i);
				String typeName = element.getAttribute("type");

				if (typeName != null && typeName.length() > 0) {
					typeSignatures.add(typeName);
				}
			}
		}

		if (!typeSignatures.isEmpty()) {
			try {
				this.requiredTypesReceiver.accept(typeSignatures);
			} catch (Exception e) {
				throw new CodecException("Error while propagating required types to the configured receiver!", e);
			}
		}
	}

	@Override
	public T decode(Document document) throws CodecException {
		//@formatter:off
		return decode(document, 
				GmDeserializationOptions
					.deriveDefaults()
						.absentifyMissingProperties(assignAbsenceInformationForMissingProperties)
						.setDecodingLenience(decodingLenience)
					.build()
					);
		//@formatter:on
	}

	protected int getGmXmlVersion(Document document) throws CodecException {
		Node node = document.getFirstChild();

		while (node != null) {
			if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
				ProcessingInstruction pi = (ProcessingInstruction) node;
				String target = pi.getTarget();

				if (target.equals("gm-xml")) {
					String rawDdata = pi.getData();
					Map<String, String> data = ProcessingInstructionParser.parseData(rawDdata);

					String encodedVersion = data.get("version");

					if (encodedVersion != null) {
						try {
							int version = Integer.parseInt(encodedVersion);
							return version;
						} catch (NumberFormatException e) {
							throw new CodecException("error while decoding gm-xml version", e);
						}
					}
				}
			}
			node = node.getNextSibling();
		}

		return 1;
	}

	@Override
	public Document encode(T value) throws CodecException {
		return encode(value, GmSerializationOptions.deriveDefaults().build());
	}

	@Override
	public Document encode(T value, GmSerializationOptions options) throws CodecException {
		try {
			EncodingContext ctx = new EncodingContext();
			Document document = getDocumentBuilder().newDocument();
			ctx.setDocument(document);
			ctx.setVersion(GmXml.defaultVersion);
			CodingContext.push(ctx);

			ProcessingInstruction gmXmlPi = document.createProcessingInstruction("gm-xml", "version=\"" + ctx.getVersion() + "\"");
			document.appendChild(gmXmlPi);

			Node rootValueNode = document;
			Element requiredTypesElement = null;

			if (ctx.getVersion() > 2) {
				Element gmDataElement = document.createElement("gm-data");
				requiredTypesElement = document.createElement("required-types");
				Element rootValueElement = document.createElement("root-value");
				Element poolElement = document.createElement("pool");

				gmDataElement.appendChild(requiredTypesElement);
				gmDataElement.appendChild(rootValueElement);
				gmDataElement.appendChild(poolElement);

				rootValueNode = rootValueElement;

				document.appendChild(gmDataElement);

				ctx.setPoolElement(poolElement);
			}

			GenericModelType actualType = getType().getActualType(value);
			GenericModelDomCodecRegistry registry = getCodecRegistry();
			Codec<T, Element> codec = registry.getCodec(actualType);
			Element element = codec.encode(value);
			rootValueNode.appendChild(element);

			if (requiredTypesElement != null) {
				for (GenericModelType requiredType : ctx.getRequiredTypes()) {
					Element typeElement = document.createElement("type");
					Text text = document.createTextNode(requiredType.getTypeSignature());
					typeElement.appendChild(text);
					requiredTypesElement.appendChild(typeElement);
				}
			}

			return document;
		} finally {
			EncodingContext.pop();
		}
	}

	@Override
	public T decode(Document document, GmDeserializationOptions options) throws CodecException {
		int version = getGmXmlVersion(document);
		DecodingContext ctx = new DecodingContext(options);
		ctx.setAssignAbsenceInformationForMissingProperties(options.getAbsentifyMissingProperties());
		ctx.setLenience(options.getDecodingLenience());
		ctx.setVersion(version);
		CodingContext.push(ctx);
		try {
			Element rootValueElement = document.getDocumentElement();

			if (ctx.getVersion() > 2) {
				Element gmDataElement = document.getDocumentElement();
				decodeRequiredTypes(gmDataElement);

				Element rootElement = XmlTools.getElementByPath(gmDataElement, "root-value");
				if (rootElement == null)
					throw new CodecException("missing /root-value element");
				rootValueElement = XmlTools.getFirstElement(rootElement, null);

				if (rootValueElement == null)
					throw new CodecException("missing a gm value element below the /root-value element");

				Element poolElement = XmlTools.getElementByPath(gmDataElement, "pool");

				if (poolElement == null)
					throw new CodecException("missing /pool element");

				// scan pool
				Map<String, Element> pool = new HashMap<>();
				Node childNode = poolElement.getFirstChild();

				while (childNode != null) {
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						Element pooledElement = (Element) childNode;
						String id = pooledElement.getAttribute("id");
						pool.put(id, pooledElement);
					}
					childNode = childNode.getNextSibling();
				}

				ctx.setPool(pool);
			}
			@SuppressWarnings("unchecked")
			T result = (T) getCodecRegistry().getCodec(getType()).decode(rootValueElement);
			return result;
		} finally {
			DecodingContext.pop();
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<T> getValueClass() {
		return (Class) getType().getJavaType();
	}

	public com.braintribe.codec.marshaller.api.DecodingLenience getDecodingLenience() {
		return this.decodingLenience;
	}

	public void setDecodingLenience(com.braintribe.codec.marshaller.api.DecodingLenience decodingLenience) {
		this.decodingLenience = decodingLenience;
	}

}
