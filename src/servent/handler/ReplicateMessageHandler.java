package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.ReplicateMessage;
import servent.message.util.MessageUtil;

public class ReplicateMessageHandler implements MessageHandler{
    private ReplicateMessage replicateMessage;

    public ReplicateMessageHandler(Message replicateMessage) {
        this.replicateMessage = (ReplicateMessage) replicateMessage;
    }

    @Override
    public void run() {
        //update node files in my node
        AppConfig.chordState.updateReplicaForNode(replicateMessage.getSenderPort(), replicateMessage.getUploadedFiles(), replicateMessage.getReplicaId());
        //if this is first replica, forward it to the next node
        if(!replicateMessage.isForwarded()){
            Message m = new ReplicateMessage(replicateMessage.getSenderPort(), AppConfig.chordState.getNextNodePort(), replicateMessage.getUploadedFiles(),
                    true, replicateMessage.getReplicaId());
            MessageUtil.sendMessage(m);
        }
    }
}
