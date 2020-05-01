package org.matsim.drtSpeedUp;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.matsim.analysis.ScoreStatsControlerListener.ScoreItem;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFareModule;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFaresConfigGroup;
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
import org.matsim.core.population.routes.RouteFactories;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.example.RunExampleDrtSpeedUp;
import org.matsim.testcases.MatsimTestUtils;

/**
 * 
 * @author ikaddoura
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RunDrtSpeedUpTest {
		
	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;
	
	@Test
	public final void test1() {
		RunExampleDrtSpeedUp.main(null);
	}
	
	@Test
	public final void test2() {
		Config config = ConfigUtils.loadConfig("scenarios/equil/config-with-drt.xml", new MultiModeDrtConfigGroup(), new DvrpConfigGroup(), new DrtFaresConfigGroup(), new DrtSpeedUpConfigGroup());
		config.controler().setRunId("test2");
		config.controler().setOutputDirectory(utils.getOutputDirectory());
		
		DrtConfigs.adjustMultiModeDrtConfig(MultiModeDrtConfigGroup.get(config), config.planCalcScore(), config.plansCalcRoute());
		DrtSpeedUpModule.adjustConfig(config);
		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		RouteFactories routeFactories = scenario.getPopulation().getFactory().getRouteFactories();
		routeFactories.setRouteFactory(DrtRoute.class, new DrtRouteFactory());
		
		Controler controler = new Controler(scenario);		
		controler.addOverridingModule(new MultiModeDrtModule());
		controler.addOverridingModule(new DvrpModule());
		controler.configureQSimComponents(DvrpQSimComponents.activateAllModes(MultiModeDrtConfigGroup.get(controler.getConfig())));				
		controler.addOverridingModule(new DrtFareModule());
		
		controler.addOverridingModule(new DrtSpeedUpModule());
		
		controler.run();
		
		Assert.assertEquals("Wrong score.", -68.67967143801974, controler.getScoreStats().getScoreHistory().get(ScoreItem.executed).get(0), MatsimTestUtils.EPSILON);
		Assert.assertEquals("Wrong score.", -51.361656180215135, controler.getScoreStats().getScoreHistory().get(ScoreItem.executed).get(5), MatsimTestUtils.EPSILON);
		Assert.assertEquals("Wrong score.", -59.45541533485486, controler.getScoreStats().getScoreHistory().get(ScoreItem.executed).get(10), MatsimTestUtils.EPSILON);

	
	}
	
}
