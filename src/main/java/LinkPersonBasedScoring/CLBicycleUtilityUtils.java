package LinkPersonBasedScoring;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.bicycle.BicycleConfigGroup;
import org.matsim.contrib.bicycle.BicycleUtils;
import org.matsim.core.network.NetworkUtils;

class CLBicycleUtilityUtils{
	private static final Logger log = Logger.getLogger( CLBicycleUtilityUtils.class );

	static double computeLinkBasedScore( Link link , Leg leg , Person person , CLBicycleConfigGroup clBicycleConfigGroup ) {

		double comfortDisutility = calculateLinkComfortDisutility(link, leg, clBicycleConfigGroup, person );

//Currently I do not wish to use the infrastructure and gradient disutilities, since these were not considered in my survey. clivings April 2019
//double infrastructureDisutility = calculateLinkInfrastructureDisutility(link, leg, bicycleConfigGroup, person, marginalUtilityOfInfrastructure_m);
//double gradientDisutility = calculateLinkGradientDisutility(link, leg, bicycleConfigGroup, person, marginalUtilityOfGradient_m_100m);
		double infrastructureDisutility = 0;
		double gradientDisutility = 0;

//Return the total disutility that is derived from link characteristics, excluding temporal characteristics, like interactions with cars
		return (infrastructureDisutility + comfortDisutility + gradientDisutility);
	}

	private static double calculateLinkComfortDisutility( Link link , Leg leg , CLBicycleConfigGroup clBicycleConfigGroup , Person person ) {
		String surface = (String) link.getAttributes().getAttribute( BicycleUtils.SURFACE );
		String type = NetworkUtils.getType( link ) ;

		double linkDistance = link.getLength();
		double legDistance = leg.getRoute().getDistance();

		double localPavementDummyCobblestone = getPavementDummyCobblestone(surface, type);

		double pavementComfortParameter = getPavementComfortParameter(surface, type, clBicycleConfigGroup );

		Integer ageGroup = (Integer) person.getAttributes().getAttribute( CLAssignPersonAttributes.AGE_GROUP_DUMMY_1 );
//		int ageGroupDummy2 = (int) person.getAttributes().getAttribute(AssignPersonAttributes.AGE_GROUP_DUMMY_2);
//		int ageGroupDummy3 = (int) person.getAttributes().getAttribute(AssignPersonAttributes.AGE_GROUP_DUMMY_3);
//		int ageGroupDummy4 = (int) person.getAttributes().getAttribute(AssignPersonAttributes.AGE_GROUP_DUMMY_4);
//		int ageGroupDummy5 = (int) person.getAttributes().getAttribute(AssignPersonAttributes.AGE_GROUP_DUMMY_5);
//		int ageGroupDummy6 = (int) person.getAttributes().getAttribute(AssignPersonAttributes.AGE_GROUP_DUMMY_6);
//TODO does this factor for ag2 and cobblestone need to be a method input? Like passed from the outside?
//double pavementComfortFactorCobblestoneAG2 = bicycleConfigGroup.getBetaCobblestoneAgeGroup2();

		double comfortDisutility = pavementComfortParameter*(linkDistance/legDistance);

//		if ( link.getId().toString().equals( "6" )) {
		if ( comfortDisutility!=0. ) {
			log.warn("comfortDisutility=" + comfortDisutility + "; linkId=" + link.getId() ) ;
		}

		if ( ageGroup==null ) {
			return comfortDisutility ;
		}

//		int ageGroupDummy2  = 0 ;

//debug
		if(ageGroup == 1) {
		}
//debug
		if(localPavementDummyCobblestone == 1 && ageGroup == 2) {
			log.warn("Found it!!" );
//			ageGroupDummy2=1 ;
		}
//debug
		if(link.getId().equals(Id.createLinkId("6"))) {
			log.warn("Found it!!" );
		}

// HOW I TRANSLATE OR PASS OR ENTER MY CHOICE MODEL TO THE SCORING FUNCTION - clivings April 2019
//		double comfortDisutility = pavementComfortParameter*(linkDistance/legDistance)+
//							     localPavementDummyCobblestone*ageGroupDummy2*pavementComfortFactorCobblestoneAG2*(linkDistance/legDistance);


		if ( ageGroup==2 && localPavementDummyCobblestone==1 ) {
//			comfortDisutility += localPavementDummyCobblestone*pavementComfortFactorCobblestoneAG2*(linkDistance/legDistance);
			comfortDisutility += 10 ;
		}



		return (comfortDisutility);
	}

	private static double getPavementComfortParameter( String surface , String type , CLBicycleConfigGroup bicycleConfigGroup ) {
		double pavementComfortFactor = 0.0;

		if (surface != null) {
			switch (surface) {
				case "paved": pavementComfortFactor = bicycleConfigGroup.getBetaAsphalt(); break;
				case "asphalt": pavementComfortFactor = bicycleConfigGroup.getBetaAsphalt(); break;
				case "cobblestone": pavementComfortFactor = bicycleConfigGroup.getBetaCobblestone(); break;
				case "cobblestone (bad)": pavementComfortFactor = bicycleConfigGroup.getBetaCobblestone(); break;
				case "sett": pavementComfortFactor = bicycleConfigGroup.getBetaFlagstones();; break;
				case "cobblestone;flattened":pavementComfortFactor = bicycleConfigGroup.getBetaCobblestone(); break;
				case "cobblestone:flattened": pavementComfortFactor = bicycleConfigGroup.getBetaCobblestone(); break;
				case "concrete": pavementComfortFactor = bicycleConfigGroup.getBetaAsphalt(); break;
				case "concrete:lanes": pavementComfortFactor = bicycleConfigGroup.getBetaAsphalt(); break;
				case "concrete_plates":pavementComfortFactor = bicycleConfigGroup.getBetaFlagstones(); break;
				case "concrete:plates": pavementComfortFactor = bicycleConfigGroup.getBetaFlagstones(); break;
				case "paving_stones": pavementComfortFactor = bicycleConfigGroup.getBetaFlagstones(); break;
				case "paving_stones:35": pavementComfortFactor = bicycleConfigGroup.getBetaFlagstones(); break;
				case "paving_stones:30": pavementComfortFactor = bicycleConfigGroup.getBetaFlagstones(); break;
				case "unpaved": pavementComfortFactor = bicycleConfigGroup.getBetaGravel(); break;
				case "compacted": pavementComfortFactor = bicycleConfigGroup.getBetaGravel(); break;
				case "dirt": pavementComfortFactor = bicycleConfigGroup.getBetaGravel(); break;
				case "earth": pavementComfortFactor = bicycleConfigGroup.getBetaGravel(); break;
				case "fine_gravel": pavementComfortFactor = bicycleConfigGroup.getBetaGravel(); break;
				case "gravel": pavementComfortFactor = bicycleConfigGroup.getBetaGravel(); break;
				case "ground": pavementComfortFactor = bicycleConfigGroup.getBetaGravel(); break;
				case "wood": pavementComfortFactor = bicycleConfigGroup.getBetaFlagstones(); break;
				case "pebblestone": pavementComfortFactor = bicycleConfigGroup.getBetaCobblestone(); break;
				case "sand": pavementComfortFactor = bicycleConfigGroup.getBetaCobblestone(); break;
				case "bricks": pavementComfortFactor = bicycleConfigGroup.getBetaCobblestone(); break;
				case "stone": pavementComfortFactor = bicycleConfigGroup.getBetaCobblestone(); break;
				case "grass": pavementComfortFactor = bicycleConfigGroup.getBetaCobblestone(); break;
				case "compressed": pavementComfortFactor = bicycleConfigGroup.getBetaGravel(); break;
				case "asphalt;paving_stones:35": pavementComfortFactor = bicycleConfigGroup.getBetaFlagstones(); break;
				case "paving_stones:3": pavementComfortFactor = bicycleConfigGroup.getBetaFlagstones(); break;
				default: pavementComfortFactor = 0.;
			}
		} else {
// For many primary and secondary roads, no surface is specified because they are by default assumed to be is asphalt.
// For tertiary roads street this is not true, e.g. Friesenstr. in Kreuzberg
			if (type != null) {
				if (type.equals("primary") || type.equals("primary_link") || type.equals("secondary") || type.equals("secondary_link")) {
					pavementComfortFactor = 0.0;
				}
			}
		}
		return pavementComfortFactor;
	}


	private static double getPavementDummyCobblestone( String surface , String type ) {
		double pavementDummyCobblestone = 0.0;

		if (surface != null) {
			switch (surface) {
				case "paved": pavementDummyCobblestone = 0.0; break;
				case "asphalt": pavementDummyCobblestone = 0.0; break;
				case "cobblestone": pavementDummyCobblestone = 1.0; break;
				case "cobblestone (bad)": pavementDummyCobblestone = 1.0; break;
				case "sett": pavementDummyCobblestone = 0.0; break;
				case "cobblestone;flattened": pavementDummyCobblestone = 1.0; break;
				case "cobblestone:flattened": pavementDummyCobblestone = 1.0; break;
				case "concrete": pavementDummyCobblestone = 0.0; break;
				case "concrete:lanes": pavementDummyCobblestone = 0.0; break;
				case "concrete_plates": pavementDummyCobblestone = 0.0; break;
				case "concrete:plates": pavementDummyCobblestone = 0.0; break;
				case "paving_stones": pavementDummyCobblestone = 0.0; break;
				case "paving_stones:35": pavementDummyCobblestone = 0.0; break;
				case "paving_stones:30": pavementDummyCobblestone = 0.0; break;
				case "unpaved": pavementDummyCobblestone = 0.0; break;
				case "compacted": pavementDummyCobblestone = 0.0; break;
				case "dirt": pavementDummyCobblestone = 0.0; break;
				case "earth": pavementDummyCobblestone = 0.0; break;
				case "fine_gravel": pavementDummyCobblestone = 0.0; break;
				case "gravel": pavementDummyCobblestone = 0.0; break;
				case "ground": pavementDummyCobblestone = 0.0; break;
				case "wood": pavementDummyCobblestone = 0.0; break;
				case "pebblestone": pavementDummyCobblestone = 1.0; break;
				case "sand": pavementDummyCobblestone = 1.0; break;
				case "bricks": pavementDummyCobblestone = 1.0; break;
				case "stone": pavementDummyCobblestone = 1.0; break;
				case "grass": pavementDummyCobblestone = 1.0; break;
				case "compressed": pavementDummyCobblestone = 0.0; break;
				case "asphalt;paving_stones:35": pavementDummyCobblestone = 0.0; break;
				case "paving_stones:3": pavementDummyCobblestone = 0.0; break;
				default: pavementDummyCobblestone = 0.0; //TODO all of them? -cliving April 2019
			}
		} else {
// For many primary and secondary roads, no surface is specified because they are by default assumed to be is asphalt.
// For tertiary roads street this is not true, e.g. Friesenstr. in Kreuzberg
			if (type != null) {
				if (type.equals("primary") || type.equals("primary_link") || type.equals("secondary") || type.equals("secondary_link")) {
					pavementDummyCobblestone = 0.0;
				}
			}
		}
		return pavementDummyCobblestone;
	}


}
