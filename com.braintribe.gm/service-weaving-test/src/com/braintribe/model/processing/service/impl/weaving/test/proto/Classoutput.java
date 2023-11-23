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
package com.braintribe.model.processing.service.impl.weaving.test.proto;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import com.braintribe.asm.Opcodes;

/*
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.util.ASMifier;
import jdk.internal.org.objectweb.asm.util.TraceClassVisitor;
*/

public class Classoutput implements Opcodes {
/*	public static void main(String[] args) {
		
		try {
			Class clazz = OpenUserSessionRequestServiceProcessor.class;
			InputStream inputStream = clazz.getResourceAsStream(clazz.getSimpleName() + ".class");

			ClassReader reader = new ClassReader(inputStream);

			ASMifier asMifier = new ASMifier();
			ClassVisitor visitor = new TraceClassVisitor(null, asMifier, new PrintWriter(System.out));
			reader.accept(visitor, ClassReader.EXPAND_FRAMES);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
