package io.sapl.pdp.embedded;

import org.junit.Before;
import org.junit.Test;

import io.sapl.api.pdp.Decision;
import io.sapl.api.pdp.Response;
import io.sapl.api.pdp.multirequest.IdentifiableAction;
import io.sapl.api.pdp.multirequest.IdentifiableResource;
import io.sapl.api.pdp.multirequest.IdentifiableResponse;
import io.sapl.api.pdp.multirequest.IdentifiableSubject;
import io.sapl.api.pdp.multirequest.MultiRequest;
import io.sapl.api.pdp.multirequest.RequestElements;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class EmbeddedPolicyDecisionPointTest {

	private EmbeddedPolicyDecisionPoint pdp;

	@Before
	public void setUp() throws Exception {
		pdp = new EmbeddedPolicyDecisionPoint();
	}

	@Test
	public void decide_withEmptyRequest_shouldReturnDeny() {
		final Flux<Response> responseFlux = pdp.decide(null, null, null);
		StepVerifier.create(responseFlux)
				.expectNextMatches(response -> response.getDecision() == Decision.DENY)
				.thenCancel()
				.verify();
	}

	@Test
	public void decide_withAllowedAction_shouldReturnPermit() {
		final Flux<Response> responseFlux = pdp.decide("willi", "read", "something");
		StepVerifier.create(responseFlux)
				.expectNextMatches(response -> response.getDecision() == Decision.PERMIT)
				.thenCancel()
				.verify();
	}

	@Test
	public void decide_withForbiddenAction_shouldReturnDeny() {
		final Flux<Response> responseFlux = pdp.decide("willi", "write", "something");
		StepVerifier.create(responseFlux)
				.expectNextMatches(response -> response.getDecision() == Decision.DENY)
				.thenCancel()
				.verify();
	}

	@Test
	public void decide_withEmptyMultiRequest_shouldReturnIndeterminateResponse() {
		final MultiRequest multiRequest = new MultiRequest();

		final Flux<IdentifiableResponse> flux = pdp.decide(multiRequest);
		StepVerifier.create(flux)
				.expectNextMatches(response ->
						response.getRequestId() == null &&
						response.getResponse().equals(Response.indeterminate())
				)
				.thenCancel()
				.verify();
	}

	@Test
	public void decide_withMultiRequest_shouldReturnResponse() {
		final MultiRequest multiRequest = new MultiRequest();
		multiRequest.addSubject(new IdentifiableSubject("sub", "willi"));
		multiRequest.addAction(new IdentifiableAction("act", "read"));
		multiRequest.addResource(new IdentifiableResource("res", "something"));
		multiRequest.addRequest("req", new RequestElements("sub", "act", "res"));

		final Flux<IdentifiableResponse> flux = pdp.decide(multiRequest);
		StepVerifier.create(flux)
				.expectNextMatches(response ->
						response.getRequestId().equals("req") &&
						response.getResponse().equals(Response.permit())
				)
				.thenCancel()
				.verify();
	}

	@Test
	public void decide_withMultiRequestContainingTwoRequests_shouldReturnTwoResponses() {
		final MultiRequest multiRequest = new MultiRequest();
		multiRequest.addSubject(new IdentifiableSubject("sub", "willi"));
		multiRequest.addAction(new IdentifiableAction("act1", "read"));
		multiRequest.addAction(new IdentifiableAction("act2", "write"));
		multiRequest.addResource(new IdentifiableResource("res", "something"));
		multiRequest.addRequest("req1", new RequestElements("sub", "act1", "res"));
		multiRequest.addRequest("req2", new RequestElements("sub", "act2", "res"));

		final Flux<IdentifiableResponse> flux = pdp.decide(multiRequest);
		StepVerifier.create(flux)
				.expectNextMatches(response -> {
							if (response.getRequestId().equals("req1")) {
								return response.getResponse().equals(Response.permit());
							} else if (response.getRequestId().equals("req2")) {
								return response.getResponse().equals(Response.deny());
							} else {
								throw new IllegalStateException("Invalid request id: " + response.getRequestId());
							}
						}
				)
				.expectNextMatches(response -> {
							if (response.getRequestId().equals("req1")) {
								return response.getResponse().equals(Response.permit());
							} else if (response.getRequestId().equals("req2")) {
								return response.getResponse().equals(Response.deny());
							} else {
								throw new IllegalStateException("Invalid request id: " + response.getRequestId());
							}
						}
				)
				.thenCancel()
				.verify();
	}
}
