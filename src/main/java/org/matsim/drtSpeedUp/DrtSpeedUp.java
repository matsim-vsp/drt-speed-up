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

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonMoneyEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Route;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFareConfigGroup;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFaresConfigGroup;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEvent;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEventHandler;
import org.matsim.contrib.dvrp.fleet.FleetSpecification;
import org.matsim.core.api.experimental.events.EventsManager;
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
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.drtSpeedUp.DrtSpeedUpConfigGroup.WaitingTimeUpdateDuringSpeedUp;

/**
* @author ikaddoura
*/

final class DrtSpeedUp implements PersonDepartureEventHandler, PersonEntersVehicleEventHandler, PersonArrivalEventHandler, StartupListener, BeforeMobsimListener, AfterMobsimListener, IterationStartsListener, IterationEndsListener, DrtRequestSubmittedEventHandler {
	private static final Logger log = Logger.getLogger(DrtSpeedUp.class);
		
	private final Map<Id<Person>, Double> person2drtDepTime = new HashMap<>();
	private final Map<Id<Person>, Double> personId2personEntersVehicleTime = new HashMap<>();
	private final Map<Id<Person>, Id<Link>> person2drtDepLinkId = new HashMap<>();
	
	private final List<Double> beelineInVehicleSpeeds = new ArrayList<>();
	private final List<Double> beelineFactors = new ArrayList<>();
	private final List<Double> waitingTimes = new ArrayList<>();
	
	private final Set<Id<Person>> dailyFeeCharged = new HashSet<>();
    
	private double currentBeelineFactorForDrtFare;
	private double currentAvgWaitingTime;
	private double currentAvgInVehicleBeelineSpeed;
	
	private final List<Tuple<Double, Double>> ridesPerVehicle2avgWaitingTime = new ArrayList<>();
	
    private DrtFareConfigGroup drtFareCfg;
	
	private boolean teleportDrtUsers = false;
    private final Map<Id<Person>, DrtRequestSubmittedEvent> lastRequestSubmission = new HashMap<>();
    
	private Scenario scenario;
	private EventsManager events;
	private DrtSpeedUpConfigGroup drtSpeedUpConfigGroup;

	private int drtTeleportationTripCounter;
	private int drtTripCounter;
	
	private final String mode;

	private FleetSpecification fleetSpecification;

	private boolean waitingTimeAdjustedDuringSpeedUp;

	public DrtSpeedUp(String mode,
			DrtSpeedUpConfigGroup drtSpeedUpConfigGroup,
			EventsManager events,
			Scenario scenario,
			FleetSpecification fleetSpecification) {	
		this.mode = mode;
		this.drtSpeedUpConfigGroup = drtSpeedUpConfigGroup;
		this.events = events;
		this.scenario = scenario;
		this.fleetSpecification = fleetSpecification;
	}

	@Override
    public void reset(int iteration) {
        dailyFeeCharged.clear();
      
        beelineInVehicleSpeeds.clear();
        beelineFactors.clear();
        waitingTimes.clear();
        
        person2drtDepTime.clear();
        person2drtDepLinkId.clear();
        personId2personEntersVehicleTime.clear();
     
        drtTripCounter = 0;
        drtTeleportationTripCounter = 0;
    }
	
	@Override
	public void notifyStartup(StartupEvent event) {
		
		for (DrtFareConfigGroup drtFareCfg : DrtFaresConfigGroup.get(scenario.getConfig()).getDrtFareConfigGroups()) {
			if (drtFareCfg.getMode().equals(mode)) {
				this.drtFareCfg = drtFareCfg;
			}
		}
		if (this.drtFareCfg == null) {
			throw new RuntimeException("Expecting a fare config group for " + mode + ". Aborting...");
		}
		
		// set some default params to start with		
		this.currentAvgWaitingTime = drtSpeedUpConfigGroup.getInitialWaitingTime();
		this.currentAvgInVehicleBeelineSpeed = drtSpeedUpConfigGroup.getInitialInVehicleBeelineSpeed();
		this.currentBeelineFactorForDrtFare = drtSpeedUpConfigGroup.getInitialBeelineFactorForDrtFare();
	}

	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		if (event.getIteration() < this.drtSpeedUpConfigGroup.getFractionOfIterationSwitchOn() * this.scenario.getConfig().controler().getLastIteration()
				|| event.getIteration() >= this.drtSpeedUpConfigGroup.getFractionOfIterationsSwitchOff() * this.scenario.getConfig().controler().getLastIteration()) {	
			// run the detailed drt simulation
			teleportDrtUsers = false;
			log.info("Drt speed up disabled. Simulating " + mode + " in iteration " + event.getIteration());

		} else {
			if (event.getIteration() % this.drtSpeedUpConfigGroup.getIntervalDetailedIteration() == 0) {
				// run the detailed drt simulation
				teleportDrtUsers = false;
				log.info("Simulating " + mode + " in iteration " + event.getIteration());
			} else {
				// teleport drt users
				teleportDrtUsers = true;
				log.info("Teleporting " + mode + " users in iteration " + event.getIteration() + "."
						+ " Current teleported mode speed: " +  this.currentAvgInVehicleBeelineSpeed + "."
						+ " Current waiting time: " +  this.currentAvgWaitingTime + "."
						+ " Current unshared ride beeline distance factor for fare calculation: " + this.currentBeelineFactorForDrtFare + ".");
			}
		}
		
		if (teleportDrtUsers) {
			this.scenario.getConfig().qsim().setNumberOfThreads(this.drtSpeedUpConfigGroup.getNumberOfThreadsForMobsimDuringSpeedUp());
		} else {
			this.scenario.getConfig().qsim().setNumberOfThreads(1);
		}
	}
	
	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		
		// print out some statistics
		log.info("Number of simulated " + mode + " trips: " + drtTripCounter);
		log.info("Number of teleported " + mode + " trips: " + drtTeleportationTripCounter);

		if (teleportDrtUsers == false && event.getIteration() >= drtSpeedUpConfigGroup.getFirstSimulatedDrtIterationToReplaceInitialDrtPerformanceParams()) {
			// update statstics
			double currentAvgInVehicleBeelineSpeed = beelineInVehicleSpeeds.stream().mapToDouble(val -> val).average().orElse(drtSpeedUpConfigGroup.getInitialInVehicleBeelineSpeed());
			log.info("Setting teleported mode speed for " + mode + "_teleportation to the average beeline speed: " + currentAvgInVehicleBeelineSpeed + " (previous value: " + this.currentAvgInVehicleBeelineSpeed + ")");
			this.currentAvgInVehicleBeelineSpeed = currentAvgInVehicleBeelineSpeed;
			
			double averageBeelineFactor = beelineFactors.stream().mapToDouble(val -> val).average().orElse(drtSpeedUpConfigGroup.getInitialBeelineFactorForDrtFare());			
			log.info("Setting unshared ride beeline distance factor for fare calculation of " + mode + "_teleportation to: " + averageBeelineFactor + " (previous value: " + this.currentBeelineFactorForDrtFare + ")");
			this.currentBeelineFactorForDrtFare = averageBeelineFactor;	
			
			double averageWaitingTime = waitingTimes.stream().mapToDouble(val -> val).average().orElse(drtSpeedUpConfigGroup.getInitialWaitingTime());			
			log.info("Setting waiting time for " + mode + "_teleportation to: " + averageWaitingTime + " (previous value: " + this.currentAvgWaitingTime + ")");
			this.currentAvgWaitingTime = averageWaitingTime;
			waitingTimeAdjustedDuringSpeedUp = false;
			
			if (drtSpeedUpConfigGroup.getWaitingTimeUpdateDuringSpeedUp() == WaitingTimeUpdateDuringSpeedUp.LinearRegression) {
				// store some additional statistics
				double fleetSize = fleetSpecification.getVehicleSpecifications().size();
				if (fleetSize < 1.) {
					throw new RuntimeException("Number of vehicles is less than 1 for drt mode "  + this.mode + ". Aborting..."); 
				}
				double ridesPerVehicle = this.drtTripCounter / fleetSize;
				ridesPerVehicle2avgWaitingTime.add(new Tuple<Double, Double>(ridesPerVehicle, currentAvgWaitingTime));
			}
		}
		
		if (drtSpeedUpConfigGroup.getWaitingTimeUpdateDuringSpeedUp() == WaitingTimeUpdateDuringSpeedUp.LinearRegression) {
			if (teleportDrtUsers == true && event.getIteration() >= drtSpeedUpConfigGroup.getFirstSimulatedDrtIterationToReplaceInitialDrtPerformanceParams()) {
				// update current average waiting time also during speed-up iterations
				
				SimpleRegression regression = new SimpleRegression();
				for (Tuple<Double, Double> x2y : ridesPerVehicle2avgWaitingTime) {
					regression.addData(x2y.getFirst(), x2y.getSecond());
				}

				log.info("Current data points for " + mode + ": "+ ridesPerVehicle2avgWaitingTime.toString());
				double currentFleetSize = fleetSpecification.getVehicleSpecifications().size();
				if (currentFleetSize < 1.) {
					throw new RuntimeException("Number of vehicles is less than 1 for drt mode "  + this.mode + ". Aborting..."); 
				}
				log.info("Current fleet size for " + mode + ": "+ currentFleetSize);
				double predictedWaitingTime = regression.predict(this.drtTeleportationTripCounter / currentFleetSize);
				log.info("Predicted average waiting time for " + mode + ": " + predictedWaitingTime);

				if (Double.isNaN(predictedWaitingTime)) {
					log.info("Not enough data points for linear regression. Don't update the average waiting time!");
				} else if (predictedWaitingTime <= 0.) {
					log.info("Predicted average waiting from linear regression is negative. Don't update the average waiting time! ");
				} else {
					log.info("Setting waiting time for " + mode + "_teleportation to: " + predictedWaitingTime + " (previous value: " + this.currentAvgWaitingTime + ")");
					this.currentAvgWaitingTime = predictedWaitingTime;
					this.waitingTimeAdjustedDuringSpeedUp = true;
				}				
			}
		}

	}

	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		int modifiedLegsCounter = 0;
		if (teleportDrtUsers) {
			// set mode to teleported drt mode
			for (Person person : scenario.getPopulation().getPersons().values()) {
				Plan selectedPlan = person.getSelectedPlan();
				
				for (PlanElement pE : selectedPlan.getPlanElements()) {
					
					if (pE instanceof Leg) {
						Leg leg = (Leg) pE;
						if (leg.getMode().equals(mode)) {
							
							leg.getAttributes().putAttribute("drtRoute", leg.getRoute());
							
							String routingMode = TripStructureUtils.getRoutingMode(leg);
							
							leg.setMode(mode + "_teleportation");
							
							// set also routing mode (we don't replan here anyway, but need to keep it)
							TripStructureUtils.setRoutingMode(leg, routingMode);
							
							Link startLink = this.scenario.getNetwork().getLinks().get(leg.getRoute().getStartLinkId());
							Link endLink = this.scenario.getNetwork().getLinks().get(leg.getRoute().getEndLinkId());
							
							final Coord fromActCoord = 	startLink.getCoord();
							Gbl.assertNotNull( fromActCoord );
							final Coord toActCoord = endLink.getCoord();
							Gbl.assertNotNull( toActCoord );
							double dist = CoordUtils.calcEuclideanDistance( fromActCoord, toActCoord );
							Route route = this.scenario.getPopulation().getFactory().getRouteFactories().createRoute(Route.class, startLink.getId(), endLink.getId());
							int travTime = (int) ( this.currentAvgWaitingTime + (dist / this.currentAvgInVehicleBeelineSpeed) );
							if (waitingTimeAdjustedDuringSpeedUp) {
								log.info("WaitingTimeAdjustedDuringSpeedUp");
							}
							route.setTravelTime(travTime);
							route.setDistance(dist);
							leg.setRoute(route);
							leg.setTravelTime(travTime);
							
							modifiedLegsCounter++;
						}
					}
				}
			}
			log.info("Number of legs changed from " + mode + " to " + mode + "_teleportation: " + modifiedLegsCounter);
		}
	}
	
	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		int modifiedLegsCounter = 0;
		if (teleportDrtUsers) {
			// set mode back to original drt mode
			for (Person person : scenario.getPopulation().getPersons().values()) {
				Plan selectedPlan = person.getSelectedPlan();
				for (PlanElement pE : selectedPlan.getPlanElements()) {		
					if (pE instanceof Leg) {
						Leg leg = (Leg) pE;
						if (leg.getMode().equals(mode + "_teleportation")) {
							String routingMode = TripStructureUtils.getRoutingMode(leg);
							
							leg.setMode(mode);
							
							// set also routing mode, setMode overwrote it with null
							TripStructureUtils.setRoutingMode(leg, routingMode);

							leg.setRoute((Route) leg.getAttributes().getAttribute("drtRoute"));
							leg.getAttributes().removeAttribute("drtRoute");					
							modifiedLegsCounter++;
						}
					}
				}				
			}
			log.info("Number of legs changed from " + mode + "_teleportation to " + mode + ": " + modifiedLegsCounter);
		}
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		if (event.getLegMode().equals(mode) || event.getLegMode().equals(mode + "_teleportation")) {
			
			Link depLink = this.scenario.getNetwork().getLinks().get(this.person2drtDepLinkId.get(event.getPersonId()));
			Link arrLink = this.scenario.getNetwork().getLinks().get(event.getLinkId());
			double beeline = NetworkUtils.getEuclideanDistance(depLink.getCoord(), arrLink.getCoord());
						
			if (event.getLegMode().equals(mode)) {
				// store the statistics

				drtTripCounter++;

				double inVehtime = event.getTime() - this.personId2personEntersVehicleTime.get(event.getPersonId());
				double waitingTime = this.personId2personEntersVehicleTime.get(event.getPersonId()) - this.person2drtDepTime.get(event.getPersonId());

				if (inVehtime > 0) this.beelineInVehicleSpeeds.add(beeline / inVehtime);		
				if (beeline > 0) this.beelineFactors.add(this.lastRequestSubmission.get(event.getPersonId()).getUnsharedRideDistance() / beeline);
				this.waitingTimes.add(waitingTime);
							
				this.personId2personEntersVehicleTime.remove(event.getPersonId());
			}	
			
			if (event.getLegMode().equals(mode + "_teleportation")) {
				// compute fares and throw money events
				
				drtTeleportationTripCounter++;
				
				double fare = 0.;
				if (!dailyFeeCharged.contains(event.getPersonId())) {
					dailyFeeCharged.add(event.getPersonId());
	                events.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), -drtFareCfg.getDailySubscriptionFee()));
	            }
				fare += drtFareCfg.getBasefare();
				fare += drtFareCfg.getDistanceFare_m() * this.currentBeelineFactorForDrtFare * beeline;
				double travelTime = event.getTime() - this.person2drtDepTime.get(event.getPersonId());
				double inVehicleTime = travelTime - this.currentAvgWaitingTime;
				fare += drtFareCfg.getTimeFare_h() * inVehicleTime / 3600.;
				if (fare < drtFareCfg.getMinFarePerTrip()) fare = drtFareCfg.getMinFarePerTrip();
	            events.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), -fare));			
			}
			
			this.person2drtDepLinkId.remove(event.getPersonId());
			this.person2drtDepTime.remove(event.getPersonId());
		}
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if (event.getLegMode().equals(mode) ||
				event.getLegMode().equals(mode + "_teleportation")) {
			this.person2drtDepTime.put(event.getPersonId(), event.getTime());
			this.person2drtDepLinkId.put(event.getPersonId(), event.getLinkId());
		}
	}
	
	@Override
    public void handleEvent(DrtRequestSubmittedEvent event) {
		if (event.getMode().equals(mode)) {
            this.lastRequestSubmission.put(event.getPersonId(), event);
        }
    }

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		if (this.person2drtDepTime.get(event.getPersonId()) != null) {
			this.personId2personEntersVehicleTime.put(event.getPersonId(), event.getTime());
		}	
	}
	
}

