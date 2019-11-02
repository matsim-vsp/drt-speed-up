/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.drtSpeedUp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonMoneyEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFareConfigGroup;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFaresConfigGroup;
import org.matsim.contrib.drt.routing.DrtRoute;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.router.TripStructureUtils.Trip;

import com.google.inject.Inject;

/**
* @author ikaddoura
*/

final class DrtSpeedUp implements PersonDepartureEventHandler, PersonArrivalEventHandler, StartupListener, BeforeMobsimListener, AfterMobsimListener, IterationStartsListener, IterationEndsListener {
	private static final Logger log = Logger.getLogger(DrtSpeedUp.class);

	private final double fractionOfIterationsSwitchOff = 0.7;
	private final double fractionOfIterationSwitchOn = 0.0;
	private final int intervalDetailedIteration = 10;
	private final String mode = "drt";
	private final double beelineDistanceFactorForFareCalculation = 1.3;
	
	private ShpUtils shpUtils;
	
	boolean teleportDrtUsers = false;
	
	Map<Id<Person>, Double> person2drtDepTime = new HashMap<>();
	Map<Id<Person>, Id<Link>> person2drtDepLinkId = new HashMap<>();
	List<Double> beelineSpeeds = new ArrayList<>();
    Set<Id<Person>> dailyFeeCharged = new HashSet<>();

	@Inject
	private Scenario scenario;
	
	@Inject
	private EventsManager events;

	@Override
    public void reset(int iteration) {
        dailyFeeCharged.clear();
        beelineSpeeds.clear();
        person2drtDepTime.clear();
        person2drtDepLinkId.clear();
    }
	
	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		if (teleportDrtUsers == false) {
			// update statstics
			double sum = 0.;
			for (Double speed : beelineSpeeds) {
				sum += speed;
			}
			double averageBeelineSpeed = 2.7777778; // default value
			if (beelineSpeeds.size() > 0) averageBeelineSpeed = sum / beelineSpeeds.size();
			log.info("Setting teleported mode speed for " + this.mode + "_teleportation to the average beeline speed: " + averageBeelineSpeed);
			
			// and then set the teleportation parameters accordingly
			ModeRoutingParams modeParams = new ModeRoutingParams(this.mode + "_teleportation");
			modeParams.setBeelineDistanceFactor(1.0);
			modeParams.setTeleportedModeSpeed(averageBeelineSpeed);
			this.scenario.getConfig().plansCalcRoute().addModeRoutingParams(modeParams);
		}
	}

	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		if (event.getIteration() < this.fractionOfIterationSwitchOn * this.scenario.getConfig().controler().getLastIteration()
				|| event.getIteration() >= this.fractionOfIterationsSwitchOff * this.scenario.getConfig().controler().getLastIteration()) {	
			// run the detailed drt simulation
			teleportDrtUsers = false;
			log.info("Simulating drt in iteration " + event.getIteration());

		} else {
			if (event.getIteration() % this.intervalDetailedIteration == 0) {
				// run the detailed drt simulation
				teleportDrtUsers = false;
				log.info("Simulating drt in iteration " + event.getIteration());
			} else {
				// teleport drt users
				teleportDrtUsers = true;
				log.info("Teleporting drt users in iteration " + event.getIteration());
			}
		}
	}

	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		if (teleportDrtUsers) {
			// set mode back to original drt mode
			// TODO: Make the following also work for intermodal trips: DRT + PT
			for (Person person : scenario.getPopulation().getPersons().values()) {
				Plan selectedPlan = person.getSelectedPlan();
				System.out.println("before: " + selectedPlan.toString());

				for (PlanElement pE : selectedPlan.getPlanElements()) {		
					if (pE instanceof Leg) {
						Leg leg = (Leg) pE;
						if (leg.getMode().equals(this.mode + "_teleportation")) {
							leg.setMode(this.mode);
							leg.setRoute(new DrtRoute(leg.getRoute().getStartLinkId(), leg.getRoute().getEndLinkId()));
						}
					}
				}
				
				System.out.println("after: " + selectedPlan.toString());
			}
		}
	}

	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		if (teleportDrtUsers) {
			// set mode to teleported drt mode
			// TODO: Make the following also work for intermodal trips: DRT + PT
			for (Person person : scenario.getPopulation().getPersons().values()) {
				Plan selectedPlan = person.getSelectedPlan();
				Plan modifiedPlan = scenario.getPopulation().getFactory().createPlan();
				boolean isFirstTrip = true;
				for (Trip trip : TripStructureUtils.getTrips(selectedPlan)) {
					
					if (isFirstTrip) {
						modifiedPlan.addActivity(trip.getOriginActivity());
						isFirstTrip = false;
					}					
					
					String mode = new MainModeIdentifierImpl().identifyMainMode(trip.getTripElements());
					if (mode.equals(this.mode)) {
						modifiedPlan.addLeg(scenario.getPopulation().getFactory().createLeg(this.mode + "_teleportation"));
					
					} else {
						for (PlanElement pE : trip.getTripElements()) {
							if (pE instanceof Activity) {
								Activity act = (Activity) pE;
								modifiedPlan.addActivity(act);
							}
							if (pE instanceof Leg) {
								Leg leg = (Leg) pE;
								modifiedPlan.addLeg(leg);
							}
						}
					}
					
					modifiedPlan.addActivity(trip.getDestinationActivity());
				}
				
				// replace previous plan
				person.removePlan(selectedPlan);
				person.addPlan(modifiedPlan);
				person.setSelectedPlan(modifiedPlan);
			}
		}
	}

	@Override
	public void notifyStartup(StartupEvent event) {
		log.info("Loading drt service area shape file...");
		for (DrtConfigGroup drtCfg : MultiModeDrtConfigGroup.get(scenario.getConfig()).getModalElements()) {
			if (drtCfg.getMode().equals(this.mode)) {
				shpUtils = new ShpUtils(drtCfg.getDrtServiceAreaShapeFile());
			}
		}
		log.info("Loading drt service area shape file... Done.");
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		if (event.getLegMode().equals(this.mode) || event.getLegMode().equals(this.mode + "_teleportation")) {
			Link depLink = this.scenario.getNetwork().getLinks().get(this.person2drtDepLinkId.get(event.getPersonId()));
			Link arrLink = this.scenario.getNetwork().getLinks().get(event.getLinkId());
			double beeline = NetworkUtils.getEuclideanDistance(depLink.getCoord(), arrLink.getCoord());
			double time = event.getTime() - this.person2drtDepTime.get(event.getPersonId());
			
			if (event.getLegMode().equals(this.mode)) {
				// store the statistics
				this.beelineSpeeds.add(beeline / time);
			}	
			
			if (event.getLegMode().equals(this.mode + "_teleportation")) {
				// compute fares and throw money events
				
				// normal drt fare
				double fare = 0.;
				for (DrtFareConfigGroup drtFareCfg : DrtFaresConfigGroup.get(scenario.getConfig()).getDrtFareConfigGroups()) {
					if (drtFareCfg.getMode().equals(this.mode)) {
						if (!dailyFeeCharged.contains(event.getPersonId())) {
							dailyFeeCharged.add(event.getPersonId());
			                events.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), -drtFareCfg.getDailySubscriptionFee()));
			            }
						fare += drtFareCfg.getBasefare();
						fare += drtFareCfg.getDistanceFare_m() * beelineDistanceFactorForFareCalculation * beeline;
						fare += drtFareCfg.getTimeFare_h() * time / 3600.;
						if (fare < drtFareCfg.getMinFarePerTrip()) fare = drtFareCfg.getMinFarePerTrip();
			            events.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), -fare));
					}
				}
				
				// now consider the service area
				if (shpUtils.isCoordInDrtServiceAreaWithBuffer(depLink.getCoord(), 1000.)
						&& shpUtils.isCoordInDrtServiceAreaWithBuffer(arrLink.getCoord(), 1000.))  {
					// trip within drt service area + buffer
				} else {
					// trip outside of drt service area + buffer
		            events.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), -999999999.));
				}
				
			}	
			this.person2drtDepLinkId.remove(event.getPersonId());
			this.person2drtDepTime.remove(event.getPersonId());
		}
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if (event.getLegMode().equals(this.mode) ||
				event.getLegMode().equals(this.mode + "_teleportation")) {
			this.person2drtDepTime.put(event.getPersonId(), event.getTime());
			this.person2drtDepLinkId.put(event.getPersonId(), event.getLinkId());
		}
	}
	
}

