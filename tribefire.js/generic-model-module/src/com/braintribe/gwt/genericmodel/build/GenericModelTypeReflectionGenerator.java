// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.gwt.genericmodel.build;

import static com.braintribe.model.generic.tools.GmValueCodec.EnumParsingMode.enumAsStringArray;
import static com.braintribe.model.processing.core.commons.SelectiveInformationSupport.SI_ID;
import static com.braintribe.model.processing.core.commons.SelectiveInformationSupport.SI_TYPE;
import static com.braintribe.model.processing.core.commons.SelectiveInformationSupport.SI_TYPE_ALT;
import static com.braintribe.model.processing.core.commons.SelectiveInformationSupport.SI_TYPE_SHORT;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.genericmodel.build.context.EntityDesc;
import com.braintribe.gwt.genericmodel.build.context.PropertyDesc;
import com.braintribe.gwt.genericmodel.build.context.TransientPropertyDesc;
import com.braintribe.gwt.genericmodel.client.CompoundPropertyRetrieval;
import com.braintribe.gwt.genericmodel.client.itw.GwtCompileTimeProperty;
import com.braintribe.gwt.genericmodel.client.itw.GwtScriptProperty;
import com.braintribe.gwt.genericmodel.client.itw.InitialEntityTypes;
import com.braintribe.gwt.genericmodel.client.reflect.AbstractGwtGenericModelTypeReflection;
import com.braintribe.gwt.template.client.Template;
import com.braintribe.gwt.template.client.TemplateException;
import com.braintribe.gwt.template.client.model.MergeContext;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.GmSystemInterface;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.annotation.Transient;
import com.braintribe.model.generic.annotation.meta.Confidential;
import com.braintribe.model.generic.base.EntityBase;
import com.braintribe.model.generic.base.GenericBase;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.AbstractProperty;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.type.custom.AbstractEntityType;
import com.braintribe.model.processing.core.commons.SelectiveInformationSupport;
import com.braintribe.model.processing.itw.InitializerTools;
import com.braintribe.model.processing.itw.InitializerTools.EnumHint;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JEnumConstant;
import com.google.gwt.core.ext.typeinfo.JEnumType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import com.google.gwt.core.ext.typeinfo.JWildcardType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

public class GenericModelTypeReflectionGenerator extends Generator {

	private static GmtrGeneratorHelper gmtrGeneratorHelper;

	@Override
	public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		if (gmtrGeneratorHelper == null) {
			gmtrGeneratorHelper = new GmtrGeneratorHelper(logger, context);
			gmtrGeneratorHelper.generate();
		}
		return "com.braintribe.gwt.genericmodel.client.reflection.GwtGenericModelTypeReflection";
	}

	public static String generateExtraInitialTypes() throws Exception {
		return gmtrGeneratorHelper.generateExtraInitialTypes();
	}

	private static class GmtrGeneratorHelper {

		private final TreeLogger logger;
		private final GeneratorContext context;
		private final boolean scriptMode;
		private final TypeOracle typeOracle;

		private final JClassType evaluatorType;
		private final Set<JClassType> maybeNextEntityTypes = newSet();
		private final Map<JClassType, EntityDesc> entityDescriptors = newMap();
		private final List<EntityDesc> newTypeDescriptors = newList();
		private final Set<JClassType> uninitializedTypes = newSet();

		private int nextEntityTypesId;
		private String nextEntityTypesIfaceFullName;

		private final JClassType genericEntityJClassType;
		private final JClassType objectJClassType;
		private final JClassType entityBaseJClassType;
		private final JClassType genericBaseJClassType;
		private final JClassType listJClassType;
		private final JClassType setJClassType;
		private final JClassType mapJClassType;
		private final JClassType evalContextType;

		private static final Set<String> simpleNames = asSet("boolean", "integer", "long", "float", "double", "decimal", "date", "string");

		static {
			Stream.of(Boolean.class, Integer.class, Long.class, Float.class, Double.class, BigDecimal.class, String.class, Date.class)
					.map(Class::getName) //
					.forEach(simpleNames::add);
		}

		// These are the types that we really need to weave, based on the traversing done by the GWT compile (and our
		// collecting)
		private final Set<String> essentialTypes = GwtCompilationInfo.getEssentialTypes();

		public GmtrGeneratorHelper(TreeLogger logger, GeneratorContext context) {
			this.logger = logger;
			this.context = context;
			this.scriptMode = context.isProdMode();

			this.typeOracle = context.getTypeOracle();

			this.genericEntityJClassType = findType(GenericEntity.class);
			this.objectJClassType = findType(Object.class);
			this.entityBaseJClassType = findType(EntityBase.class);
			this.genericBaseJClassType = findType(GenericBase.class);
			this.listJClassType = findType(List.class);
			this.setJClassType = findType(Set.class);
			this.mapJClassType = findType(Map.class);
			this.evaluatorType = findType(Evaluator.class).isGenericType().getRawType();
			this.evalContextType = findType(EvalContext.class).isGenericType().getRawType();
		}

		private JClassType findType(Class<?> clazz) {
			return typeOracle.findType(clazz.getName());
		}

		public void generate() throws UnableToCompleteException {
			try {
				generateHelper();

			} catch (Exception e) {
				logger.log(Type.ERROR, "error while merging template", e);
				throw new UnableToCompleteException();
			}
		}

		private void generateHelper() throws Exception {
			final String packageName = "com.braintribe.gwt.genericmodel.client.reflection";
			final String className = "GwtGenericModelTypeReflection";
			final String superClassName = gmtrSuperClassName();

			PrintWriter printWriter = context.tryCreate(logger, packageName, className);

			findEntityClasses();
			String entityTypeRefs = weaveEntityClasses();

			VelocityContext velocityContext = new VelocityContext();
			velocityContext.put("package", packageName);
			velocityContext.put("className", className);
			velocityContext.put("superClassName", superClassName);
			velocityContext.put("entityTypeRefsStreamExpression", entityTypeRefsStreamExpression(entityTypeRefs));
			velocityContext.put("nextTypesStreamExpression", nextTypesStreamExpression(entityTypeRefs));

			write("/com/braintribe/gwt/genericmodel/build/GwtGenericModelTypeReflection.java.vm", velocityContext, printWriter);

			logReport();
		}

		private String gmtrSuperClassName() {
			return AbstractGwtGenericModelTypeReflection.class.getName();
		}

		private void findEntityClasses() {
			uninitializedTypes.add(genericEntityJClassType);
			for (JClassType subType : genericEntityJClassType.getSubtypes()) {
				/* If our GenericEntity sub-type is not an interface, we automatically treat it as a GmSystemInterface now. Currently, this is the
				 * case for example for the special "pseudo" classes used in JavaTypeAnalysis. */
				if (subType.isInterface() != null && subType.getAnnotation(GmSystemInterface.class) == null) {
					if (essentialTypes.isEmpty() || essentialTypes.contains(subType.getQualifiedSourceName()))
						uninitializedTypes.add(subType);
					else
						maybeNextEntityTypes.add(subType);
				}
			}
		}

		/* package */ String generateExtraInitialTypes() throws Exception {
			newTypeDescriptors.clear();

			final String packageName = InitialEntityTypes.class.getPackage().getName();
			final String className = "InitialEntityTypesImpl" + nextEntityTypesId;
			final String ifaceName = nextEntityTypesIfaceFullName;
			final String fullClassName = packageName + "." + className;

			PrintWriter printWriter = context.tryCreate(logger, packageName, className);
			if (printWriter == null)
				return fullClassName;

			findExtraClasses();

			String entityTypeRefs = weaveExtraEntityClasses();

			VelocityContext velocityContext = new VelocityContext();
			velocityContext.put("package", packageName);
			velocityContext.put("className", className);
			velocityContext.put("ifaceName", ifaceName);
			velocityContext.put("entityTypeRefsStreamExpression", entityTypeRefsStreamExpression(entityTypeRefs));
			velocityContext.put("nextTypesStreamExpression", nextTypesStreamExpression(entityTypeRefs));

			write("/com/braintribe/gwt/genericmodel/build/InitialEntityTypesImpl.java.vm", velocityContext, printWriter);

			logReport();

			return fullClassName;
		}

		private void findExtraClasses() {
			for (JClassType entityType : maybeNextEntityTypes)
				if (essentialTypes.contains(entityType.getQualifiedSourceName()))
					uninitializedTypes.add(entityType);
		}

		private String weaveExtraEntityClasses() throws Exception {
			return uninitializedTypes.isEmpty() ? "" : weaveEntityClasses();
		}

		private String weaveEntityClasses() throws Exception {
			while (!uninitializedTypes.isEmpty()) {
				// copy collection to avoid concurrent modification
				for (JClassType entityClass : newList(uninitializedTypes))
					acquireEntityDesc(entityClass, true);
			}

			generateNextInitialEntityTypes();

			List<String> entityTypeRefs = newList();
			for (EntityDesc entityDesc : newTypeDescriptors) {
				generateEntityType(entityDesc);
				generateEnhancedEntity(entityDesc);

				entityTypeRefs.add(entityDesc.getEntityTypeSingletonRef());
			}

			return entityTypeRefs.stream().collect(Collectors.joining(",\n"));
		}

		private String entityTypeRefsStreamExpression(String entityTypeRefs) {
			if (entityTypeRefs.isEmpty())
				return "java.util.stream.Stream.empty()";
			else
				return "java.util.stream.Stream.concat(java.util.stream.Stream.of(\n" + entityTypeRefs //
						+ "\n), nextTypes())";
		}

		private String nextTypesStreamExpression(String entityTypeRefs) {
			if (entityTypeRefs.isEmpty())
				return "java.util.stream.Stream.empty()";
			else
				return "com.google.gwt.core.client.GWT.<" + nextEntityTypesIfaceFullName + ">" + //
						"create(" + nextEntityTypesIfaceFullName + ".class).extraInitialTypes()";
		}

		private void generateNextInitialEntityTypes() throws Exception {
			int currentEntityTypesId = ++nextEntityTypesId;

			String packageName = InitialEntityTypes.class.getPackage().getName();
			String ifaceName = InitialEntityTypes.class.getSimpleName() + currentEntityTypesId;

			nextEntityTypesIfaceFullName = packageName + "." + ifaceName;

			PrintWriter printWriter = context.tryCreate(logger, packageName, ifaceName);

			VelocityContext velocityContext = new VelocityContext();
			velocityContext.put("package", packageName);
			velocityContext.put("ifaceName", ifaceName);

			write("/com/braintribe/gwt/genericmodel/build/InitialEntityTypesInterface.java.vm", velocityContext, printWriter);
		}

		private void generateEntityType(EntityDesc entityDesc) throws Exception {
			JClassType entityIface = entityDesc.entityIface;
			String packageName = entityIface.getPackage().getName();
			String className = entityDesc.getEntityTypeClassSimpleName();

			PrintWriter printWriter = context.tryCreate(logger, packageName, className);
			if (printWriter == null) {
				return;
			}

			SelectiveInformation selectiveInformation = entityIface.getAnnotation(SelectiveInformation.class);
			ToStringInformation toStringInformation = entityIface.getAnnotation(ToStringInformation.class);

			VelocityContext velocityContext = new VelocityContext();
			velocityContext.put("package", packageName);
			velocityContext.put("className", className);
			velocityContext.put("enhancedName", entityDesc.getEnhancedClassSimpleName());
			velocityContext.put("entityDesc", entityDesc);
			velocityContext.put("properties", entityDesc.properties.values());
			velocityContext.put("transientProperties", entityDesc.transientProperties.values());
			velocityContext.put("superTypeRefs", getSuperTypeRefs(entityDesc));
			// TODO remove everything related to defaultMethods
			velocityContext.put("defaultMethods", Collections.emptySet());

			velocityContext.put("selectiveInformation", getSelectiveInformationSourceCode(entityIface, selectiveInformation));
			velocityContext.put("toStringInformation", getToStringSourceCode(entityIface, toStringInformation));

			velocityContext.put("scriptMode", scriptMode);

			write("/com/braintribe/gwt/genericmodel/build/EntityType.java.vm", velocityContext, printWriter);
		}

		private void generateEnhancedEntity(EntityDesc entityDesc) throws Exception {
			JClassType entityIface = entityDesc.entityIface;

			String packageName = entityIface.getPackage().getName();
			String className = entityDesc.getEnhancedClassSimpleName();

			PrintWriter printWriter = context.tryCreate(logger, packageName, className);
			if (printWriter == null)
				return;

			VelocityContext velocityContext = new VelocityContext();
			velocityContext.put("package", packageName);
			velocityContext.put("propertySuperType", getPropertySuperType());
			velocityContext.put("entityDesc", entityDesc);
			velocityContext.put("enhancedName", className);
			velocityContext.put("properties", entityDesc.properties.values());
			velocityContext.put("transientProperties", entityDesc.transientProperties.values());
			// TODO remove everything related to defaultMethods
			velocityContext.put("defaultMethods", Collections.emptySet());

			velocityContext.put("scriptMode", scriptMode);

			write("/com/braintribe/gwt/genericmodel/build/EnhancedEntity.java.vm", velocityContext, printWriter);
		}

		private String getPropertySuperType() {
			return (scriptMode ? GwtScriptProperty.class.getName() : AbstractProperty.class.getName());
		}

		private void write(String templateName, VelocityContext velocityContext, PrintWriter printWriter) throws Exception {
			VelocityEngine engine = getVelocityEngine();
			engine.mergeTemplate(templateName, "ISO-8859-1", velocityContext, printWriter);
			context.commit(logger, printWriter);
		}

		private void logReport() {
			if (newTypeDescriptors.isEmpty())
				return;

			try {
				List<PropertyDesc> relevantProperties = entityDescriptors.values().stream() //
						.flatMap(e -> e.properties.values().stream()) //
						.filter(p -> (!p.getIsInheritedFromSuperclass() || !p.isOverlay)) //
						.collect(Collectors.toList());

				int entities = entityDescriptors.size();
				int uniquePropertyClasses = (int) relevantProperties.stream().filter(p -> !p.getIsInheritedFromSuperclass()).count();
				int propertyAccessorMethods = relevantProperties.size();

				System.out.println("########################################################################");
				System.out.println("[TypeReflection] Generated entities: " + entities);
				System.out.println("[TypeReflection] Generated property classes: " + uniquePropertyClasses);
				System.out.println("[TypeReflection] Generated accessor methods of each kind (e.g. property getter): " + propertyAccessorMethods);
				System.out.println("########################################################################");

			} catch (Exception e) {
				logger.log(Type.ERROR, "Error while building report for TypeReflection generator.", e);
			}
		}

		// ##################################################################
		// ## . . . . . . Creating Entity/Property Descriptors . . . . . . ##
		// ##################################################################

		private EntityDesc acquireEntityDesc(JClassType type, boolean initializeProps) {
			EntityDesc result = entityDescriptors.get(type);
			if (result == null) {
				result = createEntityDesc(type);
				entityDescriptors.put(type, result);
				uninitializedTypes.add(type);
				newTypeDescriptors.add(result);
			}

			if (result.properties == null && initializeProps) {
				fillProperties(result, type);
				uninitializedTypes.remove(type);
				maybeNextEntityTypes.remove(type);
			}

			return result;
		}

		private EntityDesc createEntityDesc(JClassType type) {
			EntityDesc result = new EntityDesc();
			result.entityIface = type;
			result.isAbstract = isAbstract(type);

			return result;
		}

		private static boolean isAbstract(JClassType type) {
			return type.getAnnotation(Abstract.class) != null;
		}

		private void fillProperties(EntityDesc result, JClassType type) {
			collectPropertyDescriptors(result, type);

			EntityDesc stwmp = null;
			for (JClassType superIface : type.getImplementedInterfaces()) {
				if (!isGenericEntity(superIface))
					continue;

				EntityDesc superDesc = acquireEntityDesc(superIface, true);
				result.addSuperType(superDesc);

				if (stwmp == null || stwmp.properties.size() < superDesc.properties.size())
					result.superTypeWithMostProperties = stwmp = superDesc;
			}
		}

		private void collectPropertyDescriptors(EntityDesc entityDesc, JClassType entityIface) {
			Map<String, PropertyDesc> properties = newMap();
			Map<String, TransientPropertyDesc> transientProperties = newMap();

			Collection<JMethod> methods = getOverridableMethods(entityIface);

			for (JMethod method : methods) {
				if (!method.isPublic() || method.isStatic())
					continue;

				boolean propertyRelatedMethod = false;

				if (isGetter(method)) {
					String name = method.getName();
					JMethod getter = method;
					JType originalReturnType = rawifyIfTypeVar(getter.getReturnType());

					String setterName = "set" + name.substring(3);
					JMethod setter = getOverridableMethod(entityIface, setterName, new JType[] { originalReturnType });

					if (setter == null || !setter.isPublic())
						continue;

					propertyRelatedMethod = true;

					JType objectReturnType = ensureObjectType(originalReturnType);
					String commonPart = name.substring(4);
					String propertyName = Character.toLowerCase(name.charAt(3)) + commonPart;

					if (isTransient(getter)) {
						TransientPropertyDesc propertyDesc = new TransientPropertyDesc();
						propertyDesc.name = propertyName;
						propertyDesc.Name = PropertyDesc.firstLetterToUpperCase(propertyName);
						propertyDesc.ownerTypeDesc = entityDesc;
						propertyDesc.originalType = originalReturnType.getParameterizedQualifiedSourceName();

						transientProperties.put(propertyName, propertyDesc);

						continue;
					}

					PropertyDesc propertyDesc = new PropertyDesc();
					propertyDesc.name = propertyName;
					propertyDesc.Name = PropertyDesc.firstLetterToUpperCase(propertyName);
					propertyDesc.typeRef = getTypeRef(objectReturnType, new Owner(entityIface, propertyName));
					propertyDesc.returnType = objectReturnType.getParameterizedQualifiedSourceName();
					propertyDesc.setOriginalType(originalReturnType.getParameterizedQualifiedSourceName());
					propertyDesc.jType = originalReturnType;
					propertyDesc.ownerTypeDesc = entityDesc;
					propertyDesc.isPrimitive = originalReturnType.isPrimitive() != null;
					propertyDesc.defaultLiteral = propertyDesc.isPrimitive() ? getDefaultLiteral((JPrimitiveType) originalReturnType) : null;

					Pair<PropertyDesc, String> initEntry = extractInitializerString(entityIface, propertyDesc, propertyName, getter);
					PropertyDesc confidentialDesc = isConfidential(entityIface, propertyDesc, propertyName, getter);

					propertyDesc.initializerString = initEntry.second;
					propertyDesc.initializerLiteralOrSupplier = initializerLiteralOrSupplierFor(propertyDesc);
					propertyDesc.isConfidential = confidentialDesc != null;

					propertyDesc.declaringType = getTypeWhereWeDeclareTheProperty(getter, initEntry.first, confidentialDesc);
					propertyDesc.isOverlay = propertyDesc.declaringType != entityIface;
					propertyDesc.declaringTypeDesc = acquireEntityDesc(propertyDesc.declaringType, propertyDesc.isOverlay);

					properties.put(propertyName, propertyDesc);

					entityDesc.hasInitializedProperty |= propertyDesc.getHasNonNullInitializer();

				} else if (isSetter(method)) {
					propertyRelatedMethod = true;
				} else if (isEvalMethod(method)) {
					propertyRelatedMethod = true;
					JType evaluatesToType = extractEvalType(method);
					entityDesc.evaluatesToRef = getTypeRef(evaluatesToType, new Owner(entityIface, "@evaluatesTo"));
				}

				if (!propertyRelatedMethod) {
					JClassType enclosingType = method.getEnclosingType();
					if (enclosingType != entityBaseJClassType && enclosingType != genericBaseJClassType)
						entityDesc.nonPropertyMethodSourceSuppliers.add(() -> createUnsupportedMethodSource(method));
				}
			}

			entityDesc.properties = properties;
			entityDesc.transientProperties = transientProperties;
		}

		private JType rawifyIfTypeVar(JType originalReturnType) {
			JTypeParameter typeParameter = originalReturnType.isTypeParameter();

			if (typeParameter == null)
				return originalReturnType;

			JClassType[] bounds = typeParameter.getBounds();

			if (bounds.length > 1)
				throw new IllegalArgumentException("invalid bounds for type variable " + originalReturnType);

			return bounds[0];
		}

		private String createUnsupportedMethodSource(JMethod method) {
			return method.getReadableDeclaration(false, false, false, false, true) + "{throw new java.lang.UnsupportedOperationException();}";
		}

		private boolean isEvalMethod(JMethod method) {
			if (!method.getName().equals("eval"))
				return false;

			JType[] params = method.getParameterTypes();
			if (params.length != 1)
				return false;

			JParameterizedType param = params[0].isParameterized();
			return param != null && param.getRawType() == evaluatorType;
		}

		private JClassType extractEvalType(JMethod method) {
			JType returnType = method.getReturnType().isParameterized();
			if (returnType == null)
				throwInvalidMethodException(method);

			JParameterizedType pt = (JParameterizedType) returnType;
			if (pt.getRawType() != evalContextType)
				throwInvalidMethodException(method);

			JType arg = pt.getTypeArgs()[0];
			if (arg.isClassOrInterface() != null && arg.isWildcard() == null)
				return (JClassType) arg;

			JWildcardType wt = arg.isWildcard();
			if (wt == null)
				throwInvalidMethodException(method);

			JType[] upperBounds = wt.getUpperBounds();
			if (upperBounds.length != 1)
				throwInvalidMethodException(method);

			JType upperBound = upperBounds[0];
			if (upperBound.isClassOrInterface() == null)
				throwInvalidMethodException(method);

			return (JClassType) upperBound;
		}

		private void throwInvalidMethodException(JMethod method) {
			throw new IllegalArgumentException("Invalid return type of 'eval' method: " + method);
		}

		private boolean isTransient(JMethod method) {
			return method.isAnnotationPresent(Transient.class);
		}

		private boolean isGetter(JMethod method) {
			String name = method.getName();
			return name.startsWith("get") && name.length() > 3 && method.getParameters().length == 0;
		}

		private boolean isSetter(JMethod method) {
			String name = method.getName();
			return name.startsWith("set") && name.length() > 3 && method.getParameters().length == 1;
		}

		private String getDefaultLiteral(JPrimitiveType type) {
			switch (type) {
				case BOOLEAN:
					return "Boolean.FALSE";
				case DOUBLE:
					return "0d";
				case FLOAT:
					return "0f";
				case INT:
					return "0";
				case LONG:
					return "0l";
				default:
					throw new RuntimeException("Unsupported primitive type: " + type);

			}
		}

		// ##################################################################
		// ## . . . . . . . . . . . Initializers . . . . . . . . . . . . . ##
		// ##################################################################

		private static final Pair<PropertyDesc, String> EMPTY_PAIR = new Pair<>(null, null);

		private Pair<PropertyDesc, String> extractInitializerString(JClassType entityClass, PropertyDesc pd, String propertyName, JMethod getter) {
			Initializer gi = getInitializer(getter);

			if (gi != null)
				return new Pair<>(pd, gi.value());

			for (JClassType iface : entityClass.getImplementedInterfaces()) {
				if (!isGenericEntity(iface))
					continue;

				Pair<PropertyDesc, String> superInitializer = getInitializerDescFromSuperType(iface, propertyName);
				if (superInitializer != EMPTY_PAIR)
					return superInitializer;
			}

			return EMPTY_PAIR;
		}

		/** Might also be "null", i.e. this can be used always in the code. */
		private String initializerLiteralOrSupplierFor(PropertyDesc pd) {
			String initializerString = pd.initializerString;

			if (initializerString == null)
				return "null";

			// TODO optimize, these two should be singletons
			if (InitializerTools.NULL_STRING.equals(initializerString))
				return "() -> com.braintribe.model.generic.value.NullDescriptor.T.createRaw()";

			if (InitializerTools.NOW_STRING.equals(initializerString))
				return "() -> com.braintribe.model.bvd.time.Now.T.createRaw()";

			Object o = InitializerTools.parseInitializer(initializerString, enumAsStringArray, resolveEnumHints(pd));

			if (o instanceof String)
				return "\"" + o + "\"";

			if (o instanceof String[]) { // this is the enum indicator
				String[] parsedEnum = (String[]) o;
				return "() -> com.braintribe.model.generic.value.EnumReference.of(" + parsedEnum[0] + "." + parsedEnum[1] + ")";
			}

			if (o instanceof Boolean)
				return "Boolean." + o.toString().toUpperCase();

			if (o instanceof Integer)
				return o + "";

			if (o instanceof Long)
				return o + "l";

			if (o instanceof Float)
				return o + "f";

			if (o instanceof Double)
				return o + "d";

			if (o instanceof BigDecimal)
				return "new java.math.BigDecimal(\"" + o + "\")";

			throw new RuntimeException("Seems we forgot to handle type: " + o.getClass().getName() + ", value: " + o);
		}

		private EnumHint[] resolveEnumHints(PropertyDesc pd) {
			EnumHint[] result = resolveEnumHintsHelper(pd);
			return result == null || (result[0] == null && result[1] == null) ? null : result;
		}

		private EnumHint[] resolveEnumHintsHelper(PropertyDesc pd) {
			JType jType = pd.jType;

			if (jType.isEnum() != null)
				return new EnumHint[] { null, newEnumHint(jType) };

			JParameterizedType pt = jType.isParameterized();
			if (pt == null)
				return null;

			JClassType rawType = pt.getRawType();

			if (rawType == listJClassType || rawType == setJClassType)
				return new EnumHint[] { null, getEnumHintForTypeArgument(pt, 0) };

			if (rawType == mapJClassType)
				return new EnumHint[] { getEnumHintForTypeArgument(pt, 0), getEnumHintForTypeArgument(pt, 1) };

			return null;
		}

		private EnumHint getEnumHintForTypeArgument(JParameterizedType pt, int index) {
			JClassType paramType = pt.getTypeArgs()[index];

			return newEnumHint(paramType);
		}

		private EnumHint newEnumHint(JType jType) {
			return jType.isEnum() != null ? new EnumHint(jType.getQualifiedSourceName(), getConstantNames(jType.isEnum())) : null;
		}

		private Set<String> getConstantNames(JEnumType jEnum) {
			return Stream.of(jEnum.getEnumConstants()) //
					.map(JEnumConstant::getName) //
					.collect(Collectors.toSet());
		}

		private Pair<PropertyDesc, String> getInitializerDescFromSuperType(JClassType superType, String propertyName) {
			EntityDesc superDescriptor = acquireEntityDesc(superType, true);
			PropertyDesc pd = superDescriptor.properties.get(propertyName);
			return pd != null && pd.initializerString != null ? new Pair<>(pd, pd.initializerString) : EMPTY_PAIR;
		}

		private Initializer getInitializer(JMethod method) {
			return method.getAnnotation(Initializer.class);
		}

		// ##################################################################
		// ## . . . . . . . . . . . Confidential MD . . . . . . . . . . . .##
		// ##################################################################

		private PropertyDesc isConfidential(JClassType entityClass, PropertyDesc pd, String propertyName, JMethod getter) {
			Annotation mda = getter.getAnnotation(Confidential.class);

			if (mda != null)
				return pd;

			for (JClassType iface : entityClass.getImplementedInterfaces()) {
				if (!isGenericEntity(iface))
					continue;

				PropertyDesc superConfidential = isConfidentialOnSuperType(iface, propertyName);
				if (superConfidential != null)
					return superConfidential;
			}

			return null;
		}

		private PropertyDesc isConfidentialOnSuperType(JClassType superType, String propertyName) {
			EntityDesc superDescriptor = acquireEntityDesc(superType, true);
			PropertyDesc pd = superDescriptor.properties.get(propertyName);
			return pd != null && pd.isConfidential ? pd : null;
		}

		// ##################################################################
		// ## . . . . . . . . . . Reflection helpers . . . . . . . . . . . ##
		// ##################################################################

		private Collection<JMethod> getOverridableMethods(JClassType classType) {
			Map<String, JMethod> methods = newMap();

			Set<? extends JClassType> classTypes = classType.getFlattenedSupertypeHierarchy();
			ListIterator<? extends JClassType> listIterator = newList(classTypes).listIterator(classTypes.size());
			while (listIterator.hasPrevious()) {
				JClassType type = listIterator.previous();
				if (isGenericEntity(type))
					for (JMethod method : type.getMethods())
						if (!method.isFinal() && (method.isPublic() || method.isProtected()))
							methods.put(computeInternalSignature(method), method);
			}

			return methods.values();
		}

		private String computeInternalSignature(JMethod method) {
			StringBuffer sb = new StringBuffer();
			sb.setLength(0);
			sb.append(method.getName());
			com.google.gwt.core.ext.typeinfo.JParameter[] params = method.getParameters();
			for (com.google.gwt.core.ext.typeinfo.JParameter param : params) {
				sb.append("/");
				sb.append(param.getType().getErasedType().getQualifiedSourceName());
			}
			return sb.toString();
		}

		// helper for weak JClassType.findMethod() which does not handle superclass methods
		private JMethod getOverridableMethod(JClassType classType, String name, JType parameters[]) {
			JMethod method = classType.findMethod(name, parameters);

			if (method != null)
				return method;

			for (JClassType superClassType : classType.getFlattenedSupertypeHierarchy()) {
				method = superClassType.findMethod(name, parameters);
				if (method != null)
					return method;
			}

			return null;
		}

		private static final String GMF_getTypeReflection = GMF.class.getName() + ".getTypeReflection()";

		private String getTypeRef(JType type, Owner owner) {
			JClassType classType = type.isClassOrInterface();
			JParameterizedType parametrizedType = classType.isParameterized();
			if (parametrizedType != null)
				classType = parametrizedType.getBaseType();

			if (isGenericEntity(classType))
				return getEntityTypeRef(classType);

			if (classType == mapJClassType) {
				JParameterizedType pt = type.isParameterized();
				if (pt == null)
					throw new IllegalArgumentException("java.util.Map must be parametrized");

				return getCollectionTypeRef("map", pt, owner);
			}

			if (classType == setJClassType) {
				JParameterizedType pt = type.isParameterized();
				if (pt == null)
					throw new IllegalArgumentException("java.util.Set must be parametrized");

				return getCollectionTypeRef("set", pt, owner);
			}

			if (classType == listJClassType) {
				JParameterizedType pt = type.isParameterized();
				if (pt == null)
					throw new IllegalArgumentException("List must be parametrized");

				return getCollectionTypeRef("list", pt, owner);
			}

			if (classType.isEnum() != null)
				return GMF_getTypeReflection + ".getEnumType(" + classType.getQualifiedSourceName() + ".class)";

			if (simpleNames.contains(classType.getQualifiedSourceName()))
				return GMF_getTypeReflection + ".getSimpleType(" + classType.getQualifiedSourceName() + ".class)";

			if (classType == objectJClassType)
				return GMF_getTypeReflection + ".getBaseType()";

			throw new IllegalArgumentException(
					"invalid field type or field type parametrization: " + classType.getQualifiedSourceName() + " for property: " + owner);
		}

		private String getCollectionTypeRef(String collectionTypeName, JParameterizedType parameterizedType, Owner owner) {
			StringBuilder parameterTypeRefs = new StringBuilder();

			for (JClassType typeParameter : parameterizedType.getTypeArgs()) {
				String typeParameterRef = getTypeRef(typeParameter, owner);
				if (parameterTypeRefs.length() > 0)
					parameterTypeRefs.append(',');
				parameterTypeRefs.append(typeParameterRef);
			}
			return GMF_getTypeReflection + ".getCollectionType(\"" + collectionTypeName
					+ "\", new com.braintribe.model.generic.reflection.GenericModelType[]{" + parameterTypeRefs + "})";
		}

		/**
		 * So the question is where is the place our property is declared, meaning where the {@link GwtCompileTimeProperty} instance is created.
		 * <p>
		 * If the property is not inherited, it must be this type. If it is inherited, we have to check if we can use the supe-property or have to
		 * re-declare it. This decision depends on annotation-based information for this property. For example, if some super-type declares an
		 * initializer, we use that property. If however some other super-type declares it as confidential, we have no super-type with the combined
		 * information and have to re-declare it.
		 * <p>
		 * The entire logic with current implementation is explained here:
		 * 
		 * <pre>
		 *  init | conf | declaring type   // "init" means we are decalring an @Initializer on this level, "conf" means we declare it as @Confiedntial
		 *  NO   |  NO  | primary
		 *  YES  |  NO  | init
		 *  NO   |  YES | conf
		 *  YES  |  YES | if initializer is confidential it's that one, otherwise it's this
		 * </pre>
		 */
		private JClassType getTypeWhereWeDeclareTheProperty(JMethod getter, PropertyDesc initDesc, PropertyDesc confidDesc) {
			if (initDesc != null) {
				if (confidDesc == null || initDesc.isConfidential)
					return initDesc.ownerTypeDesc.entityIface;
				else
					return getter.getEnclosingType();
			}

			if (confidDesc != null)
				return confidDesc.ownerTypeDesc.entityIface;

			return getPrimaryDeclaringType(getter);
		}

		private JClassType getPrimaryDeclaringType(JMethod getter) {
			JClassType declaringType = getter.getEnclosingType();

			JClassType result = null;
			for (JClassType superIfaceType : declaringType.getImplementedInterfaces()) {
				JMethod superGetter = superIfaceType.findMethod(getter.getName(), getter.getParameterTypes());

				/* It may happen that we have a super-interface defined for convenience that is not a GenericEntity (e.g. OperandSet in
				 * QueryPlanModel). Such cases we want to ignore, of course. */
				if (superGetter != null && isGenericEntity(superIfaceType))
					result = getPrimaryDeclaringType(superGetter);
			}

			return result == null ? declaringType : result;
		}

		private JType ensureObjectType(JType type) {
			JPrimitiveType pt = type.isPrimitive();
			return pt == null ? type : typeOracle.findType(pt.getQualifiedBoxedSourceName());
		}

		// ##################################################################
		// ## . . . . . Convenience methods to prepare Strings . . . . . . ##
		// ##################################################################

		private String getSuperTypeRefs(EntityDesc entityDesc) throws Exception {
			StringBuilder sb = new StringBuilder();

			for (EntityDesc superDesc : entityDesc.directSuperTypes) {
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(superDesc.getEntityTypeSingletonRef());
			}

			return "java.util.Arrays.<" + AbstractEntityType.class.getName() + "<?>>asList(" + sb + ")";
		}

		private String getEntityTypeRef(JClassType classType) {
			return acquireEntityDesc(classType, false).getEntityTypeSingletonRef();
		}

		// ##################################################################
		// ## . . . . . . . ToString / ToSelectiveInformation . . . . . . .##
		// ##################################################################

		private String getToStringSourceCode(JClassType type, ToStringInformation toStringInformation) throws TemplateException {
			return toStringInformation == null ? null : getSourceCodeForTemplate(type, toStringInformation.value(), false);
		}

		private String getSelectiveInformationSourceCode(JClassType type, SelectiveInformation si) throws TemplateException {
			return si == null ? null : getSourceCodeForTemplate(type, si.value(), true);
		}

		private String getSourceCodeForTemplate(final JClassType type, String templateSource, boolean selective) throws TemplateException {
			MergeContext mergeContext = new MergeContext();
			mergeContext.setLiteralEscaper(Generator::escape);
			mergeContext.setSourceMode(true);
			mergeContext.setVariableProvider(variable -> resolveVariable(mergeContext, variable, type, selective));

			Template template = Template.parse(templateSource);

			// first build the plain result by merging the template and ensuring a string type
			String result = "\"\"+" + template.merge(mergeContext);

			return result;
		}

		private Object resolveVariable(MergeContext mergeContext, String variable, final JClassType type, boolean selective) {
			if (variable.equals(SI_TYPE_ALT) || variable.equals(SI_TYPE_SHORT))
				return "this.getShortName()";

			if (variable.equals(SI_TYPE))
				return "this.getTypeSignature()";

			if (variable.equals(SI_ID))
				return "(entity.getId() != null ? entity.getId() : entity.runtimeId())";

			if (variable.equals(SelectiveInformationSupport.SI_RUNTIME_ID))
				return "entity.runtimeId()";

			return resolvePropertyChainVariable(mergeContext, variable, type, selective);
		}

		private Object resolvePropertyChainVariable(MergeContext mergeContext, String variable, final JClassType type, boolean selective) {
			try {
				String propertyNameChain[] = variable.split("\\.");
				StringBuilder propertyChain = new StringBuilder();

				JClassType curType = type;

				for (String propertyName : propertyNameChain) {
					if (curType == null)
						return originalVariableSourceLiteral(mergeContext, variable);

					PropertyDesc pd = acquireEntityDesc(curType, true).properties.get(propertyName);
					if (pd == null)
						return originalVariableSourceLiteral(mergeContext, variable);

					String propertyRef = pd.getSingletonInstanceRef();
					if (propertyChain.length() > 0)
						propertyChain.append(',');
					propertyChain.append(propertyRef);

					curType = pd.getJType().isClassOrInterface();

					// treat list in a special way; curType can be null if our property is primitive
					if (curType != null && curType.getName().equals(List.class.getName())) {
						JParameterizedType parameterizedType = (JParameterizedType) curType;
						curType = parameterizedType.getTypeArgs()[0];
					}
				}

				return CompoundPropertyRetrieval.class.getName() + ".retrieveCompoundProperty(entity,new " + Property.class.getName() + "[]{"
						+ propertyChain + "}," + selective + ")";

			} catch (Exception e) {
				throw new RuntimeException("error while preparing selective information with compound property", e);
			}
		}

		private String originalVariableSourceLiteral(MergeContext mergeContext, String variable) throws TemplateException {
			return mergeContext.createSourceLiteral("${" + variable + "}");
		}

		// ##################################################################
		// ## . . . . . . . . . . . . . Other . . . . . . . . . . . . . . .##
		// ##################################################################

		private VelocityEngine velocityEngine;

		private VelocityEngine getVelocityEngine() throws Exception {
			if (velocityEngine == null) {
				Properties velocityProperties = new Properties();
				InputStream in = getClass().getResourceAsStream("velocity.properties");

				try {
					velocityProperties.load(in);
				} finally {
					in.close();
				}

				VelocityEngine engine = new VelocityEngine();
				engine.init(velocityProperties);

				velocityEngine = new VelocityEngine(velocityProperties);
			}

			return velocityEngine;
		}

		private boolean isGenericEntity(JClassType classType) {
			return classType.isAssignableTo(genericEntityJClassType);
		}

		/**
		 * For exception-message only.
		 */
		private static class Owner {
			private final JClassType entityIface;
			private final String propertyName;

			public Owner(JClassType entityIface, String propertyName) {
				this.entityIface = entityIface;
				this.propertyName = propertyName;
			}

			@Override
			public String toString() {
				return entityIface.getQualifiedSourceName() + "." + propertyName;
			}
		}

	}

}
