import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlanetWars {

	public static final int NEUTRAL_ID = 0;
	public static final int PLAYER_ID = 1;
	public static final int ENEMY_ID = 2;
	public static final int MINIMUM_PLANET_SIZE = 1;

	private List<Planet> planets;
	private List<Fleet> fleets;
	private List<Attack> attacks = new ArrayList<Attack>();
	private static int numberOfTurns;
	private Fleets fleetz;
	private Planets planetz;
	private Fleets myFleetz;
	private Planets myPlanetz;
	private Planets neutralPlanetz;
	private Fleets enemyFleetz;
	private Planets enemyPlanetz;
	private long startTime;
	private Planets futurePlanets;
	private Planets futureMyPlanets;
	private Planets futureEnemyPlanets;
	private long startOfRescue;
	private long endOfRescue;
	private long endOfAttack;
	private long endOfMove;
	private long initializeEndTime;
	private Planets mostStrategicPlanets;
	private Planets futureNeutralPlanets;
	private Planets closestEnemiesToAnyOfMyPlanets;
	private Planets closestFuturePlanetsToTheEnemy;
	private Planets closestPlanetsToTheEnemy;
	private static Set<Integer> previousHomePlanetIds = new HashSet<Integer>();

	PlanetWars(String gameStateString) {
		startTime = System.currentTimeMillis();
		planets = new ArrayList<Planet>();
		fleets = new ArrayList<Fleet>();
		parseGameState(gameStateString);
		fleetz = new Fleets(fleets);
		planetz = new Planets(planets);
		myFleetz = new Fleets(myFleets());
		myPlanetz = new Planets(myPlanets());
		enemyFleetz = new Fleets(enemyFleets());
		enemyPlanetz = new Planets(enemyPlanets());
		neutralPlanetz = new Planets(neutralPlanets());
		futurePlanets = planetz.inTheFuture(fleetz);
		futureMyPlanets = futurePlanets.myPlanets();
		futureEnemyPlanets = futurePlanets.enemyPlanets();
		futureNeutralPlanets = futurePlanets.neutralPlanets();
		closestEnemiesToAnyOfMyPlanets = enemyPlanetz.closestTo(myPlanetz);
		closestPlanetsToTheEnemy = myPlanetz.closestTo(enemyPlanetz);
		closestFuturePlanetsToTheEnemy = futureMyPlanets.closestTo(futureEnemyPlanets);
		mostStrategicPlanets = mostStrategicPlanets();
		this.initializeEndTime = System.currentTimeMillis();
		previousHomePlanetIds.addAll(myPlanetz.ids());
	}

	public PlanetWars(List<Planet> planets, List<Fleet> enemyFleet) {
		this.planets = planets;
		this.fleets = enemyFleet;
	}

	private List<Planet> attackedPlanets() {
		List<Planet> attackedPlanets = new ArrayList<Planet>();
		for (Attack attack : attacks) {
			attackedPlanets.add(attack.target);
		}
		return attackedPlanets;
	}

	private List<Planet> remainingHomePlanets() {
		List<Planet> myPlanets = myPlanets();
		ArrayList<Planet> remaining = new ArrayList<Planet>();
		for (Planet p : myPlanets) {
			if (safelySparableShips(p) > 0) {
				remaining.add(p);
			}
		}
		return remaining;
	}

	private void addAttack(Planet home, Planet target, int ships) {
		Attack attack = new Attack(home, target, ships);
		Fleet newFleet = new Fleet(home.owner, ships, home.id, target.id, distance(home, target),
				distance(home, target));
		fleetz.update(newFleet);
		myFleetz.update(newFleet);

		if (attack.ships < 1) {
			PlanetWars.log("Zero or Negative attack detected!");
			PlanetWars.log("attack.homePlanet=" + attack.homePlanet + ", attack.otherPlanet=" + attack.target
					+ ", attack.ships=" + attack.ships);
			throw new RuntimeException("Negative attack detected!");
		}
		if (attack.homePlanet == attack.target) {
			PlanetWars.log("Tried to attack the same planet!");
			throw new RuntimeException("Tried to attack the same planet!");
		}
		if (attack.ships > attack.homePlanet.numShips) {
			PlanetWars.log("Sending too many ships for given planet!");
			PlanetWars.log("attack.homePlanet=" + attack.homePlanet + " ships =" + attack.homePlanet.numShips
					+ ", attack.otherPlanet=" + attack.target + " ships =" + attack.target.numShips
					+ ", attack.ships=" + attack.ships);
			throw new RuntimeException("Sending too many ships for given planet!");
		}

		int remaining = home.numShips - ships;
		home.numShips(remaining);
		if (ships != 0) {
			attacks.add(attack);
		}
	}

	private void attackPlanets() {
		Planets targets = planetz.minus(futureMyPlanets);
		if (numberOfTurns == 1) {
			targets = planetz.allCloserTo1Than2(myPlanetz.first(), enemyPlanetz.first());
		}

		Planets lostPlanets = planetz.findAll(previousHomePlanetIds).minus(futureMyPlanets);
		if (lostPlanets.size() > 0) {
			targets = lostPlanets.union(enemyPlanetz);
		} else if (futureMyPlanets.production() > futureEnemyPlanets.production()) {
			if (futureMyPlanets.ships() > futureEnemyPlanets.ships()) {
				targets = planetz.minus(futureNeutralPlanets);
			} else {
				return;
			}
		}
		
		targets = sortByValue(targets);
		for (Planet target : targets.planets) {
			Planets sorted = new Planets(remainingHomePlanets()).sortByDistanceTo(target);
			for (Planet homePlanet : sorted.planets) {
				Planets remove = enemyPlanetz.remove(target);
				Planet closestEnemy = remove.findClosestTo(homePlanet);
				if (closestEnemy == null) {
					closestEnemy = target;
				}
				if (distance(homePlanet, target) <= distance(homePlanet, closestEnemy) || enemyPlanetz.averageDistanceToPlanets(target) >= myPlanetz.averageDistanceToPlanets(target)) {
					if (!wouldBeBetterToWait(targets, homePlanet, target)) {
						attack(homePlanet, target);
					}
				}
			}
		}
	}

	private boolean wouldBeBetterToWait(Planets targets, Planet homePlanet, Planet target) {
		Planets attacked = new Planets(attackedPlanets());
		Planets skippedNeutrals = targets.before(target).minus(attacked).minus(enemyPlanetz);
		for (Planet planet : skippedNeutrals.planets) {
			int distanceThereAndBack = Math.max(homePlanet.distance(planet), homePlanet.distance(target)) * 2;
			int growthTimeForSkippedPlanet = distanceThereAndBack - planet.distance(homePlanet);
			int growthTimeForTarget = distanceThereAndBack - target.distance(homePlanet);
			if (growthTimeForSkippedPlanet * planet.growthRate - planet.numShips > growthTimeForTarget * target.growthRate - target.numShips) {
				return true;
			}
		}
		return false;
	}

	private void attack(Planet homePlanet, Planet target) {
		int neededToWin = shipsNeededToWin(homePlanet, target);
		if (neededToWin > 0) {
			Integer shipsToSend = safelySparableShips(homePlanet, target);
			if (shipsToSend == 0 || neededToWin > shipsToSend) return;
			if (target.owner == ENEMY_ID) {
				addAttack(homePlanet, target, shipsToSend);
			} else {
				if (shipsToSend >= neededToWin) {
					shipsToSend = neededToWin;
					addAttack(homePlanet, target, shipsToSend);
				}
			}
		}
	}
	
	private void moveRemainingFederationToMostStrategicPlanets() {
		Planets remaining = new Planets(remainingHomePlanets()).minus(mostStrategicPlanets);
		int count = remaining.size();
		Map<Planet, Planets> proximityGroups = mostStrategicPlanets.groupByClosest(futureEnemyPlanets);
		for (int i = 0; i < count*mostStrategicPlanets.size(); i++) {
			for (Planet planet : mostStrategicPlanets.planets) {
				if (proximityGroups.get(planet) != null) {
					int ships = proximityGroups.get(planet).ships();
					int numShips = futurePlanets.planet(planet.id).numShips;
					if (ships < numShips) continue;
				}
				Planet helper = remaining.findClosestTo(planet);
				if (helper != null) {
					Planet closestEnemyToHelper = enemyPlanetz.findClosestTo(helper);
					Planet closestEnemyToTarget = enemyPlanetz.findClosestTo(helper);
					// this prevents strategic planets from sending ships to other strategic planets
					// though, this wouldn't be needed if I could balance the ships needed of strategic planets
					if (helper.distance(closestEnemyToHelper) >= planet.distance(closestEnemyToTarget)) {
						Integer safelySparableShips = safelySparableShips(helper);
						if (safelySparableShips > 0) {
							addAttack(helper, planet, safelySparableShips);
							remaining = remaining.remove(helper);
						}
					}
				}
			}
		}
	}

	private void moveRemainingFederationToCloseNeutrals() {
		Planets targets = neutralPlanetz.minus(futureMyPlanets);
		Planets lostPlanets = planetz.findAll(previousHomePlanetIds).minus(futureMyPlanets);
		for (Planet p : myPlanetz.planets) {
			if (enemyFleetz.fleetsAttacking(p).size() > 0) {
				return;
			}
		}
		for (Planet p : enemyPlanetz.planets) {
			if (myFleetz.fleetsAttacking(p).size() > 0) {
				return;
			}
		}
		if (futureMyPlanets.production() > futureEnemyPlanets.production() || lostPlanets.size() > 0) {
			return;
		}
		
		targets = sortByValue(targets);
		
		Planets sourcePlanets = myPlanetz.minus(mostStrategicPlanets);
		for (Planet target : targets.planets) {
			Planets sorted = sourcePlanets.sortByDistanceTo(target);
			for (Planet homePlanet : sorted.planets) {
				Planets remove = enemyPlanetz.remove(target);
				Planet closestEnemy = remove.findClosestTo(homePlanet);
				if (closestEnemy == null) {
					closestEnemy = target;
				}
				if (distance(homePlanet, target) <= distance(homePlanet, closestEnemy) || enemyPlanetz.averageDistanceToPlanets(target) >= myPlanetz.averageDistanceToPlanets(target)) {
					Integer shipsToSend = safelySparableShips(homePlanet, target);
					int neededToWin = shipsNeededToWin(homePlanet, target);
					if (neededToWin == 0 || shipsToSend == 0) return;
					if (shipsToSend >= neededToWin) {
						shipsToSend = neededToWin;
					}
					if (!wouldBeBetterToWait(targets, homePlanet, target)) {
						addAttack(homePlanet, target, shipsToSend);
					}
				}
			}
		}
	}

	private Planets mostStrategicPlanets() {
		Planets planets = planetz.findAll(closestFuturePlanetsToTheEnemy.union(closestPlanetsToTheEnemy).ids());
		return planets.unique().rejectWithinSafeZone(planetz.findAll(enemyPlanetz.union(futureEnemyPlanets).ids())).sortByRescueValue(planetz);
	}

	private int totalShipRemainingToBeOrdered() {
		int totalShipsRemaining = 0;
		for (Planet p : remainingHomePlanets()) {
			totalShipsRemaining += safelySparableShips(p);
		}
		return totalShipsRemaining;
	}

	private Integer shipsNeededToWin(Planet homePlanet, Planet target) {
		int neededToWin = target.neededToWin(enemyFleetz, myFleetz, homePlanet.distance(target)); 
		Planet closestEnemy = enemyPlanetz.findClosestTo(target);
		int growthBeforeEnemyAttack = target.growthRate * target.distance(closestEnemy);
		// mitigate sniping
		if (target.isNeutral() && closestEnemy.numShips > growthBeforeEnemyAttack && enemyPlanetz.averageDistanceToPlanets(target) < enemyPlanetz.averageDistanceToPlanets(homePlanet)) {
			int additionalNeeded = closestEnemy.numShips > growthBeforeEnemyAttack ? closestEnemy.numShips - growthBeforeEnemyAttack : 0;
			neededToWin = neededToWin + additionalNeeded;
		}
		return neededToWin;
	}

	private int shipsNeededToSave(Planet homePlanet, Planet closest, List<Fleet> enemyFleets, List<Fleet> myFleets) {
		return homePlanet.neededToWin(enemyFleetz, myFleetz, homePlanet.distance(closest));
	}

	private void rescuePlanets() {
		Planets rescuable = planetz.findAll(futureMyPlanets.union(myPlanetz).sortByRescueValue(planetz).ids());
		for (Planet homePlanet : rescuable.planets) {
			Planets remaining = new Planets(remainingHomePlanets()).remove(homePlanet);
			while (remaining.size() > 0) {
				Planet closest = remaining.findClosestTo(homePlanet);
				Planet closestEnemyToRescuer = enemyPlanetz.findClosestTo(closest);
				Planet closestFriendToRescuer = myPlanetz.findClosestTo(closest);
				remaining = remaining.remove(closest);
				if (safelySparableShips(closest) == 0
						|| distance(closestEnemyToRescuer, closest) < distance(closestFriendToRescuer, closest)) {
					continue;
				}
				int expectedNeed = shipsNeededToSave(homePlanet, closest, enemyFleets(), myFleets());
				if (expectedNeed > 0 && expectedNeed < totalShipRemainingToBeOrdered()) {
					Integer shipsToSend = safelySparableShips(closest);
					if (shipsToSend > expectedNeed) {
						shipsToSend = expectedNeed;
					}
					addAttack(closest, homePlanet, shipsToSend);
				} else
					break;
			}
		}
	}

	int distance(Planet source, Planet destination) {
		return source.distance(destination);
	}

	int distance(double x1, double y1, double x2, double y2) {
		double dx = x1 - x2;
		double dy = y1 - y2;
		return (int) Math.ceil(Math.sqrt(dx * dx + dy * dy));
	}

	private Integer safelySparableShips(Planet homePlanet) {
		return safelySparableShips(homePlanet, null);
	}

	private int safelySparableShips(Planet homePlanet, Planet target) {
		int available = 0;
		Planet enemy = enemyPlanetz.findClosestTo(homePlanet);
		if (homePlanet != null && enemy != null && homePlanet.sparableShips(enemyFleetz, myFleetz) > 0) {
			if (shouldLimitPlanetsOutput(homePlanet, enemy) && enemy != target && mostStrategicPlanets.contains(homePlanet)) {
				int amountToSpare = safeAmountToSpare(homePlanet, enemy);
				if (amountToSpare < homePlanet.sparableShips(enemyFleetz, myFleetz)) {
					available = amountToSpare;
				} else {
					available = homePlanet.sparableShips(enemyFleetz, myFleetz);
				}
			} else {
				available = homePlanet.sparableShips(enemyFleetz, myFleetz);
			}
		}
		return available;
	}

	private int safeAmountToSpare(Planet homePlanet, Planet enemy) {
		int amountToSpare = homePlanet.numShips - enemy.numShips + distance(homePlanet, enemy)
				* homePlanet.growthRate;
		amountToSpare = amountToSpare > homePlanet.numShips ? homePlanet.numShips : amountToSpare;
		return amountToSpare > 0 ? amountToSpare : 0;
	}

	private boolean shouldLimitPlanetsOutput(Planet homePlanet, Planet enemy) {
		Planet closestFriend = myPlanetz.remove(homePlanet).findClosestTo(homePlanet);
		if (closestFriend == null)
			return true;
		int closerFriendPlanetShips = myPlanetz.otherPlanetsWithinRadius(homePlanet, distance(homePlanet, enemy)).ships();
		return closerFriendPlanetShips < enemy.numShips;
	}

	List<Attack> attackOrders() {
		numberOfTurns++;
		this.startOfRescue = System.currentTimeMillis();
		rescuePlanets();
		this.endOfRescue = System.currentTimeMillis();
		snipingAttack();
		attackPlanets();
		this.endOfAttack = System.currentTimeMillis();
		this.endOfAttack = System.currentTimeMillis();
		moveRemainingFederationToCloseNeutrals();
		moveRemainingFederationToMostStrategicPlanets();
		this.endOfMove = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		log("Turn " + numberOfTurns + " ended in " + (endTime - this.startTime) + " milliseconds.\n" 
				+ "Initialize time was " + (this.initializeEndTime - this.startTime) + " milliseconds.\n"
				+ "Rescue time was " + (this.endOfRescue - this.startOfRescue) + " milliseconds.\n"
				+ "Attack time was " + (this.endOfAttack - this.endOfRescue) + " milliseconds.\n"
				+ "Move time was " + (this.endOfMove - this.endOfAttack) + " milliseconds.\n"
				+ "Cold War time was " + (endTime - this.endOfMove) + " milliseconds.\n"
		);
		System.gc();
		return attacks;
	}

	private void snipingAttack() {
		for(Planet planet : neutralPlanetz.planets) {
			if(planet.distance(myPlanetz.findClosestTo(planet)) <= planet.distance(enemyPlanetz.findClosestTo(planet))) {
				Fleets attacking = enemyFleetz.fleetsAttacking(planet);
				if (attacking.size() == 1 && myFleetz.fleetsAttacking(planet).size() == 0) {
					int turnsRemaining = attacking.sortByTurnsRemaining().first().turnsRemaining;
					for (Planet myPlanet : myPlanetz.sortByDistanceTo(planet).planets) {
						if (turnsRemaining <= myPlanet.distance(planet)) {
							attack(myPlanet, planet);
							break;
						}
					}
				}
			}
		}
	}

	private Planets sortByValue(Planets targets) {
		List<Planet> sortedPlanets = new ArrayList<Planet>(targets.planets);
		Collections.sort(sortedPlanets, new Comparator<Planet>() {
			@Override
			public int compare(Planet p1, Planet p2) {
				return new Integer(attackValue(p2)).compareTo(new Integer(attackValue(p1)));
			}
		});
		return new Planets(sortedPlanets);
	}

	private int attackValue(Planet planet) {
		double growthRatio = planet.growthRate / (double) planetz.maxGrowthRate();
		double distanceRatio = myPlanetz.averageDistanceToPlanets(planet)
				/ (double) planetz.maximumDistanceBetweenAnyTwoPlanets();
		int enemyValue = 0;
		if (planet.owner == ENEMY_ID) {
			enemyValue = 50;
			if (closestEnemiesToAnyOfMyPlanets.contains(planet)) {
				enemyValue = 100;
			}
		}
		
		int retakeValue = 0;
		if (previousHomePlanetIds.contains(planet.id)) {
			retakeValue = 100;
		}

		int tooExpensive = 0;
		if (planet.numShips > myPlanetz.ships() * (double) 2 / 3) {
			tooExpensive = 500;
		}

		// cost considers distance, but this helps
		int distanceValue = 100 - (int) (100 * distanceRatio);
		int cost = (planet.numShips + enemyFleetz.shipsAttacking(planet) - myFleetz.shipsAttacking(planet));
		int growthValue = (int) (150 * growthRatio);

		int poorRatioCost = 0;
		if (neutralPlanetz.contains(planet) && planet.numShips / (planet.growthRate + 1) > 20) {
			poorRatioCost = 500;
		}

		int value = retakeValue + distanceValue + growthValue + enemyValue - cost - poorRatioCost - tooExpensive;
		return value;
	}

	// Returns a list of all the planets.
	List<Planet> planets() {
		return planets;
	}

	List<Planet> myPlanets() {
		List<Planet> r = new ArrayList<Planet>();
		for (Planet p : planets) {
			if (p.owner == PLAYER_ID) {
				r.add(p);
			}
		}
		return r;
	}

	List<Planet> neutralPlanets() {
		List<Planet> r = new ArrayList<Planet>();
		for (Planet p : planets) {
			if (p.owner == NEUTRAL_ID) {
				r.add(p);
			}
		}
		return r;
	}

	List<Planet> enemyPlanets() {
		List<Planet> r = new ArrayList<Planet>();
		for (Planet p : planets) {
			if (p.owner >= ENEMY_ID) {
				r.add(p);
			}
		}
		return r;
	}

	List<Fleet> myFleets() {
		List<Fleet> r = new ArrayList<Fleet>();
		for (Fleet f : fleets) {
			if (f.owner == 1) {
				r.add(f);
			}
		}
		return r;
	}

	List<Fleet> enemyFleets() {
		List<Fleet> r = new ArrayList<Fleet>();
		for (Fleet f : fleets) {
			if (f.owner != 1) {
				r.add(f);
			}
		}
		return r;
	}

	void IssueOrder(Planet source, Planet dest, int numShips) {
		System.out.println("" + source.id + " " + dest.id + " " + numShips);
		System.out.flush();
	}

	void finishTurn() {
		System.out.println("go");
		System.out.flush();
	}

	private int parseGameState(String s) {
		planets.clear();
		fleets.clear();
		int planetID = 0;
		String[] lines = s.split("\n");
		for (int i = 0; i < lines.length; ++i) {
			String line = lines[i];
			int commentBegin = line.indexOf('#');
			if (commentBegin >= 0) {
				line = line.substring(0, commentBegin);
			}
			if (line.trim().length() == 0) {
				continue;
			}
			String[] tokens = line.split(" ");
			if (tokens.length == 0) {
				continue;
			}
			if (tokens[0].equals("P")) {
				if (tokens.length != 6) {
					return 0;
				}
				double x = Double.parseDouble(tokens[1]);
				double y = Double.parseDouble(tokens[2]);
				int owner = Integer.parseInt(tokens[3]);
				int numShips = Integer.parseInt(tokens[4]);
				int growthRate = Integer.parseInt(tokens[5]);
				Planet p = new Planet(planetID++, owner, numShips, growthRate, x, y);
				planets.add(p);
			} else if (tokens[0].equals("F")) {
				if (tokens.length != 7) {
					return 0;
				}
				int owner = Integer.parseInt(tokens[1]);
				int numShips = Integer.parseInt(tokens[2]);
				int source = Integer.parseInt(tokens[3]);
				int destination = Integer.parseInt(tokens[4]);
				int totalTripLength = Integer.parseInt(tokens[5]);
				int turnsRemaining = Integer.parseInt(tokens[6]);
				Fleet f = new Fleet(owner, numShips, source, destination, totalTripLength, turnsRemaining);
				fleets.add(f);
			} else {
				return 0;
			}
		}
		return 1;
	}

	static void log(String message) {
		if ("test".equals(System.getenv("ENVIRONMENT"))) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("runlog.txt", true));
				out.write(message);
				out.newLine();
				out.close();
			} catch (IOException e) {
			}
		}
	}

	static void log(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		e.printStackTrace(pw);
		pw.flush();
		sw.flush();
		log(sw.toString());
	}

}
