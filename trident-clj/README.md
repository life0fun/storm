# Storm Trident example written in clojure.

This is a simple example of using Storm Trident for data processing written in clojure

## Setup

In lein-2, all dependent libs are in maven repos at $HOME/.m2/repository/package
If you have local jar, use mvn deploy rather than mvn install.

  lein-2 new trident-clj
  lein-2 deps
  lein-2 with-profile dev compile
  lein-2 with-profile dev run


## dependency
  1. storm deps on exact clojure 1.4.0 ver.
  

# Notes about Lein uberjar
  1. use lein jar to generate jar of only your code (not include dep jars)
  
  2. use lein uberjar to gen standalone jar that include all dep jars(including clojure jar).

  3. lein-2 with-profile dev uberjar generate jar 
    java -jar target/dev+uberjar/trident-clj-1.0.0-SNAPSHOT-standalone.jar
  3. lein-2 uberjar generate jar 
    java -jar target/trident-clj-1.0.0-SNAPSHOT-standalone.jar

  4. Lein will remove non project class during package. This leads to class not found error when running standalone jar. Turn on the flag in project.clj. 
    ; to enure uberjar works by not deleting non project classes
    :keep-non-project-classes true


## gen-class notes

  (:gen-class
    :name com.colorcloud.trident.TweetSpout  ; convert this ns to class Tweet
    :preﬁx -     ; class methods prefix, class method get this as first arg.
    :state state ; a single map stores object fields
    :init init   ; ret [[args to superclass constructor] state-map ]

    :constructors {[] []   ; empty arg constructor
                   [String int] []}  ; a map of constructor sig to superclass construcotr signature
    :extends storm.trident.operation.BaseAggregator
    :implements [storm.trident.spout.IBatchSpout]))  ; this ns impl Function

  ## state and init
    :state refs to the object's state. add your object fields into state. e.g., put a atom {} to store your serializables. state object created in init fn.
    
    :init defines a fn that rets a vector with the first ele of vector is a vector of args to superclass constructor. ret the second arg that is object state created inside init fn. 
      e.g [ [superclass-constructor-args] (create state map)] 

    :constructors maps the args of class' constructor to the args of super class's constructor. This is to determine which constructor to call.
    Each entry in constructors map need an impl in init fn.

## redis data mapper

Start redis server, we will store intermediate state into redis server.

We use redis data mapper to store object model in redis.


## Trident Notes.

1. Each batch is a reliable transaction. 
2. GroupBy and aggregation functions are invoked per batch, init and complete upon batch start, and aggregate on each trident tuple in the batch.
