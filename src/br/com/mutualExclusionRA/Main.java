package br.com.mutualExclusionRA;

import java.util.ArrayList;
import java.util.Scanner;

import br.com.mutualExclusionRA.enums.StateType;

public class Main {

	public static void main(String[] args) {

		System.out.println("Selecione uma opção abaixo:");
		System.out.println();
		System.out.println("+---+------------------------+");
		System.out.println("+ 1 + P1 - Acessar recurso   +");
		System.out.println("+---+------------------------+");
		System.out.println("+ 2 + P2 - Acessar recurso   +");
		System.out.println("+---+------------------------+");
		System.out.println("+ 3 + P3 - Acessar recurso   +");
		System.out.println("+---+------------------------+");
		System.out.println("+ 4 + Liberar o recurso 1    +");
		System.out.println("+---+------------------------+");
		System.out.println("+ 5 + Liberar o recurso 2    +");
		System.out.println("+---+------------------------+");
		System.out.println("+ 6 +     Listar Peers       +");
		System.out.println("+---+------------------------+");
		System.out.println("+ 7 +         Sair           +");
		System.out.println("+---+------------------------+");
		System.out.println();

		Scanner scanner = new Scanner(System.in);
		while (true) {

			int opt = scanner.nextInt();
			ArrayList<Process> processQueue = new ArrayList<Process>();
			
			switch(opt) {
				case 1:
					System.out.println("P1 - Acessar recurso");
					Process p1 = new Process("Processo 1" , 1, 1000, StateType.RELEASED);
					processQueue.add(p1);
					System.out.println();
					break;
				case 2:
					System.out.println("P2 - Acessar recurso");
					Process p2 = new Process("Processo 2" , 2, 2000, StateType.RELEASED);
					processQueue.add(p2);
					System.out.println();
					break;
				case 3:
					System.out.println("P3 - Acessar recurso");
					Process p3 = new Process("Processo 3" , 3, 3000, StateType.RELEASED);
					processQueue.add(p3);
					System.out.println();
					break;
				case 4:
					System.out.println("Liberar o recurso 1");
					System.out.println();
					break;
				case 5:
					System.out.println("Liberar o recurso 2");
					System.out.println();
					break;
				case 6:
					System.out.println("Listar Peers");
					System.out.println();
					break;
				case 7:
					System.out.println("Sair");
					System.out.println();
					break;
				default:
					System.out.println("Opção inválida");
					System.out.println();
					break;
			}
			if(opt == 1 || opt == 2 || opt == 3)
				AlgorithmRA RA = new AlgorithmRA(processQueue);
				Thread t1 = new Thread(RA);
				t1.start();
		}
	}
}
