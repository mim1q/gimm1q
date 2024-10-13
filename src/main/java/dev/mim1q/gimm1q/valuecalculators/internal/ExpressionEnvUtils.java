package dev.mim1q.gimm1q.valuecalculators.internal;

import redempt.crunch.functional.ExpressionEnv;

public class ExpressionEnvUtils {
    public static ExpressionEnv createEnvWithFunctions(String[] variableNames) {
        ExpressionEnv env = new ExpressionEnv().setVariableNames(variableNames);
        env.addFunction("if", 3, (args -> args[0] != 0 ? args[1] : args[2]));
        return env;
    }
}
