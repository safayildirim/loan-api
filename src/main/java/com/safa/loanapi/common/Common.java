package com.safa.loanapi.common;

public class Common {
    /**
     * Rounds a given double value to two decimal places.
     *
     * <p>This method multiplies the input value by 100, rounds it to the nearest integer using {@link Math#round},
     * and then divides the result by 100 to return a value rounded to two decimal places.</p>
     *
     * @param amount the double value to be rounded.
     * @return the input value rounded to two decimal places.
     *
     */
    public static double roundTwoDecimal(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}
