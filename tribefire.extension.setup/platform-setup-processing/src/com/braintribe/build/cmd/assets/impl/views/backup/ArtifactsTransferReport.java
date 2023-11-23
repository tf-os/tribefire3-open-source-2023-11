package com.braintribe.build.cmd.assets.impl.views.backup;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.braintribe.common.NumberAwareStringComparator;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ArtifactsTransferReport extends GenericEntity {

	static EntityType<ArtifactsTransferReport> T = EntityTypes.T(ArtifactsTransferReport.class);

	List<TransferredArtifact> getArtifacts();
	void setArtifacts(List<TransferredArtifact> artifacts);
	
	default void sort() {
		Collections.sort(getArtifacts(), Comparator.comparing(TransferredArtifact::getName, NumberAwareStringComparator.INSTANCE));
		getArtifacts().forEach(artifact -> {
			Collections.sort(artifact.getParts(), Comparator.comparing(TransferredArtifactPart::getName, NumberAwareStringComparator.INSTANCE));
			artifact.getParts().forEach(part -> Collections.sort(part.getRepositories(), NumberAwareStringComparator.INSTANCE));
		});
	}
}