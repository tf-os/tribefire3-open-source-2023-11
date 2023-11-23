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
package com.braintribe.gwt.tribefirejs.linker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.Shardable;
import com.google.gwt.core.ext.linker.Transferable;
import com.google.gwt.core.ext.linker.impl.SelectionScriptLinker;
import com.google.gwt.dev.util.DefaultTextOutput;

/**
 * A Linker for producing a single JavaScript file from a GWT module. The use of
 * this Linker requires that the module has exactly one distinct compilation
 * result.
 */
@LinkerOrder(Order.PRIMARY)
@Shardable
public class TribefireJsSingleScriptLinker extends SelectionScriptLinker {
	
	public TribefireJsSingleScriptLinker() {
	}
	
	@Override
	public String getDescription() {
		return "TribefireJs Single Script";
	}

	@SuppressWarnings("serial")
	@Transferable
	private static class Script extends Artifact<Script> {
		private final String javaScript;
		private final String strongName = "tribefire.js";

		@SuppressWarnings("unused")
		public Script(String strongName, String javaScript) {
			super(TribefireJsSingleScriptLinker.class);
//			this.strongName = strongName;
			this.javaScript = javaScript;
		}

		@Override
		public int compareToComparableArtifact(Script that) {
			int res = strongName.compareTo(that.strongName);
			if (res == 0) {
				res = javaScript.compareTo(that.javaScript);
			}
			return res;
		}

		@Override
		public Class<Script> getComparableArtifactType() {
			return Script.class;
		}

		public String getJavaScript() {
			return javaScript;
		}

		@SuppressWarnings("unused")
		public String getStrongName() {
			return strongName;
		}

		@Override
		public int hashCode() {
			return strongName.hashCode() ^ javaScript.hashCode();
		}

		@Override
		public String toString() {
			return "Script " + strongName;
		}
	}

	@Override
	protected Collection<Artifact<?>> doEmitCompilation(TreeLogger logger, LinkerContext context,
			CompilationResult result, ArtifactSet artifacts) throws UnableToCompleteException {

		String[] js = result.getJavaScript();

		Collection<Artifact<?>> toReturn = new ArrayList<Artifact<?>>();
		toReturn.add(new Script(result.getStrongName(), js[0]));
		toReturn.addAll(emitSelectionInformation(result.getStrongName(), result));
		return toReturn;
	}

	@Override
	protected EmittedArtifact emitSelectionScript(TreeLogger logger, LinkerContext context, ArtifactSet artifacts)
			throws UnableToCompleteException {

		Set<Script> results = artifacts.find(Script.class);
		Script result = results.iterator().next();		

		DefaultTextOutput out = new DefaultTextOutput(true);
		
		String script = generateScript(logger, context, artifacts);

		script = script.replace("{tf-script}", result.getJavaScript());
		
		out.print(script);

		return emitString(logger, out.toString(), context.getModuleName() + ".nocache.js");
	}
	
	protected String generateScript(TreeLogger logger, LinkerContext context, ArtifactSet artifacts) throws UnableToCompleteException {
		    String selectionScriptText;
		    StringBuffer buffer = readFileToStringBuffer(getSelectionScriptTemplate(logger, context), logger);
		    selectionScriptText = fillSelectionScriptTemplate(buffer, logger, context, artifacts, null);
//		    selectionScriptText = context.optimizeJavaScript(logger, selectionScriptText);
		    return selectionScriptText;
		  }
	
	/**
	 * Unimplemented. Normally required by
	 * {@link #doEmitCompilation(TreeLogger, LinkerContext, CompilationResult, ArtifactSet)}.
	 */
	@Override
	protected String getCompilationExtension(TreeLogger logger, LinkerContext context)
			throws UnableToCompleteException {
		throw new UnableToCompleteException();
	}

	/**
	 * Unimplemented. Normally required by
	 * {@link #doEmitCompilation(TreeLogger, LinkerContext, CompilationResult, ArtifactSet)}.
	 */
	@Override
	protected String getModulePrefix(TreeLogger logger, LinkerContext context, String strongName)
			throws UnableToCompleteException {
		throw new UnableToCompleteException();
	}

	@Override
	protected String getSelectionScriptTemplate(TreeLogger logger, LinkerContext context)
			throws UnableToCompleteException {
		return "com/braintribe/gwt/tribefirejs/linker/tribefire.js";
	}
}
