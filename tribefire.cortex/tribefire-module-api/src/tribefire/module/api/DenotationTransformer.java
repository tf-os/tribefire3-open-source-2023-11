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
package tribefire.module.api;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * A single transformer used in the Denotation Transformation process.
 * 
 * <h3>What is Denotation Transformation?</h3>
 * 
 * DT allows the user to configure just the minimal necessary data (e.g. connection information to a DB), and let's the system convert this into
 * standard {@link Deployable}s like an IncrementalAccess.
 * <p>
 * This process may consist of multiple steps, with different experts from different extensions doing the individual steps.
 * <p>
 * <b>Example:</b> a user provides connection information to a DB under the bindId of "auth-access" (access containing users). System, based on this
 * bindId , knows this must be turned into an {@link IncrementalAccess} backed by the "user-model". It looks at {@link DenotationTransformerRegistry
 * registered transformers} and find the following chain of transformation to achieve that:
 * <ol>
 * <li>Conversion of connection data into a connection pool (e.g. HikariConnectionPool) (hikari extension)
 * <li>Conversion of connection pool into HibernateAccess (hibernate extension)
 * <li>Setting property name, externalId and model on the access (platform itself)
 * <li>Configuring proper mappings on given model, e.g. unmapping partition/globalId (hibernate extension)
 * </ol>
 * 
 * Note there are two kinds of transformers:
 * <ul>
 * <li>{@link DenotationMorpher Morphers} turn an instance of some type into another type (e.g. first one, turning DB connection data into
 * ConnectionPool)
 * <li>{@link DenotationEnricher Enrichers} simply modify the instance (e.g. the last one from above example which configures hibernate meta-data).
 * </ul>
 * 
 * <p>
 * The whole transformation is a chain of Transformer steps, and the Transformers are invoked in the following way:
 * <p>
 * Starting with given entity, call all the compatible <i>Enrichers</i> over and over again until they all say they have nothing more to do (see
 * {@link DenotationEnrichmentResult#callAgain()}). Then call a single <i>Morpher</i> (if needed), thus turning the entity into another one of
 * different type. Now again find all the compatible <i>Enrichers</i> and so on until all the <i>Morpher</i> have been called and all <i>Enrichers</i>
 * are done.
 * 
 * <h3>Important notes</h3>
 * 
 * A transformer is never invoked twice for the same {@link DenotationTransformationContext#denotationId()}s (except when an enricher asks to be
 * invoked again).
 * <p>
 * It could however be invoked again with the same entity as input, but with different denotationId. For example, we might want to convert the same DB
 * connection to an access and to a service processor, but the client (who calls these transformers) must ensure the two transformations use different
 * denotationIds.
 * <p>
 * This is important in case the transformer creates new entities with specific globalIds. It must make sure it doesn't assign the same globalId
 * twice, as that would fail (at least when the {@link DenotationTransformationContext} is backed by a session).
 * 
 * @param <S>
 *            type of the denotation entity given to this transformer
 * @param <T>
 *            type of the denotation entity returned by this transformer (this only differs from {@code <T>} for {@link DenotationMorpher}s.
 * 
 * @author peter.gazdik
 */
public interface DenotationTransformer<S extends GenericEntity, T extends GenericEntity> {

	/**
	 * Unique name of this transformer. This name is used when configuring a transformation, e.g. using DenotationTransformation (on an environment
	 * denotation entry).
	 */
	String name();

	EntityType<S> sourceType();

	EntityType<T> targetType();

	/** Returns a detailed textual description of the transformer (to make it entire clear which one it is). */
	String describeYourself();

}
