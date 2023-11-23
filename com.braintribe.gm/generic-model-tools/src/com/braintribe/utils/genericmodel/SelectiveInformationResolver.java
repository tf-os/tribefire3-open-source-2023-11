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
package com.braintribe.utils.genericmodel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.string.BooleanCodec;
import com.braintribe.codec.string.DateCodec;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.display.DisplayInfo;
import com.braintribe.model.meta.data.display.SelectiveInformation;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import com.braintribe.utils.i18n.I18nTools;
import com.braintribe.utils.template.Template;
import com.braintribe.utils.template.TemplateException;
import com.braintribe.utils.template.model.MergeContext;

/**
 * Expert responsible for handling {@link SelectiveInformation} metaData.
 *
 * @author michel.docouto
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SelectiveInformationResolver {
	private static Logger logger = com.braintribe.logging.Logger.getLogger(SelectiveInformationResolver.class);
	private static Map<Class<?>, Codec<Object, String>> typeCodecMap = new HashMap<>();
	private static final String SHORT_ENTITY_TYPE = "#type_short";
	private static final String ENTITY_TYPE = "#type";
	private static final String ENTITY_DISPLAYINFO = "#type_i18n";
	private static final String ID = "#id";
	private static SelectiveInformation defaultSelectiveInformation;

	static {
		DateCodec dateCodec = new DateCodec();
		BooleanCodec booleanCodec = new BooleanCodec();
		typeCodecMap.put(Boolean.class, (Codec) booleanCodec);
		typeCodecMap.put(Date.class, (Codec) dateCodec);

		defaultSelectiveInformation = SelectiveInformation.T.create();
		LocalizedString template = LocalizedString.T.create();
		Map<String, String> templateLocalizations = new HashMap<>();
		templateLocalizations.put("default", "${#type_i18n} (${#id})");
		template.setLocalizedValues(templateLocalizations);
		defaultSelectiveInformation.setTemplate(template);
	}

	public static void setTypeCodecMap(Map<Class<?>, Codec<Object, String>> typeCodecMap) {
		SelectiveInformationResolver.typeCodecMap = typeCodecMap;
	}

	public static void setDefaultSelectiveInformation(String template) {
		defaultSelectiveInformation.getTemplate().getLocalizedValues().put("default", template);
	}

	public static String resolve(GenericEntity entity, PersistenceGmSession session) {
		EntityType<?> entityType = entity.entityType();
		CmdResolver cmdResolver = session.getModelAccessory().getCmdResolver();
		return resolve(entityType, entity, cmdResolver, null);
	}

	public static String resolve(EntityType<?> entityType, GenericEntity genericEntity, CmdResolver cmdResolver,
			String useCase/* , SelectorContext selectorContext */) {
		EntityMdResolver entityContextBuilder;
		if (genericEntity != null) {
			entityContextBuilder = cmdResolver.getMetaData().entity(genericEntity);
		} else {
			entityContextBuilder = cmdResolver.getMetaData().entityType(entityType);
		}
		SelectiveInformation selectiveInformation = entityContextBuilder.useCase(useCase).meta(SelectiveInformation.T).exclusive();
		if (selectiveInformation == null) {
			if (entityType.hasExplicitSelectiveInformation()) {
				return entityType.getSelectiveInformation(genericEntity);
			} else {
				selectiveInformation = defaultSelectiveInformation;
			}
		}
		try {
			String resolvedSelectiveInformation = resolveSelectiveInformation(selectiveInformation, genericEntity, cmdResolver,
					useCase/* , selectorContext */);
			if (selectiveInformation != defaultSelectiveInformation
					&& (resolvedSelectiveInformation == null || resolvedSelectiveInformation.trim().isEmpty())) {
				return resolveSelectiveInformation(defaultSelectiveInformation, genericEntity, cmdResolver,
						useCase/* , selectorContext */);
			} else {
				return resolvedSelectiveInformation;
			}
		} catch (TemplateException e) {
			logger.error("Error while applying template.", e);
			e.printStackTrace();
			return null;
		}
	}

	private static String resolveSelectiveInformation(SelectiveInformation selectiveInformation, final GenericEntity entity,
			final CmdResolver cmdResolver, final String useCase
	/* final SelectorContext selectorContext */) throws TemplateException {
		String templateString = selectiveInformation.getTemplate() != null ? I18nTools.getLocalized(selectiveInformation.getTemplate()) : null;

		if (templateString != null) {
			MergeContext mergeContext = new MergeContext();
			mergeContext.setVariableProvider(new Function<String, Object>() {
				@Override
				public Object apply(String variablePath) throws RuntimeException {
					return getVariableValue(variablePath, entity, cmdResolver, useCase/* , selectorContext */);
				}
			});

			Template template = Template.parse(templateString);
			return template.merge(mergeContext);
		}
		return null;
		// EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(entity);
		// Object id = entityType.getIdProperty().getProperty(entity);
		// throw new TemplateException("no template string for the selectiveInformation set on " +
		// entityType.getShortName() + "[" + id.toString() + "]");
	}

	private static String getVariableValue(String variablePath, GenericEntity entity, CmdResolver cmdResolver,
			String useCase/* , SelectorContext selectorContext */) {
		if (entity != null) {
			if (variablePath.contains(".")) {
				try {
					return handleCompound(variablePath, entity, cmdResolver, useCase/* , selectorContext */);
				} catch (Exception ex) {
					return "";
				}
			} else {
				EntityType<GenericEntity> entityType = entity.entityType();
				if (ID.equals(variablePath)) {
					return handleValue(entity.getId(), cmdResolver, useCase/* , selectorContext */);
				} else if (ENTITY_DISPLAYINFO.equals(variablePath) || SHORT_ENTITY_TYPE.equals(variablePath)) {
					if (ENTITY_DISPLAYINFO.equals(variablePath)) {
						DisplayInfo displayInfo = cmdResolver.getMetaData().entityType(entityType)
								.useCase(useCase).meta(DisplayInfo.T).exclusive();
						if (displayInfo != null && displayInfo.getName() != null) {
							return I18nTools.getLocalized(displayInfo.getName());
						}
					}
					return entityType.getShortName();
				} else if (ENTITY_TYPE.equals(variablePath)) {
					return entityType.getTypeSignature();
				} else {
					Object value = entityType.getProperty(variablePath).get(entity);
					return handleValue(value, cmdResolver, useCase/* , selectorContext */);
				}
			}
		} else {
			return "";
		}
	}

	private static String handleValue(Object value, CmdResolver cmdResolver,
			String useCase/* , SelectorContext selectorContext */) {
		if (value instanceof LocalizedString) {
			return I18nTools.getLocalized(((LocalizedString) value));
		} else if (value instanceof GenericEntity) {
			GenericEntity genericEntity = (GenericEntity) value;
			EntityType<GenericEntity> valueEntityType = genericEntity.entityType();
			return resolve(valueEntityType, genericEntity, cmdResolver, useCase/* , selectorContext */);
		} else if (value instanceof List) {
			if (!((List<?>) value).isEmpty()) {
				return handleValue(((List<?>) value).get(0), cmdResolver, useCase/* , selectorContext */);
			}
		} else if (value != null) {
			if (typeCodecMap.containsKey(value.getClass())) {
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

	private static String handleCompound(String variablePath, GenericEntity entity, CmdResolver cmdResolver,
			String useCase/* , SelectorContext selectorContext */) {

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
				if (possibleEntity instanceof GenericEntity) {
					compoundEntity = (GenericEntity) possibleEntity;
				}
			}
		}

		return getVariableValue(variablePath.substring(firstVarIndex + 1), compoundEntity, cmdResolver,
				useCase/* , selectorContext */);
	}

}
