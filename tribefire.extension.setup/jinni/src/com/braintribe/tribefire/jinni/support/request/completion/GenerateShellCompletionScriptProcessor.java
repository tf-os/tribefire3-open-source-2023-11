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
package com.braintribe.tribefire.jinni.support.request.completion;

import static com.braintribe.tribefire.jinni.core.JinniModelAccessoryFactory.USE_CASE_EXECUTION;
import static com.braintribe.tribefire.jinni.core.JinniModelAccessoryFactory.USE_CASE_HELP;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.newTreeMap;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.cfg.Required;
import com.braintribe.freemarker.FreemarkerRenderer;
import com.braintribe.model.generic.tools.BasicStringifier;
import com.braintribe.model.jinni.api.CliCompletionStrategy;
import com.braintribe.model.jinni.api.GenerateShellCompletionScript;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.tribefire.jinni.support.JinniCommandsReflection;
import com.braintribe.tribefire.jinni.support.JinniCommandsReflection.JinniCommandsOverview;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.NullSafe;
import com.braintribe.utils.lcd.StringTools;

/**
 * @author peter.gazdik
 */
public class GenerateShellCompletionScriptProcessor implements ServiceProcessor<GenerateShellCompletionScript, Neutral> {

	private ModelOracle modelOracle;
	private ModelMdResolver mdResolver;
	private JinniCommandsReflection jinniCommandsReflection;
	private JinniCommandsOverview commandsOverview;
	private Comparator<GmEntityType> entityComparator_StandardAlias;

	private ModelMdResolver helpMdResolver;
	private JinniCommandsReflection helpJinniCommandsReflection;

	@Required
	public void setModelAccessory(ModelAccessory modelAccessory) {
		this.modelOracle = modelAccessory.getOracle();
		this.mdResolver = modelAccessory.getMetaData().useCases(USE_CASE_EXECUTION);
		this.jinniCommandsReflection = new JinniCommandsReflection(modelOracle, mdResolver);
		this.entityComparator_StandardAlias = Comparator.comparing(jinniCommandsReflection::resolveStandardAlias);

		this.helpMdResolver = modelAccessory.getMetaData().useCases(USE_CASE_EXECUTION, USE_CASE_HELP);
		this.helpJinniCommandsReflection = new JinniCommandsReflection(modelOracle, helpMdResolver);

		this.commandsOverview = jinniCommandsReflection.getCommandsOverview();
	}

	@Override
	public Neutral process(ServiceRequestContext requestContext, GenerateShellCompletionScript request) {
		File outputFile = new File(request.getFile().getPath());

		FileTools.write(outputFile).usingWriter(writer -> {
			FreemarkerRenderer.loadingViaClassLoader(getClass()) //
					.renderTemplate("jinni-completion.sh.ftl", freemarkerParams(request), writer);
		});

		return Neutral.NEUTRAL;
	}

	private Map<String, Object> freemarkerParams(GenerateShellCompletionScript request) {
		ShellCompletionGenerator generator = new ShellCompletionGenerator(request);

		return asMap(//
				"commandsList", generator.suggestedCommands(), //
				"resolveParameterTypeIfRelevant_Case", generator.parameterTypeIfRelevant_Case(), //
				"suggestParameterName_Case", generator.suggestParameterName_Case(), //
				"suggestHelp_CommandsList", generator.suggestHelp_Body(), //
				"suggestParameterValue_CustomCases", generator.suggestParameterValue_CustomCases() //
		);
	}

	private class ShellCompletionGenerator {

		private final GenerateShellCompletionScript request;
		private final EnumsRegistry enumsRegistry;

		private BasicStringifier stringifier;

		public ShellCompletionGenerator(GenerateShellCompletionScript request) {
			this.request = request;
			this.enumsRegistry = new EnumsRegistry();
		}

		private String suggestedCommands() {
			return printSuggestCommandsOverview(commandsOverview);
		}

		/**
		 * <pre>
		 * 	case $commandName in
		 * 		create-model)
		 * 			if [[ "$parameterName" =~ ^(--gwtSupport|--gwt|--overwrite|-o)$ ]]; then valueType="boolean"; return; fi
		 * 			if [[ "$parameterName" =~ ^(--file)$ ]]; then valueType="file"; return; fi
		 * 			if [[ "$parameterName" =~ ^(--dirList)$ ]]; then valueType="folder"; collectionType="list"; return; fi
		 * 			if [[ "$parameterName" =~ ^(--dirSet)$ ]]; then valueType="folder"; collectionType="set"; return; fi
		 * 			if [[ "$parameterName" =~ ^(--dirMap)$ ]]; then keyType="folder"; valueType="boolean"; collectionType="map"; return; fi
		 * 			;;
		 * 		...
		 * 	esac
		 * </pre>
		 */
		private String parameterTypeIfRelevant_Case() {
			Map<GmEntityType, Map<KnownType, List<String>>> commandToKnownTypeParams = resolveKnownTypeParams();

			stringifier = new BasicStringifier(new StringBuilder(), "\t", "\t");

			if (!commandToKnownTypeParams.isEmpty()) {
				stringifier.println("case $commandName in");
				stringifier.levelUp();

				for (Entry<GmEntityType, Map<KnownType, List<java.lang.String>>> e : commandToKnownTypeParams.entrySet()) {
					Map<KnownType, List<String>> typeToParamNames = e.getValue();

					// create-model)
					startCommandCaseWithAllAliases(e.getKey());

					for (Entry<KnownType, List<String>> e2 : typeToParamNames.entrySet())
						printSetParameterTypeForTheseParams(e2.getKey(), e2.getValue());

					endCase();
				}

				stringifier.levelDown();
				stringifier.print("esac");
			}

			return stringifier.appendable().toString();
		}

		private Map<GmEntityType, Map<KnownType, List<String>>> resolveKnownTypeParams() {
			Map<GmEntityType, Map<KnownType, List<String>>> result = newTreeMap(entityComparator_StandardAlias);

			addKnownTypes(commandsOverview.allTypes, result);

			return result;
		}

		private void addKnownTypes(List<GmEntityType> types, Map<GmEntityType, Map<KnownType, List<String>>> result) {
			for (GmEntityType gmType : types) {
				Map<KnownType, List<String>> knownParams = resolveKnownTypeParamsFor(gmType);
				knownParams.remove(KnownType.IGNORED_TYPE);

				if (!knownParams.isEmpty())
					result.put(gmType, knownParams);
			}
		}

		private Map<KnownType, List<String>> resolveKnownTypeParamsFor(GmEntityType requestType) {
			// entityType -> properties -> Map<knownTypeName, List<propertyNameAndAliases>>

			return jinniCommandsReflection.getRelevantPropertiesOf(requestType).stream() //
					.collect( //
							Collectors.groupingBy( //
									this::resolveKnownTypeOf, //
									flatMapping( //
											jinniCommandsReflection::cliNameAndAliasesOf, //
											Collectors.toList() //
									) //
							) //
					);
		}

		private KnownType resolveKnownTypeOf(GmProperty p) {
			GmType type = p.getType();
			PropertyMdResolver propertyMdResolver = jinniCommandsReflection.mdResolver.property(p);

			return KnownType.resolveKnownType(type, propertyMdResolver, enumsRegistry);
		}

		private void printSetParameterTypeForTheseParams(KnownType knownType, List<String> params) {
			String regextMatchingParams = StringTools.join("|", params);
			stringifier.println(
					"if [[ \"$parameterName\" =~ ^(" + regextMatchingParams + ")$ ]]; then" + variableAssignmentsFor(knownType) + " return; fi");
		}

		private String variableAssignmentsFor(KnownType kt) {
			String result = "";

			if (!StringTools.isEmpty(kt.keyType))
				result += " keyType=\"" + kt.keyType + "\";";

			if (!StringTools.isEmpty(kt.valueType))
				result += " valueType=\"" + kt.valueType + "\";";

			if (!StringTools.isEmpty(kt.collectionType))
				result += " collectionType=\"" + kt.collectionType + "\";";

			return result;
		}

		/**
		 * <pre>
		 * __suggestParameterName() {
		 * case $commandName in
		 * create-model)
		 * __suggest "--artifactId --groupId --gwt";;
		 * ...
		 * esac
		 * }
		 * </pre>
		 */
		private String suggestParameterName_Case() {
			stringifier = new BasicStringifier(new StringBuilder(), "\t", "\t");

			stringifier.println("case $commandName in");
			stringifier.levelUp();

			writeParameterNameSuggestionsForStandardCommands();

			stringifier.levelDown();
			stringifier.print("esac");

			return stringifier.appendable().toString();
		}

		private void writeParameterNameSuggestionsForStandardCommands() {
			Map<GmEntityType, List<String>> commandToParamNames = resolveCommandsWithParamNames();

			for (Entry<GmEntityType, List<String>> e : commandToParamNames.entrySet())
				writeParameterNameSuggestionsFor(e);
		}

		private void writeParameterNameSuggestionsFor(Entry<GmEntityType, List<String>> e) {
			List<String> paramNames = e.getValue();

			if (paramNames.isEmpty())
				return;

			startCommandCaseWithAllAliases(e.getKey());
			endCaseWithSuggestValues(paramNames);
		}

		private void startCommandCaseWithAllAliases(GmEntityType gmType) {
			String commandCase = jinniCommandsReflection.nameAndAliasesSorted(gmType).collect(Collectors.joining("|"));
			startCase(commandCase);
		}

		private Map<GmEntityType, List<String>> resolveCommandsWithParamNames() {
			Map<GmEntityType, List<String>> result = newTreeMap(entityComparator_StandardAlias);

			for (GmEntityType gmType : commandsOverview.allTypes)
				result.put(gmType, allPropsMaybeAlsoWithAliasesOf(gmType));

			return result;
		}

		private List<String> allPropsMaybeAlsoWithAliasesOf(GmEntityType gmType) {
			return jinniCommandsReflection.getRelevantPropertiesOf(gmType).stream() //
					.flatMap(this::nameAndMaybeAlsoAliasesOf) //
					.sorted() //
					.collect(Collectors.toList());
		}

		private Stream<String> nameAndMaybeAlsoAliasesOf(GmProperty gmProperty) {
			CliCompletionStrategy strategy = NullSafe.get(request.getArgumentNameCompletionStrategy(), CliCompletionStrategy.realName);

			switch (strategy) {
				case all:
					return jinniCommandsReflection.cliNameAndAliasesOf(gmProperty);
				case shortest:
					return jinniCommandsReflection.cliNameAndAliasesOf(gmProperty).sorted(StringTools::compareStringsSizeFirst).limit(1);
				case realName:
				default:
					return Stream.of(jinniCommandsReflection.cliNameOf(gmProperty));
			}
		}

		/**
		 * <pre>
		 * __suggestHelp() {
		 * 	__suggest "create-model create-module create-library...";
		 * }
		 * </pre>
		 */
		private String suggestHelp_Body() {
			JinniCommandsOverview helpCo = helpJinniCommandsReflection.getCommandsOverview();
			return printSuggestCommandsOverview(helpCo);
		}

		/**
		 * Renders custom types, e.g. enums (MyColor) or virtual enums.
		 * 
		 * <pre>
		 * __suggestParameterValue() {
		 * 	case $currentWordType in
		 * 		boolean)
		 * 			__suggest "true false";;
		 * 		file)
		 * 			__suggestFile;;
		 *		folder)
		 * 			__suggestFolder;;
		 * 		MyColor)
		 * 			__suggest "red green blue";;
		 * esac
		 * }
		 * </pre>
		 */
		private String suggestParameterValue_CustomCases() {
			stringifier = new BasicStringifier(new StringBuilder(), "\t\t", "\t");

			for (Entry<String, GmEnumType> e : enumsRegistry.shortIdentifierToType.entrySet()) {
				startCase(e.getKey());
				endCaseWithSuggestValues(EnumsRegistry.listConstantsNames(e.getValue()));
			}

			return stringifier.appendable().toString();
		}

		private void startCase(String key) {
			stringifier.println(key + ")");
			stringifier.levelUp();
		}

		private void endCaseWithSuggestValues(List<String> values) {
			stringifier.print("__suggest \"" + StringTools.join(" ", values) + "\"");
			endCase();
		}

		private void endCase() {
			stringifier.println(";;");
			stringifier.levelDown();
		}

	}

	// Helpers

	private String printSuggestCommandsOverview(JinniCommandsOverview commandsOverview) {
		return commandsOverview.allTypes.stream() //
				.map(jinniCommandsReflection::resolveStandardAlias) //
				.collect(Collectors.joining(" "));
	}

	// Once we move past Java 8 replace with Collectors.flatMapping(...)
	static <T, U, A, R> Collector<T, ?, R> flatMapping(Function<? super T, ? extends Stream<? extends U>> mapper,
			Collector<? super U, A, R> downstream) {

		BiConsumer<A, ? super U> acc = downstream.accumulator();
		return Collector.of( //
				downstream.supplier(), //
				(a, t) -> {
					try (Stream<? extends U> s = mapper.apply(t)) {
						if (s != null)
							s.forEachOrdered(u -> acc.accept(a, u));
					}
				}, //
				downstream.combiner(), //
				downstream.finisher(), //
				downstream.characteristics().toArray(new Collector.Characteristics[0]));
	}
}
