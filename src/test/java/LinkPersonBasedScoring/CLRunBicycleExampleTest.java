package LinkPersonBasedScoring;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
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
import org.matsim.core.gbl.Gbl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.testcases.MatsimTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
						List<Id<Link>> linkIdsToTest = new ArrayList<>() ;
						for ( int ii=2 ; ii<=10 ; ii++ ) {
							linkIdsToTest.add( Id.createLinkId( ii ) ) ;
						}
						for( Person person : scenario.getPopulation().getPersons().values() ){
							for( Plan plan : person.getPlans() ){
								Id<Link> usedLinkId = null ;
								for( Leg leg : TripStructureUtils.getLegs( plan ) ){
									Route route = leg.getRoute();;
									if ( route instanceof NetworkRoute ) {
										for( Id<Link> linkId : linkIdsToTest ){
											if ( ((NetworkRoute) route).getLinkIds().contains( linkId ) ) {
												usedLinkId = linkId ;
												break ;
											}
										}
									}
								}
								Gbl.assertNotNull( usedLinkId );
								switch( usedLinkId.toString() ) {
									case "2":
									case "10":
										Assert.fail(  );
										break ;
									case "3":
									case "9":
										Assert.assertEquals( 83.4, plan.getScore(), 0.5 );
										break ;
									case "4":
									case "8":
										Assert.assertEquals( 84.0, plan.getScore(), 0.5 );
										break ;
									case "5":
									case "7":
										Assert.assertEquals( 84.4, plan.getScore(), 0.5 );
										break ;
									case "6":
										Assert.assertEquals( 80.2, plan.getScore(), 0.5 );
										break ;
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
