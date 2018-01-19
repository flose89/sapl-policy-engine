package io.sapl.grammar.sapl.impl;

import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;

import com.fasterxml.jackson.databind.JsonNode;

import io.sapl.api.interpreter.PolicyEvaluationException;
import io.sapl.grammar.sapl.Step;
import io.sapl.interpreter.EvaluationContext;

public class BasicIdentifierImplCustom extends io.sapl.grammar.sapl.impl.BasicIdentifierImpl {

	private static final String UNBOUND_VARIABLE = "Evaluation error. Variable '%s' is not defined.";

	private static final int HASH_PRIME_04 = 29;
	private static final int INIT_PRIME_02 = 5;

	@Override
	public JsonNode evaluate(EvaluationContext ctx, boolean isBody, JsonNode relativeNode)
			throws PolicyEvaluationException {
		if (!ctx.getVariableCtx().exists(getIdentifier())) {
			throw new PolicyEvaluationException(String.format(UNBOUND_VARIABLE, getIdentifier()));
		}
		JsonNode resultBeforeSteps = ctx.getVariableCtx().get(getIdentifier());
		return evaluateStepsFilterSubtemplate(resultBeforeSteps, steps, ctx, isBody, relativeNode);
	}

	@Override
	public int hash(Map<String, String> imports) {
		int hash = INIT_PRIME_02;
		hash = HASH_PRIME_04 * hash + Objects.hashCode(getClass().getTypeName());
		hash = HASH_PRIME_04 * hash + ((getFilter() == null) ? 0 : getFilter().hash(imports));
		hash = HASH_PRIME_04 * hash + Objects.hashCode(getIdentifier());
		for (Step step : getSteps()) {
			hash = HASH_PRIME_04 * hash + ((step == null) ? 0 : step.hash(imports));
		}
		hash = HASH_PRIME_04 * hash + ((getSubtemplate() == null) ? 0 : getSubtemplate().hash(imports));
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
		final BasicIdentifierImplCustom otherImpl = (BasicIdentifierImplCustom) other;
		if ((getFilter() == null) ? (getFilter() != otherImpl.getFilter())
				: !getFilter().isEqualTo(otherImpl.getFilter(), otherImports, imports)) {
			return false;
		}
		if ((getSubtemplate() == null) ? (getSubtemplate() != otherImpl.getSubtemplate())
				: !getSubtemplate().isEqualTo(otherImpl.getSubtemplate(), otherImports, imports)) {
			return false;
		}
		if (!Objects.equals(getIdentifier(), otherImpl.getIdentifier())) {
			return false;
		}
		if (getSteps().size() != otherImpl.getSteps().size()) {
			return false;
		}
		ListIterator<Step> left = getSteps().listIterator();
		ListIterator<Step> right = otherImpl.getSteps().listIterator();
		while (left.hasNext()) {
			Step lhs = left.next();
			Step rhs = right.next();
			if (!lhs.isEqualTo(rhs, otherImports, imports)) {
				return false;
			}
		}
		return true;
	}

}