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
package com.braintribe.devrock.zed.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.ConsoleStyles;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.devrock.zed.api.context.ConsoleOutputContext;
import com.braintribe.devrock.zed.api.context.ConsoleOutputVerbosity;
import com.braintribe.devrock.zed.commons.Comparators;
import com.braintribe.devrock.zed.forensics.fingerprint.HasFingerPrintTokens;
import com.braintribe.devrock.zed.forensics.fingerprint.register.RatingRegistry;
import com.braintribe.zarathud.model.data.AccessModifier;
import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.AnnotationValueContainer;
import com.braintribe.zarathud.model.data.AnnotationValueContainerType;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ClassEntity;
import com.braintribe.zarathud.model.data.EnumEntity;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.ScopeModifier;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.data.natures.HasTemplateParameters;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;

/**
 * common functionality for the outputters 
 * @author pit
 *
 */
public class BasicConsoleContainerOutputCommons implements HasFingerPrintTokens{
	private static final String lotsOfTabs ="\t\t\t\t\t\t\t\t\t\t\t";
	private static final char titleFrameChar = '*';
	private static final String STRING = " ";
	private static final String DELIMITER_ARGUMENT = " ";
	private static final String DELIMITER_EXCEPTION = " ";

	/**
	 * @param context
	 * @param verbosity
	 * @return
	 */
	protected boolean verbosityLevelAtLeast( ConsoleOutputContext context, ConsoleOutputVerbosity verbosity) {
		ConsoleOutputVerbosity consoleOutputVerbosity = context.verbosity();
		return consoleOutputVerbosity.ordinal() >= verbosity.ordinal();		
	}
	
	/**
	 * @param num - the number of tabs
	 * @return - a string of tabs, max length is defined by 'lotsOfTabs'
	 */
	public static String tabs( int num) {
		if (num > lotsOfTabs.length()) {
			num = lotsOfTabs.length() - 1;
		}
		return lotsOfTabs.substring(0, num);
	}
	
	/**
	 * pads a string with tabs 
	 * @param context
	 * @param string
	 * @return
	 */
	public static String pad( ConsoleOutputContext context,String string) {
		String pad = tabs( context.peekIndent());
		return pad + string;
	}
	/**
	 * pads a new line with tabs 
	 * @param context
	 * @param string
	 * @return
	 */
	public static String padL( ConsoleOutputContext context,String string) {
		return pad( context, string) + "\n";
	}
	
	/**
	 * @param context
	 * @param title
	 */
	public static void title( ConsoleOutputContext context, String title) {
		int len = title.length();
		int llen = len + 2 * 4;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < llen; i++) {
			sb.append(titleFrameChar);
		}
		String frameString = sb.toString();
		
		context.consoleOutputContainer().append( BasicConsoleContainerOutputCommons.padL( context, frameString));
		context.consoleOutputContainer().append( BasicConsoleContainerOutputCommons.padL( context, "*** " + title + " ***"));
		context.consoleOutputContainer().append( BasicConsoleContainerOutputCommons.padL( context, frameString));
	}
	

	/**
	 * attach a list of {@link Artifact} to a {@link ConfigurableConsoleOutputContainer}, either as a comma-delimited single line or multiple lines
	 * @param cc - the {@link ConfigurableConsoleOutputContainer} to attach to
	 * @param artifacts - the List {@link Artifact}
	 * @param severity - the severity of the output 
	 * @param single - true if a single line should be made or false for multiple lines
	 */
	protected void processArtifacts( ConsoleOutputContext context, List<Artifact> artifacts, Severity severity, boolean single) {
		ConfigurableConsoleOutputContainer cc = context.consoleOutputContainer();
		if (single) {
			String line = artifacts.stream().sorted( Comparators.artifact()).map( a -> a.toVersionedStringRepresentation()).collect(Collectors.joining(","));
			
			switch( severity) {
				case major:
					cc.append(ConsoleOutputs.red( padL(context, line)));
					break;
				case minor:
					cc.append(ConsoleOutputs.yellow( padL( context, line)));
					break;
				case none:			
				default:
					cc.append( padL(context, line));
					break;			
			}
		}
		else {
			List<String> anames = artifacts.stream().sorted( Comparators.artifact()).map( a -> a.toVersionedStringRepresentation()).collect(Collectors.toList());
			anames.stream().forEach( n -> {
				switch( severity) {
					case major:
						cc.append(ConsoleOutputs.red( padL( context, n)));
						break;
					case minor:
						cc.append(ConsoleOutputs.yellow( padL( context, n)));
						break;
					case none:			
					default:
						cc.append( padL( context, n));
						break;			
				}
			});			
		}
		
	}
	
	/**
	 * main processor for {@link ZedEntity}
	 * @param context - the {@link ConsoleOutputContext} to use 
	 * @param e - the {@link ZedEntity} to process
	 */
	protected void processZedEntity(ConsoleOutputContext context, ZedEntity e) {		
		if (e instanceof InterfaceEntity) {
			processInterfaceEntity(context, (InterfaceEntity) e);
		}
		else if (e instanceof ClassEntity) {
			processClassEntity( context, (ClassEntity) e);
		}
		else if (e instanceof EnumEntity) {
			processEnumEntity( context, (EnumEntity) e);
		}		
		else if (e instanceof AnnotationEntity) {
			processAnnotation(context, (AnnotationEntity) e); 
		}
		else {
			context.consoleOutputContainer().append( padL(context, "unknown type : " + e.getClass().getName()));
		}
	}
	
	
	
	/**
	 * processor for interfaces 
	 * @param context - the {@link ConsoleOutputContext} to use 
	 * @param e - the {@link InterfaceEntity} to process
	 */
	protected void processInterfaceEntity( ConsoleOutputContext context, InterfaceEntity e) {
		ConfigurableConsoleOutputContainer cc = context.consoleOutputContainer();		
		
		// name
		cc.append( padL(context, "public interface " + e.getName() + dumpTemplateParameters(context, e)));
		
		// generic entity
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.terse)) {
			if (Boolean.TRUE.equals(e.getGenericNature())) {
				context.pushIndent();
				cc.append( padL( context, "generic entity"));
				context.popIndent();
			}
		}
		
		// super interface
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.terse)) {
			Set<InterfaceEntity> superInterfaces = e.getSuperInterfaces().stream().map( i -> (InterfaceEntity) i.getReferencedType()).collect(Collectors.toSet());
			if (superInterfaces.size() > 0) {
				context.pushIndent();
				cc.append( padL( context, "inherited interfaces"));
				context.pushIndent();
				for (InterfaceEntity interfaceEntry : superInterfaces) {
					cc.append( padL( context, interfaceEntry.getName() + dumpArtifactReferences(context, interfaceEntry)));
				}
				context.popIndent();			
				context.popIndent();
			}
		}
		// sub interface
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.terse)) {
			Set<InterfaceEntity> subInterfaces = e.getSubInterfaces();
			if (subInterfaces.size() > 0) {
				context.pushIndent();
				cc.append( padL( context, "inheriting interfaces"));
				context.pushIndent();
				for (InterfaceEntity interfaceEntry : subInterfaces) {
					cc.append( padL( context, interfaceEntry.getName() + dumpArtifactReferences(context, interfaceEntry)));
				}
				context.popIndent();			
				context.popIndent();
			}
		}
		// implementing classes
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.verbose)) {
			Set<ClassEntity> implementingClasses = e.getImplementingClasses();
			if (implementingClasses.size() > 0) {
				context.pushIndent();
				cc.append( padL( context, "implementing classes"));
				context.pushIndent();
				for (ClassEntity classEntity : implementingClasses) {
					cc.append( padL( context, classEntity.getName() + dumpArtifactReferences(context, classEntity)));
				}
				context.popIndent();			
				context.popIndent();
			}			
		}
		
 
		// fields
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.verbose)) {
			List<FieldEntity> fields = e.getFields();
			if (fields.size() > 0) {
				context.pushIndent();
				processFields( context, fields);
				context.popIndent();
			}
		}
		// methods
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.terse)) {
			Set<MethodEntity> methods = e.getMethods();
			if (methods.size() > 0) {
				context.pushIndent();
				processMethods(context, methods, " methods");
				context.popIndent();
			}
		}
		processAnnotations(context, e.getAnnotations());
		
	}
	
	/**
	 * process class
	 * @param context
	 * @param e
	 */
	protected void processClassEntity( ConsoleOutputContext context, ClassEntity e) {
		ConfigurableConsoleOutputContainer cc = context.consoleOutputContainer();
		
		AccessModifier accessModifier = e.getAccessModifier();		
		String prefix = accessModifier != null ? accessModifier.toString().toLowerCase() : "package-private";
		if (e.getAbstractNature()) {
			prefix += " abstract ";
		}		
		String str = prefix + " class " + e.getName();
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.terse)) {
			str += dumpTemplateParameters(context, e);
		}
		cc.append( padL( context, str));
		
		// super type
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.terse)) {
			
			context.pushIndent();
			TypeReferenceEntity superType = e.getSuperType();
			if (superType != null) {
				cc.append( padL(context, "super type : " + dumpTypeReference(context, superType)));
			}
			context.popIndent();
		}
		// super interfaces 
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.terse)) {
			Set<TypeReferenceEntity> implementedInterfaces = e.getImplementedInterfaces();
			if (implementedInterfaces.size() > 0) {
				context.pushIndent();
				cc.append( padL( context, "implemented interfaces : "));
				context.pushIndent();
				for (TypeReferenceEntity tf : implementedInterfaces) {
					cc.append( padL(context, dumpTypeReference(context, tf)));
				}
				context.popIndent();
				context.popIndent();
			}
		}
		// sub types
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.verbose)) {
			Set<ClassEntity> subTypes = e.getSubTypes();
			if (subTypes.size() > 0) {
				context.pushIndent();
				cc.append( padL( context, "sub types : "));
				context.pushIndent();
				for (ClassEntity classEntity : subTypes) {
					cc.append( padL( context, dumpArtifactReferences(context, classEntity)));
				}
				context.popIndent();
				context.popIndent();
			}			
		}
		
		// fields
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.verbose)) {
			List<FieldEntity> fields = e.getFields();
			if (fields.size() > 0) {
				context.pushIndent();
				processFields( context, fields);
				context.popIndent();
			}
		}
	
		// methods
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.terse)) {
			Set<MethodEntity> methods = e.getMethods();
			if (methods.size() > 0) {
				context.pushIndent();
				processMethods(context, methods, "methods");
				context.popIndent();
			}
		}
		processAnnotations(context, e.getAnnotations());
	}
	
	/**
	 * processing fields
	 * @param context
	 * @param fields
	 */
	private void processFields(ConsoleOutputContext context, Collection<FieldEntity> fields) {
		if (fields.size() == 0)
			return;
		context.consoleOutputContainer().append( padL(context, fields.size() + " field(s)"));
		context.pushIndent();
		fields.stream().sorted( Comparators.field()).forEach( f -> {
			processField( context, f);
		});;
		context.popIndent();
	}

	/**
	 * process field
	 * @param context
	 * @param f
	 */
	private void processField(ConsoleOutputContext context, FieldEntity f) {
		AccessModifier accessModifier = f.getAccessModifier();		
		
		String txt = accessToString(accessModifier);
		ScopeModifier scopeModifier = f.getScopeModifier();
		if (scopeModifier != ScopeModifier.DEFAULT) {
			txt += " " + scopeToString(scopeModifier);
		}
		if (f.getInitializer() != null) {
			txt += " " + f.getName() + " " + dumpTypeReference(context,  f.getType()) + " (" + f.getInitializer().toString() + ")";
		}
		else {
			txt += " " + f.getName() + " " + dumpTypeReference(context,  f.getType());
		}
		context.consoleOutputContainer().append( padL( context, txt));
		processAnnotations(context, f.getAnnotations());
	}

	/**
	 * process methods
	 * @param context
	 * @param methods
	 */
	protected void processMethods( ConsoleOutputContext context, Collection<MethodEntity> methods, String title) {
		if (methods.size() == 0)
			return;
		context.consoleOutputContainer().append( padL(context, methods.size() + " " + title));
		context.pushIndent();
		methods.stream().sorted( Comparators.method()).forEach( f -> {
			processMethod( context, f);
		});;
		context.popIndent();
	}
	/**
	 * process a method 
	 * @param context
	 * @param m
	 */
	private void processMethod(ConsoleOutputContext context, MethodEntity m) {		
		StringBuilder builder = new StringBuilder(); 
		AccessModifier accessModifier = m.getAccessModifier();
		builder.append( accessToString(accessModifier));
		
		if (m.getAbstractNature()) {
			builder.append(" abstract");
		}
		if (m.getStaticNature()) {
			builder.append(" static");
		}		
		if (m.getSynchronizedNature()) {
			builder.append( " synchronized");
		}
		
		// a method of an interface with a body (and not the initializer -> must be a default function 
		if (m.getOwner() instanceof InterfaceEntity && !m.getName().equalsIgnoreCase("<clinit>") && m.getIsDefault()) { 
			builder.append( " default");			
		}
		
				
		builder.append( dumpMethod(context, m));		
		
		
		Set<ClassEntity> exceptions = m.getExceptions();
		if (exceptions.size() > 0) {
			builder.append(" throws ");
			StringBuilder exceptionBuilder = new StringBuilder();
			for (ClassEntity exception : exceptions) {
				if (exceptionBuilder.length() > 0)
					exceptionBuilder.append(DELIMITER_EXCEPTION);
				exceptionBuilder.append( exception.getName());
			}
			builder.append( exceptionBuilder.toString());			
		}			
		
		context.consoleOutputContainer().append( padL( context, builder.toString()));
		
		processAnnotations(context, m.getAnnotations());		
		
		processBodyTypes( context, m.getBodyTypes());
	}

	/**
	 * collate the method body's type references
	 * @param context
	 * @param bodyTypes
	 */
	private void processBodyTypes(ConsoleOutputContext context, List<TypeReferenceEntity> bodyTypes) {
		
		List<TypeReferenceEntity> trs = bodyTypes.stream().filter( t -> {
			return !t.getReferencedType().getArtifacts().contains(context.runtimeArtifact());
		}).collect( Collectors.toList());
		
		
		if (trs.size() > 0) {
			if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.taciturn)) {
				// setup 
				Map<String, Integer> occurence = new HashMap<>();
				Map<String, TypeReferenceEntity> lookup = new HashMap<>();
				for (TypeReferenceEntity tr : trs) {
					String key = tr.getReferencedType().getName();
					Integer v = occurence.get( key);
					if (v == null) {
						v = Integer.valueOf(1);
					}
					else {
						v++;						
					}
					occurence.put(key, v);
					lookup.computeIfAbsent( key, k -> tr);
					
				}
				

				context.pushIndent();
				context.consoleOutputContainer().append( padL( context, "type references in body : "));

				context.pushIndent();
				
				List<String> keys = occurence.keySet().stream().sorted().collect( Collectors.toList());
				keys.stream().forEach( s -> {
					TypeReferenceEntity tr = lookup.get(s);
					Integer num = occurence.get(tr.getReferencedType().getName());
					context.consoleOutputContainer().append( padL( context, dumpTypeReference(context, tr) + "(" + num + ")") );
				});
				
				context.popIndent();
				context.popIndent();
			}
		}		
	}

	/**
	 * dumps return and argument types (no modifiers)
	 * @param context - 
	 * @param m
	 * @return
	 */
	protected String dumpMethod( ConsoleOutputContext context, MethodEntity m) {
		StringBuilder builder = new StringBuilder();
		builder.append( STRING + dumpTypeReference(context, m.getReturnType()));						
		
		
		builder.append( STRING + m.getName());
		
		builder.append( "(");
					
		if (m.getArgumentTypes().size() > 0) {
			StringBuilder argumentBuilder = new StringBuilder();
			
			for (TypeReferenceEntity argument: m.getArgumentTypes()) {
				if (argumentBuilder.length() > 0) {
					argumentBuilder.append(DELIMITER_ARGUMENT);
				}
				argumentBuilder.append( dumpTypeReference(context, argument));
			}
		
			builder.append( argumentBuilder.toString());
		}
		builder.append(")");
		return builder.toString();
	}
	
	/**
	 * @param context
	 * @param e
	 */	
	protected void processEnumEntity( ConsoleOutputContext context, EnumEntity e) {
		ConfigurableConsoleOutputContainer cc = context.consoleOutputContainer();
		
		String prefix = e.getAccessModifier().toString().toLowerCase();
		if (e.getAbstractNature()) {
			prefix += " abstract ";
		}		
		String str = prefix + " enum " + e.getName();
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.terse)) {
			str += dumpTemplateParameters(context, e);
		}
		cc.append( padL( context, str));
		
		// super type
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.terse)) {
			
			context.pushIndent();
			TypeReferenceEntity superType = e.getSuperType();
			if (superType != null) {
				cc.append( padL(context, "super type : " + dumpTypeReference(context, superType)));
			}
			context.popIndent();
		}
		// super interfaces 
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.terse)) {
			Set<TypeReferenceEntity> implementedInterfaces = e.getImplementedInterfaces();
			if (implementedInterfaces.size() > 0) {
				context.pushIndent();
				cc.append( padL( context, "implemented interfaces : "));
				context.pushIndent();
				for (TypeReferenceEntity tf : implementedInterfaces) {
					cc.append( padL(context, dumpTypeReference(context, tf)));
				}
				context.popIndent();
				context.popIndent();
			}
		}
		// sub types
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.verbose)) {
			Set<ClassEntity> subTypes = e.getSubTypes();
			if (subTypes.size() > 0) {
				context.pushIndent();
				cc.append( padL( context, "sub types : "));
				context.pushIndent();
				for (ClassEntity classEntity : subTypes) {
					cc.append( padL( context, dumpArtifactReferences(context, classEntity)));
				}
				context.popIndent();
				context.popIndent();
			}			
		}
		
		// fields
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.verbose)) {
			List<FieldEntity> fields = e.getFields();
			if (fields.size() > 0) {
				context.pushIndent();
				processFields( context, fields);
				context.popIndent();
			}
		}
	
		// methods
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.terse)) {
			Set<MethodEntity> methods = e.getMethods();
			if (methods.size() > 0) {
				context.pushIndent();
				processMethods(context, methods, "methods");
				context.popIndent();
			}
		}
		processAnnotations(context, e.getAnnotations());
	}
	
	/**
	 * process annotations 
	 * @param context
	 * @param annotations
	 */
	private void processAnnotations( ConsoleOutputContext context, Collection<TypeReferenceEntity> annotations) {
		if (annotations.size() > 0) {
			context.pushIndent();
			if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.taciturn)) {
				context.consoleOutputContainer().append( padL( context, "annotations : "));
				context.pushIndent();
				for (TypeReferenceEntity annotation : annotations) {
					processAnnotation(context, (AnnotationEntity) annotation.getReferencedType());
				}
				context.popIndent();
			}
			context.popIndent();
		}
	}
	
	/**
	 * process annotation
	 * @param context
	 * @param entry
	 */
	private void processAnnotation( ConsoleOutputContext context, AnnotationEntity entry)  {
		ConfigurableConsoleOutputContainer consoleOutputContainer = context.consoleOutputContainer();
		consoleOutputContainer.append( padL( context, entry.getDeclaringInterface().getReferencedType().getName() + dumpArtifactReferences( context, entry.getDeclaringInterface().getReferencedType())));
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.verbose)) {
			context.pushIndent();
			for (Entry<String, AnnotationValueContainer> slot : entry.getMembers().entrySet()) {
				consoleOutputContainer.append( padL( context, "key :" + slot.getKey()));			
				AnnotationValueContainer container = slot.getValue();			
				if (container != null) {
					//context.pushIndent();
					processAnnotationContainer(context, slot.getValue());
					//context.popIndent();
				}
			}
			context.popIndent();
		}
		
	}
	
	/**
	 * process annotation value container
	 * @param context
	 * @param container
	 */
	private void processAnnotationContainer(ConsoleOutputContext context, AnnotationValueContainer container) {
		ConfigurableConsoleOutputContainer consoleOutputContainer = context.consoleOutputContainer();
		AnnotationValueContainerType containerType = container.getContainerType();
		
		switch (containerType) {
			case annotation:				
				processAnnotation(context, container.getOwner());
				break;
			case collection:
				context.pushIndent();
				for (AnnotationValueContainer child : container.getChildren()) {				
					processAnnotationContainer(context, child);
				}				
				context.popIndent();
				break;
			case s_boolean:
				consoleOutputContainer.append( padL(context, "value :" + container.getSimpleBooleanValue()));				
				break;
			case s_date:
				consoleOutputContainer.append( padL(context, "value :" + container.getSimpleDateValue()));
				break;
			case s_double:
				consoleOutputContainer.append( padL(context, "value :" + container.getSimpleDoubleValue()));				
				break;
			case s_float:
				consoleOutputContainer.append( padL(context, "value :" + container.getSimpleFloatValue()));				
				break;
			case s_int:
				consoleOutputContainer.append( padL(context, "value :" + container.getSimpleIntegerValue()));				
				break;
			case s_long:
				consoleOutputContainer.append( padL(context, "value :" + container.getSimpleLongValue()));				
				break;
			case s_string:
				consoleOutputContainer.append( padL(context, "value :" + container.getSimpleStringValue()));				
				break;
			default:
				break;		
		}
	}
	
	/*
	 * STRING BUILDER ATOMS BELOW
	 */
	
	
	/**
	 * builds string representation of the passed {@link Artifact}
	 * @param context - {@link ConsoleOutputContext} to access the verbosity level
	 * @param artifact - the {@link Artifact}
	 * @return - a string representation
	 */
	protected String dumpArtifactReference(ConsoleOutputContext context, Artifact artifact) {
		String str = artifact.toVersionedStringRepresentation();
		switch (context.verbosity()) {
		case taciturn:
			return "";			
		case terse:
			Artifact runtimeArtifact = context.runtimeArtifact();
			if (runtimeArtifact != null && runtimeArtifact.compareTo(artifact) == 0) {
				return ""; 			
			}
			return str;
		case verbose:
		case garrulous:
		default:
			return str;
		
		}			
	}

	/**
	 * builds a string representation of the passed {@link ZedEntity}
	 * @param context - {@link ConsoleOutputContext} to access the verbosity level
	 * @param abstractType - {@link ZedEntity}
	 * @return - string representation
	 */
	protected String dumpArtifactReferences( ConsoleOutputContext context, ZedEntity abstractType) {
		List<Artifact> artifacts = abstractType.getArtifacts();
		if (artifacts.size() == 1) {
			String str = dumpArtifactReference( context, artifacts.get(0));
			if (str.length() == 0)
				return "";
			return "@" + str;
		}
		StringBuilder builder = new StringBuilder();
		for (Artifact artifact : artifacts) {
				String str = dumpArtifactReference(context, artifact);
				if (str.length() == 0)
					continue;
				if (builder.length() > 0) 
					builder.append(",");
			builder.append( str);
		}
		if (builder.length() == 0) 
			return "";
		return "@(" + builder.toString() + ")";
		
	}
	
	/**
	 * builds a string representation of the passed {@link TypeReferenceEntity}
	 * @param context - {@link ConsoleOutputContext} to access the verbosity level
	 * @param typeRef - the {@link TypeReferenceEntity}
	 * @return - string representation 
	 */
	protected String dumpTypeReference( ConsoleOutputContext context, TypeReferenceEntity typeRef) {
		if (typeRef == null) {
			return "-nil-";
		}
		StringBuilder builder = new StringBuilder();

		ZedEntity refType = typeRef.getReferencedType();
				
		// actual type
		String typeString = refType.getName() + dumpArtifactReferences( context, refType);
		builder.append( typeString);
	
		// parameterization 	
		if (typeRef.getParameterization().size() > 0) {				
			builder.append( '<');
			for (TypeReferenceEntity parameter : typeRef.getParameterization()) {
				if (builder.charAt( builder.length()-1) != '<') {
					builder.append(',');
				}							
				if (parameter.getReferencedType() == typeRef.getReferencedType()) {
					builder.append( typeString);
				}
				else {
					builder.append( dumpTypeReference( context, parameter));
				}
				
			}
			builder.append( '>');
		}
		return builder.toString();		
	}
	/**
	 * builds a string representation of the template parameters  
	 * @param context - {@link ConsoleOutputContext} to access the verbosity level
	 * @param z - the {@link ZedEntity}
	 * @return - a string representation
	 */
	protected String dumpTemplateParameters(ConsoleOutputContext context, ZedEntity z) {
		if (z instanceof HasTemplateParameters) {
			HasTemplateParameters tp = (HasTemplateParameters) z;
			Collection<TypeReferenceEntity> values = tp.getTemplateParameters().values();
			if (values.size() > 0) {
				StringBuilder builder = new StringBuilder();
				for (TypeReferenceEntity ref : values) {
					if (builder.length() > 0) 
						builder.append(',');
					builder.append( dumpTypeReference(context, ref));
				}
				return "<" + builder.toString() + ">";
			}
		}
		return "";
	}
	
	
	/**
	 * string representation of the the {@link AccessModifier}
	 * @param modifier
	 * @return
	 */
	protected static String accessToString( AccessModifier modifier) {
		switch (modifier) {
			case PRIVATE:
				return "private ";
			case PROTECTED:
				return "protected ";
			case PUBLIC:
				return "public ";
			default:
			case PACKAGE_PRIVATE:
				return "";		
		}	
	}
	
	/**
	 * string representation of the {@link ScopeModifier}
	 * @param modifier
	 * @return
	 */
	protected static String scopeToString( ScopeModifier modifier) {
		switch (modifier) {
		case FINAL:
			return "final ";
		case VOLATILE:
			return "volatile ";
		default:
		case DEFAULT:
			return "";		
		}
	}
	
	/**
	 * return the color for the different {@link ForensicsRating}
	 * @param frating
	 * @return
	 */
	protected int styleForForensicsRating( ForensicsRating frating) {
		switch (frating) {				
		case ERROR:
			return ConsoleStyles.FG_RED;			
		case WARN:
			return ConsoleStyles.FG_YELLOW;
		case INFO:
			return ConsoleStyles.FG_BLUE;			
		case OK:
		default:			
			return ConsoleStyles.FG_DEFAULT;				
		}
	}
	
	/**
	 * processes the passed codes while showing the worst rating amongst the codes  
	 * @param context - the {@link ConsoleOutputContext}
	 * @param rating - the worst rating amongst the codes 
	 * @param prints - the {@link FingerPrint}s involved 
	 */
	protected void processForensicRatingsAndCode( ConsoleOutputContext context, ForensicsRating rating, Collection<FingerPrint> prints) {
				 
		int style = styleForForensicsRating(rating);
		if (rating.ordinal() <= ForensicsRating.OK.ordinal() && !verbosityLevelAtLeast(context, ConsoleOutputVerbosity.verbose)) 
			return;
		
		Map<ForensicsRating, List<FingerPrint>> ratedFingerPrints = context.ratings().rateFingerPrints(prints);
			
		ForensicsRating worstRating = context.ratings().getWorstRatingOfFingerPrints( ratedFingerPrints);				
			
		String fingerPrintString = dumpForensicRatingsAndCode( worstRating, ratedFingerPrints);
		String line = padL(context, fingerPrintString);
		//String line = padL(context, dumpForensicRatingsAndCode(rating, prints));
		context.consoleOutputContainer().append( ConsoleOutputs.styled(style, line));
	}
	
	/**
	 * dumps the worst rating amongst the passed codes and the passed codes
	 * @param rating - the worst rating amongst the code
	 * @param codes - the {@link FingerPrint}s involved
	 * @return - the string representation 
	 */
	public static String dumpForensicRatingsAndCode( ForensicsRating rating, Collection<FingerPrint> prints) {
		if (prints == null) {
			return "";
		}
		String ratingAsString = rating.toString();
		StringBuilder builder = new StringBuilder();
		
		// a) 
		
		for (FingerPrint print : prints) {
			if (builder.length() > 0) {
				builder.append( ",");
			}
			builder.append( print.getSlots().get( ISSUE));
		}
		String codesAsString = builder.toString();
		if (codesAsString.length() > 0)
			return ratingAsString + " : " + codesAsString;
		else
			return ratingAsString;
	}
	
	public static String dumpForensicRatingsAndCode( ForensicsRating worstRating, Map<ForensicsRating, List<FingerPrint>> map) {
		
		// sort ratings by severity 
		List<ForensicsRating> ratings = new ArrayList<>( map.keySet());
		
		ratings.sort( new Comparator<ForensicsRating>() {

			@Override
			public int compare(ForensicsRating o1, ForensicsRating o2) {
				
				if (o1.ordinal() > o2.ordinal()) {
					return -1;
				}
				else if (o1.ordinal() < o2.ordinal()) {
					return 1;
				}
				return 0;					
			}			
		});
		
		StringBuilder mainStringBuilder = new StringBuilder();
		for (ForensicsRating rating : ratings) {
			List<FingerPrint> prints = map.get(rating);			
			if (prints.size() > 0) {
				List<FingerPrint> coalesce = RatingRegistry.coalesce(prints);			
				String ratingsAsString = rating.name();
					if (mainStringBuilder.length() > 0) {
						mainStringBuilder.append(";");
					}					
					mainStringBuilder.append( ratingsAsString +  ":");
					
					StringBuilder localBuilder = new StringBuilder();
					for (FingerPrint print : coalesce) {
						if (localBuilder.length() > 0) {
							localBuilder.append( ",");
						}
						localBuilder.append( print.getSlots().get( ISSUE));
					}
					mainStringBuilder.append(localBuilder.toString());
			}												
		}
		
		return mainStringBuilder.toString();			
	}
	
	
}
