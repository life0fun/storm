
# Storm

This repo contains proof of concept app of streaming and parsing
of log event in real-time using storm and trident topology.

We update and persist state from log event into redis store so that
event state can be queried in real time.

We show how to use storm trident with two implementations, one is using Java and the other is using Clojure. Note that there is no Trident clojure DSL as of today, our implementation using clojure showes how it can be done with clojure gen-class.

## trident 

Implementation of the project using Java. 
Please refer to the readme file under the project folder.


## trident-clj

Implementation of the project using clojure.
Please refer to the readme file under the project folder.
