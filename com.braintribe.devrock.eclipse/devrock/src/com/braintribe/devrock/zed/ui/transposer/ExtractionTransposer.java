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
package com.braintribe.devrock.zed.ui.transposer;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zarathud.model.common.ArtifactNode;
import com.braintribe.devrock.zarathud.model.common.Node;
import com.braintribe.devrock.zarathud.model.common.NullNode;
import com.braintribe.devrock.zarathud.model.extraction.AnnotationNode;
import com.braintribe.devrock.zarathud.model.extraction.ClassNode;
import com.braintribe.devrock.zarathud.model.extraction.EnumNode;
import com.braintribe.devrock.zarathud.model.extraction.EnumValueNode;
import com.braintribe.devrock.zarathud.model.extraction.ExtractionNode;
import com.braintribe.devrock.zarathud.model.extraction.FieldNode;
import com.braintribe.devrock.zarathud.model.extraction.InterfaceNode;
import com.braintribe.devrock.zarathud.model.extraction.MethodNode;
import com.braintribe.devrock.zarathud.model.extraction.UnknownEntityNode;
import com.braintribe.devrock.zarathud.model.extraction.subs.ContainerNode;
import com.braintribe.devrock.zarathud.model.extraction.subs.PackageNode;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.AnnotationValueContainer;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ClassEntity;
import com.braintribe.zarathud.model.data.ClassOrInterfaceEntity;
import com.braintribe.zarathud.model.data.EnumEntity;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.data.natures.HasAnnotationsNature;
import com.braintribe.zarathud.model.data.natures.HasFieldsNature;
import com.braintribe.zarathud.model.data.natures.HasMethodsNature;

/**
 * transposes an extracted {@link Artifact}'s data into nodes
 */
public class ExtractionTransposer implements HasContainerTokens {	
	
	protected Map<GenericEntity, ExtractionNode> processed = new HashMap<>();
	protected Map<ExtractionNode, GenericEntity> reverseProcessed;
	protected Map<String, PackageNode> parentNodes = new HashMap<>();
	
	
	public void clear() {
		processed.clear();
		if (reverseProcessed != null) 
			reverseProcessed.clear();
		parentNodes.clear();
	}
			
	/**
	 * transposes an {@link Artifact}
	 * @param context - the {@link ZedExtractionTransposingContext}
	 * @param artifact - the {@link Artifact}
	 * @return - a transposed {@link Node}
	 */
	public ArtifactNode transposeArtifact( ZedExtractionTransposingContext context, Artifact artifact) {
		Set<ZedEntity> entries = artifact.getEntries();
		ArtifactNode an = ArtifactNode.T.create();
		an.setIdentification( VersionedArtifactIdentification.create( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
		List<ZedEntity> toProcess = entries.stream()
										.filter( z -> z.getDefinedInTerminal())
										.collect(Collectors.toList());
		
		toProcess.sort( new Comparator<ZedEntity>() {
											@Override
											public int compare(ZedEntity z1, ZedEntity z2) {
												String n1 = z1.getName();
												String n2 = z2.getName();
												if (n1 != null && n2 != null) {
													return n1.compareTo(n2);
												}																							
												return 0;
											}																						
										});;
												 
		for (ZedEntity zn : toProcess) {			
			ExtractionNode node = transposeEntity( context, zn);
			Pair<String,String> splitName = splitToPackageAndName( zn.getName());
			Node parentNode = determineParentNode( zn, splitName.first, an);
			if (parentNode != null) {
				node.setName(splitName.second);
				attach((ExtractionNode) parentNode, node);
				//parentNode.getChildren().add( node);
			} else {
				attach (an, node);
				//an.getChildren().add(node);
				
			}
		}
		return an;
	}
	
	protected Pair<String, String> splitToPackageAndName( String qualifiedName) {	
		int lastIndexOf = qualifiedName.lastIndexOf('.');
		String packageName = qualifiedName.substring( 0, lastIndexOf);
		String name = qualifiedName.substring(lastIndexOf+1);
		return Pair.of( packageName, name);
	}
	

	protected Node determineParentNode(ZedEntity e, String packageName, ExtractionNode toAttach) {				
		PackageNode p = parentNodes.get( packageName);
		if (p == null) {
			p = PackageNode.T.create();
			p.setName(packageName);
			parentNodes.put(packageName, p);
			attach( toAttach, p);
			//toAttach.getChildren().add(p);
		}		
		return p;
	}



	/**
	 * transposes a {@link ZedEntity} by identifying the type and dispatching to the correct handler
	 * @param context - the {@link ZedExtractionTransposingContext}
	 * @param zn - the {@link ZedEntity}
	 * @return - a transpose {@link Node}
	 */
	protected ExtractionNode transposeEntity(ZedExtractionTransposingContext context, ZedEntity zn) {
		// processed already? 
		ExtractionNode node = processed.get(zn);
		if (node != null) {
			return node;
		}				
		if (zn instanceof InterfaceEntity) {
			return transpose(context, (InterfaceEntity) zn);
		}
		else if (zn instanceof EnumEntity) {
			return transpose( context, (EnumEntity) zn);
		}		
		else if (zn instanceof ClassEntity) {
			return transpose( context, (ClassEntity) zn);
		}
		else if (zn instanceof AnnotationEntity) {
			return transpose(context, (AnnotationEntity) zn); 
		}
		else {
			return transposeUnknown( context, zn);
		}	
	}

	/**
	 * transposes an {@link InterfaceEntity}
	 * @param context - the {@link ZedExtractionTransposingContext}
	 * @param e - the {@link InterfaceEntity}
	 * @return - a transpose {@link Node}
	 */
	protected ExtractionNode transpose( ZedExtractionTransposingContext context, InterfaceEntity e) {
		// don't process twice
		ExtractionNode node = processed.get(e);
		if (node != null) {
			return node;
		}
		
		InterfaceNode in = InterfaceNode.T.create();
		in.setName(e.getName());
		in.setInterfaceEntity(e);
		
		// eagerly add to list  
		processed.put(e, in);
		
		if (!context.isDetailed()) {
			return in;
		}
				 		
		// super interfaces
		transposeSuperInterfaces(context, e, in);
		
		// classes that implement this interface
		transposeImplementingClasses(context, e, in);
		
		// interfaces that derive from this interface 
		transposeDerivingInterfaces(context, e, in);
		
		// annotations 
		transposeAnnotations(context, e, in);
		
		// fields
		transposeFields(context, e, in);
		
		// methods
		transposeMethods(context, e, in);

		// template parameters
		transposeTemplateParameters(context, e, in);
		
		return in;
	}

	
	
	/**
	 * transpose a class
	 * @param context - the {@link ZedExtractionTransposingContext}
	 * @param e - the {@link ClassEntity}
	 * @return - the transposed {@link Node}
	 */
	protected ExtractionNode transpose( ZedExtractionTransposingContext context, ClassEntity e) {
		ExtractionNode node = processed.get(e);
		if (node != null) {
			return node;
		}
		
		ClassNode cn = ClassNode.T.create();
		cn.setName( e.getName());
		cn.setEntity(e);
		
		processed.put(e, cn);
		
		if (!context.isDetailed()) {
			return cn;
		}
						
		// super types 
		transposeSuperType(context, e.getSuperType(), cn);
		
		// sub types
		transposeSubTypes(context, e.getSubTypes(), cn);
		
		// interfaces
		transposeImplementedInterfaces(context, e.getImplementedInterfaces(), cn);
		
		// annotations
		transposeAnnotations(context, e, cn);
		
		// fields
		transposeFields(context, e, cn);
		
		// methods
		transposeMethods(context, e, cn); 

		// template parameters
		transposeTemplateParameters(context, e, cn);
		
		return cn;
	}
	
	
	/**
	 * transpose an enum
	 * @param context
	 * @param e
	 * @return
	 */
	protected ExtractionNode transpose( ZedExtractionTransposingContext context, EnumEntity e) {
		ExtractionNode node = processed.get(e);
		if (node != null) {
			return node;
		}
		EnumNode en = EnumNode.T.create();
		en.setName( e.getName());
		en.setEnumEntity(e);
		
		processed.put(e, en);
		
		if (!context.isDetailed()) {
			return en;
		}
		
		Set<String> values = e.getValues();
		if (values != null && values.size() > 0) {
			ContainerNode hvn = ContainerNode.T.create();
			hvn.setName( VALUES);
			for (String value : values) {
				EnumValueNode eno = EnumValueNode.T.create();
				eno.setName(value);
				attach( hvn, eno);
				//hvn.getChildren().add(eno);
			}
			attach( en, hvn);
			//en.getChildren().add(hvn);
			
		}
						
		// super types
		transposeSuperType(context, e.getSuperType(), en);
		
		// implemented interfaces 
		transposeImplementedInterfaces(context, e.getImplementedInterfaces(), en);
	
		// methods
		transposeMethods(context, e, en);
		
		// sub types
		transposeSubTypes(context, e.getSubTypes(), en);		
		
		// fields
		transposeFields(context, e, en);		
		
		// template parameters
		transposeTemplateParameters(context, e, en);
						
		return en;
	}

	
	
	
	/**
	 * transpose a field 
	 * @param context
	 * @param e
	 * @return
	 */
	protected ExtractionNode transpose( ZedExtractionTransposingContext context, FieldEntity e) {
		ExtractionNode node = processed.get(e);
		if (node != null) {
			return node;
		}
		FieldNode fn = FieldNode.T.create();
		fn.setName( e.getName());
		fn.setFieldEntity(e);
		
		processed.put(e, fn);
		
		if (!context.isDetailed()) {
			return fn;
		}
		
		// type of field
		TypeReferenceEntity typeReferenceEntity = e.getType();		
		ExtractionNode transposeEntity = transposeEntity(context, typeReferenceEntity.getReferencedType());
		attach( fn, transposeEntity);
		//fn.getChildren().add(transposeEntity);
		
		// annotations
		transposeAnnotations(context, e, fn);		
		
		// parameters
		e.getEntityTypesParameter();
		
		return fn;
	}
	
	/**
	 * transpose a method
	 * @param context
	 * @param e
	 * @return
	 */
	protected ExtractionNode transpose( ZedExtractionTransposingContext context, MethodEntity e) {
		ExtractionNode node = processed.get(e);
		if (node != null) {
			return node;
		}
		
		MethodNode mn = MethodNode.T.create();	
		mn.setName( e.getName());	
		mn.setMethodEntity(e);
		
		processed.put(e, mn);
		
		if (!context.isDetailed()) {		
			return mn;
		}
				
		// annotations
		transposeAnnotations(context, e, mn);
		
		List<TypeReferenceEntity> argumentTypes = e.getArgumentTypes();
		transposeTypeCollection(context, ARGUMENT_TYPES, argumentTypes, mn);
		
		TypeReferenceEntity returnType = e.getReturnType();		
		String desc = returnType.getReferencedType().getDesc();
		if (desc != null && !desc.equals( "V")) { 
			transposeTypeCollection(context, RETURN_TYPE, Collections.singletonList( returnType), mn);
		}
		
		List<TypeReferenceEntity> bodyTypes = e.getBodyTypes();
		transposeTypeCollection(context, TYPE_REFERENCES_IN_BODY, bodyTypes, mn);
		
		Set<ClassEntity> exceptions = e.getExceptions();
		transposeClassCollection(context, THROWN_EXCEPTIONS, exceptions, mn);
			
		
		return mn;
	}
	
	
	
	/**
	 * transpose an annotation
	 * @param context
	 * @param e
	 * @return
	 */
	protected ExtractionNode transpose( ZedExtractionTransposingContext context, AnnotationEntity e) {
		ExtractionNode node = processed.get(e);
		if (node != null) {
			return node;
		}
		
		
		AnnotationNode an = AnnotationNode.T.create();		
		String name = e.getDeclaringInterface().getReferencedType().getName();
		an.setName(name);
		an.setAnnotationEntity(e);
		processed.put(e, an);
		
		if (!context.isDetailed()) {
			return an;
		}
		
		if (name == null) {
			name = e.getDeclaringInterface().getReferencedType().getName();
		}
		an.setName( name);
		
		Map<String,AnnotationValueContainer> members = e.getMembers();
		if (members.size() == 0) {
			return an;
		}

			
		for (Map.Entry<String, AnnotationValueContainer> entry : members.entrySet()) {
			ExtractionNode cn = (ExtractionNode) transpose( context, entry.getKey(), entry.getValue());			
			if (cn != null) {
				attach( an, cn);
				//an.getChildren().add( cn);
			}
		}			
		
		return an;
	}
	
	/**
	 * transpose a value container within an annotation
	 * @param context
	 * @param key
	 * @param container
	 * @return
	 */
	protected ExtractionNode transpose(ZedExtractionTransposingContext context, String key, AnnotationValueContainer container) {
		
		ExtractionNode en = ExtractionNode.T.create();
		
		switch (container.getContainerType()) {
			case annotation:
				return transpose(context, container.getOwner());				
			case collection:
				for (AnnotationValueContainer child : container.getChildren()) {				
					ExtractionNode node = transpose(context, key, child);
					attach( en, node);
					//en.getChildren().add(node);
				}			
				return en;
					
			case s_boolean:				
				en.setName( key + " : " + container.getSimpleBooleanValue());
				return en;
			case s_date:				
				en.setName( key + " : " + container.getSimpleDateValue());
				return en;				
			case s_double:
				en.setName( key + " : " + container.getSimpleDoubleValue());
				return en;				
			case s_float:				
				en.setName( key + " : " + container.getSimpleFloatValue());
				return en;				
			case s_int:				
				en.setName( key + " : " + container.getSimpleIntegerValue());
				return en;				
			case s_long:
				en.setName( key + " : " + container.getSimpleLongValue());
				return en;				
			case s_string:
				en.setName( key + " : " + container.getSimpleStringValue());
				return en;				
			default:
				break;			
		}
		return null;
	}

	/**
	 * transpose an unknown entity
	 * @param context
	 * @param e
	 * @return
	 */
	protected ExtractionNode transposeUnknown( ZedExtractionTransposingContext context, ZedEntity e) {
		ExtractionNode node = processed.get(e);
		if (node != null) {
			return node;
		}
		
		UnknownEntityNode en = UnknownEntityNode.T.create();		
		en.setName(e.getName());
		en.setEntity(e);
				
		processed.put(e, en);
		
		return en;
	}
	
	/**
	 * transpose template parameters of interface, class, enum
	 * @param context
	 * @param e
	 * @param cn
	 */
	protected void transposeTemplateParameters(ZedExtractionTransposingContext context, ClassOrInterfaceEntity e, ExtractionNode cn) {
		Map<String,TypeReferenceEntity> templateParameters = e.getTemplateParameters();
		if (templateParameters != null && templateParameters.size() > 0) {
			transposeTypeCollection(context, TEMPLATE_PARAMETERS, templateParameters.values(), cn);
		}
	}

	
	/**
	 * transpose a collection of types 
	 * @param context
	 * @param containerKey
	 * @param types
	 * @param node
	 */
	protected void transposeTypeCollection( ZedExtractionTransposingContext context, String containerKey, Collection<TypeReferenceEntity> types, ExtractionNode node) {
		if (types == null || types.size() == 0) {
			return;
		}
		ContainerNode cn = ContainerNode.T.create();
		cn.setName( containerKey);
		Map<String, TypeReferenceEntity> map = new HashMap<>( types.size());
		
		types.stream().forEach( t -> map.put( t.getReferencedType().getName(), t));
		
		for (TypeReferenceEntity tre : map.values()) {
			ExtractionNode transposeEntity = transposeEntity(context, tre.getReferencedType());
			attach( cn, transposeEntity);
		}						
		attach( node, cn);
	}
	
	/**
	 * transpose a collection of classes
	 * @param context
	 * @param containerKey
	 * @param types
	 * @param node
	 */
	protected void transposeClassCollection( ZedExtractionTransposingContext context, String containerKey, Collection<ClassEntity> types, ExtractionNode node) {
		if (types == null || types.size() == 0) {
			return;
		}
		ContainerNode cn = ContainerNode.T.create();
		cn.setName( containerKey);
		for (ClassEntity ce : types) {
			
			ExtractionNode transposeEntity = transposeEntity(context, ce);
			
			attach( cn, transposeEntity);
			//cn.getChildren().add( transposeEntity);
		}
		attach( node, cn);
		//node.getChildren().add(cn);
	}
	
	
	/**
	 * transpose a collection of interfaces deriving from this interface
	 * @param context
	 * @param e
	 * @param in
	 */
	protected void transposeDerivingInterfaces(ZedExtractionTransposingContext context, InterfaceEntity e, ExtractionNode in) {
		Set<InterfaceEntity> subInterfaces = e.getSubInterfaces();
		if (subInterfaces != null && subInterfaces.size() > 0) {
			ContainerNode hstn = ContainerNode.T.create();
			hstn.setName( DERIVING_TYPES);
			for (InterfaceEntity ie : subInterfaces) {
				ExtractionNode transpose = transpose(context, ie);
				attach( hstn, transpose);
				//hstn.getChildren().add( transpose);
			}
			attach( in, hstn);
			//in.getChildren().add(hstn);
		}		
	}

	/**
	 * transpose a collection of classes implementing this interface 
	 * @param context
	 * @param e
	 * @param in
	 */
	protected void transposeImplementingClasses(ZedExtractionTransposingContext context, InterfaceEntity e, ExtractionNode in) {
		Set<ClassEntity> implementingClasses = e.getImplementingClasses();
		if (implementingClasses != null && implementingClasses.size() > 0) {
			ContainerNode hii = ContainerNode.T.create();
			hii.setName( IMPLEMENTING_TYPES);
			for (ClassEntity ce : implementingClasses) {
				ExtractionNode transpose = transpose(context, ce);
				attach( hii, transpose);
				//hii.getChildren().add( transpose);
			}
			attach( in, hii);
			//in.getChildren().add(hii);
		}
	}

	/**
	 * transpose a collection of interfaces this interface is deriving of 
	 * @param context
	 * @param e
	 * @param in
	 */
	protected void transposeSuperInterfaces(ZedExtractionTransposingContext context, InterfaceEntity e, ExtractionNode in) {
		Set<TypeReferenceEntity> superInterfaces = e.getSuperInterfaces();
		if (superInterfaces != null && superInterfaces.size() > 0) {
			ContainerNode hstn = ContainerNode.T.create();
			hstn.setName( SUPER_INTERFACES);
			for (TypeReferenceEntity tfe : superInterfaces) {
				ExtractionNode transpose = transpose(context, (InterfaceEntity) tfe.getReferencedType());
				attach( hstn, transpose);
				//hstn.getChildren().add( transpose);				
			}
			attach( in, hstn);
			//in.getChildren().add(hstn);
		}
	}
	/**
	 * transpose methods 
	 * @param context
	 * @param e
	 * @param cn
	 */
	protected void transposeMethods(ZedExtractionTransposingContext context, HasMethodsNature e, ExtractionNode cn) {
		Set<MethodEntity> methods = e.getMethods();
		if (methods != null && methods.size() > 0) {
			ContainerNode hmn = ContainerNode.T.create();
			hmn.setName( METHODS);
			for (MethodEntity me : methods) {
				ExtractionNode transpose = transpose(context, me);
				attach( hmn, transpose);
				//hmn.getChildren().add( transpose);				
			}
			attach( cn, hmn);
			//cn.getChildren().add(hmn);
		}
	}

	/**
	 * transpose fields
	 * @param context
	 * @param e
	 * @param cn
	 */
	protected void transposeFields(ZedExtractionTransposingContext context, HasFieldsNature e, ExtractionNode cn) {
		List<FieldEntity> fields = e.getFields();
		if (fields != null && fields.size() > 0) {
			ContainerNode hfn = ContainerNode.T.create();
			hfn.setName( FIELDS);
			for (FieldEntity fe : fields) {
				ExtractionNode transpose = transpose(context, fe);
				attach( hfn, transpose);
				//hfn.getChildren().add( transpose);
			}
			attach( cn, hfn);
			//cn.getChildren().add(hfn);
		}
	}

	/**
	 * transpose a collection of interfaces this class is implementing
	 * @param context
	 * @param implementedInterfaces
	 * @param cn
	 */
	protected void transposeImplementedInterfaces(ZedExtractionTransposingContext context, Collection<TypeReferenceEntity> implementedInterfaces, ExtractionNode cn) {
		if (implementedInterfaces != null && implementedInterfaces.size() > 0) {
			ContainerNode hii = ContainerNode.T.create();
			hii.setName( IMPLEMENTED_INTERFACES);
			for (TypeReferenceEntity tre : implementedInterfaces) {
				ExtractionNode transpose = transpose( context, (InterfaceEntity) tre.getReferencedType());
				attach( hii, transpose);
				//hii.getChildren().add( transpose);
			}
			attach( cn, hii);
			//cn.getChildren().add( hii);
		}
	}

	/**
	 * transpose a collection of classes that derive from a class
	 * @param context
	 * @param subTypes
	 * @param node
	 */
	protected void transposeSubTypes(ZedExtractionTransposingContext context,  Collection<ClassEntity> subTypes, ExtractionNode node) {
		if (subTypes != null && subTypes.size() > 0) {
			ContainerNode hstn = ContainerNode.T.create();
			hstn.setName( DERIVED_TYPES);
			for (ClassEntity ce : subTypes) {
				ExtractionNode transpose = transpose( context, ce);
				attach( hstn, transpose);
				//hstn.getChildren().add( transpose);
			}
			attach( node, hstn);
			//cn.getChildren().add(cn);
		}
	}

	/**
	 * transpose the super type of a class
	 * @param context
	 * @param superType
	 * @param node
	 */
	protected void transposeSuperType(ZedExtractionTransposingContext context, TypeReferenceEntity superType, ExtractionNode node) {
		if (superType != null) {
			ContainerNode hstn = ContainerNode.T.create();
			hstn.setName( SUPER_TYPES);
			ExtractionNode transpose = transpose(context, (ClassEntity) superType.getReferencedType());
			attach( hstn, transpose);
			//hstn.getChildren().add( transpose);
			attach( node, hstn);
			//node.getChildren().add(hstn);
		}
	}

	/**
	 * transpose annotations if any
	 * @param context
	 * @param e
	 * @param node
	 */
	protected void transposeAnnotations(ZedExtractionTransposingContext context, HasAnnotationsNature e, ExtractionNode node) {
		// annotations 
		Set<TypeReferenceEntity> annotations = e.getAnnotations();
		if (annotations != null && annotations.size() > 0) {
			ContainerNode han = ContainerNode.T.create();
			han.setName( ANNOTATIONS);
			for (TypeReferenceEntity tfe : annotations) {
				ExtractionNode anNode = transposeAnnotation(context, tfe);
				attach(han, anNode);				
			}
			attach( node, han);
			//node.getChildren().add(han);
		}
	}

	protected ExtractionNode transposeAnnotation(ZedExtractionTransposingContext context, TypeReferenceEntity tfe) {
		AnnotationEntity ano = (AnnotationEntity) tfe.getReferencedType();
		ExtractionNode anNode = transpose(context,  ano);
		return anNode;		
	}
	
	
	protected void attachCollection(ZedExtractionTransposingContext extractionContext, ContainerNode cn, List<GenericEntity> missing ) {
		GenericEntity first = missing.get(0);
		if (first instanceof MethodEntity) {
			for (GenericEntity ent : missing) {
				ExtractionNode anode = transpose(extractionContext, (MethodEntity) ent);
				attach( cn, anode);
				//cn.getChildren().add(anode);
			}
		}
		else if (first instanceof FieldEntity) {
			for (GenericEntity ent : missing) {
				ExtractionNode anode = transpose(extractionContext, (FieldEntity) ent);
				attach( cn, anode);
				//cn.getChildren().add(anode);
			}
		}
		else if (first instanceof TypeReferenceEntity) {
			for (GenericEntity ent : missing) {
				ExtractionNode anode = transposeEntity(extractionContext, ((TypeReferenceEntity) ent).getReferencedType());
				attach( cn, anode);
				//cn.getChildren().add(anode);
			}
		}
		else if (first instanceof AnnotationEntity) {
			for (GenericEntity ent : missing) {				
				ExtractionNode aNode = transpose(extractionContext,  (AnnotationEntity) ent);
				attach( cn, aNode);
				//cn.getChildren().add(anode);
			}
		}
	}

	protected ExtractionNode transposeAttachedEntity(ZedExtractionTransposingContext extractionContext, GenericEntity entitySource) {
		ExtractionNode node = null;
		if (entitySource != null) {
			if (entitySource instanceof ZedEntity) {		
				node = transposeEntity(extractionContext, (ZedEntity) entitySource);				
			}
			else if (entitySource instanceof FieldEntity){
				node = transpose(extractionContext, (FieldEntity) entitySource);				
			}
			else if (entitySource instanceof MethodEntity) {
				node = transpose(extractionContext, (MethodEntity) entitySource);
			}
			else if (entitySource instanceof TypeReferenceEntity) {
				ZedEntity zen = ((TypeReferenceEntity) entitySource).getReferencedType();
				node = transposeEntity(extractionContext, zen);
			}
			
			else {
				System.out.println("Unknown source");
			}
		}
		else  {
			System.out.println("No source for fp");
		}
		return node;
	}

	protected ExtractionNode buildNullNode() {
		NullNode n = NullNode.T.create();
		return n;
	}
		

	public Node getNodeOfEntity(GenericEntity e) {
		return processed.get(e);
	}
	
	public GenericEntity getEntityOfNode( Node node) {
		// build up first time 
		if (reverseProcessed == null) {
			reverseProcessed = new HashMap<ExtractionNode, GenericEntity>();
			for (Map.Entry<GenericEntity, ExtractionNode> entry : processed.entrySet()) {
				reverseProcessed.put( entry.getValue(), entry.getKey());
			}
		}
		// return reverese lookup data
		return reverseProcessed.get(node);
		
	}
	
	protected void attach(ExtractionNode parent, ExtractionNode child) {
		// add container node to map of container nodes of parent node..
		if (child instanceof ContainerNode) {
			ContainerNode cn = (ContainerNode) child;
			parent.getContainerNodes().put( cn.getName(), cn);						
		}
		parent.getChildren().add(child);
		child.setParent(parent);
		child.getTreepathElements().addAll(parent.getTreepathElements());
		child.getTreepathElements().add( child);
	}
	
}
