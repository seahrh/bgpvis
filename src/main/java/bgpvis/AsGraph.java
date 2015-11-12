package bgpvis;

import static bgpvis.util.StringUtil.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
	 * Adapetd from Phase 1 of Algorithm 1 (AS Graph Annotation)
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

	public static Map<String, Integer> nodeDegreeByAs(
			Multimap<String, String> neighbours) {
		Map<String, Collection<String>> neighboursMap = neighbours.asMap();
		Map<String, Integer> result = new HashMap<String, Integer>(
				neighbours.size());
		String as;
		Collection<String> asNeighbours;
		Integer nNeighbours;
		for (Map.Entry<String, Collection<String>> entry : neighboursMap.entrySet()) {
			as = entry.getKey();
			asNeighbours = entry.getValue();
			nNeighbours = asNeighbours.size();
			result.put(as, nNeighbours);
		}
		return result;
	}

	/**
	 * Index AS by node degree (number of adjacent neighbours)
	 * 
	 * @param neighbours
	 * @return
	 */
	public static TreeMultimap<Integer, String> asByNodeDegree(
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
	 * 
	 * @param asByNodeDegree
	 * @param k
	 * @return
	 */
	public static List<String> top(
			TreeMultimap<Integer, String> asByNodeDegree, int k) {
		List<String> result = new ArrayList<String>(k);
		NavigableMap<Integer, Collection<String>> asByNodeDegreeMap = asByNodeDegree.asMap()
			.descendingMap();
		Integer degree;
		Collection<String> ases;
		boolean enough = false;
		for (Map.Entry<Integer, Collection<String>> entry : asByNodeDegreeMap.entrySet()) {
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
	
	public static int indexOfTopProvider(String asPath, Map<String, Integer> nodeDegreeByAs) {
		int j = -1;
		List<String> asList = AsPath.asList(asPath);
		int size = asList.size();
		int max = 0;
		Integer degree;
		String as;
		for (int i = 0; i < size; i++) {
			as = asList.get(i);
			degree = nodeDegreeByAs.get(as);
			if (degree == null) {
				throw new IllegalArgumentException(concat("Missing node degree for AS [", as, "]"));
			}
			if (degree > max) {
				max = degree;
				j = i;
			}
		}
		return j;
	}
	
	/**
	 * Adapetd from Phase 2 of Algorithm 1 (AS Graph Annotation)
	 * @param asPath
	 * @param nodeDegreeByAs
	 * @return
	 */
	public static Multimap<String, String> transitProviders(String asPath, Map<String, Integer> nodeDegreeByAs) {
		Multimap<String, String> providers = HashMultimap.create();
		List<String> asList = AsPath.asList(asPath);
		String curr;
		String next;
		int size = asList.size();
		int j = indexOfTopProvider(asPath, nodeDegreeByAs);
		for (int i = 0; i < size - 1; i++) {
			curr = asList.get(i);
			next = asList.get(i + 1);
			if (i < j) {
				providers.put(next, curr);
				continue;
			}
			providers.put(curr, next);
		}
		return providers;
	}
	
	public static Multimap<String, String> transitProviders(List<String> asPaths, Map<String, Integer> nodeDegreeByAs) {
		Multimap<String, String> providers = HashMultimap.create();
		for (String path : asPaths) {
			providers.putAll(transitProviders(path, nodeDegreeByAs));
		}
		return providers;
	}
	
	public static void annotateRelationship() {
		
	}

}
