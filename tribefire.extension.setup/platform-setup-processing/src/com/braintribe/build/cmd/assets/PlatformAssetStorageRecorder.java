// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.build.cmd.assets.api.PlatformAssetStorageRecording;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;

public class PlatformAssetStorageRecorder implements PlatformAssetStorageRecording {
	private BasicManagedGmSession session = new BasicManagedGmSession();
	private List<Manipulation> manipulations = new ArrayList<>();
	
	public PlatformAssetStorageRecorder() {
		session.listeners().add(manipulations::add);
	}

	@Override
	public ManagedGmSession session() {
		return session;
	}

	@Override
	public List<Manipulation> manipulations() {
		return manipulations;
	}
}
