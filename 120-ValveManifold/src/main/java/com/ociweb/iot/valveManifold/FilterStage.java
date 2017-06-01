package com.ociweb.iot.valveManifold;

import com.ociweb.iot.valveManifold.schema.ValveSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class FilterStage extends PronghornStage {

	private final Pipe<ValveSchema> input;
	private final Pipe<ValveSchema> output;
	
	protected FilterStage(GraphManager graphManager, Pipe<ValveSchema> input, Pipe<ValveSchema> output) {
		super(graphManager, input, output);
		this.input=input;
		this.output=input;
	}

	@Override
	public void run() {
				
		while ( PipeWriter.hasRoomForWrite(output) &&
				PipeReader.tryReadFragment(input)) {
			
		    int msgIdx = PipeReader.getMsgIdx(input);
		    switch(msgIdx) {
		        case ValveSchema.MSG_MANIFOLDSERIALNUMBER_310:
		            ValveSchema.consumeManifoldSerialNumber(input);
		        break;
		        case ValveSchema.MSG_VALVESERIALNUMBER_311:
		            ValveSchema.consumeValveSerialNumber(input);
		        break;
		        case ValveSchema.MSG_LIFECYCLECOUNT_312:
		            ValveSchema.consumeLifeCycleCount(input);
		            
		           // ValveSchema.instance.publishLifeCycleCount(output, fieldStation, fieldValveSerialNumber)
		            
		        break;
		        case ValveSchema.MSG_SUPPLYPRESSURE_313:
				
		        	int fieldStation = PipeReader.readInt(input,ValveSchema.MSG_SUPPLYPRESSURE_313_FIELD_STATION_1);
		        	int fieldSupplyPressure = PipeReader.readInt(input,ValveSchema.MSG_SUPPLYPRESSURE_313_FIELD_SUPPLYPRESSURE_13);

		        	//TODO: find that this is a new value??
			
				    PipeWriter.presumeWriteFragment(output, ValveSchema.MSG_SUPPLYPRESSURE_313);
				    PipeWriter.writeInt(output,ValveSchema.MSG_SUPPLYPRESSURE_313_FIELD_STATION_1, fieldStation);
				    PipeWriter.writeInt(output,ValveSchema.MSG_SUPPLYPRESSURE_313_FIELD_SUPPLYPRESSURE_13, fieldSupplyPressure);
				    PipeWriter.publishWrites(output);
			
		        	break;
		        case ValveSchema.MSG_DURATIONOFLAST1_4SIGNAL_314:
		            ValveSchema.consumeDurationOfLast1_4Signal(input);
		        break;
		        case ValveSchema.MSG_DURATIONOFLAST1_2SIGNAL_315:
		            ValveSchema.consumeDurationOfLast1_2Signal(input);
		        break;
		        case ValveSchema.MSG_EQUALIZATIONAVERAGEPRESSURE_316:
		            ValveSchema.consumeEqualizationAveragePressure(input);
		        break;
		        case ValveSchema.MSG_EQUALIZATIONPRESSURERATE_317:
		            ValveSchema.consumeEqualizationPressureRate(input);
		        break;
		        case ValveSchema.MSG_RESIDUALOFDYNAMICANALYSIS_318:
		            ValveSchema.consumeResidualOfDynamicAnalysis(input);
		        break;
		        case ValveSchema.MSG_PARTNUMBER_330:
		            ValveSchema.consumePartNumber(input);
		        break;
		        case ValveSchema.MSG_VALUEFAULT_FALSE_340:
		            ValveSchema.consumeValueFault_False(input);
		        break;
		        case ValveSchema.MSG_VALUEFAULT_TRUE_341:
		            ValveSchema.consumeValueFault_True(input);
		        break;
		        case ValveSchema.MSG_PRESSUREFAULT_LOW_350:
		            ValveSchema.consumePressureFault_Low(input);
		        break;
		        case ValveSchema.MSG_PRESSUREFAULT_NONE_351:
		            ValveSchema.consumePressureFault_None(input);
		        break;
		        case ValveSchema.MSG_PRESSUREFAULT_HIGH_352:
		            ValveSchema.consumePressureFault_High(input);
		        break;
		        case ValveSchema.MSG_LEAKFAULT_FALSE_360:
		            ValveSchema.consumeLeakFault_False(input);
		        break;
		        case ValveSchema.MSG_LEAKFAULT_TRUE_361:
		            ValveSchema.consumeLeakFault_True(input);
		        break;
		        case ValveSchema.MSG_PRESSUREPOINT_319:
		            ValveSchema.consumePressurePoint(input);
		        break;
		        case -1:
		           requestShutdown();
		        break;
		    }
		    PipeReader.releaseReadLock(input);
		}
		
		
	}

}
