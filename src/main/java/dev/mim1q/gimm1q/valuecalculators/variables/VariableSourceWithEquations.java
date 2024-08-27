package dev.mim1q.gimm1q.valuecalculators.variables;

import java.util.Map;

/**
 * Your custom {@link VariableSource} should extend this interface if it might contain an equation that needs to be
 * set up before it can be evaluated, by providing the values of variables defined through
 * {@link VariableSourceWithDependencies#getPotentialVariableNames()}
 */
public interface VariableSourceWithEquations extends VariableSourceWithDependencies {
    void setupExpressionBuilder(Map<String, Double> previousVariables);
}
