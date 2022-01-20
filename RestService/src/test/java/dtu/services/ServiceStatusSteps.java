package dtu.services;

import messaging.Event;
import messaging.EventResponse;
import messaging.implementations.MockMessageQueue;
import restService.Application.AccountService;
import restService.Application.PaymentService;
import restService.Application.ReportService;
import restService.Application.ServiceHelper;
import restService.Application.TokenService;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ServiceStatusSteps {

  private static MockMessageQueue messageQueue = new MockMessageQueue();
  private TokenService tokenService = new TokenService(messageQueue);
  private AccountService AccountService = new AccountService(messageQueue);
  private PaymentService paymentService = new PaymentService(messageQueue);
  private ReportService reportService = new ReportService(messageQueue);

  ServiceHelper serviceHelper = new ServiceHelper();
  
  private CompletableFuture<String> statusMessage = new CompletableFuture<>();

  String sessionId;

  String responseStatus;
  
  
  int default_timeout;
	@Before()
	public void saveTimeout() {
		this.default_timeout = serviceHelper.TIMEOUT;
	}
	
	@After()
	public void restoreTimeout() {
		serviceHelper.TIMEOUT = this.default_timeout;
	}

	
	
	@Given("the ServiceHelper timeout is {int} ms")
	public void theTimeoutIsMs(Integer newTimeout) {
	    serviceHelper.TIMEOUT = newTimeout;
	}
	
  
  @When("the Token service is requested for its status")
  public void theTokenServiceIsRequestedForItsStatus() {
    new Thread(() -> {
      sessionId = UUID.randomUUID().toString();
      String status;
      try {
        status = tokenService.getStatus(sessionId);
      } catch (Exception e) {
        status = e.getMessage();
      }
      statusMessage.complete(status);
    }).start();
  }

  @When("the Account service is requested for its status")
  public void theAccountServiceIsRequestedForItsStatus() {
    new Thread(() -> {
      sessionId = UUID.randomUUID().toString();
      String status;
      try {
        status = AccountService.getStatus(sessionId);
      } catch (Exception e) {
        status = e.getMessage();
      }
      statusMessage.complete(status);
    }).start();
  }

  @When("the Payment service is requested for its status")
  public void thePaymentServiceIsRequestedForItsStatus() {
	    new Thread(() -> {
	        sessionId = UUID.randomUUID().toString();
	        String status;
	        try {
	          status = paymentService.getStatus(sessionId);
	        } catch (Exception e) {
	          status = e.getMessage();
	        }
	        statusMessage.complete(status);
	      }).start();
  }

  @When("the Report service is requested for its status")
  public void theReportServiceIsRequestedForItsStatus() {
	  new Thread(() -> {
	        sessionId = UUID.randomUUID().toString();
	        String status;
	        try {
	          status = reportService.getStatus(sessionId);
	        } catch (Exception e) {
	          status = e.getMessage();
	        }
	        statusMessage.complete(status);
	      }).start();
  }

  @When("the Report service replies with the status message {string}")
  public void theReportServiceRepliesWithTheStatusMessage(String statusMessage) {
	    Event event = new Event("ReportStatusResponse." + sessionId, new EventResponse(sessionId, true, null, statusMessage));
	    reportService.handleResponse(event);
  }
  
  @When("the Payment service replies with the status message {string}")
  public void thePaymentServiceRepliesWithTheStatusMessage(String statusMessage) {
	    Event event = new Event("PaymentStatusResponse." + sessionId, new EventResponse(sessionId, true, null, statusMessage));
	    paymentService.handleResponse(event);
  }
  
  @Then("the event {string} have been sent")
  public void theEventHaveBeenSent(String eventTopic) {

    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    Event expectedEvent = new Event(eventTopic, new EventResponse(sessionId, true, null));
    Event actualEvent = messageQueue.getEvent(eventTopic);
    
    assertEquals(expectedEvent, actualEvent);
  }

  @When("the Token service replies with the status message {string}")
  public void theTokenServiceRepliesWithTheStatusMessage(String statusMessage) {
    Event event = new Event("TokenStatusResponse." + sessionId, new EventResponse(sessionId, true, null, statusMessage ));
    tokenService.handleResponse(event);
  }

  @When("the Account service replies with the status message {string}")
  public void theAccountServiceRepliesWithTheStatusMessage(String statusMessage) {
    Event event = new Event("AccountStatusResponse." + sessionId, new EventResponse(sessionId, true, null, statusMessage ));
    AccountService.handleGetStatus(event);
  }

  @Then("the status message is {string}")
  public void theStatusMessageIs(String statusMessage) {
    assertEquals(statusMessage, this.statusMessage.join());
  }
  
  @When("the service does not answer")
  public void theServiceDoesNotAnswer() throws InterruptedException {
	  statusMessage.join();
  }
 
  

}
