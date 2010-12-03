import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Planet {
	protected int id;
	protected int owner;
	protected int numShips;
	protected int growthRate;
	protected double x, y;
	private Integer sparableShips;
	private int lastEnemyFleetCount;
	private int lastMyFleetCount;
	int stepsIntoFuture = 0;

	public Planet(Planet planet, int ships) {
		this(planet);
		this.numShips(ships);
	}

	protected Planet(Planet _p) {
		id = _p.id;
		owner = _p.owner;
		numShips = _p.numShips;
		growthRate = _p.growthRate;
		x = _p.x;
		y = _p.y;
	}
	
	public Planet(int planetID, int owner, int numShips, int growthRate,
			double x, double y) {
		this.id = planetID;
		this.owner = owner;
		this.numShips = numShips;
		this.growthRate = growthRate;
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString() {
		return "Planet " + id + ", Size: " + numShips + " (x = " + x + ", y = " + y + "), growthRate: " + growthRate;  
	}
	
	public void numShips(int newNumShips) {
		this.numShips = newNumShips;
	}

	public Planet stateAfterFleetsAttack(Fleets fleets) {
		fleets = fleets.sortByTurnsRemaining();
		Planet planet = new Planet((Planet) this);
		return stateAfterFleetsAttack(fleets, planet);
	}
	
	public Planet stateAfterFleetsAttack(Fleets fleets, Planet futurePlanet) {
		futurePlanet = new Planet(futurePlanet);
		fleets = fleets.fleetsAttacking(this);
		fleets = fleets.sortByTurnsRemaining();
		int turnsSoFar = futurePlanet.stepsIntoFuture;
		for (Fleet f : fleets.fleets()) {
			futurePlanet.growUnlessNeutral(f.turnsRemaining - turnsSoFar);
			turnsSoFar = f.turnsRemaining;
			futurePlanet.handleAttack(f);
		}
		futurePlanet.stepsIntoFuture = turnsSoFar;
		return futurePlanet;
	}

	protected void growUnlessNeutral(int turnsRemaining) {
		if (owner != PlanetWars.NEUTRAL_ID) {
			this.numShips += this.growthRate * turnsRemaining;
		}
	}

	protected void handleAttack(Fleet f) {
		if (f.owner != owner) {
			if (f.numShips > this.numShips) {
				this.numShips = f.numShips - this.numShips;
				this.owner = f.owner;
			} else {
				this.numShips -= f.numShips;
			}
		} else {
			this.numShips += f.numShips;
		}
	}

	public boolean isEnemy() {
		return owner == PlanetWars.ENEMY_ID;
	}
	
	public boolean isMine() {
		return owner == PlanetWars.PLAYER_ID;
	}
	
	public boolean isNeutral() {
		return owner == PlanetWars.NEUTRAL_ID;
	}
	
	public int sparableShips(Fleets enemyFleets, Fleets myFleets) {
		if (this.sparableShips != null && this.lastEnemyFleetCount == enemyFleets.size() && this.lastMyFleetCount == myFleets.size()) {
			return this.sparableShips;
		}
		Fleets all = enemyFleets.plus(myFleets).fleetsAttacking(this).sortByTurnsRemaining();
		if (all.size() == 0) {
			return numShips > 0 ? numShips - 1 : 0;
		}
		int lastAmountToSpare = 0;
		int amountToSpare = numShips;
		int bestKnownAmountToSpare = 0;
		int upperLimit = numShips;
		int lowerLimit = 0;
		Planet possibleFuture;
		boolean done = false; 
		do {
			possibleFuture = new Planet((Planet) this, numShips - amountToSpare);
			List<Fleet> testFleets = new ArrayList<Fleet>();
			for (int x = 0; x <= all.last().turnsRemaining; x++) {
				List<Fleet> fleets = all.findByTurnsRemaining(x);
				if (fleets.size() > 0) {
					possibleFuture = new Planet((Planet) this, numShips - amountToSpare);
					testFleets.addAll(fleets);
					possibleFuture = possibleFuture.stateAfterFleetsAttack(new Fleets(testFleets));
					if (possibleFuture.isEnemy()) break;
					if (testFleets.size() == all.size() && amountToSpare > bestKnownAmountToSpare) {
						bestKnownAmountToSpare = amountToSpare;
					}
				}
			}
			if (possibleFuture.isEnemy()){
				upperLimit = amountToSpare;
				amountToSpare = (upperLimit - lowerLimit) / 2;
			} else {
				lowerLimit = amountToSpare;
				amountToSpare = lowerLimit + (upperLimit - lowerLimit) / 2;
			}
			if (lastAmountToSpare == amountToSpare || amountToSpare >= numShips) {
				done = true;
			}
			lastAmountToSpare = amountToSpare;
			
		} while (!done && amountToSpare > 1);
		this.sparableShips = bestKnownAmountToSpare > 0 ? bestKnownAmountToSpare - 1 : 0;
		this.lastEnemyFleetCount = enemyFleets.size();
		this.lastMyFleetCount = myFleets.size();
		return this.sparableShips;
	}
	
	int distance(Planet other) {
		double dx = x - other.x;
		double dy = y - other.y;
		return (int) Math.ceil(Math.sqrt(dx * dx + dy * dy));
	}

	public int neededToWin(Fleets enemyFleets, Fleets myFleets, int distance) {
		Fleets all = enemyFleets.plus(myFleets).sortByTurnsRemaining();
		Planet futurePlanet = stateAfterFleetsAttack(all);
		if (futurePlanet.isEnemy()) {
			if (distance >= futurePlanet.stepsIntoFuture) {
				futurePlanet.growUnlessNeutral(distance - futurePlanet.stepsIntoFuture);
			} else {
				return neededToWinBeforeEnemyDoes(all, distance);
			}
			return futurePlanet.numShips + 1;
		} else if (futurePlanet.isNeutral()) {
			return futurePlanet.numShips + 1;
		}
		return 0;
	}

	private int neededToWinBeforeEnemyDoes(Fleets all, int distance) {
		Fleets before = all.sliceByTurnsRemaining(0, distance);
		Fleets after = all.sliceByTurnsRemaining(distance + 1, all.last().turnsRemaining);
		boolean done = false;
		int lowerBound = 0;
		int upperBound = numShips + all.ships(); // TODO: optimize: only consider enemy ships
		int neededToWin = (upperBound - lowerBound) / 2;
		while (!done && neededToWin > 1) {
			Fleet fleet = new Fleet(PlanetWars.PLAYER_ID, neededToWin, 0, this.id, 0, distance);
			Planet possibleFuture = stateAfterFleetsAttack(before.plus(fleet).plus(after));
			if (possibleFuture.isEnemy()) {
				lowerBound = neededToWin;
			} else {
				upperBound = neededToWin;
			}
			neededToWin = lowerBound + (int) Math.ceil((upperBound - lowerBound) / (double) 2);
			if (upperBound == lowerBound || upperBound < lowerBound
					|| (possibleFuture.isMine() && Math.abs(upperBound - lowerBound) < 2)) {
				done = true;
			}
		}
		return neededToWin;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		Planet other = (Planet) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
