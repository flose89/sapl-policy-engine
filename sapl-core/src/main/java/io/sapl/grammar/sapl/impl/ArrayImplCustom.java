package io.sapl.grammar.sapl.impl;

import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import io.sapl.api.interpreter.PolicyEvaluationException;
import io.sapl.grammar.sapl.Expression;
import io.sapl.interpreter.EvaluationContext;

public class ArrayImplCustom extends io.sapl.grammar.sapl.impl.ArrayImpl {

	private static final int HASH_PRIME_06 = 37;
	private static final int INIT_PRIME_02 = 5;

	@Override
	public JsonNode evaluate(EvaluationContext ctx, boolean isBody, JsonNode relativeNode)
			throws PolicyEvaluationException {
		ArrayNode result = JsonNodeFactory.instance.arrayNode(getItems().size());
		for (Expression item : getItems()) {
			result.add(item.evaluate(ctx, isBody, relativeNode));
		}
		return result;
	}

	@Override
	public int hash(Map<String, String> imports) {
		int hash = INIT_PRIME_02;
		hash = HASH_PRIME_06 * hash + Objects.hashCode(getClass().getTypeName());
		for (Expression expression : getItems()) {
			hash = HASH_PRIME_06 * hash + ((expression == null) ? 0 : expression.hash(imports));
		}
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
		final ArrayImplCustom otherImpl = (ArrayImplCustom) other;
		if (getItems().size() != otherImpl.getItems().size()) {
			return false;
		}
		ListIterator<Expression> left = getItems().listIterator();
		ListIterator<Expression> right = otherImpl.getItems().listIterator();
		while (left.hasNext()) {
			Expression lhs = left.next();
			Expression rhs = right.next();
			if (!lhs.isEqualTo(rhs, otherImports, imports)) {
				return false;
			}
		}
		return true;
	}

}