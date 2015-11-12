package bgpvis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

public final class AsGraph {
	private static final Logger log = LoggerFactory.getLogger(AsGraph.class);

	private AsGraph() {
		// Private constructor, not meant to be instantiated
	}

	/**
	 * Get neighbours of every AS
	 * @param asPaths
	 * @return 
	 */
	public static Multimap<String, String> neighbours(List<String> asPaths) {
		Multimap<String, String> neighbours = HashMultimap.create();
		List<String> asList;
		String curr;
		String next;
		int size;
		for (String path : asPaths) {
			asList = AsPath.asList(path);
			size = asList.size();
			for (int i = 0; i < size - 1; i++) {
				curr = asList.get(i);
				next = asList.get(i + 1);
				neighbours.put(curr, next);
				neighbours.put(next, curr);
			}
		}
		return neighbours;
	}

	/**
	 * Index AS by node degree (number of adjacent neighbours)
	 * @param neighbours
	 * @return
	 */
	public static TreeMultimap<Integer, String> nodeDegree(
			Multimap<String, String> neighbours) {
		Map<String, Collection<String>> neighboursMap = neighbours.asMap();
		TreeMultimap<Integer, String> result = TreeMultimap.create();
		String as;
		Collection<String> asNeighbours;
		Integer nNeighbours;
		for (Map.Entry<String, Collection<String>> entry : neighboursMap.entrySet()) {
		    as = entry.getKey();
		    asNeighbours = entry.getValue();
		    nNeighbours = asNeighbours.size();
		    result.put(nNeighbours, as);
		}
		return result;
	}
	
	/**
	 * Get top k ASes by largest node degree
	 * @param asByNodeDegree
	 * @param k
	 * @return
	 */
	public static List<String> top(TreeMultimap<Integer, String> asByNodeDegree, int k) {
		List<String> result = new ArrayList<String>(k);
		NavigableMap<Integer,Collection<String>> asByNodeDegreeMap = asByNodeDegree.asMap().descendingMap();
		Integer degree;
		Collection<String> ases;
		boolean enough = false;
		for (Map.Entry<Integer,Collection<String>> entry : asByNodeDegreeMap.entrySet()) {
			degree = entry.getKey();
		    ases = entry.getValue();
		    for (String as : ases) {
		    	result.add(as);
		    	if (result.size() == k) {
		    		enough = true;
		    		break;
		    	}
		    }
		    if (enough) {
		    	break;
		    }
		}
		return result;
	}

}
