(ns trident-clj.loc-aggregator
  (:import [java.io FileReader]
           [java.util Map Map$Entry List ArrayList Collection Iterator HashMap])
  (:import [storm.trident.operation TridentCollector Function]
           [backtype.storm.tuple Values])
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log])
  (:require [clj-redis.client :as redis])     ; bring in redis namespace
  (:require [trident-clj.redis.redis-datamapper :refer :all])
  (:require [trident-clj.redis.redis-persister :refer :all])
  (:gen-class
    :extends storm.trident.operation.BaseAggregator
    :name com.colorcloud.trident.LocAggregator  ; convert this ns to class Tweet
    ))  ; this ns impl Function


; create redis model tweet-rant to store aggregated for each tweet
(defn create-tweet-model []
  (def-redis-type tweet-rant
    (string-type :id :actor :location :text :time)
    (list-type :followers)
    (primary-key :id :actor)
    (format :json)
    (key-separator "##")))

; create a data object to persist data into redis
(defn store-tweet [id actor text location ts cnt]
  (let [tweet (tweet-rant :new)]
    (tweet :set! :id (str id))
    (tweet :set! :actor actor)
    (tweet :set! :location location)
    (tweet :set! :text text)
    (tweet :set! :time ts)
    (tweet :add! :followers (str actor "-follower-" 1))
    (tweet :add! :followers (str actor "follower-" 2))
    (tweet :save!)))

; defed redis type to find by primary key.
(defn find-tweet [id actor]
  (tweet-rant :find id actor))

(defn verify-tweet [id actor text loc ts]
  (let [db-tweet (find-tweet id actor)]
    ;(prn text " -- " (db-tweet :get :followers) (db-tweet :get-state))))
    (prn text " -- " (db-tweet :get-state))))


; prepare operation called once on start. init global state here.
(defn -prepare      ; gen-class method prefix by -
  " perpare : init global var and db conn "
  [this conf context]
  (prn "LocAggregator prepare once")
  ; init redis connection to db within redis data mapper
  (def loc-map (atom {}))
  (def batch-cnt (atom 0)))
  
(defn -cleanup
  [this]
  (prn "loc aggregation clean up"))

(defn -init
  [this batch-id collector]
  (prn "init aggregator called for batch " batch-id)
  ; reset batch counter
  (reset! batch-cnt 0)
  loc-map)  ; ret loc-state

; aggregate values in bucket after grouping. 
(defn -aggregate
  "given global state, aggregate current tuple into global state, ret void"
  [this loc-map tuple collector]
  (let [id (.getString tuple 0)
        actor (.getString tuple 1)
        loc (keyword (.getString tuple 3)) ; incoming tuple []
        rediskey (.getValueByField tuple "rediskey")
        cnt @batch-cnt
        sum (@loc-map loc)  ; ret nil when map does not have key
        incsum ((fnil inc 0) sum)]  ; in case first time, replace nil by 0
    ; now update tot
    (prn "aggregating for tuple " loc cnt sum incsum tuple)
    (swap! batch-cnt inc)   ; incr cnt within the batch
    (swap! loc-map (fn [m] (merge-with + m {loc 1})))
    (.emit collector (Values. (to-array [id actor incsum rediskey])))))

(defn -complete
  [this loc-map collector]
  (prn "aggregator batch completed "  @loc-map))
  ;(.emit collector (Values. (to-array @loc-map))))
