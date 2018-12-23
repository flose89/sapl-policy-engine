/**
 * Copyright © 2017 Dominic Heutelbeck (dheutelbeck@ftk.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.sapl.grammar.sapl.impl;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import io.sapl.api.interpreter.PolicyEvaluationException;
import io.sapl.interpreter.EvaluationContext;
import org.eclipse.emf.ecore.EObject;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

public class NotImplCustom extends io.sapl.grammar.sapl.impl.NotImpl {

	private static final int HASH_PRIME_09 = 47;
	private static final int INIT_PRIME_01 = 3;

	@Override
	public JsonNode evaluate(EvaluationContext ctx, boolean isBody, JsonNode relativeNode) throws PolicyEvaluationException {
		final JsonNode expressionResult = expression.evaluate(ctx, isBody, relativeNode);
		assertBoolean(expressionResult);
		return JSON.booleanNode(!expressionResult.asBoolean());
	}

	@Override
	public Flux<JsonNode> reactiveEvaluate(EvaluationContext ctx, boolean isBody, JsonNode relativeNode) {
		return expression.reactiveEvaluate(ctx, isBody, relativeNode)
				.map(result -> {
					try {
						assertBoolean(result);
						return (JsonNode) JSON.booleanNode(!result.asBoolean());
					}
					catch (PolicyEvaluationException e) {
						throw Exceptions.propagate(e);
					}
				})
				.distinctUntilChanged();

	}

	@Override
	public int hash(Map<String, String> imports) {
		int hash = INIT_PRIME_01;
		hash = HASH_PRIME_09 * hash + Objects.hashCode(getClass().getTypeName());
		hash = HASH_PRIME_09 * hash + getExpression().hash(imports);
		return hash;
	}

	@Override
	public boolean isEqualTo(EObject other, Map<String, String> otherImports, Map<String, String> imports) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		final NotImplCustom otherImpl = (NotImplCustom) other;
		return (getExpression() == null) ? (getExpression() == otherImpl.getExpression())
				: getExpression().isEqualTo(otherImpl.getExpression(), otherImports, imports);
	}

}
