package dev.mim1q.gimm1q.valuecalculators.variables;

import java.util.*;
import java.util.stream.Collectors;

public class VariableSourceSortUtil {
    public static List<Map.Entry<String, VariableSource>> sortVariables(
        List<Map.Entry<String, VariableSource>> variables
    ) {
        // Step 1: Build the dependency graph
        var variableNames = variables.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
        Map<String, List<String>> dependencyGraph = new HashMap<>();
        Set<String> allVariables = new HashSet<>();

        for (Map.Entry<String, VariableSource> entry : variables) {
            var varName = entry.getKey();
            var source = entry.getValue();
            allVariables.add(varName);

            if (source instanceof VariableSourceTypes.Equation equation) {
                dependencyGraph.put(
                    varName,
                    Arrays.stream(equation.potentialVariables).filter(variableNames::contains).toList()
                );
            } else {
                dependencyGraph.put(varName, Collections.emptyList());
            }
        }

        // Step 2: Perform topological sorting on the graph and check for cycles
        var sortedOrder = topologicalSort(dependencyGraph, allVariables);

        if (sortedOrder == null) {
            throw new IllegalStateException("Cycle detected in variable dependencies.");
        }

        // Step 3: Rebuild the sorted list of variables
        List<Map.Entry<String, VariableSource>> sortedVariables = new ArrayList<>();
        for (String varName : sortedOrder) {
            for (Map.Entry<String, VariableSource> entry : variables) {
                if (entry.getKey().equals(varName)) {
                    sortedVariables.add(entry);
                    break;
                }
            }
        }

        return sortedVariables;
    }

    private static List<String> topologicalSort(
        Map<String, List<String>> dependencyGraph,
        Set<String> allVariables
    ) {
        List<String> sortedOrder = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> inStack = new HashSet<>();

        for (var var : allVariables) {
            if (!visited.contains(var)) {
                if (!topologicalSortUtil(var, dependencyGraph, visited, inStack, sortedOrder)) {
                    return null; // Cycle detected
                }
            }
        }

        return sortedOrder;
    }

    private static boolean topologicalSortUtil(
        String var,
        Map<String, List<String>> dependencyGraph,
        Set<String> visited,
        Set<String> inStack,
        List<String> sortedOrder
    ) {
        visited.add(var);
        inStack.add(var);

        for (var dependency : dependencyGraph.getOrDefault(var, Collections.emptyList())) {
            if (inStack.contains(dependency)) {
                return false; // Cycle detected
            }
            if (!visited.contains(dependency)) {
                if (!topologicalSortUtil(dependency, dependencyGraph, visited, inStack, sortedOrder)) {
                    return false;
                }
            }
        }

        inStack.remove(var);
        sortedOrder.add(var);
        return true;
    }
}
