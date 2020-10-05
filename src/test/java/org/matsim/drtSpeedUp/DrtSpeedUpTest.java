package org.matsim.drtSpeedUp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.drt.routing.DrtRoute;
import org.matsim.contrib.drt.routing.DrtRouteFactory;
import org.matsim.contrib.drt.run.DrtConfigs;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtModule;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.population.routes.RouteFactories;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.testcases.MatsimTestUtils;

/**
 * 
 * @author ikaddoura
 *
 */
public class DrtSpeedUpTest {
		
	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;
	
	@Test
	public final void test1() {	
		List<Double> list = new ArrayList<>();
		list.add(2.);
		list.add(5.);
		list.add(22.);
		
		Assert.assertEquals("Wrong moving average.", 29./3., DrtSpeedUp.computeMovingAverage(3, list), MatsimTestUtils.EPSILON);
		Assert.assertEquals("Wrong moving average.", 27./2., DrtSpeedUp.computeMovingAverage(2, list), MatsimTestUtils.EPSILON);
		Assert.assertEquals("Wrong moving average.", 29./3., DrtSpeedUp.computeMovingAverage(4, list), MatsimTestUtils.EPSILON);
	}

	@Test
	public final void testQsimThreads() {
		Config config = ConfigUtils.loadConfig("scenarios/equil/config-with-drt.xml", new MultiModeDrtConfigGroup(),
				new DvrpConfigGroup(), new DrtSpeedUpConfigGroup());
		// remove demand to speed up this test
		config.plans().setInputFile(null);
		config.controler().setRunId("test2");
		config.controler().setOutputDirectory(utils.getOutputDirectory());
		config.controler().setLastIteration(2);
		config.qsim().setNumberOfThreads(4);
		DrtSpeedUpConfigGroup speedUpConfig = ConfigUtils.addOrGetModule(config, DrtSpeedUpConfigGroup.class);
		speedUpConfig.setIntervalDetailedIteration(2);
		speedUpConfig.setNumberOfThreadsForMobsimDuringSpeedUp(8);

		DrtConfigs.adjustMultiModeDrtConfig(MultiModeDrtConfigGroup.get(config), config.planCalcScore(), config.plansCalcRoute());
		MultiModeDrtSpeedUpModule.addTeleportedDrtMode(config);

		Scenario scenario = ScenarioUtils.loadScenario(config);

		RouteFactories routeFactories = scenario.getPopulation().getFactory().getRouteFactories();
		routeFactories.setRouteFactory(DrtRoute.class, new DrtRouteFactory());

		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new MultiModeDrtModule());
		controler.addOverridingModule(new DvrpModule());
		controler.configureQSimComponents(DvrpQSimComponents.activateAllModes(MultiModeDrtConfigGroup.get(controler.getConfig())));

		controler.addOverridingModule(new MultiModeDrtSpeedUpModule());

		Map<Integer, Integer> iteration2numQsimThreads = new HashMap<>();
		controler.addControlerListener(new BeforeMobsimListener() {
			@Override
			public void notifyBeforeMobsim(BeforeMobsimEvent beforeMobsimEvent) {
				iteration2numQsimThreads.put(beforeMobsimEvent.getIteration(), config.qsim().getNumberOfThreads());
			}
		});

		controler.run();

		Assert.assertEquals("Wrong number of qsim threads.", 4, iteration2numQsimThreads.get(0).intValue());
		Assert.assertEquals("Wrong number of qsim threads.", 8, iteration2numQsimThreads.get(1).intValue());
		Assert.assertEquals("Wrong number of qsim threads.", 4, iteration2numQsimThreads.get(2).intValue());
	}

}
