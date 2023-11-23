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
package com.braintribe.model.generic.annotation.meta.api;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.api.synthesis.SingleAnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.base.BasicMdaHandler;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.MetaData;

/**
 * Loader for custom {@link MdaHandler}s from classpath files under {@value #MDA_LOCATION}.
 * 
 * @author peter.gazdik
 */
/* package */ class CustomMdaHandlerLoader {

	public static void load(InternalMdaRegistry registry) {
		new CustomMdaHandlerLoader(registry).run();
	}

	// ####################################################
	// ## . . . . . . . . Implementation . . . . . . . . ##
	// ####################################################

	private static final String MDA_LOCATION = "META-INF/gmf.mda";

	private static final Logger log = Logger.getLogger(CustomMdaHandlerLoader.class);

	// -----------------

	private final InternalMdaRegistry registry;

	private final ClassLoader classLoader = GenericEntity.class.getClassLoader();
	private final Lookup lookup = MethodHandles.lookup();

	private URL currentUrl;
	private String currentLine;
	private String[] entries;

	private Class<? extends Annotation> annoClass;
	private Class<? extends MetaData> mdClass;

	private CustomMdaHandlerLoader(InternalMdaRegistry registry) {
		this.registry = registry;
	}

	private void run() {

		Enumeration<URL> declarationUrls = null;
		try {
			declarationUrls = classLoader.getResources(MDA_LOCATION);
		} catch (IOException e) {
			log.error("Error while retrieving configurer files (gm.configurer) on classpath of classloader: " + classLoader, e);
			return;
		}

		while (declarationUrls.hasMoreElements())
			processUrl(declarationUrls.nextElement());
	}

	private void processUrl(URL url) {
		currentUrl = url;

		try (Scanner scanner = new Scanner(url.openStream())) {
			while (scanner.hasNextLine())
				configure(scanner.nextLine());

		} catch (Exception e) {
			log.error("Error while parsing configurators from " + url, e);
		}
	}

	private void configure(String line) {
		line = line.trim();
		if (line.isEmpty())
			return;

		currentLine = line;

		entries = line.split(",");
		if (entries.length < 2) {
			logError("Line must contain at least 2 comma separated entries. "
					+ "First entry is the annotation class, second entry is the entity MD class (signature).");
			return;
		}

		if (!loadBothTypes())
			return;

		if (entries.length % 2 == 1) {
			logError("Line must contain an even number of entries. First entry is the annotation class, second entry is "
					+ "the entity MD class (signature), followd by pairs of annotation attribute and property name. "
					+ "E.g. 'pckg.MyAnnotation, pkg.MyMd,value,property1,attribute2,property2'");
			return;
		}

		if (configureValidLookingEntry())
			logInfo("Successfully registered MdaHandler for annotation " + annoClass.getName() + " and meta-data " + mdClass.getName());
	}

	private boolean configureValidLookingEntry() {
		if (entries.length == 2)
			return processPredicateMda();
		else
			return processRegularMda();
	}

	private boolean loadBothTypes() {
		annoClass = getClassSafe(entries[0].trim(), Annotation.class);
		if (annoClass == null)
			return false;

		mdClass = getClassSafe(entries[1].trim(), MetaData.class);
		if (mdClass == null)
			return false;

		return true;
	}

	private <T> Class<? extends T> getClassSafe(String className, Class<T> superType) {
		try {
			Class<?> clazz = Class.forName(className, false, classLoader);
			return clazz.asSubclass(superType);

		} catch (ClassNotFoundException e) {
			logError(superType.getSimpleName() + " " + className + " class not found.");
			return null;

		} catch (ClassCastException e) {
			logError("Class is not a " + superType.getSimpleName() + ": " + className);
			return null;
		}
	}

	private boolean processPredicateMda() {
		MethodHandle globalIdHandle = findGlobalIdHandle();
		if (globalIdHandle == null)
			return false;

		registry.register(new BasicMdaHandler<>( //
				annoClass, //
				mdClass, //
				anno -> (String) readAttribute(anno, globalIdHandle, "globalId") //
		));

		return false;
	}

	private String attribute;
	private String propertyName;

	private List<AttributeToProperty> resolveAtps() {
		List<AttributeToProperty> result = newList();

		int i = 2;

		while (i < entries.length) {
			attribute = entries[i++];
			propertyName = entries[i++];

			Class<?> type = resolveAttributeType();

			MethodHandle methodHandle = findHandle(attribute, type);

			if (verifyPropertyExists(type))
				result.add(new AttributeToProperty(attribute, methodHandle, propertyName));
		}

		return result;
	}

	private MethodHandle findGlobalIdHandle() {
		return findHandle("globalId", String.class);
	}

	private MethodHandle findHandle(String attribute, Class<?> type) {
		try {
			return lookup.findVirtual(annoClass, attribute, MethodType.methodType(type));

		} catch (NoSuchMethodException e) {
			logError("Annotation type " + annoClass.getName() + " has no " + attribute + "() attribute.");
			return null;

		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot access " + attribute + "() method of MD Annotation " + annoClass.getName(), e);
		}
	}

	private Class<?> resolveAttributeType() {
		try {
			Method m = annoClass.getMethod(attribute);

			return m.getReturnType();

		} catch (NoSuchMethodException e) {
			logError("Annotation type " + annoClass.getName() + " has no " + attribute + "() attribute.");
			return null;

		} catch (SecurityException e) {
			throw new RuntimeException("Cannot access " + attribute + "() method of MD Annotation " + annoClass.getName(), e);
		}
	}

	private boolean verifyPropertyExists(Class<?> type) {
		Method m = getGetter(propertyName);
		if (m == null)
			return false;

		if (m.getReturnType() != type) {
			logError("MD Entity type " + mdClass.getName() + " has a property " + propertyName + " of type " + m.getReturnType().getName()
					+ ". This doesn't match the type " + type.getName() + " of the corresponding annotation attribute " + attribute + " of "
					+ annoClass.getName());
			return false;
		}

		return true;
	}

	private Method getGetter(String propertyName) {
		String getterName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);

		try {
			return mdClass.getMethod(getterName);

		} catch (NoSuchMethodException e) {
			logError("MD Entity type " + mdClass.getName() + " has no getter for property " + propertyName);
			return null;

		} catch (SecurityException e) {
			throw new RuntimeException("Cannot getter " + getterName + "() of MD Entity type " + mdClass.getName(), e);
		}
	}

	private boolean processRegularMda() {
		MethodHandle globalIdHandle = findGlobalIdHandle();
		if (globalIdHandle == null)
			return false;

		List<AttributeToProperty> atps = resolveAtps();

		registry.register(new BasicMdaHandler<>( //
				annoClass, //
				mdClass, //
				anno -> (String) readAttribute(anno, globalIdHandle, "globalId"), //
				(ctx, anno, md) -> {
					for (AttributeToProperty atp : atps)
						copyFromAnnotationToMd(atp, anno, md);

				}, (ctx, descriptor, md) -> {
					for (AttributeToProperty atp : atps)
						copyFromMdToAnnotationDescriptor(atp, md, descriptor);
				} //
		));

		return false;
	}

	private void copyFromAnnotationToMd(AttributeToProperty atp, Annotation anno, MetaData md) {
		atp.ensureProperty(md);

		Object value = readAttribute(anno, atp.methodHandle, atp.attribute);

		md.write(atp.property, value);
	}

	private void copyFromMdToAnnotationDescriptor(AttributeToProperty atp, MetaData md, SingleAnnotationDescriptor descriptor) {
		atp.ensureProperty(md);

		Object value = md.read(atp.property);

		descriptor.addAnnotationValue(atp.attribute, value);
	}

	private Object readAttribute(Annotation anno, MethodHandle handle, String attribute) {
		try {
			return handle.invoke(anno);
		} catch (Throwable e) {
			throw new RuntimeException("Error while reading " + attribute + " from " + anno.getClass().getName() + " Annotation " + anno, e);
		}
	}

	static class AttributeToProperty {
		public final String attribute;
		public final MethodHandle methodHandle;
		public final String propertyName;
		public Property property;

		public AttributeToProperty(String attribute, MethodHandle methodHandle, String propertyName) {
			this.attribute = attribute;
			this.methodHandle = methodHandle;
			this.propertyName = propertyName;
		}

		public void ensureProperty(MetaData md) {
			if (property == null)
				property = md.entityType().getProperty(propertyName);
		}
	}

	private void logError(String message) {
		log.error("[MdaHandler CP Loader] " + message + ". Line: " + currentLine + ", URL: " + currentUrl);
	}

	private void logInfo(String message) {
		log.info("[MdaHandler CP Loader] " + message + ". Line: " + currentLine + ", URL: " + currentUrl);
	}

}
