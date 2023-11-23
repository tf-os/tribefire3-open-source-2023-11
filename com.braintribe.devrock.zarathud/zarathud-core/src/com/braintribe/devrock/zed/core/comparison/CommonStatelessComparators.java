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
package com.braintribe.devrock.zed.core.comparison;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.devrock.zed.api.comparison.ComparisonContext;
import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.zarathud.model.data.AccessModifier;
import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.ClassEntity;
import com.braintribe.zarathud.model.data.EnumEntity;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.ScopeModifier;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.data.natures.HasAbstractNature;
import com.braintribe.zarathud.model.data.natures.HasAccessModifierNature;
import com.braintribe.zarathud.model.data.natures.HasGenericNature;
import com.braintribe.zarathud.model.data.natures.HasScopeModifierNature;
import com.braintribe.zarathud.model.data.natures.HasStaticNature;
import com.braintribe.zarathud.model.data.natures.HasSynchronizedNature;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.findings.ComparisonIssueType;
import com.braintribe.zarathud.model.forensics.findings.ComparisonProcessFocus;


/**
 * collection of stateless comparators used 
 * @author pit
 */
public class CommonStatelessComparators {
	/**
	 * compare two {@link ZedEntity}
	 * @param base
	 * @param other
	 * @return
	 */
	public static void compareEntities(ComparisonContext context, ZedEntity base, ZedEntity other) {
		
		if (context.isProcessed( base.getName())) {
			return;
		}
	
		if (base instanceof ClassEntity) {  // class
			if (other instanceof ClassEntity == false) {
				FingerPrint fp = FingerPrintExpert.build( base, ComparisonIssueType.typeMismatch);
				fp.setEntityComparisonTarget(other);
				context.addFingerPrint(fp);
			}		
			new StatefulClassComparator(context).compare( (ClassEntity) base, (ClassEntity) other);
			
		} 
		else if (base instanceof InterfaceEntity) { // interface
			if (other instanceof InterfaceEntity == false) {
				FingerPrint fp = FingerPrintExpert.build( base, ComparisonIssueType.typeMismatch);
				fp.setEntityComparisonTarget(other);
				context.addFingerPrint(fp);
			}			
			new StatefulInterfaceComparator(context).compare( (InterfaceEntity) base, (InterfaceEntity) other);					
		} 
		else if (base instanceof EnumEntity) { // enum
			if (other instanceof EnumEntity == false) {
				FingerPrint fp = FingerPrintExpert.build( base, ComparisonIssueType.typeMismatch);
				fp.setEntityComparisonTarget(other);
				context.addFingerPrint(fp);				
			}		
			new StatefulEnumComparator(context).compare( (EnumEntity) base, (EnumEntity) other);							
		}
		else if (base instanceof AnnotationEntity) { // annotation
			if (other instanceof AnnotationEntity == false) {
				FingerPrint fp = FingerPrintExpert.build( base, ComparisonIssueType.typeMismatch);
				fp.setEntityComparisonTarget(other);
				context.addFingerPrint(fp);
			}
			new StatefulAnnotationComparator(context).compare( (AnnotationEntity) base, (AnnotationEntity) other);			
		}						
	}
	
	/**
	 * @param context
	 * @param base
	 * @param other
	 */
	public static void compareTypeReferenceNullSafe( ComparisonContext context, TypeReferenceEntity base, TypeReferenceEntity other) {
		// contextualize
		ComparisonIssueType cit;
		switch (context.getCurrentProcessFocus()) {
			case superType: 
				cit = ComparisonIssueType.superTypeMismatch;
				break;
			default:
				cit = ComparisonIssueType.typeMismatch;
				break;			
		}				
		GenericEntity scrutinized = context.getCurrentEntity();			
		
		if (base == null && other != null) {
			FingerPrint fp = FingerPrintExpert.build( scrutinized, cit);
			fp.setEntitySource(null); // no base super type, address is of the owning class,
			fp.setEntityComparisonTarget(other);
			context.addFingerPrint(fp);					
		}
		else if (base != null && other == null) {
			FingerPrint fp = FingerPrintExpert.build( base, cit);
			fp.setEntityComparisonTarget(other);
			context.addFingerPrint(fp);			
		}		
		else if (base != null && other != null) { 					
			CommonStatelessComparators.compareTypeReference(context, base, other);
		}
		
		
	}
	
	
	/**
	 * @param context
	 * @param base
	 * @param other
	 */
	public static void compareTypeReference( ComparisonContext context, TypeReferenceEntity base, TypeReferenceEntity other) {
		
		ComparisonIssueType cit;
		switch (context.getCurrentProcessFocus()) {
			case superType: 
				cit = ComparisonIssueType.superTypeMismatch;
				break;
			default:
				cit = ComparisonIssueType.typeMismatch;
				break;			
		}				
		GenericEntity scrutinized = context.getCurrentEntity();					
		ZedEntity baseType = base.getReferencedType();
		ZedEntity otherType = other.getReferencedType();
		
		if (baseType == null && otherType != null) {
			FingerPrint fp = FingerPrintExpert.build( scrutinized, cit);
			fp.setEntitySource(null); // no base super type, address is of the owning class,
			fp.setEntityComparisonTarget(other);
			context.addFingerPrint(fp);					
		}
		else if (baseType != null && otherType == null) {
			FingerPrint fp = FingerPrintExpert.build( base, cit);
			fp.setEntityComparisonTarget(other);
			context.addFingerPrint(fp);			
		}
		else if (baseType != null && otherType != null){
			CommonStatelessComparators.compareEntitiesShallow(context, baseType, otherType);
		}		
	}
	
	
	
	
	
	/**
	 * @param base
	 * @param other
	 * @return
	 */
	public static void compareAbstractNature( ComparisonContext context, HasAbstractNature base, HasAbstractNature other) {
		if (base.getAbstractNature() != other.getAbstractNature())  {
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.abstractModifierMismatch);
			fp.setEntityComparisonTarget( context.getCurrentOther());
			context.addFingerPrint(fp);									 			
		}
		
	}
		

	/**
	 * compare two collection of {@link TypeReferenceEntity}
	 * @param topic
	 * @param baseTypes
	 * @param otherTypes
	 * @return
	 */
	public static void compareTypeReferences(ComparisonContext context, Collection<TypeReferenceEntity> baseTypes, Collection<TypeReferenceEntity> otherTypes) {
		
		ComparisonIssueType missingCit;
		ComparisonIssueType surplusCit;
		switch (context.getCurrentProcessFocus()) {
			case implementedInterfaces:
				missingCit = ComparisonIssueType.missingImplementedInterfaces;
				surplusCit = ComparisonIssueType.surplusImplementedInterfaces;
				break;
			case  implementingClasses:
				missingCit = ComparisonIssueType.missingImplementingClasses;
				surplusCit = ComparisonIssueType.surplusImplementingClasses;
				break;
			case superInterfaces:
				missingCit = ComparisonIssueType.missingSuperInterfaces;
				surplusCit = ComparisonIssueType.surplusSuperInterfaces;
				break;			
			case subInterfaces:
				missingCit = ComparisonIssueType.missingSubInterfaces;
				surplusCit = ComparisonIssueType.surplusSubInterfaces;
				break;							
			case subTypes:
				missingCit = ComparisonIssueType.missingSubTypes;
				surplusCit = ComparisonIssueType.surplusSubTypes;
				break;
			case arguments:
				missingCit = ComparisonIssueType.missingMethodArguments;
				surplusCit = ComparisonIssueType.surplusMethodArguments;
				break;
			case exceptions: 
				missingCit = ComparisonIssueType.missingMethodExceptions;
				surplusCit = ComparisonIssueType.surplusMethodExceptions;
				break;
			default: {
				System.out.println("Unexpected process focus : " + context.getCurrentProcessFocus().name());
				return;
			}
		}
		
		List<TypeReferenceEntity> missingTypes = new ArrayList<>();
		for (TypeReferenceEntity base : baseTypes) {
			// push base
			String name = base.getReferencedType().getName();
			
			if (context.isProcessed( name)) 
				continue;
			
			TypeReferenceEntity other = otherTypes.stream().filter( s -> name.equals( s.getReferencedType().getName())).findFirst().orElse(null);
			if (other == null) {
				missingTypes.add(base);
				continue;
			}
			// push other
			ZedEntity baseEntity = base.getReferencedType();
			ZedEntity otherEntity = other.getReferencedType();
			
			CommonStatelessComparators.compareEntitiesShallow(context, baseEntity, otherEntity);
			// pop
						
		}
		List<String> baseNames = baseTypes.stream().map( t -> t.getReferencedType().getName()).collect(Collectors.toList());
		List<String> otherNames = otherTypes.stream().map( t -> t.getReferencedType().getName()).collect(Collectors.toList());
		String missing = missingTypes.stream().map( t -> t.getReferencedType().getName()).collect(Collectors.joining(","));
		
		// other implements NOT ALL interfaces 
		if (missingTypes.size() > 0) {
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), missingCit);
			fp.getIssueData().add( missing);
			fp.getComparisonIssueData().addAll( missingTypes);
			fp.setEntityComparisonTarget( context.getCurrentOther());
			context.addFingerPrint(fp);					
		}
		
		// other implements MORE interfaces
		otherNames.removeAll(baseNames);
		if (otherNames.size() > 0) {
			List<TypeReferenceEntity> surplus = otherTypes.stream().filter( tre -> otherNames.contains( tre.getReferencedType().getName())).collect( Collectors.toList());
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), surplusCit);
			fp.getIssueData().add( otherNames.stream().collect( Collectors.joining(",")));
			fp.getComparisonIssueData().addAll( surplus);
			fp.setEntityComparisonTarget( context.getCurrentOther());
			context.addFingerPrint(fp);
		}

	}
	
	private static void compareEntitiesShallow(ComparisonContext context, ZedEntity baseEntity, ZedEntity otherEntity) {
		
		String baseName = baseEntity.getName();
		String otherName = otherEntity.getName();
		
		if (!baseName.equals(otherName)) {
			ComparisonIssueType cit;
			switch (context.getCurrentProcessFocus()) {
				case superType: 
					cit = ComparisonIssueType.superTypeMismatch;
					break;
				case returnType:
					cit = ComparisonIssueType.methodReturnTypeMismatch;
					break;	
				case fieldType : 
					cit = ComparisonIssueType.fieldTypeMismatch;
					break;
				default:
					cit = ComparisonIssueType.typeMismatch;
					break;			
			}				
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), cit);
			fp.setEntityComparisonTarget( context.getCurrentOther());
			context.addFingerPrint(fp);						
		}
				
	}

	/**
	 * @param topic
	 * @param bases
	 * @param others
	 * @return
	 */
	public static void compareFields(ComparisonContext context, Collection<FieldEntity> rawBases, Collection<FieldEntity> rawOthers) {
		
		List<FieldEntity> bases = rawBases.stream().filter( f -> filterPerAccessModifier(f)).collect( Collectors.toList());
		List<FieldEntity> others = rawOthers.stream().filter( f -> filterPerAccessModifier(f)).collect( Collectors.toList());
		
		List<FieldEntity> missingFields = new ArrayList<>();
		
		
		for (FieldEntity base : bases) {
			FieldEntity other = others.stream().filter( f -> f.getName().equals(base.getName())).findFirst().orElse(null);
			if (other == null) {
				missingFields.add(base);
				continue;
			}
			context.pushCurrentEntity(base);
			context.pushCurrentOther(other);
			
			compareField(context, base, other);
			
			context.popCurrentEntity();
			context.popCurrentOther();
		}
		List<String> baseNames = bases.stream().map( f -> f.getName()).collect(Collectors.toList());
		List<String> otherNames = others.stream().map( f -> f.getName()).collect(Collectors.toList());	
		
		if (missingFields.size() > 0) {		
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.missingFields);
			fp.getIssueData().add( missingFields.stream().map( f -> f.getName()).collect( Collectors.joining(",")));
			fp.getComparisonIssueData().addAll( missingFields);
			fp.setEntityComparisonTarget( context.getCurrentOther());
			context.addFingerPrint(fp);
		}
		
		otherNames.removeAll(baseNames);
		
		if (otherNames.size() > 0) {			
			List<FieldEntity> surplusFields = others.stream().filter( o -> otherNames.contains( o.getName())).collect(Collectors.toList());
			
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.surplusFields);
			fp.getIssueData().add( otherNames.stream().collect( Collectors.joining(",")));
			fp.setEntityComparisonTarget( context.getCurrentOther());
			fp.getComparisonIssueData().addAll( surplusFields);
			context.addFingerPrint(fp);
		}		
	}
	
	/**
	 * @param baseField
	 * @param otherField
	 * @return
	 */
	public static void compareField( ComparisonContext context, FieldEntity baseField, FieldEntity otherField) {
								
		// type
		ZedEntity base = baseField.getType().getReferencedType();
		ZedEntity other = otherField.getType().getReferencedType();
		
		compareEntitiesShallow(context, base, other);
						
		// access modifier
		compareAccessModifiers( context, baseField, otherField);
		
		// scope modifier 
		compareScopeModifiers( context, baseField, otherField);
		
		// static
		compareStaticModifiers( context, baseField, otherField);
		
		// initializer
		// TODO : check extraction here 
				
	}

	/**
	 * checks the static modifier 
	 * @param base
	 * @param other
	 * @return
	 */
	public static void compareStaticModifiers(ComparisonContext context, HasStaticNature base, HasStaticNature other) {
		boolean bsn = base.getStaticNature();
		boolean osn = other.getStaticNature();
		
		if (bsn != osn) {
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.staticModifierMismatch);			
			fp.setEntityComparisonTarget( context.getCurrentOther());
			context.addFingerPrint(fp);			
		}		
	}

	/**
	 * checks the scope modifier (final, volatile et al)
	 * @param context 
	 * @param base
	 * @param other
	 * @return
	 */
	public static void compareScopeModifiers(ComparisonContext context, HasScopeModifierNature base, HasScopeModifierNature other) {
		ScopeModifier bsm = base.getScopeModifier();
		ScopeModifier osm = other.getScopeModifier();
		if (bsm != osm) {
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.scopeModifierMismatch);			
			fp.setEntityComparisonTarget( context.getCurrentOther());
			context.addFingerPrint(fp);			
		}
		
	}

	/**
	 * checks the access modifiers (public, private et al)
	 * @param context 
	 * @param base
	 * @param other
	 * @return
	 */
	public static void compareAccessModifiers(ComparisonContext context, HasAccessModifierNature base, HasAccessModifierNature other) {
		AccessModifier bam = base.getAccessModifier();
		AccessModifier oam = other.getAccessModifier();
		
		if (bam != oam) {
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.accessModifierMismatch);			
			fp.setEntityComparisonTarget( context.getCurrentOther());
			context.addFingerPrint(fp);						
		}
		
	}
	
	private static String buildKey(MethodEntity me) {
		return me.getName() + "-" + me.getDesc();
	}
	
	private static String buildCompositeMethodName( MethodEntity me) {
		String desc = me.getDesc();
		int ppos = desc.lastIndexOf(')');
		String arguments = desc.substring(0, ppos+1);
		String rtype = desc.substring(ppos+1);
		return rtype + " " + me.getName() + arguments;
	}
	
	
		
	private static boolean filterPerAccessModifier( HasAccessModifierNature hamn) {		
		AccessModifier accessModifier = hamn.getAccessModifier();
		if (accessModifier == null) {
			accessModifier = AccessModifier.PACKAGE_PRIVATE;
		}
		switch (accessModifier) {
			case PROTECTED:
			case PUBLIC:
				return true;
			case PACKAGE_PRIVATE:
			case PRIVATE:
			default:
				return false;								
		}	
	}

	/**
	 * compares methods 
	 * @param topic
	 * @param bases
	 * @param others
	 * @return
	 */
	public static void compareMethods(ComparisonContext context, Collection<MethodEntity> rawBases, Collection<MethodEntity> rawOthers) {
		// filter 
		
		List<MethodEntity> bases = rawBases.stream().filter( m -> filterPerAccessModifier(m)).collect( Collectors.toList());
		List<MethodEntity> others = rawOthers.stream().filter( m -> filterPerAccessModifier(m)).collect( Collectors.toList());
		
		List<MethodEntity> missingMethods = new ArrayList<>();
		List<MethodEntity> present = new ArrayList<>();
		
		for (MethodEntity base : bases) {
			String key = buildKey( base);
			MethodEntity other = others.stream().filter( f -> buildKey(f).equals(key)).findFirst().orElse(null);
			if (other == null) {
				missingMethods.add( base);
				continue;
			}
			present.add( other);
			
			context.pushCurrentEntity(base);
			context.pushCurrentOther(other);
			compareMethod(context, base, other);
			context.popCurrentEntity();
			context.popCurrentOther();
			
		}
		if (missingMethods.size() > 0) {
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.missingMethods);			
			fp.setEntityComparisonTarget( context.getCurrentOther());
			fp.setIssueData( missingMethods.stream().map( m -> buildCompositeMethodName(m)).collect( Collectors.toList()));
			fp.getComparisonIssueData().addAll( missingMethods);
			context.addFingerPrint(fp);		 
		
		}
		
		List<MethodEntity> remaining = new ArrayList<>(others);
		remaining.removeAll(present);
		
		if (remaining.size() > 0) {			
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.surplusMethods);			
			fp.setEntityComparisonTarget( context.getCurrentOther());
			fp.setIssueData( remaining.stream().map( m -> buildCompositeMethodName(m)).collect( Collectors.toList()));
			fp.getComparisonIssueData().addAll(remaining);
			context.addFingerPrint(fp);		 					
		}		
	}
	
	/**
	 * @param base
	 * @param other
	 * @return
	 */
	public static void compareMethod( ComparisonContext context, MethodEntity base, MethodEntity other) {
		
		// abstract
		context.pushCurrentProcessFocus( ComparisonProcessFocus.abstractModifier);
		compareAbstractNature( context, base, other);
		context.popCurrentProcessFocus();
		
		// access modifier
		context.pushCurrentProcessFocus( ComparisonProcessFocus.accessModifier);
		compareAccessModifiers( context, base, other);
		context.popCurrentProcessFocus();
		
		// scope modifier
		context.pushCurrentProcessFocus( ComparisonProcessFocus.synchronizedModifier);
		compareSynchronizedNature( context, base, other);
		context.popCurrentProcessFocus();
		
		// static
		context.pushCurrentProcessFocus( ComparisonProcessFocus.staticModifier);
		compareStaticModifiers( context, base, other);
		context.popCurrentProcessFocus();
			

		// argument types
		context.pushCurrentProcessFocus( ComparisonProcessFocus.arguments);
		compareTypeReferences(context, base.getArgumentTypes(), other.getArgumentTypes());
		context.popCurrentProcessFocus();
		
		// return type
		context.pushCurrentProcessFocus( ComparisonProcessFocus.returnType);
		compareEntitiesShallow( context, base.getReturnType().getReferencedType(), other.getReturnType().getReferencedType());
		context.popCurrentProcessFocus();
		
		// exceptions
		// convert to TRE as there's already a collection function for it 
		List<TypeReferenceEntity> cbases = wrapClasses(base.getExceptions());
		List<TypeReferenceEntity> cothers = wrapClasses(other.getExceptions());
		
		context.pushCurrentProcessFocus( ComparisonProcessFocus.exceptions);
		compareTypeReferences( context, cbases, cothers);
		context.popCurrentProcessFocus();				
		
		// annotations
		context.pushCurrentProcessFocus( ComparisonProcessFocus.annotations);
		compareAnnotations(context, base.getAnnotations(), other.getAnnotations());		
		context.popCurrentProcessFocus();
	
	}


	public static void compareSynchronizedNature(ComparisonContext context, HasSynchronizedNature base, HasSynchronizedNature other) {
		boolean bsn = base.getSynchronizedNature();
		boolean osn = other.getSynchronizedNature();
		
		if (bsn != osn) {
			FingerPrint fp = FingerPrintExpert.build( base, ComparisonIssueType.synchronizedModifierMismatch);			
			fp.setEntityComparisonTarget( other);
			context.addFingerPrint(fp);
		}
		 			
	}
	
	public static void compareGenericNature(ComparisonContext context, HasGenericNature base, HasGenericNature other) {
		Boolean bgn = base.getGenericNature();
		Boolean ogn = other.getGenericNature();
		
		if (bgn != ogn) {
			FingerPrint fp = FingerPrintExpert.build( base, ComparisonIssueType.genericityMismatch);			
			fp.setEntityComparisonTarget( other);
			context.addFingerPrint(fp);		
		}
		 		
	}
		
	public static List<TypeReferenceEntity> wrapClasses( Collection<ClassEntity> entities) {
		return entities.stream().map( ce -> {
			TypeReferenceEntity tre = TypeReferenceEntity.T.create();
			tre.setReferencedType(ce);
			return tre;
		}).collect(Collectors.toList());
	}
	
	public static List<TypeReferenceEntity> wrapInterfaces( Collection<InterfaceEntity> entities) {
		return entities.stream().map( ce -> {
			TypeReferenceEntity tre = TypeReferenceEntity.T.create();
			tre.setReferencedType(ce);
			return tre;
		}).collect(Collectors.toList());
	}


	public static void compareStringValues(ComparisonContext context, Set<String> bases, Set<String> others) {
		List<String> missing = new ArrayList<>();

	
		for (String base : bases) {
			if ( others.stream().filter( s -> base.equals(s)).findFirst().orElse(null) == null) {
				missing.add(base);
			}
		}
		others.removeAll(bases);
		
		if (missing.size() > 0) {
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.missingEnumValues);			
			fp.setEntityComparisonTarget( context.getCurrentOther());
			fp.setIssueData(missing);
			context.addFingerPrint(fp);		
					
		}
				
		if (others.size() > 0) {
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.surplusEnumValues);			
			fp.setEntityComparisonTarget( context.getCurrentOther());
			fp.setIssueData( new ArrayList<String>( others));
			context.addFingerPrint(fp);			
		}
		
	}
	
	
	public static void compareTemplateParameters(ComparisonContext context, Map<String, TypeReferenceEntity> pBases, Map<String, TypeReferenceEntity> pOthers) {
		if (pBases.size() == 0 && pOthers.size() == 0)
			return;
		List<String> missing = new ArrayList<>();		
		List<String> matches = new ArrayList<>();
		for (Map.Entry<String, TypeReferenceEntity> entry : pBases.entrySet()) {
			String key = entry.getKey();
			TypeReferenceEntity base = entry.getValue();
			TypeReferenceEntity other = pOthers.get(key);
			if (other == null) {
				missing.add(key);
				continue;
			}			
			matches.add(key);
			compareTypeReference(context, base, other);
		}
		if (missing.size() > 0) {
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.missingTemplateParameters);			
			fp.setEntityComparisonTarget( context.getCurrentOther());
			fp.setIssueData(missing);
			context.addFingerPrint(fp);		
					
		}
		List<String> others = new ArrayList<>( pOthers.keySet());
		others.removeAll( matches);
				
		if (others.size() > 0) {
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.surplusTemplateParameters);			
			fp.setEntityComparisonTarget( context.getCurrentOther());
			fp.setIssueData( new ArrayList<String>( others));
			context.addFingerPrint(fp);			
		}
		
		
	}

	public static void compareAnnotations(ComparisonContext context, Set<TypeReferenceEntity> baseAnnotations, Set<TypeReferenceEntity> otherAnnotations) {
		if (baseAnnotations.size() == 0 && otherAnnotations.size() == 0)
			return;
		
		Map<String, AnnotationEntity> bases = new HashMap<>();
		baseAnnotations.stream().forEach( a -> {
			AnnotationEntity ae = (AnnotationEntity) a.getReferencedType();
			String name = ae.getDeclaringInterface().getReferencedType().getName();
			bases.put( name, ae);
		});
		
		Map<String, AnnotationEntity> others = new HashMap<>();
		otherAnnotations.stream().forEach( a -> {
			AnnotationEntity ae = (AnnotationEntity) a.getReferencedType();
			String name = ae.getDeclaringInterface().getReferencedType().getName();
			others.put( name, ae);
		});
		
		
		List<String> missingTypes = new ArrayList<>();
		for (Map.Entry<String, AnnotationEntity> base : bases.entrySet()) {
						
			// push base						
			if (context.isProcessed( base.getKey())) 
				continue;
			
			AnnotationEntity other = others.get(base.getKey()); 
			
			if (other == null) {
				missingTypes.add(base.getKey());
				continue;
			}
			// push other			
			new StatefulAnnotationComparator(context).compare((AnnotationEntity) base.getValue(), (AnnotationEntity) other);			 
						
		}
		
		Set<String> baseNames = bases.keySet();
		Set<String> otherNames = others.keySet();			
		// other implements NOT ALL interfaces 
		if (missingTypes.size() > 0) {
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.missingAnnotations);
			fp.getIssueData().add( missingTypes.stream().collect(Collectors.joining(",")));
			fp.setEntityComparisonTarget( context.getCurrentOther());
			fp.getComparisonIssueData().addAll( bases.entrySet().stream().filter( e -> missingTypes.contains( e.getKey())).map( e -> e.getValue()).collect(Collectors.toList()));
			context.addFingerPrint(fp);					
		}
		
		// other implements MORE interfaces
		otherNames.removeAll(baseNames);
		if (otherNames.size() > 0) {
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.surplusAnnotations);
			fp.getIssueData().add( otherNames.stream().collect( Collectors.joining(",")));
			fp.getComparisonIssueData().addAll( others.entrySet().stream().filter( e -> otherNames.contains( e.getKey())).map( e -> e.getValue()).collect(Collectors.toList()));
			fp.setEntityComparisonTarget( context.getCurrentOther());
			context.addFingerPrint(fp);
		}
	}
	
}
