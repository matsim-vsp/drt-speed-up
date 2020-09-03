/* *********************************************************************** *
 * project: org.matsim.*
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

import org.apache.log4j.Logger;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Inject;

/**
 * @author ikaddoura
 */

public class MultiModeDrtSpeedUpModule extends AbstractModule {

	private static final Logger log = Logger.getLogger(MultiModeDrtSpeedUpModule.class);

	@Inject
	private DrtSpeedUpConfigGroup drtSpeedUpConfigGroup;

	@Inject
	private QSimConfigGroup qSimConfigGroup;

	@Override
	public void install() {	
		for (String mode : drtSpeedUpConfigGroup.getModes().split(",")) {
			install(new DrtSpeedUpModule(mode, qSimConfigGroup.getNumberOfThreads()));
		}
	}
	
	public static void addTeleportedDrtMode(Config config) {
		String modes = ConfigUtils.addOrGetModule(config, DrtSpeedUpConfigGroup.class).getModes();
		MultiModeDrtConfigGroup multiModeDrtConfig = ConfigUtils.addOrGetModule(config, MultiModeDrtConfigGroup.class);
				
		for (String mode : modes.split(",")) {
			log.info("Adding scoring parameters for mode " + mode + "...");

			// copy scoring parameters separately for each subpopulation if subpopulations are present
			for (PlanCalcScoreConfigGroup.ScoringParameterSet subpopulationScoringParams: config.planCalcScore().getScoringParametersPerSubpopulation().values()) {
				ModeParams subpopulationOriginalScoringParams = subpopulationScoringParams.getModes().get(mode);
				if (subpopulationOriginalScoringParams == null) {
					log.warn("No scoring parameters for mode " + mode + " in subpopulation " + subpopulationScoringParams.getName());
					continue;
				}

				ModeParams subpopulationScoringParamsFakeMode = new ModeParams(mode + "_teleportation");
				subpopulationScoringParamsFakeMode.setConstant(subpopulationOriginalScoringParams.getConstant());
				subpopulationScoringParamsFakeMode.setDailyMonetaryConstant(subpopulationOriginalScoringParams.getDailyMonetaryConstant());
				subpopulationScoringParamsFakeMode.setDailyUtilityConstant(subpopulationOriginalScoringParams.getDailyUtilityConstant());
				subpopulationScoringParamsFakeMode.setMarginalUtilityOfDistance(subpopulationOriginalScoringParams.getMarginalUtilityOfDistance());
				subpopulationScoringParamsFakeMode.setMarginalUtilityOfTraveling(subpopulationOriginalScoringParams.getMarginalUtilityOfTraveling());
				subpopulationScoringParamsFakeMode.setMonetaryDistanceRate(subpopulationOriginalScoringParams.getMonetaryDistanceRate());

				subpopulationScoringParams.addModeParams(subpopulationScoringParamsFakeMode);
			}

			// set speed up mode (used for drt speed up mode demand aggregation for rebalancing)
			for (DrtConfigGroup drtConfig: multiModeDrtConfig.getModalElements()) {
				if (drtConfig.getMode().equals(mode)) {
					drtConfig.setDrtSpeedUpMode(mode + "_teleportation");
				}
			}
		}
	}
}

