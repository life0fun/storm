package com.colorcloud.trident;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import storm.trident.TridentTopology;
import storm.trident.operation.BaseAggregator;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.generated.StormTopology;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

/**
 * This example shows the usage of aggregate() method for aggregating the WHOLE batch of Tuples.
 * <p>
 * Because we aggregate the whole batch, we produce a hashmap with the counts per each location.
 * Note how we only use the collector at the end of the Aggregator so we don't emit new Tuples for each Tuple that we process: 
 * we only emit one Tuple per batch.
 * For updating databases that's the best approach: you don't usually want to overload your DB
 * with one update per each Tuple. 
 */
public class BatchAggregate {
	
	@SuppressWarnings({ "serial" })
	public static class LocationAggregator extends BaseAggregator<Map<String, Integer>> {

		@Override
		public Map<String, Integer> init(Object batchId, TridentCollector collector) {
			return new HashMap<String, Integer>();
		}

		@Override
		public void aggregate(Map<String, Integer> val, TridentTuple tuple, TridentCollector collector) {
			String location = tuple.getString(0);
			val.put(location, MapUtils.getInteger(val, location, 0) + 1);
		}

		@Override
		public void complete(Map<String, Integer> val, TridentCollector collector) {
			collector.emit(new Values(val));
		}
	}
	
	public static StormTopology buildTopology(LocalDRPC drpc) throws IOException {
		FakeTweetsBatchSpout spout = new FakeTweetsBatchSpout(100);

		TridentTopology topology = new TridentTopology();
		topology.newStream("spout", spout)
			.aggregate(new Fields("location"), new LocationAggregator(), new Fields("location_counts"))
			.each(new Fields("location_counts"), new Utils.PrintFilter());
		
		return topology.build();
	}

	public static void main(String[] args) throws Exception {
		Config conf = new Config();

		LocalDRPC drpc = new LocalDRPC();
		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("location_batchaggregate", conf, buildTopology(drpc));
	}
}
