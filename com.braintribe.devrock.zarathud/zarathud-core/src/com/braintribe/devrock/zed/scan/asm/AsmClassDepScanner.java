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
package com.braintribe.devrock.zed.scan.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.asm.ClassReader;
import com.braintribe.asm.Opcodes;
import com.braintribe.asm.Type;
import com.braintribe.asm.tree.AbstractInsnNode;
import com.braintribe.asm.tree.AnnotationNode;
import com.braintribe.asm.tree.ClassNode;
import com.braintribe.asm.tree.FieldInsnNode;
import com.braintribe.asm.tree.FieldNode;
import com.braintribe.asm.tree.InsnList;
import com.braintribe.asm.tree.LdcInsnNode;
import com.braintribe.asm.tree.LocalVariableNode;
import com.braintribe.asm.tree.MethodInsnNode;
import com.braintribe.asm.tree.MethodNode;
import com.braintribe.asm.tree.MultiANewArrayInsnNode;
import com.braintribe.asm.tree.TryCatchBlockNode;
import com.braintribe.asm.tree.TypeInsnNode;
import com.braintribe.asm.tree.VarInsnNode;
import com.braintribe.logging.Logger;
import com.braintribe.zarathud.model.data.ScopeModifier;

/**
 * @author dirk
 *
 */
public abstract class AsmClassDepScanner {
	private static Logger log = Logger.getLogger(AsmClassDepScanner.class);
	private static Package treePackage = ClassNode.class.getPackage();
	private static String methodName = "T";
	private static String ownerName = "com/braintribe/model/generic/reflection/EntityTypes";

	public interface ClassExtracter {
		public Collection<String> extractClasses(Object value); 
	}
	
	public static class ClassFromSignatureExtractor implements ClassExtracter {
		private static Pattern pattern = Pattern.compile("L([^;<:-]*)[;<]");
		
		@Override
		public Collection<String> extractClasses(Object value) {
			String signature = (String)value;
			List<String> result = new ArrayList<String>();

			Matcher matcher = pattern.matcher(signature);
			while (matcher.find()) {
				String className = matcher.group(1);
				if (className.length() == 0)
					continue;
				result.add(className);
			}
			
			return result;
		}
	}
	
	public static class OwnerExtractor implements ClassExtracter {
		
		@Override
		public Collection<String> extractClasses(Object value) {
			String typeInsnNodeOwner = (String)value;
			return Arrays.asList(typeInsnNodeOwner);
		}
	}
	/*
	public static class CstExtractor implements ClassExtracter {
		
		public Collection<String> extractClasses(Object value) {
			if (value instanceof Type) {
				Type type = (Type)value; 
				return Arrays.asList(type.getInternalName());
			}
			else return Collections.emptySet();
		}
	}*/
	
	public static class CstExtractor implements ClassExtracter {
		  private ClassFromSignatureExtractor componentTypeExtractor = new ClassFromSignatureExtractor();
		  
		  @Override
		public Collection<String> extractClasses(Object value) {
		   if (value instanceof Type) {
		    Type type = (Type)value;
		    String name = type.getInternalName();
		    
		    if (name.charAt(0) == '[') {
		     return componentTypeExtractor.extractClasses(name);
		    }
		    else
		     return Arrays.asList(name);
		   }
		   else return Collections.emptySet();
		  }
	}
	
	public static class ClassNameListExtractor implements ClassExtracter {
		@Override
		public Collection<String> extractClasses(Object value) {
			@SuppressWarnings("unchecked")
			List<String> classNameList = (List<String>)value;
			return classNameList;
		}
	}
	
	public static class ClassNameExtractor implements ClassExtracter {
		@Override
		public Collection<String> extractClasses(Object value) {
			String className = (String)value;
			return Arrays.asList(className);
		}
	}
	
	public static class ClassNameFromTypeInsnExtractor implements ClassExtracter {
		@Override
		public Collection<String> extractClasses(Object value) {
			String className = (String)value;
			if (className.startsWith("[")) {
				int s = className.indexOf('L') + 1;
				int e = className.length() - 1;
				className = className.substring(s, e);
				
			}

			return Arrays.asList(className);
		}
	}
	
	public static class FieldMatch {
		private Class<?> type;
		private String name;
		
		public FieldMatch(Class<?> type, String name) {
			super();
			this.type = type;
			this.name = name;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FieldMatch other = (FieldMatch) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}
		
		
	}
	
	public interface Visitor {
		public void visit(Object object) throws IllegalStateException;
	}
	
	private static Map<FieldMatch, ClassExtracter> extracters = new HashMap<FieldMatch, ClassExtracter>();
	
	static {
		//extracters.put(new FieldMatch(ClassNode.class, "signature"), new ClassFromSignatureExtractor());
//		extracters.put(new FieldMatch(ClassNode.class, "interfaces"), new ClassNameListExtractor());
//		extracters.put(new FieldMatch(ClassNode.class, "superName"), new ClassNameExtractor());
//		extracters.put(new FieldMatch(FieldNode.class, "desc"), new ClassFromSignatureExtractor());
//		extracters.put(new FieldMatch(FieldNode.class, "signature"), new ClassFromSignatureExtractor());
//		extracters.put(new FieldMatch(MethodNode.class, "desc"), new ClassFromSignatureExtractor());
//		extracters.put(new FieldMatch(MethodNode.class, "signature"), new ClassFromSignatureExtractor());
//		extracters.put(new FieldMatch(MethodNode.class, "exceptions"), new ClassNameListExtractor());
		
		extracters.put(new FieldMatch(LocalVariableNode.class, "desc"), new ClassFromSignatureExtractor());
		extracters.put(new FieldMatch(LocalVariableNode.class, "signature"), new ClassFromSignatureExtractor());		
		extracters.put(new FieldMatch(TypeInsnNode.class, "desc"), new CstExtractor());
		extracters.put(new FieldMatch(TryCatchBlockNode.class, "type"), new ClassNameExtractor());
		extracters.put(new FieldMatch(MultiANewArrayInsnNode.class, "desc"), new ClassFromSignatureExtractor());
		
//		//extracters.put(new FieldMatch(AnnotationNode.class, "desc"), new ClassFromSignatureExtractor());
//		extracters.put(new FieldMatch(FieldInsnNode.class, "owner"), new OwnerExtractor());
		extracters.put(new FieldMatch(LdcInsnNode.class, "cst"), new CstExtractor());
	}
	
	public static void traverse(Object object, Visitor visitor) throws IllegalArgumentException, IllegalAccessException {
		if (object == null) 
			return;
		
		if (object instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>)object;
			for (Object element: collection) {
				if (isTreeObject(element)) {
					traverse(element, visitor);
				}
			}
		}
		else if (object instanceof InsnList) {
			InsnList insnList = (InsnList)object;
			for (int i = 0; i < insnList.size(); i++) {
				AbstractInsnNode abstractInsnNode = insnList.get(i);
				traverse(abstractInsnNode, visitor);
			}
		}
		else if (object.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(object); i++) {
				traverse(Array.get(object, i), visitor);
			}
		}
		else if (!isTreeObject(object)) {
			return;
		}

		
		if (object instanceof LocalVariableNode){
			LocalVariableNode lvn = (LocalVariableNode)object;
			if (lvn.name.equals("this"))
				return;
		}
		
		visitor.visit(object);
		
		Field[] fields = object.getClass().getFields();
		
		for (Field field: fields) {
			if ((field.getModifiers() & Modifier.STATIC) == 0) {
				Class<?> fieldType = field.getType();
				Object fieldValue = field.get(object);
				
				if (fieldType == AnnotationNode.class)
					System.out.println(fieldValue);
				
				if (fieldValue != null) {
					traverse(fieldValue, visitor);
				}
			}
		}
	}
	
	public static boolean isTreeObject(Object object) {
		return object != null && object.getClass().getPackage() == treePackage;
	}
	
	private static Collection<AnnotationTuple> extractAnnotationTuples(Stream<AnnotationNode> stream){
		List<AnnotationTuple> tuples = stream.map(n -> {
				return createTupleFromAnnotationNode(n);				
		}).collect( Collectors.toList());
		
		if (tuples == null) {
			return Collections.emptyList();
		}		
		return tuples;
	}
	
	/**
	 * currently only retrieves the values of the first annotation it finds
	 * (is thought to be the GenericModel annotation, and its gwtModule member)
	 * @param in - the {@link InputStream} to be read (the binary class)
	 * @return - a {@link Set} of {@link AnnotationTuple} extracted 
	 * @throws AsmClassDepScannerException - arrgh
	 */
	public static List<AnnotationTuple> getClassAnnotationMembers( InputStream in) throws AsmClassDepScannerException {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final List<AnnotationTuple> tuples = new ArrayList<AnnotationTuple>();
			traverse(classNode, new Visitor() {				
				@Override
				public void visit(Object object) {
					if (object instanceof ClassNode) {
						ClassNode node = (ClassNode) object;
						
						if ( node.visibleAnnotations != null) {
							tuples.addAll( extractAnnotationTuples( node.visibleAnnotations.stream()));
						}
						if (node.invisibleAnnotations != null) {
							tuples.addAll( extractAnnotationTuples( node.invisibleAnnotations.stream()));						
						}						
					}
				}
			});
			return tuples;
		} catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw new AsmClassDepScannerException("cannot close class bytes stream as " + e, e);
				}
		}
	}
	
	private static AnnotationTuple createTupleFromAnnotationNode( AnnotationNode annotationNode) {
		AnnotationTuple tuple = new AnnotationTuple();
		
		tuple.setDesc( annotationNode.desc);
		@SuppressWarnings("rawtypes")
		List values = annotationNode.values;
		if (values == null) {
			return tuple;
		}			
		for (int j = 0; j < values.size() - 1; j = j+2) {
			String memberName = (String) values.get( j);
			Object obj = values.get(j+1);
			if (obj instanceof AnnotationNode) {
				AnnotationTuple child = createTupleFromAnnotationNode( (AnnotationNode) obj);
				tuple.getValues().put( memberName, child);
			} 
			else if (obj instanceof Collection<?>) {
				@SuppressWarnings("unchecked")
				Collection<Object> collection = (Collection<Object>) obj;
				int i = 1;
				for (Object suspect : collection) {
					if (suspect instanceof AnnotationNode) { 
						AnnotationTuple child = createTupleFromAnnotationNode((AnnotationNode) suspect);
						tuple.getValues().put( memberName + "-" + i, child);
					} else { 
						tuple.getValues().put( memberName + "-" + i, suspect);
					}
					i++;
				}
			} else {
				tuple.getValues().put( memberName, obj);
			}
		}
		return tuple;
	}
	
	/**
	 * extract the interface names of the class 
	 * @param in - the {@link InputStream} to be read (the binary class)
	 * @return - a {@link List} of {@link String} extracted 
	 * @throws AsmClassDepScannerException - arrgh
	 */
	public static List<String> getInterfaces( InputStream in) throws AsmClassDepScannerException {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final List<String> interfaceList = new ArrayList<String>(); 
			traverse(classNode, new Visitor() {
				@Override
				public void visit(Object object) {
					if (object instanceof ClassNode) {
						ClassNode node = (ClassNode) object;						
						interfaceList.addAll( node.interfaces);						
					}
				}
			});
			return interfaceList;
		} catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw new AsmClassDepScannerException("cannot close class bytes stream as " + e, e);
				}
		}
	}
	
	/**
	 * extract the super types of a class 
	 * @param in - the {@link InputStream} to be read (the binary class)
	 * @return a {@link Map} name to super name 
	 * @throws AsmClassDepScannerException - arrgh
	 */
	public static Map<String, String> getSuper( InputStream in) throws AsmClassDepScannerException {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final Map<String, String> superMap = new HashMap<String, String>(); 
			traverse(classNode, new Visitor() {
				@Override
				public void visit(Object object) {
					if (object instanceof ClassNode) {
						ClassNode node = (ClassNode) object;						
						superMap.put( node.name, node.superName);						
					}
				}
			});
			return superMap;
		} catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw new AsmClassDepScannerException("cannot close class bytes stream as " + e, e);
				}
		}		
	}
	
	private static AccessModifier determineAccess(  int access) {
		// access  
		if ((access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
			return AccessModifier.PUBLIC;
		} else 
			if ((access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED) {
				return AccessModifier.PROTECTED;
			} else 
				return AccessModifier.PRIVATE;
	}
	
	private static ScopeModifier determineScope( int access) {
		if ((access & Opcodes.ACC_VOLATILE) == Opcodes.ACC_VOLATILE) {
			return ScopeModifier.VOLATILE;
		}
		else if ((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL) {
			return ScopeModifier.FINAL;
		}
		return ScopeModifier.DEFAULT;
		
	}
	
	public static ClassData getClassData( InputStream in) throws AsmClassDepScannerException {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final ClassData classData = new ClassData(); 
			traverse(classNode, new Visitor() {
				@Override			
				public void visit(Object object) {
					if (object instanceof ClassNode) {
						ClassNode node = (ClassNode) object;	
						
						classData.setSignature( node.signature);
					
						if ((node.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT) {
							classData.setIsAbstract( true);
						}
						if ((node.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
							classData.setIsStatic( true);
						}
						if ((node.access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL) {
							classData.setIsFinal( true);
						}
						if ((node.access & Opcodes.ACC_SYNCHRONIZED) == Opcodes.ACC_SYNCHRONIZED) {
							classData.setIsSynchronized( true);
						}
					
						classData.setAccessNature( determineAccess(node.access));						
						
						// fields
						List<FieldNode> fieldNodes = node.fields;
						if (
								fieldNodes != null &&
								fieldNodes.size() > 0
							) {
							List<FieldData> fieldDatas = new ArrayList<FieldData>();
							for (FieldNode fieldNode : fieldNodes) {
								FieldData fieldData = new FieldData();
								fieldData.setName( fieldNode.name);
								fieldData.setDesc( fieldNode.desc);
								fieldData.setIntializer( fieldNode.value);								
								fieldData.setSignature( fieldNode.signature);
								fieldData.setAccessModifier( determineAccess( fieldNode.access));
								List<AnnotationTuple> tuples = new ArrayList<>();
								
								
								if ((node.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
									classData.setIsStatic( true);
								}
								if ((node.access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL) {
									classData.setIsFinal( true);
								}
								
								fieldData.setScopeModifier( determineScope( fieldNode.access));
								
								
								
								if ( fieldNode.visibleAnnotations != null) {
									tuples.addAll( extractAnnotationTuples( fieldNode.visibleAnnotations.stream()));
								}
								if (fieldNode.invisibleAnnotations != null) {
									tuples.addAll( extractAnnotationTuples( fieldNode.invisibleAnnotations.stream()));						
								}	
								fieldData.setAnnotationTuples(tuples);
																
								fieldDatas.add(fieldData);							
								
							}
							classData.setFieldData(fieldDatas);
							
							List<AnnotationTuple> tuples = new ArrayList<>();
							if ( classNode.visibleAnnotations != null) {
								tuples.addAll( extractAnnotationTuples( classNode.visibleAnnotations.stream()));
							}
							if (classNode.invisibleAnnotations != null) {
								tuples.addAll( extractAnnotationTuples( classNode.invisibleAnnotations.stream()));						
							}
							
							classData.setAnnotationTuples( tuples);
						}
												
					}
				}
			});
			return classData;
		} catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw new AsmClassDepScannerException("cannot close class bytes stream as " + e, e);
				}
		}		
	}
	
	public static List<MethodData> getMethodData( InputStream in) throws AsmClassDepScannerException {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final List<MethodData> methodData = new ArrayList<MethodData>();
			traverse(classNode, new Visitor() {
				@Override
				public void visit(Object object) {
					if (object instanceof ClassNode) {
						ClassNode node = (ClassNode) object;						
						
						List<MethodNode> methods = node.methods;
						if (methods != null && methods.size() > 0) {
							for (MethodNode methodNode : methods) {																
								MethodData method = new MethodData();															
								method.setMethodName( methodNode.name);
								String desc = methodNode.desc;
								method.setDesc( desc);
								
								Type returnType = Type.getReturnType( desc);															
								method.setReturnType( returnType.toString());
								
								Type [] argumentTypes = Type.getArgumentTypes( desc);
								if (argumentTypes != null) {
									List<String> arguments = new ArrayList<String>( argumentTypes.length);
									for (Type argument : argumentTypes) {
										arguments.add( argument.toString());
									}
									method.setArgumentTypes( arguments);
								}
								
								method.setSignature( methodNode.signature);
								
								// annotations
								List<AnnotationTuple> tuples = new ArrayList<>();								
								if ( methodNode.visibleAnnotations != null) {
									tuples.addAll( extractAnnotationTuples( methodNode.visibleAnnotations.stream()));
								}
								if (methodNode.invisibleAnnotations != null) {
									tuples.addAll( extractAnnotationTuples( methodNode.invisibleAnnotations.stream()));						
								}	
								method.setAnnotationTuples(tuples);
								
								// exceptions 			
								List<String> exceptions = methodNode.exceptions;
								method.setExceptions(exceptions);
								
								if ((methodNode.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT) {
									method.setIsAbstract( true);
								}
								if ((methodNode.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
									method.setIsStatic( true);
								}
								if ((methodNode.access & Opcodes.ACC_SYNCHRONIZED) == Opcodes.ACC_SYNCHRONIZED) {
									method.setIsSynchronized( true);
								}
								if ((methodNode.access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL) {
									method.setIsFinal( true);
								}
								if ((methodNode.access & Opcodes.ACC_NATIVE) == Opcodes.ACC_NATIVE) {
									method.setIsNative( true);
								}
								if ((methodNode.access & Opcodes.ACC_STRICT) == Opcodes.ACC_STRICT) {
									method.setIsStrictFp( true);
								}
								
								method.setAccessModifier( determineAccess( methodNode.access));
																								
								// extract type references from body
								List<MethodBodyTypeReference> typeRefs = extractTypeReferences( method, methodNode);															
								method.setBodyTypes(typeRefs);
								
								// braintribe model specifica 
								if (method.getMethodName().equalsIgnoreCase("<clinit>")) {
									EntityTypeInitializerData extractEntityTypesDeclaration = extractEntityTypesDeclaration(methodNode);
									method.setEntityTypeInitializerData(extractEntityTypesDeclaration);
								}
								
								// mark if the function has any instructions at all -> to detect default methods in interfaces
								method.setContainsBody( hasBody(methodNode));
								
								
								methodData.add( method);									
							}
						}						
					}
				}

				/**
				 * specialized code to detect the T field of generic entities.. yes, badly against SOC to treat that here.. 
				 * @param methodNode - the {@link MethodNode} to traverse, "<clinit>"
				 * @return - the extracted {@link EntityTypeInitializerData}
				 */
				private EntityTypeInitializerData extractEntityTypesDeclaration(MethodNode methodNode) {
					InsnList instructions = methodNode.instructions;
					int insNum = instructions.size();
					for (int i = 0; i < insNum; i++) {
						AbstractInsnNode abstractInsnNode = instructions.get(i);
						if (abstractInsnNode instanceof MethodInsnNode) {
							MethodInsnNode minsn = (MethodInsnNode) abstractInsnNode;
							if (ownerName.equalsIgnoreCase(minsn.owner) && (methodName.equalsIgnoreCase( minsn.name))) {
								EntityTypeInitializerData etid = new EntityTypeInitializerData();
						
								AbstractInsnNode prevNode = abstractInsnNode.getPrevious();
								AbstractInsnNode nextNode = abstractInsnNode.getNext();
								
								if (prevNode != null && prevNode instanceof LdcInsnNode) {
									LdcInsnNode ldcinsn = (LdcInsnNode) prevNode;										
									// type references are strings?? 
									if (ldcinsn.cst instanceof Type) {
										etid.setValue(((Type) ldcinsn.cst).getInternalName());										
									}
								}
								if (nextNode != null && nextNode instanceof FieldInsnNode) {
									FieldInsnNode fieldInsnNode = (FieldInsnNode) nextNode;
									etid.setField( fieldInsnNode.name);
									etid.setOwner( fieldInsnNode.owner);
								}
								
								if (etid.getValue() == null || etid.getField() == null || etid.getOwner() == null) {
									log.error("entity type t reference is incomplete ");
								}
								else {
									return etid;
								}
							}

						}
					}
					return null;
					
					
				}
				
				private boolean hasBody( MethodNode methodNode) {
					InsnList instructions = methodNode.instructions;
					int insNum = instructions.size();
					return insNum > 0;
				}

				/**
				 * extract the method's instructions and retrieve the types found 
				 * @param method - the {@link MethodData}
				 * @param methodNode - the {@link MethodNode}
				 * @return - a list of {@link MethodBodyTypeReference} 
				 */
				@SuppressWarnings("unused")
				private List<MethodBodyTypeReference> extractTypeReferences(MethodData method, MethodNode methodNode) {
					InsnList instructions = methodNode.instructions;
					int insNum = instructions.size();
					List<MethodBodyTypeReference> result = new ArrayList<>();
			
					for (int i = 0; i < insNum; i++) {
						AbstractInsnNode abstractInsnNode = instructions.get(i);

						if (abstractInsnNode instanceof VarInsnNode) {
							VarInsnNode varins = (VarInsnNode) abstractInsnNode;											
						}
						else if (abstractInsnNode instanceof LdcInsnNode) {
							LdcInsnNode ldcinsn = (LdcInsnNode) abstractInsnNode;
							Object obj = ldcinsn.cst;
							// type references are strings?? 
							if (obj instanceof Type) {
								MethodBodyTypeReference mbtr = new MethodBodyTypeReference();
								mbtr.setOwner( (String) ((Type) ldcinsn.cst).getInternalName());
								result.add( mbtr);
							}							
						}
						
						else if (abstractInsnNode instanceof MethodInsnNode) {							
							MethodInsnNode minsn = (MethodInsnNode) abstractInsnNode;							
							MethodBodyTypeReference mbtr = new MethodBodyTypeReference();
							mbtr.setOwner(minsn.owner);
							mbtr.setInvokedMethodName( minsn.name);
							mbtr.setDesc( minsn.desc);
							result.add( mbtr);							
						}
						else if (abstractInsnNode instanceof FieldInsnNode) {
							FieldInsnNode finsn = (FieldInsnNode) abstractInsnNode;
							MethodBodyTypeReference mbtr = new MethodBodyTypeReference();
							mbtr.setOwner(finsn.owner);
							mbtr.setInvokedMethodName( finsn.name);
							mbtr.setDesc( finsn.desc);
							result.add( mbtr);			
						}						
						// add missing types here 
					
					}
					return result;
				}
			});
			return methodData;
		} catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw new AsmClassDepScannerException("cannot close class bytes stream as " + e, e);
				}
		}		
	}
	/**
	 * @param in - the {@link InputStream} to access the data 
	 * @return {@link InheritanceData} extracted 
	 * @throws AsmClassDepScannerException - arrgh
	 */
	public static InheritanceData getInheritanceData( InputStream in) throws AsmClassDepScannerException {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final InheritanceData inheritanceData = new InheritanceData(); 
			traverse(classNode, new Visitor() {
				@Override
				public void visit(Object object) {
					if (object instanceof ClassNode) {
						ClassNode node = (ClassNode) object;	
						if (node.superName != null) {
							inheritanceData.setSuperClass( node.superName.replaceAll( "/", "."));
						}
						inheritanceData.setInterfaces( node.interfaces);		
						if ((node.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT) {
							inheritanceData.setAbstractClass( true);
						}
						if ((node.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE) {
							inheritanceData.setInterfaceClass( true);
						}
						inheritanceData.setName( node.name);
						
					}
				}
			});
			return inheritanceData;
		} catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw new AsmClassDepScannerException("cannot close class bytes stream as " + e, e);
				}
		}		
	}
	
	/**
	 * definitively incomplete method as it doesn't retrieve all types declared in this class.. 
	 * @param in - the {@link InputStream} to read from 
	 * @return - 
	 * @throws AsmClassDepScannerException
	 */
	public static Set<String> getClassDependencies( InputStream in) throws AsmClassDepScannerException {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final Set<String> classes = new TreeSet<String>();
			traverse(classNode, new Visitor() {
				@Override
				public void visit(Object object) {
					Class<?> clazz = object.getClass();
					
					for (Field field: clazz.getFields()) {
						Object fieldValue;
						try {
							fieldValue = field.get(object);
							if (fieldValue != null) {
								FieldMatch match = new FieldMatch(clazz, field.getName());
								ClassExtracter extracter = extracters.get(match);
								if (extracter != null) {
									Collection<String> result = extracter.extractClasses(fieldValue);
									for (String str : result) {
										if (str.startsWith( "[")) {
											System.out.println("bracket");
										}
									}
									classes.addAll(result);
								}
							}
						}
						catch (RuntimeException e) {
							throw e;
						}
						catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			});
			Set<String> normalizedClasses = new HashSet<String>(classes.size());
			for (String clazz: classes) {			
				normalizedClasses.add(clazz.replace('/', '.'));
			}
			return normalizedClasses;
		} catch (Exception e) {
			throw new AsmClassDepScannerException("cannot scan class bytes as " + e, e);
		} 
		
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw new AsmClassDepScannerException("cannot close class bytes stream as " + e, e);
				}
		}
	}
	
	/**
	 * @param classFile
	 * @return
	 * @throws AsmClassDepScannerException
	 */
	public static Set<String> getClassDependencies(File classFile) throws AsmClassDepScannerException {
		try {				
			InputStream in = new FileInputStream(classFile);
			return getClassDependencies( in);
		} catch (FileNotFoundException e) {
			throw new AsmClassDepScannerException("cannot open class file stream as " + e, e);
		}		
	}
	
	
	public static void main( String [] args) {
		/*
		ClassFromSignatureExtractor ex = new ClassFromSignatureExtractor();
		for (String arg : args) {
			Collection<String> extractClasses = ex.extractClasses( arg);
			if (extractClasses == null || extractClasses.size() == 0) {
				System.out.println("no classes extracted");
			}
			else {
				System.out.println(extractClasses.stream().collect( Collectors.joining(",")));
			}
		}
		*/
		
	}
}
