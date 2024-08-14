package dev.mim1q.gimm1q.valuecalculators.variables;

/**
 * Your custom {@link VariableSource} should extend this interface if it might depend on other {@link VariableSource}s.
 * This way they will be evaluated correctly before the main {@link VariableSource}.
 */
public interface VariableSourceWithDependencies extends VariableSource {
    /**
     * Returns the potential names of the variable dependencies of this {@link VariableSource}.
     * These names <b>must include</b> all variable names that need to be evaluated before this, but they can
     * include other strings that are not variable names - they will be ignored.
     *
     * @return an array of the potential names of the variable dependencies of this variable source
     */
    String[] getPotentialVariableNames();
}
