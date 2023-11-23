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
package com.braintribe.wire.impl.compile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.braintribe.asm.AnnotationVisitor;
import com.braintribe.asm.ClassReader;
import com.braintribe.asm.ClassVisitor;
import com.braintribe.asm.ClassWriter;
import com.braintribe.asm.Label;
import com.braintribe.asm.MethodVisitor;
import com.braintribe.asm.Opcodes;
import com.braintribe.asm.Type;
import com.braintribe.asm.tree.AbstractInsnNode;
import com.braintribe.asm.tree.AnnotationNode;
import com.braintribe.asm.tree.ClassNode;
import com.braintribe.asm.tree.FieldInsnNode;
import com.braintribe.asm.tree.FieldNode;
import com.braintribe.asm.tree.InsnList;
import com.braintribe.asm.tree.InsnNode;
import com.braintribe.asm.tree.JumpInsnNode;
import com.braintribe.asm.tree.LabelNode;
import com.braintribe.asm.tree.LdcInsnNode;
import com.braintribe.asm.tree.LocalVariableNode;
import com.braintribe.asm.tree.MethodInsnNode;
import com.braintribe.asm.tree.MethodNode;
import com.braintribe.asm.tree.TryCatchBlockNode;
import com.braintribe.asm.tree.TypeInsnNode;
import com.braintribe.asm.tree.VarInsnNode;
import com.braintribe.cfg.ScopeContext;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.annotation.Scope;
import com.braintribe.wire.api.context.InternalWireContext;
import com.braintribe.wire.api.scope.InstanceParameterization;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.impl.util.Exceptions;

public class WireManagedSpaceFactory implements Opcodes, WireTypesAndMethods {
	private static final String contextVarName = "$context$";

	private static final Logger logger = Logger.getLogger(WireManagedSpaceFactory.class.getName());

	private static class FactoryInfo {
		public MethodHandle factory;
		public Class<?> wireSpaceClass;
	}

	// private Map<Class<? extends WireSpace>, MethodHandle> factories = new ConcurrentHashMap<>();
	private final Map<String, FactoryInfo> factoryByClassName = new ConcurrentHashMap<>();
	private final WireEnricherClassLoader classLoader;
	private final InternalWireContext context;
	private ClassLoader spaceClassLoader = WireManagedSpaceFactory.class.getClassLoader();

	public WireManagedSpaceFactory(InternalWireContext context, Predicate<String> classNameFilter) {
		this.context = context;
		this.classLoader = new WireEnricherClassLoader(classNameFilter);
	}

	public WireManagedSpaceFactory(InternalWireContext context, Predicate<String> classNameFilter, ClassLoader spaceClassLoader) {
		this.context = context;
		this.spaceClassLoader = spaceClassLoader;
		this.classLoader = new WireEnricherClassLoader(classNameFilter);
	}

	private FactoryInfo getFactoryInfo(String spaceClassName) {
		return factoryByClassName.computeIfAbsent(spaceClassName, this::createFactoryInfo);
	}

	private FactoryInfo createFactoryInfo(String spaceClassName) {
		try {
			logger.finest(() -> "loading wire space " + spaceClassName);

			Class<? extends WireSpace> enrichedSpaceClass = Class.forName(spaceClassName, false, classLoader).asSubclass(WireSpace.class);

			return createFactoryInfo(enrichedSpaceClass);

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while building factory for " + spaceClassName, IllegalStateException::new);
		}
	}

	private FactoryInfo createFactoryInfo(Class<? extends WireSpace> spaceClass) throws IllegalAccessException {
		try {
			FactoryInfo factoryInfo = new FactoryInfo();
			factoryInfo.factory = MethodHandles.lookup().unreflectConstructor(spaceClass.getConstructor(InternalWireContext.class));
			factoryInfo.wireSpaceClass = spaceClass;

			return factoryInfo;

		} catch (NoSuchMethodException e) {
			if (hasManagedAnnotation(spaceClass))
				throw Exceptions.unchecked(e, "Space " + spaceClass.getName() + " was not instrumented. Seems it is located in an unmapped package");
			else
				throw Exceptions.unchecked(e,
						"Space " + spaceClass.getName() + " was not instrumented. Seems you forgot to annotate it with @Managed.");
		}
	}

	private boolean hasManagedAnnotation(Class<? extends WireSpace> enrichedSpaceClass) {
		return enrichedSpaceClass.getDeclaredAnnotation(Managed.class) != null;
	}

	public <T extends WireSpace> Class<T> acquireSpaceClass(String spaceClassName) {
		return (Class<T>) getFactoryInfo(spaceClassName).wireSpaceClass;
	}

	public <T extends WireSpace> T createInstance(String spaceClassName) {
		FactoryInfo factoryInfo = getFactoryInfo(spaceClassName);

		try {
			return (T) factoryInfo.factory.invoke(context);
		} catch (Throwable e) {
			throw Exceptions.unchecked(e, "Error while constructing instance of " + spaceClassName, IllegalStateException::new);
		}
	}

	private URL getClassData(String spaceClassName) {
		String resourcePath = spaceClassName.replace('.', '/') + ".class";

		URL resourceUrl = spaceClassLoader.getResource(resourcePath);

		if (resourceUrl == null)
			resourceUrl = Thread.currentThread().getContextClassLoader().getResource(resourcePath);

		if (resourceUrl == null)
			throw new IllegalStateException("could not find classpath resource " + resourcePath);

		return resourceUrl;
	}

	private static class MainVisitor extends ClassVisitor {

		private boolean enriched = false;
		private boolean wireSpace = false;

		public MainVisitor(ClassVisitor delegate) {
			super(ASM7, delegate);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			if (desc.equals(managedType.desc) || desc.equals(beansType.desc)) {
				wireSpace = true;
			} else if (desc.equals(enrichedType.desc)) {
				enriched = true;
				// turn of delegation from now on to safe time and memory
				cv = null;
			}

			return super.visitAnnotation(desc, visible);
		}

		public boolean isEnriched() {
			return enriched;
		}

		public boolean isWireSpace() {
			return wireSpace;
		}

	}
	
	/**
	 * pumps all data up to EOF with a configurable buffer size from input to output stream (no output flushing here)
	 *
	 * <p>
	 * copied from com.braintribe.utils.IOTools
	 * 
	 * @return the number of transferred bytes
	 */
	private static long pump(final InputStream inputStream, final OutputStream outputStream, int bufferSize) throws IOException {
		final byte[] buffer = new byte[bufferSize];

		int count;
		long totalCount = 0;

		while ((count = inputStream.read(buffer)) != -1) {
			try {
				outputStream.write(buffer, 0, count);
			} catch (Exception e) {
				throw new IOException("Error while transfering data. Data transferred so far: " + totalCount + ". Current buffer size: " + count, e);
			}
			totalCount += count;
		}

		return totalCount;
	}
	
	private static final int SIZE_64K = 1 << 16;
	
	private static byte[] slurpBytes(final InputStream inputStream) throws IOException {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		pump(inputStream, outputStream, SIZE_64K);
		return outputStream.toByteArray();
	}

	public byte[] loadSpaceClass(String className, InputStream in) {

		try {
			byte classBytes[] = slurpBytes(in);
			
			ClassReader reader = new ClassReader(classBytes);

			ClassNode classNode = new ClassNode();
			MainVisitor mainVisitor = new MainVisitor(classNode);

			reader.accept(mainVisitor, ClassReader.SKIP_FRAMES);
			
			if (!mainVisitor.isWireSpace() || mainVisitor.isEnriched()) {
				return classBytes;
			}

			logger.finest(() -> "Compiling bean space " + className);
			long s = System.currentTimeMillis();

			Type targetType = Type.getObjectType(classNode.name);

			List<ManagedInstanceFactoryMethod> factoryMethods = getFactoryMethods(classNode, false);

			classNode.interfaces.add(enrichedWireSpaceType.internal);

			writeConstructor(classNode, targetType, factoryMethods);
			writeImportFieldReflection(classNode, targetType, classNode.superName);
			enrichMethodsWithLifecycleSupport(targetType, factoryMethods);

			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
				@Override
				protected String getCommonSuperClass(String type1, String type2) {
					Class<?> class1;
					try {
						class1 = Class.forName(type1.replace('/', '.'), false, classLoader);
					} catch (Exception e) {
						throw new TypeNotPresentException(type1, e);
					}
					Class<?> class2;
					try {
						class2 = Class.forName(type2.replace('/', '.'), false, classLoader);
					} catch (Exception e) {
						throw new TypeNotPresentException(type2, e);
					}
					if (class1.isAssignableFrom(class2)) {
						return type1;
					}
					if (class2.isAssignableFrom(class1)) {
						return type2;
					}
					if (class1.isInterface() || class2.isInterface()) {
						return "java/lang/Object";
					} else {
						do {
							class1 = class1.getSuperclass();
						} while (!class1.isAssignableFrom(class2));
						return class1.getName().replace('.', '/');
					}
				}
			};
			classNode.accept(writer);

			long e = System.currentTimeMillis();
			long d = e - s;

			logger.finest(() -> "Compiled wire space " + className + " in " + d + "ms");
			return writer.toByteArray();
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while enriching WireSpace", IllegalStateException::new);
		}
	}

	private static void writeImportFieldReflection(ClassNode classNode, Type targetType, String superInternalName) {
		List<FieldNode> fields = classNode.fields;
		List<FieldNode> importFields = new ArrayList<>();

		for (FieldNode fieldNode : fields) {
			if (isImportField(fieldNode)) {
				fieldNode.access = ACC_PUBLIC;
				importFields.add(fieldNode);
			}
		}

		MethodVisitor reflectImportFields = classNode.visitMethod(ACC_PUBLIC, listImportFieldsMethod.name(), listImportFieldsMethod.asmDesc(), null,
				null);
		reflectImportFields.visitCode();
		reflectImportFields.visitLdcInsn(enrichedWireSpaceType.type);
		reflectImportFields.visitLdcInsn(targetType);
		getSuperclassMethod.invoke(reflectImportFields);
		isAssignableFromMethod.invoke(reflectImportFields);
		Label beginOfLocalRecording = new Label();
		reflectImportFields.visitJumpInsn(IFEQ, beginOfLocalRecording);
		reflectImportFields.visitVarInsn(ALOAD, 0);
		reflectImportFields.visitVarInsn(ALOAD, 1);
		reflectImportFields.visitMethodInsn(INVOKESPECIAL, superInternalName, listImportFieldsMethod.name(), listImportFieldsMethod.asmDesc(), false);
		reflectImportFields.visitLabel(beginOfLocalRecording);

		MethodVisitor setImportField = classNode.visitMethod(ACC_PUBLIC, setImportFieldMethod.name(), setImportFieldMethod.asmDesc(), null, null);
		setImportField.visitCode();
		setImportField.visitLdcInsn(targetType);
		setImportField.visitVarInsn(ALOAD, 1);
		Label thisClassMatchedLabel = new Label();
		setImportField.visitJumpInsn(IF_ACMPEQ, thisClassMatchedLabel);
		setImportField.visitVarInsn(ALOAD, 0);
		setImportField.visitVarInsn(ALOAD, 1);
		setImportField.visitVarInsn(ILOAD, 2);
		setImportField.visitVarInsn(ALOAD, 3);
		setImportField.visitMethodInsn(INVOKESPECIAL, superInternalName, setImportFieldMethod.name(), setImportFieldMethod.asmDesc(), false);
		setImportField.visitInsn(RETURN);
		setImportField.visitLabel(thisClassMatchedLabel);

		Label switchDefault = new Label();

		if (!importFields.isEmpty()) {
			Label[] switchLabels = new Label[importFields.size()];
			for (int i = 0; i < switchLabels.length; i++)
				switchLabels[i] = new Label();

			setImportField.visitVarInsn(ILOAD, 2);
			setImportField.visitTableSwitchInsn(0, importFields.size() - 1, switchDefault, switchLabels);

			String targetTypeInternalName = targetType.getInternalName();

			for (int i = 0; i < importFields.size(); i++) {
				FieldNode fieldNode = importFields.get(i);

				// contribute to reflect method
				reflectImportFields.visitVarInsn(ALOAD, 1);
				reflectImportFields.visitLdcInsn(targetType);
				reflectImportFields.visitLdcInsn(Type.getType(fieldNode.desc));
				reflectImportFields.visitIntInsn(BIPUSH, i);
				recordMethod.invoke(reflectImportFields);

				// contribute to set method
				setImportField.visitLabel(switchLabels[i]);
				setImportField.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				setImportField.visitVarInsn(ALOAD, 0);
				setImportField.visitVarInsn(ALOAD, 3);
				setImportField.visitTypeInsn(CHECKCAST, Type.getType(fieldNode.desc).getInternalName());
				setImportField.visitFieldInsn(PUTFIELD, targetTypeInternalName, fieldNode.name, fieldNode.desc);
				setImportField.visitInsn(RETURN);
			}
		}

		setImportField.visitLabel(switchDefault);
		setImportField.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		setImportField.visitTypeInsn(NEW, illegalArgumentExceptionType.internal);
		setImportField.visitInsn(DUP);
		setImportField.visitLdcInsn("index out of bounds");
		illegalArgumentExceptionConstructor.invoke(setImportField);
		setImportField.visitInsn(ATHROW);
		setImportField.visitEnd();

		reflectImportFields.visitInsn(RETURN);
		reflectImportFields.visitEnd();
	}

	private static boolean isImportField(FieldNode fieldNode) {
		List<AnnotationNode> visibleAnnotations = fieldNode.visibleAnnotations;

		if (visibleAnnotations == null)
			return false;

		for (AnnotationNode annotationNode : visibleAnnotations) {
			if (annotationNode.desc.equals(importType.desc)) {
				return true;
			}
		}

		return false;
	}

	private static void enrichMethodsWithLifecycleSupport(Type targetType, List<ManagedInstanceFactoryMethod> factoryMethods) {
		for (ManagedInstanceFactoryMethod factoryMethod : factoryMethods) {

			MethodNode methodNode = factoryMethod.methodNode;

			Type[] parameters = factoryMethod.parameters;

			List<LocalVariableNode> localVariables = methodNode.localVariables;
			if (localVariables == null) {
				localVariables = new ArrayList<>();
				methodNode.localVariables = localVariables;
			}

			LabelNode startLabel = new LabelNode();
			LabelNode endLabel = new LabelNode();

			LocalVariableNode thisVariable = localVariables.stream().filter(v -> v.index == 0).findFirst().orElse(null);
			if (thisVariable != null) {
				thisVariable.start = startLabel;
				thisVariable.end = endLabel;
			}

			int nextVarIndex = methodNode.maxLocals;
			int returnVariableIndex = nextVarIndex++;
			int holderVariableIndex = nextVarIndex++;
			LocalVariableNode returnVariable = new LocalVariableNode("$instance$", objectType.desc, null, startLabel, endLabel, returnVariableIndex);
			LocalVariableNode holderVariable = new LocalVariableNode("$instanceHolder$", instanceHolderType.desc, null, startLabel, endLabel,
					holderVariableIndex);

			localVariables.add(returnVariable);
			localVariables.add(holderVariable);

			InsnList instructions = methodNode.instructions;

			instructions.insert(startLabel);

			AbstractInsnNode insnNode = instructions.getLast();

			List<Integer> vars = new ArrayList<>();
			List<VarInsnNode> publishPoints = new ArrayList<>();
			List<AbstractInsnNode> returns = new ArrayList<>();
			List<MethodInsnNode> currentBeanCalls = new ArrayList<>();
			List<MethodInsnNode> currentInstanceCalls = new ArrayList<>();

			while (insnNode != null) {
				switch (insnNode.getOpcode()) {
					case ARETURN: {
						returns.add(insnNode);
						insnNode = insnNode.getPrevious();

						if (insnNode != null && insnNode.getOpcode() == ALOAD) {
							VarInsnNode varInsnNode = (VarInsnNode) insnNode;
							vars.add(varInsnNode.var);
						}
						break;
					}
					case ASTORE: {
						VarInsnNode varInsnNode = (VarInsnNode) insnNode;
						if (vars.contains(varInsnNode.var)) {
							publishPoints.add(varInsnNode);
						}
						break;
					}
					case INVOKESTATIC: {
						MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;

						if (currentInstanceMethod.matches(methodInsnNode)) {
							currentInstanceCalls.add(methodInsnNode);
						} else if (currentBeanMethod.matches(methodInsnNode)) {
							currentBeanCalls.add(methodInsnNode);
						}
					}
				}

				insnNode = insnNode.getPrevious();
			}

			String targetTypeInternalName = targetType.getInternalName();

			// initialize instanceHolder variable depending on context sensitivity
			InsnList beforeBlock = new InsnList();

			initializeHolderVariable(factoryMethod, parameters, holderVariableIndex, targetTypeInternalName, beforeBlock);

			Label tryFinaleLabel = new Label();
			LabelNode tryFinale = new LabelNode(tryFinaleLabel);

			// enhance return statements
			for (AbstractInsnNode returnNode : returns) {
				// check if the stack situation comes from a variable that consequently was published
				// or if it was established by other ways
				if (returnNode.getPrevious().getOpcode() == ALOAD) {
					// publish was done and holder will have the value already
					// thus stack will be simply consumed with no purpose
					InsnList gotoBlock = new InsnList();
					gotoBlock.add(new VarInsnNode(ALOAD, holderVariableIndex));
					gotoBlock.add(new InsnNode(SWAP));

					// store the finished instance in the returnVariable
					gotoBlock.add(new InsnNode(DUP)); // copy stack value which must be the instance and consume it with
														// an ASTORE
					gotoBlock.add(new VarInsnNode(ASTORE, returnVariable.index));

					// call postConstruct on InstanceHolder for notification
					gotoBlock.add(onPostConstructMethod.invoke());
					gotoBlock.add(new JumpInsnNode(GOTO, tryFinale));
					instructions.insert(returnNode, gotoBlock);
				} else {
					// publish was not done so far as no variable was used to be returned and therefore detected for
					// eager publish
					// thus publishing will be done here and stack will be consumed
					InsnList gotoBlock = new InsnList();
					gotoBlock.add(new InsnNode(DUP));
					// publish notification
					gotoBlock.add(new VarInsnNode(ALOAD, holderVariableIndex));
					gotoBlock.add(new InsnNode(SWAP));
					gotoBlock.add(publishMethod.invoke());
					// post construct notification
					gotoBlock.add(new VarInsnNode(ALOAD, holderVariableIndex));
					gotoBlock.add(new InsnNode(SWAP));

					// store the finished instance in the returnVariable
					gotoBlock.add(new InsnNode(DUP)); // copy stack value which must be the instance and consume it with
														// an ASTORE
					gotoBlock.add(new VarInsnNode(ASTORE, returnVariable.index));

					// call postConstruct on InstanceHolder for notification
					gotoBlock.add(onPostConstructMethod.invoke());
					gotoBlock.add(new JumpInsnNode(GOTO, tryFinale));
					instructions.insert(returnNode, gotoBlock);
				}

				instructions.remove(returnNode);
			}

			// insert publish calls where needed
			for (VarInsnNode publishPoint : publishPoints) {
				InsnList insertInstructions = new InsnList();
				insertInstructions.add(new VarInsnNode(ALOAD, holderVariableIndex));
				insertInstructions.add(new VarInsnNode(ALOAD, publishPoint.var));
				insertInstructions.add(publishMethod.invoke());
				instructions.insert(publishPoint, insertInstructions);
			}

			// replace InstanceConfiguration.currentInstance() calls by instanceHolder.config() calls
			for (MethodInsnNode currentInstanceCall : currentInstanceCalls) {
				writeCurrentInstance(holderVariableIndex, instructions, currentInstanceCall, false);
			}

			// replace legacy BeanConfigurations.currentBean() calls by beanHolder.config() calls
			for (MethodInsnNode currentBeanCall : currentBeanCalls) {
				writeCurrentInstance(holderVariableIndex, instructions, currentBeanCall, true);
			}

			Label beginTryLabel = new Label();
			Label beginCatchLabel = new Label();
			LabelNode beginTry = new LabelNode(beginTryLabel);
			LabelNode beginCatch = new LabelNode(beginCatchLabel);
			methodNode.tryCatchBlocks.add(new TryCatchBlockNode(beginTry, beginCatch, beginCatch, null));

			writeLockCreation(targetTypeInternalName, beforeBlock, holderVariableIndex);
			beforeBlock.add(new JumpInsnNode(IFNE, beginTry));
			beforeBlock.add(new VarInsnNode(ALOAD, holderVariableIndex));
			beforeBlock.add(getMethod.invoke());
			beforeBlock.add(new TypeInsnNode(CHECKCAST, Type.getMethodType(methodNode.desc).getReturnType().getInternalName()));
			beforeBlock.add(new InsnNode(ARETURN));

			beforeBlock.add(beginTry);

			InsnList afterBlock = new InsnList();

			// catch block
			afterBlock.add(beginCatch);
			writeOnCreationFailure(holderVariableIndex, afterBlock);
			writeUnlockCreation(targetTypeInternalName, afterBlock, holderVariableIndex);
			afterBlock.add(new InsnNode(ATHROW));

			// try finale
			afterBlock.add(tryFinale);

			writeUnlockCreation(targetTypeInternalName, afterBlock, holderVariableIndex);

			// ending block
			afterBlock.add(new VarInsnNode(ALOAD, returnVariable.index));
			afterBlock.add(new InsnNode(ARETURN));

			// enrich method with beforeBlock and afterBlock which care for locking
			methodNode.instructions.insert(startLabel, beforeBlock);
			methodNode.instructions.add(afterBlock);
			methodNode.instructions.add(endLabel);

			// ignore methodNode.maxStack as this will be recomputed by the ClassWriter
		}
	}

	private static void writeOnCreationFailure(int holderVariableIndex, InsnList afterBlock) {
		afterBlock.add(new InsnNode(DUP));
		afterBlock.add(new VarInsnNode(ALOAD, holderVariableIndex));
		afterBlock.add(new InsnNode(SWAP));
		afterBlock.add(onCreationFailureMethod.invoke());
	}

	private static void writeCurrentInstance(int holderVariableIndex, InsnList instructions, MethodInsnNode currentInstanceCall, boolean legacy) {
		InsnList insertInstructions = new InsnList();
		insertInstructions.add(new VarInsnNode(ALOAD, holderVariableIndex));

		insertInstructions.add(wovenCurrentInstanceMethod.invoke());

		// if legacy then adapt the type
		if (legacy) {
			insertInstructions.add(currentBeanAdaptMethod.invoke());
		}

		// replace placeholder instruction
		instructions.insert(currentInstanceCall, insertInstructions);
		instructions.remove(currentInstanceCall);
	}

	private static void initializeHolderVariable(ManagedInstanceFactoryMethod factoryMethod, Type parameterTypes[], int holderVariableIndex,
			String targetTypeInternalName, InsnList beforeBlock) {
		beforeBlock.add(new VarInsnNode(ALOAD, 0));
		beforeBlock.add(new FieldInsnNode(GETFIELD, targetTypeInternalName, factoryMethod.holderSupplierName, instanceHolderSupplierType.desc));

		switch (parameterTypes.length) {
			case 0:
				// null parameter based instance holder resolution
				beforeBlock.add(new InsnNode(ACONST_NULL));
				break;

			case 1:
				// single parameter based instance holder resolution
				loadObjectArg(parameterTypes[0], 1, beforeBlock);
				break;

			default:
				// multi parameter based instance holder resolution passed as ArrayList
				beforeBlock.add(new TypeInsnNode(NEW, arrayListType.internal));
				beforeBlock.add(new InsnNode(DUP));
				beforeBlock.add(new LdcInsnNode(parameterTypes.length));
				beforeBlock.add(arrayListConstructorMethod.invoke());

				/**
				 * list/list/element
				 */

				int contextParameterCount = parameterTypes.length;
				for (int i = 0; i < contextParameterCount; i++) {
					beforeBlock.add(new InsnNode(DUP));
					loadObjectArg(parameterTypes[i], i + 1, beforeBlock);
					beforeBlock.add(addMethod.invoke());
					beforeBlock.add(new InsnNode(POP));
				}

				break;
		}

		beforeBlock.add(getHolderMethod.invoke());
		beforeBlock.add(new VarInsnNode(ASTORE, holderVariableIndex));
	}

	private static void loadObjectArg(Type type, int i, InsnList target) {
		switch (type.getSort()) {
			case Type.BOOLEAN:
				target.add(new VarInsnNode(ILOAD, i));
				target.add(booleanBoxMethod.invoke());
				break;
			case Type.BYTE:
				target.add(new VarInsnNode(ILOAD, i));
				target.add(byteBoxMethod.invoke());
				break;
			case Type.SHORT:
				target.add(new VarInsnNode(ILOAD, i));
				target.add(shortBoxMethod.invoke());
				break;
			case Type.INT:
				target.add(new VarInsnNode(ILOAD, i));
				target.add(intBoxMethod.invoke());
				break;
			case Type.LONG:
				target.add(new VarInsnNode(LLOAD, i));
				target.add(longBoxMethod.invoke());
				break;
			case Type.FLOAT:
				target.add(new VarInsnNode(FLOAD, i));
				target.add(floatBoxMethod.invoke());
				break;
			case Type.DOUBLE:
				target.add(new VarInsnNode(DLOAD, i));
				target.add(doubleBoxMethod.invoke());
				break;
			case Type.CHAR:
				target.add(new VarInsnNode(ILOAD, i));
				target.add(charBoxMethod.invoke());
				break;
			default:
				target.add(new VarInsnNode(ALOAD, i));
				break;
		}
	}

	private static void writeLockCreation(String targetTypeInternalName, InsnList block, int holderVariableIndex) {
		pushLockCallParameters(targetTypeInternalName, block, holderVariableIndex);
		block.add(lockCreationMethod.invoke());
	}

	private static void pushLockCallParameters(String targetTypeInternalName, InsnList block, int holderVariableIndex) {
		block.add(new VarInsnNode(ALOAD, 0));
		block.add(new FieldInsnNode(GETFIELD, targetTypeInternalName, contextVarName, wireContextType.desc));
		block.add(new VarInsnNode(ALOAD, holderVariableIndex));
	}

	private static void writeUnlockCreation(String targetTypeInternalName, InsnList block, int holderVariableIndex) {
		pushLockCallParameters(targetTypeInternalName, block, holderVariableIndex);
		block.add(unlockCreationMethod.invoke());
	}

	private List<ManagedInstanceFactoryMethod> getFactoryMethods(ClassNode classNode, boolean runtime) {
		Type spaceDefaultScopeType = getScopeType(classNode);

		if (runtime) {
			if (classNode.fields != null) {
				classNode.fields.clear();
			}
		}

		if (classNode.methods != null) {
			List<ManagedInstanceFactoryMethod> factoryMethods = new ArrayList<>();
			UniqueNameFunction uniqueNameFunction = new UniqueNameFunction();
			for (Iterator<MethodNode> it = classNode.methods.iterator(); it.hasNext();) {
				MethodNode methodNode = it.next();
				// Ignore bridge methods that are created to covariant return types in contract and space code
				if ((methodNode.access & Opcodes.ACC_BRIDGE) != 0) {
					continue;
				}
				Type scopeType = getScopeType(methodNode);
				if (scopeType != null) {
					if (scopeType.getInternalName().equals(defaultScopeType.internal))
						scopeType = spaceDefaultScopeType;

					factoryMethods.add(new ManagedInstanceFactoryMethod(methodNode, scopeType, uniqueNameFunction));
				} else {
					if (runtime)
						it.remove();
				}
			}
			return factoryMethods;
		} else
			return Collections.emptyList();
	}

	private static void writeConstructor(ClassNode classNode, Type targetType, List<ManagedInstanceFactoryMethod> factoryMethods) {
		classNode.visitField(ACC_PRIVATE, contextVarName, wireContextType.desc, null, null);

		MethodVisitor cv = classNode.visitMethod(ACC_PUBLIC, wireSpaceConstructor.name(), wireSpaceConstructor.asmDesc(), null, null);
		cv.visitCode();

		cv.visitVarInsn(ALOAD, 0);
		if (classNode.superName.equals(objectType.internal))
			objectConstructor.invoke(cv);
		else {
			cv.visitVarInsn(ALOAD, 1);
			cv.visitMethodInsn(INVOKESPECIAL, classNode.superName, wireSpaceConstructor.name(), wireSpaceConstructor.asmDesc(), false);
		}

		// // calling default constructor
		// cv.visitVarInsn(ALOAD, 0);
		// cv.visitMethodInsn(INVOKESPECIAL, targetType.getInternalName(), "<init>", "()V", false);

		// storing passed context into member "$context$"
		cv.visitVarInsn(ALOAD, 0);
		// cv.visitInsn(DUP);
		cv.visitVarInsn(ALOAD, 1);
		cv.visitFieldInsn(PUTFIELD, targetType.getInternalName(), contextVarName, wireContextType.desc);

		for (ManagedInstanceFactoryMethod factoryMethod : factoryMethods) {
			String holderSupplierName = factoryMethod.holderSupplierName;
			classNode.visitField(ACC_PRIVATE, holderSupplierName, instanceHolderSupplierType.desc, null, null);

			cv.visitVarInsn(ALOAD, 1);
			cv.visitLdcInsn(factoryMethod.scopeType);
			getScopeMethod.invoke(cv);
			cv.visitVarInsn(ASTORE, 2);

			cv.visitVarInsn(ALOAD, 0);
			cv.visitInsn(DUP);
			cv.visitVarInsn(ALOAD, 2);
			cv.visitInsn(SWAP);
			cv.visitLdcInsn(factoryMethod.methodNode.name);

			cv.visitFieldInsn(GETSTATIC, instanceParameterizationType.internal, factoryMethod.parameterization.name(),
					instanceParameterizationType.desc);
			createHolderSupplierMethod.invoke(cv);
			cv.visitFieldInsn(PUTFIELD, targetType.getInternalName(), holderSupplierName, instanceHolderSupplierType.desc);
		}

		cv.visitInsn(RETURN);
		cv.visitMaxs(3, 3);
		cv.visitEnd();
	}

	private class ManagedInstanceFactoryMethod {
		public MethodNode methodNode;
		public String holderSupplierName;
		public Type scopeType;
		public Type[] parameters;
		public InstanceParameterization parameterization;

		public ManagedInstanceFactoryMethod(MethodNode methodNode, Type scopeType, Function<String, String> nameMapper) {
			this.methodNode = methodNode;
			this.holderSupplierName = nameMapper.apply('$' + methodNode.name);
			this.scopeType = scopeType;

			Type[] parameters = Type.getArgumentTypes(methodNode.desc);

			switch (parameters.length) {
				case 0:
					parameterization = InstanceParameterization.none;
					break;

				case 1:
					parameterization = isScopeContext(parameters[0]) ? InstanceParameterization.context : InstanceParameterization.params;
					break;

				default:
					parameterization = InstanceParameterization.params;
					break;
			}
			this.parameters = parameters;
		}

		private boolean isScopeContext(Type type) {
			try {
				Class<?> clazz = Class.forName(type.getClassName(), false, spaceClassLoader);

				return ScopeContext.class.isAssignableFrom(clazz);
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("managed instance parameter type [" + type.getClassName() + "] not found for " + methodNode, e);
			}
		}
	}

	private class WireEnricherClassLoader extends ClassLoader {
		private final Predicate<String> classNameFilter;

		public WireEnricherClassLoader(Predicate<String> classNameFilter) {
			super(spaceClassLoader);
			this.classNameFilter = classNameFilter;
		}

		@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			synchronized (getClassLoadingLock(name)) {
				if (classNameFilter.test(name)) {
					Class<?> loadedClass = findLoadedClass(name);

					if (loadedClass == null) {
						byte[] classBytes = loadAndEnsureEnriched(name);

						try {
							loadedClass = defineClass(name, classBytes, 0, classBytes.length);
							loadedClass.getConstructors();
						} catch (LinkageError e) {
							// dump information about wrong class definition
							File dumpFile = dumpBytecode(name, classBytes);
							logger.log(Level.SEVERE, "Error in class definition for class [" + name + "]. Dumped class data to "
									+ (dumpFile != null ? dumpFile.getAbsolutePath() : "void"), e);
							throw e;
						}
					}

					ensurePackage(loadedClass);

					if (resolve)
						resolveClass(loadedClass);

					return loadedClass;
				} else
					return super.loadClass(name, resolve);
			}
		}

		/* private boolean isInnerClass(String name) { int index = name.lastIndexOf('.');
		 * 
		 * String simpleClassName = null;
		 * 
		 * if (index != -1) { simpleClassName = name.substring(index + 1); } else { simpleClassName = name; }
		 * 
		 * return simpleClassName.indexOf('$') != -1; } */

		private byte[] loadAndEnsureEnriched(String name) {
			try (InputStream in = getClassData(name).openStream()) {
				byte[] spaceClass = loadSpaceClass(name, in);

				/* OutputStream out = new FileOutputStream(new File(name + ".class")); out.write(spaceClass); out.close(); */

				return spaceClass;
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "could not load or enrich " + name, IllegalStateException::new);
			}
		}

		private File dumpBytecode(String name, byte[] classBytes) {
			try {
				File file = Files.createTempFile(name + "-", ".class").toFile();
				try (FileOutputStream out = new FileOutputStream(file)) {
					out.write(classBytes);
				}
				return file;
			} catch (IOException e1) {
				logger.log(Level.SEVERE, "Error while dumping class data for analysis when handling an error", e1);
				return null;
			}
		}

		private void ensurePackage(Class<?> loadedClass) {
			if (loadedClass.getPackage() != null)
				return;

			String name = loadedClass.getName();
			int i = name.lastIndexOf('.');
			if (i == -1)
				return;

			String packageName = name.substring(0, i);
			definePackage(packageName, null, null, "Pharao's curse", null, null, "Enjoy!", null);
		}

	}

	private static Type getScope(List<AnnotationNode> annotations) {
		if (annotations == null)
			return null;

		for (AnnotationNode annotationNode : annotations) {
			if (annotationNode.desc.equals(managedType.desc)) {
				List<Object> values = annotationNode.values;

				if (values != null) {
					int size = values.size();
					for (int i = 0; i < size;) {
						String name = (String) values.get(i++);
						if (name.equals("value")) {
							String enumQualification[] = (String[]) values.get(i);
							String constantName = enumQualification[1];
							Scope scope = Scope.valueOf(constantName);

							switch (scope) {
								case inherit:
									return defaultScopeType.type;
								case singleton:
									return singletonScopeType.type;
								case prototype:
									return prototypeScopeType.type;
								case aggregate:
									return aggregateScopeType.type;
								case caller:
									return callerScopeType.type;
								default:
									throw new IllegalStateException("unsupported scope type " + scope);
							}
						}
						i++;
					}
				}

				return defaultScopeType.type;
			}
		}

		return null;
	}

	private static Type getScopeType(ClassNode classNode) {
		List<AnnotationNode> visibleAnnotations = classNode.visibleAnnotations;
		Type scopeType = getScope(visibleAnnotations);

		if (scopeType == null) {
			// fallback to legacy annotation support
			scopeType = scanScopeAnnotation(visibleAnnotations, beansType.type, "defaultScope");
		}

		return scopeType != null ? scopeType : defaultScopeType.type;
	}

	private static Type getScopeType(MethodNode methodNode) {
		List<AnnotationNode> visibleAnnotations = methodNode.visibleAnnotations;
		Type scopeType = getScope(visibleAnnotations);

		if (scopeType == null) {
			// fallback to legacy annotation support
			scopeType = scanScopeAnnotation(visibleAnnotations, beanType.type, "scope");
		}

		return scopeType;
	}

	private static Type scanScopeAnnotation(List<AnnotationNode> visibleAnnotations, Type scopingAnnotationType, String scopeProperty) {
		if (visibleAnnotations == null)
			return null;

		for (AnnotationNode annotationNode : visibleAnnotations) {
			if (annotationNode.desc.equals(scopingAnnotationType.getDescriptor())) {
				List<Object> values = annotationNode.values;

				if (values != null) {
					int size = values.size();
					for (int i = 0; i < size;) {
						String name = (String) values.get(i++);
						if (name.equals(scopeProperty)) {
							Type type = (Type) values.get(i);
							return type;
						}
						i++;
					}
				}

				return defaultScopeType.type;

			}
		}

		return null;
	}

	private static class UniqueNameFunction implements Function<String, String> {
		private final Map<String, Integer> names = new HashMap<>();

		@Override
		public String apply(String name) {

			int num = names.compute(name, (k, v) -> v == null ? 1 : v.intValue() + 1);
			return num == 1 ? name : name + "-" + num;
		}

	}
}
