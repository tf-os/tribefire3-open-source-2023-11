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
package tribefire.extension.xml.schemed.mapper.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.override.GmCustomTypeOverride;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.meta.override.GmEnumTypeOverride;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EnumMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

import tribefire.extension.xml.schemed.mapper.SchemedXmlMappingException;
import tribefire.extension.xml.schemed.mapper.api.MapperInfoRegistry;
import tribefire.extension.xml.schemed.mapping.metadata.EntityTypeMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.EnumConstantMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.EnumTypeMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.MappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.ModelMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.Namespace;
import tribefire.extension.xml.schemed.mapping.metadata.PropertyMappingMetaData;
import tribefire.extension.xml.schemed.marshaller.commons.QNameExpert;
import tribefire.extension.xml.schemed.model.xsd.QName;

public class BasicMapperInfoRegistry implements MapperInfoRegistry {	
	private static Logger log = Logger.getLogger(BasicMapperInfoRegistry.class);
	private MapperInfoForMetaModel mim;
	private Map<String, GmEntityType> signatureToEntityTypeMap = new HashMap<>();
	private Map<String, GmEnumType> signatureToEnumTypeMap = new HashMap<>();
	private Set<String> usedNamespaceUris = new HashSet<>();
	private Map<String, List<String>> namespaceUriToSignatureMap = new HashMap<>(); 
	private Map<String, String> qnameToSignatureMap = new HashMap<>();
	
	@Override
	public Set<String> getUsedNamespaces() {	
		return usedNamespaceUris;
	}
	
	/**
	 * collects all properties from a type, with the properties of the super types first
	 * @param type - the {@link GmEntityType}
	 * @return - a {@link List} of {@link GmProperty}
	 */
	private List<GmProperty> collectProperties( GmEntityType type) {
		List<GmProperty> properties = new ArrayList<GmProperty>();		
		for (GmEntityType superType : type.getSuperTypes()) {
			properties.addAll( collectProperties( superType));
		}
		properties.addAll( type.getProperties());
		return properties;
	}
		
	public void initializeWithMappingMetaModel( GmMetaModel metaModel) {		
		mim = new MapperInfoForMetaModel();

		if (metaModel == null || metaModel.getMetaData().size() == 0) {
			return;
		}
		
		ModelOracle modelOracle = new BasicModelOracle(metaModel);
		CmdResolver cmdResolver = new CmdResolverImpl(modelOracle);
		
		ModelMdResolver modelBuilder = cmdResolver.getMetaData();
		
		List<ModelMappingMetaData> mmmds = modelBuilder.meta( ModelMappingMetaData.T).list();
		if (mmmds == null || mmmds.size() == 0)
			return;		
		if (mmmds.size() > 0) {
			mim.setMetaData( mmmds.get(0));
		}
				
		
		Set<GmCustomTypeOverride> typeOverrides = metaModel.getTypeOverrides();
		for (GmCustomTypeOverride override : typeOverrides) {
			if (override.isGmEntityOverride()) {
				
				GmEntityTypeOverride eOverride = (GmEntityTypeOverride) override;
				GmEntityType entityType = eOverride.getEntityType();
				EntityMdResolver builder = modelBuilder.entityType(entityType);
				List<EntityTypeMappingMetaData> etmds = builder.meta( EntityTypeMappingMetaData.T).list();
				if (etmds == null || etmds.size() ==0) {
					continue;
				}
				// actually, we only expect one 
				//addTypeMetaData(etmds.get(0)
				MapperInfoForEntitytype miet = new MapperInfoForEntitytype();
				EntityTypeMappingMetaData metaData = etmds.get(0);
				
				
					String xsdName = metaData.getXsdName();
					
					String namespace = metaData.getNamespace();
					
					if (namespace == null) {
						namespace = "";
					}
					List<String> signatures = namespaceUriToSignatureMap.get( namespace);
					if (signatures == null) {
						signatures = new ArrayList<>();
						namespaceUriToSignatureMap.put( namespace, signatures);
					}
					signatures.add( entityType.getTypeSignature());
					usedNamespaceUris.add( metaData.getNamespace());
					qnameToSignatureMap.put( namespace + ":" + xsdName, entityType.getTypeSignature());
										
				
				miet.setMetaData( metaData);
								
				miet.setType(entityType);
								
				signatureToEntityTypeMap.put( entityType.getTypeSignature(), entityType);
				
				// collect all properties from the type hierarchy 
				List<GmProperty> properties = collectProperties( entityType);
				int offset = 0;
				int lastIndex = 0;
				GmEntityType type = null;
				for (GmProperty property : properties) {
					
					// check if we are switching types 
					if (type == null) {
						type = property.getDeclaringType();
					} else {
						GmEntityType propertysType = property.getDeclaringType();
						if (type != propertysType) {
							offset = lastIndex;
							type = propertysType;
						}
					}
					List<PropertyMappingMetaData> pmds = builder.property(property).meta( PropertyMappingMetaData.T).list();
					// properties like "id" have no mapping metadata
					if (pmds == null || pmds.size() == 0) {
						continue;
					}
					// build mapper info 
					MapperInfoForProperty mip = new MapperInfoForProperty();
					PropertyMappingMetaData md = pmds.get(0);				
					// move the index (take supertype's properties into account)
					List<Integer> indices = md.getIndex();
					if (indices.size() > 0) {
						int value = indices.get(0);
						lastIndex = value;
						indices.set(0, lastIndex + offset);
						if  (log.isDebugEnabled()) {
						String msg = "mapping property [" + property.getName() + "] of [" + property.getDeclaringType().getTypeSignature() + "] from index [" + value + "] to [" + (lastIndex + offset) +"]";
							log.debug(msg);
						}						
					}					
					
					mip.setMetaData( md);
					mip.setProperty(property);
					miet.addProperty(mip);
				}
				
				mim.addTypeInfo(miet);					
				
			}
			else if (override.isGmEnumOverride()) {
				GmEnumTypeOverride eOverride = (GmEnumTypeOverride) override;
				GmEnumType enumType = eOverride.getEnumType();
				EnumMdResolver builder = modelBuilder.enumType(enumType);
				List<EnumTypeMappingMetaData> etmnds = builder.meta( EnumTypeMappingMetaData.T).list();
				MapperInfoForEnumtype mient = new MapperInfoForEnumtype();
				mient.setType(enumType);
				EnumTypeMappingMetaData metadata = etmnds.get(0);
				mient.setMetaData( metadata);
				if (metadata.getNamespace() != null)
					usedNamespaceUris.add(metadata.getNamespace());
				signatureToEnumTypeMap.put( enumType.getTypeSignature(), enumType);
				
				for (GmEnumConstant constant : enumType.getConstants()) {
					List<EnumConstantMappingMetaData> ecmds = builder.constant( constant).meta( EnumConstantMappingMetaData.T).list();
					if (ecmds == null || ecmds.size() == 0) {
						continue;
					}
					MapperInfoForEnumConstant miec = new MapperInfoForEnumConstant();
					miec.setConstant(constant);
					miec.setMetaData( ecmds.get(0));
					mient.addEnumConstant(miec);
				}
				mim.addTypeInfo(mient);
			}
		}
		mim.setupContainerMap();	
						
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getXmlNameForType(com.braintribe.model.meta.GmType)
	 */
	@Override
	public String getXmlNameForType( GmType type) {
		return mim.getXmlNameForType(type);
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getMatchingEntityType(java.lang.String)
	 */
	@Override
	public GmEntityType getMatchingEntityType( String signature) {
		GmEntityType type = signatureToEntityTypeMap.get(signature);		
		return type;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getMatchingEnumType(java.lang.String)
	 */
	@Override
	public GmEnumType getMatchingEnumType( String signature) {
		GmEnumType type = signatureToEnumTypeMap.get(signature);		
		return type;
	}
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getTypeSignatureOfPropertyType(java.lang.String, com.braintribe.marshaller.schemedXml.xsd.QName)
	 */
	@Override
	public String getTypeSignatureOfPropertyType( String signature, QName tuple, boolean attribute) {
		GmEntityType entityType = getMatchingEntityType(signature);
		if (entityType == null)
			return null;
		return mim.getTypeSignatureOfPropertyType(entityType, tuple, attribute);
				
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getTypeSignatureOfPropertyElementType(java.lang.String, com.braintribe.marshaller.schemedXml.xsd.QName)
	 */
	@Override
	public String getTypeSignatureOfPropertyElementType( String signature, QName tuple) {
		GmEntityType entityType = getMatchingEntityType(signature);
		if (entityType == null)
			return null;		
		String typeSignatureOfPropertyElementType = mim.getTypeSignatureOfPropertyElementType(entityType, tuple);
		if (typeSignatureOfPropertyElementType == null) {
			// no hit, it may be that the property 
			List<GmProperty> properties = entityType.getProperties();
			if (properties.size() == 1) {
				GmProperty property = properties.get(0);
				GmType propertyType = property.getType();
				if (propertyType instanceof GmEntityType) {
					GmEntityType propertyEntityType = (GmEntityType) propertyType;
					EntityTypeMappingMetaData entityTypeMetaData = getEntityTypeMetaData(propertyEntityType);
					if (entityTypeMetaData.getIsVirtual()) {
						return getTypeSignatureOfPropertyElementType( propertyEntityType.getTypeSignature(), tuple);
					}
				}
			}
		}
		return typeSignatureOfPropertyElementType;			
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getMappedMainTypeSignature(java.lang.String)
	 */
	@Override
	public String getMappedMainTypeSignature( String name){
		return mim.getMappedMainTypeSignature(name);
	}
		
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getMappedProtoNameForMainTypeSignature(java.lang.String)
	 */
	@Override
	public String getMappedProtoNameForMainTypeSignature( String signature) {
		return mim.getMappedXmlNameForMainTypeSignature(signature);
	}
	
	/**
	 * attach the {@link ModelMappingMetaData}
	 * @param modelMappingData - the {@link ModelMappingMetaData}
	 */
	public void addModelMetaData( ModelMappingMetaData modelMappingData) {
		mim.setMetaData(modelMappingData);
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getModelMappingMetaData()
	 */
	@Override
	public ModelMappingMetaData getModelMappingMetaData() {
		return mim.getMetaData();
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getEntityTypeMetaData(com.braintribe.model.meta.GmEntityType)
	 */
	@Override
	public EntityTypeMappingMetaData getEntityTypeMetaData(GmEntityType entityType) {
		MapperInfoForEntitytype info = mim.getTypeInfo(entityType);
		if (info == null)
			return null;
		return info.getMetaData();
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getEnumTypeMetaData(com.braintribe.model.meta.GmEnumType)
	 */
	@Override
	public EnumTypeMappingMetaData getEnumTypeMetaData( GmEnumType enumtype){
		MapperInfoForEnumtype info = mim.getTypeInfo(enumtype);
		if (info == null)
			return null;
		return info.getMetaData();
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getEnumConstantMetaData(com.braintribe.model.meta.GmEnumConstant)
	 */
	@Override
	public EnumConstantMappingMetaData getEnumConstantMetaData( GmEnumConstant constant) {
		GmEnumType enumType = constant.getDeclaringType();
		MapperInfoForEnumtype info = mim.getTypeInfo( enumType);
		if (info == null)
			return null;
		MapperInfoForEnumConstant cInfo = info.getInfoForConstant(constant);
		if (cInfo == null)
			return null;
		return cInfo.getMetaData();
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getPropertyMappingMetaData(com.braintribe.model.meta.GmProperty)
	 */
	@Override
	public PropertyMappingMetaData getPropertyMappingMetaData( GmProperty property) {
		MapperInfoForProperty info = mim.getPropertyInfo( property);
		if (info == null)
			return null;		
		return info.getMetaData();		
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getPropertyMappingMetaData(com.braintribe.model.meta.GmEntityType, java.lang.String)
	 */
	@Override
	public PropertyMappingMetaData getPropertyMappingMetaData(GmEntityType type, String propertyName) {
		MapperInfoForProperty info = mim.getPropertyInfo( type, propertyName);
		if (info == null) {
			// ugly fix - must ask again
			if (!propertyName.startsWith( "$")) {
				info = mim.getPropertyInfo( type, propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1));
			}
			if (info != null)
				return info.getMetaData();
			return null;	
		}
		return info.getMetaData();		
	}
	
	
	
	@Override
	public PropertyMappingMetaData getPropertyMappingMetaData(GmEntityType type, javax.xml.namespace.QName qName, boolean attribute) {
		MapperInfoForProperty info = mim.getPropertyInfo( type, QNameExpert.parse(qName), attribute);
		if (info != null)
			return info.getMetaData();
		return null;		
	}

	/**
	 * adds info about an {@link GmEntityType}
	 * @param metadata - {@link EntityTypeMappingMetaData}
	 */
	public void addTypeMetaData(EntityTypeMappingMetaData metadata) {
		MapperInfoForEntitytype miet = new MapperInfoForEntitytype();
		miet.setMetaData( metadata);
		GmEntityType type = metadata.getType(); 
		miet.setType( type);
		mim.addTypeInfo(miet);
		signatureToEntityTypeMap.put( type.getTypeSignature(), type);
		if (metadata.getNamespace() != null)
			usedNamespaceUris.add( metadata.getNamespace());
	}
	
	/**
	 * adds info about a {@link GmEnumType}
	 * @param metadata - {@link EnumTypeMappingMetaData}
	 */
	public void addTypeMetaData(EnumTypeMappingMetaData metadata) {	
		MapperInfoForEnumtype mient = new MapperInfoForEnumtype();
		GmEnumType enumType = metadata.getType();
		mient.setType(enumType);
		mient.setMetaData( metadata);
		mim.addTypeInfo(mient);
		signatureToEnumTypeMap.put( enumType.getTypeSignature(), enumType);
		if (metadata.getNamespace() != null)
			usedNamespaceUris.add( metadata.getNamespace());
		
	}
	/**
	 * add info about a {@link GmProperty}
	 * @param metadata - the {@link PropertyMappingMetaData} to add 
	 */
	public void addPropertyMetaData(PropertyMappingMetaData metadata)  {
		MapperInfoForProperty mip = new MapperInfoForProperty();
		GmProperty property = metadata.getProperty();
		mip.setMetaData( metadata);
		mip.setProperty(property);
		GmEntityType owner = property.getDeclaringType();
		
		MapperInfoForEntitytype miet = mim.getTypeInfo(owner);
		if (miet == null) {
			String msg= "cannot find MapperInfo for entity type [" + owner.getTypeSignature() + "]";
			log.error(msg);
			throw new SchemedXmlMappingException(msg);
		}
		miet.addProperty(mip);
	}
	
	/**
	 * add info about a {@link GmEnumConstant}
	 * @param metadata - te {@link EnumConstantMappingMetaData}
	 */
	public void addEnumConstantMetaData( EnumConstantMappingMetaData metadata) {
		MapperInfoForEnumConstant miec = new MapperInfoForEnumConstant();
		GmEnumConstant constant = metadata.getConstant();
		miec.setConstant(constant);
		miec.setMetaData( metadata);
		GmEnumType owner = constant.getDeclaringType();
		MapperInfoForEnumtype mie = mim.getTypeInfo( owner);
		mie.addEnumConstant(miec);
	}
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getPropertyInfosForType(com.braintribe.model.meta.GmEntityType)
	 */
	@Override
	public Collection<MapperInfoForProperty> getPropertyInfosForType( GmEntityType type) {
		return mim.getPropertyInfosForType(type);
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getPropertyInfo(com.braintribe.model.meta.GmEntityType, com.braintribe.marshaller.schemedXml.xsd.QName)
	 */
	@Override
	public MapperInfoForProperty getPropertyInfo( GmEntityType type, QName tuple, boolean attribute) {
		return mim.getPropertyInfo(type, tuple, attribute);
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getTypeInfo(com.braintribe.model.meta.GmType)
	 */
	@Override
	public <T extends MapperInfoForType> T getTypeInfo( GmType type){
		return mim.getTypeInfo(type);
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getSimpleContentPropertyInfoForType(com.braintribe.model.meta.GmEntityType)
	 */
	@Override
	public MapperInfoForProperty getSimpleContentPropertyInfoForType(GmEntityType entityType) {
		MapperInfoForEntitytype info = mim.getTypeInfo(entityType);
		if (info == null)
			return null;
		return info.getInfoForSimpleContentProperty();
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getPrefixOfNamespace(java.lang.String)
	 */
	@Override
	public String getPrefixOfNamespace( String namespace) {
		return mim.getPrefixOfNamespace(namespace);
	}
	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#getNamespaceOfPrefix(java.lang.String)
	 */
	@Override
	public String getNamespaceOfPrefix( String prefix) {
		return mim.getNamespaceOfPrefix(prefix);
	}
	

	@Override
	public Map<String, Namespace> getTargetNamespaces() {		
		return mim.getNamespaces();
	}
	
	

	@Override
	public boolean getElementQualification(String uri) {	
		return mim.getElementQualificationForNamespace(uri);
	}

	@Override
	public boolean getAttributeQualification(String uri) {
		return mim.getAttributeQualificationForNamespace(uri);
	}

	/* (non-Javadoc)
	 * @see com.braintribe.schemedXml.xsd.xml.mapper.structure.IMapperInfoRegistry#hasRelevantProperties(com.braintribe.model.meta.GmEntityType)
	 */
	@Override
	public boolean hasRelevantProperties( GmEntityType entitytype) {
		return mim.hasRelevantProperties(entitytype);
	}
	
	@Override
	public String getMappedTypeSignature( QName domName, Map<String,String> namespaceMap) {
		ModelMappingMetaData modelMappingMetaData = getModelMappingMetaData();	
		if (modelMappingMetaData == null)
			return null;
		
		
		Map<String, MappingMetaData> map = modelMappingMetaData.getMappingMetaDataMap();
		String prefix = domName.getPrefix();
		String uri = domName.getNamespaceUri();
		String name = domName.getLocalPart();

		
		if (uri != null) {
			List<String> signatures = namespaceUriToSignatureMap.get(uri);
			if (signatures == null)
				return null;		
		}
		/*
		
		if (map == null)
			return null;
		int p = domName.indexOf(':');
		if (p > 0) {
			if (namespaceMap != null) {
				String prefix = domName.substring(0, p);
				//String name = domName.substring(p+1);
				String namespace = namespaceMap.get( prefix);
				for (Entry<String,MappingMetaData> entry : map.entrySet()) {
					if (entry.getKey().equalsIgnoreCase( domName)) {					
						MappingMetaData metaData = entry.getValue();
						if (metaData instanceof EntityTypeMappingMetaData) {
							EntityTypeMappingMetaData entityTypeMappingMetaData = (EntityTypeMappingMetaData) metaData;
							if (entityTypeMappingMetaData.getNamespace().equalsIgnoreCase(namespace)) {
								return entityTypeMappingMetaData.getType().getTypeSignature();
							}
						}
						if (metaData instanceof EnumTypeMappingMetaData) {
							EnumTypeMappingMetaData enumTypeMetaData = (EnumTypeMappingMetaData) metaData;
							if (enumTypeMetaData.getNamespace().equalsIgnoreCase(namespace)) {
								return enumTypeMetaData.getType().getTypeSignature();
							}
						}
					}
				}
				return null;
			}
		}
		MappingMetaData mappingMetaData = map.get( domName);
		if (mappingMetaData == null)
			return null;
		if (mappingMetaData instanceof EntityTypeMappingMetaData) {
			EntityTypeMappingMetaData entityTypeMappingMetaData = (EntityTypeMappingMetaData) mappingMetaData;
			return entityTypeMappingMetaData.getType().getTypeSignature();
		}
		if (mappingMetaData instanceof EnumTypeMappingMetaData) {
			EnumTypeMappingMetaData enumTypeMappingMetaData = (EnumTypeMappingMetaData) mappingMetaData;
			return enumTypeMappingMetaData.getType().getTypeSignature();
		}
		*/
		return null;		
	}
	
}
