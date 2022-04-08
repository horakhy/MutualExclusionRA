package br.com.mutualExclusionRA;

import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

import br.com.mutualExclusionRA.enums.MessageTypes;
import br.com.mutualExclusionRA.enums.StateTypes;

public class Process extends Thread {
	public String id;
	private StateTypes stateSC;
	private MulticastSocket socket;
	private StateTypes resourceState;
	private MulticastSocket socketResponse;
	private Map<String, byte[]> waitingList;
	private Set<String> resourceWaitingList;
	private Boolean init;
	private InetAddress group;

	public Process(String id, StateTypes stateSC) {
		this.id = id;
		this.stateSC = stateSC;
		this.resourceWaitingList = new HashSet<String>();
		this.init = false;
		this.waitingList = new HashMap<String, byte[]>();
		this.init = false;
	}

	public void setStateSC(StateTypes st) {
		this.stateSC = st;
	}

	public StateTypes getStateSC() {
		return this.stateSC;
	}

	public void run() {
		try {
			this.connect();

			this.entryResponse();

			while (true) {
				this.receiveMessage();
			}
		} catch (Exception e) {
			System.out.println();
			System.out.println("Connection Failed");
			System.out.println();
			e.printStackTrace();
		} finally {
			if (this.socket != null) {
				this.socket.close();
			}
		}
	}

	private void connect() {
		try {
			this.group = InetAddress.getByName("232.232.232");
			this.socket = new MulticastSocket(6789);
			this.socket.joinGroup(group);

			this.socketResponse = new MulticastSocket(6789);
			this.socketResponse.joinGroup(group);
			this.socketResponse.setSoTimeout(3000);

			System.out.println();
			System.out.println("---" + "Connected to " + "232.232.232" + ":"
					+ "6789");
			System.out.println();
		} catch (Exception e) {
			System.out.println("Group Connection Failed");
		}
	}
	
	private void disconnect() {
		try {
			this.socket.leaveGroup(group);
			this.socket.disconnect();
		} catch (Exception e) {
			System.out.println("Group Disconnection Failed");
		}
	}

	public void requestResource() throws Exception {
		// Atualiza a situação do recurso
		if (this.resourceState != null && this.resourceState != StateTypes.RELEASED) {
			System.out.println();
			System.out.println("The resource has been already requested");
			System.out.println();
			return;
		}

		// Atualizando a situação do recurso para WANTED
		this.resourceState = StateTypes.WANTED;

		// Envia uma mensagem de requisição do recurso
		Message request = new Message(MessageTypes.REQUEST, this.id);
		this.sendMessage(request);

		Long timestampInicio = System.currentTimeMillis();

		Set<Message> requestsResponses = new HashSet<Message>();
		while (requestsResponses.size() != this.waitingList.size()) { // qtde de peers 3
			try {
				requestsResponses.add(this.receiveResponseRequest(timestampInicio));
			} catch (SocketTimeoutException timeoutException) {
				// Pelo menos um peer esperado não respondeu a tempo
				Set<String> answeredP = requestsResponses.stream()
						.map(requestResponse -> requestResponse.getName()).collect(Collectors.toSet());

				Map<String, byte[]> remainingPeers = new HashMap<String, byte[]>();

				answeredP.stream()
						.forEach(peer -> remainingPeers.put(peer, this.waitingList.get(peer)));

				Set<String> removedPeers = this.waitingList.keySet();
				removedPeers.removeAll(answeredP);

				System.out.println("Removed Peers: " + removedPeers.toString());

				this.waitingList = remainingPeers;

				System.out.println();
			}
		}
		// Neste ponto, todos os peers já responderam ou foram removidos
		Set<String> newWaitingList = requestsResponses.stream().filter(
				requestResponse -> !requestResponse.getResourceState().equals(StateTypes.RELEASED))
				.map(requestResponse -> requestResponse.getName()).collect(Collectors.toSet());

		if (newWaitingList.size() == 0) {
			// Todos peers responderam que o recurso está RELEASED
			this.resourceState = StateTypes.HELD;
			System.out.println();
			System.out.println("Resource Acess Released");
			System.out.println();
		} else {
			// Atualiza a lista de espera pelo recurso
			this.resourceWaitingList = newWaitingList;
			System.out.println();
			System.out.println("Waiting peers release"
					+ newWaitingList.toString());
			System.out.println();
		}
	}

	public void releaseResource(Boolean errorNotify) throws Exception {
		if (this.resourceState == null) {
			System.out.println();
			System.out.println("The resource has not been requested");
			System.out.println();
			return;
		}
		StateTypes resourceState = this.resourceState;
		if (!resourceState.equals(StateTypes.HELD)) {
			if (Boolean.TRUE.equals(errorNotify)) {
				System.out.println();
				System.out.println("Resource state is " + resourceState);
				System.out.println();
			}
			return;
		}

		this.resourceState = StateTypes.RELEASED;

		Message release = new Message(MessageTypes.RELEASE, this.id);
		this.sendMessage(release);
	}

	private void sendMessage(Message message) throws Exception {
		byte[] messageBytes = Message.toBytes(message);
		try {
			DatagramPacket messageOut = new DatagramPacket(messageBytes, messageBytes.length, this.group, 6789);
			this.socket.send(messageOut);
		} catch (Exception e) {
			throw new Exception("Failed to send message");
		}
	}

	private void receiveMessage() throws Exception {
		try {
			byte[] buffer = new byte[1000];
			Message message;
			DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);

			this.socket.receive(messageIn);

			message = Message.fromBytes(buffer);

			System.out.println(message.toString());

			this.manageMessage(message);

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to send message");
		}
	}

	// anuncia a saída do peer
	public void leaveMessage() throws Exception {
		Message exit = new Message(MessageTypes.EXIT, this.id);
		this.sendMessage(exit);

		this.disconnect();
	}
	
	private Message receiveResponseRequest(Long timestampInicio) throws Exception {
		try {
			byte[] buffer = new byte[1000];
			Message message;
			DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);

			this.socketResponse.receive(messageIn);

			message = Message.fromBytes(buffer);

			// Limpa mensagens estão no buffer e não interessam
			if (!message.getMessageType().equals(MessageTypes.REQUEST_RESPONSE)
					|| timestampInicio > message.getTimestamp()) {
				return this.receiveResponseRequest(timestampInicio);
			}

			return message;
		} catch (SocketTimeoutException timeoutException) {
			throw new SocketTimeoutException();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to receive message");
		}
	}

	public void printConnectedPeers() {
		System.out.println();
		System.out.println(
				"Connected Peers: " + this.resourceWaitingList.toString());
		System.out.println();
	}

	private void manageMessage(Message mensagem) throws Exception {
		switch (mensagem.getMessageType()) {
			case ENTRY:
				mensagem.setInit(true);
				Message entryResponse = new Message(MessageTypes.ENTRY_RESPONSE, true, this.id);
				this.sendMessage(entryResponse);
				break;
			case ENTRY_RESPONSE:
				// Atualiza o estado (inicializado ou não)
				this.init = mensagem.getInit();
				break;
			case REQUEST:
				// Responde à requisição de recursos
				Message requestsResponses = new Message(MessageTypes.REQUEST_RESPONSE, this.resourceState, this.id);
				this.sendMessage(requestsResponses);
				break;
			case RELEASE:
				// Atualiza a lista de espera do recurso, caso esteja interessado
				StateTypes resourceReleasedState = this.resourceState;

				if (resourceReleasedState.equals(StateTypes.WANTED)) {
					Set<String> resourceReleasedStateList = this.resourceWaitingList;
					resourceReleasedStateList.remove(mensagem.getName());

					if (resourceReleasedStateList.size() == 0) {
						this.resourceState = StateTypes.HELD;
						System.out.println();
						System.out.println(
								"Resource Access Granted");
						System.out.println();
					} else {
						System.out.println();
						System.out.println("Waiting to peers release "
								+ resourceReleasedStateList.toString());
						System.out.println();
					}
				}

			default:
				break;
		}
	}

	private void entryResponse() throws Exception {
		Message entryBroadcast = new Message(MessageTypes.ENTRY, this.id);
		this.sendMessage(entryBroadcast);
	}

	public Boolean getInit() {
		return init;
	}
}