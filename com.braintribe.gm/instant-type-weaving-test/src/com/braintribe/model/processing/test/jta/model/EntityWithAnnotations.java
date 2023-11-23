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
package com.braintribe.model.processing.test.jta.model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Bidirectional;
import com.braintribe.model.generic.annotation.meta.Color;
import com.braintribe.model.generic.annotation.meta.CompoundUnique;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Emphasized;
import com.braintribe.model.generic.annotation.meta.Max;
import com.braintribe.model.generic.annotation.meta.Min;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.NonDeletable;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.annotation.meta.Unique;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
@CompoundUnique("compoundUniqueSingle")
@CompoundUnique({ "compoundUnique1", "compoundUnique2" })
@SelectiveInformation("Selective INFORMATION")
@PositionalArguments({"unique", "bidi"})
@NonDeletable
public interface EntityWithAnnotations extends GenericEntity {

	EntityType<EntityWithAnnotations> T = EntityTypes.T(EntityWithAnnotations.class);

	@Unique
	String getUnique();
	void setUnique(String unique);

	@Bidirectional(type = EntityWithAnnotations.class, property = "bidi")
	EntityWithAnnotations getBidi();
	void setBidi(EntityWithAnnotations bidi);

	String getCoumpoundUniqueSingle();
	void setCoumpoundUniqueSingle(String coumpoundUniqueSingle);

	String getCoumpoundUnique1();
	void setCoumpoundUnique1(String coumpoundUnique1);

	String getCoumpoundUnique2();
	void setCoumpoundUnique2(String coumpoundUnique2);

	@Name("default name")
	String getPropertyWithName();
	void setPropertyWithName(String propertyWithName);

	@Name("default name")
	@Name(locale = "de", value = "Der Name", globalId = "md.names")
	@Name(locale = "br", value = "O Nome", globalId = "md.names")
	String getPropertyWithNames();
	void setPropertyWithNames(String propertyWithNames);

	@Description(value = "default description", globalId = "md.description")
	String getPropertyWithDescription();
	void setPropertyWithDescription(String propertyWithDescription);

	@Description("default description")
	@Description(locale = "de", value = "Unfug")
	String getPropertyWithDescriptions();
	void setPropertyWithDescriptions(String propertyWithDescriptions);

	@Min("0")
	@Max("100")
	Integer getHasLimit();
	void setHasLimit(Integer hasLimit);

	@Alias("alias")
	String getAliased();
	void setAliased(String aliased);

	@Alias("a")
	@Alias("ALIAS")
	String getAliasedMulti();
	void setAliasedMulti(String aliasedMulti);

	@Priority(666.666)
	String getPrioritized();
	void setPrioritized(String prioritized);
	
	@Emphasized
	String getEmphasized();
	void setEmphasized(String emphasized);

	@Color("#0F0")
	String getColored();
	void setColored(String colored);
	
	@Deprecated
	String getDeprecated();
	void setDeprecated(String deprecated);

}
