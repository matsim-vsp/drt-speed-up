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

import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.controler.AbstractModule;

/**
* @author ikaddoura
*/

public class DrtSpeedUpModule extends AbstractModule {

	@Override
	public void install() {
		this.bind(DrtSpeedUp.class).asEagerSingleton();
		this.addControlerListenerBinding().to(DrtSpeedUp.class);
		this.addEventHandlerBinding().to(DrtSpeedUp.class);
	}

	public static void adjustConfig(Config config, String mode) {
		ModeRoutingParams modeParams = new ModeRoutingParams(mode + "_teleportation");
		modeParams.setBeelineDistanceFactor(1.0);
		modeParams.setTeleportedModeSpeed(2.7777778);
		config.plansCalcRoute().addModeRoutingParams(modeParams);
	
		ModeParams currentScoringParams = config.planCalcScore().getModes().get(mode);
		ModeParams scoringParamsTeleportedMode = new ModeParams(mode + "_teleportation");
		scoringParamsTeleportedMode.setConstant(currentScoringParams.getConstant());
		scoringParamsTeleportedMode.setDailyMonetaryConstant(currentScoringParams.getDailyMonetaryConstant());
		scoringParamsTeleportedMode.setDailyUtilityConstant(currentScoringParams.getDailyUtilityConstant());
		scoringParamsTeleportedMode.setMarginalUtilityOfDistance(currentScoringParams.getMarginalUtilityOfDistance());
		scoringParamsTeleportedMode.setMarginalUtilityOfTraveling(currentScoringParams.getMarginalUtilityOfTraveling());
		scoringParamsTeleportedMode.setMonetaryDistanceRate(currentScoringParams.getMonetaryDistanceRate());
		config.planCalcScore().addModeParams(scoringParamsTeleportedMode);
	}

}

