# Goals

Analyze the business relationships between autonomous systems (ASes), and then classify these ASes into multiple categories.

## References

L. Gao. On inferring autonomous system relationships in the internet. IEEE/ACM Transactions on Networking, 9(6):733{745, 2001.

L. Subramanian, S. Agarwal, J. Rexford, and R. H. Katz. Characterizing the internet hierarchy from multiple vantage points. In Proceedings of the 21st Annual Joint Conference of the IEEE. Computer and Communications Societies, volume 2, pages 618{627. IEEE, 2002.

# Task 1

* Duplicate ASes and AS paths are removed with java.util.HashSet.
* See bgpvis.etl.BgpPreprocessor.java, bgpvis.AsPath

## Run Instructions

Run bgpvis.etl.BgpPreprocessor.java with the following VM arguments:
```
-Xms2048m
-Dbgp.in.file="path/to/file"
-Dbgp.out.file="path/to/file"
```

# Task 2

* Get the neighbours of each AS, then count the neighbours (node degree).
* Insert node degree and a set of ASes that share the same node degree into a java.util.TreeMap, so that the ASes can be sorted by the key (node degree). 
* Sort the TreeMap in descending node degree and get the top k ASes.
* See bgpvis.NodeDegreeRanker, bgpvis.AsPath, bgpvis.AsGraph

# Task 3

* Assume number of misconfigured BGP speakers L = 1 and degree size ratio R = 60. L and R are the same configuration as in Gao (2001). 
* Implementation based on Algorithms 1 and 2 given in the assignment paper.
* Guava’s Multiset is used to count the number of transit relationships.
* See bgpvis.AsGraph, bgpvis.AsGraphAnnotator

# Task 4

* Prune stubs, then prune regional ISPs from the relationship graph. Remaining nodes are cores.
* No further pruning is performed at this point. 
* Transit cores are those that peer with dense cores and other transit cores. So I call the method recursively to check whether the neighbour of a transit core is also a transit core.
* See bgpvis.AsGraph, bgpvis.AsClassifier
