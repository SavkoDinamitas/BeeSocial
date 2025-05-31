package servent.message;

import java.io.Serial;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ReplicateMessage extends BasicMessage{
    @Serial
    private static final long serialVersionUID = -6788130009601616936L;

    private Set<String> uploadedFiles;
    private boolean forwarded;
    private int replicaId;

    public ReplicateMessage(int senderPort, int receiverPort, Set<String> uploadedFiles, boolean forwarded, int replicaId) {
        super(MessageType.REPLICATE, senderPort, receiverPort);
        this.uploadedFiles = Collections.synchronizedSet(new HashSet<>(uploadedFiles));
        this.forwarded = forwarded;
        this.replicaId = replicaId;
    }

    public Set<String> getUploadedFiles() {
        return uploadedFiles;
    }

    public boolean isForwarded() {
        return forwarded;
    }

    public int getReplicaId() {
        return replicaId;
    }
}
