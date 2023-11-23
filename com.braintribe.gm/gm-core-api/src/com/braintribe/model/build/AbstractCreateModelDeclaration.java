// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.model.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ForwardDeclaration;
import com.braintribe.model.generic.reflection.GenericModelException;

public class AbstractCreateModelDeclaration {
	private static final String JAVA_CLASS_EXT = ".class";
	private static final String MODEL_FORWARD_DECLARATION_FILENAME = "model-forward-declaration.xml";
	private static final String MODEL_DECLARATION_FILENAME = "model-declaration.xml";
	private static final String GM_CORE_API = "gm-core-api";

	static {
		try {
			// Copied from UpdateModelDeclaration. Can this fail at all?
			Class.forName(AbstractCreateModelDeclaration.class.getName(), false, AbstractCreateModelDeclaration.class.getClassLoader());
			Class.forName(GenericEntity.class.getName(), false, AbstractCreateModelDeclaration.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Error while performing classloader check! Please make sure all model dependencies are on the classpath.", e);
		}
	}

	public static void createModelDeclaration(ModelDeclrationContext modelDeclrationContext) {
		if (modelDeclrationContext.classesFolder == null) {
			throw new IllegalArgumentException("Manadatory argument 'classesFolder' is missing!");
		}

		if (modelDeclrationContext.artifact == null) {
			throw new IllegalArgumentException("Manadatory argument 'artifact' is missing!");
		}

		File classesFolderFile = new File(modelDeclrationContext.classesFolder);
		if (!classesFolderFile.exists()) {
			throw new IllegalArgumentException("Argument 'classesFolder' is set to '" + modelDeclrationContext.classesFolder + "' but folder '"
					+ classesFolderFile.getAbsolutePath() + "' does not exist!");
		}

		try {
			ModelDescriptor modelDescriptor = buildModelDescriptor(modelDeclrationContext.artifact,
					Arrays.asList(modelDeclrationContext.dependencies.split(",")), modelDeclrationContext.modelRevision);
			createModelDeclaration(classesFolderFile, modelDescriptor);
		} catch (Exception e) {
			throw new GenericModelException("Error while creating model declaration!", e);
		}
	}

	static void createModelDeclaration(File classesFolder, ModelDescriptor modelDescriptor) throws Exception {
		Map<String, File> classes = new TreeMap<>();

		scanTypeCandidatesRecursively(null, classesFolder, classes);

		filterTypeCandidates(classes.keySet(), modelDescriptor.declaredTypes, modelDescriptor.forwardTypes);

		Collection<File> sortedClassFiles = createSortedClassFiles(classes);

		modelDescriptor.hash = buildHash(sortedClassFiles.stream());

		Map<String, Set<String>> forwards = scanForwards();

		Set<String> forwardTypes = forwards.get(modelDescriptor.name);

		if (forwardTypes != null) {
			modelDescriptor.declaredTypes.addAll(forwardTypes);
		}

		if (!modelDescriptor.artifactId.equals(GM_CORE_API)) {
			writeDescriptor(new File(classesFolder, MODEL_DECLARATION_FILENAME), modelDescriptor);
		} else {
			writeForwardDescriptor(new File(classesFolder, MODEL_FORWARD_DECLARATION_FILENAME), modelDescriptor);
		}
	}

	private static Collection<File> createSortedClassFiles(Map<String, File> values) {
		SortedMap<String, File> sortedFiles = new TreeMap<>(values);
		return sortedFiles.values();
	}

	private static String buildHash(Stream<File> fileStream) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");

			byte buffer[] = new byte[0xffff];

			fileStream.forEach(file -> {
				try {
					try (InputStream in = new FileInputStream(file)) {
						int bytes = 0;
						while ((bytes = in.read(buffer)) != -1) {
							digest.update(buffer, 0, bytes);
						}
					}
				} catch (IOException e) {
					throw new RuntimeException("Failed to create hash for '" + file.getAbsolutePath() + "'!");
				}
			});

			byte[] hashBytes = digest.digest();

			return UpdateModelDeclaration.printHexBinary(hashBytes).toLowerCase();
		} catch (Exception e) {
			throw new GenericModelException("Error while building hash!");
		}
	}

	private static Map<String, Set<String>> scanForwards() throws Exception {
		Enumeration<URL> urls = AbstractCreateModelDeclaration.class.getClassLoader().getResources(MODEL_FORWARD_DECLARATION_FILENAME);

		Map<String, Set<String>> map = new HashMap<String, Set<String>>();

		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();

			readModelForwardDeclaration(url, map);
		}

		return map;
	}

	private static void readModelForwardDeclaration(URL url, Map<String, Set<String>> map) throws Exception {
		try (InputStream in = url.openStream()) {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);

			Element documentElement = doc.getDocumentElement();

			for (Node node = documentElement.getFirstChild(); node != null; node = node.getNextSibling()) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					if (element.getTagName().equals("for-model")) {
						String modelName = element.getAttribute("name");

						Set<String> forwardTypes = map.get(modelName);

						if (forwardTypes == null) {
							forwardTypes = new HashSet<String>();
							map.put(modelName, forwardTypes);
						}

						for (Node subNode = element.getFirstChild(); subNode != null; subNode = subNode.getNextSibling()) {

							if (subNode.getNodeType() == Node.ELEMENT_NODE) {
								Element subElement = (Element) subNode;
								if (subElement.getTagName().equals("type")) {
									String type = subElement.getTextContent();
									forwardTypes.add(type);
								}
							}
						}
					}
				}
			}
		}
	}

	private static void writeForwardDescriptor(File file, ModelDescriptor modelDescriptor) throws Exception {
		if (modelDescriptor.forwardTypes.isEmpty()) {
			return;
		}

		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
			writer.write("<model-forward-declaration>\n\n");

			for (Map.Entry<String, Set<String>> entry : modelDescriptor.forwardTypes.entrySet()) {
				writer.write("  <for-model name='" + entry.getKey() + "'>\n");
				for (String type : entry.getValue()) {
					writer.write("    <type>");
					writer.write(type);
					writer.write("</type>\n");
				}
				writer.write("  </for-model>\n\n");
			}

			writer.write("</model-forward-declaration>");

		}
	}

	private static void writeDescriptor(File file, ModelDescriptor modelDescriptor) throws Exception {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
			writer.write("<model-declaration>\n\n");

			writer.write("  <name>");
			writer.write(modelDescriptor.name);
			writer.write("</name>\n\n");
			writer.write("  <groupId>");
			writer.write(modelDescriptor.groupId);
			writer.write("</groupId>\n");
			writer.write("  <artifactId>");
			writer.write(modelDescriptor.artifactId);
			writer.write("</artifactId>\n");
			writer.write("  <version>");
			writer.write(modelDescriptor.version);
			writer.write("</version>\n");
			writer.write("  <hash>");
			writer.write(modelDescriptor.hash);
			writer.write("</hash>\n\n");

			writer.write("  <dependencies>\n");
			for (String dependency : modelDescriptor.dependencies) {
				writer.write("    <dependency>");
				writer.write(dependency);
				writer.write("</dependency>\n");
			}
			writer.write("  </dependencies>\n\n");

			writer.write("  <types>\n");
			for (String type : modelDescriptor.declaredTypes) {
				writer.write("    <type>");
				writer.write(type);
				writer.write("</type>\n");
			}
			writer.write("  </types>\n\n");

			writer.write("</model-declaration>");
		}
	}

	private static ModelDescriptor buildModelDescriptor(String artifact, List<String> dependencies, String modelRevision) {
		ModelDescriptor modelDescriptor = new ModelDescriptor();

		Artifact mainArtifact = parseArtifact(artifact);
		modelDescriptor.groupId = mainArtifact.getGroupId();
		modelDescriptor.artifactId = mainArtifact.getArtifactId();
		modelDescriptor.version = mainArtifact.getVersion();
		modelDescriptor.name = modelDescriptor.groupId + ":" + modelDescriptor.artifactId;

		if (modelRevision != null && !modelRevision.isEmpty()) {
			modelDescriptor.version += "." + modelRevision;
		}

		if (dependencies != null) {
			for (String dependency : dependencies) {
				Artifact depArtifact = parseArtifact(dependency);
				String groupId = depArtifact.getGroupId();
				String artifactId = depArtifact.getArtifactId();

				if (!artifactId.equals("gm-core-api")) {
					String modelName = groupId + ":" + artifactId;
					modelDescriptor.dependencies.add(modelName);
				}
			}
		}

		return modelDescriptor;
	}

	static class ModelDescriptor {
		String groupId;
		String artifactId;
		String version;
		String name;
		String hash;
		Set<String> declaredTypes = new TreeSet<String>();
		Set<String> dependencies = new TreeSet<String>();
		Map<String, Set<String>> forwardTypes = new TreeMap<String, Set<String>>();
	}

	private static void filterTypeCandidates(Collection<String> classNames, Set<String> declaredTypes, Map<String, Set<String>> forwardTypes) {
		for (String className : classNames) {
			try {
				Class<?> candidateClass = Class.forName(className, false, AbstractCreateModelDeclaration.class.getClassLoader());
				if (candidateClass.isEnum() || GenericEntity.class.isAssignableFrom(candidateClass)) {
					ForwardDeclaration forwardDeclaration = candidateClass.getAnnotation(ForwardDeclaration.class);
					if (forwardDeclaration == null) {
						declaredTypes.add(className);
					} else {
						String forwardModel = forwardDeclaration.value();
						Set<String> modelForwardTypes = forwardTypes.get(forwardModel);
						if (modelForwardTypes == null) {
							modelForwardTypes = new TreeSet<String>();
							forwardTypes.put(forwardModel, modelForwardTypes);
						}
						modelForwardTypes.add(className);
					}
				}
			} catch (ClassNotFoundException e) {
				// TODO: In UpdateModelDeclaration this is ignored (but when does it really happen?)
				throw new GenericModelException("Error while filtering type candidates!", e);
			}
		}
	}

	private static void scanTypeCandidatesRecursively(String path, File folder, Map<String, File> classes) {

		for (File file : folder.listFiles()) {
			String fileName = file.getName();
			if (file.isDirectory()) {
				scanTypeCandidatesRecursively(concat(path, fileName), file, classes);
			} else if (fileName.endsWith(JAVA_CLASS_EXT)) {
				String plainName = fileName.substring(0, fileName.length() - JAVA_CLASS_EXT.length());
				String className = concat(path, plainName);
				classes.put(className, file);
			}
		}
	}

	private static String concat(String path, String name) {
		return path == null ? name : path + "." + name;
	}

	private static Artifact parseArtifact(String artifactString) {
		String[] artifactParts = artifactString.split(":");
		if (artifactParts.length != 3) {
			throw new IllegalArgumentException(
					"Invalid artifact declararion '" + artifactString + "'! Expected format: 'artifact=com.braintribe.testing:example-model:1.2'");
		}
		return new Artifact(artifactParts[0], artifactParts[1], artifactParts[2]);
	}

	private static class Artifact {

		public Artifact(String groupId, String artifactId, String version) {
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
		}

		public String getGroupId() {
			return groupId;
		}
		public String getArtifactId() {
			return artifactId;
		}
		public String getVersion() {
			return version;
		}

		private final String groupId;
		private final String artifactId;
		private final String version;

	}

	public static abstract class AbstractCommandLineParameters {

		static final String CLASSES_FOLDER = "classesFolder";
		static final String ARTIFACT = "artifact";
		static final String DEPENDENCIES = "dependencies";
		static final String LOCAL_REPOSITORY = "localRepository";
		static final String MODEL_REVISION = "modelRevision";

		public static Map<String, String> parseParameterValues(List<String> parameters, Set<String> mandatoryProperties) {
			Map<String, String> parameterValueOf = new HashMap<>();
			for (String parameter : parameters) {
				final int index = parameter.indexOf("=");
				if (index < 0) {
					throw new IllegalArgumentException("Invalid argument '" + parameter
							+ "'! Please pass your arguments in name=value format. (e.g. artifact=com.braintribe.testing:example-model:1.2)");
				}

				String key = parameter.substring(0, index);
				String value = parameter.substring(index + 1);
				parameterValueOf.put(key, value);
			}

			for (String mandatoryProperty : mandatoryProperties) {
				if (!parameterValueOf.containsKey(mandatoryProperty)) {
					throw new IllegalArgumentException("Mandatory argument '" + mandatoryProperty + "' is missing!");
				}
			}
			return parameterValueOf;
		}

	}

}

class ModelDeclrationContext {
	String classesFolder = null;
	String artifact = null;
	String dependencies = null;
	String localRepository = null;
	String modelRevision = null;
}
