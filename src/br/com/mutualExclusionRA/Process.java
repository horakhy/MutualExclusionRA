package br.com.mutualExclusionRA;

import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

import br.com.mutualExclusionRA.enums.MessageTypes;

// import constants.DisplayConstants;
// import enums.EnumMessageType;
// import enums.EnumResourceId;
// import enums.EnumResourceStatus;
// import utils.RSAUtils;

import br.com.mutualExclusionRA.enums.StateTypes;

public class Process extends Thread {
	public String id;
	private int senderIdentifier;
	private int senderTime;
	private StateTypes stateSC;
	private MulticastSocket socket;
	private StateTypes resourceState;
	private MulticastSocket socketResponse;
	private Set<String> waitingList;
	// private HashSet<String> listaChavesPublicas;
	private Boolean init;
	private InetAddress group;

	public Process(String id, int senderIdentifier, int senderTime, StateTypes stateSC) {
		this.id = id;
		this.senderIdentifier = senderIdentifier;
		this.senderTime = senderTime;
		this.stateSC = stateSC;
		// this.listaChavesPublicas = new HashSet<String>();
		this.init = false;
	}

	public void setStateSC(StateTypes st) {
		this.stateSC = st;
	}

	public StateTypes getStateSC() {
		return this.stateSC;
	}

	public int getSenderTime() {
		return this.senderTime;
	}

	public int getSenderIdentifier() {
		return this.senderIdentifier;
	}

	public void run() {
		try {
			// Conecta-se ao multicast
			this.connect();

			// Anuncia sua entrada
			this.entryResponse();

			// Espera por mensagens
			while (true) {
				this.receiveMessage();
			}
		} catch (Exception e) {
			System.out.println();
			System.out.println("Conexão encerrada");
			System.out.println();
			e.printStackTrace();
		} finally {
			if (this.socket != null) {
				this.socket.close();
			}
		}
	}

	private void connect() throws Exception {
		try {
			this.group = InetAddress.getByName("232.232.232");
			this.socket = new MulticastSocket(6789);
			this.socket.joinGroup(group);

			this.socketResponse = new MulticastSocket(6789);
			this.socketResponse.joinGroup(group);
			this.socketResponse.setSoTimeout(3000);

			System.out.println();
			System.out.println("---" + "Conectado a " + "232.232.232" + ":"
					+ "6789");
			System.out.println();
		} catch (Exception e) {
			throw new Exception("Erro ao connect-se ao grupo");
		}
	}

	/**
	 * Desconecta-se do grupo
	 * 
	 * @throws Exception
	 */
	private void disconnect() throws Exception {
		try {
			this.socket.leaveGroup(group);
			this.socket.disconnect();
		} catch (Exception e) {
			throw new Exception("Erro ao disconectar-se do grupo");
		}
	}

	public void requisitarRecurso() throws Exception {
		// Atualiza a situação do recurso
		if (this.resourceState != StateTypes.RELEASED) {
			System.out.println();
			System.out.println("O Recurso já foi requisitado");
			System.out.println();
			return;
		}

		// Atualizando a situação do recurso para WANTED
		this.resourceState = StateTypes.WANTED;

		// Envia uma mensagem de requisição do recurso
		Message request = new Message(MessageTypes.REQUEST, this.id);
		this.enviarMensagem(request);

		Long timestampInicio = System.currentTimeMillis();

		Set<Message> respostasRequisicoes = new HashSet<Message>();
		while (respostasRequisicoes.size() != 3) { // qtde de peers 3
			try {
				respostasRequisicoes.add(this.receberRespostaRequisicao(timestampInicio));
			} catch (SocketTimeoutException timeoutException) {
				// Pelo menos um peer esperado não respondeu a tempo
				// Set<String> peersQueResponderam = respostasRequisicoes.stream()
				// 		.map(respostaRequisicao -> respostaRequisicao.getName()).collect(Collectors.toSet());

				// Map<String, byte[]> peersQuePermanecem = new HashMap<String, byte[]>();
				System.out.println();
 			}
		}
		// Neste ponto, todos os peers já responderam ou foram removidos
		Set<String> novaListaEspera = respostasRequisicoes.stream().filter(
				respostaRequisicao -> !respostaRequisicao.getResourceState().equals(StateTypes.RELEASED))
				.map(respostaRequisicao -> respostaRequisicao.getName()).collect(Collectors.toSet());

		if (novaListaEspera.size() == 0) {
			// Todos peers responderam que o recurso está RELEASED
			this.resourceState = StateTypes.HELD;
			System.out.println();
			System.out.println("Acesso liberado ao Recurso");
			System.out.println();
		} else {
			// Atualiza a lista de espera pelo recurso
			this.waitingList = novaListaEspera;
			System.out.println();
			System.out.println("Esperando pela liberação dos peers"
					+ novaListaEspera.toString());
			System.out.println();
		}
	}

	public void liberarRecurso(Boolean notificarErro) throws Exception {
		StateTypes situacaoRecurso = this.resourceState;
		if (!situacaoRecurso.equals(StateTypes.HELD)) {
			if (Boolean.TRUE.equals(notificarErro)) {
				System.out.println();
				System.out.println("O Recurso está em " + situacaoRecurso);
				System.out.println();
			}
			return;
		}

		this.resourceState = StateTypes.RELEASED;

		Message liberacao = new Message(MessageTypes.RELEASE, this.id);
		this.enviarMensagem(liberacao);
	}

	private void enviarMensagem(Message mensagem) throws Exception {
		byte[] mensagemBytes = Message.toBytes(mensagem);
		try {
			DatagramPacket messageOut = new DatagramPacket(mensagemBytes, mensagemBytes.length, this.group, 6789);
			this.socket.send(messageOut);
		} catch (Exception e) {
			throw new Exception("Erro ao enviar mensagem");
		}
	}

	private void receiveMessage() throws Exception {
		try {
			byte[] buffer = new byte[1000];
			Message mensagem;
			DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);

			this.socket.receive(messageIn);

			mensagem = Message.fromBytes(buffer);

			System.out.println(mensagem.toString());

			this.tratarMensagem(mensagem);

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Erro ao receber mensagem");
		}
	}

	public void anunciarSaida() throws Exception {
		Message exit = new Message(MessageTypes.EXIT, this.id);
		this.enviarMensagem(exit);

		this.disconnect();
	}

	private Message receberRespostaRequisicao(Long timestampInicio) throws Exception {
		try {
			byte[] buffer = new byte[1000];
			Message message;
			DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);

			this.socketResponse.receive(messageIn);

			message = Message.fromBytes(buffer);

			// Limpa mensagens estão no buffer e não interessam
			if (!message.getMessageType().equals(MessageTypes.REQUEST_RESPONSE)
					|| timestampInicio > message.getTimestamp()) {
				return this.receberRespostaRequisicao(timestampInicio);
			}

			return message;
		} catch (SocketTimeoutException timeoutException) {
			throw new SocketTimeoutException();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Erro ao receber mensagem");
		}
	}

	private void tratarMensagem(Message mensagem) throws Exception {
		switch (mensagem.getMessageType()) {
			case ENTRY:
				mensagem.setInit(true);
				Message entryResponse = new Message(MessageTypes.ENTRY_RESPONSE, true, this.id);
				this.enviarMensagem(entryResponse);
				break;
			case ENTRY_RESPONSE:
				// Atualiza o estado (inicializado ou não)
				this.init = mensagem.getInit();
				break;
			case REQUEST:
				// Responde à requisição de recursos
				Message requestResponse = new Message(MessageTypes.REQUEST_RESPONSE, this.resourceState, this.id);
				this.enviarMensagem(requestResponse);
				break;
			case RELEASE:
				// Atualiza a lista de espera do recurso, caso esteja interessado
				StateTypes situacaoRecursoLiberado = this.resourceState;

				if (situacaoRecursoLiberado.equals(StateTypes.WANTED)) {
					Set<String> listaEsperaRecursoLiberado = this.waitingList;
					listaEsperaRecursoLiberado.remove(mensagem.getName());

					if (listaEsperaRecursoLiberado.size() == 0) {
						this.resourceState = StateTypes.HELD;
						System.out.println();
						System.out.println(
								"Acesso liberado ao Recurso");
						System.out.println();
					} else {
						System.out.println();
						System.out.println("Esperando pela liberação dos peers "
								+ listaEsperaRecursoLiberado.toString());
						System.out.println();
					}
				}
			
			default:
				break;
		}
	}

	private void entryResponse() throws Exception {
		Message entryBroadcast = new Message(MessageTypes.ENTRY, this.id);
		this.enviarMensagem(entryBroadcast);
	}

	public Boolean getInit() {
		return init;
	}
}

/*
 * MulticastSocket s = null;
 * try {
 * InetAddress group = "233.233.233"
 * s = new MulticastSocket(6789);
 * s.joinGroup(group);
 * byte[] m = args[0].getBytes();
 * byte[] buffer = new byte[1000];
 * for (int i = 0; i < 3; i++) { // get messages from others in group
 * DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
 * s.receive(messageIn);
 * System.out.println("Received:" + new String(messageIn.getData()));
 * }
 * } catch (SocketException e) {
 * System.out.println("Socket: " + e.getMessage());
 * } catch (IOException e) {
 * System.out.println("IO: " + e.getMessage());
 * } finally {
 * if (s != null)
 * s.close();
 * }
 */