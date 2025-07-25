package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import servent.message.*;
import servent.message.util.MessageUtil;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;
	public static int chordHash(int value) {
		return 61 * value % CHORD_SIZE;
	}
	
	private int chordLevel; //log_2(CHORD_SIZE)
	
	private volatile ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;
	
	//we DO NOT use this to send messages, but only to construct the successor table
	private List<ServentInfo> allNodeInfo;
	
	private Map<Integer, Integer> valueMap;
	//map of uploaded files, including the backups
	private Map<Integer, Set<String>> uploadedFiles = new ConcurrentHashMap<>();
	private Map<Integer, Set<Integer>> pendingRequests = new ConcurrentHashMap<>();
	private Map<Integer, Set<Integer>> followers = new ConcurrentHashMap<>();
	private Map<Integer, Boolean> publicProfile = new ConcurrentHashMap<>();
	private int changeId = 0;
	private Map<Integer, Integer> nodeReplicasChanges = new ConcurrentHashMap<>();
	private volatile boolean successorAlive = true;
	private Map<Integer, Boolean> requestedPings = new ConcurrentHashMap<>();
	private Map<Integer, Boolean> gotDeleteBack = new ConcurrentHashMap<>();

	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { //not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}
		
		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
		
		predecessorInfo = null;
		valueMap = new HashMap<>();
		allNodeInfo = new ArrayList<>();
	}
	
	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		//set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo("localhost", welcomeMsg.getSenderPort());
		this.uploadedFiles = welcomeMsg.getValues();
		this.followers = welcomeMsg.getFollowers();
		this.pendingRequests = welcomeMsg.getRequests();

		//tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			
			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getChordLevel() {
		return chordLevel;
	}
	
	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}
	
	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}
	
	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}
	
	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	public Map<Integer, Set<String>> getValueMap() {
		return uploadedFiles;
	}
	
	public void setValueMap(Map<Integer, Integer> valueMap) {
		this.valueMap = valueMap;
	}
	
	public boolean isCollision(int chordId) {
		if (chordId == AppConfig.myServentInfo.getChordId()) {
			return true;
		}
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() == chordId) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}
		
		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();
		
		if (predecessorChordId < myChordId) { //no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { //overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Main chord operation - find the nearest node to hop to to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}
		
		//normally we start the search from our first successor
		int startInd = 0;
		
		//if the key is smaller than us, and we are not the owner,
		//then all nodes up to CHORD_SIZE will never be the owner,
		//so we start the search from the first item in our table after CHORD_SIZE
		//we know that such a node must exist, because otherwise we would own this key
		if (key < AppConfig.myServentInfo.getChordId()) {
			int skip = 1;
			while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
				startInd++;
				skip++;
			}
		}
		
		int previousId = successorTable[startInd].getChordId();
		
		for (int i = startInd + 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}
			
			int successorId = successorTable[i].getChordId();
			
			if (successorId >= key) {
				return successorTable[i-1];
			}
			if (key > previousId && successorId < previousId) { //overflow
				return successorTable[i-1];
			}
			previousId = successorId;
		}
		//if we have only one node in all slots in the table, we might get here
		//then we can return any item
		return successorTable[0];
	}

	private void updateSuccessorTable() {
		//first node after me has to be successorTable[0]
		
		int currentNodeIndex = 0;
		ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
		successorTable[0] = currentNode;
		
		int currentIncrement = 2;
		
		ServentInfo previousNode = AppConfig.myServentInfo;
		
		//i is successorTable index
		for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			//we are looking for the node that has larger chordId than this
			int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;
			
			int currentId = currentNode.getChordId();
			int previousId = previousNode.getChordId();
			
			//this loop needs to skip all nodes that have smaller chordId than currentValue
			while (true) {
				if (currentValue > currentId) {
					//before skipping, check for overflow
					if (currentId > previousId || currentValue < previousId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				} else { //node id is larger
					ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
					int nextNodeId = nextNode.getChordId();
					//check for overflow
					if (nextNodeId < currentId && currentValue <= nextNodeId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				}
			}
		}
		
	}

	/**
	 * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
	 * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
	 * 
	 */
	public void addNodes(List<ServentInfo> newNodes) {
		allNodeInfo.addAll(newNodes);
		
		allNodeInfo.sort(new Comparator<ServentInfo>() {
			
			@Override
			public int compare(ServentInfo o1, ServentInfo o2) {
				return o1.getChordId() - o2.getChordId();
			}
			
		});
		
		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();
		
		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);
			} else {
				newList.add(serventInfo);
			}
		}
		
		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if (newList2.size() > 0) {
			predecessorInfo = newList2.get(newList2.size()-1);
		} else {
			predecessorInfo = newList.get(newList.size()-1);
		}
		
		updateSuccessorTable();
	}

	public void deleteNode(ServentInfo deleteNode){
		//already got message for this removal
		if(!allNodeInfo.contains(deleteNode))
			return;
		if(predecessorInfo.getChordId() == deleteNode.getChordId()){
			for(int i = 0; i < allNodeInfo.size(); i++){
				if(allNodeInfo.get(i).getChordId() == deleteNode.getChordId()){
					if(i != 0)
						predecessorInfo = allNodeInfo.get(i-1);
					else
						predecessorInfo = allNodeInfo.getLast();
					break;
				}
			}
		}
		allNodeInfo.remove(deleteNode);
		updateSuccessorTable();
	}

	private Set<String> makeSetWithInitialValue(String value){
		Set<String> set = ConcurrentHashMap.newKeySet();
		set.add(value);
		return set;
	}

	private Set<Integer> makeSetWithInitialValue(Integer value){
		Set<Integer> set = ConcurrentHashMap.newKeySet();
		set.add(value);
		return set;
	}

	/**
	 * The Chord put operation. Stores locally if key is ours, otherwise sends it on.
	 */
	public void putValue(Integer key, String value) {
		uploadedFiles.merge(key, makeSetWithInitialValue(value), (set, x) -> {set.add(value); return set;});
	}
	
	/**
	 * The chord get operation. Gets the value locally if key is ours, otherwise asks someone else to give us the value.
	 * @return <ul>
	 *			<li>The value, if we have it</li>
	 *			<li>-1 if we own the key, but there is nothing there</li>
	 *			<li>-2 if we asked someone else</li>
	 *		   </ul>
	 */
	public int getValue(int key) {
		if (isKeyMine(key)) {
			if (valueMap.containsKey(key)) {
				return valueMap.get(key);
			} else {
				return -1;
			}
		}
		
		ServentInfo nextNode = getNextNodeForKey(key);
		AskGetMessage agm = new AskGetMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), String.valueOf(key));
		MessageUtil.sendMessage(agm);
		
		return -2;
	}

	public void solveFileRequest(int key, int requester){
		int me = AppConfig.myServentInfo.getListenerPort();
		Message m = null;
		ServentInfo reciever = getNextNodeForKey(chordHash(requester));
		if(!publicProfile.getOrDefault(key, false) && (followers.get(key) == null || !followers.get(key).contains(requester))) {
			m = new FileListResultMessage(key, reciever.getListenerPort(), (Set<String>) null, requester);
		}
		else{
			m = new FileListResultMessage(key, reciever.getListenerPort(), uploadedFiles.get(key), requester);
		}
		MessageUtil.sendMessage(m);
	}

	//add follow request to collection
	public void getFollowRequest(int requester, int requested){
		//int me = AppConfig.myServentInfo.getListenerPort();
		pendingRequests.merge(requested, makeSetWithInitialValue(requester), (set, x) -> {set.add(requester); return set;});
		//pendingRequests.get(AppConfig.myServentInfo.getListenerPort()).add(requester);
	}

	public void acceptFollower(int requester){
		int me = AppConfig.myServentInfo.getListenerPort();
		followers.merge(me, makeSetWithInitialValue(requester), (set, x) -> {set.add(requester); return set;});
	}

	public Set<Integer> getFollowers() {
		if(followers == null){

		}
		return followers.get(AppConfig.myServentInfo.getListenerPort());
	}

	public void setFollowers(Map<Integer, Set<Integer>> followers) {
		this.followers = followers;
	}

	public Set<Integer> getPendingRequests() {
		return pendingRequests.get(AppConfig.myServentInfo.getListenerPort());
	}

	public void setPendingRequests(Map<Integer, Set<Integer>> pendingRequests) {
		this.pendingRequests = pendingRequests;
	}

	public Set<String> getUploadedFiles() {
		return uploadedFiles.get(AppConfig.myServentInfo.getListenerPort());
	}


	public boolean isPublicProfile() {
		return publicProfile.get(AppConfig.myServentInfo.getListenerPort());
	}

	public void setPublicProfile(boolean publicProfile) {
		this.publicProfile.put(AppConfig.myServentInfo.getListenerPort(), publicProfile);
	}

	public int getChangeId() {
		return changeId;
	}

	public void incrementChangeId(){
		changeId++;
	}

	public void resetChanges(Integer key){
		nodeReplicasChanges.put(key, 0);
	}

	public void updateReplicaForNode(Integer key, Set<String> files, Set<Integer> requests, Set<Integer> followers, Integer changeId){
		if(changeId > nodeReplicasChanges.getOrDefault(key, 0)){
			uploadedFiles.put(key, files);
			if(requests == null){
				requests = Collections.synchronizedSet(new HashSet<>());
			}
			if(followers == null){
				followers = Collections.synchronizedSet(new HashSet<>());
			}
			pendingRequests.put(key, requests);
			this.followers.put(key, followers);
			nodeReplicasChanges.put(key, changeId);
		}
	}

	public Map<Integer, Set<String>> getAllFilesStored(){
		return uploadedFiles;
	}

	public Map<Integer, Set<Integer>> getAllFollowers(){
		return followers;
	}

	public Map<Integer, Set<Integer>> getAllPendingRequests(){
		return pendingRequests;
	}

	public boolean isSuccessorAlive() {
		return successorAlive;
	}

	public void setSuccessorAlive(boolean successorAlive) {
		this.successorAlive = successorAlive;
	}

	public List<ServentInfo> getAllNodeInfo() {
		return allNodeInfo;
	}

	public Map<Integer, Boolean> getRequestedPings() {
		return requestedPings;
	}

	public boolean isGotDeleteBack(Integer key) {
		return gotDeleteBack.getOrDefault(key, false);
	}

	public void setGotDeleteBack(Integer key) {
		this.gotDeleteBack.put(key, true);
	}
}
