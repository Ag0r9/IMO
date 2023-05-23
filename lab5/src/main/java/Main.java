import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class Main {
    static class Cycles {
        List<Integer> first_cycle;
        List<Integer> second_cycle;

        Cycles(List<Integer> first_cycle, List<Integer> second_cycle) {
            this.first_cycle = first_cycle;
            this.second_cycle = second_cycle;
        }
    }

    static int size = 100;

    public static void main(String[] args) throws IOException {
        HelperFunctions.Node[] nodes = new HelperFunctions.Node[size];
        HelperFunctions.load_data(nodes, "kroA200.tsp");
        double[][] distances = HelperFunctions.calculate_distance(nodes);
        Random rand = new Random();
        int first_id, second_id;
        first_id = rand.nextInt(size);
        second_id = HelperFunctions.find_second_starting_node(first_id, distances);
        Cycles cycles = hybrid_evolutionary(distances);

        //Cycles cycles = generate_greedy_cycles(distances, first_id, second_id);
        //cycles = destroy_and_repair(distances, cycles);

        System.out.println(HelperFunctions.get_total_dist(distances, cycles));
        cycles.first_cycle.forEach(i -> System.out.print(i + " "));
        System.out.println();
        cycles.second_cycle.forEach(i -> System.out.print(i + " "));
    }

    private static Cycles hybrid_evolutionary(double[][] distances) {
        Random rand = new Random();
        List<Cycles> list_of_cycles = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            int first_id = rand.nextInt(size);
            int second_id = HelperFunctions.find_second_starting_node(first_id, distances);
            list_of_cycles.add(GreedyCycle.generate_greedy_cycles(distances, first_id, second_id));
        }
        list_of_cycles.sort(Comparator.comparingDouble(c -> HelperFunctions.get_total_dist(distances, c)));
        //Utwórz początkową populację

        //poszukaj wspólnych ścieżek pomiędzy dwoma rozwiązaniami
        Set<List<Integer>> same_paths = find_same_paths(list_of_cycles.get(0), list_of_cycles.get(1));

        List<Integer> not_used = IntStream.range(0, size).boxed().collect(Collectors.toList());
        for (List<Integer> x : same_paths) {
            for (int i = 1; i < x.size() - 1; i++) {
                not_used.remove(x.get(i));
            }
        }
        //złóż ścieżkę


        //lokalne przeszukiwanie

        return list_of_cycles.get(0);
    }

    private static Set<List<Integer>> find_same_paths(Cycles first_solution, Cycles second_solution) {
        Set<List<Integer>> same_paths = new HashSet<>();
        same_paths = perform_findind_between_two_cycles(same_paths, first_solution.first_cycle, second_solution.first_cycle);
        same_paths = perform_findind_between_two_cycles(same_paths, first_solution.first_cycle, second_solution.second_cycle);
        same_paths = perform_findind_between_two_cycles(same_paths, first_solution.second_cycle, second_solution.first_cycle);
        same_paths = perform_findind_between_two_cycles(same_paths, first_solution.second_cycle, second_solution.second_cycle);
        same_paths = eliminateSublists(same_paths);
        return same_paths;
    }

    private static Set<List<Integer>> eliminateSublists(Set<List<Integer>> setOfLists) {
        Set<List<Integer>> filteredSet = new HashSet<>();
        for (List<Integer> currentList : setOfLists) {
            boolean isSubset = false;
            for (List<Integer> otherList : setOfLists) {
                if (currentList != otherList && otherList.containsAll(currentList)) {
                    isSubset = true;
                    break;
                }
            }
            if (!isSubset) {
                filteredSet.add(currentList);
            }
        }
        return filteredSet;
    }

    private static Set<List<Integer>> perform_findind_between_two_cycles(Set<List<Integer>> commonPaths, List<Integer> cycle1, List<Integer> cycle2) {

        for (int i = 0; i < cycle1.size() - 2; i++) {
            int node1 = cycle1.get(i);
            int node2 = cycle1.get(i + 1);

            for (int j = 0; j < cycle2.size() - 2; j++) {
                int otherNode1 = cycle2.get(j);
                int otherNode2 = cycle2.get(j + 1);

                if (node1 == otherNode1 && node2 == otherNode2) {
                    List<Integer> commonPath = new ArrayList<>(cycle1.subList(i, i + 2));
                    int k = i + 2;
                    int l = j + 2;

                    while (k < cycle1.size() && l < cycle2.size()) {
                        int nextNode1 = cycle1.get(k);
                        int nextNode2 = cycle2.get(l);

                        if (nextNode1 == nextNode2) {
                            commonPath.add(nextNode1);
                            k++;
                            l++;
                        } else {
                            break; // Break the path if nodes don't match
                        }
                    }
                    commonPaths.add(commonPath);
                }
            }
        }
        return commonPaths;
    }
}