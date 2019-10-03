import org.junit.Test;

import static org.junit.Assert.*;

public class Parser311Test {

    @Test
    public void generate311HashTable() {
    }

    @Test
    public void isValid311JsonBadPathIsFalse() {
        Parser311 parser311 = new Parser311("/bad/path");

        assertFalse(parser311.isValid311DgraphJson());
    }

    @Test
    public void isValid311JsonGoodFile() {
        Parser311 parser311 = new Parser311("test_data/valid_dgraph.json");

        assertTrue(parser311.isValid311DgraphJson());
    }

    @Test
    public void isValid311JsonHashMap() {
        Parser311 parser311 = new Parser311("test_data/valid_dgraph.json");

        try {
            parser311.generate311HashTable();
        }
        catch (Exception e)
        {
            fail();
        }

        assertTrue(true);
    }

    @Test
    public void invalidDgraphfile() {
        Parser311 parser311 = new Parser311("test_data/invalid_dgraph.json");

        assertFalse(parser311.isValid311DgraphJson());
    }

    @Test
    public void invalidDgraphHashMap() {
        Parser311 parser311 = new Parser311("test_data/invalid_dgraph.json");

        try {
            parser311.generate311HashTable();
            fail();
        }
        catch (Exception e) {
            assertTrue(true);
        }
    }
}