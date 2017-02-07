package com.ociweb.iot.valveManifold.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class ValveSchema extends MessageSchema {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400003,0x80000000,0x80000001,0xc0200003,0xc0400003,0x80000000,0x80000002,0xc0200003,0xc0400003,0x80000000,0x80000003,0xc0200003,0xc0400003,0x80000000,0x80000004,0xc0200003,0xc0400003,0x80000000,0x80000005,0xc0200003,0xc0400003,0x80000000,0x80000006,0xc0200003,0xc0400003,0x80000000,0x80000007,0xc0200003,0xc0400003,0x80000000,0x88000008,0xc0200003,0xc0400003,0x80000000,0x80000009,0xc0200003,0xc0400003,0x80000000,0xa8000000,0xc0200003,0xc0400002,0x80000000,0xc0200002,0xc0400002,0x80000000,0xc0200002,0xc0400002,0x80000000,0xc0200002,0xc0400002,0x80000000,0xc0200002,0xc0400002,0x80000000,0xc0200002,0xc0400002,0x80000000,0xc0200002,0xc0400002,0x80000000,0xc0200002,0xc0400003,0x80000000,0x8000000a,0xc0200003},
		    (short)0,
		    new String[]{"ManifoldSerialNumber","Station","ManifoldSerialNumber",null,"ValveSerialNumber",
		    "Station","ValveSerialNumber",null,"LifeCycleCount","Station","ValveSerialNumber",
		    null,"SupplyPressure","Station","SupplyPressure",null,"DurationOfLast1_4Signal","Station",
		    "DurationOfLast1_4Signal",null,"DurationOfLast1_2Signal","Station","DurationOfLast1_2Signal",
		    null,"EqualizationAveragePressure","Station","EqualizationAveragePressure",null,"EqualizationPressureRate",
		    "Station","EqualizationPressureRate",null,"ResidualOfDynamicAnalysis","Station","ResidualOfDynamicAnalysis",
		    null,"PartNumber","Station","PartNumber",null,"ValueFault/False","Station",null,"ValueFault/True",
		    "Station",null,"PressureFault/Low","Station",null,"PressureFault/None","Station",
		    null,"PressureFault/High","Station",null,"LeakFault/False","Station",null,"LeakFault/True",
		    "Station",null,"PressurePoint","Station","PressurePoint",null},
		    new long[]{310, 1, 10, 0, 311, 1, 11, 0, 312, 1, 12, 0, 313, 1, 13, 0, 314, 1, 14, 0, 315, 1, 15, 0, 316, 1, 16, 0, 317, 1, 17, 0, 318, 1, 18, 0, 330, 1, 30, 0, 340, 1, 0, 341, 1, 0, 350, 1, 0, 351, 1, 0, 352, 1, 0, 360, 1, 0, 361, 1, 0, 319, 1, 19, 0},
		    new String[]{"global",null,null,null,"global",null,null,null,"global",null,null,null,"global",
		    null,null,null,"global",null,null,null,"global",null,null,null,"global",null,null,
		    null,"global",null,null,null,"global",null,null,null,"global",null,null,null,"global",
		    null,null,"global",null,null,"global",null,null,"global",null,null,"global",null,
		    null,"global",null,null,"global",null,null,"global",null,null,null},
		    "ValveSchema.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});

    
    public static final ValveSchema instance = new ValveSchema();
    
    public static final int MSG_MANIFOLDSERIALNUMBER_310 = 0x00000000;
    public static final int MSG_MANIFOLDSERIALNUMBER_310_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_MANIFOLDSERIALNUMBER_310_FIELD_MANIFOLDSERIALNUMBER_10 = 0x00000002;
    public static final int MSG_VALVESERIALNUMBER_311 = 0x00000004;
    public static final int MSG_VALVESERIALNUMBER_311_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_VALVESERIALNUMBER_311_FIELD_VALVESERIALNUMBER_11 = 0x00000002;
    public static final int MSG_LIFECYCLECOUNT_312 = 0x00000008;
    public static final int MSG_LIFECYCLECOUNT_312_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_LIFECYCLECOUNT_312_FIELD_VALVESERIALNUMBER_12 = 0x00000002;
    public static final int MSG_SUPPLYPRESSURE_313 = 0x0000000c;
    public static final int MSG_SUPPLYPRESSURE_313_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_SUPPLYPRESSURE_313_FIELD_SUPPLYPRESSURE_13 = 0x00000002;
    public static final int MSG_DURATIONOFLAST1_4SIGNAL_314 = 0x00000010;
    public static final int MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_DURATIONOFLAST1_4SIGNAL_14 = 0x00000002;
    public static final int MSG_DURATIONOFLAST1_2SIGNAL_315 = 0x00000014;
    public static final int MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_DURATIONOFLAST1_2SIGNAL_15 = 0x00000002;
    public static final int MSG_EQUALIZATIONAVERAGEPRESSURE_316 = 0x00000018;
    public static final int MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_EQUALIZATIONAVERAGEPRESSURE_16 = 0x00000002;
    public static final int MSG_EQUALIZATIONPRESSURERATE_317 = 0x0000001c;
    public static final int MSG_EQUALIZATIONPRESSURERATE_317_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_EQUALIZATIONPRESSURERATE_317_FIELD_EQUALIZATIONPRESSURERATE_17 = 0x00400002;
    public static final int MSG_RESIDUALOFDYNAMICANALYSIS_318 = 0x00000020;
    public static final int MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_RESIDUALOFDYNAMICANALYSIS_18 = 0x00000002;
    public static final int MSG_PARTNUMBER_330 = 0x00000024;
    public static final int MSG_PARTNUMBER_330_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_PARTNUMBER_330_FIELD_PARTNUMBER_30 = 0x01400002;
    public static final int MSG_VALUEFAULT_FALSE_340 = 0x00000028;
    public static final int MSG_VALUEFAULT_FALSE_340_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_VALUEFAULT_TRUE_341 = 0x0000002b;
    public static final int MSG_VALUEFAULT_TRUE_341_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_PRESSUREFAULT_LOW_350 = 0x0000002e;
    public static final int MSG_PRESSUREFAULT_LOW_350_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_PRESSUREFAULT_NONE_351 = 0x00000031;
    public static final int MSG_PRESSUREFAULT_NONE_351_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_PRESSUREFAULT_HIGH_352 = 0x00000034;
    public static final int MSG_PRESSUREFAULT_HIGH_352_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_LEAKFAULT_FALSE_360 = 0x00000037;
    public static final int MSG_LEAKFAULT_FALSE_360_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_LEAKFAULT_TRUE_361 = 0x0000003a;
    public static final int MSG_LEAKFAULT_TRUE_361_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_PRESSUREPOINT_319 = 0x0000003d;
    public static final int MSG_PRESSUREPOINT_319_FIELD_STATION_1 = 0x00000001;
    public static final int MSG_PRESSUREPOINT_319_FIELD_PRESSUREPOINT_19 = 0x00000002;
    
    
    protected ValveSchema() {
        super(FROM);
    }
        
}
