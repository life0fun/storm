 /**
  * storm real-time threshold detection
  */
package com.colorcloud.threshold.logspout;

/**
 * spout to emit stream of tuples by reading lines from log file.
 */
public class LogSpout extends BaseRichSpout {
  private static final long serialVersionUID = -2871793574597747583L;
  SpoutOutputCollector _collector;
  LinkedBlockingQueue<Object> queue = null;
  String logfile = null;
  BufferedReader fileReader = null;

  public LogSpout(String filename){
    logfile = filename;
  }

  @Override
  public void open(Map conf, TopologyContext context,SpoutOutputCollector collector){
    _collector = collector;
    try{
    	fileReader  =  new BufferedReader(new FileReader(new File(file)));
    }catch (FileNotFoundException e){
    	System.exit(1);
    }
  }
                                                                                                  
  public void nextTuple(){
    protected void ListenFile(File file){
    	Utils.sleep(2000);
    	RandomAccessFile access = null; 
    	String line = null;                  
      try{ 
  			while ((line = access.readLine()) != null){ 
  				if (line !=null){
  					String[] fields=null;
  					if (tupleInfo.getDelimiter().equals("|"))
  						fields = line.split("\\"+tupleInfo.getDelimiter());
  					else
  						fields = line.split(tupleInfo.getDelimiter());         
  					
            if (tupleInfo.getFieldList().size() == fields.length)
  						_collector.emit(new Values(fields)); 
  				}       
        } 
      }catch (IOException ex) {
      } 
    }
  }
 
  public void declareOutputFields(OutputFieldsDeclarer declarer){
  	String[] fieldsArr = new String [tupleInfo.getFieldList().size()];
  	for(int i=0; i<tupleInfo.getFieldList().size(); i++){
  	  fieldsArr[i] = tupleInfo.getFieldList().get(i).getColumnName();
  	}          
  	declarer.declare(new Fields(fieldsArr));
  }   
}