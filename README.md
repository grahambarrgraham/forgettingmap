
# Origami Code Challenge

### Build and run tests

```bash
./gradlew clean test
```

### Objective  

The objective of this task is design, implement and test a thread-safe 'forgetting map'.  

A 'forgetting' map should hold associations between a ‘key’ and some ‘content’. It should implement at least two methods:  

1.   add (add an association)  

2.    find (find content using the specified key).  

It should hold as many associations as it can, but no more than x associations at any time, with x being a parameter passed to the constructor. The association that is least used is removed from the map when a new entry requires a space and the map is at capacity, where “least used” relates to the number of times each association has been retrieved by the “find” method.  A suitable tie breaker should be used in the case where there are multiple least-used entries. 

Implementation  

·        Java would be preferable.  

·        You may use standard collections (HashMap, LinkedList, etc.) to build your solution but the use of a pre-existing library solution with the required behaviour, though an option in everyday work, is not what we’re looking for here. 

