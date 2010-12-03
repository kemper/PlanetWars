import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Planets {

	List<Planet> planets;

	public Planets(Collection<Planet> planets) {
		this.planets = new ArrayList<Planet>(planets);
	}

	public Planets(Planet... planets) {
		this(Arrays.asList(planets));
	}

	int minimumDistanceFrom(Planet target) {
		int distance = Integer.MAX_VALUE;
		for (Planet planet : planets) {
			if (distance(planet, target) < distance) {
				distance = distance(planet, target);
			}
		}
		return distance;
	}

	double averageDistanceToPlanets(Planet o1) {
		if (planets.size() == 0)
			return Double.MAX_VALUE;
		double totalDistance = 0;
		for (Planet planet : planets) {
			totalDistance += distance(planet, o1);
		}
		return totalDistance / planets.size();
	}

	int minDistanceToPlanets(Planet o1, List<Planet> planets) {
		int minDistance = Integer.MAX_VALUE;
		if (planets.size() > 0) {
			for (Planet planet : planets) {
				if (minDistance > distance(o1, planet)) {
					minDistance = distance(o1, planet);
				}
			}
		}
		return minDistance;
	}

	int averageDistanceFrom(List<Planet> planets) {
		int average = 0;
		for (Planet planet : planets) {
			average += averageDistanceToPlanets(planet);
		}
		return average / planets.size();
	}

	Planets sortByMinimumDistanceToTheEnemy(final Planets enemy) {
		List<Planet> sorted = new ArrayList<Planet>(planets);
		Collections.sort(sorted, new Comparator<Planet>() {
			@Override
			public int compare(Planet o1, Planet o2) {
				int d1 = distance(o1, enemy.findClosestTo(o1));
				int d2 = distance(o2, enemy.findClosestTo(o2));
				return new Integer(d1).compareTo(new Integer(d2));
			}

		});
		return new Planets(sorted);
	}

	int minDistanceTo(Planet o1) {
		int minDistance = Integer.MAX_VALUE;
		if (planets.size() > 0) {
			for (Planet planet : planets) {
				if (minDistance > distance(o1, planet)) {
					minDistance = distance(o1, planet);
				}
			}
		}
		return minDistance;
	}

	Planet largest() {
		if (planets.isEmpty())
			return null;
		Planet planet = planets.get(0);
		for (Planet p : planets) {
			if (p.numShips > planet.numShips) {
				planet = p;
			}
		}
		return planet;
	}

	Planet findClosestTo(Planet planet) {
		Planet closest = null;
		for (Planet p : planets) {
			if (closest == null || distance(planet, p) < distance(planet, closest)) {
				closest = p;
			}
		}
		return closest;
	}

	int distance(Planet source, Planet destination) {
		double dx = source.x - destination.x;
		double dy = source.y - destination.y;
		return (int) Math.ceil(Math.sqrt(dx * dx + dy * dy));
	}

	int maxGrowthRate() {
		int rate = Integer.MIN_VALUE;
		for (Planet planet1 : planets) {
			if (planet1.growthRate > rate) {
				rate = planet1.growthRate;
			}
		}
		return rate;
	}

	int production() {
		int prod = 0;
		for (Planet p : planets) {
			prod += p.growthRate;
		}
		return prod;
	}

	int maximumDistanceBetweenAnyTwoPlanets() {
		int distance = 0;
		for (Planet planet1 : planets) {
			for (Planet planet2 : planets) {
				if (distance(planet1, planet2) > distance) {
					distance = distance(planet1, planet2);
				}
			}
		}
		return distance;
	}

	Planet planet(int id) {
		for (Planet p : planets) {
			if (p.id == id)
				return p;
		}
		return null;
	}

	Planets allCloserTo1Than2(Planet p1, Planet p2) {
		List<Planet> closest = new ArrayList<Planet>();
		for (Planet p : planets) {
			if (distance(p2, p) >= distance(p1, p)) {
				closest.add(p);
			}
		}
		return new Planets(closest);
	}

	Planet first() {
		return planets.get(0);
	}

	Planets remove(Planet planet) {
		List<Planet> newPlanets = new ArrayList<Planet>(planets);
		newPlanets.remove(planet);
		return new Planets(newPlanets);
	}

	Planets otherPlanetsWithinRadius(Planet planet, int distance) {
		List<Planet> closest = new ArrayList<Planet>();
		for (Planet p : planets) {
			if (planet != p && distance(planet, p) < distance) {
				closest.add(p);
			}
		}
		return new Planets(closest);
	}

	Planets sortByDistanceTo(final Planet planet) {
		List<Planet> sortedPlanets = new ArrayList<Planet>(planets);
		Collections.sort(sortedPlanets, new Comparator<Planet>() {
			@Override
			public int compare(Planet o1, Planet o2) {
				Integer distance1 = distance(planet, o1);
				Integer distance2 = distance(planet, o2);
				return distance1.compareTo(distance2);
			}
		});
		return new Planets(sortedPlanets);
	}

	int ships() {
		int ships = 0;
		for (Planet planet : planets) {
			ships += planet.numShips;
		}
		return ships;
	}

	Planets sortByRescueValue(final Planets planetz) {
		List<Planet> sortedPlanets = new ArrayList<Planet>(planets);
		Collections.sort(sortedPlanets, new Comparator<Planet>() {
			@Override
			public int compare(Planet p1, Planet p2) {
				return new Integer(value(p2)).compareTo(new Integer(value(p1)));
			}

			private int value(Planet planet) {
				double growthRatio = planet.growthRate / (double) maxGrowthRate();
				int graphDistanceValue = (int) (50 - (50 * averageDistanceToPlanets(planet)) / averageDistanceFrom(planetz.planets));
				int growthValue = (int) (100 * growthRatio);
				int value = graphDistanceValue + growthValue;
				return value;
			}
		});
		return new Planets(sortedPlanets);
	}

	Planets sortByGrowthPotential(final Planet referencePlanet, final int maxTurns) {
		List<Planet> sortedPlanets = new ArrayList<Planet>(planets);
		Collections.sort(sortedPlanets, new Comparator<Planet>() {
			@Override
			public int compare(Planet o1, Planet o2) {
				Integer potential1 = (maxTurns - distance(referencePlanet, o1)) * o1.growthRate - o1.numShips;
				Integer potential2 = (maxTurns - distance(referencePlanet, o2)) * o2.growthRate - o2.numShips;
				return potential2.compareTo(potential1);
			}
		});
		return new Planets(sortedPlanets);
	}
	
	public Planets before(Planet splitter) {
		List<Planet> before = new ArrayList<Planet>();
		for (Planet planet : planets) {
			if (splitter.equals(planet)) {
				return new Planets(before);
			}
			before.add(planet);
		}
		return new Planets(before);
	}

	void print() {
		System.out.println(toString());
	}

	public String toString() {
		StringBuilder string = new StringBuilder();
		for (Planet p : planets) {
			string.append(p.toString());
			string.append("\n");
		}
		return string.toString();
	}

	public boolean contains(Planet planet) {
		return planets.contains(planet);
	}

	boolean contains(int id) {
		for (Planet p : planets) {
			if (p.id == id) {
				return true;
			}
		}
		return false;
	}

	public Planet second() {
		return planets.get(1);
	}

	public int size() {
		return planets.size();
	}

	public Planet last() {
		return planets.get(planets.size() - 1);
	}

	boolean beingAttacked(Fleets fleets) {
		boolean beingAttacked = false;
		for (Planet p : planets) {
			if (fleets.fleetsAttacking(p).size() > 0) {
				beingAttacked = true;
			}
		}
		return beingAttacked;
	}

	public Planets sortByAverageDistanceTo(final Planets enemyPlanetz) {
		List<Planet> planets = this.planets();
		Collections.sort(planets, new Comparator<Planet>() {
			@Override
			public int compare(Planet o1, Planet o2) {
				return new Double(enemyPlanetz.averageDistanceToPlanets(o1)).compareTo(new Double(enemyPlanetz
						.averageDistanceToPlanets(o2)));
			}
		});
		return new Planets(planets);
	}

	List<Planet> planets() {
		return new ArrayList<Planet>(planets);
	}

	public Planets inTheFuture(Fleets fleets) {
		List<Planet> futurePlanets = new ArrayList<Planet>();
		for (Planet planet : planets) {
			futurePlanets.add(planet.stateAfterFleetsAttack(fleets));
		}
		return new Planets(futurePlanets);
	}

	public Planets myPlanets() {
		List<Planet> myPlanets = new ArrayList<Planet>();
		for (Planet planet : planets) {
			if (planet.owner == PlanetWars.PLAYER_ID) {
				myPlanets.add(planet);
			}
		}
		return new Planets(myPlanets);
	}

	public Planets neutralPlanets() {
		List<Planet> myPlanets = new ArrayList<Planet>();
		for (Planet planet : planets) {
			if (planet.owner == PlanetWars.NEUTRAL_ID) {
				myPlanets.add(planet);
			}
		}
		return new Planets(myPlanets);
	}

	public Planets enemyPlanets() {
		List<Planet> enemyPlanets = new ArrayList<Planet>();
		for (Planet planet : planets) {
			if (planet.owner == PlanetWars.ENEMY_ID) {
				enemyPlanets.add(planet);
			}
		}
		return new Planets(enemyPlanets);
	}

	public Planets union(Planets myPlanetz) {
		LinkedHashSet<Planet> set = new LinkedHashSet<Planet>(planets);
		set.addAll(myPlanetz.planets);
		return new Planets(new ArrayList<Planet>(set));
	}

	private Planets union(Planet p) {
		return union(new Planets(p));
	}

	public Set<Integer> ids() {
		Set<Integer> ids = new HashSet<Integer>();
		for (Planet p : planets) {
			ids.add(p.id);
		}
		return ids;
	}

	public Planets rejectWithinSafeZone(Planets enemyPlanets) {
		Planets planets = new Planets();
		for (Planet p : this.planets) {
			if (planets.size() == 0) {
				planets = planets.union(p);
			} else {
				int distance = p.distance(enemyPlanets.findClosestTo(p));
				int otherDistance = p.distance(planets.remove(p).findClosestTo(p));
				if (distance < otherDistance) {
					planets = planets.union(p); 
				}
			}
		}
		return planets;
	}

	public Planets unique() {
		return new Planets(new ArrayList<Planet>(new LinkedHashSet<Planet>(this.planets)));
	}

	public Planets minus(Planets planets) {
		List<Planet> newPlanets = new ArrayList<Planet>(this.planets);
		newPlanets.removeAll(planets.planets);
		return new Planets(newPlanets);
	}

	public Planets findAll(Set<Integer> ids) {
		List<Planet> found = new ArrayList<Planet>();
		for(Integer id : ids) {
			found.add(planet(id));
		}
		return new Planets(found);
	}

	public Planets reverse() {
		List<Planet> planets = planets();
		Collections.reverse(planets);
		return new Planets(planets);
	}

	public Planets sortByLargest() {
		List<Planet> sortedPlanets = new ArrayList<Planet>(planets);
		Collections.sort(sortedPlanets, new Comparator<Planet>() {
			@Override
			public int compare(Planet o1, Planet o2) {
				Integer ships1 = o1.numShips;
				Integer ships2 = o2.numShips;
				return ships2.compareTo(ships1);
			}
		});
		return new Planets(sortedPlanets);
	}

	public Planets closestTo(Planets planetz) {
		Set<Planet> closest = new HashSet<Planet>();
		if (size() > 0) {
			for(Planet planet : planetz.planets) {
				closest.add(findClosestTo(planet));
			}
		}
		return new Planets(closest);
	}

	// myPlanetz.proximityGroups(enemyPlanetz)
	public Map<Planet, Set<Planet>> proximityGroupsSet(Planets planetz) {
		HashMap<Planet, Set<Planet>> hashMap = new HashMap<Planet, Set<Planet>>();
		if (size() > 0) {
			for(Planet planet : planetz.planets) {
				Planet findClosestTo = findClosestTo(planet);
				if (hashMap.get(findClosestTo) == null) {
					Set<Planet> hashSet = new HashSet<Planet>();
					hashSet.add(planet);
					hashMap.put(findClosestTo, hashSet);
				} else {
					Set<Planet> hashSet = hashMap.get(findClosestTo);
					hashSet.add(planet);
				}
			}
		}
		return hashMap;
	}

	public Map<Planet, Planets> groupByClosest(Planets planetz) {
		Map<Planet, Set<Planet>> hashMap = proximityGroupsSet(planetz);
		Map<Planet, Planets> hashMap2 = new HashMap<Planet, Planets>();
		for (Planet p : hashMap.keySet()) {
			hashMap2.put(p, new Planets(hashMap.get(p)));
		}
		return hashMap2;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((planets == null) ? 0 : planets.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Planets other = (Planets) obj;
		if (planets == null) {
			if (other.planets != null)
				return false;
		} else if (!planets.equals(other.planets))
			return false;
		return true;
	}


}
