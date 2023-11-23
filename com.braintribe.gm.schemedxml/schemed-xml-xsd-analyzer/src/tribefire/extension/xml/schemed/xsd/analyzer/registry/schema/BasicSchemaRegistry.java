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
package tribefire.extension.xml.schemed.xsd.analyzer.registry.schema;


import static com.braintribe.console.ConsoleOutputs.out;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

import tribefire.extension.xml.schemed.marshaller.commons.QNameExpert;
import tribefire.extension.xml.schemed.marshaller.commons.QNameWrapperCodec;
import tribefire.extension.xml.schemed.marshaller.xsd.resolver.SchemaReferenceResolver;
import tribefire.extension.xml.schemed.model.xsd.Attribute;
import tribefire.extension.xml.schemed.model.xsd.AttributeGroup;
import tribefire.extension.xml.schemed.model.xsd.ComplexType;
import tribefire.extension.xml.schemed.model.xsd.Element;
import tribefire.extension.xml.schemed.model.xsd.Group;
import tribefire.extension.xml.schemed.model.xsd.Import;
import tribefire.extension.xml.schemed.model.xsd.Include;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.QName;
import tribefire.extension.xml.schemed.model.xsd.Qualification;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SchemaEntity;
import tribefire.extension.xml.schemed.model.xsd.SimpleType;
import tribefire.extension.xml.schemed.model.xsd.Type;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.MappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.TypeResolver;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.structure.ElementResolver;
import tribefire.extension.xml.schemed.xsd.api.analyzer.AnalyzerRegistry;
import tribefire.extension.xml.schemed.xsd.api.analyzer.SchemaLookupResult;
import tribefire.extension.xml.schemed.xsd.api.analyzer.SchemaRegistry;
import tribefire.extension.xml.schemed.xsd.api.analyzer.naming.NamespaceGenerator;
import tribefire.extension.xml.schemed.xsd.api.analyzer.naming.QPathGenerator;

/**
 * a basic functional implementation of the {@link SchemaRegistry}
 * @author pit
 *
 */
public class BasicSchemaRegistry implements SchemaRegistry, QPathGenerator {
	private static Logger log = Logger.getLogger(BasicSchemaRegistry.class);

	private Map<QName, SimpleType> qnameToSimpleTypeMap = CodingMap.createHashMapBased( new QNameWrapperCodec());
	private Map<QName, ComplexType> qnameToComplexTypeMap = CodingMap.createHashMapBased( new QNameWrapperCodec());
	private Map<QName, Element> qnameToTopElementMap = CodingMap.createHashMapBased( new QNameWrapperCodec());
	private Map<QName, Group> qnameToGroupMap = CodingMap.createHashMapBased( new QNameWrapperCodec());
	private Map<QName, AttributeGroup> qnameToAttributeGroupMap = CodingMap.createHashMapBased( new QNameWrapperCodec());
	private Map<QName, Attribute> qnameToAttributeMap = CodingMap.createHashMapBased( new QNameWrapperCodec());
	
	private Map<Element, GmType> elementToTypeMap = new HashMap<>();

	private SchemaReferenceResolver schemaReferenceresolver;
	private NamespaceGenerator namespaceGenerator;
	private Map<String, SchemaRegistry> namespaceUriToRegistryMap = new HashMap<>();
	private Map<String, SchemaRegistry> prefixToSchemaRegistryMap = new HashMap<>();
	private Map<Schema, String> schemaToPrefixMap = new HashMap<>();
	private Map<Schema, String> schemaToNamespaceUriMap = new HashMap<>();
	
	private NamespaceRegistry namespaceRegistry;
	private Schema schema;
	private SchemaRegistry parentSchemaRegistry;
	private AnalyzerRegistry backingRegistry;
	private QPathGenerator qpathGenerator;
	private List<Schema> includedSchemata = new ArrayList<>();
	private boolean verbose = false;
	
	
	public static BasicSchemaRegistry createFromSchema( Schema schema, SchemaReferenceResolver schemaResolver, NamespaceGenerator namespaceGenerator, AnalyzerRegistry backingRegistry, QPathGenerator qpathGenerator) {	
		BasicSchemaRegistry registry = new BasicSchemaRegistry();
		registry.setBackingRegistry(backingRegistry);
		registry.setResolver(schemaResolver);
		registry.setNamespaceGenerator(namespaceGenerator);
		registry.namespaceRegistry = NamespaceRegistry.createFromSchema(schema);
		registry.setQpathGenerator(qpathGenerator);
		backingRegistry.setBackingRegistryForSchema(schema, registry);

		registry.process(schema);		
		return registry;
	}
	
	@Configurable @Required
	public void setQpathGenerator(QPathGenerator qpathGenerator) {
		this.qpathGenerator = qpathGenerator;
	}
	
	@Configurable @Required
	public void setResolver(SchemaReferenceResolver resolver) {
		this.schemaReferenceresolver = resolver;
	}	
	@Configurable @Required
	public void setNamespaceGenerator(NamespaceGenerator namespaceGenerator) {
		this.namespaceGenerator = namespaceGenerator;
	}
	@Configurable
	public void setParentSchemaRegistry(SchemaRegistry parentSchemaRegistry) {
		this.parentSchemaRegistry = parentSchemaRegistry;
	}
	
	@Override
	public SchemaRegistry getParentSchemaRegistry() {		
		return parentSchemaRegistry;
	}
	@Configurable @Required
	public void setBackingRegistry(AnalyzerRegistry backingRegistry) {
		this.backingRegistry = backingRegistry;
	}
	
	@Override
	public Schema getSchema() {	
		return schema;
	}
	@Configurable
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	/**
	 * process the passed {@link Schema}
	 * @param schema
	 */
	private void process( Schema schema) {
			
		this.schema = schema;
		
		Namespace targetNamespace = schema.getTargetNamespace();
		if (targetNamespace != null) {
			targetNamespace.setElementQualified( schema.getElementFormDefault() == Qualification.qualified);
			targetNamespace.setAttributeQualified( schema.getAttributeFormDefault() == Qualification.qualified);
			targetNamespace.setDeclaringSchema(schema);
		}
	
		merge( schema);
		
		for (Import toImport : schema.getImports()) {
			String location = toImport.getSchemaLocation();
			String importedNamespace = toImport.getNamespace();
			
			Schema importedSchema = schemaReferenceresolver.resolve(schema, location);
			BasicSchemaRegistry importedRegistry = createFromSchema( importedSchema, schemaReferenceresolver, namespaceGenerator, backingRegistry, qpathGenerator);
				
			importedRegistry.setParentSchemaRegistry( this);
			namespaceUriToRegistryMap.put(importedSchema.getTargetNamespace().getUri(), importedRegistry);
			schemaToNamespaceUriMap.put(schema, importedNamespace);
			
			
			
			for (Namespace namespace : schema.getNamespaces()) {
				if (namespace.getUri().equalsIgnoreCase( importedNamespace)) {					
					prefixToSchemaRegistryMap.put( namespace.getPrefix(), importedRegistry);
					schemaToPrefixMap.put( importedSchema, namespace.getPrefix());
				}				
			}
			
		}
				
	}

	/**
	 * merge two schema (main with included) or just a single one, to get some lookup maps 
	 * @param additional - the schema to merge into the lookup maps
	 */
	private void merge( Schema additional) {
		Schema schema = getSchema();
		
		// 
		String prefix = additional.getTargetNamespace() != null ? additional.getTargetNamespace().getPrefix() : null;
		
		for (Include entry : additional.getIncludes()) {
			String location = entry.getSchemaLocation();
			Schema includedSchema = schemaReferenceresolver.resolve( schema, location);
			if (!includedSchemata.contains(includedSchema))  {
				if (includedSchema != null) { 
					merge( includedSchema);			
				}
				includedSchemata.add(includedSchema);
			}
			else {
				if (verbose) {
					out("already included [" + location + "]");
				}
			}
		}
		
		for (SchemaEntity entity : additional.getEntities()) {
			if (entity instanceof Import) {
				schema.getImports().add( (Import) entity);
			}
			entity.setDeclaringSchema(schema);
		}
		
		// create map of complex types 
		for (ComplexType entry : additional.getComplexTypes()) {
			String name = entry.getName();
			if (!name.contains(":")) {
				name = prefix != null ? prefix + ":" + name : name;
			}
			QName qName = QNameExpert.parse( name);
			qName = ensureQName(null, qName);
			qnameToComplexTypeMap.putIfAbsent( qName, entry);
		}
		// create map of simple types 
		for (SimpleType entry : additional.getSimpleTypes()) {
			String name = entry.getName();
			if (!name.contains(":")) {
				name = prefix != null ? prefix + ":" + name : name;
			}
			QName qName = QNameExpert.parse( name);
			qName = ensureQName(null, qName);
			qnameToSimpleTypeMap.putIfAbsent( qName, entry);
			//
			if (verbose) {
				out("simple : " + name);
			}
		}

		// create map of top level elements 
		for (Element entry : additional.getToplevelElements()) {			
			String name = entry.getName();
			if (!name.contains(":")) {
				name = prefix != null ? prefix + ":" + name : name;
			}
			QName qName = QNameExpert.parse( name);
			qName = ensureQName(null, qName);
			qnameToTopElementMap.putIfAbsent( qName, entry);
		}
		// create map of groups
		for (Group entry : additional.getGroups()) {
			String name = entry.getName();
			if (!name.contains(":")) {
				name = prefix != null ? prefix + ":" + name : name;
			}
			QName qName = QNameExpert.parse( name);
			qName = ensureQName(null, qName);
			qnameToGroupMap.putIfAbsent( qName, entry);
		}
		// create map of attributes 
		for (Attribute entry : additional.getAttributes()) {			
			String name = entry.getName();
			if (!name.contains(":")) {
				name = prefix != null ? prefix + ":" + name : name;
			}
			QName qName = QNameExpert.parse( name);
			qName = ensureQName(null, qName);
			qnameToAttributeMap.putIfAbsent( qName, entry);
		}
		// create map of attribute groups
		for (AttributeGroup entry : additional.getAttributeGroups()) {
			String name = entry.getName();
			if (!name.contains(":")) {
				name = prefix != null ? prefix + ":" + name : name;
			}
			QName qName = QNameExpert.parse( name);
			qName = ensureQName(null, qName);
			qnameToAttributeGroupMap.putIfAbsent( qName, entry);
		}
	
	
	}
	
	@Override
	public QName generateQName(Schema schema, String name) {		
		QName qname = QNameExpert.parse(name);		
		return ensureQName( schema, qname);
	}
	
	

	@Override
	public QName ensureQName(Schema containingSchema, QName qname) {
		Schema lookupSchema = containingSchema != null ? containingSchema : schema;
		if (qname.getPrefix() == null) {			
			Namespace targetNamespace = lookupSchema.getTargetNamespace();
			if (targetNamespace != null) {
				qname.setPrefix( targetNamespace.getPrefix());
			}
			else {						
				String prefix = schemaToPrefixMap.get( lookupSchema);
				qname.setPrefix(prefix);
			}		
		}		
		qname.setNamespaceUri(namespaceRegistry.getUriForNamespacePrefix( qname.getPrefix()));
		return qname;
	}

	@Override
	public Map<String, SchemaRegistry> getImportedSchemata() {
		Map<String, SchemaRegistry> result = new HashMap<>();
		for (SchemaRegistry childRegistry : prefixToSchemaRegistryMap.values()) {
			result.putAll( childRegistry.getImportedSchemata());
		}
		result.putAll(namespaceUriToRegistryMap);
		return result;
	}	 
	@Override
	public Map<QName, SimpleType> getSimpleTypes() {	
		return qnameToSimpleTypeMap;
	}
	@Override
	public Map<QName, ComplexType> getComplexTypes() {
		return qnameToComplexTypeMap;
	}
	@Override
	public Map<QName, Element> getTopLevelElements() {
		return qnameToTopElementMap;
	}
	
	@Override
	public void adoptType(Type type) {
		QName qName = generateQName( type.getDeclaringSchema(), type.getName());
		
		if (type instanceof ComplexType) {
			qnameToComplexTypeMap.put( qName, (ComplexType) type);
		}
		else {
			qnameToSimpleTypeMap.put( qName, (SimpleType) type); 
		}		
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.analyzer.registry.SchemaRegistryInt#lookupComplexType(com.braintribe.marshaller.schemedXml.xsd.QName)
	 */
	@Override
	public SchemaLookupResult<ComplexType> lookupComplexType( QName qname) {
		qname = ensureQName(null, qname);
		String prefix = qname.getPrefix();
		if (prefix != null) {
			SchemaRegistry associatedSchemaRegistry = prefixToSchemaRegistryMap.get( prefix);
			if (associatedSchemaRegistry != null) {
				//String targetNamespaceUri = associatedSchemaRegistry.getSchema().getTargetNamespace().getUri();		
				QName localName = QNameExpert.parse( qname.getLocalPart());
				return associatedSchemaRegistry.lookupComplexType(localName);		
			}
			else {
				Namespace targetNamespace = schema.getTargetNamespace();
				if (targetNamespace == null || !prefix.equalsIgnoreCase( targetNamespace.getPrefix())) {
					throw new IllegalStateException("no backing schema registry found declared for  [" + QNameExpert.toString(qname) + "]");				
				}
			}
		}
		
		ComplexType ctype = qnameToComplexTypeMap.get(qname);
		if (ctype != null) {
			SchemaLookupResult<ComplexType> result = new SchemaLookupResult<>(getTargetNamespace(), ctype);
			return result;
		}
		return null;		
	}
	
	

	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.analyzer.registry.SchemaRegistryInt#lookupSimpleType(com.braintribe.marshaller.schemedXml.xsd.QName)
	 */
	@Override
	public SchemaLookupResult<SimpleType> lookupSimpleType( QName qname) {
		qname = ensureQName( null, qname);
		String prefix = qname.getPrefix();
		if (prefix != null) {
			SchemaRegistry associatedSchemaRegistry = prefixToSchemaRegistryMap.get( prefix);
			if (associatedSchemaRegistry != null) {
				//String targetNamespaceUri = associatedSchemaRegistry.getSchema().getTargetNamespace().getUri();		
				QName localName = QNameExpert.parse( qname.getLocalPart());
				return associatedSchemaRegistry.lookupSimpleType(localName);
			}
			else {
				Namespace targetNamespace = schema.getTargetNamespace();
				if (targetNamespace == null || !prefix.equalsIgnoreCase( targetNamespace.getPrefix())) {
					throw new IllegalStateException("no backing schema registry found declared for  [" + QNameExpert.toString(qname) + "]");				
				}
			}
		}
		
		SimpleType ctype = qnameToSimpleTypeMap.get(qname);
		if (ctype != null) {
			SchemaLookupResult<SimpleType> result = new SchemaLookupResult<SimpleType>(getTargetNamespace(), ctype);
			return result;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.analyzer.registry.SchemaRegistryInt#lookupElement(com.braintribe.marshaller.schemedXml.xsd.QName)
	 */
	@Override
	public SchemaLookupResult<Element> lookupElement( QName qname) {
		qname = ensureQName( null, qname);
		String prefix = qname.getPrefix();
		if (prefix != null) {
			SchemaRegistry associatedSchemaRegistry = prefixToSchemaRegistryMap.get( prefix);
			if (associatedSchemaRegistry != null) {
				//String targetNamespaceUri = associatedSchemaRegistry.getSchema().getTargetNamespace().getUri();		
				QName localName = QNameExpert.parse( qname.getLocalPart());
				return associatedSchemaRegistry.lookupElement(localName);
			}
			else {
				Namespace targetNamespace = schema.getTargetNamespace();
				if (targetNamespace == null || !prefix.equalsIgnoreCase( targetNamespace.getPrefix())) {
					throw new IllegalStateException("no backing schema registry found declared for  [" + QNameExpert.toString(qname) + "]");				
				}
			}
		}
		Element element = qnameToTopElementMap.get(qname);
		if (element != null) {
			SchemaLookupResult<Element> result = new SchemaLookupResult<Element>(getTargetNamespace(), element);
			return result;
		}
		return null;
	}
	


	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.analyzer.api.SchemaRegistry#lookupGroup(com.braintribe.marshaller.schemedXml.xsd.QName)
	 */
	@Override
	public SchemaLookupResult<Group> lookupGroup(QName qname) {		
		qname = ensureQName( null, qname);
		String prefix = qname.getPrefix();
		if (prefix != null) {
			SchemaRegistry associatedSchemaRegistry = prefixToSchemaRegistryMap.get( prefix);
			if (associatedSchemaRegistry != null) {
				//String targetNamespaceUri = associatedSchemaRegistry.getSchema().getTargetNamespace().getUri();		
				QName localName = QNameExpert.parse( qname.getLocalPart());
				return associatedSchemaRegistry.lookupGroup(localName);
			}
			else {
				Namespace targetNamespace = schema.getTargetNamespace();
				if (targetNamespace == null || !prefix.equalsIgnoreCase( targetNamespace.getPrefix())) {
					throw new IllegalStateException("no backing schema registry found declared for  [" + QNameExpert.toString(qname) + "]");				
				}
			}
		}
		qname = ensureQName(null, qname);
		Group group = qnameToGroupMap.get(qname);
		if (group != null) {
			SchemaLookupResult<Group> result = new SchemaLookupResult<Group>(getTargetNamespace(), group);
			return result;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.analyzer.api.SchemaRegistry#lookupAttributeGroup(com.braintribe.marshaller.schemedXml.xsd.QName)
	 */
	@Override
	public SchemaLookupResult<AttributeGroup> lookupAttributeGroup(QName qname) {		
		qname = ensureQName( null, qname);
		String prefix = qname.getPrefix();
		if (prefix != null) {
			SchemaRegistry associatedSchemaRegistry = prefixToSchemaRegistryMap.get( prefix);
			if (associatedSchemaRegistry != null) {
				//String targetNamespaceUri = associatedSchemaRegistry.getSchema().getTargetNamespace().getUri();		
				QName localName = QNameExpert.parse( qname.getLocalPart());
				return associatedSchemaRegistry.lookupAttributeGroup(localName);
			}
			else {
				Namespace targetNamespace = schema.getTargetNamespace();
				if (targetNamespace == null || !prefix.equalsIgnoreCase( targetNamespace.getPrefix())) {
					throw new IllegalStateException("no backing schema registry found declared for  [" + QNameExpert.toString(qname) + "]");				
				}
			}
		}
		qname = ensureQName(null, qname);
		AttributeGroup attributeGroup = qnameToAttributeGroupMap.get(qname);
		if (attributeGroup != null) {
			SchemaLookupResult<AttributeGroup> result = new SchemaLookupResult<AttributeGroup>(getTargetNamespace(), attributeGroup);
			return result;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.braintribe.marshaller.schemedXml.xsd.api.analyzer.SchemaRegistry#lookupAttribute(com.braintribe.marshaller.schemedXml.xsd.QName)
	 */
	@Override
	public SchemaLookupResult<Attribute> lookupAttribute(QName qname) {
		qname = ensureQName( null, qname);
		String prefix = qname.getPrefix();
		if (prefix != null) {
			SchemaRegistry associatedSchemaRegistry = prefixToSchemaRegistryMap.get( prefix);
			if (associatedSchemaRegistry != null) {
				//String targetNamespaceUri = associatedSchemaRegistry.getSchema().getTargetNamespace().getUri();		
				QName localName = QNameExpert.parse( qname.getLocalPart());
				return associatedSchemaRegistry.lookupAttribute(localName);
			}
			else {
				Namespace targetNamespace = schema.getTargetNamespace();
				if (targetNamespace == null || !prefix.equalsIgnoreCase( targetNamespace.getPrefix())) {
					throw new IllegalStateException("no backing schema registry found declared for  [" + QNameExpert.toString(qname) + "]");				
				}
			}
		}
		qname = ensureQName(null, qname);
		Attribute attribute = qnameToAttributeMap.get(qname);
		if (attribute != null) {
			SchemaLookupResult<Attribute> result = new SchemaLookupResult<Attribute>(getTargetNamespace(), attribute);
			return result;
		}
		return null;
	}
	
	@Override
	public void associateElementWithType(Element element, GmType type) {
		elementToTypeMap.put(element, type);		
	}
	
	@Override
	public boolean isElementMarkedAsAlreadyProcessed(Element element) {
		return (elementToTypeMap.get(element) != null);		
	}
	
	

	@Override
	public Set<GmType> map( MappingContext mappingContext) {
		if (verbose) {
			out("mapping schema [" + schemaReferenceresolver.getUriOfSchema(getSchema()) + "]");
		}
		
		SchemaMappingContext sContext = new SchemaMappingContext( mappingContext);
		sContext.schema = schema;
		sContext.registry = this;
		sContext.analyzerRegistry = backingRegistry;
		sContext.qpathGenerator = qpathGenerator;
					
		Set<GmType> mappedTypes = new HashSet<>();
		if (verbose) {
			out( "** mapping top level elements **");
		}
		mapTopLevelElements( sContext);		
		
		if (verbose) {
			out( "** mapping unmapped complex types **");
		}
		mappedTypes.addAll(mapComplexTypes( sContext));
		
		if (verbose) {
			out("** mapping unmapped simple types **");
		}
		mappedTypes.addAll(mapSimpleTypes( sContext));
				
		// now do all mappings of the other files. 
		for (SchemaRegistry registry : namespaceUriToRegistryMap.values()) {
			sContext.registry = this;
			if (verbose) {
				out( "** mapping imported schema **");
			}				
			mappedTypes.addAll( registry.map( mappingContext));
		}
		return mappedTypes;
		
	}
	/**
	 * process the top level elements (determines the container types) 
	 * @param context - the {@link SchemaMappingContext} 
	 * @return - a {@link Set} of the {@link GmType} that back the top level elements 
	 */
	private Set<GmType> mapTopLevelElements( SchemaMappingContext context) {
		Map<String,GmType> topLevelTypeAssociation = new HashMap<>();
		Set<GmType> mappedTypes = new HashSet<>();
		for (Entry<QName, Element> entry : qnameToTopElementMap.entrySet()) {
			QName qname = ensureQName( context.schema, entry.getKey());		
			Element element = entry.getValue();
			if (!isElementMarkedAsAlreadyProcessed(element)) {
				GmProperty property = ElementResolver.resolve(context, element, false);
				GmType gmType = property.getType();
				elementToTypeMap.put(element,  gmType);
				mappedTypes.add( gmType);
			}			
			// TODO : mark its type as container type 
			// it's the name of the element combined with GmType
			GmType toplevelType = elementToTypeMap.get(element);
			topLevelTypeAssociation.put( QNameExpert.toString(qname), toplevelType);
			
		}
		context.mappingContext.typeMapper.acknowledgeToplevelElementToTypeAssociation( topLevelTypeAssociation);
		return mappedTypes;
	}


	/**
	 * process all simple types in the {@link Schema} 
	 * @param context - the {@link SchemaMappingContext}
	 * @return - a {@link Set} of {@link GmType} that reflects all {@link SimpleType}s found
	 */
	private Set<GmType> mapSimpleTypes( SchemaMappingContext context) {
		Set<GmType> mappedTypes = new HashSet<>();
		for (Entry<QName, SimpleType> entry : qnameToSimpleTypeMap.entrySet()) {
			QName qname = ensureQName(null, entry.getKey());			
			// might be a standard type as injected by default			
			if (qname.getPrefix() != null && qname.getPrefix().equalsIgnoreCase( schema.getSchemaPrefix())) {
				continue;
			}								
			TypeResolver.acquireType(context, getSchema(), qname);
		}
		return mappedTypes;
	}

	/**
	 * process all complex types in the {@link Schema}
	 * @param context - the {@link SchemaMappingContext}
	 * @return - a {@link Set} of {@link GmType} that reflects all {@link ComplexType}s found 
	 */
	private Set<GmType> mapComplexTypes( SchemaMappingContext context) {
		Set<GmType> mappedTypes = new HashSet<>();
			for (Entry<QName, ComplexType> entry : qnameToComplexTypeMap.entrySet()) {
			QName qname = ensureQName(null, entry.getKey());		
			TypeResolver.acquireType(context, getSchema(), qname);
		}
		return mappedTypes;
	}
	@Override
	public QPath generateQPathForSchemaEntity(Schema schema) {		
		return qpathGenerator.generateQPathForSchemaEntity(schema);
	}

	@Override
	public String getNamespaceForSchema(Schema sourceSchema) {
		return schemaToPrefixMap.get(sourceSchema);		
	}

	@Override
	public Namespace getTargetNamespace() {
		return schema.getTargetNamespace();
	}
	
	

	
				
}
