package io.sapl.prp.embedded;

import java.io.IOException;

import org.junit.Test;

import io.sapl.api.functions.FunctionException;
import io.sapl.api.interpreter.PolicyEvaluationException;
import io.sapl.api.pip.AttributeException;
import io.sapl.pdp.embedded.EmbeddedPolicyDecisionPoint;

public class EmbeddedPRPTest {

	public static double nanoToMs(double nanoseconds) {
		return nanoseconds / 1000000.0D;
	}

	public static double nanoToS(double nanoseconds) {
		return nanoseconds / 1000000000.0D;
	}

	@Test
	public void testTest() throws IOException, PolicyEvaluationException, AttributeException, FunctionException {
		// long startpdp = System.nanoTime();
		EmbeddedPolicyDecisionPoint pdp = new EmbeddedPolicyDecisionPoint();
		// long endpdp = System.nanoTime();
		// System.out.println("Measuring PDP and PRP initialization:");
		// System.out.println("Start : " + startpdp);
		// System.out.println("End : " + endpdp);
		// System.out.println("Total runtime : " + nanoToS(endpdp - startpdp) + " s");
		// System.out.println();

		// long start = System.nanoTime();
		int RUNS = 100;
		for (int i = 0; i < RUNS; i++) {
			// Response response =
			pdp.decide("willi", "read", "something");
			// System.out.println("response: " + response.toString());
		}
		// long end = System.nanoTime();
		// System.out.println("Measuring requests:");
		// System.out.println("Start : " + start);
		// System.out.println("End : " + end);
		// System.out.println("Runs : " + RUNS);
		// System.out.println("Total runtime : " + nanoToS(end - start) + " s");
		//
		// System.out.println(
		// "Avg. runtime : " + nanoToMs((end - start) / RUNS) + " ms");

	}
}