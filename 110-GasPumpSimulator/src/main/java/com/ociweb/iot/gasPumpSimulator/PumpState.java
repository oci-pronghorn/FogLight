package com.ociweb.iot.gasPumpSimulator;

public enum PumpState {
	
	Idle(0,""), //will show tank value
	PumpUnleaded(251,"Unleaded"),
	PumpPlus(275,"Unleaded Plus"),
	PumpPremium(301,"Premium Unleaded"),
	Receipt(0,"");
	
	public int centsPerGallon;
	public final String fuelName;
		
	private PumpState(int centsPerGallon, String fuelName) {
		this.centsPerGallon = centsPerGallon;
		this.fuelName = fuelName;
	}
}
