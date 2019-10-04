import org.junit.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Test
    public void testPruneHashTable() {
        Parser311 parser311 = new Parser311("test_data/valid_dgraph.json");
        Map<String, Map<String, Integer>> hm = null;
        Map<String, Map<String, Integer>> pruned;
        try {
            hm = parser311.generate311HashTable();
        }
        catch (Exception e)
        {
            fail();
        }

        //verify that original contains unique nodes
        Set originalSet = hm.entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream())
                .filter(f -> f.getValue() == 1).collect(Collectors.toSet());
        assertTrue(originalSet.size() > 0);

        pruned = parser311.pruneUniqueNodesFrom311HashMap(hm);

        //verify that pruned set has no unique nodes
        Set prunedSet = pruned.entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream())
                .filter(f -> f.getValue() == 1).collect(Collectors.toSet());
        assertEquals(0, prunedSet.size());


    }
}