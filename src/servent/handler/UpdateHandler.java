package servent.handler;

import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.ServentInfo;
import servent.SuccessorAlive;
import servent.message.FinishedReorganizationMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.util.MessageUtil;

public class UpdateHandler implements MessageHandler {

	private Message clientMessage;
	
	public UpdateHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.UPDATE) {
			if (clientMessage.getSenderPort() != AppConfig.myServentInfo.getListenerPort()) {
				ServentInfo newNodInfo = new ServentInfo("localhost", clientMessage.getSenderPort());
				List<ServentInfo> newNodes = new ArrayList<>();
				newNodes.add(newNodInfo);
				
				AppConfig.chordState.addNodes(newNodes);
				String newMessageText = "";
				if (clientMessage.getMessageText().equals("")) {
					newMessageText = String.valueOf(AppConfig.myServentInfo.getListenerPort());
				} else {
					newMessageText = clientMessage.getMessageText() + "," + AppConfig.myServentInfo.getListenerPort();
				}
				AppConfig.chordState.resetChanges(newNodInfo.getListenerPort());
				AppConfig.suzukiKasamiMutex.nodeReentered(newNodInfo.getListenerPort());
				Message nextUpdate = new UpdateMessage(clientMessage.getSenderPort(), AppConfig.chordState.getNextNodePort(),
						newMessageText);
				MessageUtil.sendMessage(nextUpdate);
			} else {
				String messageText = clientMessage.getMessageText();
				String[] ports = messageText.split(",");
				
				List<ServentInfo> allNodes = new ArrayList<>();
				for (String port : ports) {
					allNodes.add(new ServentInfo("localhost", Integer.parseInt(port)));
				}
				AppConfig.chordState.addNodes(allNodes);
				AppConfig.chordState.resetChanges(clientMessage.getSenderPort());
				AppConfig.suzukiKasamiMutex.nodeReentered(clientMessage.getSenderPort());
				Message m = new FinishedReorganizationMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort());
				MessageUtil.sendMessage(m);
				//make thread to check if my successor is alive
				Thread t = new Thread(new SuccessorAlive());
				t.start();
			}
		} else {
			AppConfig.timestampedErrorPrint("Update message handler got message that is not UPDATE");
		}
	}

}
