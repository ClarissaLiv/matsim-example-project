package org.matsim.contrib.bicycle;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;

public interface NotInUseBicycleUtilityUtils {
	
	public static double computeLinkBasedScore(Link link, Leg leg, BicycleConfigGroup bicycleConfigGroup, Person person, double marginalUtilityOfComfort_m,
			double marginalUtilityOfInfrastructure_m, double marginalUtilityOfGradient_m_100m) {
		double infrastructureDisutility;
		double comfortDisutility;
		double gradientDisutility;
		
		return (infrastructureDisutility + comfortDisutility + gradientDisutility);
	}
	
	public static double getGradientFactor(Link link) {
		return gradient;
	}
	public static double getComfortFactor(String surface, String type) {
		return comfortFactor;
	}	
	
	public static double getInfrastructureFactor(String type, String cyclewaytype) {
		return infrastructureFactor;
	}
}
