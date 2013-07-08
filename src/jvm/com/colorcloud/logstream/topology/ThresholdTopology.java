 /**
  * storm real-time threshold detection
  */
 
public class ThresholdTopology{
  public static void main(String[] args) throws AlreadyAliveException, 
                                               InvalidTopologyException, 
                                               InterruptedException 
  {
    // log file spout read data from log file and stream into topology
    ParallelFileSpout parallelFileSpout = new ParallelFileSpout();
    ThresholdBolt thresholdBolt = new ThresholdBolt();
    DBWriterBolt dbWriterBolt = new DBWriterBolt();

    TopologyBuilder builder = new TopologyBuilder();
    builder.setSpout("spout", parallelFileSpout, 1);
    builder.setBolt("thresholdBolt", thresholdBolt,1).shuffleGrouping("spout");
    builder.setBolt("dbWriterBolt",dbWriterBolt,1).shuffleGrouping("thresholdBolt");

    if(this.argsMain!=null && this.argsMain.length > 0){
      conf.setNumWorkers(1);
      StormSubmitter.submitTopology(this.argsMain[0], conf, builder.createTopology());
    }else{    
      Config conf = new Config();
      conf.setDebug(true);
      conf.setMaxTaskParallelism(3);
      LocalCluster cluster = new LocalCluster();
      cluster.submitTopology("Threshold_Test", conf, builder.createTopology());
    }
  }
}
