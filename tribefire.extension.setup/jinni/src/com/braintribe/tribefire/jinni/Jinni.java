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
package com.braintribe.tribefire.jinni;

import static com.braintribe.console.ConsoleOutputs.brightGreen;
import static com.braintribe.console.ConsoleOutputs.brightRed;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.tribefire.jinni.core.JinniModelAccessoryFactory.USE_CASE_EXECUTION;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.NoSuchElementException;

import com.braintribe.build.cmd.assets.impl.Constants;
import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.api.CharsetOption;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.api.options.GmSerializationContextBuilder;
import com.braintribe.common.attribute.common.CallerEnvironment;
import com.braintribe.common.attribute.common.impl.BasicCallerEnvironment;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.logging.Logger;
import com.braintribe.mime.Mimetype;
import com.braintribe.mime.Mimetypes;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.jinni.api.GetVersion;
import com.braintribe.model.jinni.api.Introduce;
import com.braintribe.model.jinni.api.JinniOptions;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.ValidationExpertRegistry;
import com.braintribe.model.processing.Validator;
import com.braintribe.model.processing.impl.ValidatorImpl;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.service.api.OutputConfigAspect;
import com.braintribe.model.processing.service.impl.BasicOutputConfig;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.setup.tools.TfSetupOutputs;
import com.braintribe.tribefire.jinni.cmdline.api.CommandLineParser;
import com.braintribe.tribefire.jinni.cmdline.api.ParsedCommandLine;
import com.braintribe.tribefire.jinni.cmdline.impl.PosixCommandLineParser;
import com.braintribe.tribefire.jinni.helpers.FromResolver;
import com.braintribe.tribefire.jinni.helpers.JinniConfigHelper;
import com.braintribe.tribefire.jinni.helpers.JinniEntityFactory;
import com.braintribe.tribefire.jinni.helpers.OutputProvider;
import com.braintribe.tribefire.jinni.support.VersionSupplier;
import com.braintribe.tribefire.jinni.wire.JinniWireModule;
import com.braintribe.tribefire.jinni.wire.contract.JinniContract;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.ve.api.VirtualEnvironmentAttribute;
import com.braintribe.ve.impl.ContextualizedVirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * 
 * <h2>Evaluation Context Aspects</h2>
 * 
 * <ul>
 * <li>{@link OutputConfigAspect}
 * <li>{@link CallerEnvironment}
 * <li>{@link VirtualEnvironmentAttribute}
 * </ul>
 * 
 * See {@link #evalRequest()}
 */
public class Jinni {
	private static Logger logger = Logger.getLogger(Jinni.class);

	private final File installationDir;
	private final File confDir;

	public static void main(String[] args) {
		try {
			tryMain(args);
		} catch (RuntimeException e) {
			throw Exceptions.contextualize(e, "Error while executing: jinni " + String.join(" ", args) + "");
		}
	}

	private static void tryMain(String[] args) {
		JinniConfigHelper.turnOffDefaultLogging();

		// until configuration of the protocol use standard ansi console as protocol
		JinniConfigHelper.configureDefaultProtocolling();

		new Jinni().execute(args);
	}

	public Jinni() {
		this.installationDir = newFile(System.getProperty("jinni.installationDir"));
		this.confDir = confDir(installationDir);
	}

	private static File confDir(File installationDir) {
		String confDirProperty = System.getProperty("conf.dir");

		if (confDirProperty != null && !confDirProperty.isEmpty())
			return newFile(confDirProperty);
		else
			return new File(installationDir, "conf");
	}

	private static File newFile(String pathname) {
		try {
			return new File(pathname).getCanonicalFile();
		} catch (IOException e) {
			throw new UncheckedIOException("Error while resolving canonical file for path: " + pathname, e);
		}
	}

	private JinniContract jinniContract;
	private ServiceRequest request;
	private JinniOptions options;
	private JinniEnvironment environment;

	public void execute(String[] args) {
		try (WireContext<JinniContract> context = jinniWireContext()) {
			jinniContract = context.contract();

			tryExecute(args);

		} catch (Exception e) {
			logger.error(e);

			if (options != null && options.getVerbose())
				printFullException(e);
			else
				printShortErrorMessage(e);

			System.out.flush();
			System.exit(1);
		}
	}

	private WireContext<JinniContract> jinniWireContext() {
		JinniPlatformSetupDependencyEnvironment depEnv = new JinniPlatformSetupDependencyEnvironment(installationDir);
		depEnv.setVirtualEnvironment(ContextualizedVirtualEnvironment.INSTANCE);

		return Wire.context(new JinniWireModule(depEnv, confDir));
	}

	private void tryExecute(String[] args) throws IOException {
		Reason error = loadRequestAndOptions(args);
		
		if (error != null) {
			printErrorMessage(error);
			return;
		}

		validateRequest();

		loadJinniEnvironement();

		JinniConfigHelper.configureLogging(options, installationDir);
		JinniConfigHelper.configureProtocolling(options);

		executeGeneralTasks();

		// validation
		if (!validateRequestAgainForSomeReason())
			System.exit(1);

		if (!StringTools.isBlank(options.getAlias())) {
			createAlias();
			return;
		}

		evalAndHandleResponse();

		maybePrintDone();
	}

	private Reason loadRequestAndOptions(String[] args) {
		Maybe<ParsedCommandLine> commandLineMaybe = parseCommand(args);
		
		if (commandLineMaybe.isUnsatisfied())
			return commandLineMaybe.whyUnsatisfied();
		
		ParsedCommandLine commandLine = commandLineMaybe.get();
		
		options = commandLine.acquireInstance(JinniOptions.T);
		request = commandLine.findInstance(ServiceRequest.T).orElseGet(Introduce.T::create);
		
		return null;
	}

	private Maybe<ParsedCommandLine> parseCommand(String[] args) {
		try {
			CommandLineParser commandLineParser = createCommandLineParser();
			JinniEntityFactory entityFactory = new JinniEntityFactory(jinniContract);
			return commandLineParser.parseReasoned(args, entityFactory, jinniContract.modelAccessory());
		} catch (Exception e) {
			// If an error happens here, we couldn't even have parsed the "verbose" option, so we simply print the full error
			printFullException(e);
			throw e;
		}
	}

	private CommandLineParser createCommandLineParser() {
		PosixCommandLineParser posixCommandLineParser = new PosixCommandLineParser();
		posixCommandLineParser.setFromExpert(new FromResolver(jinniContract, StandardEnvironment.INSTANCE));
		return posixCommandLineParser;
	}

	private void validateRequest() {
		if (!jinniContract.modelAccessory().getMetaData().useCase(USE_CASE_EXECUTION).entity(request).is(Visible.T))
			throw new IllegalArgumentException("Unsupported request. See 'jinni help'.");
	}

	private void loadJinniEnvironement() {
		File environmentFile = new File(confDir, "environment." + options.getEnvironment() + ".properties");

		environment = new JinniEnvironment(environmentFile);
		environment.setEnv(Constants.JINNI_INSTALLATION_DIR_PROPERTY_NAME, installationDir.getAbsolutePath());
		environment.setEnv(Constants.JINNI_VERSION_PROPERTY_NAME, VersionSupplier.jinniVersion(installationDir));

		initEnvironmentFromOptions();
	}

	private void executeGeneralTasks() throws IOException {
		if (!options.getSuppressHistory())
			jinniContract.history().historize(request);

		// print jinni version if required
		if (options.getPrintVersion())
			printVersion();

		if (options.getEchoCommand())
			echoCommand();
	}

	private void printVersion() {
		String version = GetVersion.T.create().eval(jinniContract.evaluator()).get();
		println(sequence(text("Jinni version: "), TfSetupOutputs.version(version)));
	}

	private void echoCommand() {
		StringWriter buffer = new StringWriter();
		jinniContract.yamlMarshaller().marshall(buffer, request, GmSerializationOptions.defaultOptions);

		println("Executing command:\n");
		println(buffer.toString());
	}

	private void initEnvironmentFromOptions() {
		// for convenience we make it possible to set user/global/exclusive settings via
		// Jinni Options
		if (options.getUserSettings() != null) {
			environment.setEnv(Constants.ARTIFACT_REPOSITORIES_USER_SETTINGS, options.getUserSettings());
		}
		if (options.getGlobalSettings() != null) {
			environment.setEnv(Constants.ARTIFACT_REPOSITORIES_GLOBAL_SETTINGS, options.getGlobalSettings());
		}
		if (options.getRepositoryConfiguration() != null) {
			environment.setEnv(Constants.DEVROCK_REPOSITORY_CONFIGURATION, options.getRepositoryConfiguration());
		}
		if (options.getExclusiveSettings() != null) {
			String exclusiveSettingsAsStr = options.getExclusiveSettings();
			File f = new File(exclusiveSettingsAsStr);
			if (!f.exists())
				throw new IllegalArgumentException("--exclusiveSettings points to a nonexisting path!");

			environment.setEnv(Constants.ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS, exclusiveSettingsAsStr);
		}
		if (options.getOffline() != null) {
			if (options.getOffline()) {
				environment.setEnv(Constants.MC_CONNECTIVITY_MODE, Constants.MC_CONNECTIVITY_MODE_OFFLINE);
			} else {
				// instead of setting to "online", we just do not set the setting (as suggested by PST)
			}
		}

		environment.setEnvs(options.getEnvironmentVariables());
	}

	private boolean validateRequestAgainForSomeReason() {
		CmdResolver cmdResolver = jinniContract.modelAccessory().getCmdResolver();
		Validator validator = new ValidatorImpl(cmdResolver, ValidationExpertRegistry.createDefault());
		validator.validate(request);

		return true;
	}

	private void createAlias() {
		jinniContract.alias().createAlias(options.getAlias(), request);
	}

	private void evalAndHandleResponse() throws IOException {
		Object value = evalRequest();

		handleResponse(value);
	}

	private Object evalRequest() {
		EvalContext<?> evalContext = request.eval(jinniContract.evaluator());
		evalContext.setAttribute(OutputConfigAspect.class, new BasicOutputConfig(options.getVerbose()));
		evalContext.setAttribute(CallerEnvironment.class, callerEnvironment());
		evalContext.setAttribute(VirtualEnvironmentAttribute.class, environment);

		// evaluate the request
		return evalContext.get();
	}

	private CallerEnvironment callerEnvironment() {
		File currentWorkingDirectory = new File(System.getProperty("user.dir"));
		return new BasicCallerEnvironment(true, currentWorkingDirectory);
	}

	private void handleResponse(Object value) throws IOException {
		OutputProvider outputProvider = JinniConfigHelper.configureResponding(options);
		if (outputProvider != null)
			outputResult(outputProvider, value);
	}
	private void outputResult(OutputProvider outputProvider, Object value) throws IOException {
		Mimetype mimetype = Mimetypes.parse(options.getResponseMimeType());

		Marshaller marshaller = resolveMarshallerFor(mimetype);

		String charset = mimetype.getCharset();
		GmSerializationContextBuilder highlyPretty = GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high);

		if (marshaller instanceof CharacterMarshaller) {
			try (Writer writer = outputProvider.openOutputWriter(charset, mimetype.hasExplicitCharset())) {
				((CharacterMarshaller) marshaller).marshall(writer, value, highlyPretty.build());
			}

		} else {
			try (OutputStream responseOut = outputProvider.openOutputStream()) {
				marshaller.marshall(responseOut, value, highlyPretty.set(CharsetOption.class, charset).build());
			}
		}
	}

	private Marshaller resolveMarshallerFor(Mimetype mimetype) {
		Marshaller marshaller = jinniContract.marshallerRegistry().getMarshaller(mimetype.getMimeType());
		if (marshaller == null)
			throw new NoSuchElementException("No marshaller found for mimetype: " + mimetype.getMimeType());

		return marshaller;
	}

	private void maybePrintDone() {
		if (!Boolean.TRUE.toString().equals(System.getProperty("jinni.suppressDone")))
			println(brightGreen("\nDONE"));
	}

	private static void printFullException(Exception e) {
		println(brightRed("\nERROR:\n"));
		println(Exceptions.stringify(e));
	}

	private void printShortErrorMessage(Exception e) {
		printErrorMessage(getErrorMessage(e));
	}
	
	private void printErrorMessage(Reason error) {
		printErrorMessage(error.stringify());
	}
	
	private void printErrorMessage(String msg) {
		println(sequence(brightRed("\nERROR: "), text(msg)));
	}
	

	private static String getErrorMessage(Throwable e) {
		String message = e.getMessage();

		if (!StringTools.isEmpty(message))
			return message;

		StackTraceElement ste = e.getStackTrace()[0];
		return e.getClass().getSimpleName() + " occurred in " + ste.getClassName() + '.' + ste.getMethodName() + " line " + ste.getLineNumber();
	}

}
