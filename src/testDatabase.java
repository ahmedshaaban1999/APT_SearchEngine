import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class testDatabase {
	Database db;

	public testDatabase() {
		// TODO Auto-generated constructor stub
		db = new Database();
	}

	@Test
	public void testUpdateDocument() {
		fail("Not yet implemented");

	}

	@Test
	public void testDeleteDocument() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindDocument() {
		try {
			// ArrayList<String> expected = new ArrayList<>();
			// assertArrayEquals(expected, actuals);
			// fail("Not yet implemented");
			HashMap<String,Integer> expected = new HashMap<String,Integer>();
			expected = db.findDocument("word", "type");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testSaveLinks() {
		fail("Not yet implemented");
	}

	@Test
	public void testSaveLexion() {
		fail("Not yet implemented");
	}

}
