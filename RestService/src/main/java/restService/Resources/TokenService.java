package restService.Resources;

import messaging.Event;
import messaging.MessageQueue;
import restService.Domain.Token;

import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TokenService {
    private MessageQueue mq;
    public CompletableFuture<Token> sessionHandled;
    public TokenService(MessageQueue mq){
        this.mq = mq;
    }

    private void handleGetTokens(Event event) {
//        String sessionId = event.getArgument(1, String.class);
//        HashSet returnVal = event.getArgument(0, HashSet.class);
        sessionHandled.complete(event.getArgument(0, Token.class));
    }

    public Token getTokensMessageSerivce(String customerId, int numOfTokens){
        String sessionID = UUID.randomUUID().toString();
        String topic = "CustomerVerified";
        sessionHandled = new CompletableFuture<>();
        Event e = new Event(topic, new Object[]{customerId, sessionID});
        mq.addHandler(topic + "#" + sessionID, this::handleGetTokens);
        mq.publish(e);
        return sessionHandled.join();
    }
}