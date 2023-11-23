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
package com.braintribe.model.processing.notification.api.builder;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.notification.CommandNotification;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.query.Query;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.uicommand.ApplyManipulation;
import com.braintribe.model.uicommand.DownloadResource;
import com.braintribe.model.uicommand.GotoModelPath;
import com.braintribe.model.uicommand.GotoUrl;
import com.braintribe.model.uicommand.PrintResource;
import com.braintribe.model.uicommand.Refresh;
import com.braintribe.model.uicommand.RefreshPreview;
import com.braintribe.model.uicommand.Reload;
import com.braintribe.model.uicommand.ReloadView;
import com.braintribe.model.uicommand.RunQuery;
import com.braintribe.model.uicommand.RunQueryString;
import com.braintribe.model.uicommand.RunWorkbenchAction;

/**
 * Builder API to fluently create a {@link CommandNotification}
 */
public interface CommandBuilder {

	/**
	 * Returns a {@link ModelPathBuilder} that can be used to create a {@link GotoModelPath} command.
	 */
	ModelPathBuilder gotoModelPath(String name);

	/**
	 * Returns a {@link ModelPathBuilder} that can be used to create a {@link GotoModelPath} command.
	 */
	ModelPathBuilder gotoModelPath(String name, boolean addToCurrentView, boolean showFullModelPath);
	/**
	 * Returns a {@link UrlBuilder} that can be used to create a {@link GotoUrl} command.
	 */
	UrlBuilder gotoUrl(String name);
	/**
	 * Creates a new {@link Refresh} command that tells the UI to refresh the current selected entity.
	 */
	NotificationBuilder refresh(String name);
	/**
	 * Creates a new {@link Refresh} command that tells the UI to refresh the passed entity.
	 */
	NotificationBuilder refresh(String name, GenericEntity entity);

	/**
	 * Creates a new {@link Refresh} command that tells the UI to refresh the entity identified by the passed
	 * EntityReference.
	 */
	NotificationBuilder refresh(String name, PersistentEntityReference reference);

	/**
	 * Creates a new {@link Refresh} command that tells the UI to refresh the entity identified by the passed type and
	 * id.
	 */
	NotificationBuilder refresh(String name, EntityType<? extends GenericEntity> type, Object id);

	/**
	 * Creates a new {@link Reload} command that tells the UI to reload the current session.
	 */
	NotificationBuilder reload(String name);
	/**
	 * Creates a new {@link ReloadView} command that tells the UI to reload the current view.
	 */
	NotificationBuilder reloadView(String name);

	/**
	 * Creates a new {@link ReloadView} command that tells the UI to reload the current view and sets the reloadAll flag to true.
	 */
	NotificationBuilder reloadAllViews(String name);

	/**
	 * Creates a new {@link RefreshPreview} command that tells the UI refresh the preview for a given entity.
	 */
	NotificationBuilder refreshPreview(String name, String typeSignature, Object id);
	NotificationBuilder refreshPreview(String name, EntityType<? extends GenericEntity> typeSignature, Object id);
	NotificationBuilder refreshPreview(String name, GenericEntity entity);

	/**
	 * Creates a new {@link DownloadResource} command that tells the UI to download a resource.
	 */
	NotificationBuilder downloadResource(String name, Resource resource);
	NotificationBuilder downloadResource(String name, Resource... resources);
	NotificationBuilder downloadResource(String name, List<Resource> resources);
	/**
	 * Creates a new {@link PrintResource} command that tells the UI to open the browsers print dialog
	 */
	NotificationBuilder printResource(String name, Resource resource);
	NotificationBuilder printResource(String name, Resource... resources);
	NotificationBuilder printResource(String name, List<Resource> resources);
	/**
	 * Creates a new {@link ApplyManipulation} command to be applied on the client side.
	 */
	NotificationBuilder applyManipulation(String name, Manipulation manipulation);
	/**
	 * Same as {@link #applyManipulation(String, Manipulation)} but creates a single manipulation object based on given
	 * manipulation list.
	 */
	NotificationBuilder applyManipulations(String name, List<Manipulation> manipulation);
	/**
	 * Same as {@link #applyManipulation(String, Supplier, Object, Consumer, Function)} but without the optional
	 * manipulationAdapter.
	 */
	<T> NotificationBuilder applyManipulation(String name, Supplier<ManagedGmSession> trackingSessionSupplier, T trackingSubject,
			Consumer<T> tracker);
	/**
	 * Creates a new {@link ApplyManipulation} command with manipulations tracked for the given trackingSubject during
	 * execution of the given tracker.
	 * 
	 * @param name
	 *            the name of the command.
	 * @param trackingSessionSupplier
	 *            provides the session that should be used for tracking the manipulations. usually thats a
	 *            BasicManagedGmSession
	 * @param trackingSubject
	 *            the GenericModel assembly that should be merged into the tracking session.
	 * @param tracker
	 *            a consumer that accepts the managed trackingSubject. all manipulations done in this tracker are
	 *            collected.
	 * @param trackedManipulationAdapter
	 *            an optional function that can intercept/adapt the collected manipulation before passing it to the
	 *            ApplyManipulation command.
	 */
	<T> NotificationBuilder applyManipulation(String name, Supplier<ManagedGmSession> trackingSessionSupplier, T trackingSubject, Consumer<T> tracker,
			Function<Manipulation, Manipulation> trackedManipulationAdapter);
	/**
	 * Creates a new {@link RunQueryString} command to be run on the client side.
	 */
	NotificationBuilder runQuery(String name, String queryString);
	/**
	 * Creates a new {@link RunQuery} command to be run on the client side.
	 */
	NotificationBuilder runQuery(String name, Query queryString);

	/**
	 * Creates a new {@link RunWorkbenchAction} command to be run on the client side.
	 */
	NotificationBuilder runWorkbenchAction(String name, String workbenchActionFolderId);
	
	/**
	 * Creates a new {@link RunWorkbenchAction} command to be run on the client side.
	 */
	NotificationBuilder runWorkbenchAction(String name, String workbenchActionFolderId, Map<String, Object> variableValues);


}
