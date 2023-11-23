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
package com.braintribe.model.generic.annotation.meta.handlers;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.List;

import com.braintribe.common.lcd.UnknownEnumException;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Bidirectional;
import com.braintribe.model.generic.annotation.meta.Color;
import com.braintribe.model.generic.annotation.meta.IndexClass;
import com.braintribe.model.generic.annotation.meta.Indexed;
import com.braintribe.model.generic.annotation.meta.Max;
import com.braintribe.model.generic.annotation.meta.MaxLength;
import com.braintribe.model.generic.annotation.meta.Min;
import com.braintribe.model.generic.annotation.meta.MinLength;
import com.braintribe.model.generic.annotation.meta.Pattern;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.annotation.meta.TypeSpecification;
import com.braintribe.model.generic.annotation.meta.api.MdaHandler;
import com.braintribe.model.generic.annotation.meta.api.analysis.MdaAnalysisContext;
import com.braintribe.model.generic.annotation.meta.api.synthesis.ClassReference;
import com.braintribe.model.generic.annotation.meta.api.synthesis.MdaSynthesisContext;
import com.braintribe.model.generic.annotation.meta.api.synthesis.SingleAnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.base.BasicMdaHandler;
import com.braintribe.model.generic.tools.GmValueCodec;
import com.braintribe.model.generic.tools.GmValueCodec.EnumParsingMode;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.query.Index;
import com.braintribe.model.meta.data.query.IndexType;
import com.braintribe.utils.i18n.I18nTools;

/**
 * @author peter.gazdik
 */
public class SimpleMdaHandlers {

	private static final Logger log = Logger.getLogger(SimpleMdaHandlers.class);

	// ###############################################
	// ## . . . . . . . BIDIRECTIONAL . . . . . . . ##
	// ###############################################

	public static final MdaHandler<Bidirectional, com.braintribe.model.meta.data.constraint.Bidirectional> BIDIRECTIONAL = new BasicMdaHandler<>( //
			Bidirectional.class, com.braintribe.model.meta.data.constraint.Bidirectional.class, Bidirectional::globalId, //
			SimpleMdaHandlers::copyBidirectionalAnnoProps, //
			SimpleMdaHandlers::copyBidirectionalMdProps);

	private static void copyBidirectionalAnnoProps(MdaAnalysisContext context, Bidirectional annotation,
			com.braintribe.model.meta.data.constraint.Bidirectional metaData) {

		GmEntityType gmType = (GmEntityType) context.getGmType(annotation.type());
		context.addPostProcessor(() -> metaData.setLinkedProperty(findProperty(gmType, annotation.property(), context)));
	}

	private static GmProperty findProperty(GmEntityType gmType, String propertyName, MdaAnalysisContext context) {
		List<GmProperty> properties = gmType.getProperties();
		for (GmProperty gmProperty : properties) {
			if (propertyName.equals(gmProperty.getName())) {
				return gmProperty;
			}
		}

		GmProperty target = (GmProperty) context.getTarget();
		log.error("Bidirectional property " + gmType.getTypeSignature() + ":" + propertyName + " not found. Configured on: "
				+ target.getDeclaringType());

		return null;
	}

	@SuppressWarnings("unused")
	private static void copyBidirectionalMdProps(MdaSynthesisContext context, SingleAnnotationDescriptor descriptor,
			com.braintribe.model.meta.data.constraint.Bidirectional md) {

		GmProperty linkedProperty = md.getLinkedProperty();
		descriptor.addAnnotationValue("type", new ClassReference(linkedProperty.getDeclaringType().getTypeSignature()));
		descriptor.addAnnotationValue("property", linkedProperty.getName());
	}

	// ###############################################
	// ## . . . . . . . . . COLOR . . . . . . . . . ##
	// ###############################################

	public static final MdaHandler<Color, com.braintribe.model.meta.data.display.Color> COLOR = new BasicMdaHandler<>( //
			Color.class, com.braintribe.model.meta.data.display.Color.class, Color::globalId, //
			SimpleMdaHandlers::copyColorAnnoProps, //
			SimpleMdaHandlers::copyColorMdProps);

	@SuppressWarnings("unused")
	private static void copyColorAnnoProps(MdaAnalysisContext context, Color annotation, com.braintribe.model.meta.data.display.Color md) {
		md.setCode(annotation.value());
	}

	@SuppressWarnings("unused")
	private static void copyColorMdProps(MdaSynthesisContext context, SingleAnnotationDescriptor descriptor,
			com.braintribe.model.meta.data.display.Color md) {
		descriptor.addAnnotationValue("value", md.getCode());
	}

	// ###############################################
	// ## . . . . . . . . . INDEX . . . . . . . . . ##
	// ###############################################

	public static final MdaHandler<Indexed, Index> INDEX = new BasicMdaHandler<>( //
			Indexed.class, Index.class, Indexed::globalId, //
			SimpleMdaHandlers::copyIndexAnnoProps, //
			SimpleMdaHandlers::copyIndexMdProps);

	@SuppressWarnings("unused")
	private static void copyIndexAnnoProps(MdaAnalysisContext context, Indexed annotation, Index metaData) {
		metaData.setIndexType(toIndexType(annotation.type()));
	}

	private static IndexType toIndexType(IndexClass type) {
		switch (type) {
			case auto:
				return IndexType.auto;
			case lookup:
				return IndexType.lookup;
			case metric:
				return IndexType.metric;
			case none:
				return IndexType.none;
			default:
				throw new UnknownEnumException(type);
		}
	}

	@SuppressWarnings("unused")
	private static void copyIndexMdProps(MdaSynthesisContext context, SingleAnnotationDescriptor descriptor, Index md) {
		descriptor.addAnnotationValue("type", indexType(md).name());
	}

	private static IndexType indexType(Index md) {
		IndexType indexType = md.getIndexType();
		return indexType == null ? IndexType.auto : indexType;
	}

	// ###############################################
	// ## . . . . . . . . . MAX . . . . . . . . . . ##
	// ###############################################

	public static final MdaHandler<Max, com.braintribe.model.meta.data.constraint.Max> MAX = new BasicMdaHandler<>( //
			Max.class, com.braintribe.model.meta.data.constraint.Max.class, Max::globalId, //
			SimpleMdaHandlers::copyMaxAnnoProps, //
			SimpleMdaHandlers::copyMaxMdProps);

	@SuppressWarnings("unused")
	private static void copyMaxAnnoProps(MdaAnalysisContext context, Max annotation, com.braintribe.model.meta.data.constraint.Max metaData) {
		metaData.setLimit(GmValueCodec.objectFromGmString(annotation.value(), EnumParsingMode.enumAsValue));
		metaData.setExclusive(annotation.exclusive());
	}

	@SuppressWarnings("unused")
	private static void copyMaxMdProps(MdaSynthesisContext context, SingleAnnotationDescriptor descriptor,
			com.braintribe.model.meta.data.constraint.Max md) {
		descriptor.addAnnotationValue("value", GmValueCodec.objectToGmString(md.getLimit()));
		descriptor.addAnnotationValue("exclusive", md.getExclusive());
	}

	// ###############################################
	// ## . . . . . . . . MAX LENGTH . . . . . . . .##
	// ###############################################

	public static final MdaHandler<MaxLength, com.braintribe.model.meta.data.constraint.MaxLength> MAX_LENGTH = new BasicMdaHandler<>( //
			MaxLength.class, com.braintribe.model.meta.data.constraint.MaxLength.class, MaxLength::globalId, //
			SimpleMdaHandlers::copyMaxLengthAnnoProps, //
			SimpleMdaHandlers::copyMaxLengthMdProps);

	@SuppressWarnings("unused")
	private static void copyMaxLengthAnnoProps(MdaAnalysisContext context, MaxLength annotation,
			com.braintribe.model.meta.data.constraint.MaxLength metaData) {
		metaData.setLength(annotation.value());
	}

	@SuppressWarnings("unused")
	private static void copyMaxLengthMdProps(MdaSynthesisContext context, SingleAnnotationDescriptor descriptor,
			com.braintribe.model.meta.data.constraint.MaxLength md) {
		descriptor.addAnnotationValue("value", md.getLength());
	}

	// ###############################################
	// ## . . . . . . . . . MIN . . . . . . . . . . ##
	// ###############################################

	public static final MdaHandler<Min, com.braintribe.model.meta.data.constraint.Min> MIN = new BasicMdaHandler<>( //
			Min.class, com.braintribe.model.meta.data.constraint.Min.class, Min::globalId, //
			SimpleMdaHandlers::copyMinAnnoProps, //
			SimpleMdaHandlers::copyMinMdProps);

	@SuppressWarnings("unused")
	private static void copyMinAnnoProps(MdaAnalysisContext context, Min annotation, com.braintribe.model.meta.data.constraint.Min metaData) {
		metaData.setLimit(GmValueCodec.objectFromGmString(annotation.value(), EnumParsingMode.enumAsValue));
		metaData.setExclusive(annotation.exclusive());
	}

	@SuppressWarnings("unused")
	private static void copyMinMdProps(MdaSynthesisContext context, SingleAnnotationDescriptor descriptor,
			com.braintribe.model.meta.data.constraint.Min md) {
		descriptor.addAnnotationValue("value", GmValueCodec.objectToGmString(md.getLimit()));
		descriptor.addAnnotationValue("exclusive", md.getExclusive());
	}

	// ###############################################
	// ## . . . . . . . . MIN LENGTH . . . . . . . .##
	// ###############################################

	public static final MdaHandler<MinLength, com.braintribe.model.meta.data.constraint.MinLength> MIN_LENGTH = new BasicMdaHandler<>( //
			MinLength.class, com.braintribe.model.meta.data.constraint.MinLength.class, MinLength::globalId, //
			SimpleMdaHandlers::copyMinLengthAnnoProps, //
			SimpleMdaHandlers::copyMinLengthMdProps);

	@SuppressWarnings("unused")
	private static void copyMinLengthAnnoProps(MdaAnalysisContext context, MinLength annotation,
			com.braintribe.model.meta.data.constraint.MinLength metaData) {
		metaData.setLength(annotation.value());
	}

	@SuppressWarnings("unused")
	private static void copyMinLengthMdProps(MdaSynthesisContext context, SingleAnnotationDescriptor descriptor,
			com.braintribe.model.meta.data.constraint.MinLength md) {
		descriptor.addAnnotationValue("value", md.getLength());
	}

	// ###############################################
	// ## . . . . . . . . . PATTERN . . . . . . . . ##
	// ###############################################

	public static final MdaHandler<Pattern, com.braintribe.model.meta.data.constraint.Pattern> PATTERN = new BasicMdaHandler<>( //
			Pattern.class, com.braintribe.model.meta.data.constraint.Pattern.class, Pattern::globalId, //
			SimpleMdaHandlers::copyPatternAnnoProps, //
			SimpleMdaHandlers::copyPatternMdProps);

	@SuppressWarnings("unused")
	private static void copyPatternAnnoProps(MdaAnalysisContext context, Pattern annotation,
			com.braintribe.model.meta.data.constraint.Pattern metaData) {
		metaData.setExpression(annotation.value());
	}

	@SuppressWarnings("unused")
	private static void copyPatternMdProps(MdaSynthesisContext context, SingleAnnotationDescriptor descriptor,
			com.braintribe.model.meta.data.constraint.Pattern md) {
		descriptor.addAnnotationValue("value", md.getExpression());
	}

	// ###############################################
	// ## . . . . . POSITIONAL ARGUMENTS . . . . . ##
	// ###############################################

	public static final MdaHandler<PositionalArguments, com.braintribe.model.meta.data.mapping.PositionalArguments> POSITIONAL_ARGUMENTS = new BasicMdaHandler<>( //
			PositionalArguments.class, com.braintribe.model.meta.data.mapping.PositionalArguments.class, PositionalArguments::globalId, //
			SimpleMdaHandlers::copyPositionalArgumentsAnnoProps, //
			SimpleMdaHandlers::copyPositionalArgumentsMdProps);

	@SuppressWarnings("unused")
	private static void copyPositionalArgumentsAnnoProps(MdaAnalysisContext context, PositionalArguments annotation,
			com.braintribe.model.meta.data.mapping.PositionalArguments metaData) {
		metaData.setProperties(asList(annotation.value()));
	}

	@SuppressWarnings("unused")
	private static void copyPositionalArgumentsMdProps(MdaSynthesisContext context, SingleAnnotationDescriptor descriptor,
			com.braintribe.model.meta.data.mapping.PositionalArguments md) {
		List<String> props = md.getProperties();
		descriptor.addAnnotationValue("value", props.toArray(new String[props.size()]));
	}

	// ###############################################
	// ## . . . . . . . . PRIORITY . . . . . . . . .##
	// ###############################################

	public static final MdaHandler<Priority, com.braintribe.model.meta.data.prompt.Priority> PRIORITY = new BasicMdaHandler<>( //
			Priority.class, com.braintribe.model.meta.data.prompt.Priority.class, Priority::globalId, //
			SimpleMdaHandlers::copyPriorityAnnoProps, //
			SimpleMdaHandlers::copyPriorityMdProps);

	@SuppressWarnings("unused")
	private static void copyPriorityAnnoProps(MdaAnalysisContext context, Priority annotation,
			com.braintribe.model.meta.data.prompt.Priority metaData) {
		metaData.setPriority(annotation.value());
	}

	@SuppressWarnings("unused")
	private static void copyPriorityMdProps(MdaSynthesisContext context, SingleAnnotationDescriptor descriptor,
			com.braintribe.model.meta.data.prompt.Priority md) {
		descriptor.addAnnotationValue("value", md.getPriority());
	}

	// ###############################################
	// ## . . . . . SELECTIVE INFORMATION . . . . . ##
	// ###############################################

	public static final MdaHandler<SelectiveInformation, com.braintribe.model.meta.data.display.SelectiveInformation> SELECTIVE_INFORMATION = new BasicMdaHandler<>( //
			SelectiveInformation.class, com.braintribe.model.meta.data.display.SelectiveInformation.class, SelectiveInformation::globalId, //
			SimpleMdaHandlers::copySelectiveInformationAnnoProps, //
			SimpleMdaHandlers::copySelectiveInformationMdProps);

	@SuppressWarnings("unused")
	private static void copySelectiveInformationAnnoProps(MdaAnalysisContext context, SelectiveInformation annotation,
			com.braintribe.model.meta.data.display.SelectiveInformation metaData) {
		metaData.setTemplate(I18nTools.createLsWithGlobalId(annotation.value(), "ls:" + metaData.getGlobalId()));
	}

	@SuppressWarnings("unused")
	private static void copySelectiveInformationMdProps(MdaSynthesisContext context, SingleAnnotationDescriptor descriptor,
			com.braintribe.model.meta.data.display.SelectiveInformation md) {
		descriptor.addAnnotationValue("value", I18nTools.getDefault(md.getTemplate()));
	}

	// ###############################################
	// ## . . . . . . TYPE SPECIFICATION . . . . . .##
	// ###############################################

	public static final MdaHandler<TypeSpecification, com.braintribe.model.meta.data.constraint.TypeSpecification> TYPE_SPECIFICATION = new BasicMdaHandler<>( //
			TypeSpecification.class, com.braintribe.model.meta.data.constraint.TypeSpecification.class, TypeSpecification::globalId, //
			SimpleMdaHandlers::copyTypeSpecificationAnnoProps, //
			SimpleMdaHandlers::copyTypeSpecificationMdProps);

	private static void copyTypeSpecificationAnnoProps(MdaAnalysisContext context, TypeSpecification annotation,
			com.braintribe.model.meta.data.constraint.TypeSpecification metaData) {
		GmType gmType = (GmType) context.getGmType(annotation.value());
		metaData.setType(gmType);
	}

	@SuppressWarnings("unused")
	private static void copyTypeSpecificationMdProps(MdaSynthesisContext context, SingleAnnotationDescriptor descriptor,
			com.braintribe.model.meta.data.constraint.TypeSpecification md) {

		String javaTypeSignature = getJavaTypeSignature(md.getType());
		descriptor.addAnnotationValue("value", new ClassReference(javaTypeSignature));
	}

	private static String getJavaTypeSignature(GmType type) {
		return type.isGmCustom() ? type.getTypeSignature() : type.reflectionType().getJavaType().getName();
	}

}
