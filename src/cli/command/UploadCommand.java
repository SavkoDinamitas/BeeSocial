package cli.command;

import app.AppConfig;
import app.ChordState;
import servent.message.Message;
import servent.message.ReplicateMessage;
import servent.message.util.MessageUtil;

public class UploadCommand implements CLICommand {

	@Override
	public String commandName() {
		return "upload";
	}

	@Override
	public void execute(String args) {
		//just add file to the collection
		AppConfig.chordState.putValue(AppConfig.myServentInfo.getListenerPort(), args);
		AppConfig.timestampedStandardPrint("File " + args + " has been uploaded");
		Message m = new ReplicateMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), AppConfig.chordState.getUploadedFiles(),
				AppConfig.chordState.getPendingRequests(), AppConfig.chordState.getFollowers(),false, AppConfig.chordState.getChangeId());
		AppConfig.chordState.incrementChangeId();
		MessageUtil.sendMessage(m);
	}

}
