package org.matsim.drtSpeedUp;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.testcases.MatsimTestUtils;

/**
 * 
 * @author ikaddoura
 *
 */
public class DrtSpeedUpTest {
		
	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;
	
	@Test
	public final void test1() {	
		List<Double> list = new ArrayList<>();
		list.add(2.);
		list.add(5.);
		list.add(22.);
		
		Assert.assertEquals("Wrong moving average.", 29./3., DrtSpeedUp.computeMovingAverage(3, list), MatsimTestUtils.EPSILON);
		Assert.assertEquals("Wrong moving average.", 27./2., DrtSpeedUp.computeMovingAverage(2, list), MatsimTestUtils.EPSILON);
		Assert.assertEquals("Wrong moving average.", 29./3., DrtSpeedUp.computeMovingAverage(4, list), MatsimTestUtils.EPSILON);
	}

}
