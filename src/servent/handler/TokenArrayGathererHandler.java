package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.Message;
import servent.message.PrivilegeMessage;
import servent.message.TokenArrayMessage;
import servent.message.util.MessageUtil;

import java.util.*;

public class TokenArrayGathererHandler implements MessageHandler{
    TokenArrayMessage message;

    public TokenArrayGathererHandler(Message m){
        message = (TokenArrayMessage) m;
    }
    @Override
    public void run() {
        if(message.getSenderPort() != AppConfig.myServentInfo.getListenerPort()){
            int myId = AppConfig.myServentInfo.getListenerPort();
            var ln = AppConfig.suzukiKasamiMutex.getLn();
            var rn = AppConfig.suzukiKasamiMutex.getRn();
            message.getRn().merge(myId, rn.getOrDefault(myId, -1), Integer::max);
            for (final var le : ln.entrySet())
                message.getLn().merge(le.getKey(), le.getValue(), Integer::max);
            Message m = new TokenArrayMessage(message.getSenderPort(), AppConfig.chordState.getNextNodePort(), message.getLn(), message.getRn());
            MessageUtil.sendMessage(m);
        }
        else{
            List<Integer> queue = new ArrayList<>();
            var ln = message.getLn();
            var rn = message.getRn();
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
            queue.sort(Integer::compareTo);
            if(!queue.isEmpty()){
                int priviledgeReciever = queue.getFirst();
                queue.removeFirst();
                ServentInfo reciever = AppConfig.chordState.getNextNodeForKey(ChordState.chordHash(priviledgeReciever));
                Message m = new PrivilegeMessage(AppConfig.myServentInfo.getListenerPort(), reciever.getListenerPort(), new ArrayDeque<>(queue), new HashMap<>(ln), priviledgeReciever);
                MessageUtil.sendMessage(m);
            }
            else{
                AppConfig.suzukiKasamiMutex.setLn(message.getLn());
                AppConfig.suzukiKasamiMutex.setHavePrivilege(true);
            }
        }
    }
}
