package br.com.mutualExclusionRA;

public class Process {
	public String name;
	private int senderIdentifier;
	private int senderTime;
	private StateType stateSC;
	
	public Process(String name ,int senderIdentifier, int senderTime, StateType stateSC) {
		this.name = name;
		this.senderIdentifier = senderIdentifier;
		this.senderTime = senderTime;
		this.stateSC = stateSC;
	}
	public void setStateSC(StateType st) {
		this.stateSC = st;
	}
	public StateType getStateSC() {
		return this.stateSC;
	}
	public int getSenderTime() {
		return this.senderTime;
	}
	public int getSenderIdentifier() {
		return this.senderIdentifier;
	}
}
