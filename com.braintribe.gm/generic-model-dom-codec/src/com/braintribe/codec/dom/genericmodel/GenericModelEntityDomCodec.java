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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.context.CodingContext;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.GmReflectionTools;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.utils.xml.XmlTools;

public class GenericModelEntityDomCodec<T extends GenericEntity> implements Codec<T, Element> {
	protected GenericModelDomCodecRegistry codecRegistry;
	protected GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	private EntityType<T> type;

	@Configurable @Required
	public void setType(EntityType<T> type) {
		this.type = type;
	}

	public void setCodecRegistry(
			GenericModelDomCodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	public GenericModelDomCodecRegistry getCodecRegistry() {
		if (codecRegistry == null) {
			codecRegistry = new GenericModelDomCodecRegistry();

		}

		return codecRegistry;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T decode(Element element) throws CodecException {

		if(element.getTagName().equals("null")) {
			return null;
		}

		DecodingContext decodingContext = CodingContext.get();
		GenericModelDomCodecRegistry codecRegistry = getCodecRegistry();

		String ref = element.getAttribute("ref");
		if (ref != null && ref.length() > 0) {
			Object referencedEntity = decodingContext.getEntity(ref);

			if (referencedEntity == null) {
				Element pooledElement = decodingContext.getPooledElement(ref);
				
				if (pooledElement == null) {
					if (!decodingContext.getLenience().isTypeLenient()) 
						throw new CodecException("referenced non existent entity with id " + ref);
				}
				else {
					referencedEntity = decode(pooledElement);
				}
				
			}

			try {
				return (T)referencedEntity;
			} catch (Exception e) {
				throw new CodecException("referenced entity with id " + ref + " has wrong type", e);
			}
		}
		else {
			try {
				T entity = null;

				String typeName = element.getAttribute("type");

				if (typeName == null || typeName.length() == 0)
					throw new CodecException("missing type attribute");
				
				String idString = element.getAttribute("id");
				
				//boolean partial = Boolean.TRUE.toString().equals(element.getAttribute("partial"));
				
				EntityType<T> type = null;
				try {
					type = (EntityType<T>)typeReflection.getEntityType(typeName);
					entity = decodingContext.createRaw(type);
				} catch (GenericModelException e) {
					if (decodingContext.getLenience().isTypeLenient()) {
						// unknown type --> ignore it
						Iterator<Element> it = XmlTools.getElementIterator(element, null);

						while (it.hasNext()) {
							Element propertyElement = it.next();
							Element valueElement = XmlTools.getFirstElement(propertyElement, null);
							Codec<Object, Element> codec = codecRegistry.getCodec(BaseType.INSTANCE);
							codec.decode(valueElement);
						}
						
						return null;
					}
					throw e;
				}

				if (idString != null && idString.length() > 0) {
					decodingContext.register(idString, entity);
				}

				PropertyAbsenceHelper propertyAbsenceHelper = decodingContext.shouldAssignAbsenceInformationForMissingProperties()?
						new ActivePropertyAbsenceHelper(decodingContext): InactivePropertyAbsenceHelper.instance;
						
				Iterator<Element> it = XmlTools.getElementIterator(element, null);

				while (it.hasNext()) {
					Element propertyElement = it.next();
					String propertyName = propertyElement.getAttribute("name");
					
					Property property = type.findProperty(propertyName);
					if (property != null) {
						propertyAbsenceHelper.addPresent(property);
						boolean absent = Boolean.TRUE.toString().equals(propertyElement.getAttribute("absent"));

						if (absent) {
							Codec<AbsenceInformation, Element> codec = codecRegistry.getCodec(AbsenceInformation.T);
							Element absenceInformationElement = XmlTools.getFirstElement(propertyElement, null);
							AbsenceInformation absenceInformation = codec.decode(absenceInformationElement);
							GmReflectionTools.setOptimalAbsenceInformation(entity, property, absenceInformation);
						}
						else {
							Element valueElement = XmlTools.getFirstElement(propertyElement, null);
							GenericModelType propertyType = property.getType();
							Codec<Object, Element> codec = codecRegistry.getCodec(propertyType);
							try {
								Object value = codec.decode(valueElement);
								property.set(entity, value);
							//} catch (ClassCastException e) {
							} catch (Exception e) {
								if (decodingContext.getLenience().isPropertyClassCastExceptionLenient()) {
									// types don't match --> ignore (i.e. don't set property value)
								} else {
									throw e;
								}
							} 
						}
					} else {
						// property not found
						if (decodingContext.getLenience().isPropertyLenient()) {
							Element valueElement = XmlTools.getFirstElement(propertyElement, null);
							Codec<Object, Element> codec = codecRegistry.getCodec(BaseType.INSTANCE);
							codec.decode(valueElement);
							// ignore missing property
						} else {
							throw new CodecException("EntityType " + type.getTypeSignature() + " has no property named '" + propertyName + "'!"); 
						}
					}
				}
				
				propertyAbsenceHelper.ensureAbsenceInformation(type, entity);

				return entity;
			}
			catch (Exception e) {
				throw new CodecException("error while decoding entity", e);
			}
		}
	}

	@Override
	public Element encode(T entity) throws CodecException {
		EncodingContext encodingContext = CodingContext.get();
		Document document = encodingContext.getDocument();

		if (entity == null) {
			return document.createElement("null");
		}

		Integer refId = encodingContext.getId(entity);

		if (refId != null) {
			// encode reference
			Element element = document.createElement("entity");
			element.setAttribute("ref", refId.toString());
			return element;
		}
		else {
			try {
				// encode entity
				refId = encodingContext.register(entity);
				Element element = document.createElement("entity");
				element.setAttribute("id", refId.toString());

				EntityType<T> type = entity.entityType();
				
				encodingContext.registerRequiredType(type);

				element.setAttribute("type", type.getTypeSignature());

				boolean partial = false;

				for (Property property: type.getProperties()) {

					String propertyName = property.getName();
					Object value = property.get(entity);
					GenericModelType propertyType = property.getType();

					Element propertyElement = document.createElement("property");
					propertyElement.setAttribute("name", propertyName);

					AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);

					// Partial Representation needed here?
					if (absenceInformation != null) {
						// stop serializing of value here and deliver only AbsenceInformation
						partial = true;
						propertyElement.setAttribute("absent", Boolean.TRUE.toString());

						GenericModelType absenceInformationType = AbsenceInformation.T;
						Codec<AbsenceInformation, Element> codec = codecRegistry.getCodec(absenceInformationType);
						Element childElement = codec.encode(absenceInformation);
						propertyElement.appendChild(childElement);
					}
					else {
						// normal value serializing here
						Codec<Object, Element> codec = codecRegistry.getCodec(propertyType);
						Element propertyValueElement = codec.encode(value);
						propertyElement.appendChild(propertyValueElement);
					}

					element.appendChild(propertyElement);
				}

				if (partial)
					element.setAttribute("partial", Boolean.TRUE.toString());
					
				Element entitiesElement = encodingContext.getPoolElement();
				
				// store real entities only in the entities element if present and return only entity ref elements in that case
				if (entitiesElement != null) {
					entitiesElement.appendChild(element);
					
					element = document.createElement("entity");
					element.setAttribute("ref", refId.toString());
				}

				return element;
			} catch (GenericModelException e) {
				throw new CodecException("error while encoding entity", e);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<T> getValueClass() {
		if (type != null) {
			return type.getJavaType();
		}
		else
			return null;
	}
	
	private static abstract class PropertyAbsenceHelper {
		
		
		public abstract void addPresent(Property property);
		public abstract void ensureAbsenceInformation(EntityType<?> entityType, GenericEntity entity);
	}
	
	private static class ActivePropertyAbsenceHelper extends PropertyAbsenceHelper {
		private final Set<Property> presentProperties = new HashSet<>();
		private final DecodingContext context;
		
		public ActivePropertyAbsenceHelper(DecodingContext context) {
			super();
			this.context = context;
		}

		@Override
		public void addPresent(Property property) {
			presentProperties.add(property);
		}
		
		@Override
		public void ensureAbsenceInformation(EntityType<?> entityType,
				GenericEntity entity) {
			List<Property> properties = entityType.getProperties();
			
			if (properties.size() != presentProperties.size()) {
				for (Property property: properties) {
					if (!presentProperties.contains(property)) {
						property.setAbsenceInformation(entity, context.getAbsenceInformationForMissingProperties());
					}
				}
			}
			
		}
	}
	
	private static class InactivePropertyAbsenceHelper extends PropertyAbsenceHelper {
		public static InactivePropertyAbsenceHelper instance = new InactivePropertyAbsenceHelper();
		@Override
		public void addPresent(Property property) {
			// noop
		}
		
		@Override
		public void ensureAbsenceInformation(EntityType<?> entityType, GenericEntity entity) {
			// noop
		}
	}

}
