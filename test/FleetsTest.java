import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class FleetsTest {

	@Test
	public void splits_fleets_by_turns_remaining() {
		Fleets notExpected = new Fleets(
			new Fleet(0, 0, 0, 0, 0, 1),
			new Fleet(0, 0, 0, 0, 0, 5),
			new Fleet(0, 0, 0, 0, 0, 6));
		Fleets expected = new Fleets(
				new Fleet(0, 0, 0, 0, 0, 2),
				new Fleet(0, 0, 0, 0, 0, 3),
				new Fleet(0, 0, 0, 0, 0, 4));
		assertEquals(expected, notExpected.plus(expected).sliceByTurnsRemaining(2, 4));
	}
	
	@Test
	public void should_return_nothing_if_nothing_is_in_range() {
		Fleets notExpected = new Fleets(
				new Fleet(0, 0, 0, 0, 0, 1),
				new Fleet(0, 0, 0, 0, 0, 2),
				new Fleet(0, 0, 0, 0, 0, 3));
		Fleets expected = new Fleets();
		assertEquals(expected, notExpected.plus(expected).sliceByTurnsRemaining(4, 5));
	}
	
}
