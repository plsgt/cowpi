package net.cowpi.perf.client;

import com.google.protobuf.ByteString;
import com.google.protobuf.MapEntry;
import net.cowpi.crypto.CryptoEngine;
import net.cowpi.crypto.EphemeralKeyPair;
import net.cowpi.crypto.KeyPair;
import net.cowpi.oes.jooq.tables.RouterMailbox;
import net.cowpi.perf.router.RouterMessage;
import net.cowpi.protobuf.CiphertextProto.RegisterUser;
import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.protobuf.OesProto.OesPreKey;
import net.cowpi.protobuf.UserProtos.FetchPrekey;
import net.cowpi.protobuf.UserProtos.PreKey;
import net.cowpi.server.engine.UserEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.*;

@Singleton
public class ClientExperiment {
    private static final Logger logger = LoggerFactory.getLogger(ClientExperiment.class);
    private final String USERNAME_PREFIX = "USER_";

    private final UserEngine userEngine;
    private final CryptoEngine crypto;
    private final Provider<User> userProvider;

    private final List<User> users = new ArrayList<>(50);
    private final Map<Long, User> userMap = new HashMap<>(50);


    @Inject
    public ClientExperiment(UserEngine userEngine, CryptoEngine crypto, Provider<User> userProvider){
        this.userEngine = userEngine;
        this.crypto = crypto;
        this.userProvider = userProvider;
    }

    public void registerUsers(int count, int prekeyCount) {
        for(int i=0; i<count; i++) {
            String username = USERNAME_PREFIX + i;
            KeyPair longtermKeyPair = crypto.generateLongtermKeyPair();

            RegisterUser.Builder registerUser = RegisterUser.newBuilder()
                    .setUsername(username)
                    .setLongtermKey(ByteString.copyFrom(longtermKeyPair.getPublicKey()));

            List<PreKey> uploadPrekeys = new ArrayList<>();

            List<EphemeralKeyPair> prekeys = new ArrayList<>(prekeyCount);
            for(int j=0; j<prekeyCount; j++){
                EphemeralKeyPair prekey = crypto.generateEphemeralKeyPair(j, longtermKeyPair);
                PreKey.Builder prekeyBuilder = PreKey.newBuilder()
                        .setKeyId(prekey.getId())
                        .setPrekey(ByteString.copyFrom(prekey.getPublicKey()));

                uploadPrekeys.add(prekeyBuilder.build());

                prekeys.add(prekey);
            }

            userEngine.addUser(registerUser.build());
            long userId = userEngine.getUserRecord(username).getId();
            userEngine.addPreKeys(userId, uploadPrekeys);

            User user = userProvider.get();
            user.init(username, userId, longtermKeyPair, prekeys);
            users.add(user);
            userMap.put(userId, user);
        }

    }


    public List<CowpiMessage> createSetup(int count, int size, Map<String, List<OesPreKey>> oesPrekeys) throws InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        List<CowpiMessage> result = new ArrayList<>(count);

        for(int i=0; i<count; i++){
            Collections.shuffle(users);

            List<User> participants = new ArrayList<>(size);
            for(int j=1; j<size; j++){
                participants.add(users.get(j));
            }

            Map<String, OesPreKey> oesKeys = new HashMap<>();
            for(Map.Entry<String, List<OesPreKey>> prekey: oesPrekeys.entrySet()){
                oesKeys.put(prekey.getKey(), prekey.getValue().get(i));
            }

            users.get(0).createConversation(i, participants, oesKeys);
        }

        long start = System.currentTimeMillis();
        for(User user: users){
            for(Map.Entry<Long, CowpiSession> sessionEntry: user.getSessions().entrySet()){
                CowpiSession session = sessionEntry.getValue();
                result.add(session.setup());
            }
        }
        long end = System.currentTimeMillis();

        logger.info("Setup {} of size {} in {}.", count, size, end-start);

        return result;
    }

    public void reset(){
        users.clear();
    }

    public void handleSetupMessage(List<RouterMessage> routerSetupMessages) {

        long start = System.currentTimeMillis();

        for(RouterMessage message: routerSetupMessages){
            userMap.get(message.getUserId()).handleSetup(message.getMessage());
        }

        long end = System.currentTimeMillis();

        logger.info("Setup {} of size {} in {}.", routerSetupMessages.size(), routerSetupMessages.get(0).getMessage().getParticipantCount(), end-start);
    }
}
