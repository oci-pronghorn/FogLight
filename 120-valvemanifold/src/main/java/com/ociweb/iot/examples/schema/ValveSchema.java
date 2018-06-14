package com.ociweb.iot.examples.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;

public class ValveSchema extends MessageSchema<ValveSchema> {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400004,0x80000000,0x90000000,0x80000001,0xc0200004,0xc0400004,0x80000000,0x90000000,0x80000002,0xc0200004,0xc0400004,0x80000000,0x90000000,0x80000003,0xc0200004,0xc0400004,0x80000000,0x90000000,0x80000004,0xc0200004,0xc0400004,0x80000000,0x90000000,0x80000005,0xc0200004,0xc0400004,0x80000000,0x90000000,0x80000006,0xc0200004,0xc0400004,0x80000000,0x90000000,0x88000007,0xc0200004,0xc0400004,0x80000000,0x90000000,0x80000008,0xc0200004,0xc0400004,0x80000000,0x90000000,0xa8000000,0xc0200004,0xc0400004,0x80000000,0x90000000,0x80000009,0xc0200004,0xc0400004,0x80000000,0x90000000,0xa8000001,0xc0200004,0xc0400004,0x80000000,0x90000000,0x8000000a,0xc0200004,0xc0400004,0x80000000,0x90000000,0x8000000b,0xc0200004,0xc0400004,0x80000000,0x90000000,0x8000000c,0xc0200004},
            (short)0,
            new String[]{"ValveSerialNumber","Station","Timestamp","ValveSerialNumber",null,"LifeCycleCount",
                    "Station","Timestamp","LifeCycleCount",null,"SupplyPressure","Station","Timestamp",
                    "SupplyPressure",null,"DurationOfLast1_4Signal","Station","Timestamp","DurationOfLast1_4Signal",
                    null,"DurationOfLast1_2Signal","Station","Timestamp","DurationOfLast1_2Signal",null,
                    "EqualizationAveragePressure","Station","Timestamp","EqualizationAveragePressure",
                    null,"EqualizationPressureRate","Station","Timestamp","EqualizationPressureRate",
                    null,"ResidualOfDynamicAnalysis","Station","Timestamp","ResidualOfDynamicAnalysis",
                    null,"PartNumber","Station","Timestamp","PartNumber",null,"ValveFault","Station",
                    "Timestamp","ValveFault",null,"PressureFault","Station","Timestamp","PressureFault",
                    null,"LeakFault","Station","Timestamp","LeakFault",null,"DataFault","Station","Timestamp",
                    "DataFault",null,"PressurePoint","Station","Timestamp","PressurePoint",null},
            new long[]{311, 1, 2, 11, 0, 312, 1, 2, 12, 0, 313, 1, 2, 13, 0, 314, 1, 2, 14, 0, 315, 1, 2, 15, 0, 316, 1, 2, 16, 0, 317, 1, 2, 17, 0, 318, 1, 2, 18, 0, 330, 1, 2, 30, 0, 340, 1, 2, 40, 0, 350, 1, 2, 50, 0, 360, 1, 2, 60, 0, 362, 1, 2, 62, 0, 319, 1, 2, 19, 0},
            new String[]{"global",null,null,null,null,"global",null,null,null,null,"global",null,null,null,
                    null,"global",null,null,null,null,"global",null,null,null,null,"global",null,null,
                    null,null,"global",null,null,null,null,"global",null,null,null,null,"global",null,
                    null,null,null,"global",null,null,null,null,"global",null,null,null,null,"global",
                    null,null,null,null,"global",null,null,null,null,"global",null,null,null,null},
            "ValveSchema.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});


    protected ValveSchema() {
        super(FROM);
    }

    public static final ValveSchema instance = new ValveSchema();

    public static final int MSG_VALVESERIALNUMBER_311 = 0x00000000;
    public static final int MSG_VALVESERIALNUMBER_311_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_VALVESERIALNUMBER_311_FIELD_TIMESTAMP_2 = 0x00800002;
    public static final int MSG_VALVESERIALNUMBER_311_FIELD_VALVESERIALNUMBER_11 = 0x00000004;
    public static final int MSG_LIFECYCLECOUNT_312 = 0x00000005;
    public static final int MSG_LIFECYCLECOUNT_312_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_LIFECYCLECOUNT_312_FIELD_TIMESTAMP_2 = 0x00800002;
    public static final int MSG_LIFECYCLECOUNT_312_FIELD_LIFECYCLECOUNT_12 = 0x00000004;
    public static final int MSG_SUPPLYPRESSURE_313 = 0x0000000a;
    public static final int MSG_SUPPLYPRESSURE_313_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_SUPPLYPRESSURE_313_FIELD_TIMESTAMP_2 = 0x00800002;
    public static final int MSG_SUPPLYPRESSURE_313_FIELD_SUPPLYPRESSURE_13 = 0x00000004;
    public static final int MSG_DURATIONOFLAST1_4SIGNAL_314 = 0x0000000f;
    public static final int MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_TIMESTAMP_2 = 0x00800002;
    public static final int MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_DURATIONOFLAST1_4SIGNAL_14 = 0x00000004;
    public static final int MSG_DURATIONOFLAST1_2SIGNAL_315 = 0x00000014;
    public static final int MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_TIMESTAMP_2 = 0x00800002;
    public static final int MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_DURATIONOFLAST1_2SIGNAL_15 = 0x00000004;
    public static final int MSG_EQUALIZATIONAVERAGEPRESSURE_316 = 0x00000019;
    public static final int MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_TIMESTAMP_2 = 0x00800002;
    public static final int MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_EQUALIZATIONAVERAGEPRESSURE_16 = 0x00000004;
    public static final int MSG_EQUALIZATIONPRESSURERATE_317 = 0x0000001e;
    public static final int MSG_EQUALIZATIONPRESSURERATE_317_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_EQUALIZATIONPRESSURERATE_317_FIELD_TIMESTAMP_2 = 0x00800002;
    public static final int MSG_EQUALIZATIONPRESSURERATE_317_FIELD_EQUALIZATIONPRESSURERATE_17 = 0x00400004;
    public static final int MSG_RESIDUALOFDYNAMICANALYSIS_318 = 0x00000023;
    public static final int MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_TIMESTAMP_2 = 0x00800002;
    public static final int MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_RESIDUALOFDYNAMICANALYSIS_18 = 0x00000004;
    public static final int MSG_PARTNUMBER_330 = 0x00000028;
    public static final int MSG_PARTNUMBER_330_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_PARTNUMBER_330_FIELD_TIMESTAMP_2 = 0x00800002;
    public static final int MSG_PARTNUMBER_330_FIELD_PARTNUMBER_30 = 0x01400004;
    public static final int MSG_VALVEFAULT_340 = 0x0000002d;
    public static final int MSG_VALVEFAULT_340_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_VALVEFAULT_340_FIELD_TIMESTAMP_2 = 0x00800002;
    public static final int MSG_VALVEFAULT_340_FIELD_VALVEFAULT_40 = 0x00000004;
    public static final int MSG_PRESSUREFAULT_350 = 0x00000032;
    public static final int MSG_PRESSUREFAULT_350_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_PRESSUREFAULT_350_FIELD_TIMESTAMP_2 = 0x00800002;
    public static final int MSG_PRESSUREFAULT_350_FIELD_PRESSUREFAULT_50 = 0x01400004;
    public static final int MSG_LEAKFAULT_360 = 0x00000037;
    public static final int MSG_LEAKFAULT_360_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_LEAKFAULT_360_FIELD_TIMESTAMP_2 = 0x00800002;
    public static final int MSG_LEAKFAULT_360_FIELD_LEAKFAULT_60 = 0x00000004;
    public static final int MSG_DATAFAULT_362 = 0x0000003c;
    public static final int MSG_DATAFAULT_362_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_DATAFAULT_362_FIELD_TIMESTAMP_2 = 0x00800002;
    public static final int MSG_DATAFAULT_362_FIELD_DATAFAULT_62 = 0x00000004;
    public static final int MSG_PRESSUREPOINT_319 = 0x00000041;
    public static final int MSG_PRESSUREPOINT_319_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_PRESSUREPOINT_319_FIELD_TIMESTAMP_2 = 0x00800002;
    public static final int MSG_PRESSUREPOINT_319_FIELD_PRESSUREPOINT_19 = 0x00000004;


    public static void consume(Pipe<ValveSchema> input) {
        while (PipeReader.tryReadFragment(input)) {
            int msgIdx = PipeReader.getMsgIdx(input);
            switch(msgIdx) {
                case MSG_VALVESERIALNUMBER_311:
                    consumeValveSerialNumber(input);
                    break;
                case MSG_LIFECYCLECOUNT_312:
                    consumeLifeCycleCount(input);
                    break;
                case MSG_SUPPLYPRESSURE_313:
                    consumeSupplyPressure(input);
                    break;
                case MSG_DURATIONOFLAST1_4SIGNAL_314:
                    consumeDurationOfLast1_4Signal(input);
                    break;
                case MSG_DURATIONOFLAST1_2SIGNAL_315:
                    consumeDurationOfLast1_2Signal(input);
                    break;
                case MSG_EQUALIZATIONAVERAGEPRESSURE_316:
                    consumeEqualizationAveragePressure(input);
                    break;
                case MSG_EQUALIZATIONPRESSURERATE_317:
                    consumeEqualizationPressureRate(input);
                    break;
                case MSG_RESIDUALOFDYNAMICANALYSIS_318:
                    consumeResidualOfDynamicAnalysis(input);
                    break;
                case MSG_PARTNUMBER_330:
                    consumePartNumber(input);
                    break;
                case MSG_VALVEFAULT_340:
                    consumeValveFault(input);
                    break;
                case MSG_PRESSUREFAULT_350:
                    consumePressureFault(input);
                    break;
                case MSG_LEAKFAULT_360:
                    consumeLeakFault(input);
                    break;
                case MSG_DATAFAULT_362:
                    consumeDataFault(input);
                    break;
                case MSG_PRESSUREPOINT_319:
                    consumePressurePoint(input);
                    break;
                case -1:
                    //requestShutdown();
                    break;
            }
            PipeReader.releaseReadLock(input);
        }
    }

    public static void consumeValveSerialNumber(Pipe<ValveSchema> input) {
        int fieldStation = PipeReader.readInt(input,MSG_VALVESERIALNUMBER_311_FIELD_STATION_1);
        long fieldTimestamp = PipeReader.readLong(input,MSG_VALVESERIALNUMBER_311_FIELD_TIMESTAMP_2);
        int fieldValveSerialNumber = PipeReader.readInt(input,MSG_VALVESERIALNUMBER_311_FIELD_VALVESERIALNUMBER_11);
    }
    public static void consumeLifeCycleCount(Pipe<ValveSchema> input) {
        int fieldStation = PipeReader.readInt(input,MSG_LIFECYCLECOUNT_312_FIELD_STATION_1);
        long fieldTimestamp = PipeReader.readLong(input,MSG_LIFECYCLECOUNT_312_FIELD_TIMESTAMP_2);
        int fieldLifeCycleCount = PipeReader.readInt(input,MSG_LIFECYCLECOUNT_312_FIELD_LIFECYCLECOUNT_12);
    }
    public static void consumeSupplyPressure(Pipe<ValveSchema> input) {
        int fieldStation = PipeReader.readInt(input,MSG_SUPPLYPRESSURE_313_FIELD_STATION_1);
        long fieldTimestamp = PipeReader.readLong(input,MSG_SUPPLYPRESSURE_313_FIELD_TIMESTAMP_2);
        int fieldSupplyPressure = PipeReader.readInt(input,MSG_SUPPLYPRESSURE_313_FIELD_SUPPLYPRESSURE_13);
    }
    public static void consumeDurationOfLast1_4Signal(Pipe<ValveSchema> input) {
        int fieldStation = PipeReader.readInt(input,MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_STATION_1);
        long fieldTimestamp = PipeReader.readLong(input,MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_TIMESTAMP_2);
        int fieldDurationOfLast1_4Signal = PipeReader.readInt(input,MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_DURATIONOFLAST1_4SIGNAL_14);
    }
    public static void consumeDurationOfLast1_2Signal(Pipe<ValveSchema> input) {
        int fieldStation = PipeReader.readInt(input,MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_STATION_1);
        long fieldTimestamp = PipeReader.readLong(input,MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_TIMESTAMP_2);
        int fieldDurationOfLast1_2Signal = PipeReader.readInt(input,MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_DURATIONOFLAST1_2SIGNAL_15);
    }
    public static void consumeEqualizationAveragePressure(Pipe<ValveSchema> input) {
        int fieldStation = PipeReader.readInt(input,MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_STATION_1);
        long fieldTimestamp = PipeReader.readLong(input,MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_TIMESTAMP_2);
        int fieldEqualizationAveragePressure = PipeReader.readInt(input,MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_EQUALIZATIONAVERAGEPRESSURE_16);
    }
    public static void consumeEqualizationPressureRate(Pipe<ValveSchema> input) {
        int fieldStation = PipeReader.readInt(input,MSG_EQUALIZATIONPRESSURERATE_317_FIELD_STATION_1);
        long fieldTimestamp = PipeReader.readLong(input,MSG_EQUALIZATIONPRESSURERATE_317_FIELD_TIMESTAMP_2);
        int fieldEqualizationPressureRate = PipeReader.readInt(input,MSG_EQUALIZATIONPRESSURERATE_317_FIELD_EQUALIZATIONPRESSURERATE_17);
    }
    public static void consumeResidualOfDynamicAnalysis(Pipe<ValveSchema> input) {
        int fieldStation = PipeReader.readInt(input,MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_STATION_1);
        long fieldTimestamp = PipeReader.readLong(input,MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_TIMESTAMP_2);
        int fieldResidualOfDynamicAnalysis = PipeReader.readInt(input,MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_RESIDUALOFDYNAMICANALYSIS_18);
    }
    public static void consumePartNumber(Pipe<ValveSchema> input) {
        int fieldStation = PipeReader.readInt(input,MSG_PARTNUMBER_330_FIELD_STATION_1);
        long fieldTimestamp = PipeReader.readLong(input,MSG_PARTNUMBER_330_FIELD_TIMESTAMP_2);
        StringBuilder fieldPartNumber = PipeReader.readUTF8(input,MSG_PARTNUMBER_330_FIELD_PARTNUMBER_30,new StringBuilder(PipeReader.readBytesLength(input,MSG_PARTNUMBER_330_FIELD_PARTNUMBER_30)));
    }
    public static void consumeValveFault(Pipe<ValveSchema> input) {
        int fieldStation = PipeReader.readInt(input,MSG_VALVEFAULT_340_FIELD_STATION_1);
        long fieldTimestamp = PipeReader.readLong(input,MSG_VALVEFAULT_340_FIELD_TIMESTAMP_2);
        int fieldValveFault = PipeReader.readInt(input,MSG_VALVEFAULT_340_FIELD_VALVEFAULT_40);
    }
    public static void consumePressureFault(Pipe<ValveSchema> input) {
        int fieldStation = PipeReader.readInt(input,MSG_PRESSUREFAULT_350_FIELD_STATION_1);
        long fieldTimestamp = PipeReader.readLong(input,MSG_PRESSUREFAULT_350_FIELD_TIMESTAMP_2);
        StringBuilder fieldPressureFault = PipeReader.readUTF8(input,MSG_PRESSUREFAULT_350_FIELD_PRESSUREFAULT_50,new StringBuilder(PipeReader.readBytesLength(input,MSG_PRESSUREFAULT_350_FIELD_PRESSUREFAULT_50)));
    }
    public static void consumeLeakFault(Pipe<ValveSchema> input) {
        int fieldStation = PipeReader.readInt(input,MSG_LEAKFAULT_360_FIELD_STATION_1);
        long fieldTimestamp = PipeReader.readLong(input,MSG_LEAKFAULT_360_FIELD_TIMESTAMP_2);
        int fieldLeakFault = PipeReader.readInt(input,MSG_LEAKFAULT_360_FIELD_LEAKFAULT_60);
    }
    public static void consumeDataFault(Pipe<ValveSchema> input) {
        int fieldStation = PipeReader.readInt(input,MSG_DATAFAULT_362_FIELD_STATION_1);
        long fieldTimestamp = PipeReader.readLong(input,MSG_DATAFAULT_362_FIELD_TIMESTAMP_2);
        int fieldDataFault = PipeReader.readInt(input,MSG_DATAFAULT_362_FIELD_DATAFAULT_62);
    }
    public static void consumePressurePoint(Pipe<ValveSchema> input) {
        int fieldStation = PipeReader.readInt(input,MSG_PRESSUREPOINT_319_FIELD_STATION_1);
        long fieldTimestamp = PipeReader.readLong(input,MSG_PRESSUREPOINT_319_FIELD_TIMESTAMP_2);
        int fieldPressurePoint = PipeReader.readInt(input,MSG_PRESSUREPOINT_319_FIELD_PRESSUREPOINT_19);
    }

    public static void publishValveSerialNumber(Pipe<ValveSchema> output, int fieldStation, long fieldTimestamp, int fieldValveSerialNumber) {
        PipeWriter.presumeWriteFragment(output, MSG_VALVESERIALNUMBER_311);
        PipeWriter.writeInt(output,MSG_VALVESERIALNUMBER_311_FIELD_STATION_1, fieldStation);
        PipeWriter.writeLong(output,MSG_VALVESERIALNUMBER_311_FIELD_TIMESTAMP_2, fieldTimestamp);
        PipeWriter.writeInt(output,MSG_VALVESERIALNUMBER_311_FIELD_VALVESERIALNUMBER_11, fieldValveSerialNumber);
        PipeWriter.publishWrites(output);
    }
    public static void publishLifeCycleCount(Pipe<ValveSchema> output, int fieldStation, long fieldTimestamp, int fieldLifeCycleCount) {
        PipeWriter.presumeWriteFragment(output, MSG_LIFECYCLECOUNT_312);
        PipeWriter.writeInt(output,MSG_LIFECYCLECOUNT_312_FIELD_STATION_1, fieldStation);
        PipeWriter.writeLong(output,MSG_LIFECYCLECOUNT_312_FIELD_TIMESTAMP_2, fieldTimestamp);
        PipeWriter.writeInt(output,MSG_LIFECYCLECOUNT_312_FIELD_LIFECYCLECOUNT_12, fieldLifeCycleCount);
        PipeWriter.publishWrites(output);
    }
    public static void publishSupplyPressure(Pipe<ValveSchema> output, int fieldStation, long fieldTimestamp, int fieldSupplyPressure) {
        PipeWriter.presumeWriteFragment(output, MSG_SUPPLYPRESSURE_313);
        PipeWriter.writeInt(output,MSG_SUPPLYPRESSURE_313_FIELD_STATION_1, fieldStation);
        PipeWriter.writeLong(output,MSG_SUPPLYPRESSURE_313_FIELD_TIMESTAMP_2, fieldTimestamp);
        PipeWriter.writeInt(output,MSG_SUPPLYPRESSURE_313_FIELD_SUPPLYPRESSURE_13, fieldSupplyPressure);
        PipeWriter.publishWrites(output);
    }
    public static void publishDurationOfLast1_4Signal(Pipe<ValveSchema> output, int fieldStation, long fieldTimestamp, int fieldDurationOfLast1_4Signal) {
        PipeWriter.presumeWriteFragment(output, MSG_DURATIONOFLAST1_4SIGNAL_314);
        PipeWriter.writeInt(output,MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_STATION_1, fieldStation);
        PipeWriter.writeLong(output,MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_TIMESTAMP_2, fieldTimestamp);
        PipeWriter.writeInt(output,MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_DURATIONOFLAST1_4SIGNAL_14, fieldDurationOfLast1_4Signal);
        PipeWriter.publishWrites(output);
    }
    public static void publishDurationOfLast1_2Signal(Pipe<ValveSchema> output, int fieldStation, long fieldTimestamp, int fieldDurationOfLast1_2Signal) {
        PipeWriter.presumeWriteFragment(output, MSG_DURATIONOFLAST1_2SIGNAL_315);
        PipeWriter.writeInt(output,MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_STATION_1, fieldStation);
        PipeWriter.writeLong(output,MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_TIMESTAMP_2, fieldTimestamp);
        PipeWriter.writeInt(output,MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_DURATIONOFLAST1_2SIGNAL_15, fieldDurationOfLast1_2Signal);
        PipeWriter.publishWrites(output);
    }
    public static void publishEqualizationAveragePressure(Pipe<ValveSchema> output, int fieldStation, long fieldTimestamp, int fieldEqualizationAveragePressure) {
        PipeWriter.presumeWriteFragment(output, MSG_EQUALIZATIONAVERAGEPRESSURE_316);
        PipeWriter.writeInt(output,MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_STATION_1, fieldStation);
        PipeWriter.writeLong(output,MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_TIMESTAMP_2, fieldTimestamp);
        PipeWriter.writeInt(output,MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_EQUALIZATIONAVERAGEPRESSURE_16, fieldEqualizationAveragePressure);
        PipeWriter.publishWrites(output);
    }
    public static void publishEqualizationPressureRate(Pipe<ValveSchema> output, int fieldStation, long fieldTimestamp, int fieldEqualizationPressureRate) {
        PipeWriter.presumeWriteFragment(output, MSG_EQUALIZATIONPRESSURERATE_317);
        PipeWriter.writeInt(output,MSG_EQUALIZATIONPRESSURERATE_317_FIELD_STATION_1, fieldStation);
        PipeWriter.writeLong(output,MSG_EQUALIZATIONPRESSURERATE_317_FIELD_TIMESTAMP_2, fieldTimestamp);
        PipeWriter.writeInt(output,MSG_EQUALIZATIONPRESSURERATE_317_FIELD_EQUALIZATIONPRESSURERATE_17, fieldEqualizationPressureRate);
        PipeWriter.publishWrites(output);
    }
    public static void publishResidualOfDynamicAnalysis(Pipe<ValveSchema> output, int fieldStation, long fieldTimestamp, int fieldResidualOfDynamicAnalysis) {
        PipeWriter.presumeWriteFragment(output, MSG_RESIDUALOFDYNAMICANALYSIS_318);
        PipeWriter.writeInt(output,MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_STATION_1, fieldStation);
        PipeWriter.writeLong(output,MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_TIMESTAMP_2, fieldTimestamp);
        PipeWriter.writeInt(output,MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_RESIDUALOFDYNAMICANALYSIS_18, fieldResidualOfDynamicAnalysis);
        PipeWriter.publishWrites(output);
    }
    public static void publishPartNumber(Pipe<ValveSchema> output, int fieldStation, long fieldTimestamp, CharSequence fieldPartNumber) {
        PipeWriter.presumeWriteFragment(output, MSG_PARTNUMBER_330);
        PipeWriter.writeInt(output,MSG_PARTNUMBER_330_FIELD_STATION_1, fieldStation);
        PipeWriter.writeLong(output,MSG_PARTNUMBER_330_FIELD_TIMESTAMP_2, fieldTimestamp);
        PipeWriter.writeUTF8(output,MSG_PARTNUMBER_330_FIELD_PARTNUMBER_30, fieldPartNumber);
        PipeWriter.publishWrites(output);
    }
    public static void publishValveFault(Pipe<ValveSchema> output, int fieldStation, long fieldTimestamp, int fieldValveFault) {
        PipeWriter.presumeWriteFragment(output, MSG_VALVEFAULT_340);
        PipeWriter.writeInt(output,MSG_VALVEFAULT_340_FIELD_STATION_1, fieldStation);
        PipeWriter.writeLong(output,MSG_VALVEFAULT_340_FIELD_TIMESTAMP_2, fieldTimestamp);
        PipeWriter.writeInt(output,MSG_VALVEFAULT_340_FIELD_VALVEFAULT_40, fieldValveFault);
        PipeWriter.publishWrites(output);
    }
    public static void publishPressureFault(Pipe<ValveSchema> output, int fieldStation, long fieldTimestamp, CharSequence fieldPressureFault) {
        PipeWriter.presumeWriteFragment(output, MSG_PRESSUREFAULT_350);
        PipeWriter.writeInt(output,MSG_PRESSUREFAULT_350_FIELD_STATION_1, fieldStation);
        PipeWriter.writeLong(output,MSG_PRESSUREFAULT_350_FIELD_TIMESTAMP_2, fieldTimestamp);
        PipeWriter.writeUTF8(output,MSG_PRESSUREFAULT_350_FIELD_PRESSUREFAULT_50, fieldPressureFault);
        PipeWriter.publishWrites(output);
    }
    public static void publishLeakFault(Pipe<ValveSchema> output, int fieldStation, long fieldTimestamp, int fieldLeakFault) {
        PipeWriter.presumeWriteFragment(output, MSG_LEAKFAULT_360);
        PipeWriter.writeInt(output,MSG_LEAKFAULT_360_FIELD_STATION_1, fieldStation);
        PipeWriter.writeLong(output,MSG_LEAKFAULT_360_FIELD_TIMESTAMP_2, fieldTimestamp);
        PipeWriter.writeInt(output,MSG_LEAKFAULT_360_FIELD_LEAKFAULT_60, fieldLeakFault);
        PipeWriter.publishWrites(output);
    }
    public static void publishDataFault(Pipe<ValveSchema> output, int fieldStation, long fieldTimestamp, int fieldDataFault) {
        PipeWriter.presumeWriteFragment(output, MSG_DATAFAULT_362);
        PipeWriter.writeInt(output,MSG_DATAFAULT_362_FIELD_STATION_1, fieldStation);
        PipeWriter.writeLong(output,MSG_DATAFAULT_362_FIELD_TIMESTAMP_2, fieldTimestamp);
        PipeWriter.writeInt(output,MSG_DATAFAULT_362_FIELD_DATAFAULT_62, fieldDataFault);
        PipeWriter.publishWrites(output);
    }
    public static void publishPressurePoint(Pipe<ValveSchema> output, int fieldStation, long fieldTimestamp, int fieldPressurePoint) {
        PipeWriter.presumeWriteFragment(output, MSG_PRESSUREPOINT_319);
        PipeWriter.writeInt(output,MSG_PRESSUREPOINT_319_FIELD_STATION_1, fieldStation);
        PipeWriter.writeLong(output,MSG_PRESSUREPOINT_319_FIELD_TIMESTAMP_2, fieldTimestamp);
        PipeWriter.writeInt(output,MSG_PRESSUREPOINT_319_FIELD_PRESSUREPOINT_19, fieldPressurePoint);
        PipeWriter.publishWrites(output);
    }
}