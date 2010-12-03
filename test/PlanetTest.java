import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PlanetTest {

	/**
	 * stateAfterFleetsAttack
	 */

	@Test
	public void should_not_consider_planets_not_attacking() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.NEUTRAL_ID, ships = 30, growthRate = 5, x = 5, y = 5);
		Fleet mine = new Fleet(PlanetWars.PLAYER_ID, 31, 0, id, 10, 1);
		Fleet unrelated1 = new Fleet(PlanetWars.ENEMY_ID, 10, 0, 2, 10, 1);
		Fleet unrelated2 = new Fleet(PlanetWars.PLAYER_ID, 20, 0, 3, 10, 1);
		Planet futurePlanet = planet.stateAfterFleetsAttack(new Fleets(mine, unrelated1, unrelated2));
		assertTrue(futurePlanet.isMine());
		assertEquals(1, futurePlanet.numShips);
	}

	@Test
	public void should_figure_out_future_state_of_planet_with_one_fleet() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.NEUTRAL_ID, ships = 30, growthRate = 5, x = 5, y = 5);
		Fleet mine = new Fleet(PlanetWars.PLAYER_ID, 31, 0, id, 10, 1);
		Planet futurePlanet = planet.stateAfterFleetsAttack(new Fleets(mine));
		assertTrue(futurePlanet.isMine());
		assertEquals(1, futurePlanet.numShips);
	}

	@Test
	public void should_figure_out_future_state_of_planet_with_alternating_attack_sequence() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.NEUTRAL_ID, ships = 30, growthRate = 5, x = 5, y = 5);
		Fleet mine = new Fleet(PlanetWars.PLAYER_ID, 31, 0, id, 10, 1);
		Fleet enemy = new Fleet(PlanetWars.ENEMY_ID, 7, 0, id, 10, 2);
		Fleet mine2 = new Fleet(PlanetWars.PLAYER_ID, 17, 0, id, 10, 5);
		Fleet enemy2 = new Fleet(PlanetWars.ENEMY_ID, 27, 0, id, 10, 10);
		Fleets fleets = new Fleets(mine, enemy, mine2, enemy2);
		Planet futurePlanet = planet.stateAfterFleetsAttack(fleets);
		assertTrue(futurePlanet.isEnemy());
		assertEquals(1, futurePlanet.numShips);
	}

	@Test
	public void should_figure_out_future_state_of_planet_with_one_fleet_when_enemy_owns_the_planet_and_rescues() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.ENEMY_ID, ships = 30, growthRate = 5, x = 5, y = 5);
		Fleet mine = new Fleet(PlanetWars.PLAYER_ID, 36, 0, id, 10, 1);
		Fleet enemy = new Fleet(PlanetWars.ENEMY_ID, 7, 0, id, 10, 2);
		Fleet enemy2 = new Fleet(PlanetWars.ENEMY_ID, 5, 0, id, 10, 5);
		Planet futurePlanet = planet.stateAfterFleetsAttack(new Fleets(mine, enemy, enemy2));
		assertTrue(futurePlanet.isEnemy());
		assertEquals(21, futurePlanet.numShips);
	}

	@Test
	public void should_not_lose_planet_if_ships_sent_is_same_as_number_of_ships() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.ENEMY_ID, ships = 20, growthRate = 5, x = 5, y = 5);
		Fleet mine = new Fleet(PlanetWars.PLAYER_ID, 25, 0, id, 10, 1);
		Planet futurePlanet = planet.stateAfterFleetsAttack(new Fleets(mine));
		assertTrue(futurePlanet.isEnemy());
		assertEquals(0, futurePlanet.numShips);
	}

	@Test
	public void should_lose_planet_when_multiple_fleets_sent_from_both_players_at_same_time_and_owned_by_me() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.PLAYER_ID, ships = 5, growthRate = 5, x = 5, y = 5);
		Fleets fleets = new Fleets(
				new Fleet(PlanetWars.ENEMY_ID, 25, 0, id, 10, 1),
				new Fleet(PlanetWars.PLAYER_ID, 15, 0, id, 10, 1),
				new Fleet(PlanetWars.PLAYER_ID, 15, 0, id, 10, 1),
				new Fleet(PlanetWars.ENEMY_ID, 20, 0, id, 10, 1)
		);
		Planet futurePlanet = planet.stateAfterFleetsAttack(fleets);
		assertTrue(futurePlanet.isEnemy());
		assertEquals(5, futurePlanet.numShips);
	}
	
	@Test
	public void should_lose_planet_when_multiple_fleets_sent_from_both_players_at_same_time_and_owned_by_enemy() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.ENEMY_ID, ships = 5, growthRate = 5, x = 5, y = 5);
		Fleets fleets = new Fleets(
				new Fleet(PlanetWars.PLAYER_ID, 25, 0, id, 10, 1),
				new Fleet(PlanetWars.ENEMY_ID, 15, 0, id, 10, 1),
				new Fleet(PlanetWars.ENEMY_ID, 15, 0, id, 10, 1),
				new Fleet(PlanetWars.PLAYER_ID, 20, 0, id, 10, 1)
		);
		Planet futurePlanet = planet.stateAfterFleetsAttack(fleets);
		assertTrue(futurePlanet.isMine());
		assertEquals(5, futurePlanet.numShips);
	}

	@Test
	public void should_not_lose_planet_if_fleets_sent_in_same_turn_is_same_as_number_of_ships() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.PLAYER_ID, ships = 5, growthRate = 5, x = 5, y = 5);
		Fleets fleets = new Fleets(
				new Fleet(PlanetWars.ENEMY_ID, 25, 0, id, 10, 1),
				new Fleet(PlanetWars.PLAYER_ID, 20, 0, id, 10, 1),
				new Fleet(PlanetWars.ENEMY_ID, 25, 0, id, 10, 1),
				new Fleet(PlanetWars.PLAYER_ID, 20, 0, id, 10, 1)
		);
		Planet futurePlanet = planet.stateAfterFleetsAttack(fleets);
		assertTrue(futurePlanet.isMine());
		assertEquals(0, futurePlanet.numShips);
	}

	@Test
	public void should_figure_out_future_state_of_planet_based_on_incoming_fleets_and_growth() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.NEUTRAL_ID, ships = 30, growthRate = 5, x = 5, y = 5);
		Fleet mine = new Fleet(PlanetWars.PLAYER_ID, 31, 0, id, 10, 1);
		Fleet enemy = new Fleet(PlanetWars.ENEMY_ID, 7, 0, id, 10, 2);
		Fleet mine2 = new Fleet(PlanetWars.PLAYER_ID, 17, 0, id, 10, 5);
		Fleet enemy2 = new Fleet(PlanetWars.ENEMY_ID, 27, 0, id, 10, 10);
		Fleets fleets = new Fleets(mine, enemy, mine2, enemy2);
		Planet futurePlanet = planet.stateAfterFleetsAttack(fleets);
		assertTrue(futurePlanet.isEnemy());
		assertEquals(1, futurePlanet.numShips);
	}

	/**
	 * sparableShips
	 */
	@Test
	public void should_return_the_amount_remaining_after_the_fleets_have_played_out_minus_one() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.PLAYER_ID, ships = 40, growthRate = 5, x = 5, y = 5);
		Fleet enemy = new Fleet(PlanetWars.ENEMY_ID, 15, 0, id, 10, 1);
		Fleet mine2 = new Fleet(PlanetWars.PLAYER_ID, 15, 0, id, 10, 1);
		Fleet enemy2 = new Fleet(PlanetWars.ENEMY_ID, 25, 0, id, 10, 3);
		assertEquals(29, planet.sparableShips(new Fleets(enemy, enemy2), new Fleets(mine2)));
	}
	
	@Test
	public void should_return_the_amount_remaining_when_no_fleets_are_attacking() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.PLAYER_ID, ships = 40, growthRate = 5, x = 5, y = 5);
		assertEquals(39, planet.sparableShips(new Fleets(), new Fleets()));
	}
	
	@Test
	public void should_return_the_amount_remaining_when_no_fleets_are_attacking_and_numships_is_zero() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.PLAYER_ID, ships = 0, growthRate = 5, x = 5, y = 5);
		assertEquals(0, planet.sparableShips(new Fleets(), new Fleets()));
	}
	
	@Test
	public void should_not_return_more_than_the_current_number_of_ships() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.PLAYER_ID, ships = 40, growthRate = 5, x = 5, y = 5);
		Fleet mine = new Fleet(PlanetWars.PLAYER_ID, 15, 0, id, 10, 2);
		assertEquals(39, planet.sparableShips(new Fleets(), new Fleets(mine)));
	}

	@Test
	public void should_return_zero_when_the_planet_will_be_lost() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.PLAYER_ID, ships = 40, growthRate = 5, x = 5, y = 5);
		Fleet enemy = new Fleet(PlanetWars.ENEMY_ID, 55, 0, id, 10, 1);
		assertEquals(0, planet.sparableShips(new Fleets(enemy), new Fleets()));
	}
	
	@Test
	public void should_return_zero_when_the_planet_will_be_lost_exactly() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.PLAYER_ID, ships = 40, growthRate = 5, x = 5, y = 5);
		Fleet enemy = new Fleet(PlanetWars.ENEMY_ID, 45, 0, id, 10, 1);
		assertEquals(0, planet.sparableShips(new Fleets(enemy), new Fleets()));
	}
	
	@Test
	public void should_return_zero_when_sending_ships_now_will_result_in_a_loss_even_if_retaken_later() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.PLAYER_ID, ships = 30, growthRate = 5, x = 5, y = 5);
		Fleet enemy = new Fleet(PlanetWars.ENEMY_ID, 40, 0, id, 10, 1);
		Fleet mine2 = new Fleet(PlanetWars.PLAYER_ID, 100, 0, id, 10, 10);
		assertEquals(0, planet.sparableShips(new Fleets(enemy), new Fleets(mine2)));
	}

	/**
	 * neededToWin and minimumTurnNeededToSave
	 */
	@Test
	public void should_return_the_number_needed_to_save_a_planet() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.PLAYER_ID, ships = 40, growthRate = 5, x = 5, y = 5);
		Fleet enemy = new Fleet(PlanetWars.ENEMY_ID, 55, 0, id, 10, 2);
		assertEquals(6, planet.neededToWin(new Fleets(enemy), new Fleets(), 2));
	}
	
	/**
	 * neededToWin and minimumTurnNeededToSave
	 */
	@Test
	public void should_return_the_number_needed_to_win_a_neutral_planet() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.NEUTRAL_ID, ships = 40, growthRate = 5, x = 5, y = 5);
		Fleet enemy = new Fleet(PlanetWars.ENEMY_ID, 20, 0, id, 10, 2);
		assertEquals(21, planet.neededToWin(new Fleets(enemy), new Fleets(), 1));
	}
	
	/**
	 * neededToWin
	 */
	
	@Test // TODO: verify whether the boundary condition is right should it be 6 or 11?
	public void should_return_the_number_needed_to_save_a_planet_when_enemy_wins_and_support_planet_is_too_distant() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.PLAYER_ID, ships = 40, growthRate = 5, x = 5, y = 5);
		Fleet enemy = new Fleet(PlanetWars.ENEMY_ID, 55, 0, id, 10, 2);
		assertEquals(6, planet.neededToWin(new Fleets(enemy), new Fleets(), 2));
	}

	@Test
	public void should_return_the_number_needed_to_win_an_enemy_planet() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.ENEMY_ID, ships = 40, growthRate = 5, x = 5, y = 5);
		assertEquals(51, planet.neededToWin(new Fleets(), new Fleets(), 2));
	}

	@Test
	public void should_return_the_number_needed_to_win_a_planet_from_a_given_distance() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.ENEMY_ID, ships = 10, growthRate = 5, x = 5, y = 5);
		Fleet mine = new Fleet(PlanetWars.PLAYER_ID, 20, 0, id, 10, 1);
		Fleet enemy = new Fleet(PlanetWars.ENEMY_ID, 40, 0, id, 10, 2);
		assertEquals(41, planet.neededToWin(new Fleets(enemy), new Fleets(mine), 4));
	}

	@Test
	public void should_figure_out_how_many_are_needed_to_win_before_a_planet_would_be_lost_and_retaken() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.PLAYER_ID, ships = 10, growthRate = 5, x = 5, y = 5);
		Fleet enemy = new Fleet(PlanetWars.ENEMY_ID, 40, 0, id, 10, 1);
		Fleet mine = new Fleet(PlanetWars.PLAYER_ID, 40, 0, id, 10, 3);
		Fleet enemy2 = new Fleet(PlanetWars.ENEMY_ID, 40, 0, id, 10, 5);
		assertEquals(25, planet.neededToWin(new Fleets(enemy, enemy2), new Fleets(mine), 1));
	}
	
	@Test
	public void should_figure_out_how_many_are_needed_to_win_when_more_enemy_ships_are_coming() {
		int x, y, growthRate, ships, owner, id;
		Planet planet = new Planet(id = 5, owner = PlanetWars.ENEMY_ID, ships = 10, growthRate = 5, x = 5, y = 5);
		Fleet enemy = new Fleet(PlanetWars.ENEMY_ID, 10, 0, id, 10, 1);
		Fleet enemy2 = new Fleet(PlanetWars.ENEMY_ID, 15, 0, id, 10, 5);
		assertEquals(31, planet.neededToWin(new Fleets(enemy, enemy2), new Fleets(), 2));
	}
}
