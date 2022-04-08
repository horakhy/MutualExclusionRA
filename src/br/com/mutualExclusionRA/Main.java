package br.com.mutualExclusionRA;

import java.util.Scanner;

import br.com.mutualExclusionRA.enums.StateTypes;

public class Main {

	public static void main(String[] args) throws Exception {

		// Process process_U = new Process("1", 0, 0, StateTypes.RELEASED);
		// process_U.start();
		// Process process_D = new Process("2", 0, 0, StateTypes.RELEASED);
		// process_D.start();
		// Process process_T = new Process("3", 0, 0, StateTypes.RELEASED);
		// process_T.start();

		System.out.println("Initializing process...");
		Process process = null;
		Process processo2 = null;
		// Process processo3 = null;

		try {
			process = new Process("1", StateTypes.RELEASED);
			process.start();
			// processo2 = new Process("2", StateTypes.RELEASED);
			// processo2.start();
			// processo3 = new Process("3", StateTypes.RELEASED);
			// processo3.start();
		} catch (Exception e) {
			System.out.println("Process Create Error");
			e.printStackTrace();
		}
		System.out.println(process);
		Scanner scanner = new Scanner(System.in);
		while (true) {

			int opt = scanner.nextInt();

			if (process.getInit() || opt == 5 || opt == 6) {
				switch (opt) {
				case 1:
					System.out.println();
					System.out.println("Requesting resource...");
					System.out.println();
					process.requestResource();
					break;
				case 2:
					System.out.println();
					System.out.println("Requisitando recurso...");
					System.out.println();
					process.requestResource();
					break;
				case 3:
					System.out.println();
					System.out.println("Releasing resource 1...");
					System.out.println();
					process.releaseResource(Boolean.TRUE);
					break;
				case 4:
					System.out.println();
					System.out.println("Releasing resource 2...");
					System.out.println();
					process.releaseResource(Boolean.TRUE);
					break;
				case 5:
					process.printConnectedPeers();
					break;
				case 6:
					System.out.println();
					System.out.println("Leaving program...");
					System.out.println();
					process.releaseResource(Boolean.FALSE);
					process.leaveMessage();
					scanner.close();
					System.exit(0);
					break;
				default:
					System.out.println("Invalid choice!");
					break;
				}
			} else {
				System.out.println("No mínimo 3 peers devem se conectar para iniciar o processo");
			}

		}
		// System.out.println("Selecione uma opção abaixo:");
		// System.out.println();
		// System.out.println("+---+------------------------+");
		// System.out.println("+ 1 + P1 - Acessar recurso   +");
		// System.out.println("+---+------------------------+");
		// System.out.println("+ 2 + P2 - Acessar recurso   +");
		// System.out.println("+---+------------------------+");
		// System.out.println("+ 3 + P3 - Acessar recurso   +");
		// System.out.println("+---+------------------------+");
		// System.out.println("+ 1 + P1 - Liberar recurso   +");
		// System.out.println("+---+------------------------+");
		// System.out.println("+ 2 + P2 - Liberar recurso   +");
		// System.out.println("+---+------------------------+");
		// System.out.println("+ 3 + P3 - Liberar recurso   +");
		// System.out.println("+---+------------------------+");
		// System.out.println("+ 7 +         Sair           +");
		// System.out.println("+---+------------------------+");
		// System.out.println();

		// Scanner scanner = new Scanner(System.in);
		// while (true) {

		// 	int opt = scanner.nextInt();
		// 	ArrayList<Process> processQueue = new ArrayList<Process>();
			
		// 	switch(opt) {
		// 		case 1:
		// 			System.out.println("P1 - Acessar recurso");
		// 			Process p1 = new Process("Processo 1" , 1, 1000, StateTypes.RELEASED);
		// 			processQueue.add(p1);
		// 			System.out.println();
		// 			break;
		// 		case 2:
		// 			System.out.println("P2 - Acessar recurso");
		// 			Process p2 = new Process("Processo 2" , 2, 2000, StateTypes.RELEASED);
		// 			processQueue.add(p2);
		// 			System.out.println();
		// 			break;
		// 		case 3:
		// 			System.out.println("P3 - Acessar recurso");
		// 			Process p3 = new Process("Processo 3" , 3, 3000, StateTypes.RELEASED);
		// 			processQueue.add(p3);
		// 			System.out.println();
		// 			break;
		// 		case 4:
		// 			System.out.println("Liberar o recurso 1");
		// 			System.out.println();
		// 			break;
		// 		case 5:
		// 			System.out.println("Liberar o recurso 2");
		// 			System.out.println();
		// 			break;
		// 		case 6:
		// 			System.out.println("Listar Peers");
		// 			System.out.println();
		// 			break;
		// 		case 7:
		// 			System.out.println("Sair");
		// 			System.out.println();
		// 			break;
		// 		default:
		// 			System.out.println("Opção inválida");
		// 			System.out.println();
		// 			break;
		// 	}
		// 	if(opt == 1 || opt == 2 || opt == 3){
		// 		AlgorithmRA RA = new AlgorithmRA(processQueue);
		// 		Thread t1 = new Thread(RA);
		// 		t1.start();
		// 	}
		// scanner.close();
		// }
	}
}
