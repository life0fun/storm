 /**
  * storm real-time threshold detection
  */
package com.colorcloud.threshold;

/**
 * DBWriterBolt to persistent state into db.
 * create a db connection on every group substream ?
 */
public class DBWriteBolt extends BaseRichSpout {

  public void prepare(Map StormConf, TopologyContext context ){		
    try {
      Class.forName(dbClass);
    } catch (ClassNotFoundException e) {
      System.out.println("Driver not found");
      e.printStackTrace();
    }

    try {
			connection driverManager.getConnection( 
			   "jdbc:mysql://"+databaseIP+":"+databasePort+"/"+databaseName, userName, pwd);
			connection.prepareStatement("DROP TABLE IF EXISTS "+tableName).execute();

			StringBuilder createQuery = new StringBuilder(
			   "CREATE TABLE IF NOT EXISTS "+tableName+"(");

			for(Field fields : tupleInfo.getFieldList()){
				if(fields.getColumnType().equalsIgnoreCase("String"))
					createQuery.append(fields.getColumnName()+" VARCHAR(500),");
				else
			    createQuery.append(fields.getColumnName()+" "+fields.getColumnType()+",");
			}
			createQuery.append("thresholdTimeStamp timestamp)");
			connection.prepareStatement(createQuery.toString()).execute();

			// Insert Query
			StringBuilder insertQuery = new StringBuilder("INSERT INTO "+tableName+"(");
			String tempCreateQuery = new String();
			for(Field fields : tupleInfo.getFieldList()){
			    insertQuery.append(fields.getColumnName()+",");
			}
			insertQuery.append("thresholdTimeStamp").append(") values (");
			for(Field fields : tupleInfo.getFieldList())
			{
			   insertQuery.append("?,");
			}

			insertQuery.append("?)");
			prepStatement = connection.prepareStatement(insertQuery.toString());
			}
			catch (SQLException e) 
			{		
			e.printStackTrace();
			}		
  }

  public void execute(Tuple tuple, BasicOutputCollector collector){
  	batchExecuted=false;
  	if(tuple!=null){
  		List<Object> inputTupleList = (List<Object>) tuple.getValues();
  		int dbIndex=0;
  		for(int i=0;i<tupleInfo.getFieldList().size();i++){
  		  Field field = tupleInfo.getFieldList().get(i);
  		  try {
  		    dbIndex = i+1;
  		    if(field.getColumnType().equalsIgnoreCase("String"))				
  		   		prepStatement.setString(dbIndex, inputTupleList.get(i).toString());
  		    else if(field.getColumnType().equalsIgnoreCase("int"))
  		      prepStatement.setInt(dbIndex,
  		           Integer.parseInt(inputTupleList.get(i).toString()));
  		    else if(field.getColumnType().equalsIgnoreCase("long"))
  		       prepStatement.setLong(dbIndex, 
  		           Long.parseLong(inputTupleList.get(i).toString()));
  		    else if(field.getColumnType().equalsIgnoreCase("float"))
  		       prepStatement.setFloat(dbIndex, 
  		           Float.parseFloat(inputTupleList.get(i).toString()));
  		    else if(field.getColumnType().equalsIgnoreCase("double"))
  		       prepStatement.setDouble(dbIndex, 
  		           Double.parseDouble(inputTupleList.get(i).toString()));
  		    else if(field.getColumnType().equalsIgnoreCase("short"))
  		       prepStatement.setShort(dbIndex, 
  		           Short.parseShort(inputTupleList.get(i).toString()));
  		    else if(field.getColumnType().equalsIgnoreCase("boolean"))
  		       prepStatement.setBoolean(dbIndex, 
  		           Boolean.parseBoolean(inputTupleList.get(i).toString()));
  		    else if(field.getColumnType().equalsIgnoreCase("byte"))
  		       prepStatement.setByte(dbIndex, 
  		           Byte.parseByte(inputTupleList.get(i).toString()));
  		    else if(field.getColumnType().equalsIgnoreCase("Date")){
  		      Date dateToAdd=null;
  		      if (!(inputTupleList.get(i) instanceof Date)){  
  					DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
  		      try {
  						dateToAdd = df.parse(inputTupleList.get(i).toString());
  					}catch (ParseException e){
  						System.err.println("Data type not valid");
  					}
  				} else {
  					dateToAdd = (Date)inputTupleList.get(i);
  					java.sql.Date sqlDate = new java.sql.Date(dateToAdd.getTime());
  					prepStatement.setDate(dbIndex, sqlDate);
  				}	
  			} catch (SQLException e) {
  				e.printStackTrace();
  			}
  		}

  		Date now = new Date();			
  		try{
  	  	prepStatement.setTimestamp(dbIndex+1, new java.sql.Timestamp(now.getTime()));
  	  	prepStatement.addBatch();  
  	  	counter.incrementAndGet();
  	  	if (counter.get()== batchSize) 
  				executeBatch();  <-- execute db operations after all tuples in batch done.
  		} catch (SQLException e1) {
  	  	e1.printStackTrace();
  		}			
  	}
  	else
  	{
  		long curTime = System.currentTimeMillis();
  		long diffInSeconds = (curTime-startTime)/(60*1000);
  		if(counter.get()<batchSize && diffInSeconds>batchTimeWindowInSeconds){
  	    try {
  	      executeBatch();
  	      startTime = System.currentTimeMillis();
  	    }catch (SQLException e) {
  				e.printStackTrace();
  	    }
  		}
  	}
  }

  public void executeBatch() throws SQLException {
      batchExecuted=true;
      prepStatement.executeBatch();
      counter = new AtomicInteger(0);
  }
}