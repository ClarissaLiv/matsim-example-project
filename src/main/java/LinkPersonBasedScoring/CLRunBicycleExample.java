/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.bicycle.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelActivityScoring;
import org.matsim.core.scoring.functions.CharyparNagelAgentStuckScoring;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dziemke
 */
class CLRunBicycleExample{
	private static final Logger LOG = Logger.getLogger( CLRunBicycleExample.class );

	// the program needs to do the following:
	// (1) assign values to population members
	// (2) somehow read in these values
	// (3) somehow assign them to the scoring function


	public static void main(String[] args) {

		Config config = prepareConfig();

		// ---

		Scenario scenario = prepareScenario( config );

		// ---

		Controler controler = prepareControler( scenario );

		// ---

		controler.run();
	}

	static Controler prepareControler( Scenario scenario ){
		Controler controler = new Controler(scenario);
		controler.addOverridingModule( new AbstractModule(){

			@Override
			public void install() {

				// A travel time that uses BicycleLinkSpeedCalculator, but is not sensitive to congestion:
				addTravelTimeBinding("bicycle").to( BicycleTravelTime.class ).in( Singleton.class ) ;

				// the default BicycleLinkSpeedCalculator has speed dependent on surface.  CL does not want this (?):
				bind( BicycleLinkSpeedCalculator.class ).toInstance( new BicycleLinkSpeedCalculator(){
					@Override public double getMaximumVelocityForLink( Link link ){
						return link.getFreespeed() ;
					}
					@Override public double getMaximumVelocity( QVehicle vehicle , Link link , double time ){
						return link.getFreespeed() ;
					}
				} );

				// use default travel disutility.  This may not work forever, but for the time being we hope that the randomizing router is enough.
				addTravelDisutilityFactoryBinding("bicycle").to( BicycleTravelDisutilityFactory.class ).in( Singleton.class ) ;

				// use CL leg scoring:
				bindScoringFunctionFactory().toInstance( new ScoringFunctionFactory(){
					@Inject ScoringParametersForPerson parameters;
					@Inject Scenario scenario;

					@Override
					public ScoringFunction createNewScoringFunction( Person person ) {
						SumScoringFunction sumScoringFunction = new SumScoringFunction();

						final ScoringParameters params = parameters.getScoringParameters(person );
						sumScoringFunction.addScoringFunction(new CharyparNagelActivityScoring(params) ) ;
						sumScoringFunction.addScoringFunction(new CharyparNagelAgentStuckScoring(params) );

						CLBicycleConfigGroup bicycleConfigGroup = ConfigUtils.addOrGetModule( scenario.getConfig() , CLBicycleConfigGroup.class );;
						Gbl.assertIf( bicycleConfigGroup.getBicycleScoringType()== CLBicycleConfigGroup.BicycleScoringType.legBased );
						// the other execution paths do not exist in this implementation.  kai, sep'19

						sumScoringFunction.addScoringFunction(new CLBicycleLegScoring(params, person , scenario ) );

						return sumScoringFunction;
					}

				} );
			}
		} );
		controler.addOverridingQSimModule(new BicycleQSimModule());
		return controler;
	}

	static Scenario prepareScenario( Config config ){
		Scenario scenario = ScenarioUtils.loadScenario( config );
		for( Link link : scenario.getNetwork().getLinks().values() ){
			link.getAttributes().putAttribute( BicycleUtils.BICYCLE_INFRASTRUCTURE_SPEED_FACTOR, 1. ) ;
			// (needed in 12.x. kai, sep'19)
		}
		{
			VehicleType car = VehicleUtils.getFactory().createVehicleType( Id.create( TransportMode.car , VehicleType.class ) );
			scenario.getVehicles().addVehicleType( car );
		}
		{
			VehicleType bicycle = VehicleUtils.getFactory().createVehicleType( Id.create( "bicycle" , VehicleType.class ) );
			bicycle.setMaximumVelocity( 20.0 / 3.6 );
			bicycle.setPcuEquivalents( 0.25 );
			scenario.getVehicles().addVehicleType( bicycle );
		}
//		CLAssignPersonAttributes.main(scenario ) ;
		return scenario;
	}

	static Config prepareConfig(){
		Config config = ConfigUtils.createConfig( "TestParamInput/" ) ;
		config.network().setInputFile( "network_centerCobblestone.xml.gz" );
		config.plans().setInputFile( "population_1200.xml" );

		config.controler().setLastIteration(100);

		config.global().setNumberOfThreads(1);
		config.controler().setOverwriteFileSetting( OverwriteFileSetting.deleteDirectoryIfExists );

		config.plansCalcRoute().setRoutingRandomness(3.);

		config.qsim().setVehiclesSource( QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData );

		config.controler().setWriteEventsInterval(1 );

		BicycleConfigGroup bicycleConfigGroup = ConfigUtils.addOrGetModule( config, BicycleConfigGroup.class ) ;

		CLBicycleConfigGroup clBicycleConfigGroup = ConfigUtils.addOrGetModule( config , CLBicycleConfigGroup.class );

		clBicycleConfigGroup.setMarginalUtilityOfInfrastructure_m( -0.0002 );
		clBicycleConfigGroup.setMarginalUtilityOfComfort_m(  -0.0002 );
		clBicycleConfigGroup.setMarginalUtilityOfGradient_m_100m( -0.02 );
		clBicycleConfigGroup.setBetaCobblestone( -10. );

		List<String> mainModeList = new ArrayList<>();
		mainModeList.add("bicycle");
		mainModeList.add( TransportMode.car );
		config.qsim().setMainModes(mainModeList );

		{
			StrategySettings strategySettings = new StrategySettings();
			strategySettings.setStrategyName("ChangeExpBeta");
			strategySettings.setWeight(0.8);
			config.strategy().addStrategySettings(strategySettings );
		}
		{
			StrategySettings strategySettings = new StrategySettings();
			strategySettings.setStrategyName("ReRoute");
			strategySettings.setWeight(0.2);
			config.strategy().addStrategySettings(strategySettings );
		}
		{
			ActivityParams homeActivity = new ActivityParams( "home" );
			homeActivity.setTypicalDuration( 12 * 60 * 60 );
			config.planCalcScore().addActivityParams( homeActivity );
		}
		{
			ActivityParams workActivity = new ActivityParams( "work" );
			workActivity.setTypicalDuration( 8 * 60 * 60 );
			config.planCalcScore().addActivityParams( workActivity );
		}
		{
			ModeParams bicycle1 = new ModeParams( "bicycle" );
			bicycle1.setConstant( 0. );
			bicycle1.setMarginalUtilityOfDistance( -0.0004 ); // util/m
			bicycle1.setMarginalUtilityOfTraveling( -6.0 ); // util/h
			bicycle1.setMonetaryDistanceRate( 0. );
			config.planCalcScore().addModeParams( bicycle1 );
		}
		config.plansCalcRoute().setNetworkModes(mainModeList );
		return config;
	}

}
