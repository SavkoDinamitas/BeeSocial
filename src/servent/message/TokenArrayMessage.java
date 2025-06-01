package servent.message;

import java.util.Map;

public class TokenArrayMessage extends BasicMessage{
    private final Map<Integer, Integer> ln, rn;

    public TokenArrayMessage(int senderPort, int receiverPort, Map<Integer, Integer> ln, Map<Integer, Integer> rn) {
        super(MessageType.GATHER, senderPort, receiverPort);
        this.ln = ln;
        this.rn = rn;
    }

    public Map<Integer, Integer> getLn() {
        return ln;
    }

    public Map<Integer, Integer> getRn() {
        return rn;
    }
}
