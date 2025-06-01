package cli.command;

import app.AppConfig;
import servent.message.Message;
import servent.message.ReplicateMessage;
import servent.message.util.MessageUtil;

public class RemoveCommand implements CLICommand{
    @Override
    public String commandName() {
        return "remove_file";
    }

    @Override
    public void execute(String args) {
        if(!AppConfig.chordState.getUploadedFiles().contains(args)){
            AppConfig.timestampedStandardPrint("You don't have this file in directory!");
        }
        else{
            AppConfig.chordState.getUploadedFiles().remove(args);
            Message m = new ReplicateMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), AppConfig.chordState.getUploadedFiles(),
                    AppConfig.chordState.getPendingRequests(), AppConfig.chordState.getFollowers(), false, AppConfig.chordState.getChangeId());
            AppConfig.chordState.incrementChangeId();
            MessageUtil.sendMessage(m);
        }
    }
}
