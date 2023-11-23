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
package com.braintribe.gwt.genericmodel.client.codec.dom4;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.codec.CodecException;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.genericmodel.client.codec.api.DefaultDecodingContext;
import com.braintribe.gwt.genericmodel.client.codec.api.GmDecodingContext;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.proxy.ProxyContext;
import com.braintribe.model.generic.proxy.ProxyValue;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.model.generic.reflection.TypeCode;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;

public class DomDecodingContextImpl extends AbstractCodingContext implements DomDecodingContext, GmCodecConstants {
	private static final GenericModelTypeReflection TYPE_REFLECTION = GMF.getTypeReflection();
	private final Document document;
	private final Map<String, TypeInfo> typeInfoByAlias = new HashMap<>();
	private final Map<String, TypeInfo> typeInfoByKey = new HashMap<>();
	private final Map<String, EntityRegistration> entitiesById = new HashMap<>();
	private final AbsenceInformation absenceInformationForMissingProperties = GMF.absenceInformation();
	private int maxDeferred = -1;
	private final GmDecodingContext gmDecodingContext;
	private ProxyContext proxyContext;

	public DomDecodingContextImpl(Document document, GmDecodingContext gmDecodingContext) {
		this.document = document;
		this.gmDecodingContext = gmDecodingContext;
		this.proxyContext = gmDecodingContext.getProxyContext();
	}
	
	public DomDecodingContextImpl(Document document) {
		this.document = document;
		this.gmDecodingContext = new DefaultDecodingContext();
	}
	
	public void setMaxDeferred(int maxDeferred) {
		this.maxDeferred = maxDeferred;
	}
	
	@Override
	public ProxyContext getProxyContext() {
		return proxyContext;
	}
	
	@Override
	public int getMaxDeferred() {
		return maxDeferred;
	}
	
	@Override
	public Document getDocument() {
		return document;
	}
	
	@Override
	public void registerTypeInfo(TypeInfo typeInfo) {
		typeInfoByAlias.put(typeInfo.alias, typeInfo);
		typeInfoByKey.put(typeInfo.as, typeInfo);
	}
	
	@Override
	public TypeInfo getTypeInfoByKey(String key) {
		return typeInfoByKey.get(key);
	}

	@Override
	public EntityRegistration acquireEntity(String ref) throws CodecException {
		EntityRegistration registration = entitiesById.get(ref);
		
		if (registration == null) {
			int index = ref.indexOf('-');
			String alias = ref.substring(0, index);
			TypeInfo typeInfo = typeInfoByAlias.get(alias);
			
			if (typeInfo == null)
				throw new CodecException("invalid ref id " + ref);
			
			EntityType<?> entityType = (EntityType<?>)typeInfo.type;
			GenericEntity entity = gmDecodingContext.create(entityType);
			registration = new EntityRegistration();
			registration.entity = entity;
			registration.typeInfo = typeInfo;
			entitiesById.put(ref, registration);
		}
		
		return registration;
	}

	@Override
	public AbsenceInformation getAbsenceInformationForMissingProperties() {
		return absenceInformationForMissingProperties;
	}
	
	@Override
	public Set<String> getRequiredTypes() {
		Set<String> requiredTypes = new HashSet<>();
		for (TypeInfo typeInfo: typeInfoByAlias.values()) {
			requiredTypes.add(typeInfo.type.getTypeSignature());
		}
		return requiredTypes;
	}
	
	@Override
	public Object decodeValue(Element element) throws CodecException {
		String tagName = element.getTagName();
		int c = tagName.charAt(0);
		
		try {
			switch (c) {
			// simple values
			case 'b': return new Boolean(getTextContent(element));
			case 's': return getTextContent(element);
			case 'i': return new Integer(getTextContent(element));
			case 'l': return new Long(getTextContent(element));
			case 'f': return new Float(getTextContent(element));
			case 'd': return new Double(getTextContent(element));
			case 'D': return new BigDecimal(getTextContent(element));
			case 'T': return dateFormat.parseStrict(getTextContent(element));
			
			// collections
			case 'L': return decodeList(element, new ArrayList<Object>());
			case 'S': return decodeSet(element, new HashSet<Object>());
			case 'M': return decodeMap(element);
			
			// null
			case 'n': return null;
			
			// custom types
			case 'e': return decodeEnum(element);
			case 'r': return acquireEntity(getTextContent(element)).entity;
			
			default: 
				throw new CodecException("Unsupported element type "+tagName);
			}
		} catch (Exception e) {
			throw new CodecException("error while decoding value", e);
		}
	}
	
	private Object decodeEnum(Element element) {
		String text = getTextContent(element);
		int index = text.lastIndexOf('.');
		
		String typeKey = text.substring(0, index); 
		ScalarType enumType = (ScalarType) getTypeInfoByKey(typeKey).type;
		
		String constantName = text.substring(index + 1);
		return enumType.instanceFromString(constantName);
	}
	
	private Collection<Object> decodeSet(final Element element, final Set<Object> collection) throws CodecException {
		final int maxDeferred = getMaxDeferred();
		
		if (maxDeferred != -1) {
			appendDeferredProcessor(new DeferredProcessor() {
				private Node node = element.getFirstChild();
				@Override
				public boolean continueProcessing() throws CodecException {
					node = decodeSet(collection, node, maxDeferred);
					return node != null;
				}
			});
		}
		else {
			Node node = element.getFirstChild();
			decodeSet(collection, node, -1);
		}
		
		return collection;

	}
	
	private Node decodeSet(Set<Object> collection, Node node, int maxDecode) throws CodecException {
		int i = 0;
		boolean proxyAware = proxyContext != null;
		while (node != null) {
			
			if (maxDecode != -1 && i == maxDecode) {
				return node;
			}
			
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element)node;
				
				Object e = decodeValue(childElement);
				
				if (proxyAware) {
					if (e instanceof ProxyValue) {
						proxyContext.deferCollectionAdd(collection, (ProxyValue)e);
					}
					else {
						collection.add(e);
					}
				}
				else {
					collection.add(e);
				}
				
				i++;
			}
			
			node = node.getNextSibling();
		}
		
		return null;
	}

	private Collection<Object> decodeList(final Element element, final List<Object> collection) throws CodecException {
		final int maxDeferred = getMaxDeferred();
		
		if (maxDeferred != -1) {
			appendDeferredProcessor(new DeferredProcessor() {
				private Node node = element.getFirstChild();
				@Override
				public boolean continueProcessing() throws CodecException {
					node = decodeList(collection, node, maxDeferred);
					return node != null;
				}
			});
		}
		else {
			Node node = element.getFirstChild();
			decodeList(collection, node, -1);
		}
		
		return collection;
		
	}
	
	private Node decodeList(List<Object> collection, Node node, int maxDecode) throws CodecException {
		int i = 0;
		int offset = collection.size();
		boolean proxyAware = proxyContext != null;
		while (node != null) {
			
			if (maxDecode != -1 && i == maxDecode) {
				return node;
			}
			
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element)node;
				
				Object e = decodeValue(childElement);

				if (proxyAware) {
					if (e instanceof ProxyValue) {
						proxyContext.deferListInsert(collection, (ProxyValue)e, i + offset);
						collection.add(null);
					}
					else {
						collection.add(e);
					}
				}
				else {
					collection.add(e);
				}
				
				i++;
			}
			
			node = node.getNextSibling();
		}
		
		return null;
	}
	
	
	private Map<Object, Object> decodeMap(final Element element) throws CodecException {
		final Map<Object, Object> map = new HashMap<Object, Object>();
		final int maxDeferred = getMaxDeferred();

		if (maxDeferred != -1) {
			appendDeferredProcessor(new DeferredProcessor() {
				private Node node = element.getFirstChild();
				@Override
				public boolean continueProcessing() throws CodecException {
					node = decodeMap(map, node, maxDeferred);
					return node != null;
				}
			});
		}
		else {
			Node node = element.getFirstChild();
			decodeMap(map, node, -1);
		}

		return map;
	}
	
	private Node decodeMap(Map<Object, Object> map, Node node, int maxDecode) throws CodecException {
		int i = 0;
		boolean proxyAware = proxyContext != null;
		while (node != null) {
			
			if (maxDecode != -1 && i == maxDecode) {
				return node;
			}
			
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element entryElement = (Element)node;
				
				Element []childElements = new Element[2];
				
				Node childNode = entryElement.getFirstChild();
				
				int n = 0;
				while (childNode != null) {
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						childElements[n++] = (Element)childNode;
						if (n == 2)
							break;
					}
					childNode = childNode.getNextSibling();
				}
				
				if (n != 2)
					throw new CodecException("map entry element is missing child elements");
				
				Object key = decodeValue(childElements[0]);
				Object value = decodeValue(childElements[1]);
				
				if (proxyAware) {
					if (key instanceof ProxyValue) {
						if (value instanceof ProxyValue) {
							proxyContext.deferMapPut(map, (ProxyValue)key, (ProxyValue)value);
						}
						else {
							proxyContext.deferMapPut(map, (ProxyValue)key, value);
						}
					}
					else if (value instanceof ProxyValue) {
						proxyContext.deferMapPut(map, key, (ProxyValue)value);
					}
					else {
						map.put(key, value);
					}
				}
				else {
					map.put(key, value);
				}
				i++;
			}
			
			node = node.getNextSibling();
		}
		
		return null;
	}
	
	public static String getTextContent(Element element) {
		Node node = element.getFirstChild();
		
		String text = "";
		StringBuilder builder = null;
		int state = 0;
		
		while (node != null) {
			short nodeType = node.getNodeType();
			if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
				switch (state) {
				case 0: 
					text = node.getNodeValue();
					state = 1;
					break;
				case 1:
					String moreText = node.getNodeValue();
					builder = new StringBuilder(text.length() + moreText.length());
					builder.append(text);
					builder.append(moreText);
					state = 2;
					break;
				case 2:
					builder.append(node.getNodeValue());
					break;
				}
			}
			node = node.getNextSibling();
		}

		return builder != null? builder.toString(): text;
	}

	private void decodePool(final Element element) throws CodecException {
		
		final int maxDeferred = getMaxDeferred();
		
		if (maxDeferred != -1) {
			appendDeferredProcessor(new DeferredProcessor() {
				private Node node = element.getFirstChild();
				@Override
				public boolean continueProcessing() throws CodecException {
					node = decodePool(node, maxDeferred);
					return node != null;
				}
			});
		}
		else {
			Node node = element.getFirstChild();
			decodePool(node, -1);
		}
	}
	
	private Node decodePool(Node node, int maxDecode) throws CodecException {
		int i = 0;
		while (node != null) {
			if (maxDecode != -1 && i == maxDecode) 
				break;
			
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element)node;
				
				String id = childElement.getAttribute("id");
				EntityRegistration entityRegistration = acquireEntity(id);
				
				TypeInfo typeInfo = entityRegistration.typeInfo;
				
				EntityType<?> entityType = (EntityType<?>) typeInfo.type;
				
				EntityDomDeferredDecoder decoder = new EntityDomDeferredDecoder(this, entityType, childElement);
				
				appendDeferredProcessor(decoder);
			}
			node = node.getNextSibling();
		}
		
		return node;
	}
	
	public Object decodeGmData(Element element) throws CodecException {
		Node node = element.getFirstChild();
		
		Object rootValue = null;
				
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element)node;
				
				switch (childElement.getTagName()) {
				case "required-types":
					decodeRequiredTypes(childElement);
					break;
				case "root-value":
					rootValue = decodeRootValue(childElement);
					break;
				case "pool":
					decodePool(childElement);
					break;
				}
			}
			node = node.getNextSibling();
		}
		
		runDeferredProcessors();
		
		return rootValue;
	}

	private Object decodeRootValue(Element element) throws CodecException {
		Node node = element.getFirstChild();
		
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element)node;
				return decodeValue(childElement);
			}
			node = node.getNextSibling();
		}
		
		throw new CodecException("missing root value");
	}

	private void decodeRequiredTypes(Element element) throws CodecException {
		Node node = element.getFirstChild();
		GenericModelTypeReflection typeReflection = TYPE_REFLECTION;
		
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element)node;
				
				String typeSignature = DomDecodingContextImpl.getTextContent(childElement);
				GenericModelType _type = typeReflection.getType(typeSignature);
				
				TypeInfo typeInfo = new TypeInfo();
				typeInfo.type = _type;
				typeInfo.as = childElement.getAttribute("as");
				typeInfo.alias = childElement.getAttribute("alias");
				
				if (_type.getTypeCode() == TypeCode.entityType) {
					typeInfo.setCount(Integer.parseInt(childElement.getAttribute("num")));
				}
				
				registerTypeInfo(typeInfo);
			}
			
			node = node.getNextSibling();
		}
		
		gmDecodingContext.ensureTypes(getRequiredTypes());
	}
	
	private void decodeRequiredTypesAsync(Element element, AsyncCallback<Void> callback) {
		try {
			Node node = element.getFirstChild();
			GenericModelTypeReflection typeReflection = TYPE_REFLECTION;
			
			while (node != null) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element childElement = (Element)node;
					String typeSignature = DomDecodingContextImpl.getTextContent(childElement);
					String numStr = childElement.getAttribute("num");
					boolean isEntityType = numStr != null && !numStr.isEmpty();

					GenericModelType _type = null;
					
					if (proxyContext != null) {
						_type = typeReflection.findType(typeSignature);
						
						if (_type == null) {
							_type = isEntityType? 
									proxyContext.getProxyEntityType(typeSignature):
									proxyContext.getProxyEnumType(typeSignature);
						}
					}
					else {
						_type = typeReflection.getType(typeSignature);
					}
					
					
					TypeInfo typeInfo = new TypeInfo();
					typeInfo.type = _type;
					typeInfo.as = childElement.getAttribute("as");
					typeInfo.alias = childElement.getAttribute("alias");
					
					
					if (isEntityType) {
						int num = Integer.parseInt(numStr);
						typeInfo.setCount(num);
					}
					
					registerTypeInfo(typeInfo);
				}
				
				node = node.getNextSibling();
			}
		} catch (Exception e) {
			callback.onFailure(new CodecException("error while decoding required types section", e));
			return;
		}
		
		gmDecodingContext.ensureTypes(getRequiredTypes(), callback);
	}

	@Override
	public boolean isPropertyLenient() {
		return true;
	}
	

	private class AsyncGmDataDecoding {
		private final Future<Object> future = new Future<>();
		private Node node;
		private final Element documentElement;
		private Object rootValue;
		
		private void continueIteration() {
			while (node != null) {
				Node currentNode = node;
				node = currentNode.getNextSibling();
				if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
					Element childElement = (Element)currentNode;
					
					switch (childElement.getTagName()) {
					case "required-types":
						decodeRequiredTypesAsync(childElement, AsyncCallbacks.of(v -> continueIteration(), future::onFailure));
						return;
					case "root-value":
						try {
							rootValue = decodeRootValue(childElement);
						} catch (CodecException e) {
							future.onFailure(new CodecException("error while decoding root value", e));
							return;
						}
						break;
					case "pool":
						try {
							decodePool(childElement);
						} catch (CodecException e) {
							future.onFailure(new CodecException("error while decoding pool", e));
							return;
						}
						break;
					}
				}
			}
			
			Object result = rootValue;
			runDeferredProcessorsAsync().andFinally(() -> future.onSuccess(result)).onError(future::onFailure);
		}
		
		public Future<Object> start() {
			node = documentElement.getFirstChild();
			continueIteration();
			return future;
		}
		
		public AsyncGmDataDecoding(Document document) {
			documentElement = document.getDocumentElement();
		}
	}
	
	public Future<Object> decodeGmDataAsync(Document encodedValue) {
		AsyncGmDataDecoding asyncGmDataDecoding = new AsyncGmDataDecoding(encodedValue);
		return asyncGmDataDecoding.start();
	}

}
