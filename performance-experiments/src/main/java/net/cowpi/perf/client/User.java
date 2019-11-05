package net.cowpi.perf.client;

import net.cowpi.crypto.EphemeralKeyPair;
import net.cowpi.crypto.KeyPair;
import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.protobuf.OesProto.OesPreKey;
import net.cowpi.protobuf.UserProtos.FetchPrekey;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    private String name;
    private long id;
    private KeyPair keyPair;
    private List<EphemeralKeyPair> ephemeralKeyPairs;
    private int nextKeyPair = -1;

    private final Provider<CowpiSession> sessionProvider;
    private final Map<Long, CowpiSession> sessions = new HashMap<>();

    @Inject
    public User(Provider<CowpiSession> sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    public void init(String name, long id, KeyPair keyPair, List<EphemeralKeyPair> prekeys){
        this.name = name;
        this.id = id;
        this.keyPair = keyPair;
        this.ephemeralKeyPairs = prekeys;
    }

    public String getName() {
        return name;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public List<EphemeralKeyPair> getEphemeralKeyPairs() {
        return ephemeralKeyPairs;
    }

    public long getId() {
        return id;
    }

    public EphemeralKeyPair nextEkp() {
        nextKeyPair = (nextKeyPair+1) % ephemeralKeyPairs.size();

        return ephemeralKeyPairs.get(nextKeyPair);
    }

    public void createConversation(long conversationId, List<User> participants, Map<String, OesPreKey> oesPrekeys) {
        CowpiSession session = sessionProvider.get();
        session.init(this, conversationId, participants, oesPrekeys);
        sessions.put(conversationId, session);
    }

    public Map<Long, CowpiSession> getSessions(){
        return sessions;
    }

    public void handleSetup(CowpiMessage message) {

        if(name.equals(message.getAuthor())){
            return;
        }

        CowpiSession session = sessionProvider.get();
        session.init(this, message);
    }
}
