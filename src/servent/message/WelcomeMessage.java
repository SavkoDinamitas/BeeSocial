package servent.message;

import java.util.Map;
import java.util.Set;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private Map<Integer, Set<String>> values;
	private Map<Integer, Set<Integer>> followers;
	private Map<Integer, Set<Integer>> requests;
	
	public WelcomeMessage(int senderPort, int receiverPort, Map<Integer, Set<String>> values, Map<Integer, Set<Integer>> followers, Map<Integer, Set<Integer>> pendingRequests) {
		super(MessageType.WELCOME, senderPort, receiverPort);
		this.requests = pendingRequests;
		this.followers = followers;
		this.values = values;
	}
	
	public Map<Integer, Set<String>> getValues() {
		return values;
	}

	public Map<Integer, Set<Integer>> getFollowers() {
		return followers;
	}

	public Map<Integer, Set<Integer>> getRequests() {
		return requests;
	}
}
