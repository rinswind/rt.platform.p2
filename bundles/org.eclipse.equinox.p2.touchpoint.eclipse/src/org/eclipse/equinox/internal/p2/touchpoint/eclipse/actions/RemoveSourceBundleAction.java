/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.internal.p2.touchpoint.eclipse.actions;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.touchpoint.eclipse.*;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.osgi.util.NLS;

public class RemoveSourceBundleAction extends ProvisioningAction {
	public static final String ID = "removeSourceBundle"; //$NON-NLS-1$

	public IStatus execute(Map<String, Object> parameters) {
		return RemoveSourceBundleAction.removeSourceBundle(parameters);
	}

	public IStatus undo(Map<String, Object> parameters) {
		return AddSourceBundleAction.addSourceBundle(parameters);
	}

	public static IStatus removeSourceBundle(Map<String, Object> parameters) {
		IProvisioningAgent agent = (IProvisioningAgent) parameters.get(ActionConstants.PARM_AGENT);
		IProfile profile = (IProfile) parameters.get(ActionConstants.PARM_PROFILE);
		IInstallableUnit iu = (IInstallableUnit) parameters.get(EclipseTouchpoint.PARM_IU);
		SourceManipulator manipulator = (SourceManipulator) parameters.get(EclipseTouchpoint.PARM_SOURCE_BUNDLES);
		String bundleId = (String) parameters.get(ActionConstants.PARM_BUNDLE);
		if (bundleId == null)
			return Util.createError(NLS.bind(Messages.parameter_not_set, ActionConstants.PARM_BUNDLE, ID));

		Collection<IArtifactKey> artifacts = iu.getArtifacts();
		if (artifacts == null || artifacts.isEmpty())
			return Util.createError(NLS.bind(Messages.iu_contains_no_arifacts, iu));

		IArtifactKey artifactKey = null;
		for (IArtifactKey candidate : artifacts) {
			if (candidate.toString().equals(bundleId)) {
				artifactKey = candidate;
				break;
			}
		}
		if (artifactKey == null)
			throw new IllegalArgumentException(NLS.bind(Messages.no_matching_artifact, bundleId));

		// the bundleFile might be null here, that's OK.
		File bundleFile = Util.getArtifactFile(agent, artifactKey, profile);

		try {
			manipulator.removeBundle(bundleFile, artifactKey.getId(), artifactKey.getVersion());
		} catch (IOException e) {
			return Util.createError(NLS.bind(Messages.cannot_configure_source_bundle, artifactKey));
		}
		return Status.OK_STATUS;
	}
}