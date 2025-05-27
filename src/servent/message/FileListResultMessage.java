package servent.message;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileListResultMessage extends BasicMessage{
    @Serial
    private static final long serialVersionUID = 5975692529841980302L;

    private List<String> files;
    private int requester;
    public FileListResultMessage(int senderPort, int receiverPort, Set<String> files, int requester) {
        super(MessageType.LIST_RESULT, senderPort, receiverPort);
        this.requester = requester;
        //make a copy of sync set
        this.files = null;
        if(files != null){
            synchronized (files){
                this.files = new ArrayList<>(files);
            }
        }
    }

    public FileListResultMessage(int senderPort, int receiverPort, List<String> files, int requester) {
        super(MessageType.LIST_RESULT, senderPort, receiverPort);
        this.requester = requester;
        this.files = files;
    }

    public List<String> getFiles() {
        return files;
    }

    public int getRequester() {
        return requester;
    }
}
