package cli.command;

import app.AppConfig;
import cli.CLIParser;
import servent.SimpleServentListener;
import servent.message.Message;
import servent.message.PrivilegeMessage;
import servent.message.StopMessage;
import servent.message.util.MessageUtil;

public class StopCommand implements CLICommand {

	private CLIParser parser;
	private SimpleServentListener listener;
	
	public StopCommand(CLIParser parser, SimpleServentListener listener) {
		this.parser = parser;
		this.listener = listener;
	}
	
	@Override
	public String commandName() {
		return "stop";
	}

	@Override
	public void execute(String args) {
		//TODO get node out of the system
		Message m = new StopMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), AppConfig.myServentInfo);
		MessageUtil.sendMessage(m);
		if(AppConfig.suzukiKasamiMutex.isHavePrivilege()){
			Message m1 = new PrivilegeMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), AppConfig.suzukiKasamiMutex.getQueue(),
					AppConfig.suzukiKasamiMutex.getLn(), AppConfig.chordState.getNextNodePort());
			MessageUtil.sendMessage(m1);
		}
		AppConfig.timestampedStandardPrint("Stopping...");
		parser.stop();
		listener.stop();
	}

}
