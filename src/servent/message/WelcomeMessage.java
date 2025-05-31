package servent.message;

import java.util.Map;
import java.util.Set;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private Map<Integer, Set<String>> values;
	
	public WelcomeMessage(int senderPort, int receiverPort, Map<Integer, Set<String>> values) {
		super(MessageType.WELCOME, senderPort, receiverPort);
		
		this.values = values;
	}
	
	public Map<Integer, Set<String>> getValues() {
		return values;
	}
}
