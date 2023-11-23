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
package com.braintribe.doc.lunr;

import java.io.File;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.braintribe.utils.IOTools;

public class LunrIndexGeneration {
	public static void main(String[] args) {
		try {
			ScriptEngineManager factory = new ScriptEngineManager();
			ScriptEngine engine = factory.getEngineByName("JavaScript");
			String script = IOTools.slurp(new File("markdown/sub/lunr.js"), "UTF-8");
			String index = IOTools.slurp(new File("markdown/sub/index.js"), "UTF-8");
			
			engine.eval(index);
			engine.eval(script);
			engine.eval("serIdx = JSON.stringify(lunr(function () { this.ref('id'); this.field('body'); idxContent.forEach(function (doc) { this.add(doc) }, this) }));");
			Object x = engine.get("serIdx");
			System.out.println(x);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
