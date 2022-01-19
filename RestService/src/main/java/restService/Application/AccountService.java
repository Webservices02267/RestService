package restService.Application;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import messaging.Event;
import messaging.EventResponse;
import messaging.MessageQueue;

public class AccountService {

	public AccountService(MessageQueue messageQueue) {
		AccountService.messageQueue = messageQueue;
	}

	public enum Role {
		CUSTOMER("CustomerCreationRequest", "CustomerCreationResponse"),
		MERCHANT("MerchantCreationRequest", "MerchantCreationResponse");

		public final String CREATION_REQUEST;
		public final String CREATION_RESPONSE;

		private Role(String request, String respons) {
			this.CREATION_REQUEST = request;
			this.CREATION_RESPONSE = respons;
		}
	}

	private static MessageQueue messageQueue;
	ServiceHelper serviceHelper = new ServiceHelper();

	private ConcurrentHashMap<String, CompletableFuture<Event>> sessions = new ConcurrentHashMap<>();

	public String getStatus(String sessionId) throws Exception {

		messageQueue.addHandler("AccountStatusResponse." + sessionId, this::handleGetStatus);
		sessions.put(sessionId, new CompletableFuture<Event>());
		messageQueue.publish(new Event("AccountStatusRequest", new Object[] { sessionId }));

		(new Thread() {
			public void run() {
				try {
					Thread.sleep(5000);
					EventResponse eventResponse = new EventResponse(sessionId, false, "No reply from a Account service");
					sessions.get(sessionId).complete(new Event("", eventResponse));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

		EventResponse eventResponse = sessions.get(sessionId).join().getArgument(0, EventResponse.class);

		if (eventResponse.isSuccess()) {
			return eventResponse.getArgument(0, String.class);
		}
		throw new Exception(eventResponse.getErrorMessage());
	}

	public void handleGetStatus(Event event) {
		String sessionId = event.getArgument(0, EventResponse.class).getSessionId();
		sessions.get(sessionId).complete(event);
	}

	public String createCustomerCreationRequest(String sessionId, String accountNumber, Role role)
			throws InterruptedException, ExecutionException {

		sessions.put(sessionId, new CompletableFuture<Event>());

		Event event = new Event(role.CREATION_REQUEST, accountNumber, sessionId);

		messageQueue.addHandler(role.CREATION_RESPONSE + "." + sessionId, this::customerCreationResponseHandler);
		messageQueue.publish(event);

		serviceHelper.addTimeOut(sessionId, sessions.get(sessionId), "ERROR: Request timed out");

		if (sessions.get(sessionId).join().getArgument(0, EventResponse.class).isSuccess()) {
			return sessions.get(sessionId).get().getArgument(0, EventResponse.class).getArgument(0, String.class);
		}

		return sessions.get(sessionId).get().getArgument(0, EventResponse.class).getErrorMessage();
	}

	public void customerCreationResponseHandler(Event event) {
		String sessionId = event.getArgument(0, EventResponse.class).getSessionId();
		sessions.get(sessionId).complete(event);
	}

}
