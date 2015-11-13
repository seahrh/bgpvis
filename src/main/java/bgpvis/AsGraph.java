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

	/**
	 * Map AS to its node degree.
	 * 
	 * @param neighbours
	 * @return
	 */
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
	 * Map node degree (number of adjacent neighbours) to ASes that have the
	 * same node degree. Data structure allows for easy sorting of ASes by node
	 * degree.
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

	/**
	 * Index of top provider in an AS path.
	 * 
	 * @param asPath
	 * @param nodeDegreeByAs
	 * @return
	 */
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
	 * Map of customers to its transit providers.
	 * <p>
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

	/**
	 * Map of customers to its transit providers.
	 * <p>
	 * Key: a given AS
	 * <p>
	 * Value: collection of transit providers that serve this AS
	 * <p>
	 * 
	 * @param asPaths
	 * @param nodeDegreeByAs
	 * @return
	 */
	public static Multimap<String, String> transitProvidersByCustomer(
			List<String> asPaths, Map<String, Integer> nodeDegreeByAs) {
		Multimap<String, String> providers = HashMultimap.create();
		for (String path : asPaths) {
			providers.putAll(transitProvidersByCustomer(path, nodeDegreeByAs));
		}
		return providers;
	}

	/**
	 * Count customer-to-provider transit relationships
	 * <p>
	 * Key: a given customer-to-provider transit relationship
	 * <p>
	 * Value: Number of occurrences of this transit relationship
	 * <p>
	 * 
	 * @param asPath
	 * @param nodeDegreeByAs
	 * @return
	 */
	public static Multiset<String> countTransitRelationship(String asPath,
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

	/**
	 * Count customer-to-provider transit relationships
	 * <p>
	 * Key: a given customer-to-provider transit relationship
	 * <p>
	 * Value: Number of occurrences of this transit relationship
	 * <p>
	 * 
	 * @param asPaths
	 * @param nodeDegreeByAs
	 * @return
	 */
	public static Multiset<String> countTransitRelationship(
			List<String> asPaths, Map<String, Integer> nodeDegreeByAs) {
		Multiset<String> result = HashMultiset.create(nodeDegreeByAs.size());
		for (String path : asPaths) {
			result.addAll(countTransitRelationship(path, nodeDegreeByAs));
		}
		return result;
	}

	/**
	 * Assign sibling-to-sibling, customer-to-provider, or provider-to-customer
	 * relationships. Based on Task 3 Algorithm 1 Phase 3.
	 * 
	 * @param asPath
	 * @param transitCustomerToProvider
	 * @param threshold
	 * @return
	 */
	public static List<String[]> relationships(String asPath,
			Multiset<String> transitCustomerToProvider, int threshold) {
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
			log.debug("({}): {}, ({}): {}", pair1, nextServedByCurr, pair2,
					currServedByNext);

			// If both ASes are greater than threshold L,
			// mark the edge as sibling

			if (nextServedByCurr > threshold && currServedByNext > threshold) {
				triplet = new String[] { curr, next, SIBLING_TO_SIBLING };
				result.add(triplet);
				triplet = new String[] { next, curr, SIBLING_TO_SIBLING };
				result.add(triplet);
				continue;
			}

			// If both ASes are less than threshold L and greater than zero,
			// mark the edge as sibling

			if (currServedByNext <= threshold && currServedByNext > 0
					&& nextServedByCurr <= threshold && nextServedByCurr > 0) {
				triplet = new String[] { curr, next, SIBLING_TO_SIBLING };
				result.add(triplet);
				triplet = new String[] { next, curr, SIBLING_TO_SIBLING };
				result.add(triplet);
				continue;
			}

			if (nextServedByCurr > threshold || currServedByNext == 0) {
				triplet = new String[] { curr, next, PROVIDER_TO_CUSTOMER };
				result.add(triplet);
				triplet = new String[] { next, curr, CUSTOMER_TO_PROVIDER };
				result.add(triplet);
				continue;
			}

			if (currServedByNext > threshold || nextServedByCurr == 0) {
				triplet = new String[] { next, curr, PROVIDER_TO_CUSTOMER };
				result.add(triplet);
				triplet = new String[] { curr, next, CUSTOMER_TO_PROVIDER };
				result.add(triplet);
				continue;
			}
		}
		return result;
	}

	/**
	 * Assign sibling-to-sibling, customer-to-provider, or provider-to-customer
	 * relationships. Based on Task 3 Algorithm 1 Phase 3.
	 * 
	 * @param asPaths
	 * @param transitCustomerToProvider
	 * @param threshold
	 * @return
	 */
	public static Map<String, Map<String, String>> relationships(
			List<String> asPaths, Multiset<String> transitCustomerToProvider,
			int threshold) {
		int size = asPaths.size();
		Map<String, Map<String, String>> result = new HashMap<>(size);
		Map<String, String> row;
		List<String[]> relationships;
		for (String path : asPaths) {
			relationships = relationships(path, transitCustomerToProvider,
					threshold);
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

	/**
	 * Mark AS pair edges as non-peering. Based on Task 3 Algorithm 2 Phase 2.
	 * 
	 * @param asPath
	 * @param nodeDegreeByAs
	 * @param relationships
	 * @return
	 */
	public static Multimap<String, String> nonPeers(String asPath,
			Map<String, Integer> nodeDegreeByAs,
			Map<String, Map<String, String>> relationships) {
		Multimap<String, String> ret = HashMultimap.create();
		List<String> asList = AsPath.asList(asPath);
		String curr;
		String next;
		int size = asList.size();
		int j = indexOfTopProvider(asPath, nodeDegreeByAs);
		for (int i = 0; i < size - 1; i++) {
			curr = asList.get(i);
			next = asList.get(i + 1);
			if (i < j - 1) {
				ret.put(curr, next);
				continue;
			}
			if (i > j) {
				ret.put(curr, next);
				continue;
			}
		}
		if (j - 1 >= 0 && j + 1 < size) {
			boolean hasLeftSibling = false;
			boolean hasRightSibling = false;
			String topProvider = asList.get(j);
			String prev = asList.get(j - 1);
			next = asList.get(j + 1);
			Map<String, String> temp;
			temp = relationships.get(prev);
			if (temp == null) {
				throw new IllegalArgumentException(concat(
						"Missing relationship for AS: ", prev));
			}
			if (temp.get(topProvider)
				.equals(SIBLING_TO_SIBLING)) {
				hasLeftSibling = false;
			}
			temp = relationships.get(topProvider);
			if (temp == null) {
				throw new IllegalArgumentException(concat(
						"Missing relationship for AS: ", topProvider));
			}
			if (temp.get(next)
				.equals(SIBLING_TO_SIBLING)) {
				hasRightSibling = true;
			}
			if (!hasLeftSibling && !hasRightSibling) {
				Integer prevDegree = nodeDegreeByAs.get(prev);
				if (prevDegree == null) {
					throw new IllegalArgumentException(concat(
							"Missing node degree for AS before top provider: ",
							prev));
				}
				Integer nextDegree = nodeDegreeByAs.get(next);
				if (nextDegree == null) {
					throw new IllegalArgumentException(concat(
							"Missing node degree for AS after top provider: ",
							next));
				}
				if (prevDegree > nextDegree) {
					ret.put(topProvider, next);
				} else {
					ret.put(prev, topProvider);
				}
			}
		}
		return ret;
	}

	/**
	 * Mark AS pair edges as non-peering. Based on Task 3 Algorithm 2 Phase 2.
	 * 
	 * @param asPaths
	 * @param nodeDegreeByAs
	 * @param relationships
	 * @return
	 */
	public static Multimap<String, String> nonPeers(List<String> asPaths,
			Map<String, Integer> nodeDegreeByAs,
			Map<String, Map<String, String>> relationships) {
		Multimap<String, String> ret = HashMultimap.create();
		for (String path : asPaths) {
			ret.putAll(nonPeers(path, nodeDegreeByAs, relationships));
		}
		return ret;
	}

	/**
	 * Assign peer-to-peer relationships to AS pair edges. Based on Task 3
	 * Algorithm 2 Phase 3.
	 * 
	 * @param asPath
	 * @param nodeDegreeByAs
	 * @param relationships
	 * @param nonPeers
	 * @param degreeSizeRatio
	 */
	public static void peeringRelationships(String asPath,
			Map<String, Integer> nodeDegreeByAs,
			Map<String, Map<String, String>> relationships,
			Multimap<String, String> nonPeers, double degreeSizeRatio) {
		List<String> asList = AsPath.asList(asPath);
		int size = asList.size();
		String curr;
		String next;
		Integer currDegree;
		Integer nextDegree;
		double ratio;
		Map<String, String> temp;
		for (int i = 0; i < size - 1; i++) {
			curr = asList.get(i);
			next = asList.get(i + 1);
			currDegree = nodeDegreeByAs.get(curr);
			if (currDegree == null) {
				throw new IllegalArgumentException(concat(
						"Missing node degree for current AS: ", curr));
			}
			nextDegree = nodeDegreeByAs.get(next);
			if (nextDegree == null) {
				throw new IllegalArgumentException(concat(
						"Missing node degree for next AS: ", next));
			}
			ratio = currDegree / nextDegree;

			// Both curr and next ASes are non-peering in both directions,
			// and degree size ratio does not exceed threshold

			if (!nonPeers.containsEntry(curr, next)
					&& !nonPeers.containsEntry(next, curr)
					&& ratio < degreeSizeRatio && ratio > (1 / degreeSizeRatio)) {
				temp = relationships.get(curr);
				if (temp == null) {
					temp = new HashMap<>();
				}
				temp.put(next, PEER_TO_PEER);
				relationships.put(curr, temp);
			}
		}
	}

	/**
	 * Assign peer-to-peer relationships to AS pair edges. Based on Task 3
	 * Algorithm 2 Phase 3.
	 * 
	 * @param asPaths
	 * @param nodeDegreeByAs
	 * @param relationships
	 * @param nonPeers
	 * @param degreeSizeRatio
	 */
	public static void peeringRelationships(List<String> asPaths,
			Map<String, Integer> nodeDegreeByAs,
			Map<String, Map<String, String>> relationships,
			Multimap<String, String> nonPeers, double degreeSizeRatio) {
		for (String path : asPaths) {
			peeringRelationships(path, nodeDegreeByAs, relationships, nonPeers,
					degreeSizeRatio);
		}
	}

	/**
	 * Returns a string representation of a list of ASes. Usually used to name a
	 * relationship edge (AS pair).
	 * 
	 * @param as
	 * @return
	 */
	private static String toString(String... as) {
		return join(Arrays.asList(as), AS_SEPARATOR);
	}

}
