
public class Attack {
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((target == null) ? 0 : target.hashCode());
		result = prime * result
				+ ((homePlanet == null) ? 0 : homePlanet.hashCode());
		result = prime * result + ships;
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
		Attack other = (Attack) obj;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		if (homePlanet == null) {
			if (other.homePlanet != null)
				return false;
		} else if (!homePlanet.equals(other.homePlanet))
			return false;
		if (ships != other.ships)
			return false;
		return true;
	}
	public Attack(Planet home, Planet enemy, int ships) {
		this.homePlanet = home;
		this.target = enemy;
		this.ships = ships;
	}
	
	@Override
	public String toString() {
		return "Attack(homePlanetId=" + homePlanet.id + ", otherPlanetId=" + target.id + ", ships=" + ships;
	}
	
	public Planet target;
	public Planet homePlanet;
	public int ships;
}
