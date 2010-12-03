import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


	public class Fleets {

		private List<Fleet> fleets;

		List<Fleet> fleets() {
			return new ArrayList<Fleet>(fleets);
		}
		
		public Fleets(List<Fleet> fleets) {
			this.fleets = fleets;
		}

		public Fleets(Fleet...fleets) {
			this.fleets = new ArrayList<Fleet>(Arrays.asList(fleets));
		}
		
		public List<Fleet> findByTurnsRemaining(int turnsRemaining) {
			List<Fleet> attacking = new ArrayList<Fleet>();
			for (Fleet fleet : fleets()) {
				if (fleet.turnsRemaining == turnsRemaining) {
					attacking.add(fleet);
				}
			}
			return attacking;
		}

		int shipsAttacking(Planet homePlanet) {
			int attacking = 0;
			for (Fleet fleet : fleetsAttacking(homePlanet).fleets) {
				attacking += fleet.numShips;
			}
			return attacking;
		}

		Fleets fleetsAttacking(Planet p) {
			List<Fleet> attacking = new ArrayList<Fleet>();
			for (Fleet fleet : fleets) {
				if (fleet.destinationPlanet == p.id) {
					attacking.add(fleet);
				}
			}
			return new Fleets(attacking);
		}

		int distanceToClosest(Planet planet) {
			int closestFleet = 0;
			if (!fleets.isEmpty()) {
				closestFleet = fleets.get(0).turnsRemaining;
				for (Fleet fleet : fleets) {
					if (fleet.turnsRemaining < closestFleet) {
						closestFleet = fleet.turnsRemaining;
					}
				}
			}
			return closestFleet;
		}

		int distanceOfNearestAttackingFleet(Planet homePlanet) {
			int distance = Integer.MAX_VALUE;
			for (Fleet fleet : fleetsAttacking(homePlanet).fleets) {
				if (fleet.turnsRemaining < distance)
					distance = fleet.turnsRemaining;
			}
			return distance;
		}

		Fleets sortByTurnsRemaining() {
			List<Fleet> sortedFleets = new ArrayList<Fleet>(fleets);
			Collections.sort(sortedFleets, new Comparator<Fleet>() {
				@Override
				public int compare(Fleet o1, Fleet o2) {
					if (o1.turnsRemaining == o2.turnsRemaining) {
						return new Integer(o1.owner).compareTo(o2.owner);
					} else {
						return new Integer(o1.turnsRemaining).compareTo(o2.turnsRemaining);
					}
				}
			});
			return new Fleets(sortedFleets);
		}

		int size() {
			return fleets.size();
		}

		Fleet first() {
			return fleets.get(0);
		}

		int ships() {
			int ships = 0;
			for (Fleet fleet : fleets) {
				ships += fleet.numShips;
			}
			return ships;
		}

		public void update(Fleet newFleet) {
			fleets.add(newFleet);
		}

		public Fleets plus(Fleets myFleets) {
			List<Fleet> fleet = new ArrayList<Fleet>(fleets);
			fleet.addAll(myFleets.fleets);
			return new Fleets(fleet);
		}

		public Fleet last() {
			return fleets.get(fleets.size() - 1);
		}

		public Fleets sliceByTurnsRemaining(int start, int end) {
			List<Fleet> newFleets = new ArrayList<Fleet>();
			for (Fleet f : fleets) {
				if (start <= f.turnsRemaining && f.turnsRemaining <= end) {
					newFleets.add(f);
				}
			}
			return new Fleets(newFleets);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fleets == null) ? 0 : fleets.hashCode());
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
			Fleets other = (Fleets) obj;
			if (fleets == null) {
				if (other.fleets != null)
					return false;
			} else if (!fleets.equals(other.fleets))
				return false;
			return true;
		}

		public String toString() {
			StringBuilder string = new StringBuilder();
			for(Fleet f : fleets) {
				string.append(f.toString());
				string.append("\n");
			}
			return string.toString();
		}

		public Fleets plus(Fleet fleet) {
			return plus(new Fleets(fleet));
		}

		public Fleets sentBy(Planet planet) {
			List<Fleet> sent = new ArrayList<Fleet>();
			for (Fleet fleet : fleets) {
				if (fleet.sourcePlanet == planet.id) {
					sent.add(fleet);
				}
			}
			return new Fleets(sent);
		}
	}
