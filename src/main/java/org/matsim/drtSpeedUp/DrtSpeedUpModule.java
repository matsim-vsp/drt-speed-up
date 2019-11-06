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

import org.apache.log4j.Logger;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Inject;

/**
* @author ikaddoura
*/

public class DrtSpeedUpModule extends AbstractModule {
	private static final Logger log = Logger.getLogger(DrtSpeedUpModule.class);
	
	@Inject
	private DrtSpeedUpConfigGroup drtSpeedUpConfigGroup;

	@Override
	public void install() {

		DrtSpeedUp speedUp = new DrtSpeedUp(drtSpeedUpConfigGroup.getMode());
		
		this.addControlerListenerBinding().toInstance(speedUp);
		this.addEventHandlerBinding().toInstance(speedUp);
		
//        addRoutingModuleBinding(drtSpeedUpConfigGroup.getMode() + "_teleportation").toProvider(new SpeedUpTeleportationRoutingModuleProvider(drtSpeedUpConfigGroup.getMode() + "_teleportation", speedUp));
	}

	public static void adjustConfig(Config config) {
		String mode = ConfigUtils.addOrGetModule(config, DrtSpeedUpConfigGroup.class).getMode();
				
		log.info("Adding scoring parameters for mode " + mode + "...");
		
		ModeParams originalScoringParams = config.planCalcScore().getModes().get(mode);
		ModeParams scoringParamsFakeMode = new ModeParams(mode + "_teleportation");
		scoringParamsFakeMode.setConstant(originalScoringParams.getConstant());
		scoringParamsFakeMode.setDailyMonetaryConstant(originalScoringParams.getDailyMonetaryConstant());
		scoringParamsFakeMode.setDailyUtilityConstant(originalScoringParams.getDailyUtilityConstant());
		scoringParamsFakeMode.setMarginalUtilityOfDistance(originalScoringParams.getMarginalUtilityOfDistance());
		scoringParamsFakeMode.setMarginalUtilityOfTraveling(originalScoringParams.getMarginalUtilityOfTraveling());
		scoringParamsFakeMode.setMonetaryDistanceRate(originalScoringParams.getMonetaryDistanceRate());
		config.planCalcScore().addModeParams(scoringParamsFakeMode);
	}

}

