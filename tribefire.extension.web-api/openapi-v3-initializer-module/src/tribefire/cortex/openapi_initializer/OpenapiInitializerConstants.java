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
package tribefire.cortex.openapi_initializer;

import java.util.Map;

import com.braintribe.utils.MapTools;

public interface OpenapiInitializerConstants {
	Map<Object, Object> ENDPOINT_PROPERTIES_DESCRIPTIONS = MapTools.getMap("startIndex",
			"Starting index used for pagination. If not set, defaults to <b>0</b>. Optional.",

			"maxResults", "Maximum number of results to return. If not set, returns all results. Optional.",

			"orderBy",
			"Properties to order by. </br> For example, `/rest/v2/entities/myAccess/MyEntity?orderBy=property1&orderBy=property2` orders by <b>property1</b> and, "
					+ "if multiple entities have the same value, <b>property2</b>, both in ascending order. Optional.",

			"orderDirection",
			"Ordering direction of the property you want to order by using the orderBy parameter. </br> "
					+ "Available values: <b>ascending</b> or <b>descending</b>. </br> "
					+ "Example: `/rest/v2/entities/myAccess/MyEntity?orderBy=property1&orderingDirection=descending&orderBy=property2` </br> "
					+ "In the example, the returned value is ordered by <b>property1</b> in <b>descending</b> order and, if multiple entities have the same value, <b>property2</b> in <b>ascending</b> order.",

			"distinct",
			"A Boolean flag influencing whether or not to include duplicate values in the query result. If not set, defaults to <b>false</b>. Optional.",

			"id", "Unique identifier of the entity used to retrive specific object from database.",

			"partition", "Partition of the entity, if there are more than one entities with the same id in the access. Optional.",

			"depth",
			"A simplified TraversingCriterion. For complex assemblies, it dictates how deep the returned assembly should be.<br/>"
					+ "Available values: <b>shallow</b>, <b>reachable</b> or a number >= 0." + "If not set, defaults to <b>3</b>. Optional.",

			"entityRecurrenceDepth",
			"A integer value. For complex recurrence of entities, it dictates how deep returned recurrence assembly should be. <br/>"
					+ "Available values: any number starting from -1. In case of 0 it will use _id and _ref in result. If -1 is used it will traverse <br/>"
					+ "recurrence tree indefinitely. <br/>" + "If not set, defaults to <b>0</b>. Optional.",

			"typeExplicitness",
			"Represents how type of objects should be marshalled.<br/>"
					+ "Available values: <b>auto</b>, <b>always</b>, <b>entities</b>, <b>polymorphic</b>. <br/>"
					+ "<b>auto</b> - The marshaller decides which of the other options it will choose. Look at the individual marshaller's to see for the individual case. <br>"
					+ "<b>always</b> - The types are made explicit to allow to preserve the correct type under all circumstances which means not to rely on any contextual <br/>"
					+ "information to auto convert it from another type. <br/>"
					+ "<b>entities</b> - The types are made explicit for entities in all cases and the other values can get simpler types if appropriate and the context of the value <br/>"
					+ "can give the information to reestablish the correct type with an auto conversion. <br/>"
					+ "<b>polymorphic</b> - The types are made explicit if the actual type cannot be reestablished from the context of the value which is the case that value is <br/>"
					+ "a concretization of the type given by the context. <br/>" + "If not set, defaults to <b>auto</b>. Optional.",

			"identityManagementMode",
			"Represents how duplicates of objects should be unmarshalled.<br/>"
					+ "Available values: <b>auto</b>, <b>off</b>, <b>_id</b>, <b>id</b>. <br/>"
					+ "<b>auto</b> - Depending on the parsed assembly the marshaler automatically detects the identification information and uses it for identity management. <br>"
					+ "<b>off</b> - No identity management done at all. <br/>"
					+ "<b>_id</b> - The internal generated _id information will be used if available. <br/>"
					+ "<b>id</b> - The id property will be used if available. <br/>" + "If not set, defaults to <b>auto</b>. Optional.",

			"prettiness",
			"Represents the level of prettiness to be used when writing the assembly back to the body of the response. "
					+ "Each implementation of the marshallers may use this value slightly differently, but as a rule of thumb, "
					+ "<b>none</b> contains no new lines or indentation information and should only be used to minimize the size of the body and "
					+ "<b>high</b> provides the best possible indentation for humans </br>"
					+ "Available values: <b>none</b>, <b>low</b>, <b>mid</b> or <b>high</b>. If not set, defaults to <b>mid</b>. Optional. ",

			"stabilizeOrder",
			"When this Boolean flag is set to <b>true</b>, the marshaller ensures that properties in different instances of the same entity are "
					+ "always in the same order. Especially useful when <b>writeEmptyProperties=true</b>. If not set, defaults to <b>false</b>.",

			"writeEmptyProperties",
			"When this Boolean flag is set to <b>true</b>, the marshaller writes all properties that are set to <b>null</b> or are empty (for maps, sets and lists).",

			"writeAbsenceInformation",
			"When this Boolean flag is set to <b>true</b>, the marshaller writes an information for all absent properties. ",

			"sessionId", "A valid tribefire sessionId. Alternatively this could also provided via tfsessionId cookie or a header parameter.",

			"listEntitiesRequest",
			"The flag indicates that requests sends multiple entities in payload. Available values are: <b>true</b> and <b>false</b>. If not set default <b>false</b>. Optional.",

			"allowMultipleDelete",
			"Flag to mark allowing multiple (group or all) entities to be delete. This param is used due prevention of unintentional deletion group of entities. "
					+ "Available options <i>true</i> | <i>false</i>. Default value: <i>false</i>.",

			"deleteMode",
			"Way of handling the reference(s) to the deleted entity(ies). "
					+ "Available options <i>dropReferences, dropReferencesIfPossible, failIfReferenced, ignoreReferences</i>",

			"responseContentType", "Force a specific content type to appear in the response header.", "responseFilename",
			"Filename that will be used in the Content-Disposition header of the response and i.e. is used by the browser as a suggested filename when downloading.",
			"saveLocally",
			"When this boolean flag is set to true, the Content-Disposition header of the response will be of type <i>attachment</i>, which will force the browser to save the response locally instead of opening within the browser.",
			"downloadResource",
			"When this boolean flag is set to true <b>and</b> the (projected) response is of type <i>Resource</i>, its binary content is streamed instead of its assembly and can e.g. be used to directly download it or display it via the browser.");
}
