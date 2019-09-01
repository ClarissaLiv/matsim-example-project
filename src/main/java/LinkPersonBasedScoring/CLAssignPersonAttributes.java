package LinkPersonBasedScoring;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;

import java.util.Random;

public class CLAssignPersonAttributes{

	static final String AGE_GROUP_DUMMY_1 = "ageGroupDummy_1";
//	static final String AGE_GROUP_DUMMY_2 = "ageGroupDummy_2";
//	static final String AGE_GROUP_DUMMY_3 = "ageGroupDummy_3";
//	static final String AGE_GROUP_DUMMY_4 = "ageGroupDummy_4";
//	static final String AGE_GROUP_DUMMY_5 = "ageGroupDummy_5";
//	static final String AGE_GROUP_DUMMY_6 = "ageGroupDummy_6";


	static public void main( Scenario scenario ){
		Random random = new Random(1);

		for (Person person : scenario.getPopulation().getPersons().values()) {
			double r = random.nextDouble();

			if (r<= 0.01) {
				person.getAttributes().putAttribute(AGE_GROUP_DUMMY_1, 1);
			} else if (r<= 0.16){
				person.getAttributes().putAttribute(AGE_GROUP_DUMMY_1, 2);
			} else if (r<= 0.49) {
				person.getAttributes().putAttribute(AGE_GROUP_DUMMY_1, 3);
			} else if (r<= 0.79) {
				person.getAttributes().putAttribute(AGE_GROUP_DUMMY_1, 4);
			} else if (r<= 0.94) {
				person.getAttributes().putAttribute(AGE_GROUP_DUMMY_1, 5);
			} else {
				person.getAttributes().putAttribute(AGE_GROUP_DUMMY_1, 6);
			}
		}

	}
}
