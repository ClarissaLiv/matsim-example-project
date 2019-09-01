package LinkPersonBasedScoring;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.bicycle.BicycleConfigGroup;
import org.matsim.contrib.bicycle.BicycleUtils;

class CLBicycleUtilityUtils{
	private static final Logger log = Logger.getLogger( CLBicycleUtilityUtils.class );

	public static double computeLinkBasedScore( Link link , Leg leg , BicycleConfigGroup bicycleConfigGroup , Person person ,
								  double marginalUtilityOfComfort_m ,
								  double marginalUtilityOfInfrastructure_m , double marginalUtilityOfGradient_m_100m ,
								  double pavementComfortFactorCobblestoneAG2 ,
								  CLBicycleConfigGroup clBicycleConfigGroup ) {

		double comfortDisutility = calculateLinkComfortDisutility(link, leg, clBicycleConfigGroup, person, marginalUtilityOfComfort_m,
			  pavementComfortFactorCobblestoneAG2);

//Currently I do not wish to use the infrastructure and gradient disutilities, since these were not considered in my survey. clivings April 2019
//double infrastructureDisutility = calculateLinkInfrastructureDisutility(link, leg, bicycleConfigGroup, person, marginalUtilityOfInfrastructure_m);
//double gradientDisutility = calculateLinkGradientDisutility(link, leg, bicycleConfigGroup, person, marginalUtilityOfGradient_m_100m);
		double infrastructureDisutility = 0;
		double gradientDisutility = 0;

//Return the total disutility that is derived from link characteristics, excluding temporal characteristics, like interactions with cars
		return (infrastructureDisutility + comfortDisutility + gradientDisutility);
	}

	private static double calculateLinkComfortDisutility( Link link , Leg leg , CLBicycleConfigGroup clBicycleConfigGroup , Person person ,
										double marginalUtilityOfComfort_m ,
										double pavementComfortFactorCobblestoneAG2 ) {
		String surface = (String) link.getAttributes().getAttribute( BicycleUtils.SURFACE );
		String type = (String) link.getAttributes().getAttribute("type");

		double linkDistance = link.getLength();
		double legDistance = leg.getRoute().getDistance();

		double localPavementDummyAsphalt = getPavementDummyAsphalt(surface, type);
		double localPavementDummyFlagstones = getPavementDummyFlagstones(surface, type);
		double localPavementDummyGravel = getPavementDummyGravel(surface, type);
		double localPavementDummyCobblestone = getPavementDummyCobblestone(surface, type);

		double pavementComfortParameter = CLBicycleUtilityUtils.getPavementComfortParameter(surface, type, clBicycleConfigGroup );

		Integer ageGroup = (Integer) person.getAttributes().getAttribute( CLAssignPersonAttributes.AGE_GROUP_DUMMY_1 );
//		int ageGroupDummy2 = (int) person.getAttributes().getAttribute(AssignPersonAttributes.AGE_GROUP_DUMMY_2);
//		int ageGroupDummy3 = (int) person.getAttributes().getAttribute(AssignPersonAttributes.AGE_GROUP_DUMMY_3);
//		int ageGroupDummy4 = (int) person.getAttributes().getAttribute(AssignPersonAttributes.AGE_GROUP_DUMMY_4);
//		int ageGroupDummy5 = (int) person.getAttributes().getAttribute(AssignPersonAttributes.AGE_GROUP_DUMMY_5);
//		int ageGroupDummy6 = (int) person.getAttributes().getAttribute(AssignPersonAttributes.AGE_GROUP_DUMMY_6);
//TODO does this factor for ag2 and cobblestone need to be a method input? Like passed from the outside?
//double pavementComfortFactorCobblestoneAG2 = bicycleConfigGroup.getBetaCobblestoneAgeGroup2();

		double comfortDisutility = pavementComfortParameter*(linkDistance/legDistance);

		if ( link.getId().toString().equals( "6" )) {
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

//	public static double calculateLinkInfrastructureDisutility (Link link, Leg leg, BicycleConfigGroup bicycleConfigGroup, Person person, double marginalUtilityOfInfrastructure_m) {
//
//		String type = (String) link.getAttributes().getAttribute("type");
//		String cyclewaytype = (String) link.getAttributes().getAttribute(BicycleLabels.CYCLEWAY);
//
//		double linkDistance = link.getLength();
//
////InfrastructureFactor set to 0 because Gradient was not an Attribute considered in the estimation of the utility function used in
//// (Livingston, Beyer-Bartana, Ziemke, Bahamode-Birke; 2019)
//		double infrastructureFactor = BicycleUtilityUtils.getInfrastructureFactor(type, cyclewaytype );
//		double infrastructureDisutility = marginalUtilityOfInfrastructure_m * (1. - infrastructureFactor) * linkDistance;
//
//		return infrastructureDisutility;
//	}
//
//	public static double calculateLinkGradientDisutility (Link link, Leg leg, BicycleConfigGroup bicycleConfigGroup, Person person, double marginalUtilityOfGradient_m_100m) {
//
//		double linkDistance = link.getLength();
//
////GradientFactor set to 0 because Gradient was not an Attribute considered in the estimation of the utility function used in
//// (Livingston, Beyer-Bartana, Ziemke, Bahamode-Birke; 2019)
//		double gradientFactor = BicycleUtilityUtils.getGradientFactor(link );
//		double gradientDisutility = marginalUtilityOfGradient_m_100m * gradientFactor * linkDistance;
//
//		return gradientDisutility;
//	}

	//GradientFactor is only used in the calculation of the travel disutility for the router, and not during scoring
//because Gradient was not an Attribute considered in the estimation of the utility function used in
// (Livingston, Beyer-Bartana, Ziemke, Bahamode-Birke; 2019)
	public static double getGradientFactor(Link link) {
		double gradient = 0.;
		Double fromNodeZ = link.getFromNode().getCoord().getZ();
		Double toNodeZ = link.getToNode().getCoord().getZ();
		if ((fromNodeZ != null) && (toNodeZ != null)) {
			if (toNodeZ > fromNodeZ) { // No positive utility for downhill, only negative for uphill
				gradient = (toNodeZ - fromNodeZ) / link.getLength();
			}
		}
		return gradient;
	}


	public static double getPavementComfortParameter(String surface, String type, CLBicycleConfigGroup bicycleConfigGroup ) {
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

	public static double getPavementDummyAsphalt(String surface, String type) {
		double pavementDummyAsphalt = 0.0;

		if (surface != null) {
			switch (surface) {
				case "paved": pavementDummyAsphalt = 1.0; break;
				case "asphalt": pavementDummyAsphalt = 1.0; break;
				case "cobblestone": pavementDummyAsphalt = 0.0; break;
				case "cobblestone (bad)": pavementDummyAsphalt = 0.0; break;
				case "sett": pavementDummyAsphalt = 0.0; break;
				case "cobblestone;flattened": pavementDummyAsphalt = 0.0; break;
				case "cobblestone:flattened": pavementDummyAsphalt = 0.0; break;
				case "concrete": pavementDummyAsphalt = 1.0; break;
				case "concrete:lanes": pavementDummyAsphalt = 1.0; break;
				case "concrete_plates": pavementDummyAsphalt = 0.0; break;
				case "concrete:plates": pavementDummyAsphalt = 0.0; break;
				case "paving_stones": pavementDummyAsphalt = 0.0; break;
				case "paving_stones:35": pavementDummyAsphalt = 0.0; break;
				case "paving_stones:30": pavementDummyAsphalt = 0.0; break;
				case "unpaved": pavementDummyAsphalt = 0.0; break;
				case "compacted": pavementDummyAsphalt = 0.0; break;
				case "dirt": pavementDummyAsphalt = 0.0; break;
				case "earth": pavementDummyAsphalt = 0.0; break;
				case "fine_gravel": pavementDummyAsphalt = 0.0; break;
				case "gravel": pavementDummyAsphalt = 0.0; break;
				case "ground": pavementDummyAsphalt = 0.0; break;
				case "wood": pavementDummyAsphalt = 0.0; break;
				case "pebblestone": pavementDummyAsphalt = 0.0; break;
				case "sand": pavementDummyAsphalt = 0.0; break;
				case "bricks": pavementDummyAsphalt = 0.0; break;
				case "stone": pavementDummyAsphalt = 0.0; break;
				case "grass": pavementDummyAsphalt = 0.0; break;
				case "compressed": pavementDummyAsphalt = 0.0; break;
				case "asphalt;paving_stones:35": pavementDummyAsphalt = 0.0; break;
				case "paving_stones:3": pavementDummyAsphalt = 0.0; break;
				default: pavementDummyAsphalt = 1.0; //TODO all of them? -cliving April 2019
			}
		} else {
// For many primary and secondary roads, no surface is specified because they are by default assumed to be is asphalt.
// For tertiary roads street this is not true, e.g. Friesenstr. in Kreuzberg
			if (type != null) {
				if (type.equals("primary") || type.equals("primary_link") || type.equals("secondary") || type.equals("secondary_link")) {
					pavementDummyAsphalt = 1.0;
				}
			}
		}
		return pavementDummyAsphalt;
	}

	public static double getPavementDummyFlagstones(String surface, String type) {
		double pavementDummyFlagstones = 0.0;

		if (surface != null) {
			switch (surface) {
				case "paved": pavementDummyFlagstones = 0.0; break;
				case "asphalt": pavementDummyFlagstones = 0.0; break;
				case "cobblestone": pavementDummyFlagstones = 0.0; break;
				case "cobblestone (bad)": pavementDummyFlagstones = 0.0; break;
				case "sett": pavementDummyFlagstones = 1.0; break;
				case "cobblestone;flattened": pavementDummyFlagstones = 0.0; break;
				case "cobblestone:flattened": pavementDummyFlagstones = 0.0; break;
				case "concrete": pavementDummyFlagstones = 0.0; break;
				case "concrete:lanes": pavementDummyFlagstones = 0.0; break;
				case "concrete_plates": pavementDummyFlagstones = 1.0; break;
				case "concrete:plates": pavementDummyFlagstones = 1.0; break;
				case "paving_stones": pavementDummyFlagstones = 1.0; break;
				case "paving_stones:35": pavementDummyFlagstones = 1.0; break;
				case "paving_stones:30": pavementDummyFlagstones = 1.0; break;
				case "unpaved": pavementDummyFlagstones = 0.0; break;
				case "compacted": pavementDummyFlagstones = 0.0; break;
				case "dirt": pavementDummyFlagstones = 0.0; break;
				case "earth": pavementDummyFlagstones = 0.0; break;
				case "fine_gravel": pavementDummyFlagstones = 0.0; break;
				case "gravel": pavementDummyFlagstones = 0.0; break;
				case "ground": pavementDummyFlagstones = 0.0; break;
				case "wood": pavementDummyFlagstones = 1.0; break;
				case "pebblestone": pavementDummyFlagstones = 0.0; break;
				case "sand": pavementDummyFlagstones = 0.0; break;
				case "bricks": pavementDummyFlagstones = 1.0; break;
				case "stone": pavementDummyFlagstones = 0.0; break;
				case "grass": pavementDummyFlagstones = 0.0; break;
				case "compressed": pavementDummyFlagstones = 0.0; break;
				case "asphalt;paving_stones:35": pavementDummyFlagstones = 1.0; break;
				case "paving_stones:3": pavementDummyFlagstones = 1.0; break;
				default: pavementDummyFlagstones = 0.0; //TODO all of them? -cliving April 2019
			}
		} else {
// For many primary and secondary roads, no surface is specified because they are by default assumed to be is asphalt.
// For tertiary roads street this is not true, e.g. Friesenstr. in Kreuzberg
			if (type != null) {
				if (type.equals("primary") || type.equals("primary_link") || type.equals("secondary") || type.equals("secondary_link")) {
					pavementDummyFlagstones = 0.0;
				}
			}
		}
		return pavementDummyFlagstones;
	}

	public static double getPavementDummyGravel(String surface, String type) {
		double pavementDummyGravel = 0.0;

		if (surface != null) {
			switch (surface) {
				case "paved": pavementDummyGravel = 0.0; break;
				case "asphalt": pavementDummyGravel = 0.0; break;
				case "cobblestone": pavementDummyGravel = 0.0; break;
				case "cobblestone (bad)": pavementDummyGravel = 0.0; break;
				case "sett": pavementDummyGravel = 0.0; break;
				case "cobblestone;flattened": pavementDummyGravel = 0.0; break;
				case "cobblestone:flattened": pavementDummyGravel = 0.0; break;
				case "concrete": pavementDummyGravel = 0.0; break;
				case "concrete:lanes": pavementDummyGravel = 0.0; break;
				case "concrete_plates": pavementDummyGravel = 0.0; break;
				case "concrete:plates": pavementDummyGravel = 0.0; break;
				case "paving_stones": pavementDummyGravel = 0.0; break;
				case "paving_stones:35": pavementDummyGravel = 0.0; break;
				case "paving_stones:30": pavementDummyGravel = 0.0; break;
				case "unpaved": pavementDummyGravel = 1.0; break;
				case "compacted": pavementDummyGravel = 1.0; break;
				case "dirt": pavementDummyGravel = 1.0; break;
				case "earth": pavementDummyGravel = 1.0; break;
				case "fine_gravel": pavementDummyGravel = 1.0; break;
				case "gravel": pavementDummyGravel = 1.0; break;
				case "ground": pavementDummyGravel = 1.0; break;
				case "wood": pavementDummyGravel = 0.0; break;
				case "pebblestone": pavementDummyGravel = 0.0; break;
				case "sand": pavementDummyGravel = 0.0; break;
				case "bricks": pavementDummyGravel = 0.0; break;
				case "stone": pavementDummyGravel = 0.0; break;
				case "grass": pavementDummyGravel = 0.0; break;
				case "compressed": pavementDummyGravel = 1.0; break;
				case "asphalt;paving_stones:35": pavementDummyGravel = 0.0; break;
				case "paving_stones:3": pavementDummyGravel = 0.0; break;
				default: pavementDummyGravel = 0.0; //TODO all of them? -cliving April 2019
			}
		} else {
// For many primary and secondary roads, no surface is specified because they are by default assumed to be is asphalt.
// For tertiary roads street this is not true, e.g. Friesenstr. in Kreuzberg
			if (type != null) {
				if (type.equals("primary") || type.equals("primary_link") || type.equals("secondary") || type.equals("secondary_link")) {
					pavementDummyGravel = 0.0;
				}
			}
		}
		return pavementDummyGravel;
	}

	public static double getPavementDummyCobblestone(String surface, String type) {
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


//InfrastructureFactor is only used in the calculation of the travel disutility for the router, and not during scoring
//because infrastructure was not an Attribute considered in the estimation of the utility function used in
// (Livingston, Beyer-Bartana, Ziemke, Bahamode-Birke; 2019) - clivings April 2019

	public static double getInfrastructureFactor(String type, String cyclewaytype) {
		double infrastructureFactor = 0.0; //was set to 1.0 for original bicycle contrib - clivings April 2019
		if (type != null) {
			if (type.equals("trunk")) {
				if (cyclewaytype == null || cyclewaytype.equals("no") || cyclewaytype.equals("none")) { // No cycleway
					infrastructureFactor = .05;
				} else { // Some kind of cycleway
					infrastructureFactor = .95;
				}
			} else if (type.equals("primary") || type.equals("primary_link")) {
				if (cyclewaytype == null || cyclewaytype.equals("no") || cyclewaytype.equals("none")) { // No cycleway
					infrastructureFactor = .10;
				} else { // Some kind of cycleway
					infrastructureFactor = .95;
				}
			} else if (type.equals("secondary") || type.equals("secondary_link")) {
				if (cyclewaytype == null || cyclewaytype.equals("no") || cyclewaytype.equals("none")) { // No cycleway
					infrastructureFactor = .30;
				} else { // Some kind of cycleway
					infrastructureFactor = .95;
				}
			} else if (type.equals("tertiary") || type.equals("tertiary_link")) {
				if (cyclewaytype == null || cyclewaytype.equals("no") || cyclewaytype.equals("none")) { // No cycleway
					infrastructureFactor = .40;
				} else { // Some kind of cycleway
					infrastructureFactor = .95;
				}
			} else if (type.equals("unclassified")) {
				if (cyclewaytype == null || cyclewaytype.equals("no") || cyclewaytype.equals("none")) { // No cycleway
					infrastructureFactor = .90;
				} else { // Some kind of cycleway
					infrastructureFactor = .95;
				}
			} else if (type.equals("unclassified")) {
				infrastructureFactor = .95;
			} else if (type.equals("service") || type.equals("living_street") || type.equals("minor")) {
				infrastructureFactor = .95;
			} else if (type.equals("cycleway") || type.equals("path")) {
				infrastructureFactor = 1.00;
			} else if (type.equals("footway") || type.equals("track") || type.equals("pedestrian")) {
				infrastructureFactor = .95;
			} else if (type.equals("steps")) {
				infrastructureFactor = .10;
			}
		} else {
			infrastructureFactor = .85;
		}
		return infrastructureFactor;
	}


	// TODO This method was left in because I did not have time to figure out how to feed my parameters into the Disultility functions,
// which do not have knowledge of legs. I need knowledge of legs to use my parameters, because they are route based
// and thus I need to know the leg distance of the route so I can calculate the partial utility of each link in relation to the whole
// route.
	public static double getComfortFactor(String surface, String type) {
		double comfortFactor = 1.0;
		if (surface != null) {
			switch (surface) {
				case "paved":
				case "asphalt": comfortFactor = 1.0; break;
				case "cobblestone": comfortFactor = .40; break;
				case "cobblestone (bad)": comfortFactor = .30; break;
				case "sett": comfortFactor = .50; break;
				case "cobblestone;flattened":
				case "cobblestone:flattened": comfortFactor = .50; break;
				case "concrete": comfortFactor = .100; break;
				case "concrete:lanes": comfortFactor = .95; break;
				case "concrete_plates":
				case "concrete:plates": comfortFactor = .90; break;
				case "paving_stones": comfortFactor = .80; break;
				case "paving_stones:35":
				case "paving_stones:30": comfortFactor = .80; break;
				case "unpaved": comfortFactor = .60; break;
				case "compacted": comfortFactor = .70; break;
				case "dirt":
				case "earth": comfortFactor = .30; break;
				case "fine_gravel": comfortFactor = .90; break;
				case "gravel":
				case "ground": comfortFactor = .60; break;
				case "wood":
				case "pebblestone":
				case "sand": comfortFactor = .30; break;
				case "bricks": comfortFactor = .60; break;
				case "stone":
				case "grass":
				case "compressed": comfortFactor = .40; break;
				case "asphalt;paving_stones:35": comfortFactor = .60; break;
				case "paving_stones:3": comfortFactor = .40; break;
				default: comfortFactor = .85;
			}
		} else {
// For many primary and secondary roads, no surface is specified because they are by default assumed to be is asphalt.
// For tertiary roads street this is not true, e.g. Friesenstr. in Kreuzberg
			if (type != null) {
				if (type.equals("primary") || type.equals("primary_link") || type.equals("secondary") || type.equals("secondary_link")) {
					comfortFactor = 1.0;
				}
			}
		}
		return comfortFactor;
	}
}
