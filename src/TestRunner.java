/**
 * Minimal test runner for characterization tests.
 *
 * Deliberately simple: no framework dependency, no annotations.
 * Each test suite creates one instance, calls check() for every
 * assertion, then calls summary() at the end.
 *
 * Output format is human-readable and grep-friendly:
 *   PASS  CardTest: color of R5 is R
 *   FAIL  CardTest: color of G+2 is G   <-- assertion failed
 *
 * summary() prints a one-line result and throws RuntimeException
 * if any check failed, so scripts/test.sh exits non-zero on failure.
 */
public class TestRunner {

    private final String suiteName;
    private int passed = 0;
    private int failed = 0;

    public TestRunner(String suiteName) {
        this.suiteName = suiteName;
    }

    /**
     * Assert that {@code condition} is true.
     * Prints PASS or FAIL and records the result.
     *
     * @param condition the assertion to evaluate
     * @param label     short human-readable description of what is being checked
     */
    public void check(boolean condition, String label) {
        if (condition) {
            System.out.println("PASS  " + suiteName + ": " + label);
            passed++;
        } else {
            System.out.println("FAIL  " + suiteName + ": " + label);
            failed++;
        }
    }

    /**
     * Print a summary line.
     * Throws {@link RuntimeException} when any check has failed so the
     * calling test main() exits with a non-zero status, which causes
     * scripts/test.sh to report failure.
     */
    public void summary() {
        int total = passed + failed;
        System.out.println("\n" + suiteName + ": " + passed + "/" + total + " passed"
                + (failed > 0 ? "  *** " + failed + " FAILED ***" : ""));
        if (failed > 0) {
            throw new RuntimeException(suiteName + " had " + failed + " failing check(s).");
        }
    }
}