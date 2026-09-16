package gm.engine.lmsr;

/**
 * Pure math for the Logarithmic Market Scoring Rule (LMSR), as described in Appendix A of the assignment.
 */
public final class LmsrCalculator {

    private LmsrCalculator() {
    }

    /** Cost function C(q) = b * ln( sum_i e^(q_i / b) ). */
    public static double cost(double[] quantities, int b) {
        double sumExp = 0.0;
        for (double q : quantities) {
            sumExp += Math.exp(q / b);
        }
        return b * Math.log(sumExp);
    }

    /** Instantaneous price of option i = e^(q_i / b) / sum_j e^(q_j / b). Always between 0 and 1. */
    public static double price(double[] quantities, int b, int optionIndex) {
        double sumExp = 0.0;
        for (double q : quantities) {
            sumExp += Math.exp(q / b);
        }
        return Math.exp(quantities[optionIndex] / b) / sumExp;
    }
}
