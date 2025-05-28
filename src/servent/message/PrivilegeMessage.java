package servent.message;

import java.io.Serial;
import java.util.Map;
import java.util.Queue;

public class PrivilegeMessage extends BasicMessage{
    @Serial
    private static final long serialVersionUID = -6421873529925675324L;
    Queue<Integer> queue;
    Map<Integer, Integer> ln;
    int privilegeReciever;
    public PrivilegeMessage(int senderPort, int receiverPort, Queue<Integer> queue, Map<Integer, Integer> ln, int privilegeReciever) {
        super(MessageType.PRIVILEGE, senderPort, receiverPort);
        this.queue = queue;
        this.ln = ln;
        this.privilegeReciever = privilegeReciever;
    }

    public Queue<Integer> getQueue() {
        return queue;
    }

    public Map<Integer, Integer> getLn() {
        return ln;
    }

    public int getPrivilegeReciever() {
        return privilegeReciever;
    }
}
