/*
 * Copyright © 2017-2021 Dominic Heutelbeck (dominic@heutelbeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sapl.interpreter.combinators;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import io.sapl.api.pdp.AuthorizationDecision;
import io.sapl.api.pdp.Decision;

public class ObligationAdviceCollector {

	private final static JsonNodeFactory JSON = JsonNodeFactory.instance;

	Map<Decision, ArrayNode> obligations = new EnumMap<>(Decision.class);
	Map<Decision, ArrayNode> advices = new EnumMap<>(Decision.class);

	public ObligationAdviceCollector() {
		obligations.put(Decision.DENY, JSON.arrayNode());
		obligations.put(Decision.PERMIT, JSON.arrayNode());
		advices.put(Decision.DENY, JSON.arrayNode());
		advices.put(Decision.PERMIT, JSON.arrayNode());
	}

	public void registerDecisionsObligationsAndAdvices(AuthorizationDecision authzDecision) {
		if (authzDecision.getDecision() != Decision.PERMIT && authzDecision.getDecision() != Decision.DENY)
			return;
		registerObligationIfPresent(authzDecision);
		registerAdviceIfPresent(authzDecision);
	}

	private void registerAdviceIfPresent(AuthorizationDecision authzDecision) {
		if (authzDecision.getAdvices().isPresent())
			advices.get(authzDecision.getDecision()).addAll(authzDecision.getAdvices().get());
	}

	private void registerObligationIfPresent(AuthorizationDecision authzDecision) {
		if (authzDecision.getObligations().isPresent())
			obligations.get(authzDecision.getDecision()).addAll(authzDecision.getObligations().get());
	}

	public void add(AuthorizationDecision authzDecision) {
		registerDecisionsObligationsAndAdvices(authzDecision);
	}

	public Optional<ArrayNode> getObligations(Decision decision) {
		if ((decision == Decision.PERMIT || decision == Decision.DENY) && obligations.get(decision).size() > 0)
			return Optional.of(obligations.get(decision));
		return Optional.empty();
	}

	public Optional<ArrayNode> getAdvices(Decision decision) {
		if ((decision == Decision.PERMIT || decision == Decision.DENY) && advices.get(decision).size() > 0)
			return Optional.of(advices.get(decision));
		return Optional.empty();
	}

}
