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

	private static final String MODES = "modes";
	private static final String SWITCH_OFF_FRACTION_ITERATION = "fractionOfIterationsSwitchOff";
	private static final String SWITCH_ON_FRACTION_ITERATION = "fractionOfIterationSwitchOn";
	private static final String DETAILED_ITERATION_INTERVAL = "intervalDetailedIteration";
	private static final String NUMBER_OF_THREADS_FOR_MOBSIM_DURING_SPEED_UP = "numberOfThreadsForMobsimDuringSpeedUp";
	private static final String INITIAL_WAITING_TIME = "initialWaitingTime";
	private static final String INITIAL_IN_VEHICLE_BEELINE_SPEED = "initialInVehicleBeelineSpeed";
	private static final String INITIAL_BEELINE_FACTOR_DRT_FARE = "initialBeelineFactorForDrtFare";
	private static final String FIRST_SIMULATED_DRT_ITERATION_TO_REPLACE_INITIAL_DRT_PERFORMANCE_PARAMS = "firstSimulatedDrtIterationToReplaceInitialDrtPerformanceParams";
	private static final String WAITING_TIME_UPDATE_DURING_SPEED_UP = "waitingTimeUpdateDuringSpeedUp";
	private static final String MOVING_AVERAGE_SIZE = "movingAverageSize";

	public DrtSpeedUpConfigGroup() {
		super(GROUP_NAME);
	}
	
	private String modes = "drt";
	private double fractionOfIterationsSwitchOff = 0.99;
	private double fractionOfIterationSwitchOn = 0.;
	private int intervalDetailedIteration = 10;
	private int numberOfThreadsForMobsimDuringSpeedUp = 4;
	
	private double initialWaitingTime = 900.;
	private double initialInVehicleBeelineSpeed = 4.16667;
	private double initialBeelineFactorForDrtFare = 1.5;
	private int firstSimulatedDrtIterationToReplaceInitialDrtPerformanceParams = 0;
	private WaitingTimeUpdateDuringSpeedUp waitingTimeUpdateDuringSpeedUp = WaitingTimeUpdateDuringSpeedUp.Disabled;
	private int movingAverageSize = 1;

	public enum WaitingTimeUpdateDuringSpeedUp {
		Disabled, LinearRegression
	}

	@StringGetter( MODES )
	public String getModes() {
		return modes;
	}
	
	@StringSetter( MODES )
	public void setModes(String modes) {
		this.modes = modes;
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

	@StringGetter( NUMBER_OF_THREADS_FOR_MOBSIM_DURING_SPEED_UP )
	public int getNumberOfThreadsForMobsimDuringSpeedUp() {
		return numberOfThreadsForMobsimDuringSpeedUp;
	}

	@StringSetter( NUMBER_OF_THREADS_FOR_MOBSIM_DURING_SPEED_UP )
	public void setNumberOfThreadsForMobsimDuringSpeedUp(int numberOfThreadsForMobsimDuringSpeedUp) {
		this.numberOfThreadsForMobsimDuringSpeedUp = numberOfThreadsForMobsimDuringSpeedUp;
	}

	@StringGetter( INITIAL_WAITING_TIME )
	public double getInitialWaitingTime() {
		return initialWaitingTime;
	}

	@StringSetter( INITIAL_WAITING_TIME )
	public void setInitialWaitingTime(double initialWaitingTime) {
		this.initialWaitingTime = initialWaitingTime;
	}

	@StringGetter( INITIAL_IN_VEHICLE_BEELINE_SPEED )
	public double getInitialInVehicleBeelineSpeed() {
		return initialInVehicleBeelineSpeed;
	}

	@StringSetter( INITIAL_IN_VEHICLE_BEELINE_SPEED )
	public void setInitialInVehicleBeelineSpeed(double initialInVehicleBeelineSpeed) {
		this.initialInVehicleBeelineSpeed = initialInVehicleBeelineSpeed;
	}

	@StringGetter( INITIAL_BEELINE_FACTOR_DRT_FARE )
	public double getInitialBeelineFactorForDrtFare() {
		return initialBeelineFactorForDrtFare;
	}

	@StringSetter( INITIAL_BEELINE_FACTOR_DRT_FARE )
	public void setInitialBeelineFactorForDrtFare(double initialBeelineFactorForDrtFare) {
		this.initialBeelineFactorForDrtFare = initialBeelineFactorForDrtFare;
	}

	@StringGetter( FIRST_SIMULATED_DRT_ITERATION_TO_REPLACE_INITIAL_DRT_PERFORMANCE_PARAMS )
	public int getFirstSimulatedDrtIterationToReplaceInitialDrtPerformanceParams() {
		return firstSimulatedDrtIterationToReplaceInitialDrtPerformanceParams;
	}

	@StringSetter( FIRST_SIMULATED_DRT_ITERATION_TO_REPLACE_INITIAL_DRT_PERFORMANCE_PARAMS )
	public void setFirstSimulatedDrtIterationToReplaceInitialDrtPerformanceParams(int firstSimulatedDrtIterationToReplaceInitialDrtPerformanceParams) {
		this.firstSimulatedDrtIterationToReplaceInitialDrtPerformanceParams = firstSimulatedDrtIterationToReplaceInitialDrtPerformanceParams;
	}

	@StringGetter( WAITING_TIME_UPDATE_DURING_SPEED_UP )
	public WaitingTimeUpdateDuringSpeedUp getWaitingTimeUpdateDuringSpeedUp() {
		return waitingTimeUpdateDuringSpeedUp;
	}

	@StringSetter( WAITING_TIME_UPDATE_DURING_SPEED_UP )
	public void setWaitingTimeUpdateDuringSpeedUp(WaitingTimeUpdateDuringSpeedUp waitingTimeUpdateDuringSpeedUp) {
		this.waitingTimeUpdateDuringSpeedUp = waitingTimeUpdateDuringSpeedUp;
	}

	@StringGetter( MOVING_AVERAGE_SIZE )
	public int getMovingAverageSize() {
		return movingAverageSize;
	}

	@StringSetter( MOVING_AVERAGE_SIZE )
	public void setMovingAverageSize(int movingAverageSize) {
		this.movingAverageSize = movingAverageSize;
	}
			
}

