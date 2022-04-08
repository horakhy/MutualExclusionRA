package br.com.mutualExclusionRA;

import java.io.*;

import br.com.mutualExclusionRA.enums.MessageTypes;
import br.com.mutualExclusionRA.enums.StateTypes;


public class Message implements Serializable{

	private String name;
	private Long timestamp;
	private MessageTypes messageTypes;

	// updated if the message is a permission message
	private StateTypes resourceState;

	// updated if the message is a permission message
	private Boolean init;

	public String getName() {
		return name;
	}

	public void setName(String _name) {
		this.name = _name;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long _timestamp) {
		this.timestamp = _timestamp;
	}

	public MessageTypes getMessageType() {
		return messageTypes;
	}

	public void setMessageType(MessageTypes _messageTypes) {
		this.messageTypes = _messageTypes;
	}

	public StateTypes getResourceState() {
		return resourceState;
	}

	public void setResourceState(StateTypes _resourceState) {
		this.resourceState = _resourceState;
	}

	public Boolean getInit() {
		return init;
	}

	public void setInit(Boolean _init) {
		this.init = _init;
	}

	// RELEASE MESSAGE OR REQUEST MESSAGE OR ENTRY MESSAGE OR EXIT
	public Message(MessageTypes messageTypes, String name){
		if (name == null || name.isEmpty()) {
			System.out.println("REQUIRES A SENDER NAME");
		}

		if (!messageTypes.equals(MessageTypes.REQUEST)
				&& !messageTypes.equals(MessageTypes.RELEASE)
				&& !messageTypes.equals(MessageTypes.ENTRY)
				&& !messageTypes.equals(MessageTypes.EXIT)) {
			System.out.println("REQUIRES A VALID MESSAGE TYPE");
		}

		this.timestamp = System.currentTimeMillis();
		this.messageTypes = messageTypes;
		this.name = name;
	}

	// ENTRY RESPONSE
	public Message(MessageTypes messageTypes, Boolean init, String name){
		if (name == null || name.isEmpty()) {
			System.out.println("REQUIRES A SENDER NAME");
		}

		if (!messageTypes.equals(MessageTypes.ENTRY_RESPONSE)) {
			System.out.println("REQUIRES A VALID MESSAGE TYPE");
		}

		if (init == null) {
			System.out.println("REQUIRES A VALID INIT");
		}

		this.timestamp = System.currentTimeMillis();
		this.messageTypes = messageTypes;
		this.name = name;
		this.init = init;
	}
	// REQUEST RESPONSE
	public Message(MessageTypes messageTypes, StateTypes resourceState, String name) {
		if (name == null || name.isEmpty()) {
			System.out.println("REQUIRES A SENDER NAME");
		}

		if (!messageTypes.equals(MessageTypes.REQUEST_RESPONSE)) {
			System.out.println("REQUIRES A VALID MESSAGE TYPE");
		}

		if (resourceState == null) {
			System.out.println("REQUIRES A VALID RESOURCE STATE");
		}

		this.timestamp = System.currentTimeMillis();
		this.messageTypes = messageTypes;
		this.resourceState = resourceState;
		this.name = name;
	}

	// MESSAGE TO BYTES
	public static byte[] toBytes(Message mensagem) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(mensagem);
			out.flush();
			return bos.toByteArray();
		} finally {
			try {
				bos.close();
			} catch (Exception e) {
				// Ignores
			}
		}
	}

	// BYTES TO MESSAGE
	public static Message fromBytes(byte[] bytes) throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
			return (Message) in.readObject();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				// Ignores
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("\t> Message: ");

		switch (this.getMessageType()) {
		case ENTRY:
			sb.append(this.getName() + " joined the group");
			break;
		case ENTRY_RESPONSE:
			sb.append(this.getName() + " replied the entry");
			break;
		case EXIT:
			sb.append(this.getName() + " leaved the group");
			break;
		case REQUEST:
			sb.append(this.getName() + " is requesting the resource");
			break;
		case REQUEST_RESPONSE:
			sb.append(this.getName() + " informs that the resource is in the state " + this.getResourceState().toString());
			break;
		case RELEASE:
			sb.append(this.getName() + " released the resource ");
			break;
		default:
			break;
		}

		return sb.toString();
	}
}