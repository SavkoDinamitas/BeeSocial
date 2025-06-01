package servent.handler;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewNodeMessage;
import servent.message.SorryMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class NewNodeHandler implements MessageHandler {

	private Message clientMessage;
	
	public NewNodeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.NEW_NODE) {
			int newNodePort = clientMessage.getSenderPort();
			ServentInfo newNodeInfo = new ServentInfo("localhost", newNodePort);

            try {
                AppConfig.suzukiKasamiMutex.lock();
            } catch (InterruptedException e) {
                AppConfig.timestampedStandardPrint("Usro se i umro");
            }

            //check if the new node collides with another existing node.
			if (AppConfig.chordState.isCollision(newNodeInfo.getChordId())) {
				Message sry = new SorryMessage(AppConfig.myServentInfo.getListenerPort(), clientMessage.getSenderPort());
				MessageUtil.sendMessage(sry);
				AppConfig.suzukiKasamiMutex.unlock();
				return;
			}
			
			//check if he is my predecessor
			boolean isMyPred = AppConfig.chordState.isKeyMine(newNodeInfo.getChordId());
			if (isMyPred) { //if yes, prepare and send welcome message
				ServentInfo hisPred = AppConfig.chordState.getPredecessor();
				if (hisPred == null) {
					hisPred = AppConfig.myServentInfo;
				}
				
				AppConfig.chordState.setPredecessor(newNodeInfo);
				
				Map<Integer, Set<String>> myValues = AppConfig.chordState.getValueMap();
				Map<Integer, Set<Integer>> myFollowers = AppConfig.chordState.getAllFollowers();
				Map<Integer, Set<Integer>> myPendingRequests = AppConfig.chordState.getAllPendingRequests();
				Map<Integer, Set<String>> hisValues = new ConcurrentHashMap<>();
				Map<Integer, Set<Integer>> hisFollowers = new ConcurrentHashMap<>();
				Map<Integer, Set<Integer>> hisPendingRequests = new ConcurrentHashMap<>();
				
				int myId = AppConfig.myServentInfo.getChordId();
				int hisPredId = hisPred.getChordId();
				int newNodeId = newNodeInfo.getChordId();
				
				for (Entry<Integer, Set<String>> valueEntry : myValues.entrySet()) {
					int valueEntryKey = ChordState.chordHash(valueEntry.getKey());
					if (hisPredId == myId) { //i am first and he is second
						if (myId < newNodeId) {
							if (valueEntryKey <= newNodeId && valueEntryKey > myId) {
								hisValues.put(valueEntry.getKey(), Collections.synchronizedSet(new HashSet<>(valueEntry.getValue())));
								hisFollowers.put(valueEntry.getKey(), myFollowers.get(valueEntry.getKey()));
								hisPendingRequests.put(valueEntry.getKey(), myPendingRequests.get(valueEntry.getKey()));
							}
						} else {
							if (valueEntryKey <= newNodeId || valueEntryKey > myId) {
								hisValues.put(valueEntry.getKey(), Collections.synchronizedSet(new HashSet<>(valueEntry.getValue())));
								hisFollowers.put(valueEntry.getKey(), myFollowers.get(valueEntry.getKey()));
								hisPendingRequests.put(valueEntry.getKey(), myPendingRequests.get(valueEntry.getKey()));
							}
						}
					}
					if (hisPredId < myId) { //my old predecesor was before me
						if (valueEntryKey <= newNodeId) {
							hisValues.put(valueEntry.getKey(), Collections.synchronizedSet(new HashSet<>(valueEntry.getValue())));
							hisFollowers.put(valueEntry.getKey(), myFollowers.get(valueEntry.getKey()));
							hisPendingRequests.put(valueEntry.getKey(), myPendingRequests.get(valueEntry.getKey()));
						}
					} else { //my old predecessor was after me
						if (hisPredId > newNodeId) { //new node overflow
							if (valueEntryKey <= newNodeId || valueEntryKey > hisPredId) {
								hisValues.put(valueEntry.getKey(), Collections.synchronizedSet(new HashSet<>(valueEntry.getValue())));
								hisFollowers.put(valueEntry.getKey(), myFollowers.get(valueEntry.getKey()));
								hisPendingRequests.put(valueEntry.getKey(), myPendingRequests.get(valueEntry.getKey()));
							}
						} else { //no new node overflow
							if (valueEntryKey <= newNodeId && valueEntryKey > hisPredId) {
								hisValues.put(valueEntry.getKey(), Collections.synchronizedSet(new HashSet<>(valueEntry.getValue())));
								hisFollowers.put(valueEntry.getKey(), myFollowers.get(valueEntry.getKey()));
								hisPendingRequests.put(valueEntry.getKey(), myPendingRequests.get(valueEntry.getKey()));
							}
						}
						
					}
					
				}
				//don't erase my copy of his elements, they are needed for backup
				/*for (Integer key : hisValues.keySet()) { //remove his values from my map
					myValues.remove(key);
				}
				AppConfig.chordState.setValueMap(myValues);*/
				
				WelcomeMessage wm = new WelcomeMessage(AppConfig.myServentInfo.getListenerPort(), newNodePort, hisValues, hisFollowers, hisPendingRequests);
				MessageUtil.sendMessage(wm);
			} else { //if he is not my predecessor, let someone else take care of it
				ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(newNodeInfo.getChordId());
				NewNodeMessage nnm = new NewNodeMessage(newNodePort, nextNode.getListenerPort());
				MessageUtil.sendMessage(nnm);
				AppConfig.suzukiKasamiMutex.unlock();
			}
			
		} else {
			AppConfig.timestampedErrorPrint("NEW_NODE handler got something that is not new node message.");
		}

	}

}
