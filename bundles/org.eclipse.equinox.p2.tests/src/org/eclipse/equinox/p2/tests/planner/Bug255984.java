package org.eclipse.equinox.p2.tests.planner;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.provisional.p2.director.*;
import org.eclipse.equinox.internal.provisional.p2.engine.*;
import org.eclipse.equinox.internal.provisional.p2.metadata.*;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Version;

public class Bug255984 extends AbstractProvisioningTest {
	IInstallableUnit a;
	IInstallableUnit b;
	IInstallableUnit c;
	IProfile profile1;
	IPlanner planner;
	IEngine engine;

	protected void setUp() throws Exception {
		super.setUp();
		a = createIU("A", new Version("1.0.0"), new RequiredCapability[] {MetadataFactory.createRequiredCapability(IInstallableUnit.NAMESPACE_IU_ID, "B", new VersionRange("[1.0.0, 1.0.0]"), null, false, false, true)}, NO_PROPERTIES, true);

		b = createIU("B", new Version("1.0.0"), true);

		createTestMetdataRepository(new IInstallableUnit[] {a, b});

		planner = createPlanner();
		engine = createEngine();
	}

	public void testProperties() {
		//Install B
		profile1 = createProfile("TestProfile." + getName());
		ProfileChangeRequest req = new ProfileChangeRequest(profile1);
		req.addInstallableUnits(new IInstallableUnit[] {b});
		req.setInstallableUnitInclusionRules(a, PlannerHelper.createStrictInclusionRule(b));
		req.setInstallableUnitProfileProperty(b, "foo", "bar");
		ProvisioningPlan plan = planner.getProvisioningPlan(req, null, null);
		assertEquals(IStatus.OK, plan.getStatus().getSeverity());
		engine.perform(profile1, new DefaultPhaseSet(), plan.getOperands(), null, null);
		assertProfileContainsAll("B is missing", profile1, new IInstallableUnit[] {b});
		assertEquals(1, profile1.query(InstallableUnitQuery.ANY, new Collector(), null).size());

		//Install A
		ProfileChangeRequest req2 = new ProfileChangeRequest(profile1);
		req2.addInstallableUnits(new IInstallableUnit[] {a});
		req2.setInstallableUnitInclusionRules(a, PlannerHelper.createStrictInclusionRule(a));
		req.setInstallableUnitProfileProperty(a, "foo", "bar");
		ProvisioningPlan plan2 = planner.getProvisioningPlan(req2, null, null);
		assertEquals(IStatus.OK, plan2.getStatus().getSeverity());
		engine.perform(profile1, new DefaultPhaseSet(), plan2.getOperands(), null, null);
		assertProfileContainsAll("A is missing", profile1, new IInstallableUnit[] {a});
		assertEquals(2, profile1.query(InstallableUnitQuery.ANY, new Collector(), null).size());

		//Uninstall B
		ProfileChangeRequest req3 = new ProfileChangeRequest(profile1);
		req3.removeInstallableUnits(new IInstallableUnit[] {b});
		req3.removeInstallableUnitProfileProperty(b, "foo");
		ProvisioningPlan plan3 = planner.getProvisioningPlan(req3, null, null);
		assertEquals(IStatus.OK, plan3.getStatus().getSeverity());
		engine.perform(profile1, new DefaultPhaseSet(), plan3.getOperands(), null, null);
		assertProfileContainsAll("A is missing", profile1, new IInstallableUnit[] {a});
		assertEquals(2, profile1.query(InstallableUnitQuery.ANY, new Collector(), null).size());
	}
}
