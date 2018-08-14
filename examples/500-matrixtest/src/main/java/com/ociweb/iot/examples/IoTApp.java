package com.ociweb.iot.examples;


import java.util.concurrent.TimeUnit;

import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.stage.math.BuildMatrixCompute;
import com.ociweb.pronghorn.stage.math.BuildMatrixCompute.MatrixTypes;
import com.ociweb.pronghorn.stage.math.ColumnSchema;
import com.ociweb.pronghorn.stage.math.ColumnsToRowsStage;
import com.ociweb.pronghorn.stage.math.DecimalSchema;
import com.ociweb.pronghorn.stage.math.MatrixSchema;
import com.ociweb.pronghorn.stage.math.RowSchema;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;
import com.ociweb.pronghorn.stage.test.PipeCleanerStage;

public class IoTApp implements FogApp
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
    public void declareBehavior(FogRuntime runtime) {
    	runtime.registerListener(new Behavior(runtime));
    }
    
    
    public <M extends MatrixSchema<M> > void matrixTest() {
    	
		//speed
		//slow     Doubles  Longs    6.15 5.8      7.024  7.18
		//         Decimals          5.9           9.40 - 13
		//         Floats            6.06           6.26
		//fast     Integers          5.80           5.95
		
    	
		MatrixTypes type = MatrixTypes.Integers;//Decimals;//Integers; //2, 3328335 longs/ints/doubles   [0,332833152] floats
		
		//TypeMask.Decimal;
		
		
		//when matrix size is larger than CPU cache we run into issues.
		int leftRows=1200;
		int leftColumns = 1000;
		int rightColumns=1000;
				
		int rightRows=leftColumns;		
		
		
		//walk leftRows , by rightCol for output
		//5x2
		//2x3
		
		
		MatrixSchema leftSchema = BuildMatrixCompute.buildSchema(leftRows, leftColumns, type);		
		RowSchema<M> leftRowSchema = new RowSchema<M>(leftSchema);
		
		MatrixSchema rightSchema = BuildMatrixCompute.buildSchema(rightRows, rightColumns, type);
		RowSchema<M> rightRowSchema = new RowSchema<M>(rightSchema);
				
		MatrixSchema resultSchema = BuildMatrixCompute.buildResultSchema(leftSchema, rightSchema);
		RowSchema<M> rowResultSchema = new RowSchema<M>(resultSchema);		
		
		
		DecimalSchema result2Schema = new DecimalSchema<M>(resultSchema);

		
		GraphManager gm = new GraphManager();
		
		//GraphManager.addDefaultNota(gm, GraphManager.SCHEDULE_RATE, 500);
		
		//TODO: not sure why but the splitter that consumes left needs a minimum ring size or it gets stuck,
		Pipe<RowSchema<M>> left = new Pipe<RowSchema<M>>(new PipeConfig<RowSchema<M>>(leftRowSchema, leftRows /*Math.min(16, leftRows)*/));
		
		Pipe<RowSchema<M>> right = new Pipe<RowSchema<M>>(new PipeConfig<RowSchema<M>>(rightRowSchema, rightRows /*Math.min(16, rightRows)*/));
		
		Pipe<RowSchema<M>> result = new Pipe<RowSchema<M>>(new PipeConfig<RowSchema<M>>(rowResultSchema, /*Math.min(16,*/ resultSchema.getRows() )); //NOTE: reqires 2 or JSON will not write out !!
	//	Pipe<DecimalSchema<MatrixSchema>> result2 = new Pipe<DecimalSchema<MatrixSchema>>(new PipeConfig<DecimalSchema<MatrixSchema>>(result2Schema, resultSchema.getRows())); //NOTE: reqires 2 or JSON will not write out !!
		
		
		int targetThreadCount = 10;//60; //105967ms
		Pipe<ColumnSchema<M>>[] colResults = BuildMatrixCompute.buildProductGraphRC(gm, left, right, targetThreadCount-2);
		
//		int x = colResults.length;
//		PipeCleanerStage[] watches = new PipeCleanerStage[colResults.length];
//		while (--x>=0) {
//			watches[x] =  new PipeCleanerStage<>(gm, colResults[x]);
//		}
		
		
		ColumnsToRowsStage<M> ctr = new ColumnsToRowsStage(gm, colResults, result);
		
		
		//ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		
		//ConvertToDecimalStage<MatrixSchema> convert = new ConvertToDecimalStage<MatrixSchema>(gm, resultSchema, result, result2);
		
		//ConsoleJSONDumpStage<?> watch = new ConsoleJSONDumpStage<>(gm, result , new PrintStream(baos));
	//	ConsoleSummaryStage<RowSchema<MatrixSchema>> watch = new ConsoleSummaryStage<>(gm, result);
		
		PipeCleanerStage watch = new PipeCleanerStage<>(gm, result);
		
		GraphManager.enableBatching(gm);
		
		//gm.exportGraphDotFile();//TODO: this may not work and cause issues.
		
		//MonitorConsoleStage.attach(gm);
		
		StageScheduler scheduler = new ThreadPerStageScheduler(gm);
			                      //new FixedThreadsScheduler(gm, targetThreadCount);
		
		scheduler.startup();	
		
		int testSize = 100;//5000;
		int k = testSize;
		long timeout = 0;
		while (--k>=0) {
			timeout = System.currentTimeMillis()+5000;

			for(int c=0;c<rightRows;c++) {
				while (!Pipe.hasRoomForWrite(right)) {
					Thread.yield();
					if (System.currentTimeMillis()>timeout) {
						scheduler.shutdown();
						scheduler.awaitTermination(20, TimeUnit.SECONDS);
						System.err.println("A, unable to write all of test data!");
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
			
			for(int c=0;c<leftRows;c++) {
				while (!Pipe.hasRoomForWrite(left)) {
					Thread.yield();
					if (System.currentTimeMillis()>timeout) {
						scheduler.shutdown();
						scheduler.awaitTermination(20, TimeUnit.SECONDS);
						System.err.println("B, unable to write all of test data!");
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
			

		}
		
		
		if (k<0) {
			//only works because we have multiple threads in play
			while (!Pipe.hasRoomForWrite(left, Pipe.EOF_SIZE)) {
			    Pipe.spinWork(left);
			}
			while (!Pipe.hasRoomForWrite(right, Pipe.EOF_SIZE)) {
			    Pipe.spinWork(right);
			}
			Pipe.publishEOF(left);
			Pipe.publishEOF(right);
		}

		
		if (GraphManager.blockUntilStageBeginsShutdown(gm, watch, 6000)) {//timeout in ms
		} else {
			System.err.println("time out!");
		}
			
		
		scheduler.shutdown();
		
		//all the requests have now been sent.
//		int w = watches.length;
//		while (--w>=0) {
//			if (!GraphManager.blockUntilStageBeginsShutdown(gm, watches[w], 5000)) {//timeout in ms
//				System.err.println("ERROR SHUTDOWN");
//				System.exit(-1);
//			}
//		}

		scheduler.awaitTermination(10, TimeUnit.SECONDS);
				
	//	System.out.println("len "+baos.toByteArray().length+"  "+new String(baos.toByteArray()));
		
		
    	
    	
    }
    
        
  
}
