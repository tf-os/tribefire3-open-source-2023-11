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

import java.io.InputStream;
import java.io.PrintWriter;

import com.braintribe.asm.ClassReader;
import com.braintribe.asm.ClassVisitor;
import com.braintribe.asm.Opcodes;
import com.braintribe.asm.util.ASMifier;
import com.braintribe.asm.util.Textifier;
import com.braintribe.asm.util.TraceClassVisitor;



public class ProtoOutput implements Opcodes {
	public static void main(String[] args) {
	
		try {
			InputStream inputStream = Space1Enriched.class.getResourceAsStream(Space1Enriched.class.getSimpleName() + ".class");

			ClassReader reader = new ClassReader(inputStream);

			ASMifier asMifier = new ASMifier();
			Textifier textifier = new Textifier();
			ClassVisitor visitor = new TraceClassVisitor(null, textifier, new PrintWriter(System.out));
			reader.accept(visitor, ClassReader.EXPAND_FRAMES);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
