package io.sapl.grammar.sapl.impl;

import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;

import com.fasterxml.jackson.databind.JsonNode;

import io.sapl.api.interpreter.PolicyEvaluationException;
import io.sapl.interpreter.EvaluationContext;

public class MinusImplCustom extends io.sapl.grammar.sapl.impl.MinusImpl {

	private static final int HASH_PRIME_12 = 61;
	private static final int INIT_PRIME_01 = 3;

	@Override
	public JsonNode evaluate(EvaluationContext ctx, boolean isBody, JsonNode relativeNode)
			throws PolicyEvaluationException {
		JsonNode left = getLeft().evaluate(ctx, isBody, relativeNode);
		JsonNode right = getRight().evaluate(ctx, isBody, relativeNode);
		assertNumbers(left, right);
		return JSON.numberNode(left.decimalValue().subtract(right.decimalValue()));
	}

	@Override
	public int hash(Map<String, String> imports) {
		int hash = INIT_PRIME_01;
		hash = HASH_PRIME_12 * hash + Objects.hashCode(getClass().getTypeName());
		hash = HASH_PRIME_12 * hash + ((getLeft() == null) ? 0 : getLeft().hash(imports));
		hash = HASH_PRIME_12 * hash + ((getRight() == null) ? 0 : getRight().hash(imports));
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
		final MinusImplCustom otherImpl = (MinusImplCustom) other;
		if ((getLeft() == null) ? (getLeft() != otherImpl.getLeft())
				: !getLeft().isEqualTo(otherImpl.getLeft(), otherImports, imports)) {
			return false;
		}
		return (getRight() == null) ? (getRight() == otherImpl.getRight())
				: getRight().isEqualTo(otherImpl.getRight(), otherImports, imports);
	}

}