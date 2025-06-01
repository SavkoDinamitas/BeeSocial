package servent;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.Message;
import servent.message.PrivilegeMessage;
import servent.message.RequestPrivilegeMessage;
import servent.message.util.MessageUtil;

import java.util.*;

public class SuzukiKasamiMutex {
    private boolean requesting = false, havePrivilege;
    private Queue<Integer> queue = new ArrayDeque<>();
    Map<Integer, Integer> ln =  new HashMap<>(), rn =  new HashMap<>();
    private boolean inCriticalSection = false;

    public SuzukiKasamiMutex(boolean havePrivilege) {
        this.havePrivilege = havePrivilege;
    }

    public synchronized void lock() throws InterruptedException {
        while(!havePrivilege || this.inCriticalSection){
            if (!requesting && !havePrivilege){
                requesting = true;
                //send request to all nodes
                ServentInfo me = AppConfig.myServentInfo;
                rn.merge(me.getListenerPort(), 0, (x, y) -> x + 1);
                int next = AppConfig.chordState.getNextNodePort();
                Message m = new RequestPrivilegeMessage(me.getListenerPort(), next, rn.get(me.getListenerPort()));
                MessageUtil.sendMessage(m);
            }
            wait();
        }

        this.inCriticalSection = true;
    }

    public synchronized void handleRecievePrivilege(PrivilegeMessage message){
        havePrivilege = true;
        queue = message.getQueue();
        ln = message.getLn();
        notifyAll();
    }

    public synchronized void handleRequest(RequestPrivilegeMessage message){
        rn.put(message.getSenderPort(), Math.max(rn.getOrDefault(message.getSenderPort(), -1), message.getRequestNumber()));
        if(havePrivilege && !requesting && !inCriticalSection && rn.get(message.getSenderPort()) == ln.getOrDefault(message.getSenderPort(), -1) + 1){
            havePrivilege = false;
            ServentInfo reciever = AppConfig.chordState.getNextNodeForKey(ChordState.chordHash(message.getSenderPort()));
            Message m = new PrivilegeMessage(AppConfig.myServentInfo.getListenerPort(), reciever.getListenerPort(), new ArrayDeque<>(queue), new HashMap<>(ln), message.getSenderPort());
            MessageUtil.sendMessage(m);
        }
    }

    public synchronized void unlock(){
        ln.put(AppConfig.myServentInfo.getListenerPort(), rn.getOrDefault(AppConfig.myServentInfo.getListenerPort(), -1));
        Set<Integer> servents = new HashSet<>();
        servents.addAll(rn.keySet());
        servents.addAll(ln.keySet());
        for(var x : servents){
            if(x == AppConfig.myServentInfo.getListenerPort()){
                continue;
            }
            if(!queue.contains(x) && rn.getOrDefault(x, -1) == ln.getOrDefault(x, -1) + 1){
                queue.add(x);
            }
        }
        if(queue != null && !queue.isEmpty()){
            havePrivilege = false;
            int priviledgeReciever = queue.poll();
            ServentInfo reciever = AppConfig.chordState.getNextNodeForKey(ChordState.chordHash(priviledgeReciever));
            Message m = new PrivilegeMessage(AppConfig.myServentInfo.getListenerPort(), reciever.getListenerPort(), new ArrayDeque<>(queue), new HashMap<>(ln), priviledgeReciever);
            MessageUtil.sendMessage(m);
        }
        requesting = false;
        this.inCriticalSection = false;
        notifyAll();
    }

    public boolean isHavePrivilege() {
        return havePrivilege;
    }

    public Queue<Integer> getQueue() {
        return queue;
    }

    public Map<Integer, Integer> getLn() {
        return ln;
    }

    public Map<Integer, Integer> getRn() {
        return rn;
    }

    public void nodeReentered(Integer port){
        //reset maps after rejoining
        rn.remove(port);
        ln.remove(port);
    }

    public void setHavePrivilege(boolean havePrivilege) {
        this.havePrivilege = havePrivilege;
    }

    public void setLn(Map<Integer, Integer> ln) {
        this.ln = ln;
    }
}
