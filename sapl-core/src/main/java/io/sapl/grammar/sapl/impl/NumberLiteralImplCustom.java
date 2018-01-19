package io.sapl.grammar.sapl.impl;

import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import io.sapl.interpreter.EvaluationContext;

public class NumberLiteralImplCustom extends io.sapl.grammar.sapl.impl.NumberLiteralImpl {

	private static final int HASH_PRIME_02 = 19;
	private static final int INIT_PRIME_02 = 5;

	@Override
	public JsonNode evaluate(EvaluationContext ctx, boolean isBody, JsonNode relativeNode) {
		return JsonNodeFactory.instance.numberNode(getNumber());
	}

	@Override
	public int hash(Map<String, String> imports) {
		int hash = INIT_PRIME_02;
		hash = HASH_PRIME_02 * hash + Objects.hashCode(getClass().getTypeName());
		hash = HASH_PRIME_02 * hash + Objects.hashCode(getNumber());
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
		final NumberLiteralImplCustom otherImpl = (NumberLiteralImplCustom) other;
		return Objects.equals(getNumber(), otherImpl.getNumber());
	}

}