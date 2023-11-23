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
package tribefire.platform.impl.deployment;

import static com.braintribe.model.processing.bootstrapping.TribefireRuntime.ENVIRONMENT_DCSA_STORAGE;
import static com.braintribe.model.processing.bootstrapping.TribefireRuntime.ENVIRONMENT_DCSA_STORAGE_LOCATION;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.model.dcsadeployment.DcsaSharedStorage;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.traverse.EntityCollector;
import com.braintribe.utils.FileTools;

/**
 * Loads the {@link DcsaSharedStorage} denotation type instance based on configured environment variables or default shared storage configuration.
 * There are two possible variables, but only one of them can be used, otherwise the loader throws an {@link IllegalStateException}.
 * <ul>
 * <li>TRIBEFIRE_DCSA_STORAGE specifies the denotation instance directly.
 * <li>TRIBEFIRE_DCSA_STORAGE_LOCATION specifies the file containing the denotation instance.
 * </ul>
 * In both cases an entity must be specified in YAML format. If a value of the property is in any way incorrect (YAML cannot be parsed, file does not
 * exist...) an exception is thrown.
 * <p>
 * If none of the properties is specified, the fallback is to use the file provided via constructor, which is probably
 * <tt>default-dcsa-shared-storage.yml</tt> file from the conf folder.
 * <p>
 * Example for the content of a DCSA location file:
 * 
 * <pre>
 * !tribefire.extension.jdbc.dcsa.model.deployment.JdbcDcsaSharedStorage
 * project: "tribefire"
 * externalId: "main.dcsa.sharedStorage"
 * autoUpdateSchema: true
 * connectionPool: !com.braintribe.model.deployment.database.pool.HikariCpConnectionPool
 *   externalId: "main.dcsa.connectionPool"
 *   connectionDescriptor: !com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor
 *     driver: "org.postgresql.Driver"
 *     url: "jdbc:postgresql://localhost:5432/dcsa"
 *     user: "postgres"
 *     password: "root"
 * </pre>
 * 
 * <i>NOTE: For for this to work you have to <b>make sure the relevant models</b> (for JdbcDcsaSharedStorage, HikariCpConnectionPool and
 * GenericDatabaseConnectionDescriptor), <b>deployment experts</b> (for the storage and the connection pool) <b>and third party libraries</b> (for
 * posgres driver) <b>are included in you project</b> (probably via modules and platform libraries).</i>
 * 
 * @author peter.gazdik
 */
public class DcsaSharedStorageLoader {

	private final DcsaSharedStorage storage;
	private final EntityCollector collector;

	public DcsaSharedStorageLoader(File defaultDssFile, Supplier<DcsaSharedStorage> defaultSupplier) {
		storage = loadStorage(defaultDssFile, defaultSupplier);
		collector = initializedCollector(storage);
	}

	private static DcsaSharedStorage loadStorage(File defaultDssFile, Supplier<DcsaSharedStorage> defaultSupplier) {
		String dcsaStorage = TribefireRuntime.getProperty(ENVIRONMENT_DCSA_STORAGE);
		String dcsaStorageLocation = TribefireRuntime.getProperty(ENVIRONMENT_DCSA_STORAGE_LOCATION);

		if (dcsaStorage != null && dcsaStorageLocation != null)
			throw new IllegalStateException(String.format(
					"Inconsistent DCSA configuration. Only one environment variable can be set, but they are set both. Values: %s -> %s, %s -> %s",
					ENVIRONMENT_DCSA_STORAGE, dcsaStorage, ENVIRONMENT_DCSA_STORAGE_LOCATION, dcsaStorageLocation));

		if (dcsaStorage != null)
			return unmarshallValue(dcsaStorage);

		if (dcsaStorageLocation != null)
			return unmarshallFromConfiguredLocation(dcsaStorageLocation);

		DcsaSharedStorage result = loadDefaultStorageIfPossible(defaultDssFile);
		if (result != null)
			return result;

		return defaultSupplier.get();
	}

	private static DcsaSharedStorage unmarshallFromConfiguredLocation(String dcsaStorageLocation) {
		File file = new File(dcsaStorageLocation);

		if (!file.exists())
			throwWrongConfiguration("Configured DCSA SharedStoarge location file does not exist: ", dcsaStorageLocation);

		if (file.isDirectory())
			throwWrongConfiguration("Configured DCSA SharedStoarge location file is a directory: ", dcsaStorageLocation);

		return unmarshallFile(file);
	}

	private static void throwWrongConfiguration(String msg, String dcsaStorageLocation) {
		throw new IllegalArgumentException(msg + dcsaStorageLocation + ", property name: " + ENVIRONMENT_DCSA_STORAGE_LOCATION);
	}

	private static DcsaSharedStorage loadDefaultStorageIfPossible(File defaultDssFile) {
		if (!defaultDssFile.exists() || defaultDssFile.isDirectory())
			return null;
		else
			return unmarshallFile(defaultDssFile);
	}

	private static <T> T unmarshallValue(String value) {
		return unmarshall(new StringReader(value));
	}

	private static <T> T unmarshallFile(File file) {
		// Uses UTF-8
		return (T) FileTools.read(file).fromReader(DcsaSharedStorageLoader::unmarshall);
	}

	private static <T> T unmarshall(Reader reader) {
		return (T) new YamlMarshaller().unmarshall(reader);
	}

	private static EntityCollector initializedCollector(Object assembly) {
		EntityCollector result = new EntityCollector();
		result.visit(assembly);

		return result;
	}

	/** May return <tt>null</tt>. */
	public DcsaSharedStorage storage() {
		return storage;
	}

	public Set<GenericEntity> entities() {
		return collector.getEntities();
	}

	public Set<Enum<?>> enums() {
		return collector.getEnums();
	}

}
