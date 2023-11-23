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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.codec.CodecException;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.genericmodel.client.codec.api.GmEncodingContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.TypeCode;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.ProcessingInstruction;
import com.google.gwt.xml.client.XMLParser;

public class DomEncodingContextImpl extends AbstractCodingContext implements DomEncodingContext, GmCodecConstants {
	private static final String aliasDigits = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private final Map<GenericEntity, String> qualifiedIdByEntities = new HashMap<>();
	private EntityType<AbsenceInformation> absenceInformationType;
	//private Set<GenericModelType> requiredGenericModelTypes = new HashSet<GenericModelType>();
	private final Map<GenericModelType, TypeInfo> requiredTypes = new HashMap<>();
	private final byte[] aliasSequence = new byte[10]; 
	private int aliasDigitCount = 1; 
	private final Set<String> shortTypeNames = new HashSet<>();
	private final GenericModelTypeReflection typeReflection;
	private boolean writeAbsenceInformation = true;
	private final EntityType<?> aiType;
	private GmEncodingContext encodingContext;

	private Document document = XMLParser.createDocument();

	private Element pool;

	private final int maxDeferred = -1;

	
	public DomEncodingContextImpl(Document document, GenericModelTypeReflection typeReflection, GmEncodingContext encodingContext) {
		this.document = document;
		this.typeReflection = typeReflection;
		this.aiType = typeReflection.getEntityType(AbsenceInformation.class);
		this.encodingContext = encodingContext;
	}

	@Override
	public boolean isSimpleAbsenceInformation(AbsenceInformation absenceInformation) {
		return absenceInformation.entityType() == aiType && absenceInformation.getSize() == null;
	}

	public void setWriteAbsenceInformation(boolean writeAbsenceInformation) {
		this.writeAbsenceInformation = writeAbsenceInformation;
	}

	@Override
	public TypeInfo registerRequiredType(GenericModelType type) {
		TypeInfo typeInfo = requiredTypes.get(type);
		
		if (typeInfo == null) {
			typeInfo = new TypeInfo();
			typeInfo.type = type;
			typeInfo.as = getShortName(type);
			typeInfo.alias = nextAlias();
			requiredTypes.put(type, typeInfo);
		}
		
		return typeInfo;
	}

	private String getShortName(GenericModelType type) {
		String suggestion = type.getJavaType().getSimpleName();
		return ensureUniqueShortName(suggestion, 1);
	}

	private String ensureUniqueShortName(String suggestion, int alternative) {
		String shortName = suggestion;
		if (alternative > 1) {
			shortName += alternative;
		}
		if (shortTypeNames.add(shortName)) {
			return shortName;
		}
		else {
			return ensureUniqueShortName(suggestion, alternative + 1);
		}
	}

	private String nextAlias() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < aliasDigitCount; i++) {
			builder.append(aliasDigits.charAt(aliasSequence[i]));
		}
		
		for (int i = 0; i < aliasSequence.length; i++) {
			byte num = aliasSequence[i];
			if (++num == aliasDigits.length()) {
				aliasSequence[i] = 0;
			}
			else {
				aliasSequence[i] = num;
				int countCandidate = i + 1;
				if (countCandidate > aliasDigitCount)
					aliasDigitCount = countCandidate;
				break;
			}
		}

		
		return builder.toString();
	}

	@Override
	public String lookupQualifiedId(GenericEntity entity) throws CodecException {
		String id = qualifiedIdByEntities.get(entity);
		
		if (id == null) {
			TypeInfo typeInfo = registerRequiredType(entity.<GenericEntity>entityType());
			
			id = typeInfo.nextId();
			qualifiedIdByEntities.put(entity, id);
			
			EntityDomDeferredEncoder deferredEncoder = new EntityDomDeferredEncoder(this, typeInfo, entity, id);

			appendDeferredProcessor(deferredEncoder);
		}
		
		return id;
	}
	
	public EntityType<AbsenceInformation> getAbsenceInformationType() {
		if (absenceInformationType == null) {
			absenceInformationType = this.typeReflection.getEntityType(AbsenceInformation.class);
		}

		return absenceInformationType;
	}

	@Override
	public boolean shouldWriteAbsenceInformation() {
		return writeAbsenceInformation;
	}

	@Override
	public Collection<TypeInfo> getRequiredTypeInfos() {
		return requiredTypes.values();
	}
	
	@Override
	public Document getDocument() {
		return document;
	}

	@Override
	public int getMaxDeferred() {
		return maxDeferred ;
	}

	@Override
	public void appendToPool(Element element) {
		this.pool.appendChild(element);
	}

	@Override
	public void setPool(Element element) {
		this.pool = element;
	}

	private Element createValueElement(String tagName, String content) {
		Element element = document.createElement(tagName);
		element.appendChild(document.createTextNode(content));
		return element;
	}
	
	@Override
	public Element encodeValue(GenericModelType type, Object value, boolean skipEmptyCollections) throws CodecException {
		if (value == null) {
			return document.createElement("n");
		}
		else {
			switch (type.getTypeCode()) {
				case objectType: return encodeValue(type.getActualType(value), value, false);
				
				// simple scalar types
				case booleanType: return createValueElement("b", value.toString());
				case dateType: return createValueElement("T", dateFormat.format((Date)value));
				case decimalType: return createValueElement("D", value.toString());
				case doubleType: return createValueElement("d", value.toString());
				case floatType: return createValueElement("f", value.toString());
				case integerType: return createValueElement("i", value.toString());
				case longType: return createValueElement("l", value.toString());
				case stringType: return createValueElement("s", (String)value);
				
				// custom types
				case entityType: return createValueElement("r", lookupQualifiedId((GenericEntity)value));
				case enumType: return createValueElement("e", registerRequiredType(typeReflection.getType((Enum<?>)value)).as + "." + value);
				
				// collections
				case listType: return encodeCollection("L", (CollectionType)type, (Collection<?>)value, skipEmptyCollections);
				case mapType: return encodeMap((CollectionType)type, (Map<?, ?>)value, skipEmptyCollections);
				case setType: return encodeCollection("S", (CollectionType)type, (Collection<?>)value, skipEmptyCollections);
				
				default:
					throw new CodecException("unsupported GenericModelType " + type);
			}
		}
	}
	
	private Element encodeCollection(String tagName, CollectionType collectionType, Collection<?> value, boolean skipEmptyCollections) throws CodecException {
		if (skipEmptyCollections && value.isEmpty())
			return null;
		
		final GenericModelType elementType = collectionType.getCollectionElementType();
		final Element element = getDocument().createElement(tagName);
		final int maxDeferred = getMaxDeferred();
		
		if (maxDeferred != -1) {
			final Iterator<?> it = value.iterator();
			
			appendDeferredProcessor(new DeferredProcessor() {
				@Override
				public boolean continueProcessing() throws CodecException {
					return encodeCollection(elementType, it, element, maxDeferred);
				}
			});
		}
		else {
			encodeCollection(elementType, value.iterator(), element, -1);
		}
		
		return element;
	}
	
	private boolean encodeCollection(GenericModelType elementType, Iterator<?> it, Element element, int maxEncode) throws CodecException {
		int i = 0;
		while (it.hasNext()) {
			
			if (maxEncode != -1 && i == maxEncode) 
				return true;
			
			Object e = it.next();
			element.appendChild(encodeValue(elementType, e, false));
			
			i++;
		}
		
		return false;
	}

	private Element encodeMap(CollectionType collectionType, Map<?, ?> value, boolean skipEmptyCollections) throws CodecException {
		final Document document = getDocument();
		
		if (skipEmptyCollections && value.isEmpty())
			return null;
		
		GenericModelType[] parameterization = collectionType.getParameterization();
		final GenericModelType keyType = parameterization[0];
		final GenericModelType valueType = parameterization[1];
		final Element element = document.createElement("M");
		final int maxDeferred = getMaxDeferred();
		final Iterator<? extends Map.Entry<?, ?>> it = value.entrySet().iterator();
		
		if (maxDeferred != -1) {
			appendDeferredProcessor(new DeferredProcessor() {
				@Override
				public boolean continueProcessing() throws CodecException {
					return encodeMap(keyType, valueType, it, element, maxDeferred);
				}
			});
		}
		else {
			encodeMap(keyType, valueType, it, element, -1);
		}

		return element;
	}

	private boolean encodeMap(GenericModelType keyType, GenericModelType valueType, Iterator<? extends Map.Entry<?, ?>> it, Element element, int maxEncode) throws CodecException {
		int i = 0;
		Document document = getDocument();
		while (it.hasNext()) {
			
			if (maxEncode != -1 && i >= maxEncode) 
				return true;
			
			Map.Entry<?, ?> entry = it.next();

			Element entryElement = document.createElement("m");
			Element keyElement = encodeValue(keyType, entry.getKey(), false);
			Element valueElement = encodeValue(valueType, entry.getValue(), false);
			entryElement.appendChild(keyElement);
			entryElement.appendChild(valueElement);
			element.appendChild(entryElement);
			
			i += 2;
		}
		
		return false;
	}

	public Future<Document> encodeGmDataAsync(Object value) {
		final Future<Document> future = new Future<Document>();
		ProcessingInstruction gmXmlPi = document.createProcessingInstruction("gm-xml", "version=\"4\"");
 		document.appendChild(gmXmlPi);

		try {
			final Element rootValueElement = encodeRootValue(value);
			final Element poolElement = getDocument().createElement("pool");
			setPool(poolElement);
			
			runDeferredProcessorsAsync() //
					.andThen(v -> {
						Element requiredTypesElement = encodeRequiredTypes();

						Element gmDataElement = getDocument().createElement("gm-data");

						gmDataElement.appendChild(requiredTypesElement);
						gmDataElement.appendChild(rootValueElement);
						gmDataElement.appendChild(poolElement);

						document.appendChild(gmDataElement);

						future.onSuccess(document);
					}).onError(future::onFailure);
		} catch (CodecException e) {
			future.onFailure(e);
		}
		
		return future;
	}
	
	public Document encodeGmData(Object value) throws CodecException {
		ProcessingInstruction gmXmlPi = document.createProcessingInstruction("gm-xml", "version=\"4\"");
 		document.appendChild(gmXmlPi);

		
		Element rootValueElement = encodeRootValue(value);
		
		Element poolElement = getDocument().createElement("pool");
		setPool(poolElement);

		runDeferredProcessors();
		
		Element requiredTypesElement = encodeRequiredTypes();
		
		Element gmDataElement = getDocument().createElement("gm-data");
		
		gmDataElement.appendChild(requiredTypesElement);
		gmDataElement.appendChild(rootValueElement);
		gmDataElement.appendChild(poolElement);
		
		document.appendChild(gmDataElement);
		
		return document;
	}
	
	
	private Element encodeRequiredTypes() {
		Document document = getDocument();
		Element element = document.createElement("required-types");
		
		List<TypeInfo> typeInfos = new ArrayList<>(getRequiredTypeInfos());
		Collections.sort(typeInfos);
		
		for (TypeInfo typeInfo: typeInfos) {
			Element typeElement = document.createElement("t");
			typeElement.setAttribute("as", typeInfo.as);
			typeElement.setAttribute("alias", typeInfo.alias);
			
			if (typeInfo.type.getTypeCode() == TypeCode.entityType)
				typeElement.setAttribute("num", Integer.toString(typeInfo.getCount()));
			typeElement.appendChild(document.createTextNode(typeInfo.type.getTypeSignature()));

			element.appendChild(typeElement);
		}
	
		return element;
	}
	
	private Element encodeRootValue(Object value) throws CodecException {
		Element element = getDocument().createElement("root-value");
		Element valueElement = encodeValue(typeReflection.getBaseType(), value, false);
		element.appendChild(valueElement);
		return element;
	}
		
	@Override
	public void visit(GenericEntity entity) {
		if(encodingContext != null)
			encodingContext.visit(entity);
	}
	
}