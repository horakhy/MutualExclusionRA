package br.com.mutualExclusionRA;

import java.util.*;
import java.lang.*;

public class AlgorithmRA implements Runnable {
	private ArrayList<Process> processList;
	private ArrayList<Process> processQueueSC;

	public AlgorithmRA(ArrayList<Process> processQueue) {
		this.processList = processQueue;
	}

	public void requestCriticalSection(Process requestingProcess) {
//		if(processQueue. (process => process.getStateType === StateType.HELD));
		// Verifica se já existe algum processo na seção crítica
		for (Process p : processList) {
			if (p.getStateSC() == StateType.HELD) {
				System.out.println("Outro processo está na seção crítica");
				System.out.println("O " + requestingProcess.name + " será o próximo");
				if (requestingProcess.getStateSC() == StateType.WANTED)
					this.processQueueSC.add(requestingProcess);
				return;
			}
		}

		int time = requestingProcess.getSenderTime();
		requestingProcess.setStateSC(StateType.HELD);

		try {
			Thread.sleep(time);
			requestingProcess.setStateSC(StateType.RELEASED);
			System.out.println(requestingProcess.name + " está na seção crítica");
			System.out.println("delay de " + time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			// Fila de prioridades
			if (processQueueSC != null) {
				for (Process p : processQueueSC) {
					System.out.println("Entrei aqui menóó");
					requestCriticalSection(p);
					// Randomizar o estado para acesso a seção crítica
					if (p.getStateSC() != StateType.WANTED)
						p.setStateSC(new Random().nextBoolean() ? StateType.WANTED : StateType.RELEASED);
				}

			} else {
				// Caso a fila não exista ainda
				for (Process p : processList) {
					requestCriticalSection(p);
					p.setStateSC(new Random().nextBoolean() ? StateType.WANTED : StateType.RELEASED);
				}
			}
		}

		// System.out.println("Fim da Execução");
	}
}
