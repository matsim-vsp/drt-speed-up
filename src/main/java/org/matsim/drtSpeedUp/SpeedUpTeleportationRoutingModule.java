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

import java.util.Arrays;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.FacilityWrapperActivity;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.Facility;

/**
* @author ikaddoura
*/

class SpeedUpTeleportationRoutingModule implements RoutingModule {

	private final String mode;
	private final Scenario scenario;
	private final DrtSpeedUp speedUp;

	SpeedUpTeleportationRoutingModule(String mode, Scenario scenario, DrtSpeedUp speedUp) {
		this.mode = mode;
		this.scenario = scenario;
		this.speedUp = speedUp;
	}

	@Override
	public List<? extends PlanElement> calcRoute(
			final Facility fromFacility,
			final Facility toFacility,
			final double departureTime,
			final Person person) {
		Leg newLeg = this.scenario.getPopulation().getFactory().createLeg( this.mode );
		newLeg.setDepartureTime( departureTime );

		double travTime = routeLeg(
				person,
				newLeg,
				new FacilityWrapperActivity( fromFacility ),
				new FacilityWrapperActivity( toFacility ),
				departureTime);

		// otherwise, information may be lost
		newLeg.setTravelTime( travTime );

		return Arrays.asList(newLeg);
	}

	@Override
	public String toString() {
		return "[TeleportationRoutingModule: mode="+this.mode+"]";
	}


	private double routeLeg(Person person, Leg leg, Activity fromAct, Activity toAct, double depTime) {
		final Coord fromActCoord = 	PopulationUtils.decideOnCoordForActivity( fromAct, scenario) ;
		Gbl.assertNotNull( fromActCoord );
		final Coord toActCoord = PopulationUtils.decideOnCoordForActivity( toAct, scenario ) ;
		Gbl.assertNotNull( toActCoord );
		double dist = CoordUtils.calcEuclideanDistance( fromActCoord, toActCoord );
		// create an empty route, but with realistic travel time
		Route route = this.scenario.getPopulation().getFactory().getRouteFactories().createRoute(Route.class, fromAct.getLinkId(), toAct.getLinkId());
		int travTime = (int) ( this.speedUp.getCurrentAvgWaitingTime() + (dist / this.speedUp.getCurrentAvgInVehicleBeelineSpeed()) );
		route.setTravelTime(travTime);
		route.setDistance(dist);
		leg.setRoute(route);
		leg.setDepartureTime(depTime);
		leg.setTravelTime(travTime);
		return travTime;
	}

}

