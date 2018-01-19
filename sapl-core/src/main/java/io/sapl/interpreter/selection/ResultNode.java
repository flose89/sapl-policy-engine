package io.sapl.interpreter.selection;

import com.fasterxml.jackson.databind.JsonNode;

import io.sapl.api.interpreter.PolicyEvaluationException;
import io.sapl.grammar.sapl.Arguments;
import io.sapl.grammar.sapl.Step;
import io.sapl.interpreter.EvaluationContext;

/**
 * Interface representing a node in a selection result tree.
 *
 * The node can be a JsonNode which is annotated with its parent node in a
 * JsonTree (which is an ObjectNode or an ArrayNode) and a unique access method
 * (i.e., attribute name or array index). The node can also be a result array
 * containing zero to many JsonNodes with annotations.
 *
 * The annotations allow both for modifying the selected node in a Jackson JSON
 * tree or checking whether selected nodes are not only equal, but also
 * identical inside a Jackson JSON tree.
 */
public interface ResultNode {

	/**
	 * Removes all annotations from the selection result tree and returns the root
	 * JsonNode.
	 *
	 * @return the root JsonNode
	 */
	JsonNode asJsonWithoutAnnotations();

	/**
	 * Checks if the node is a result array (which can contain multiple
	 * AnnotatedJsonNodes as children).
	 *
	 * @return true, if the node is a result array
	 */
	boolean isResultArray();

	/**
	 * Checks if the node is an annotated JsonNode with no parents.
	 *
	 * @return true, if the node is an annotated JsonNode with no parents
	 */
	boolean isNodeWithoutParent();

	/**
	 * Checks if the node is an annotated JsonNode with an object as parent node.
	 *
	 * @return true, if the node is an annotated JsonNode with an object as parent
	 *         node
	 */
	boolean isNodeWithParentObject();

	/**
	 * Checks if the node is an annotated JsonNode with an array as parent node.
	 *
	 * @return true, if the node is an annotated JsonNode with an array as parent
	 *         node
	 */
	boolean isNodeWithParentArray();

	/**
	 * Removes the selected JsonNode from its parent. If the selected node is an
	 * array, the param each can be used to specify that each element should be
	 * removed from this array.
	 *
	 * @param each
	 *            true, if the selection should be treated as an array and the
	 *            remove operation should be applied to each item
	 * @throws PolicyEvaluationException
	 *             in case the remove operation could not be applied
	 */
	void removeFromTree(boolean each) throws PolicyEvaluationException;

	/**
	 * Applies a function to the selected JsonNode. If the selected node is an
	 * array, the param each can be used to specify that the function should be
	 * applied to each item of this array.
	 *
	 * @param function
	 *            name of the function
	 * @param arguments
	 *            arguments to pass to the function
	 * @param each
	 *            true, if the selection should be treated as an array and the
	 *            function should be applied to each of its items
	 * @param ctx
	 *            the evaluation context
	 * @throws PolicyEvaluationException
	 *             in case an error occurs during application of the filter function
	 *             or in case the function is not applicable
	 */
	void applyFunction(String function, Arguments arguments, boolean each, EvaluationContext ctx)
			throws PolicyEvaluationException;

	/**
	 * Applies a step to the result node and returns a new result node.
	 *
	 * @param step
	 *            the step to apply
	 * @param ctx
	 *            the evaluation context
	 * @param isBody
	 *            true if the step is applied within the policy body
	 * @param relativeNode
	 *            the node a relative expression evaluates to
	 * @return the result node resulting from application of the step
	 * @throws PolicyEvaluationException
	 *             in case an error occurs during application of the step
	 */
	ResultNode applyStep(Step step, EvaluationContext ctx, boolean isBody, JsonNode relativeNode)
			throws PolicyEvaluationException;
}