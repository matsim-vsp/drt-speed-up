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

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dvrp.fleet.FleetSpecification;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.core.api.experimental.events.EventsManager;

import com.google.inject.Inject;

/**
* @author ikaddoura
*/

class DrtSpeedUpModule extends AbstractDvrpModeModule {
	
	private final String mode;
	private final int numberOfQsimThreads;
	
	protected DrtSpeedUpModule(String mode, int numberOfQsimThreads) {
		super(mode);
		this.mode = mode;
		this.numberOfQsimThreads = numberOfQsimThreads;
	}
	
	@Inject
	private DrtSpeedUpConfigGroup drtSpeedUpConfigGroup;

	@Override
	public void install() {
		bindModal(DrtSpeedUp.class).toProvider(modalProvider(
				getter -> new DrtSpeedUp(mode,
						drtSpeedUpConfigGroup,
						numberOfQsimThreads,
						getter.get(EventsManager.class),
						getter.get(Scenario.class),
						getter.getModal(FleetSpecification.class)))).asEagerSingleton();
					
		addControlerListenerBinding().toProvider(modalProvider(
				getter -> getter.getModal(DrtSpeedUp.class)));
		
		addEventHandlerBinding().toProvider(modalProvider(
				getter -> getter.getModal(DrtSpeedUp.class)));
	}

}

