
/* *********************************************************************** *
 * project: org.matsim.*
 * BeelineTeleportationRouting.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
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

import javax.inject.Inject;
import javax.inject.Provider;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.router.DefaultRoutingModules;
import org.matsim.core.router.RoutingModule;

class FlexibleBeelineTeleportationRouting implements Provider<RoutingModule> {

	private final DrtSpeedUp speedUp;
	private final String mode;

	public FlexibleBeelineTeleportationRouting(String mode, DrtSpeedUp speedUp) {
		this.speedUp = speedUp;
		this.mode = mode;
	}

	@Inject
	private Scenario scenario ;

	@Override
	public RoutingModule get() {
		return DefaultRoutingModules.createTeleportationRouter(mode, scenario, speedUp.getModeParams());
	}
}
