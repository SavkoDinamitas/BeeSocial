package servent.message;

import java.io.Serial;
import java.io.Serializable;

public class FollowMessage extends BasicMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = -3914573374587004266L;

    private final int followPort;

    public FollowMessage(int senderPort, int receiverPort, int followPort) {
        super(MessageType.FOLLOW, senderPort, receiverPort);
        this.followPort = followPort;
    }

    public int getFollowPort() {
        return followPort;
    }
}
