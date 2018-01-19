package io.sapl.grammar.sapl.impl;

import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import io.sapl.interpreter.EvaluationContext;

public class TrueLiteralImplCustom extends TrueLiteralImpl {

	private static final int HASH_PRIME_05 = 31;
	private static final int INIT_PRIME_02 = 5;

	@Override
	public JsonNode evaluate(EvaluationContext ctx, boolean isBody, JsonNode relativeNode) {
		return JsonNodeFactory.instance.booleanNode(true);
	}

	@Override
	public int hash(Map<String, String> imports) {
		int hash = INIT_PRIME_02;
		hash = HASH_PRIME_05 * hash + Objects.hashCode(getClass().getTypeName());
		return hash;
	}

	@Override
	public boolean isEqualTo(EObject other, Map<String, String> otherImports, Map<String, String> imports) {
		if (this == other) {
			return true;
		}
		return !(other == null || getClass() != other.getClass());
	}

}