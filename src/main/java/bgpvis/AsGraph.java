package bgpvis;

import static bgpvis.util.StringUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import com.google.common.collect.TreeMultimap;

public final class AsGraph {
	private static final Logger log = LoggerFactory.getLogger(AsGraph.class);
	private static final String AS_SEPARATOR = " ";
	private static final String SIBLING_TO_SIBLING = "s2s";
	private static final String PEER_TO_PEER = "p2p";
	private static final String CUSTOMER_TO_PROVIDER = "c2p";
	private static final String PROVIDER_TO_CUSTOMER = "p2c";

	private AsGraph() {
		// Private constructor, not meant to be instantiated
	}

	/**
	 * Get the adjacent neighbours of every AS
	 * <p>
	 * Key: a given AS
	 * <p>
	 * Value: collection of neighbours of this AS
	 * <p>
	 * Based on Phase 1 of Algorithm 1 (AS Graph Annotation)
	 * 
	 * @param asPaths
	 * @return
	 */
	public static Multimap<String, String> neighboursByAs(List<String> asPaths) {
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

	public static int indexOfTopProvider(String asPath,
			Map<String, Integer> nodeDegreeByAs) {
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
				throw new IllegalArgumentException(concat(
						"Missing node degree for AS [", as, "]"));
			}
			if (degree > max) {
				max = degree;
				j = i;
			}
		}
		return j;
	}

	/**
	 * Key: a given AS
	 * <p>
	 * Value: collection of transit providers that serve this AS
	 * <p>
	 * 
	 * @param asPath
	 * @param nodeDegreeByAs
	 * @return
	 */
	public static Multimap<String, String> transitProvidersByCustomer(
			String asPath, Map<String, Integer> nodeDegreeByAs) {
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
				providers.put(curr, next);
				continue;
			}
			providers.put(next, curr);
		}
		return providers;
	}

	public static Multimap<String, String> transitProvidersByCustomer(
			List<String> asPaths, Map<String, Integer> nodeDegreeByAs) {
		Multimap<String, String> providers = HashMultimap.create();
		for (String path : asPaths) {
			providers.putAll(transitProvidersByCustomer(path, nodeDegreeByAs));
		}
		return providers;
	}

	public static Multiset<String> transitCustomerToProvider(String asPath,
			Map<String, Integer> nodeDegreeByAs) {
		List<String> asList = AsPath.asList(asPath);
		String curr;
		String next;
		int size = asList.size();
		int j = indexOfTopProvider(asPath, nodeDegreeByAs);
		Multiset<String> result = HashMultiset.create(nodeDegreeByAs.size());
		for (int i = 0; i < size - 1; i++) {
			curr = asList.get(i);
			next = asList.get(i + 1);
			if (i < j) {
				result.add(toString(curr, next));
				continue;
			}
			result.add(toString(next, curr));
		}
		return result;
	}

	public static Multiset<String> transitCustomerToProvider(
			List<String> asPaths, Map<String, Integer> nodeDegreeByAs) {
		Multiset<String> result = HashMultiset.create(nodeDegreeByAs.size());
		for (String path : asPaths) {
			result.addAll(transitCustomerToProvider(path, nodeDegreeByAs));
		}
		return result;
	}

	public static List<String[]> annotateRelationship(
			String asPath, Multiset<String> transitCustomerToProvider,
			int threshold) {
		List<String> asList = AsPath.asList(asPath);
		int size = asList.size();
		List<String[]> result = new ArrayList<String[]>(size);
		String triplet[];
		String curr;
		String next;
		String pair1, pair2;
		int nextServedByCurr;
		int currServedByNext;
		for (int i = 0; i < size - 1; i++) {
			curr = asList.get(i);
			next = asList.get(i + 1);
			pair1 = toString(next, curr);
			nextServedByCurr = transitCustomerToProvider.count(pair1);
			pair2 = toString(curr, next);
			currServedByNext = transitCustomerToProvider.count(pair2);
			log.debug("({}): {}, ({}): {}", pair1, nextServedByCurr, pair2, currServedByNext);

			// If both ASes are greater than threshold L,
			// mark the edge as sibling

			if (nextServedByCurr > threshold && currServedByNext > threshold) {
				triplet = new String[]{curr, next, SIBLING_TO_SIBLING};
				result.add(triplet);
				triplet = new String[]{next, curr, SIBLING_TO_SIBLING};
				result.add(triplet);
				continue;
			}

			// If both ASes are less than threshold L and greater than zero,
			// mark the edge as sibling

			if (currServedByNext <= threshold && currServedByNext > 0
					&& nextServedByCurr <= threshold && nextServedByCurr > 0) {
				triplet = new String[]{curr, next, SIBLING_TO_SIBLING};
				result.add(triplet);
				triplet = new String[]{next, curr, SIBLING_TO_SIBLING};
				result.add(triplet);
				continue;
			}
			
			if (nextServedByCurr > threshold || currServedByNext == 0) {
				triplet = new String[]{curr, next, PROVIDER_TO_CUSTOMER};
				result.add(triplet);
				triplet = new String[]{next, curr, CUSTOMER_TO_PROVIDER};
				result.add(triplet);
				continue;
			}
			
			if (currServedByNext > threshold || nextServedByCurr == 0) {
				triplet = new String[]{next, curr, PROVIDER_TO_CUSTOMER};
				result.add(triplet);
				triplet = new String[]{curr, next, CUSTOMER_TO_PROVIDER};
				result.add(triplet);
				continue;
			}
		}
		return result;
	}
	
	public static Map<String, Map<String, String>> annotateRelationship(
			List<String> asPaths, Multiset<String> transitCustomerToProvider,
			int threshold) {
		int size = asPaths.size();
		Map<String, Map<String, String>> result = new HashMap<>(size);
		Map<String, String> row;
		List<String[]> relationships;
		for (String path : asPaths) {
			relationships = annotateRelationship(path, transitCustomerToProvider, threshold);
			for (String[] triplet : relationships) {
				row = result.get(triplet[0]);
				if (row == null) {
					row = new HashMap<>();
				}
				row.put(triplet[1], triplet[2]);
				result.put(triplet[0], row);
			}
		}
		return result;
	}

	public static String toString(String... as) {
		return join(Arrays.asList(as), AS_SEPARATOR);
	}

}
