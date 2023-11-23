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
package com.braintribe.devrock.api.ui.viewers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.mc.reason.IncompleteResolution;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.reason.ReasonMessageCollector;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;
import com.braintribe.model.version.VersionExpression;

/**
 * splits the text output of a {@link Reason} into different section with a 'symbolic style' name attached
 * @author pit
 *
 */
public class ReasonStyler implements ReasonMessageCollector {
	
	public static final String STYLER_STANDARD = "standard";
	public static final String STYLER_EMPHASIS = "emphasis";
	public static final String STYLER_DELIMITER = "delimiter";
	public static final String STYLER_UNDEFINED = "undefined";
	public static final String STYLER_GROUPID = "groupId";
	public static final String STYLER_ARTIFACTID = "artifactId";
	public static final String STYLER_VERSION = "version";
	public static final String STYLER_PART_CLASSIFIER = "part-classifier";
	public static final String STYLER_PART_TYPE = "part-type";
	public static final String STYLER_ERROR = "error";
	public static final String STYLER_WARNING = "warning";

	private PolymorphicDenotationMap<GenericEntity, Consumer<? extends GenericEntity>> formatters = new PolymorphicDenotationMap<>();
	private List<Pair<String,String>> collectedStringParts = new ArrayList<>();
	
	/**
	 * creates an initializes the {@link ReasonStyler}
	 */
	public ReasonStyler() {		
		registerFormatter(CompiledArtifactIdentification.T, this::formatCai);
		registerFormatter(PartIdentification.T, this::formatPi);
		registerFormatter(CompiledDependencyIdentification.T, this::formatCdi);
		registerFormatter( VersionExpression.T, this::formatVersionExpression);
		registerFormatter( AnalysisArtifact.T, this::formatVai);
		registerFormatter( AnalysisDependency.T, this::formatVai);
		//just to make sure that PDM doesn't get confused as some reason get fed with real class
		registerFormatter(CompiledArtifact.T, this::formatCai);
		registerFormatter(CompiledDependency.T, this::formatCdi);
	}
	
	
	/**
	 * process a reason, split it into sequences 
	 * @param reason - the {@link Reason} to parse
	 * @return - a {@link List} of sequences as {@link Pair} of the text and the attributed styler id
	 */
	public List<Pair<String,String>> process( Reason reason) {
		collectedStringParts.clear();
		boolean templatePresent = TemplateReasons.format( reason,  this);
		if (!templatePresent) {
			collectedStringParts.add( Pair.of( reason.getText(), STYLER_STANDARD));
		}
		return collectedStringParts;
	}
	
	/**
	 * register a specific formatter 
	 * @param <E> 
	 * @param type
	 * @param formatter
	 */
	public <E extends GenericEntity> void registerFormatter(EntityType<E> type,Consumer<E> formatter) {
		formatters.put(type, formatter);
	}
	
	/**
	 * format {@link PartIdentification}
	 * @param pi - the {@link PartIdentification}
	 */
	private void formatPi(PartIdentification pi) {
		if (pi == null) {
			collectedStringParts.add( Pair.of( "n/a", STYLER_ERROR));
			return;
		}
		String classifier = pi.getClassifier();
		if (classifier != null) {
			collectedStringParts.add( Pair.of( classifier, STYLER_PART_CLASSIFIER));
		}
		collectedStringParts.add( Pair.of(":", STYLER_DELIMITER));
				
		String type = pi.getType();
		if (type != null) {
			collectedStringParts.add( Pair.of( type, STYLER_PART_TYPE));
		}
	}
		
	/**
	 * format a {@link CompiledArtifactIdentification}
	 * @param cai - the {@link CompiledArtifactIdentification}
	 */
	private void formatCai( CompiledArtifactIdentification cai) {
		if (cai == null) {
			collectedStringParts.add( Pair.of( "n/a", STYLER_ERROR));
			return;
		}
		collectedStringParts.add( Pair.of( cai.getGroupId(), STYLER_GROUPID));
		collectedStringParts.add( Pair.of( ":", STYLER_DELIMITER));
		collectedStringParts.add( Pair.of( cai.getArtifactId(), STYLER_ARTIFACTID));
		collectedStringParts.add( Pair.of( "#", STYLER_DELIMITER));
		formatVersionExpression( cai.getVersion());		
	}
	
	/**
	 * format a {@link CompiledDependencyIdentification}
	 * @param cdi - the {@link CompiledDependencyIdentification}
	 */
	private void formatCdi( CompiledDependencyIdentification cdi) {
		collectedStringParts.add( Pair.of( cdi.getGroupId(), STYLER_GROUPID));
		collectedStringParts.add( Pair.of( ":", STYLER_DELIMITER));
		collectedStringParts.add( Pair.of( cdi.getArtifactId(), STYLER_ARTIFACTID));
		collectedStringParts.add( Pair.of( "#", STYLER_DELIMITER));
		formatVersionExpression( cdi.getVersion());		
	}
	
	/**
	 * format a version or a version range - the basic {@link VersionExpression}
	 * @param ve - the {@link VersionExpression}
	 */
	private  void formatVersionExpression(VersionExpression ve) {
		collectedStringParts.add( Pair.of( ve.asString(), STYLER_VERSION));
	}	
	
	/**
	 * format a {@link VersionedArtifactIdentification}
	 * @param vai - {@link VersionedArtifactIdentification}
	 */
	private void formatVai(VersionedArtifactIdentification vai) {
		collectedStringParts.add( Pair.of( vai.getGroupId(), STYLER_GROUPID));
		collectedStringParts.add( Pair.of( ":", STYLER_DELIMITER));
		collectedStringParts.add( Pair.of( vai.getArtifactId(), STYLER_ARTIFACTID));
		collectedStringParts.add( Pair.of( "#", STYLER_DELIMITER));
		formatVersionExpression( VersionExpression.parse( vai.getVersion()));
	}
	
	@Override
	public void append(String text) {
		collectedStringParts.add(Pair.of(text, STYLER_STANDARD));		
	}

	@Override
	public void appendProperty(GenericEntity reasonEntity, Property property, GenericModelType propertyType, Object propertyValue) {
		//
		// check any experts on the reason
		Consumer<GenericEntity> specificReasonFormatter = formatters.find( reasonEntity.entityType());
		if (specificReasonFormatter != null) {
			specificReasonFormatter.accept(reasonEntity);
			return;
		}
		
		// check any experts of the property
		if (propertyType.getTypeCode() == TypeCode.entityType) {
			Consumer<GenericEntity> specificPropertyFormatter = formatters.find( (EntityType<?>) propertyType);
			if (specificPropertyFormatter != null) {
				specificPropertyFormatter.accept((GenericEntity) propertyValue);
				return;
			}
		}
		 				
		outputValue(propertyValue, propertyType);	
	}
	
	private void outputValue(Object value, GenericModelType type) {
		if (value == null) {
			collectedStringParts.add( Pair.of("<n/a>", STYLER_UNDEFINED));			
			return;
		}
		
		switch (type.getTypeCode()) {
		case objectType:
			outputValue(value, type.getActualType(value));
			break;
			
		case booleanType:
		case dateType:
		case decimalType:
		case doubleType:
		case enumType:
		case floatType:
		case integerType:
		case longType:
		case stringType:
			collectedStringParts.add( Pair.of(value.toString(), STYLER_EMPHASIS));
			break;
		case entityType:
			Consumer<GenericEntity> specificPropertyFormatter = formatters.find((EntityType<?>) type);
			if (specificPropertyFormatter != null) {
				specificPropertyFormatter.accept((GenericEntity) value);				
			}
			else {
				collectedStringParts.add( Pair.of(value.toString(), STYLER_EMPHASIS));
			}
			break;

		case listType:
		case setType: {
			int i = 0;
			GenericModelType eT = ((LinearCollectionType)type).getCollectionElementType(); 
			for (Object e : (Collection<?>)value) {
				if (i > 0) {					
					collectedStringParts.add( Pair.of(", ", STYLER_STANDARD));
				}
				outputValue(e, eT);
				i++;
			}
			break;
		}
		case mapType: {
			int i = 0;
			MapType mapType = (MapType)type;
			
			GenericModelType kT = mapType.getKeyType(); 
			GenericModelType vT = mapType.getValueType(); 
			
			for (Map.Entry<?, ?> e : ((Map<?,?>)value).entrySet()) {
				if (i > 0) {
					collectedStringParts.add( Pair.of(", ", STYLER_STANDARD));
				}
			
				outputValue(e.getKey(), kT);				
				collectedStringParts.add( Pair.of(" = ", STYLER_EMPHASIS));
				outputValue(e.getValue(), vT);
				i++;
			}
			break;
		}
		
		default:
			collectedStringParts.add( Pair.of("<n/a>", STYLER_UNDEFINED));
			break;
		}
	}

	
	public static void main(String[] args) {
		IncompleteResolution ir = IncompleteResolution.T.create();
		
		AnalysisArtifact aa = AnalysisArtifact.T.create();
		aa.setGroupId("com.braintribe.devrock.test");
		aa.setArtifactId("aa");
		aa.setVersion( "1.0.1");
		ir.getTerminals().add(aa);
		
		AnalysisDependency ad = AnalysisDependency.T.create();
		ad.setGroupId("com.braintribe.devrock.test");
		ad.setArtifactId("aa");
		ad.setVersion( "[1.0,1.1)");		
		ir.getTerminals().add(ad);
		
		ReasonStyler reasonStyler = new ReasonStyler();
		List<Pair<String,String>> process = reasonStyler.process( ir);
		System.err.println(process);
	}
	
	
	
}
