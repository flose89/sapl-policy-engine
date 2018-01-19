package io.sapl.grammar.sapl.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;

import com.fasterxml.jackson.databind.JsonNode;

import io.sapl.api.interpreter.PolicyEvaluationException;
import io.sapl.interpreter.EvaluationContext;
import io.sapl.interpreter.selection.AbstractAnnotatedJsonNode;
import io.sapl.interpreter.selection.ArrayResultNode;
import io.sapl.interpreter.selection.JsonNodeWithParentObject;
import io.sapl.interpreter.selection.ResultNode;

public class AttributeUnionStepImplCustom extends AttributeUnionStepImpl {

	private static final String UNION_TYPE_MISMATCH = "Type mismatch.";

	private static final int HASH_PRIME_03 = 23;
	private static final int INIT_PRIME_01 = 3;

	@Override
	public ResultNode apply(AbstractAnnotatedJsonNode previousResult, EvaluationContext ctx, boolean isBody,
			JsonNode relativeNode) throws PolicyEvaluationException {
		if (!previousResult.getNode().isObject()) {
			throw new PolicyEvaluationException(UNION_TYPE_MISMATCH);
		}
		ArrayList<AbstractAnnotatedJsonNode> resultList = new ArrayList<>();
		JsonNode previousResultNode = previousResult.getNode();
		HashSet<String> attributes = new HashSet<>(getAttributes());

		Iterator<String> iterator = previousResultNode.fieldNames();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (attributes.contains(key)) {
				resultList.add(new JsonNodeWithParentObject(previousResultNode.get(key), previousResultNode, key));
			}
		}
		return new ArrayResultNode(resultList);
	}

	@Override
	public ResultNode apply(ArrayResultNode previousResult, EvaluationContext ctx, boolean isBody,
			JsonNode relativeNode)
			throws PolicyEvaluationException {
		throw new PolicyEvaluationException(UNION_TYPE_MISMATCH);
	}

	@Override
	public int hash(Map<String, String> imports) {
		int hash = INIT_PRIME_01;
		hash = HASH_PRIME_03 * hash + Objects.hashCode(getClass().getTypeName());
		for (String attribute : getAttributes()) {
			hash = HASH_PRIME_03 * hash + Objects.hashCode(attribute);
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
		final AttributeUnionStepImplCustom otherImpl = (AttributeUnionStepImplCustom) other;
		if (getAttributes().size() != otherImpl.getAttributes().size()) {
			return false;
		}
		ListIterator<String> left = getAttributes().listIterator();
		ListIterator<String> right = otherImpl.getAttributes().listIterator();
		while (left.hasNext()) {
			String lhs = left.next();
			String rhs = right.next();
			if (!Objects.equals(lhs, rhs)) {
				return false;
			}
		}
		return true;
	}

}