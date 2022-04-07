package br.com.mutualExclusionRA;

import java.util.Scanner;

import br.com.mutualExclusionRA.enums.StateTypes;

public class Main {

	public static void main(String[] args) throws Exception {

		Process process_U = new Process("1", 0, 0, StateTypes.RELEASED);
		process_U.start();
		Process process_D = new Process("2", 0, 0, StateTypes.RELEASED);
		process_D.start();
		Process process_T = new Process("3", 0, 0, StateTypes.RELEASED);
		process_T.start();

		System.out.println("Processos iniciados");
		Process processo = null;
		try {
			processo = new Process("1", 0, 1000, StateTypes.RELEASED);
			processo.start();
		} catch (Exception e) {
			System.out.println("Erro ao criar processo");
			e.printStackTrace();
		}
		System.out.println(processo);
		Scanner scanner = new Scanner(System.in);
		while (true) {

			int opcao = scanner.nextInt();

			if (processo.getInit() || opcao == 5 || opcao == 6) {
				switch (opcao) {
				case 1:
					System.out.println();
					System.out.println("Requisitando recurso 1...");
					System.out.println();
					processo.requisitarRecurso();
					break;
				case 2:
					System.out.println();
					System.out.println("Requisitando recurso 2...");
					System.out.println();
					processo.requisitarRecurso();
					break;
				case 3:
					System.out.println();
					System.out.println("Liberando recurso 1...");
					System.out.println();
					processo.liberarRecurso(Boolean.TRUE);
					break;
				case 4:
					System.out.println();
					System.out.println("Liberando recurso 2...");
					System.out.println();
					processo.liberarRecurso(Boolean.TRUE);
					break;
				case 5:
					// processo.imprimirPeersConectados();
					break;
				case 6:
					System.out.println();
					System.out.println("Saindo do programa...");
					System.out.println();
					processo.liberarRecurso(Boolean.FALSE);
					processo.liberarRecurso(Boolean.FALSE);
					processo.anunciarSaida();
					scanner.close();
					System.exit(0);
					break;
				default:
					System.out.println("Opção inválida!");
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
