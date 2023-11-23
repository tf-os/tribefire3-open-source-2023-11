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
import java.io.OutputStream;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ForwardDeclaration;
import com.braintribe.model.generic.reflection.GenericModelException;


public class UpdateModelDeclaration {
	private static final String JAVA_CLASS_EXT = ".class";

	private static final char[] hexCode;
	static {
		hexCode = "0123456789ABCDEF".toCharArray();
	}
	
	public static void main(String[] args) {
		try {
			Class.forName(UpdateModelDeclaration.class.getName(), false, UpdateModelDeclaration.class.getClassLoader());
			String buildFolderInput = args.length > 0? args[0]: "build";
			
			String []buildFoldersInput = buildFolderInput.split(File.pathSeparator);
			
			
			List<File> buildFolders = Arrays.stream(buildFoldersInput)
					.map(s -> new File(s))
					.collect(Collectors.toList());
			
			File buildFolder = buildFolders.get(0);
			
			File pomFile = new File(buildFolder.getParent(), "pom.xml");
			
			ModelDescriptor modelDescriptor = buildModelDescriptor(pomFile);
	
			Map<String, File> classes= new TreeMap<>();
			
			for (File scanFolder: buildFolders)
				scanTypeCandidates(null, scanFolder, classes);
			
			filterTypeCandidates(classes.keySet(), modelDescriptor.declaredTypes, modelDescriptor.forwardTypes);
			
			Collection<File> sortedClassFiles = createSortedClassFiles(classes);
			
			modelDescriptor.hash = buildHash(Stream.concat(Stream.of(pomFile), sortedClassFiles.stream()));
			
			Map<String, Set<String>> forwards = scanForwards();
			
			Set<String> forwardTypes = forwards.get(modelDescriptor.name);
			
			if (forwardTypes != null) {
				modelDescriptor.declaredTypes.addAll(forwardTypes);
			}

			if (!modelDescriptor.artifactId.equals("GmCoreApi")) {
				writeDescriptor(new File(buildFolder, "model-declaration.xml"), modelDescriptor);
			}
			else {
				writeForwardDescriptor(new File(buildFolder, "model-forward-declaration.xml"), modelDescriptor);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Collection<File> createSortedClassFiles(Map<String, File> values) {
		SortedMap<String, File> sortedFiles = new TreeMap<>(values);
		return sortedFiles.values();
	}

	public static String buildHash(Stream<File> fileStream) {
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
					throw new RuntimeException("failed to hash for " + file);
				} 
			});
			
			byte[] hashBytes = digest.digest();
			
			return printHexBinary(hashBytes).toLowerCase();
		} catch (Exception e) {
			throw new GenericModelException("error while building hash");
		}
	}
	
	protected static String printHexBinary(byte[] data) {
		StringBuilder r = new StringBuilder(data.length * 2);
		for (byte b : data) {
			r.append(hexCode[(b >> 4 & 0xF)]);
			r.append(hexCode[(b & 0xF)]);
		}
		return r.toString();
	}

	private static Map<String, Set<String>> scanForwards() throws Exception {
		Enumeration<URL> urls = UpdateModelDeclaration.class.getClassLoader().getResources("model-forward-declaration.xml");
		
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
					Element element = (Element)node;
					if (element.getTagName().equals("for-model")) {
						String modelName = element.getAttribute("name");
						
						Set<String> forwardTypes = map.get(modelName);
						
						if (forwardTypes == null) {
							forwardTypes = new HashSet<String>();
							map.put(modelName, forwardTypes);
						}
						
						for (Node subNode = element.getFirstChild(); subNode != null; subNode = subNode.getNextSibling()) {
							
							if (subNode.getNodeType() == Node.ELEMENT_NODE) {
								Element subElement = (Element)subNode;
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
		if (modelDescriptor.forwardTypes.isEmpty())
			return;
		
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
			writer.write("<model-forward-declaration>\n\n");
			
			for (Map.Entry<String, Set<String>> entry: modelDescriptor.forwardTypes.entrySet()) {
				writer.write("  <for-model name='" + entry.getKey() + "'>\n");
				for (String type: entry.getValue()) {
					writer.write("    <type>"); writer.write(type); writer.write("</type>\n");
				}
				writer.write("  </for-model>\n\n");
			}
			
			writer.write("</model-forward-declaration>");
			
		}
	}
	

	public static void writeDescriptor(File file, ModelDescriptor modelDescriptor) throws Exception {
		writeDescriptor(new FileOutputStream(file), modelDescriptor);
	}
	
	public static void writeDescriptor(OutputStream out, ModelDescriptor modelDescriptor) throws Exception {
		OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
		writeDescriptor(writer, modelDescriptor);
		writer.flush();
	}
	
	public static void writeDescriptor(Writer writer, ModelDescriptor modelDescriptor) throws Exception {
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		writer.write("<model-declaration>\n\n");
		
		writer.write("  <name>"); writer.write(modelDescriptor.name); writer.write("</name>\n\n");
		writer.write("  <groupId>"); writer.write(modelDescriptor.groupId); writer.write("</groupId>\n");
		writer.write("  <artifactId>"); writer.write(modelDescriptor.artifactId); writer.write("</artifactId>\n");
		writer.write("  <version>"); writer.write(modelDescriptor.version); writer.write("</version>\n");
		writer.write("  <hash>"); writer.write(modelDescriptor.hash); writer.write("</hash>\n\n");
		
		writer.write("  <dependencies>\n");
		for (String dependency: modelDescriptor.dependencies) {
			writer.write("    <dependency>"); writer.write(dependency); writer.write("</dependency>\n");
		}
		writer.write("  </dependencies>\n\n");
		
		writer.write("  <types>\n");
		for (String type: modelDescriptor.declaredTypes) {
			writer.write("    <type>"); writer.write(type); writer.write("</type>\n");
		}
		writer.write("  </types>\n\n");
		
		writer.write("</model-declaration>");
	}
	
	private static ModelDescriptor buildModelDescriptor(File file) throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		
		Element project = doc.getDocumentElement();
		
		ModelDescriptor modelDescriptor = new ModelDescriptor();
		
		modelDescriptor.groupId = getFirstElement(project, "groupId").getTextContent();
		modelDescriptor.artifactId = getFirstElement(project, "artifactId").getTextContent();
		modelDescriptor.version = getFirstElement(project, "version").getTextContent();
		modelDescriptor.name = modelDescriptor.groupId + ":" + modelDescriptor.artifactId;
		
		/* Dirk: removed propagation of the model revision as it is wrong
		// properties
		Element propertiesElement = getFirstElement(project, "properties");
		
		if (propertiesElement != null) {
			Element modelRevisionElement = getFirstElement(propertiesElement, "model-revision");
			if (modelRevisionElement != null) {
				modelDescriptor.version += "." + modelRevisionElement.getTextContent(); 
			}
		}
		*/
		
		// dependencies
		Element dependenciesElement = getFirstElement(project, "dependencies");
		
		if (dependenciesElement != null) {
			Node node = dependenciesElement.getFirstChild();
			
			while (node != null) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element)node;
					if (element.getTagName().equals("dependency")) {
						String groupId = getFirstElement(element, "groupId").getTextContent();
						String artifactId = getFirstElement(element, "artifactId").getTextContent();
						
						if (isModelDependency(artifactId, element)) {
							String modelName = groupId + ":" + artifactId;
							modelDescriptor.dependencies.add(modelName);
						}
								
					}
				}
				node = node.getNextSibling();
			}
		}
		
		return modelDescriptor;
	}
	
	private static boolean isModelDependency(String artifactId, Element element) {
		return !artifactId.equals("GmCoreApi") && !isFunctionalDependency(element);
	}

	private static boolean isFunctionalDependency(Element element) {
		return "functional".equals(getGroup(element));
	}

	private static String getGroup(Element dependencyElement) {
		for (Node node = dependencyElement.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE) {
				continue;
			}

			ProcessingInstruction pi = (ProcessingInstruction) node;
			if (!pi.getTarget().equals("group")) {
				continue;
			}

			return pi.getData();
		}

		return "default";
	}

	private static Element getFirstElement(Element project, String tagName) {
		Node node = project.getFirstChild();
		
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element)node;
				if (element.getTagName().equals(tagName)) {
					return element;
				}
			}
			node = node.getNextSibling();
		}
		
		return null;
	}

	public static class ModelDescriptor {
		public String groupId;
		public String artifactId;
		public String version;
		public String name;
		public String hash;
		public Set<String> declaredTypes = new TreeSet<String>();
		public Set<String> dependencies = new TreeSet<String>();
		public Map<String, Set<String>> forwardTypes = new TreeMap<String, Set<String>>();
	}
	
	

	private static void filterTypeCandidates(Collection<String> classNames, Set<String> declaredTypes, Map<String, Set<String>> forwardTypes) {
		for (String className: classNames) {
			try {
				Class<?> candidateClass = Class.forName(className, false, UpdateModelDeclaration.class.getClassLoader());
				if (candidateClass.isEnum() || GenericEntity.class.isAssignableFrom(candidateClass)) {
					ForwardDeclaration forwardDeclaration = candidateClass.getAnnotation(ForwardDeclaration.class);
					if (forwardDeclaration == null) {
						declaredTypes.add(className);
					}
					else {
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
				// ignore
				e.printStackTrace();
			}
		}
	}

	private static void scanTypeCandidates(String path, File folder, Map<String, File> classes) {
		
		for (File file: folder.listFiles()) {
			String fileName = file.getName();
			if (file.isDirectory()) {
				scanTypeCandidates(concat(path, fileName), file, classes);
			}
			else if (fileName.endsWith(JAVA_CLASS_EXT)) {
				String plainName = fileName.substring(0, fileName.length() - JAVA_CLASS_EXT.length());
				String className = concat(path, plainName);
				classes.put(className, file);
			}
		}
	}

	private static String concat(String path, String name) {
		return path == null? name: path + "." + name;
	}
}
