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
package tribefire.cortex.manipulation.conversion;

import static com.braintribe.utils.SysPrint.spOut;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.manipulation.parser.api.MutableGmmlParserConfiguration;
import com.braintribe.model.processing.manipulation.parser.impl.Gmml;
import com.braintribe.model.processing.manipulation.parser.impl.ManipulationParser;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.NullSafe;

import tribefire.cortex.manipulation.conversion.beans.EntityBean;
import tribefire.cortex.manipulation.conversion.code.CodeWriterParams;
import tribefire.cortex.manipulation.conversion.code.InitializerCodeWriter;
import tribefire.cortex.manipulation.conversion.code.InitializerSources;
import tribefire.cortex.manipulation.conversion.code.JscPool;
import tribefire.cortex.sourcewriter.JavaSourceClass;

/**
 * @author peter.gazdik
 */
public class GmmlToInitializerMain {

	public static final String[] initializers = { //
			"workbench", //

			// Processed
			// "audit-wb", //
			// "auth", //
			// "auth-wb", //
			// "cortex", //
			// "cortex-wb", //
			// "setup-wb", //
			// "user-sessions-wb", //
			// "user-statistics-wb", //
	};

	// ##########################################
	// ## . . . . . . . . Main . . . . . . . . ##
	// ##########################################

	public static void main(String[] args) {
		new GmmlToInitializerMain().run();
	}

	// ##########################################

	private void run() {
		for (String base : initializers)
			run(base);
	}

	private void run(String base) {
		spOut(base);

		InitContext context = new InitContext(base);
		CodeWriterParams params = newParams(context, base);
		InitializerSources sources = InitializerCodeWriter.writeInitializer(params);

		FileTools.write(context.outInitializerFile).string(sources.initializerSpace);
		FileTools.write(context.outLookupFile).string(sources.lookupContract);

		spOut("\tInitializer: " + printClassFile(context.outInitializerFile));
		spOut("\tLookup: " + context.outLookupFile.getAbsolutePath());
	}

	private CodeWriterParams newParams(InitContext context, String base) {
		Manipulation manipulation = parse(context);

		CodeWriterParams params = new CodeWriterParams();
		params.initializerPackage = context.initializerPackage;
		params.spacePrefix = context.spacePrefix;
		params.manipulation = manipulation;

		if (base.endsWith("-wb") || base.equals("workbench"))
			params.allowedRootTypeFilter = bean -> isWbRootBean(bean, params.jscPool);

		return params;
	}

	private boolean isWbRootBean(EntityBean<?> bean, JscPool jscPool) {
		JavaSourceClass jsc = bean.jsc;
		return jsc == jscPool.folderJsc || jsc == jscPool.wbPerscpectiveJsc;
	}

	private String printClassFile(File f) {
		return f.getParentFile().getAbsolutePath() + "\n\t\t(" + f.getName() + ":20)";
	}

	private Manipulation parse(InitContext context) {
		MutableGmmlParserConfiguration config = Gmml.configuration();
		config.setParseSingleBlock(true);

		spOut("\tParsing: " + context.input.getName());
		try (InputStream in = new FileInputStream(context.input)) {
			return ManipulationParser.parse(in, "UTF-8", config);

		} catch (IOException e) {
			throw new RuntimeException("Could not read input file: " + context.input.getAbsolutePath(), e);
		}
	}

	private static class InitContext {
		public final String initializerPackage;
		public final String spacePrefix;

		public final File input;

		public final File outInitializerFile;
		public final File outLookupFile;

		public InitContext(String base) {
			String baseSnake = base.replace("-", "_");
			String basePascal = toPascalCase(base, "-");

			this.initializerPackage = "tribefire.cortex.assets." + baseSnake + ".initializer.wire";
			this.spacePrefix = basePascal;

			this.input = new File("input/" + base + "-initial-priming.data.man");

			File folder = new File("C:/Peter/Work/git/tribefire.cortex.assets/" + base + "-initializer/src/tribefire/cortex/assets/" + baseSnake
					+ "/initializer/wire");

			this.outInitializerFile = new File(folder, "space/" + spacePrefix + "InitializerSpace.java");
			this.outLookupFile = new File(folder, "contract/" + spacePrefix + "LookupContract.java");
		}
	}

	/**
	 * Transforms the provided string to pascal case. <br>
	 * <br>
	 * 
	 * E.g: foo-bar -> FooBar (when delimiter is specified as '-')
	 */
	public static String toPascalCase(String originalString, String delimiterRegex) {
		NullSafe.nonNull(originalString, "value");
		NullSafe.nonNull(delimiterRegex, "delimiter");

		return Stream.of(originalString.split(delimiterRegex)) //
				.map(StringTools::capitalize) //
				.collect(Collectors.joining(""));

	}

}
