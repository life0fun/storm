 /**
  * storm real-time threshold detection
  */
package com.colorcloud.threshold.logspout;

/**
 * ThresholdCalculatorBolt to get the threshold column val and check 
 * against the predefined value, incr the tot cnt when threshold met.
 */
public class ThresholdBolt extends BaseRichSpout {
  public void execute(Tuple tuple, BasicOutputCollector collector) {
	if(tuple!=null){
		// get all values in the tuple into a list
	  List<Object> inputTupleList = (List<Object>) tuple.getValues();
	  // which col contains the value to check
	  int thresholdColNum = thresholdInfo.getThresholdColNumber();
	  // the pre-configed threshod value
	  Object thresholdValue = thresholdInfo.getThresholdValue();
	  String thresholdDataType = 
	      tupleInfo.getFieldList().get(thresholdColNum-1).getColumnType();
	  Integer timeWindow = thresholdInfo.getTimeWindow();
	  int frequency = thresholdInfo.getFrequencyOfOccurence();

	  // type cast de-ref value before conduct checking
	  if(thresholdDataType.equalsIgnoreCase("string")){
	  	// from col idx, get the val to check from tuple
      String valueToCheck = inputTupleList.get(thresholdColNum-1).toString();
      String frequencyChkOp = thresholdInfo.getAction();
      // time series check,
      if(timeWindow!=null){
        long curTime = System.currentTimeMillis();
        long diffInMinutes = (curTime-startTime)/(1000);
        if(diffInMinutes>=timeWindow){
        	// what we check, check equal or gt, or...
          if(frequencyChkOp.equals("==")){
						if(valueToCheck.equalsIgnoreCase(thresholdValue.toString())){
						  count.incrementAndGet();  // tot cnts of threshold met
						  if(count.get() > frequency)
						    splitAndEmit(inputTupleList,collector);
						}
          }else if(frequencyChkOp.equals("!=")){
	          if(!valueToCheck.equalsIgnoreCase(thresholdValue.toString())){
              count.incrementAndGet();
              if(count.get() > frequency)
                splitAndEmit(inputTupleList,collector);
	          }
          }else
            System.out.println("Operator not supported");
        }
      }else{
				if(frequencyChkOp.equals("==")){
					if(valueToCheck.equalsIgnoreCase(thresholdValue.toString())){
					  count.incrementAndGet();  
					  if(count.get() > frequency)
					       splitAndEmit(inputTupleList,collector);    
					}
				}
				else if(frequencyChkOp.equals("!="))
				{
				    if(!valueToCheck.equalsIgnoreCase(thresholdValue.toString()))
				    {
				        count.incrementAndGet();
				        if(count.get() > frequency)
				            splitAndEmit(inputTupleList,collector);   
				    }
				}
        }
     }
     else if(thresholdDataType.equalsIgnoreCase("int") || 
             thresholdDataType.equalsIgnoreCase("double") || 
             thresholdDataType.equalsIgnoreCase("float") || 
             thresholdDataType.equalsIgnoreCase("long") || 
             thresholdDataType.equalsIgnoreCase("short"))
     {
         String frequencyChkOp = thresholdInfo.getAction();
         if(timeWindow!=null)
         {
              long valueToCheck = 
                  Long.parseLong(inputTupleList.get(thresholdColNum-1).toString());
              long curTime = System.currentTimeMillis();
              long diffInMinutes = (curTime-startTime)/(1000);
              System.out.println("Difference in minutes="+diffInMinutes);
              if(diffInMinutes>=timeWindow)
              {
                   if(frequencyChkOp.equals("<"))
                   {
                       if(valueToCheck < Double.parseDouble(thresholdValue.toString()))
                       {
                            count.incrementAndGet();
                            if(count.get() > frequency)
                                splitAndEmit(inputTupleList,collector);
                       }
                   }
                   else if(frequencyChkOp.equals(">"))
                   {
                        if(valueToCheck > Double.parseDouble(thresholdValue.toString())) 
                        {
                            count.incrementAndGet();
                            if(count.get() > frequency)
                                splitAndEmit(inputTupleList,collector);
                        }
                    }
                    else if(frequencyChkOp.equals("=="))
                    {
                       if(valueToCheck == Double.parseDouble(thresholdValue.toString()))
                       {
                           count.incrementAndGet();
                           if(count.get() > frequency)
                               splitAndEmit(inputTupleList,collector);
                        }
                    }
                    else if(frequencyChkOp.equals("!="))
                    {
                    }
                }
             
       }
	else
	    splitAndEmit(null,collector);
	}
	else
	{
	    System.err.println("Emitting null in bolt");
	    splitAndEmit(null,collector);
 }
}