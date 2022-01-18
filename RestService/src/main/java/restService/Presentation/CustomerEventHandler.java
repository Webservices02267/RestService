package restService.Presentation;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import messaging.MessageQueue;
import messaging.Event;

public class CustomerEventHandler {

	public static final String CUSTOMER_CREATION_REQUEST = "CustomerCreationRequest";
	public static final String CUSTOMER_CREATION_RESPONSE = "CustomerCreationResponse";
	
    private static MessageQueue messageQueue;

    public static HashMap<String, CompletableFuture<String>> customerCreationPending;
    
    public CustomerEventHandler(MessageQueue messageQueue) {
        CustomerEventHandler.messageQueue = messageQueue;

        //this.messageQueue.addHandler("CustomerCreationResponse_old", this::customerCreationResponse);
    }
    
    
    public String createCustomerCreationRequest(String sessionId, String accountNumber) {

    	// Create a place for the response to reside
    	customerCreationPending.put(sessionId, new CompletableFuture<String>());
    	
    	// Create the event and send it
    	Event event = new Event(CUSTOMER_CREATION_REQUEST, new Object[] { accountNumber, sessionId } );
    	messageQueue.publish(event);
        
    	// Create a handler for the response
    	messageQueue.addHandler(CUSTOMER_CREATION_RESPONSE.concat(".").concat(sessionId), e -> {
    		extracted(sessionId, event);
    	});
    	
    	// Create a timeout
        (new Thread() {
            public void run() {
                try {
                    Thread.sleep(5000);
                    customerCreationPending.get(sessionId).complete("Timed out");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        
    	return customerCreationPending.get(sessionId).join();
    }


	public void extracted(String sessionId, Event event) {
		// Extract the customerId from response
		String customerId = event.getArgument(0, String.class);
		customerCreationPending.get(sessionId).complete(customerId);
	}
    
//    public void customerCreationResponse(Event event) {
//    	String accountNumber = event.getArgument(0, String.class);
//        Event response = new Event("MerchantVerificationRequest", new Object[] {  } );
//        messageQueue.publish(response);
//    }
    
}
