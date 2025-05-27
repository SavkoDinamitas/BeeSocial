package cli.command;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.ListFilesMessage;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class ListFilesCommand implements CLICommand{
    @Override
    public String commandName() {
        return "list_files";
    }

    @Override
    public void execute(String args) {
        if(Integer.parseInt(args) == AppConfig.myServentInfo.getListenerPort()){
            AppConfig.timestampedStandardPrint("Uploaded files for user: ");
            for(var x : AppConfig.chordState.getUploadedFiles()){
                AppConfig.timestampedStandardPrint("\t" + x);
            }
        }
        else{
            //send message to required user
            ServentInfo reciever = AppConfig.chordState.getNextNodeForKey(ChordState.chordHash(Integer.parseInt(args)));
            Message m = new ListFilesMessage(AppConfig.myServentInfo.getListenerPort(), reciever.getListenerPort(), Integer.parseInt(args));
            MessageUtil.sendMessage(m);
        }
    }
}
