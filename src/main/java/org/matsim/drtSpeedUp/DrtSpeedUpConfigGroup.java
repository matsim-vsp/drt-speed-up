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

import org.matsim.core.config.ReflectiveConfigGroup;

/**
 * 
 * @author ikaddoura
 */

public class DrtSpeedUpConfigGroup extends ReflectiveConfigGroup {
	public static final String GROUP_NAME = "drtSpeedUp" ;

	private static final String MODE = "mode";
	private static final String SWITCH_OFF_FRACTION_ITERATION = "fractionOfIterationsSwitchOff";
	private static final String SWITCH_ON_FRACTION_ITERATION = "fractionOfIterationSwitchOn";
	private static final String DETAILED_ITERATION_INTERVAL = "intervalDetailedIteration";

	public DrtSpeedUpConfigGroup() {
		super(GROUP_NAME);
	}
	
	private String mode = "drt";
	private double fractionOfIterationsSwitchOff = 0.6;
	private double fractionOfIterationSwitchOn = 0.01;
	private int intervalDetailedIteration = 10;

	@StringGetter( MODE )
	public String getMode() {
		return mode;
	}
	
	@StringSetter( MODE )
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	@StringGetter( SWITCH_OFF_FRACTION_ITERATION )
	public double getFractionOfIterationsSwitchOff() {
		return fractionOfIterationsSwitchOff;
	}
	
	@StringSetter( SWITCH_OFF_FRACTION_ITERATION )
	public void setFractionOfIterationsSwitchOff(double fractionOfIterationsSwitchOff) {
		this.fractionOfIterationsSwitchOff = fractionOfIterationsSwitchOff;
	}
	
	@StringGetter( SWITCH_ON_FRACTION_ITERATION )
	public double getFractionOfIterationSwitchOn() {
		return fractionOfIterationSwitchOn;
	}
	
	@StringSetter( SWITCH_ON_FRACTION_ITERATION )
	public void setFractionOfIterationSwitchOn(double fractionOfIterationSwitchOn) {
		this.fractionOfIterationSwitchOn = fractionOfIterationSwitchOn;
	}
	
	@StringGetter( DETAILED_ITERATION_INTERVAL )
	public int getIntervalDetailedIteration() {
		return intervalDetailedIteration;
	}
	
	@StringSetter( DETAILED_ITERATION_INTERVAL )
	public void setIntervalDetailedIteration(int intervalDetailedIteration) {
		this.intervalDetailedIteration = intervalDetailedIteration;
	}
			
}

