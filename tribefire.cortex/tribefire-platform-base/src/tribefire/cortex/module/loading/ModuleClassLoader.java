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
package tribefire.cortex.module.loading;

import java.lang.reflect.Constructor;
import java.net.URL;

import com.braintribe.asm.ClassWriter;
import com.braintribe.asm.Label;
import com.braintribe.asm.MethodVisitor;
import com.braintribe.asm.Opcodes;
import com.braintribe.logging.Logger;
import com.braintribe.utils.classloader.ReverseOrderURLClassLoader;

import tribefire.descriptor.model.ModuleDescriptor;
import tribefire.descriptor.model.ModulesDescriptor;

/**
 * ClassLoader for a module, which also holds a {@link ModulesDescriptor}.
 * 
 * @author peter.gazdik
 */
public class ModuleClassLoader extends ReverseOrderURLClassLoader {

	private final ModuleDescriptor moduleDescriptor;

	protected ModuleClassLoader(URL[] urls, ClassLoader parent, ModuleDescriptor moduleDescriptor) {
		super(urls, parent);
		this.moduleDescriptor = moduleDescriptor;
	}

	public ModuleDescriptor getModuleDescriptor() {
		return moduleDescriptor;
	}

	public String getModuleName() {
		return moduleDescriptor.getPath();
	}

	@Override
	public String toString() {
		return "ModuleClassLoader:" + moduleDescriptor.name();
	}

	public static ModuleClassLoader create(URL[] urls, ClassLoader parent, ModuleDescriptor moduleDescriptor) {
		return ModuleClassLoaderWeaver.create(urls, parent, moduleDescriptor);
	}

	/**
	 * This creates a custom ClassLoader for given module, something like:
	 * {@code public class ModuleClassLoader--my-module--my-group extends ModuleClassLoader }
	 * 
	 * The reason is that certain type of {@link LinkageError}s only mention {@link ClassLoader} type, but do not call toString(), so it's difficult
	 * to find out which module makes problems.
	 * <p>
	 * Example: <tt>
	 * loader constraint violation: when resolving method "org.slf4j.impl.StaticLoggerBinder.getLoggerFactory()Lorg/slf4j/ILoggerFactory;" the class
	 * loader (instance of tribefire/cortex/module/loading/ModuleClassLoader) of the current class, org/slf4j/LoggerFactory, and the class loader
	 * (instance of org/apache/catalina/loader/ParallelWebappClassLoader) for the method's defining class, org/slf4j/impl/StaticLoggerBinder, have
	 * different Class objects for the type org/slf4j/ILoggerFactory used in the signature
	 * </tt>
	 */
	private static class ModuleClassLoaderWeaver implements Opcodes {

		private static final Logger log = Logger.getLogger(ModuleClassLoaderWeaver.class);

		public static ModuleClassLoader create(URL[] urls, ClassLoader parent, ModuleDescriptor md) {
			try {
				String suffix = ("--" + md.getArtifactId() + "--" + md.getGroupId()).replace(".", "-");

				String binaryName = ModuleClassLoader.class.getName() + suffix;
				String internalName = ModuleClassLoader.class.getName().replace(".", "/") + suffix;

				byte[] classBytes = createCustomMclImplBytes(internalName);

				Class<? extends ModuleClassLoader> clazz = new BytecodeLoader() //
						.doYourThing(binaryName, classBytes) //
						.asSubclass(ModuleClassLoader.class);

				Constructor<?> constructor = clazz.getConstructors()[0];

				return (ModuleClassLoader) constructor.newInstance(urls, parent, md);

			} catch (Throwable t) {
				log.warn("Failed to create a custom ModuleClassLoader class for module: " + md.name() + ". Returning ModuleClassLoader directly. "
						+ "As a consequence it might be more difficult to troubleshoot a certain type of LinkageErrors", t);

				return new ModuleClassLoader(urls, parent, md);
			}
		}

		private static byte[] createCustomMclImplBytes(String internalName) {
			ClassWriter classWriter = new ClassWriter(0);

			final String superInternalName = "tribefire/cortex/module/loading/ModuleClassLoader";
			final String constructorSignature = "([Ljava/net/URL;Ljava/lang/ClassLoader;Ltribefire/descriptor/model/ModuleDescriptor;)V";

			classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, internalName, null, superInternalName, null);

			{
				MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", constructorSignature, null, null);
				mv.visitCode();
				Label label0 = new Label();
				mv.visitLabel(label0);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitVarInsn(ALOAD, 2);
				mv.visitVarInsn(ALOAD, 3);
				mv.visitMethodInsn(INVOKESPECIAL, superInternalName, "<init>", constructorSignature, false);
				Label label1 = new Label();
				mv.visitLabel(label1);
				mv.visitInsn(RETURN);
				Label label2 = new Label();
				mv.visitLabel(label2);
				mv.visitMaxs(4, 4);
				mv.visitEnd();
			}

			classWriter.visitEnd();

			return classWriter.toByteArray();
		}

	}

	private static class BytecodeLoader extends ClassLoader {
		public BytecodeLoader() {
			super(ModuleClassLoader.class.getClassLoader());
		}

		public Class<?> doYourThing(String name, byte[] bytes) {
			return super.defineClass(name, bytes, 0, bytes.length);
		}
	}

}
