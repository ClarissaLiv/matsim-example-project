/* *********************************************************************** *
 * project: org.matsim.*                                                   *
 * BicycleLegScoring.java                                                  *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package LinkPersonBasedScoring;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.bicycle.BicycleConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelLegScoring;
import org.matsim.core.scoring.functions.ScoringParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dziemke
 */
class CLBicycleLegScoring implements SumScoringFunction.LegScoring, SumScoringFunction.ArbitraryEventScoring {
	private static final Logger log = Logger.getLogger(CLBicycleLegScoring.class );

	private final CharyparNagelLegScoring delegate ;

	private final double marginalUtilityOfInfrastructure_m;
	private final double marginalUtilityOfComfort_m;
	private final double marginalUtilityOfGradient_m_100m;
	private final BicycleConfigGroup bicycleConfigGroup;
	private final double pavementComfortFactorCobblestoneAG2;
	private final Person person;
	private final CLBicycleConfigGroup clBicycleConfigGroup;
	private final Network network;
	private double additionalScore;

	CLBicycleLegScoring( final ScoringParameters params , Person person , Scenario scenario ) {
		delegate = new CharyparNagelLegScoring( params, scenario.getNetwork() ) ;

		Config config = scenario.getConfig() ;
		this.bicycleConfigGroup = ConfigUtils.addOrGetModule( config, BicycleConfigGroup.class ) ;
		this.marginalUtilityOfInfrastructure_m = bicycleConfigGroup.getMarginalUtilityOfInfrastructure_m();
		this.marginalUtilityOfComfort_m = bicycleConfigGroup.getMarginalUtilityOfComfort_m();
		this.marginalUtilityOfGradient_m_100m = bicycleConfigGroup.getMarginalUtilityOfGradient_m_100m();
		this.clBicycleConfigGroup = ConfigUtils.addOrGetModule( config, CLBicycleConfigGroup.class ) ;
		this.pavementComfortFactorCobblestoneAG2 = clBicycleConfigGroup.getBetaCobblestoneAgeGroup2();
		this.person = person;
		this.network = scenario.getNetwork() ;
	
	}

	@Override
	public void handleLeg(Leg leg) {
		delegate.handleLeg( leg ) ;

		NetworkRoute networkRoute = (NetworkRoute) leg.getRoute();

		List<Id<Link>> linkIds = new ArrayList<>( networkRoute.getLinkIds() );
		linkIds.add(networkRoute.getEndLinkId());

		// Iterate over all links of the route
		double legScore = 0. ;
		for (Id<Link> linkId : linkIds) {
			double scoreOnLink = CLBicycleUtilityUtils.computeLinkBasedScore(network.getLinks().get(linkId ), leg , person ,
				  clBicycleConfigGroup );
			// LOG.warn("----- link = " + linkId + " -- scoreOnLink = " + scoreOnLink);
			legScore += scoreOnLink;
		}
		this.additionalScore += legScore;
	}


	@Override public void finish(){
		delegate.finish();
	}
	@Override public double getScore(){
		return this.additionalScore + delegate.getScore();
	}
	@Override public void handleEvent( Event event ){
		delegate.handleEvent( event );
	}
}
