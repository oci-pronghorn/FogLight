package com.ociweb.matrix;


import java.util.concurrent.TimeUnit;

import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.stage.math.BuildMatrixCompute;
import com.ociweb.pronghorn.stage.math.BuildMatrixCompute.MatrixTypes;
import com.ociweb.pronghorn.stage.math.ColumnSchema;
import com.ociweb.pronghorn.stage.math.ColumnsToRowsStage;
import com.ociweb.pronghorn.stage.math.DecimalSchema;
import com.ociweb.pronghorn.stage.math.MatrixSchema;
import com.ociweb.pronghorn.stage.math.RowSchema;
import com.ociweb.pronghorn.stage.monitor.MonitorConsoleStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;
import com.ociweb.pronghorn.stage.test.ConsoleSummaryStage;
import com.ociweb.pronghorn.stage.test.PipeCleanerStage;

public class IoTApp implements IoTSetup
{
    public static void main( String[] args ) {
        IoTApp app = new IoTApp();
    	//DeviceRuntime.run(app);
    	app.matrixTest();
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
        
        // // specify each of the connections on the harware, eg which component is plugged into which connection.        
              
        //c.connect(Button, BUTTON_PORT); 
        //c.connect(Relay, RELAY_PORT);         
        //c.connect(LightSensor, LIGHT_SENSOR_PORT); 
        //c.connect(LED, LED_PORT);        
        //c.useI2C();
        
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
    	runtime.registerListener(new Behavior(runtime));
    }
    
    
    public void matrixTest() {
    	
		//speed
		//slow     Doubles  Longs    6.15 5.8      7.024  7.18
		//         Decimals          5.9           9.40 - 13
		//         Floats            6.06           6.26
		//fast     Integers          5.80           5.95
		
    	
		MatrixTypes type = MatrixTypes.Floats;//Decimals;//Integers; //2, 3328335 longs/ints/doubles   [0,332833152] floats
		
		//TypeMask.Decimal;
		
		
		//when matrix size is larger than CPU cache we run into issues.
		int leftRows=500;
		int rightColumns=500;
				
		int leftColumns = 500;
		int rightRows=leftColumns;		
		
		
		//walk leftRows , by rightCol for output
		//5x2
		//2x3
		
		
		MatrixSchema leftSchema = BuildMatrixCompute.buildSchema(leftRows, leftColumns, type);		
		RowSchema<MatrixSchema> leftRowSchema = new RowSchema<MatrixSchema>(leftSchema);
		
		MatrixSchema rightSchema = BuildMatrixCompute.buildSchema(rightRows, rightColumns, type);
		RowSchema<MatrixSchema> rightRowSchema = new RowSchema<MatrixSchema>(rightSchema);
				
		MatrixSchema resultSchema = BuildMatrixCompute.buildResultSchema(leftSchema, rightSchema);
		RowSchema<MatrixSchema> rowResultSchema = new RowSchema<MatrixSchema>(resultSchema);		
		
		
		DecimalSchema result2Schema = new DecimalSchema<MatrixSchema>(resultSchema);

		
		GraphManager gm = new GraphManager();
		
		//GraphManager.addDefaultNota(gm, GraphManager.SCHEDULE_RATE, 500);
		
		//TODO: not sure why but the splitter that consumes left needs a minimum ring size or it gets stuck,
		Pipe<RowSchema<MatrixSchema>> left = new Pipe<RowSchema<MatrixSchema>>(new PipeConfig<RowSchema<MatrixSchema>>(leftRowSchema, leftRows));
		
		Pipe<RowSchema<MatrixSchema>> right = new Pipe<RowSchema<MatrixSchema>>(new PipeConfig<RowSchema<MatrixSchema>>(rightRowSchema, Math.min(16, rightRows)));
		
		Pipe<RowSchema<MatrixSchema>> result = new Pipe<RowSchema<MatrixSchema>>(new PipeConfig<RowSchema<MatrixSchema>>(rowResultSchema, Math.min(16, resultSchema.getRows()))); //NOTE: reqires 2 or JSON will not write out !!
	//	Pipe<DecimalSchema<MatrixSchema>> result2 = new Pipe<DecimalSchema<MatrixSchema>>(new PipeConfig<DecimalSchema<MatrixSchema>>(result2Schema, resultSchema.getRows())); //NOTE: reqires 2 or JSON will not write out !!
		
		
		int targetThreadCount = 60;
		Pipe<ColumnSchema<MatrixSchema>>[] colResults = BuildMatrixCompute.buildGraph(gm, resultSchema, leftSchema, rightSchema, left, right, targetThreadCount-2);
		
//		int x = colResults.length;
//		PipeCleanerStage[] watches = new PipeCleanerStage[colResults.length];
//		while (--x>=0) {
//			watches[x] =  new PipeCleanerStage<>(gm, colResults[x]);
//		}
		
		ColumnsToRowsStage<MatrixSchema> ctr = new ColumnsToRowsStage<MatrixSchema>(gm, resultSchema, colResults, result);
		
		
		//ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		
		//ConvertToDecimalStage<MatrixSchema> convert = new ConvertToDecimalStage<MatrixSchema>(gm, resultSchema, result, result2);
		
		//ConsoleJSONDumpStage<?> watch = new ConsoleJSONDumpStage<>(gm, result , new PrintStream(baos));
	//	ConsoleSummaryStage<RowSchema<MatrixSchema>> watch = new ConsoleSummaryStage<>(gm, result);
		
		PipeCleanerStage watch = new PipeCleanerStage<>(gm, result);
		
		GraphManager.enableBatching(gm);
		
		//gm.exportGraphDotFile();
		
		//MonitorConsoleStage.attach(gm);
		
		StageScheduler scheduler = new ThreadPerStageScheduler(gm);
			                     //new FixedThreadsScheduler(gm, targetThreadCount);
		
		scheduler.startup();	
		
		int testSize = 50;
		int k = testSize;
		long timeout = 0;
		while (--k>=0) {
			timeout = System.currentTimeMillis()+5000;
			//System.out.println(k);
			for(int c=0;c<leftRows;c++) {
				while (!Pipe.hasRoomForWrite(left)) {
					Thread.yield();
					if (System.currentTimeMillis()>timeout) {
						scheduler.shutdown();
						scheduler.awaitTermination(20, TimeUnit.SECONDS);
						
						return;
					}
				}
				Pipe.addMsgIdx(left, resultSchema.rowId);		
					for(int r=0;r<leftColumns;r++) {
						type.addValue(r, left);
					}
				Pipe.confirmLowLevelWrite(left, Pipe.sizeOf(left, resultSchema.rowId));
				Pipe.publishWrites(left);
			}
			
			for(int c=0;c<rightRows;c++) {
				while (!Pipe.hasRoomForWrite(right)) {
					Thread.yield();
					if (System.currentTimeMillis()>timeout) {
						scheduler.shutdown();
						scheduler.awaitTermination(20, TimeUnit.SECONDS);
						
						return;
					}
				}
				Pipe.addMsgIdx(right, resultSchema.rowId);		
					for(int r=0;r<rightColumns;r++) {
						type.addValue(c, right);
					}
				Pipe.confirmLowLevelWrite(right, Pipe.sizeOf(right, resultSchema.rowId));
				Pipe.publishWrites(right);
			}

		}
		
		
		if (k<0) {
			Pipe.spinBlockForRoom(left, Pipe.EOF_SIZE);
			Pipe.spinBlockForRoom(right, Pipe.EOF_SIZE);
			Pipe.publishEOF(left);
			Pipe.publishEOF(right);
		}
		
		GraphManager.blockUntilStageBeginsShutdown(gm, watch, 5000);//timeout in ms
		
//		//all the requests have now been sent.
//		int w = watches.length;
//		while (--w>=0) {
//			GraphManager.blockUntilStageBeginsShutdown(gm, watches[w], 5000);//timeout in ms
//		}

		scheduler.awaitTermination(20, TimeUnit.SECONDS);
				
	//	System.out.println("len "+baos.toByteArray().length+"  "+new String(baos.toByteArray()));
		
		
    	
    	
    }
    
        
  
}
