import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class PlanetsTest {

	@Test
	public void should_not_consider_planets_not_attacking() {
		int x, y, growthRate, ships, owner, id;
		Planet planet1 = new Planet(id = 1, owner = PlanetWars.ENEMY_ID, ships = 30, growthRate = 5, x = 9, y = 9);
		Planet planet2 = new Planet(id = 2, owner = PlanetWars.ENEMY_ID, ships = 30, growthRate = 5, x = 8, y = 8);
		Planet planet3 = new Planet(id = 3, owner = PlanetWars.ENEMY_ID, ships = 30, growthRate = 5, x = 1, y = 1);
		Planet planet4 = new Planet(id = 4, owner = PlanetWars.ENEMY_ID, ships = 30, growthRate = 5, x = 2, y = 2);

		Planet mine1 = new Planet(id = 5, owner = PlanetWars.PLAYER_ID, ships = 30, growthRate = 10, x = 5, y = 10);
		Planet mine2 = new Planet(id = 6, owner = PlanetWars.PLAYER_ID, ships = 30, growthRate = 5, x = 0, y = 0);
		Planets myPlanets = new Planets(mine1, mine2);
		Planets enemyPlanets = new Planets(planet1, planet2, planet3, planet4);

		assertEquals(new Planets(planet1, planet2), myPlanets.groupByClosest(enemyPlanets).get(mine1));
		assertEquals(new Planets(planet3, planet4), myPlanets.groupByClosest(enemyPlanets).get(mine2));
	}
}
