
public class Fleet {
	final int owner;
	final int numShips;
	final int sourcePlanet;
	final int destinationPlanet;
	final int totalTripLength;
	final int turnsRemaining;

	public Fleet(int owner, int numShips, int sourcePlanet,
			int destinationPlanet, int totalTripLength, int turnsRemaining) {
		this.owner = owner;
		this.numShips = numShips;
		this.sourcePlanet = sourcePlanet;
		this.destinationPlanet = destinationPlanet;
		this.totalTripLength = totalTripLength;
		this.turnsRemaining = turnsRemaining;
	}


	public Fleet(int owner, int numShips) {
		this.owner = owner;
		this.numShips = numShips;
		this.sourcePlanet = -1;
		this.destinationPlanet = -1;
		this.totalTripLength = -1;
		this.turnsRemaining = -1;
	}

	public String toString() {
		return "owner=" + owner 
				+ ", numShips=" + numShips 
				+ ", sourcePlanet=" + sourcePlanet 
				+ ", destinationPlanet=" + destinationPlanet
				+ ", totalTripLength=" + totalTripLength
				+ ", turnsRemaining=" + turnsRemaining; 
	}
}
