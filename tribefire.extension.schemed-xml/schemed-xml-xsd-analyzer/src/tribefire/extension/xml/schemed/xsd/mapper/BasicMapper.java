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
package tribefire.extension.xml.schemed.xsd.mapper;



import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.yellow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.HasMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.utils.lcd.ReflectionTools;

import tribefire.extension.xml.schemed.mapping.metadata.AnyProcessingTokens;
import tribefire.extension.xml.schemed.mapping.metadata.EntityTypeMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.EnumConstantMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.EnumTypeMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.ModelMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.PropertyMappingMetaData;
import tribefire.extension.xml.schemed.marshaller.commons.HasCommonTokens;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.BidirectionalLink;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.CollectionOverride;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.MappingOverride;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemaAddress;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.ShallowSubstitutingModel;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.Substitution;
import tribefire.extension.xml.schemed.model.xsd.QName;
import tribefire.extension.xml.schemed.model.xsd.Type;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.QPath;
import tribefire.extension.xml.schemed.xsd.api.analyzer.ConfigurableFromRequest;
import tribefire.extension.xml.schemed.xsd.api.mapper.metadata.MetaDataMapper;
import tribefire.extension.xml.schemed.xsd.api.mapper.name.HasTokens;
import tribefire.extension.xml.schemed.xsd.api.mapper.name.NameMapper;
import tribefire.extension.xml.schemed.xsd.api.mapper.type.TypeMapper;
import tribefire.extension.xml.schemed.xsd.mapper.metadata.MappingMetaDataExpert;
import tribefire.extension.xml.schemed.xsd.mapper.metadata.MetaDataExpert;

/**
 * an implementation of the different mappers 
 * @author pit
 *
 */
public class BasicMapper implements HasCommonTokens, HasTokens, AnyProcessingTokens, TypeMapper, NameMapper, MetaDataMapper, SpecialConstraintsTypeResolver, ConfigurableFromRequest {		
	
	private static Logger log = Logger.getLogger(BasicMapper.class);

	private final Map<Type, GmType> typeToGmTypeMap = new HashMap<>();
	private final Set<GmType> mappedTypes = new HashSet<>();
	private final Set<String> mappedTypeNames = new HashSet<>();
	
	private final Map<String, GmType> simpleTypeMap = new HashMap<>();
	private BasicModelOracle modelOracle;
	
	private final Map<String, List<GmType>> standardTypeNameToDerivedTypes = new HashMap<>();
	
	private final Map<String, GmLinearCollectionType> signatureToCollectionTypeMap = new HashMap<>();
	private String packageName;
	private Set<String> usedXsdPrefixes;
	
	private final Map<GmProperty, PropertyMappingMetaData> propertyToMappingMetadataMap = new HashMap<>();
	private final Map<GmEntityType, EntityTypeMappingMetaData> entityTypeToMappingMetadataMap = new HashMap<>();
	private final Map<GmEnumType, EnumTypeMappingMetaData> enumTypeToMappingMetadataMap = new HashMap<>();
	private final Map<GmEnumConstant, EnumConstantMappingMetaData> enumConstantToMappingMetadataMap = new HashMap<>();
	
	private final Map<GmMetaModel, ModelMappingMetaData> modelToMappingMetadataMap = new HashMap<>();

	private final Map<String, GmType> topLevelElementToTypeMap = new HashMap<>();
	
	private final Map<String, Integer> nameToUniqueNumberMap = new HashMap<>();
	
	private final List<String> protectedPropertyNames = getProtectedPropertyNames();
	private final List<String> entityTypesStandingInForSimpleTypes = getEntityTypesAsSimpleTypes();
	
	private final Map<String, String> nameOverrides = new HashMap<>();
	private final Set<String> collectionOverridesAsSet = new HashSet<>();
	private final Map<String,String> bidirectionals = new HashMap<>();
	private final Map<String, GmEntityType> substitutions = new HashMap<>();
	private final Set<GmType> actuallyRequestedSubstitutions = new HashSet<>();
	
	private GmType anyType, anyAttributeType;
	private boolean verbose;
	
	@Configurable
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	 	
	@Override
	public void parametrize(SchemedXmlXsdAnalyzerRequest request) {
		packageName = request.getTopPackageName();
		// extract the mapping overrides (overriding names)
		List<MappingOverride> mappingOverrides = request.getMappingOverrides();
		for (MappingOverride override : mappingOverrides) {
			SchemaAddress schemaAddress = override.getSchemaAddress();
			String schemaTypeName = schemaAddress.getParent();
			String schemaPropertyName = schemaAddress.getElement();
			
			String propertyNameToUse = schemaPropertyName != null ? schemaPropertyName : "";
			String typeNameToUse = schemaTypeName != null ? schemaTypeName : "";
			
			String key = typeNameToUse + ":" + propertyNameToUse;						
			String overridingName = override.getNameOverride();
			nameOverrides.put( key, overridingName);
		}
		// extract the collection overrides (which ones should be set rather than list)
		List<CollectionOverride> collectionOverrides = request.getCollectionOverrides();
		for (CollectionOverride override : collectionOverrides) {
			if (override.getCollectionAsSet()) {
				SchemaAddress schemaAddress = override.getSchemaAddress();
				String key = schemaAddress.getParent() + ":" +schemaAddress.getElement();
				collectionOverridesAsSet.add(key);
			}
		}		
		// extract the bidirectionals.. 
		List<BidirectionalLink> bidirectionalLinks = request.getBidirectionalLinks();
		for (BidirectionalLink link : bidirectionalLinks) {
			SchemaAddress schemaAddress = link.getSchemaAddress();
			String key = schemaAddress.getParent() + ":" +schemaAddress.getElement();
			bidirectionals.put(key, link.getBacklinkProperty());
		}
		
		// substitutes..
		for (ShallowSubstitutingModel substitutionModel : request.getShallowSubstitutingModels()) {
			for (Substitution substitution : substitutionModel.getSubstitutions()) {
				GmEntityType entityType = GmEntityType.T.create();
				entityType.setTypeSignature( substitution.getReplacementSignature());
				entityType.setGlobalId( substitution.getReplacementGlobalId());
				
				String declaringModelAsString = substitutionModel.getDeclaringModel();				
				VersionedArtifactIdentification artifact = VersionedArtifactIdentification.parse(declaringModelAsString);

				GmMetaModel declaringModel = GmMetaModel.T.create();
				declaringModel.setName( artifact.getGroupId() + ":" + artifact.getArtifactId());
				declaringModel.setVersion(artifact.getVersion());
				entityType.setDeclaringModel( declaringModel);
				
				SchemaAddress schemaAddress = substitution.getSchemaAddress();
				String schemaTypeName = schemaAddress.getParent();
				String schemaPropertyName = schemaAddress.getElement();
				
				String propertyNameToUse = schemaPropertyName != null ? schemaPropertyName : "";
				String typeNameToUse = schemaTypeName != null ? schemaTypeName : "";
				String key = typeNameToUse + ":" + propertyNameToUse;
				substitutions.put( key, entityType);								
			}
		}
		
	}

	public void setUsedXsdPrefixes( Set<String> usedXsdPrefixes) {
		this.usedXsdPrefixes = usedXsdPrefixes;	
	}

	private BasicModelOracle acquireRootModelOracle() {
		if (modelOracle != null) {
			return modelOracle;
		}
		GenericModelTypeReflection reflection = GMF.getTypeReflection();
		Model model = reflection.getModel("com.braintribe.gm:root-model");
		GmMetaModel metaModel = model.getMetaModel();
		
		modelOracle = new BasicModelOracle( metaModel);
		
		return modelOracle;
	}
	
	@Override
	public GmEntityType acquireGenericEntityType() {
		return acquireRootModelOracle().getEntityTypeOracle(GenericEntity.T).asGmEntityType();		
	}

	private void setupBasicTypeSystem() {
		
	
		// put 
		GmType gmStringType = acquireRootModelOracle().findGmType( SimpleTypes.TYPE_STRING);
		injectType( "string", gmStringType);

		simpleTypeMap.put( "string", gmStringType);			
		simpleTypeMap.put( "token", gmStringType);				
		simpleTypeMap.put( "hexBinary", gmStringType);
		simpleTypeMap.put( "base64Binary", gmStringType);
		simpleTypeMap.put( "anyURI", gmStringType);
		simpleTypeMap.put( "NOTATION", gmStringType);
		simpleTypeMap.put( "QName", gmStringType);
		simpleTypeMap.put( "duration", gmStringType);
		// g.. gee this is simple fix
		simpleTypeMap.put( "gDay", gmStringType);
		simpleTypeMap.put( "gMonth", gmStringType);
		simpleTypeMap.put( "gYear", gmStringType);
		
		GmType gmNmTokenType = createNmToken();
		simpleTypeMap.put ("NMTOKEN", gmNmTokenType);
		injectType("string", gmNmTokenType);
		
		GmType gmTokenType = createToken();
		simpleTypeMap.put ("TOKEN", gmTokenType);
		injectType("string", gmTokenType);
		
		GmType gmLanguageType = createLanguage();
		simpleTypeMap.put ("language", gmLanguageType);
		injectType("string", gmLanguageType);
		
		GmType gmNormalizedStringType = createNormalizedString();
		simpleTypeMap.put ("normalizedString", gmNormalizedStringType);
		injectType("string", gmNormalizedStringType);
		
		GmType gmNcNameType = createNcName();
		simpleTypeMap.put ("NCName", gmNcNameType);
		injectType("string", gmNcNameType);
		
		GmType gmIdType = createId();
		simpleTypeMap.put ("ID", gmIdType);
		injectType("string", gmIdType);
		
		GmType gmIdRefType = createIdRef();
		simpleTypeMap.put ("IDREF", gmIdRefType);
		injectType("string", gmIdRefType);
		
		
		GmType gmDoubleType = acquireRootModelOracle().findGmType( SimpleTypes.TYPE_DOUBLE);
		simpleTypeMap.put( "double", gmDoubleType);
		injectType("double", gmDoubleType);
		
		GmType gmDateType = acquireRootModelOracle().findGmType( SimpleTypes.TYPE_DATE);
		injectType("date", gmDateType);
		simpleTypeMap.put( "date", gmDateType);
		simpleTypeMap.put( "time", gmDateType);
		simpleTypeMap.put( "dateTime", gmDateType);
		
	
		GmType gmDecimalType = acquireRootModelOracle().findGmType( SimpleTypes.TYPE_DECIMAL);
		simpleTypeMap.put( "decimal", gmDecimalType);
		injectType("decimal", gmDecimalType);
		
		GmType gmIntegerType = acquireRootModelOracle().findGmType( SimpleTypes.TYPE_INTEGER);
		injectType( "integer", gmIntegerType);
		simpleTypeMap.put( "int", gmIntegerType);
		simpleTypeMap.put( "unsignedShort", gmIntegerType);
		simpleTypeMap.put( "unsignedInt", gmIntegerType);
		simpleTypeMap.put( "short", gmIntegerType);
		simpleTypeMap.put( "byte", gmIntegerType);
		simpleTypeMap.put( "unsignedByte", gmIntegerType);

		simpleTypeMap.put( "integer", gmIntegerType);
		simpleTypeMap.put( "negativeInteger", gmIntegerType);
		simpleTypeMap.put( "nonNegativeInteger", gmIntegerType);
		simpleTypeMap.put( "positiveInteger", gmIntegerType);
		simpleTypeMap.put( "nonPositiveInteger", gmIntegerType);

		GmType gmLongType = acquireRootModelOracle().findGmType( SimpleTypes.TYPE_LONG);
		injectType("long", gmLongType);
		simpleTypeMap.put( "long", gmLongType);
		simpleTypeMap.put( "unsignedLong", gmLongType);
		
		
		GmType gmBooleanType = acquireRootModelOracle().findGmType( SimpleTypes.TYPE_BOOLEAN);
		injectType("boolean", gmBooleanType);
		simpleTypeMap.put( "boolean", gmBooleanType);
		
		GmType gmFloatType = acquireRootModelOracle().findGmType( SimpleTypes.TYPE_FLOAT);
		injectType("float", gmFloatType);
		simpleTypeMap.put( "float", gmFloatType);	
		
		acquireAnyType();

	}
	
	private void injectType( String xsdName, GmType gmType) {
		
		List<GmType> types = standardTypeNameToDerivedTypes.get(xsdName);
		if (types == null) {
			types = new ArrayList<>();
			standardTypeNameToDerivedTypes.put(xsdName, types);
		}
		types.add(gmType);
		
	}
		
	
	
	@Override
	public boolean getIsCollectionTypeOverridenAsSet(String parentName, String propertyName) {
		String key = parentName + ":" + propertyName;
		return collectionOverridesAsSet.contains(key);
	}
	

	@Override
	public String getBacklinkPropertyToInjectFor(String parentName, String propertyName) {
		String key = parentName + ":" + propertyName;		
		return bidirectionals.get(key);
	}

	@Override
	public GmLinearCollectionType acquireCollectionType(GmType elementType, boolean asSet) {
		String typesignature;
		if (asSet) {
			typesignature = "set<" + elementType.getTypeSignature() + ">";
		}
		else {
			typesignature = "list<" + elementType.getTypeSignature() + ">";
		}
		GmLinearCollectionType matchingType = signatureToCollectionTypeMap.get(typesignature);
		if (matchingType == null) {
			if (asSet) {
				matchingType = GmSetType.T.create();
			}
			else {
				matchingType = GmListType.T.create();
			}
			matchingType.setTypeSignature(typesignature);
			matchingType.setGlobalId( JavaTypeAnalysis.typeGlobalId(typesignature));
			matchingType.setElementType( elementType);
			signatureToCollectionTypeMap.put(typesignature, matchingType);
			mappedTypes.add( matchingType);
		}
		return matchingType;
	}
	
	

	@Override
	public boolean isTypeNameAvailable(String name) {
		return !mappedTypeNames.contains(name);		
	}

	@Override
	public void initializeTypeSystem() {
		setupBasicTypeSystem();		
	}

	@Override
	public String getMappedNameOfType(GmType type) {
		for (Entry<String, GmType> entry : simpleTypeMap.entrySet()) {
			if (entry.getValue() == type) 
				return entry.getKey();
		}
		return null;
	}

	@Override
	public String getSimpleStringBase(GmType type) {
		for (Entry<String, List<GmType>> entry : standardTypeNameToDerivedTypes.entrySet()) {
			if (entry.getValue().contains(type)) {
				return entry.getKey();
			}
		}
		log.warn("no association found for type [" + type.getTypeSignature() + "]");
		return null;
	}
	/*
	 * 
	 * TYPE MAPPING STUFF
	 * 
	 */

	@Override
	public GmType createNmToken() {
		List<MetaData> metadata = new ArrayList<>();
		metadata.add( MetaDataExpert.createMetaDataforPattern( token_Pattern));
		metadata.add( MetaDataExpert.createMetaDataforPattern( nmToken_Pattern));
		metadata.add( MappingMetaDataExpert.createInjectedEntityTypeMetaData( "NMTOKEN"));
		return createSpecializedType(this, "NMTOKEN", acquireRootModelOracle().findGmType( SimpleTypes.TYPE_STRING), metadata);
	}

	@Override
	public GmType createLanguage() {
		List<MetaData> metadata = new ArrayList<>();
		metadata.add( MetaDataExpert.createMetaDataforPattern( language_Pattern));
		metadata.add( MappingMetaDataExpert.createInjectedEntityTypeMetaData( "language"));
		return createSpecializedType(this, "language", acquireRootModelOracle().findGmType( SimpleTypes.TYPE_STRING), metadata);
	}

	@Override
	public GmType createToken() {
		List<MetaData> metadata = new ArrayList<>();
		metadata.add( MetaDataExpert.createMetaDataforPattern( token_Pattern));	
		metadata.add( MappingMetaDataExpert.createInjectedEntityTypeMetaData( "TOKEN"));
		return createSpecializedType(this, "TOKEN", acquireRootModelOracle().findGmType( SimpleTypes.TYPE_STRING), metadata);
	}

	@Override
	public GmType createNormalizedString() {
		List<MetaData> metadata = new ArrayList<>();
		metadata.add( MetaDataExpert.createMetaDataforPattern( normalizedString_Pattern));
		metadata.add( MappingMetaDataExpert.createInjectedEntityTypeMetaData( "normalizedString"));
		return createSpecializedType(this, "normalizedString", acquireRootModelOracle().findGmType( SimpleTypes.TYPE_STRING), metadata);		
	}

	@Override
	public GmType createNcName() {
		List<MetaData> metadata = new ArrayList<>();
		metadata.add( MetaDataExpert.createMetaDataforPattern( ncName_Pattern));	
		metadata.add( MappingMetaDataExpert.createInjectedEntityTypeMetaData( "NCName"));
		return createSpecializedType(this, "NCName", acquireRootModelOracle().findGmType( SimpleTypes.TYPE_STRING), metadata);	
	}		

	@Override
	public GmType createId() {
		List<MetaData> metadata = new ArrayList<>();
		metadata.add( MetaDataExpert.createMetaDataforPattern( ncName_Pattern));	
		metadata.add( MappingMetaDataExpert.createInjectedEntityTypeMetaData( "ID"));
		return createSpecializedType(this, "ID", acquireRootModelOracle().findGmType( SimpleTypes.TYPE_STRING), metadata);		
	}

	@Override
	public GmType createIdRef() {
		List<MetaData> metadata = new ArrayList<>();
		metadata.add( MetaDataExpert.createMetaDataforPattern( ncName_Pattern));
		metadata.add( MappingMetaDataExpert.createInjectedEntityTypeMetaData( "IDREF"));
		return createSpecializedType(this, "IDREF", acquireRootModelOracle().findGmType( SimpleTypes.TYPE_STRING), metadata);
	}
	
	public GmType createAnyAttributeType( String packageName) {
		GmEntityType anyAttributeType = GmEntityType.T.create();
		anyAttributeType.setTypeSignature( packageName + "." + TYPE_ANY_ATTRIBUTE_TYPE);
		
		GmType stringType = acquireRootModelOracle().findGmType( SimpleTypes.TYPE_STRING);
		
		GmProperty name = GmProperty.T.create();
		name.setName( TYPE_ANY_NAME);
		name.setType(stringType);
		name.setDeclaringType(anyAttributeType);
		
		PropertyMappingMetaData namedMappingMetaData = acquireMetaData(name);
		namedMappingMetaData.setIsMultiple(false);
		namedMappingMetaData.setXsdName( MD_ANY_NAME);
		namedMappingMetaData.setActualXsdType("string");
		anyAttributeType.getProperties().add( name);
		
		GmProperty value = GmProperty.T.create();
		value.setName( TYPE_ANY_VALUE);
		value.setType(stringType);
		value.setDeclaringType(anyAttributeType);
		
		PropertyMappingMetaData valueMappingMetaData = acquireMetaData(name);
		valueMappingMetaData.setIsMultiple(false);
		valueMappingMetaData.setXsdName( MD_ANY_VALUE);
		valueMappingMetaData.setActualXsdType("string");
		
		anyAttributeType.getProperties().add(value);
		
		EntityTypeMappingMetaData entityTypeMappingMetaData = acquireMetaData(anyAttributeType);
		//entityTypeMappingMetaData.setHasAnyType(true);
		entityTypeMappingMetaData.setXsdName( MD_ANY_ATTRIBUTES);
		
		mappedTypes.add(anyAttributeType);
				
		return anyAttributeType;
		
	}

	@Override
	public GmType createAnyType(String packageName) {
		
		GmType anyAttributeType = acquireAnyAttributeType();
		
		GmEntityType anyType = GmEntityType.T.create();
		anyType.setTypeSignature( packageName + "." + TYPE_ANY_TYPE);
		
		GmType stringType = acquireRootModelOracle().findGmType( SimpleTypes.TYPE_STRING);
		
		GmProperty name = GmProperty.T.create();
		name.setName( TYPE_ANY_NAME);
		name.setType(stringType);
		name.setDeclaringType(anyType);
		
		PropertyMappingMetaData namedMappingMetaData = acquireMetaData(name);
		namedMappingMetaData.setIsMultiple(false);
		namedMappingMetaData.setXsdName( MD_ANY_NAME);
		namedMappingMetaData.setActualXsdType("string");
		anyType.getProperties().add( name);
		
		String typesignature = "list<" + anyAttributeType.getTypeSignature() + ">";
		
		
		GmProperty attributes = GmProperty.T.create();
		GmListType attributesType= GmListType.T.create();
		attributesType.setTypeSignature( typesignature);
		attributesType.setGlobalId( JavaTypeAnalysis.typeGlobalId( typesignature));
		attributesType.setElementType( anyAttributeType);
		attributes.setType(attributesType);
		attributes.setName( TYPE_ANY_ATTRIBUTES);
		anyType.getProperties().add(attributes);
		attributes.setDeclaringType(anyType);
		
		PropertyMappingMetaData propertyMappingMetaData = acquireMetaData(attributes);
		propertyMappingMetaData.setIsMultiple(true);
		propertyMappingMetaData.setXsdName( MD_ANY_ATTRIBUTES);
				
		
		GmEntityType genericEntityGmEntityType = acquireGenericEntityType();
		
		
		typesignature = "list<" + GenericEntity.T.getTypeSignature() + ">";
		
		GmProperty properties = GmProperty.T.create();
		GmListType propertiesType= GmListType.T.create();			
		propertiesType.setElementType( anyType);
		propertiesType.setTypeSignature(typesignature);
		propertiesType.setGlobalId( JavaTypeAnalysis.typeGlobalId(typesignature));
		propertiesType.setElementType( genericEntityGmEntityType);
		properties.setType(propertiesType);
		properties.setName( TYPE_ANY_PROPERTIES);
		
		anyType.getProperties().add( properties);
		properties.setDeclaringType(anyType);
		
		PropertyMappingMetaData propertiesMappingMetaData = acquireMetaData(properties);
		propertiesMappingMetaData.setIsMultiple(true);
		propertiesMappingMetaData.setXsdName( MD_ANY_PROPERTIES);
		propertiesMappingMetaData.setActualXsdType( "anyType");
		propertiesMappingMetaData.setApparentXsdType( "anyType");
		
		
		GmProperty valueProperty = GmProperty.T.create();
		valueProperty.setDeclaringType(anyType);
		valueProperty.setType( stringType);
		valueProperty.setName("value");
		anyType.getProperties().add(valueProperty);
		
		PropertyMappingMetaData valueMappingMetaData = acquireMetaData(valueProperty);
		valueMappingMetaData.setIsValue(true);
		valueMappingMetaData.setXsdName( MD_ANY_VALUE);
		valueMappingMetaData.setActualXsdType("string");
		valueMappingMetaData.setApparentXsdType("string");
					
		
		EntityTypeMappingMetaData entityTypeMappingMetaData = acquireMetaData(anyType);
		entityTypeMappingMetaData.setHasAnyType(true);		
		entityTypeMappingMetaData.setXsdName(MD_ANY_TYPE);
		
		mappedTypes.add(anyType);
				
		return anyType;
	}
	
	@Override
	public GmType acquireAnyAttributeType() {
		if (anyAttributeType != null) {
			return anyAttributeType;
		}
		anyAttributeType = createAnyAttributeType(PACKAGE_STANDARD_XML_TYPES);//packageName + ".xml");//INTERNAL_TYPE_PACKAGE);
		return anyAttributeType;
	}
	

	@Override
	public GmType acquireAnyType() {
		if (anyType != null) {
			return anyType;
		}
		anyType = createAnyType( PACKAGE_STANDARD_XML_TYPES);//packageName + ".xml");//INTERNAL_TYPE_PACKAGE);
		return anyType;
	}
	private String ensurePackageName(String name) { 
		if (entityTypesStandingInForSimpleTypes.contains(name)) {
			//return INTERNAL_TYPE_PACKAGE;//
			//return packageName + ".xml";
			return PACKAGE_STANDARD_XML_TYPES;
		}		
		return packageName;				
	}
	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.type.TypeMapper#generateGmEntityType(com.braintribe.marshaller.schemedXml.xsd.QName, com.braintribe.marshaller.schemedXml.xsd.Type, java.lang.String)
	 */
	@Override
	public GmEntityType generateGmEntityType(QPath qpath, Type type, String typeName) {
		GmEntityType entityType = GmEntityType.T.create();			
		String packagename = ensurePackageName( typeName);
		if (qpath.getPath().length() > 0) {
			entityType.setTypeSignature(packagename + "." + qpath.getPath() + "." + typeName);
		}
		else {
			entityType.setTypeSignature(packagename + "." + typeName);
		}
		entityType.setGlobalId( JavaTypeAnalysis.typeGlobalId( entityType.getTypeSignature()));		
		if (type != null) {
			typeToGmTypeMap.put(type, entityType);
			if (verbose) {
				println("Mapping entity type [" + entityType.getTypeSignature() + "], type [" + type.getName() + "], name [" + typeName + "]");
			}			
		}
		else {
			if (verbose) {
				println( yellow("NOT Mapping entity type [" + entityType.getTypeSignature() + "], name [" + typeName + "] as no type is available"));
			}
		}
		mappedTypes.add(entityType);
		mappedTypeNames.add( typeName);
		return entityType;
	}
	
	
	@Override
	public GmEntityType generateGmEntityTypeForSimpleType(QPath name, Type type, String typeName, GmType propertyType) {	
		typeName = assertNonCollidingTypeName( typeName);
		GmEntityType gmEntityType = generateGmEntityType(name, type, typeName);
		GmProperty property = generateGmProperty( VIRTUAL_VALUE_PROPERTY);
		property.setDeclaringType(gmEntityType);
		property.setType(propertyType);
		property.setGlobalId( JavaTypeAnalysis.propertyGlobalId( gmEntityType.getTypeSignature(), VIRTUAL_VALUE_PROPERTY));
		gmEntityType.getProperties().add(property);
		//mappedTypes.add(gmEntityType);
		PropertyMappingMetaData propertyMetaData = acquireMetaData(property);
		propertyMetaData.setIsValue(true);
		return gmEntityType;
	}
	
	

	@Override
	public GmEntityType remapGmEntityType(Type type, GmEntityType entityType, String name) {
		
		// switch to new type, i.e. transfer data from old type, and the remove old type
		GmEntityType currentMappedType = (GmEntityType) typeToGmTypeMap.get( type);
		if (currentMappedType != null) {
			entityType.getMetaData().addAll( currentMappedType.getMetaData());
			EntityTypeMappingMetaData entityTypeMappingMetaData = entityTypeToMappingMetadataMap.get(currentMappedType);
			entityTypeToMappingMetadataMap.put(entityType, entityTypeMappingMetaData);
			mappedTypes.remove(currentMappedType);
			entityTypeToMappingMetadataMap.remove(currentMappedType);		
		}
		else {
			EntityTypeMappingMetaData entityTypeMappingMetaData = (EntityTypeMappingMetaData) entityType.getMetaData().stream().filter( md ->  (md instanceof EntityTypeMappingMetaData) ? true : false).findFirst().orElse(null);
			if (entityTypeMappingMetaData != null) {
				entityTypeToMappingMetadataMap.put(entityType, entityTypeMappingMetaData);
			}
			
		}

		mappedTypes.add(entityType);
		
		String signature = entityType.getTypeSignature();
		String signaturePrefix = signature.substring(0, signature.lastIndexOf('.'));
		String newSignature = signaturePrefix + "." + name;
		entityType.setTypeSignature(newSignature);
		typeToGmTypeMap.put(type, entityType);
		if (verbose) {
			println( yellow( "remapping [" + signature + "] to [" + newSignature + "], replacing internal map of [" + (type != null ? type.getName() : null) + "]"));
		}
		return entityType;
	}
	
	

	@Override
	public void unmapGmEntityType(Type type, GmEntityType gmEntityTypeToDrop) {
		return;
		/*
		GmType existingGmType = typeToGmTypeMap.get( type);
		if (existingGmType != gmEntityTypeToDrop) {
			System.out.println("NOT removing entity type [" + gmEntityTypeToDrop.getTypeSignature() + "] mapped to name [" + type.getName() + "] as no it's mapped to [" + existingGmType.getTypeSignature() +"]");		
		}
		else {
			typeToGmTypeMap.remove(type);
			mappedTypes.remove(gmEntityTypeToDrop);
		}
		*/
	}

	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.type.TypeMapper#generateGmEnumType(com.braintribe.marshaller.schemedXml.xsd.QName, com.braintribe.marshaller.schemedXml.xsd.Type, java.lang.String)
	 */
	@Override
	public GmEnumType generateGmEnumType(QPath qname, Type type, String typeName) {	
		GmEnumType enumType = GmEnumType.T.create();		
		String packagename = ensurePackageName( typeName);
		if (qname.getPath().length() > 0) {
			enumType.setTypeSignature(packagename + "." + qname.getPath() + "." + typeName);
		}
		else {
			enumType.setTypeSignature(packagename + "." + typeName);
		}
		enumType.setGlobalId( JavaTypeAnalysis.typeGlobalId( enumType.getTypeSignature()));
		if (type != null) {
			typeToGmTypeMap.put(type, enumType);
			if (verbose) {
				println("Mapping enum type [" + enumType.getTypeSignature() + "], type [" + type.getName() + "], name [" + typeName + "]");
			}
		}
		else {
			if (verbose) {
				println(yellow("NOT Mapping enum type [" + enumType.getTypeSignature() + "], name [" + typeName + "] as no type is available"));
			}
		}		
		mappedTypes.add(enumType);
		mappedTypeNames.add( typeName);
		return enumType;
	}
	
	


	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.type.TypeMapper#generateGmEnumConstant(java.lang.String, com.braintribe.model.meta.GmEnumType)
	 */
	@Override
	public GmEnumConstant generateGmEnumConstant(String name, GmEnumType declaringType) {
		GmEnumConstant constant = GmEnumConstant.T.create();
		constant.setDeclaringType(declaringType);
		declaringType.getConstants().add(constant);
		constant.setName(name);
		constant.setGlobalId( JavaTypeAnalysis.constantGlobalId(declaringType.getTypeSignature(), name));
		return constant;
	}


	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.type.TypeMapper#generateGmProperty(java.lang.String)
	 */
	@Override
	public GmProperty generateGmProperty(String name) {
		GmProperty gmProperty = GmProperty.T.create();	
		gmProperty.setName(name);		
		return gmProperty;
	}
	
	

	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.type.TypeMapper#lookupStandardSimpleType(com.braintribe.marshaller.schemedXml.xsd.QName)
	 */
	@Override
	public GmType lookupStandardSimpleType(QName qname) {
		if (usedXsdPrefixes.contains(qname.getPrefix())) {
			return simpleTypeMap.get( qname.getLocalPart());
		}
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.type.TypeMapper#lookupType(com.braintribe.marshaller.schemedXml.xsd.Type)
	 */
	@Override
	public GmType lookupType(Type type) {	
		return typeToGmTypeMap.get(type);
	}

	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.type.TypeMapper#getExtractedTypes()
	 */
	@Override
	public Collection<GmType> getExtractedTypes() {		
		return mappedTypes;	
	}

	@Override
	public Map<String, GmType> getTopLevelElementToTypeAssociation() {
		return topLevelElementToTypeMap;
	}
	
	
	/*
	 * 
	 * NAME MAPPING STUFF
	 *  
	 */

	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.name.NameMapper#generateCollectionName(java.lang.String)
	 */
	@Override
	public String generateCollectionName(String propertyName) {
		String domName = generateValidJavaName( propertyName);
		String suffix;
		if (domName.endsWith( "s")) {
			suffix = "ses";
		}
		else {
			suffix = "s";
		}
	
		return domName = domName.substring(0, 1).toLowerCase() + domName.substring(1) + suffix;
	
	}


	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.name.NameMapper#generateJavaCompatiblePropertyName(java.lang.String)
	 */
	@Override
	public String generateJavaCompatiblePropertyName(String name) {
		String proposedName = name;
		 if (protectedPropertyNames.contains( proposedName.toUpperCase())) {
			 proposedName = XML_NAME_PREFIX + proposedName;
		 }			
		return generateValidJavaName(proposedName);
	}


	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.name.NameMapper#generateJavaCompatibleTypeName(java.lang.String)
	 */
	@Override
	public String generateJavaCompatibleTypeName(String name) {		
		String validJavaName = generateValidJavaName(name);
		return validJavaName.substring(0, 1).toUpperCase() + validJavaName.substring(1);
	}


	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.name.NameMapper#generateJavaCompatibleEnumValues(java.util.List)
	 */
	@Override
	public Map<String, String> generateJavaCompatibleEnumValues(List<String> enumeration) {
		Map<String, String> enumValueTransalationMap = new HashMap<>();
		for (String input : enumeration) {
			String output;
			if (getProtectedEnumValues().contains(input) || getProtectedPropertyNames().contains(input)) {
				output = "_"  + input;
			}
			else {
				if (input.length() == 0) {
					output = "_";
				}
				else {
					output = input.replaceAll( enumReplacePattern, SPACE_REPLACER);
				}
			}
			enumValueTransalationMap.put( output, input);			
		}
		return enumValueTransalationMap;
	}
	
	private String generateUniqueSuffix( String prefix) {
		Integer number = nameToUniqueNumberMap.get(prefix);
		if (number == null) {
			number = 0;
			nameToUniqueNumberMap.put(prefix, number);
			return "";
		}
		else {
			number++;
			nameToUniqueNumberMap.put(prefix, number);
		}
		return "_" + number.toString(); 	
	}
	
	@Override
	public String generateJavaCompatibleTypeNameForVirtualPropertyType(String propertyName) {
		String prefix = VIRTUAL_TYPE_PREFIX + propertyName;
		String suffix = generateUniqueSuffix(prefix);
		return generateValidJavaName(prefix + suffix);	
	}
	
	@Override
	public String generateJavaCompatibleTypeNameForVirtualSequenceType() {
		String prefix = VIRTUAL_TYPE_PREFIX + SEQUENCE;
		String suffix = generateUniqueSuffix(prefix);
		return generateValidJavaName(prefix + suffix);	
	}
	
	@Override
	public String generateJavaCompatibleTypeNameForVirtualChoiceType(String name) {
		if (name != null) {			
			String prefix = VIRTUAL_TYPE_PREFIX + CHOICE + "_" + name;
			String suffix = generateUniqueSuffix(prefix);
			return generateValidJavaName(prefix  + suffix);
		}
		else {
			String prefix = VIRTUAL_TYPE_PREFIX + CHOICE;
			String suffix = generateUniqueSuffix(prefix);
			return generateValidJavaName( prefix + suffix);
		}
	}

	@Override
	public String generateJavaCompatibleTypeNameForVirtualRestrictionType(String baseName) {
		String prefix = VIRTUAL_TYPE_PREFIX + baseName + "_" + RESTRICTION;
		String suffix = generateUniqueSuffix(prefix);
		return generateValidJavaName(prefix  + suffix);
	}
	

	@Override
	public String generateJavaCompatibleTypeNameForVirtualChoiceElementType(String name) {
		String prefix = VIRTUAL_TYPE_PREFIX + CHOICE + "_" + name;
		String suffix = generateUniqueSuffix(prefix);
		return generateValidJavaName(prefix  + suffix);		
	}

	@Override
	public String generateJavaCompatibleTypeNameForVirtualChoiceGroupType() {
		String prefix = VIRTUAL_TYPE_PREFIX + CHOICE + "_" + GROUP;
		String suffix = generateUniqueSuffix(prefix);
		return generateValidJavaName(prefix  + suffix);
		
	}

	@Override
	public String generateJavaCompatibleTypeNameForVirtualChoiceSequenceType() {
		String prefix = VIRTUAL_TYPE_PREFIX + CHOICE + "_" + SEQUENCE;
		String suffix = generateUniqueSuffix(prefix);
		return generateValidJavaName(prefix + suffix);
	}

	@Override
	public String generateJavaCompatibleTypeNameForVirtualChoiceChoiceType() {
		String prefix = VIRTUAL_TYPE_PREFIX + CHOICE + "_" + CHOICE;
		String suffix = generateUniqueSuffix(prefix);
		return generateValidJavaName(prefix  + suffix);
	}

	@Override
	public String generateJavaCompatibleTypeNameForVirtualType(String name) {
		String prefix = VIRTUAL_TYPE_PREFIX + TYPE + "_" + name;
		String suffix = generateUniqueSuffix(prefix);
		return generateValidJavaName(prefix  + suffix);
	}
	
	
	@Override
	public String generateJavaCompatibleTypeNameForVirtualComplexRestrictedType(String base) {
		String prefix = VIRTUAL_TYPE_PREFIX + TYPE + "_" + base + "_" + RESTRICTION;
		String suffix = generateUniqueSuffix(prefix);
		return generateValidJavaName(prefix + suffix);
	}

	@Override
	public String generateJavaCompatibleTypeNameForVirtualComplexExtendedType(String base) {
		String prefix = VIRTUAL_TYPE_PREFIX + TYPE + "_" + base + "_" + EXTENSION;
		String suffix = generateUniqueSuffix(prefix);
		return generateValidJavaName(prefix + suffix);
	}

	/**
	 * generate a name that can be used within Java 
	 * @param domName - the name as it appears in XSD
	 * @return - the sanitized string
	 */
	private String generateValidJavaName( String domName) {		
		String result = domName;
		if (result.contains( ":")) {
			result = result.substring( result.indexOf(':')+1);
		}
		if (result.contains(".")) {
			result = result.replace(".", SPACE_REPLACER);
		}
		if (result.contains("-")) {
			String [] parts = result.split( "-");
			StringBuffer buffer = new StringBuffer();
			for (String part : parts) {
				if (buffer.length() > 0) 
					buffer.append( part.substring(0, 1).toUpperCase() + part.substring(1));
				else
					buffer.append( part);
			}
			result = buffer.toString();			
		} 			
		return ReflectionTools.ensureStableBeanPropertyName( result);	
	}
	
	/*
	 * 
	 * METADATA MAPPING STUFF
	 *  
	 */	
	@Override
	public PropertyMappingMetaData acquireMetaData( GmProperty property) {
		PropertyMappingMetaData md = propertyToMappingMetadataMap.get(property);
		if (md == null) {
			md = PropertyMappingMetaData.T.create();
			propertyToMappingMetadataMap.put(property, md);
			property.getMetaData().add( md);
			md.setProperty(property);
		}
		return md;
	}
	
	@Override
	public EntityTypeMappingMetaData acquireMetaData( GmEntityType type) {
		EntityTypeMappingMetaData md = entityTypeToMappingMetadataMap.get(type);
		if (md == null) {
			md = EntityTypeMappingMetaData.T.create();
			entityTypeToMappingMetadataMap.put(type, md);
			type.getMetaData().add( md);
			md.setType(type);
		}
		return md;
	}
	
	@Override
	public EnumTypeMappingMetaData acquireMetaData( GmEnumType type) {
		EnumTypeMappingMetaData md = enumTypeToMappingMetadataMap.get(type);
		if (md == null) {
			md = EnumTypeMappingMetaData.T.create();
			enumTypeToMappingMetadataMap.put(type, md);
			type.getMetaData().add( md);
			md.setType(type);
		}
		return md;
	}
	@Override
	public EnumConstantMappingMetaData acquireMetaData( GmEnumConstant constant) {
		EnumConstantMappingMetaData md = enumConstantToMappingMetadataMap.get(constant);
		if (md == null) {
			md = EnumConstantMappingMetaData.T.create();
			enumConstantToMappingMetadataMap.put(constant, md);
			constant.getMetaData().add( md);
			md.setConstant(constant);
		}
		return md;
	}
	
	@Override
	public ModelMappingMetaData acquireMetaData( GmMetaModel metamodel) {
		ModelMappingMetaData md = modelToMappingMetadataMap.get(metamodel);
		if (md == null) {
			md = ModelMappingMetaData.T.create();
			modelToMappingMetadataMap.put( metamodel, md);
			metamodel.getMetaData().add(md);
			md.setModel(metamodel);
		}
		return md;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.metadata.MetaDataMapper#mapProperty(com.braintribe.model.meta.GmProperty, int, boolean, java.lang.String, com.braintribe.marshaller.schemedXml.xsd.QName, com.braintribe.marshaller.schemedXml.xsd.QName)
	 */
	
	public PropertyMappingMetaData mapPropertyOld(GmProperty gmProperty, int index, boolean asAttribute, String name, QName apparentXsdType, QName actualXsdType) {
		PropertyMappingMetaData metaData = acquireMetaData(gmProperty);
		metaData.setProperty(gmProperty);
		metaData.setIsAttribute(asAttribute);
		metaData.setXsdName(name);
		metaData.setApparentXsdType(apparentXsdType.getLocalPart());
		metaData.setActualXsdType(actualXsdType.getLocalPart());
		metaData.setGlobalId("meta:propertyMapping" + gmProperty.getName() + "@" + gmProperty.getDeclaringType().getTypeSignature());
		GmType gmType = gmProperty.getType();
		if (gmType instanceof GmLinearCollectionType) {
			GmType elementType = ((GmLinearCollectionType) gmType).getElementType();
			metaData.setElementType(elementType);
		}
		
		return metaData;
	}
	
	
	public PropertyMappingMetaData mapPropertyOld(GmProperty gmProperty, String name, boolean asAttribute, QName apparentXsdType, QName actualXsdType) {
		PropertyMappingMetaData metaData = acquireMetaData(gmProperty);
		metaData.setXsdName(name);
		metaData.setIsAttribute(asAttribute);
		metaData.setApparentXsdType( apparentXsdType.getLocalPart());
		metaData.setActualXsdType( actualXsdType != null ? actualXsdType.getLocalPart() : apparentXsdType.getLocalPart());
		return metaData;
	}

	
	public PropertyMappingMetaData mapPropertyIndexInSequenceOld(GmProperty gmProperty, int sequenceIndex) {
		PropertyMappingMetaData metaData = acquireMetaData(gmProperty);
		metaData.getIndex().add( sequenceIndex);
		return metaData;
	}

	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.metadata.MetaDataMapper#mapType(com.braintribe.model.meta.GmEntityType, java.lang.String)
	 */
	
	public EntityTypeMappingMetaData mapType(GmEntityType gmEntityType, String xsdType) {
		EntityTypeMappingMetaData metaData = acquireMetaData(gmEntityType);
		metaData.setType( gmEntityType);
		metaData.setXsdName(xsdType);
		metaData.setGlobalId("meta:entityTypeMapping" + gmEntityType.getTypeSignature());
		gmEntityType.getMetaData().add( metaData);	
		return metaData;
	}

	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.metadata.MetaDataMapper#mapType(com.braintribe.model.meta.GmEnumType, java.lang.String)
	 */

	public EnumTypeMappingMetaData mapType(GmEnumType gmEnumType, String xsdType) {
		EnumTypeMappingMetaData metaData = acquireMetaData(gmEnumType);
		metaData.setType(gmEnumType);
		metaData.setXsdName(xsdType);
		metaData.setGlobalId("meta:enumTypeMapping" + gmEnumType.getTypeSignature());
		gmEnumType.getMetaData().add(metaData);
		return metaData;
	}

	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.mapper.metadata.MetaDataMapper#mapEnumConstant(com.braintribe.model.meta.GmEnumConstant, java.lang.String)
	 */

	public EnumConstantMappingMetaData mapEnumConstant(GmEnumConstant constant, String xsdValue) {
		EnumConstantMappingMetaData metaData = acquireMetaData(constant);
		metaData.setConstant(constant);
		metaData.setXsdName(xsdValue);
		metaData.setGlobalId("meta:enumConstantMapping" + constant.getName() + "@" + constant.getDeclaringType().getTypeSignature());			
		constant.getMetaData().add(metaData);	
		return metaData;
	}

	@Override
	public void attachConstraints(HasMetaData hasMetaData, Collection<MetaData> constraints) {
		hasMetaData.getMetaData().addAll(constraints);		
	}

	@Override
	public void acknowledgeToplevelElementToTypeAssociation(Map<String, GmType> topLevelElementToTypeMap) {
		this.topLevelElementToTypeMap.putAll( topLevelElementToTypeMap);				
	}

	@Override
	public String assertNonCollidingTypeName( String proposal) {
		do {
			boolean isAvailable = isTypeNameAvailable( proposal); 
			if ( !isAvailable) {
				proposal = "_" +proposal;
			} else {				
				return proposal;
			}
		} while (true);
	}

	@Override
	public String getOverridingName(String typeName, String propertyName) {
		String propertyNameToUse = propertyName != null ? propertyName : "";
		String typeNameToUse = typeName != null ? typeName : "";
		String key = typeNameToUse + ":" + propertyNameToUse;			
		return nameOverrides.get(key);
	}

	@Override
	public GmEntityType getSubstitutingType(String name) {		
		GmEntityType gmEntityType = substitutions.get(name);
		if (gmEntityType != null) {
			actuallyRequestedSubstitutions.add(gmEntityType);
		}
		return gmEntityType;
	}
	
	@Override
	public GmEntityType getSubstitutingType(String typeName, String propertyName) {
		String propertyNameToUse = propertyName != null ? propertyName : "";
		String typeNameToUse = typeName != null ? typeName : "";
		String key = typeNameToUse + ":" + propertyNameToUse;			
		GmEntityType gmEntityType = substitutions.get(key);
		if (gmEntityType != null) {
			actuallyRequestedSubstitutions.add(gmEntityType);
		}
		return gmEntityType;
	}

	public Collection<GmType> getActuallySubstitutedTypes() {
		return actuallyRequestedSubstitutions;		
	}
	
	

	
}
