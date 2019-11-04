package org.matsim.project;

import java.util.Map;
import java.util.Random;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.berlin.OpenBerlinIntermodalPtDrtRouterModeIdentifierWithDrtTeleportation;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.drtSpeedUp.DrtSpeedUpConfigGroup;
import org.matsim.drtSpeedUp.DrtSpeedUpModule;
import org.matsim.run.drt.RunDrtOpenBerlinScenario;
import org.matsim.testcases.MatsimTestUtils;

/**
 * 
 * @author ikaddoura
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RunDrtSpeedUpOpenBerlinScenarioTest {
		
	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;
	
	@Test
	public final void test1() {
		try {
			final String[] args = {"test/input/berlin-drt-v5.5-1pct.config.xml"};
			
			Config config = RunDrtOpenBerlinScenario.prepareConfig( args , new DrtSpeedUpConfigGroup()) ;
			config.controler().setLastIteration(10);
			config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
			config.controler().setWritePlansInterval(10);
			config.controler().setWriteEventsInterval(10);
			config.controler().setOutputDirectory(utils.getOutputDirectory());
			config.global().setNumberOfThreads(1);
			config.plans().setInputFile("drt-test-agents.xml");
			
			// switch off transit and use teleportation instead
			config.transit().setUseTransit(false);
			ModeRoutingParams pars = new ModeRoutingParams("pt");
			pars.setTeleportedModeSpeed(3.1388889);
			config.plansCalcRoute().addModeRoutingParams(pars);
			
			DrtSpeedUpModule.adjustConfig(config);

			Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario( config ) ;
			downsample( scenario.getPopulation().getPersons(), 1.0 );
			
			Controler controler = RunDrtOpenBerlinScenario.prepareControler( scenario ) ;
			
			controler.addOverridingModule(new AbstractModule() {
				
				@Override
				public void install() {
					// the current one in open berlin needs to be overwritten
					bind(MainModeIdentifier.class).to(OpenBerlinIntermodalPtDrtRouterModeIdentifierWithDrtTeleportation.class);
				}
			});
			
			controler.addOverridingModule(new DrtSpeedUpModule());

			controler.run() ;			
			
		} catch ( Exception ee ) {
			ee.printStackTrace();
			throw new RuntimeException(ee) ;
		}
	}
	
	@Test
	public final void test2() {
		try {
			final String[] args = {"test/input/berlin-drt-v5.5-1pct.config.xml"};
			
			Config config = RunDrtOpenBerlinScenario.prepareConfig( args , new DrtSpeedUpConfigGroup()) ;
			config.controler().setLastIteration(30);
			config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
			config.controler().setWritePlansInterval(1);
			config.controler().setWriteEventsInterval(10);
			config.global().setNumberOfThreads(1);
			config.controler().setOutputDirectory(utils.getOutputDirectory());
			
			// switch off transit and use teleportation instead
			config.transit().setUseTransit(false);
			ModeRoutingParams pars = new ModeRoutingParams("pt");
			pars.setTeleportedModeSpeed(3.1388889);
			config.plansCalcRoute().addModeRoutingParams(pars);
			
			DrtSpeedUpModule.adjustConfig(config);

			Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario( config ) ;
			downsample( scenario.getPopulation().getPersons(), 0.01 );
			
			Controler controler = RunDrtOpenBerlinScenario.prepareControler( scenario ) ;
			
			controler.addOverridingModule(new AbstractModule() {
				
				@Override
				public void install() {
					// the current one in open berlin needs to be overwritten
					bind(MainModeIdentifier.class).to(OpenBerlinIntermodalPtDrtRouterModeIdentifierWithDrtTeleportation.class);
				}
			});
			
			controler.addOverridingModule(new DrtSpeedUpModule());

			controler.run() ;			
			
		} catch ( Exception ee ) {
			ee.printStackTrace();
			throw new RuntimeException(ee) ;
		}
	}
	
	private static void downsample( final Map<Id<Person>, ? extends Person> map, final double sample ) {
		final Random rnd = MatsimRandom.getLocalInstance();
		map.values().removeIf( person -> rnd.nextDouble() > sample ) ;
	}
}
