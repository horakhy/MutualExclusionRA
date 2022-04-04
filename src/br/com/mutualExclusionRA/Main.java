package br.com.mutualExclusionRA;

import java.util.ArrayList;

import br.com.mutualExclusionRA.enums.StateType;

public class Main {

	public static void main(String[] args) {
		// 3 processos que irão requisitar acesso a seção crítica
		Process p1 = new Process("Processo 1" , 1, 1000, StateType.RELEASED);
		Process p2 = new Process("Processo 2" , 2, 2000, StateType.RELEASED);
		Process p3 = new Process("Processo 3" , 3, 3000, StateType.RELEASED);
		
		ArrayList<Process> processQueue = new ArrayList<Process>();
		processQueue.add(p1);
		processQueue.add(p2);
		processQueue.add(p3);
		
		AlgorithmRA RA = new AlgorithmRA(processQueue);
		
		Thread t1 = new Thread(RA);
		
		t1.start();

	}

}
