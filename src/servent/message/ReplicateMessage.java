package servent.message;

import java.io.Serial;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ReplicateMessage extends BasicMessage{
    @Serial
    private static final long serialVersionUID = -6788130009601616936L;

    private Set<String> uploadedFiles;
    private Set<Integer> pendingRequests;
    private Set<Integer> followers;
    private boolean forwarded;
    private int replicaId;

    public ReplicateMessage(int senderPort, int receiverPort, Set<String> uploadedFiles, Set<Integer> pendingRequests, Set<Integer> followers, boolean forwarded, int replicaId) {
        super(MessageType.REPLICATE, senderPort, receiverPort);
        if(uploadedFiles != null)
            this.uploadedFiles = Collections.synchronizedSet(new HashSet<>(uploadedFiles));
        else
            this.uploadedFiles = Collections.synchronizedSet(new HashSet<>());
        this.forwarded = forwarded;
        this.replicaId = replicaId;
        this.pendingRequests = pendingRequests;
        this.followers = followers;
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

    public Set<Integer> getPendingRequests() {
        return pendingRequests;
    }

    public Set<Integer> getFollowers() {
        return followers;
    }
}
