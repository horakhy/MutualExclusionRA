package br.com.mutualExclusionRA;

import java.util.*;

import br.com.mutualExclusionRA.enums.StateTypes;

public class AlgorithmRA implements Runnable {
	private ArrayList<Process> processList;
	private ArrayList<Process> processQueueSC;

	public AlgorithmRA(ArrayList<br.com.mutualExclusionRA.Process> processQueue) {
		this.processList = processQueue;
	}

	public void requestCriticalSection(Process requestingProcess) {
//		if(processQueue. (process => process.getStateTypes === StateTypes.HELD));
		// Verifica se já existe algum processo na seção crítica
		for (Process p : processList) {
			if (p.getStateSC() == StateTypes.HELD) {
				System.out.println("Outro processo está na seção crítica");
				System.out.println("O " + requestingProcess.id + " será o próximo");
				if (requestingProcess.getStateSC() == StateTypes.WANTED)
					this.processQueueSC.add(requestingProcess);
				return;
			}
		}

		int time = requestingProcess.getSenderTime();
		requestingProcess.setStateSC(StateTypes.HELD);

		try {
			Thread.sleep(time);
			requestingProcess.setStateSC(StateTypes.RELEASED);
			System.out.println(requestingProcess.id + " está na seção crítica");
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
					if (p.getStateSC() != StateTypes.WANTED)
						p.setStateSC(new Random().nextBoolean() ? StateTypes.WANTED : StateTypes.RELEASED);
				}

			} else {
				// Caso a fila não exista ainda
				for (Process p : processList) {
					requestCriticalSection(p);
					p.setStateSC(new Random().nextBoolean() ? StateTypes.WANTED : StateTypes.RELEASED);
				}
			}
		}

		// System.out.println("Fim da Execução");
	}
}
