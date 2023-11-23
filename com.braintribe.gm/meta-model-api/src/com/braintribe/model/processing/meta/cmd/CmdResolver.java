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
package com.braintribe.model.processing.meta.cmd;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.Predicate;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.Priority;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.meta.cmd.builders.MdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.EntityAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.UseCaseAspect;
import com.braintribe.model.processing.meta.cmd.result.MdResult;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * A resolver for {@link MetaData} attached to a given {@link GmMetaModel}. This class is able to evaluate a query for meta data of given model,
 * entity, property, enum or enum constant.
 * <p>
 * The entry method for MD resolution is {@link #getMetaData()}.
 * <p>
 * The resolver implementation must be thread safe and highly concurrent.
 * 
 * <h3>How to use</h3>
 * 
 * When resolving meta-data, we have to specify multiple things:
 * <ul>
 * <li><b>model element:</b> which element are we resolving for - model, entity, property, enum or enum constant</li>
 * <li><b>resolution context:</b> any information relevant for the MD resolution, specifically when evaluating the {@link MetaDataSelector}s of our
 * meta data. Can be the {@link EntityAspect entity itself}, {@link UseCaseAspect use-case} (explained later) or any other
 * {@link SelectorContextAspect aspect}.</li>
 * <li><b>meta-data type:</b> specifies the concrete meta data we are resolving, e.g. {@link Name}, {@link Description} or {@link Priority}</li>
 * <li><b>result kind:</b> what information do we want to get - all meta-data, just the one with highest priority, or just whether a {@link Predicate}
 * is {@code true} or {@code false}. See methods of {@link MdResult}, as well as {@link MdResolver#is(EntityType)} - a special method for resolving
 * predicates.</li>
 * </ul>
 * 
 * <b>Examples:</b>
 * <p>
 * Resolving property meta-data:
 * 
 * <pre>
 * Size size = cmdResolver.getMetaData() //
 * 		.entity(myEntity) //
 * 		.property("myProperty") //
 * 		.useCase("font") //
 * 		.meta(Size.T) //
 * 		.exclusive();
 * </pre>
 * 
 * This is a made-up example, as there is no Size MD in our core models. But it illustrates the recommended approach to MD - rather than creating a
 * custom MD called FontSize we use a more general-one called Size and define the MD with a "font" {@link UseCaseSelector}. This ensures the MD is
 * ignored in other situations.
 * 
 * <p>
 * Resolving predicate meta-data:
 * 
 * <pre>
 * boolean isVisible = cmdResolver.getMetaData() //
 * 		.entityType(MyEntity.T) //
 * 		.property("myProperty") //
 * 		.is(Visible.T);
 * </pre>
 * 
 * <h3>Efficient caching of MetaData-based configuration</h3>
 * 
 * In some situations we might want to cache the data that is computed based on configured MD, but we might not know whether it is possible. For
 * example, some MD might depend on property values of given entity, thus resolving for different instances might return different values. For this
 * scenario it is possible to tell the resolver to {@link MdResolver#ignoreSelectors() ignore the MD selectors}, which makes the resolver treat each
 * MD as if there was not selector.
 * <p>
 * <i>How does this help?</i>
 * <p>
 * In case of {@link MdResult#exclusive() exclusive} MD resolution, if the MD resolved this way still has no selector (or has a selector which in our
 * case is always true, e.g. use-case with our value), we know this instance will always be returned.
 * <p>
 * In case of {@link MdResult#list() list} MD resolution it's similar, as long as we can see that all the resolved MD instances would always be
 * resolved, we know the result can be cached.
 * <p>
 * <i>OK, but what about that use-case selector. The example above said that rather than creating custom MD I should use more general ones, but with a
 * use-case specific selector. Wouldn't this pollute my result if I say I want to ignore selectors in such cases and I get MD mix from different
 * use-cases?</i>
 * <p>
 * Yes, indeed. And for this case we have a method to {@link MdResolver#ignoreSelectorsExcept(EntityType...) ignore all selectors except those stated
 * explicitly}. Typically we'd "not ignore" the use-case selector, thus getting all possible MD for given use-case, but the user can chose other
 * selectors if relevant.
 * <p>
 * <i>So what does it mean we ignore some selectors but not others?</i>
 * <p>
 * It means the evaluation uses a three value logic, where each selector is evaluated to either {@code true}, {@code false} or {@code unknown}, where
 * the last value is assigned to each ignored selector. The MD is then relevant iff it's selector evaluates to {@code true} or {@code unkown}, while
 * the value {@code false} indicates the MD is not resolvable under any circumstances.
 * <p>
 * Note that as of right now the arguments don't support polymorphism, so you have to specify exactly which selectors you want to exclude from being
 * ignored (i.e. it's not enough to specify the super type). If needed, we'll provide an alternative configuration with a {@link Predicate} in the
 * future.
 * 
 * @see Predicate
 * @see ModelMetaDataEditor
 * @see ModelOracle
 */
public interface CmdResolver {

	/** Returns the {@link ModelOracle} that backs this resolver. */
	ModelOracle getModelOracle();

	ModelMdResolver getMetaData();

	MdSelectorResolver getMdSelectorResolver();

	/** Resolves the type of {@link GenericEntity#id id} property based on TypeSpecification meta data. */
	<T extends ScalarType> T getIdType(String typeSignature);

}
