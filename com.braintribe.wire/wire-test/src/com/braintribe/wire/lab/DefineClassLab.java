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
package com.braintribe.wire.lab;

import java.lang.reflect.Field;

import com.braintribe.asm.AnnotationVisitor;
import com.braintribe.asm.ClassWriter;
import com.braintribe.asm.FieldVisitor;
import com.braintribe.asm.Label;
import com.braintribe.asm.MethodVisitor;
import com.braintribe.asm.Opcodes;

public class DefineClassLab implements Opcodes {
	private static class ExtraClassLoader extends ClassLoader {
		
		public ExtraClassLoader(ClassLoader parent) {
			super(parent);
			// TODO Auto-generated constructor stub
		}

		public Class<?> deploy(String name, byte[] data) {
			return defineClass(name, data, 0, data.length);
		}
	}
	public static void main(String[] args) {
		try {
			byte[] aClassData = buildA();
			byte[] bClassData = buildB();
			
			ExtraClassLoader classLoader = new ExtraClassLoader(DefineClassLab.class.getClassLoader());
			
			Class<?> aClass = classLoader.deploy("com.braintribe.wire.lab.A", aClassData);
			Class<?> bClass = classLoader.deploy("com.braintribe.wire.lab.B", bClassData);
			
			Field field = aClass.getField("b");
			
			System.out.println(field);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static byte[] buildA() {
		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(52, ACC_PUBLIC + ACC_SUPER, "com/braintribe/wire/lab/A", null, "java/lang/Object", null);

		cw.visitSource("A.java", null);

		{
		fv = cw.visitField(ACC_PUBLIC, "b", "Lcom/braintribe/wire/lab/B;", null, null);
		fv.visitEnd();
		}
		{
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(3, l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitInsn(RETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", "Lcom/braintribe/wire/lab/A;", null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
	
	public static byte[] buildB() {
		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;
		
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, "com/braintribe/wire/lab/B", null, "java/lang/Object", null);
		
		cw.visitSource("A.java", null);
		
		{
			fv = cw.visitField(ACC_PUBLIC, "a", "Lcom/braintribe/wire/lab/A;", null, null);
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(3, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitInsn(RETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "Lcom/braintribe/wire/lab/B;", null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		cw.visitEnd();
		
		return cw.toByteArray();
	}
}
