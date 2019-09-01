package LinkPersonBasedScoring;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.testcases.MatsimTestUtils;

import static org.junit.Assert.*;

public class CLRunBicycleExampleTest{
	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;

	@Test
	public void testWithStandardSetup(){
		Config config = CLRunBicycleExample.prepareConfig() ;
		config.controler().setOutputDirectory( utils.getOutputDirectory() );
		config.controler().setLastIteration( 1 );

		Scenario scenario = CLRunBicycleExample.prepareScenario( config ) ;

		Controler controler = CLRunBicycleExample.prepareControler( scenario ) ;

		controler.addOverridingModule( new AbstractModule(){
			@Override public void install(){
				this.addControlerListenerBinding().toInstance( new ShutdownListener(){
					@Override public void notifyShutdown( ShutdownEvent event ) {
						for( Person person : scenario.getPopulation().getPersons().values() ){
							for( Plan plan : person.getPlans() ){
								boolean isUsingLink3 = false ;
								boolean isUsingLink6 = false ;
								boolean isUsingLink9 = false ;
								for( Leg leg : TripStructureUtils.getLegs( plan ) ){
									Route route = leg.getRoute();;
									if ( route instanceof NetworkRoute ) {
										if ( ((NetworkRoute) route).getLinkIds().contains( Id.createLinkId(6) ) ) {
											isUsingLink6=true ;
											break ;
										} else if ( ((NetworkRoute) route).getLinkIds().contains( Id.createLinkId( 9 ) ) ) {
											isUsingLink9 = true ;
											break ;
										} else if ( ((NetworkRoute) route).getLinkIds().contains( Id.createLinkId( 3 ) ) ) {
											isUsingLink3 = true ;
											break ;
										}
									}
								}
								if ( isUsingLink6 ){
									Assert.assertEquals( 48.5 , plan.getScore() , 0.5 );
								} else if ( isUsingLink9 || isUsingLink3) {
									Assert.assertEquals( 51.0 , plan.getScore() , 0.5 );
								} else {
									Assert.assertEquals( 52.5, plan.getScore(), 0.5 );
								}
							}
						}
					}
				} );
			}
		} ) ;

		controler.run() ;
	}
}
