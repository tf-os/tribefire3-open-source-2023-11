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
package com.braintribe.gwt.gmview.metadata.client;

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.codec.string.client.GwtDateCodec;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.LocaleUtil;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.template.client.Template;
import com.braintribe.gwt.template.client.TemplateException;
import com.braintribe.gwt.template.client.model.MergeContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.display.SelectiveInformation;
import com.braintribe.model.processing.core.expert.api.DenotationMap;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.utils.i18n.I18nTools;
import com.sencha.gxt.core.shared.FastMap;

/**
 * Expert responsible for handling {@link SelectiveInformation} metaData.
 * @author michel.docouto
 *
 */
@SuppressWarnings("rawtypes")
public class SelectiveInformationResolver {

	private static final Logger logger = new Logger(SelectiveInformationResolver.class);
	private static final DenotationMap<GenericEntity, Function<GenericEntity, String >> selectiveInformationExperts = SelectiveInformationStaticExperts.experts();
	private static final String SHORT_ENTITY_TYPE = "#type_short";
	private static final String ENTITY_TYPE = "#type";
	private static final String ENTITY_DISPLAYINFO = "#type_i18n";
	private static final String ID = "#id";
	private static final SelectiveInformation defaultSelectiveInformation;
	private static final GwtDateCodec GWT_DATE_CODEC;

	private static Map<Class<?>, Codec<Object, String>> typeCodecMap = new HashMap<>();
	
	static {
		GWT_DATE_CODEC = new GwtDateCodec();
		typeCodecMap.put(Boolean.class, (Codec) new I18NBooleanCodec());
		typeCodecMap.put(Date.class, (Codec) GWT_DATE_CODEC);

		defaultSelectiveInformation = SelectiveInformation.T.create();
		LocalizedString template = LocalizedString.T.create();
		Map<String, String> templateLocalizations = new FastMap<>();
		templateLocalizations.put("default", "${#type_i18n} (${#id})");
		template.setLocalizedValues(templateLocalizations);
		defaultSelectiveInformation.setTemplate(template);
	}
	
	public static void setTypeCodecMap(Map<Class<?>, Codec<Object, String>> typeCodecMap) {
		SelectiveInformationResolver.typeCodecMap = typeCodecMap;
	}
	
	public static String resolve(GenericEntity genericEntity, EntityMdResolver entityContextBuilder/*, SelectorContext selectorContext*/) {
		if (genericEntity instanceof LocalizedString)
			return I18nTools.getLocalized((LocalizedString) genericEntity);
		
		Function<GenericEntity, String> expert = selectiveInformationExperts.find(genericEntity);
		if (expert != null)
			return expert.apply(genericEntity);

		SelectiveInformation selectiveInformation = null;
		if (entityContextBuilder != null)
			selectiveInformation = entityContextBuilder.meta(SelectiveInformation.T).exclusive();
		
		if (selectiveInformation == null)
			selectiveInformation = defaultSelectiveInformation;

		try {
			String resolvedSelectiveInformation = resolveSelectiveInformation(selectiveInformation, genericEntity, entityContextBuilder/*, selectorContext*/);
			if (selectiveInformation != defaultSelectiveInformation && (resolvedSelectiveInformation == null || resolvedSelectiveInformation.trim().isEmpty()))
				return resolveSelectiveInformation(defaultSelectiveInformation, genericEntity, entityContextBuilder/*, selectorContext*/);
			else
				return resolvedSelectiveInformation;

		} catch (TemplateException e) {
			logger.error("Error while applying template.", e);
			e.printStackTrace();
			return null;
		}
	}
	
	public static String resolve(EntityType<?> entityType, GenericEntity genericEntity, CmdResolver cmdResolver, String useCase) {
		return resolve(entityType, genericEntity, cmdResolver != null ? cmdResolver.getMetaData() : null, useCase, true);
	}
	
	public static String resolve(EntityType<?> entityType, GenericEntity genericEntity, CmdResolver cmdResolver, String useCase, boolean lenient/*, SelectorContext selectorContext*/) {
		return resolve(entityType, genericEntity, cmdResolver != null ? cmdResolver.getMetaData() : null, useCase, lenient/*, selectorContext*/);
	}
	
	public static String resolve(EntityType<?> entityType, GenericEntity genericEntity, ModelMdResolver cmdResolver, String useCase) {
		return resolve(entityType, genericEntity, cmdResolver, useCase, true);
	}
	
	
	/**
	 * @param entityType - with the current implementation, this parameter is not used
	 * TODO - remove it
	 */
	public static String resolve(EntityType<?> entityType, GenericEntity genericEntity, ModelMdResolver cmdResolver, String useCase, boolean lenient/*, SelectorContext selectorContext*/) {
		if (genericEntity != null)
			cmdResolver = getMetaData(genericEntity);
		else 
			return "";
		EntityMdResolver entityContextBuilder = null;
		if (cmdResolver != null) {
			entityContextBuilder = cmdResolver.lenient(lenient).entity(genericEntity);
			
			if (useCase != null)
				entityContextBuilder = entityContextBuilder.useCase(useCase);
		}
		
		return resolve(genericEntity, entityContextBuilder /*, selectorContext*/);
					
		//SelectiveInformation selectiveInformation = entityContextBuilder.useCase(useCase).meta(SelectiveInformation.T).exclusive();
		//if (selectiveInformation == null) {
		//	if (entityType.hasExplicitSelectiveInformation())
		//		return entityType.getSelectiveInformation(genericEntity);
		//	else
		//		selectiveInformation = defaultSelectiveInformation;
		//}
		//try {
		//	String resolvedSelectiveInformation = resolveSelectiveInformation(selectiveInformation, genericEntity, cmdResolver, useCase/*, selectorContext*/);
		//	if (selectiveInformation != defaultSelectiveInformation && (resolvedSelectiveInformation == null || resolvedSelectiveInformation.trim().isEmpty()))
		//		return resolveSelectiveInformation(defaultSelectiveInformation, genericEntity, cmdResolver, useCase/*, selectorContext*/);
		//	else
		//		return resolvedSelectiveInformation;
		//} catch (TemplateException e) {
		//	logger.error("Error while applying template.", e);
		//	e.printStackTrace();
		//	return null;
		//}		
	}
	
	public static String resolve(GmEntityType gmEntityType, GenericEntity genericEntity, CmdResolver cmdResolver, String useCase/*, SelectorContext selectorContext*/) {
		if (genericEntity instanceof LocalizedString)
			return I18nTools.getLocalized((LocalizedString) genericEntity);
		
		EntityMdResolver entityContextBuilder = cmdResolver.getMetaData().entityTypeSignature(gmEntityType.getTypeSignature());
		if (useCase != null)
			entityContextBuilder = entityContextBuilder.useCase(useCase);		
		
		SelectiveInformation selectiveInformation = entityContextBuilder.meta(SelectiveInformation.T).exclusive();
		if (selectiveInformation == null)
			selectiveInformation = defaultSelectiveInformation;
		
		try {
			String resolvedSelectiveInformation = resolveSelectiveInformation(selectiveInformation, genericEntity, entityContextBuilder/*, selectorContext*/);
			if (selectiveInformation != defaultSelectiveInformation && (resolvedSelectiveInformation == null || resolvedSelectiveInformation.trim().isEmpty()))
				return resolveSelectiveInformation(defaultSelectiveInformation, genericEntity, entityContextBuilder/*, selectorContext*/);
			
			return resolvedSelectiveInformation;
		} catch (TemplateException e) {
			logger.error("Error while applying template.", e);
			e.printStackTrace();
			return null;
		}
	}
	
	private static String resolveSelectiveInformation(SelectiveInformation selectiveInformation, final GenericEntity entity,
			final EntityMdResolver entityContextBuilder /* final SelectorContext selectorContext */) throws TemplateException {
		String templateString = selectiveInformation.getTemplate() != null ? I18nTools.getLocalized(selectiveInformation.getTemplate()) : null;
		
		return resolveTemplateString(templateString, entity, entityContextBuilder/* , selectorContext*/);
	}
	
	/**
	 * Resolves the given template string.
	 */
	public static String resolveTemplateString(String templateString, GenericEntity entity, EntityMdResolver entityMdResolver)
			throws TemplateException {
		if (templateString == null)
			return null;
		
		MergeContext mergeContext = new MergeContext();
		mergeContext.setVariableProvider(variablePath -> getVariableValue(variablePath, entity, entityMdResolver/*, selectorContext*/));
		
		Template template = Template.parse(templateString);
		return template.merge(mergeContext);
		
//		EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(entity);
//		Object id = entityType.getIdProperty().getProperty(entity);
// 		throw new TemplateException("no template string for the selectiveInformation set on " +
// 				entityType.getShortName() + "[" + id.toString() + "]");
	}
	
	private static String getVariableValue(String variablePath, GenericEntity entity, EntityMdResolver entityContextBuilder/*, SelectorContext selectorContext*/) {
		if (entity == null)
			return "";
		
		if (variablePath.contains(".")) {
			try {
				return handleCompound(variablePath, entity, entityContextBuilder/*, selectorContext*/);
			} catch(Exception ex) {
				return "";
			}
		}
		
		EntityType<GenericEntity> entityType = entity.entityType();
		if (ID.equals(variablePath)) {
			Object id = entity.getId();
			return id == null ? LocalizedText.INSTANCE.newEntity() : handleValue(entity.getId(), entityContextBuilder, variablePath/*, selectorContext*/);
		}
		
		if (ENTITY_DISPLAYINFO.equals(variablePath) || SHORT_ENTITY_TYPE.equals(variablePath)) {
			if (ENTITY_DISPLAYINFO.equals(variablePath)) {
				String entityDisplayInfo = GMEMetadataUtil.getEntityTypeDisplay(entityContextBuilder);
				if (entityDisplayInfo != null)
					return entityDisplayInfo;
			}
			return entityType.getShortName();
		}
		
		if (ENTITY_TYPE.equals(variablePath))
			return entityType.getTypeSignature();
		
		Object value = entityType.getProperty(variablePath).get(entity);
		return handleValue(value, entityContextBuilder, variablePath/*, selectorContext*/);
	}

	/*
	private static String getVariableValue(String variablePath, GenericEntity entity, CascadingMetaDataResolver cmdResolver, String useCase) {
		if (entity != null) {
			if (variablePath.contains(".")) {
				try{
					return handleCompound(variablePath, entity, cmdResolver, useCase);
				}catch(Exception ex){
					return "";
				}
			} else {
				EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(entity);
				if (ID.equals(variablePath)) {
					Object id = entityType.getIdProperty() != null ? entityType.getPropertyValue(entity, entityType.getIdProperty().getPropertyName()) : null;
					return handleValue(id, cmdResolver, useCase);
				} else if (ENTITY_DISPLAYINFO.equals(variablePath) || SHORT_ENTITY_TYPE.equals(variablePath)) {
					if (ENTITY_DISPLAYINFO.equals(variablePath)) {
						EntityTypeDisplayInfo displayInfo = cmdResolver.getMetaData().entityType(entityType).useCase(useCase).meta(EntityTypeDisplayInfo.T).exclusive();
						if (displayInfo != null && displayInfo.getName() != null)
							return I18nTools.getLocalized(displayInfo.getName());
					}
					return entityType.getShortName();
				} else if (ENTITY_TYPE.equals(variablePath)){
					return entityType.getEntityTypeName();
				} else {
					Object value = entityType.getPropertyValue(entity, variablePath);
					return handleValue(value, cmdResolver, useCase);
				}
			}
		} else
			return "";
	}	
	*/
	
	private static String handleValue(Object value, EntityMdResolver entityContextBuilder, String propertyName/*, SelectorContext selectorContext*/) {
		if (value instanceof LocalizedString)
			return I18nTools.getLocalized(((LocalizedString) value));

		if (value instanceof GenericEntity)
			return resolve((GenericEntity) value, entityContextBuilder/*, selectorContext*/);

		if (value instanceof List) {
			if (!((List<?>) value).isEmpty())
				return handleValue(((List<?>) value).get(0), entityContextBuilder, propertyName/*, selectorContext*/);
		} else if (value != null) {
			if (typeCodecMap.containsKey(value.getClass())) {
				try {
					Codec<?, String> codec = typeCodecMap.get(value.getClass());
					if (codec == GWT_DATE_CODEC) {
						String format = entityContextBuilder == null ? LocaleUtil.getDateFormat()
								: GMEMetadataUtil.getDatePattern(entityContextBuilder.property(propertyName), LocaleUtil.getDateFormat());
						GWT_DATE_CODEC.setFormatByString(format);
					}
					return typeCodecMap.get(value.getClass()).encode(value);
				} catch (CodecException e) {
					//NOP
				} finally {
					GWT_DATE_CODEC.setFormatByString(LocaleUtil.getDateFormat());
				}
			}
			return value.toString();
		}
		return "";
	}

	/*
	private static String handleValue(Object value, CascadingMetaDataResolver cmdResolver, String useCase) {
		if (value instanceof LocalizedString) {
			return I18nTools.getLocalized(((LocalizedString) value));
		} else if (value instanceof GenericEntity) {
			GenericEntity genericEntity = (GenericEntity) value;
			EntityType<GenericEntity> valueEntityType = GMF.getTypeReflection().getEntityType(genericEntity);
			return resolve(valueEntityType, genericEntity, cmdResolver, useCase);
		} else if (value instanceof List) {
			if (!((List<?>) value).isEmpty()) {
				return handleValue(((List<?>) value).get(0), cmdResolver, useCase);
			}
		} else if (value != null) {
			if(typeCodecMap.containsKey(value.getClass())){
				try {
					return typeCodecMap.get(value.getClass()).encode(value);
				} catch (CodecException e) {
					return value.toString();
				}
			}
			return value.toString();
		}
		return "";
	}
	*/	
	
	private static String handleCompound(String variablePath, GenericEntity entity, EntityMdResolver entityContextBuilder/*, SelectorContext selectorContext*/) {
		int firstVarIndex = variablePath.indexOf(".");
		String firstPath = variablePath.substring(0, firstVarIndex);
		EntityType<GenericEntity> entityType = entity.entityType();
		
		Object object = entityType.getProperty(firstPath).get(entity);
		GenericEntity compoundEntity = null;
		if (object instanceof GenericEntity) {
			compoundEntity = (GenericEntity) object;
		} else if (object instanceof List) {
			if (!((List<?>) object).isEmpty()) {
				Object possibleEntity = ((List<?>) object).get(0);
				if (possibleEntity instanceof GenericEntity)
					compoundEntity = (GenericEntity) possibleEntity;
			}
		}
		
		return getVariableValue(variablePath.substring(firstVarIndex + 1), compoundEntity, entityContextBuilder/*, selectorContext*/);
	}

	/*
	private static String handleCompound(String variablePath, GenericEntity entity, CascadingMetaDataResolver cmdResolver, String useCase) {
		int firstVarIndex = variablePath.indexOf(".");
		String firstPath = variablePath.substring(0, firstVarIndex);
		EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(entity);
		
		Object object = entityType.getPropertyValue(entity, firstPath);
		GenericEntity compoundEntity = null;
		if (object instanceof GenericEntity) {
			compoundEntity = (GenericEntity) object;
		} else if (object instanceof List) {
			if (!((List<?>) object).isEmpty()) {
				Object possibleEntity = ((List<?>) object).get(0);
				if (possibleEntity instanceof GenericEntity)
					compoundEntity = (GenericEntity) possibleEntity;
			}
		}
		
		return getVariableValue(variablePath.substring(firstVarIndex + 1), compoundEntity, cmdResolver, useCase);
	}
    */
}
