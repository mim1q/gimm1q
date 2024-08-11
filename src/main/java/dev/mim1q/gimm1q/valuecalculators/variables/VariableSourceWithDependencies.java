package dev.mim1q.gimm1q.valuecalculators.variables;

public interface VariableSourceWithDependencies extends VariableSource {
    String[] getPotentialVariableNames();
}
