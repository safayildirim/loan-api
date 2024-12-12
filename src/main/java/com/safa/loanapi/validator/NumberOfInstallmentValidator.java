package com.safa.loanapi.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Validator to ensure that the number of installments is one of the allowed values.
 *
 * <p>This class implements the {@link ConstraintValidator} interface and validates whether a given
 * {@code Integer} value matches one of the allowed values specified in the {@link NumberOfInstallment}
 * annotation.</p>
 *
 * <p>Example usage of {@code @NumberOfInstallment}:
 * <pre>
 * &#64;NumberOfInstallment(Values = {3, 6, 12, 24})
 * private Integer numberOfInstallments;
 * </pre>
 * </p>
 */

public class NumberOfInstallmentValidator implements ConstraintValidator<NumberOfInstallment, Integer> {
    private List<Integer> allowedNumberOfInstallments;

    /**
     * Initializes the validator with the allowed values specified in the {@link NumberOfInstallment} annotation.
     *
     * @param constraintAnnotation the annotation instance containing the allowed values.
     */
    @Override
    public void initialize(NumberOfInstallment constraintAnnotation) {
        int[] values = constraintAnnotation.Values();
        this.allowedNumberOfInstallments = IntStream.of(values).boxed().toList();
    }

    /**
     * Checks if the given number of installments is valid.
     *
     * @param value the number of installments to validate.
     * @param constraintValidatorContext context in which the constraint is evaluated.
     * @return {@code true} if the value is one of the allowed values; {@code false} otherwise.
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        return this.allowedNumberOfInstallments.contains(value);
    }
}
