package org.eclipse.equinox.p2.publisher.actions;

import org.eclipse.equinox.internal.provisional.p2.metadata.ProvidedCapability;
import org.eclipse.equinox.internal.provisional.p2.metadata.RequiredCapability;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.eclipse.equinox.p2.publisher.IPublisherAdvice;

public interface ICapabilityAdvice extends IPublisherAdvice {

	public ProvidedCapability[] getProvidedCapabilities(InstallableUnitDescription iu);

	public RequiredCapability[] getRequiredCapabilities(InstallableUnitDescription iu);
}
