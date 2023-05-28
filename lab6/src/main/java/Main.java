import java.util.*;
import java.io.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class Main {
    static class Cycles  implements Cloneable {
        List<Integer> first_cycle;
        List<Integer> second_cycle;

        Cycles(List<Integer> first_cycle, List<Integer> second_cycle) {
            this.first_cycle = first_cycle;
            this.second_cycle = second_cycle;
        }

        @Override
        public Cycles clone() {
            List<Integer> clonedFirstCycle = new ArrayList<>(first_cycle);
            List<Integer> clonedSecondCycle = new ArrayList<>(second_cycle);
            return new Cycles(clonedFirstCycle, clonedSecondCycle);
        }
    }

    static int size = 200;
    static int population = 15;

    public static void main(String[] args) throws IOException {
        HelperFunctions.Node[] nodes = new HelperFunctions.Node[size];
        HelperFunctions.load_data(nodes, "kroA200.tsp");
        double[][] distances = HelperFunctions.calculate_distance(nodes);
        Cycles cycles = hybrid_evolutionary(distances);

        System.out.println(HelperFunctions.get_total_dist(distances, cycles));
        System.out.println();
        cycles = DestroyAndRepair.destroy_and_repair(distances,cycles);
        System.out.println(HelperFunctions.get_total_dist(distances, cycles));


        cycles.first_cycle.forEach(i -> System.out.print(i + " "));
        System.out.println();
        cycles.second_cycle.forEach(i -> System.out.print(i + " "));
    }

    public static boolean hasDuplicates(List<Integer> list) {
        Set<Integer> uniqueElements = new HashSet<>();
        for (int element : list) {
            if (uniqueElements.contains(element)) {
                return true; // Found a duplicate
            }
            uniqueElements.add(element);
        }
        return false; // No duplicates found
    }

    private static Cycles hybrid_evolutionary(double[][] distances) {
        Random rand = new Random();
        List<Cycles> list_of_cycles = new ArrayList<>();
        //Utwórz początkową populację
        for (int i = 0; i < population; i++) {
            int first_id = rand.nextInt(size);
            int second_id = HelperFunctions.find_second_starting_node(first_id, distances);
            list_of_cycles.add(GreedyCycle.generate_greedy_cycles(distances, first_id, second_id));
        }

        list_of_cycles.sort(Comparator.comparingDouble(c -> HelperFunctions.get_total_dist(distances, c)));
        System.out.println(HelperFunctions.get_total_dist(distances,list_of_cycles.get(0)));
        System.out.println();
        for (int q = 0; q < 60; q++) {
            //posortuj rozwiazania wg jakosci
            list_of_cycles.sort(Comparator.comparingDouble(c -> HelperFunctions.get_total_dist(distances, c)));

            //usun rozwiazania, jesli mają te samą wartość długości
            list_of_cycles = remove_same_dist_solutions(distances, list_of_cycles);

            //poszukaj wspólnych ścieżek pomiędzy dwoma wylosowanymi rozwiązaniami
            int first_id = rand.nextInt(3);
            int second_id = first_id;
            while (second_id == first_id)
                second_id = rand.nextInt(list_of_cycles.size());

            List<List<Integer>> same_paths = find_same_paths(list_of_cycles.get(first_id), list_of_cycles.get(second_id));
            List<Integer> not_used = IntStream.range(0, size).boxed().collect(toList());
            for (List<Integer> x : same_paths) {
                for (int i = 0; i < x.size(); i++) {
                    not_used.remove(x.get(i));
                }
            }

            //znajdz najbardziej od siebie oddalone punkty startowe, ktore sa albo wolnymi wierzcholkami albo krawedziami sciezki
            //i utworz poczatkowy cykl skladajacy sie z 1 punktu albo ze sciezki
            List<Integer> two_most_distant_nodes_from_available = find_two_most_distant_nodes_from_available(distances, not_used);

            List<Integer> cycle1 = new ArrayList<>();
            List<Integer> cycle2 = new ArrayList<>();
            int first_start = two_most_distant_nodes_from_available.get(0);
            int second_start = two_most_distant_nodes_from_available.get(1);

            cycle1.add(first_start);
            cycle1.add(first_start);

            cycle2.add(second_start);
            cycle2.add(second_start);

            not_used.removeAll(Arrays.asList(second_start, first_start));

            //dodawaj do cykli sciezki w taki sposob, ze dodajemy sciezke do blizszego mu cyklu,
            // pod warunkiem, ze nie przekraczamy polowy wierzcholkow
            if (cycle_creation_paths(distances, same_paths, cycle1, cycle2) == -1) {
                continue;
            }

            //dodaj wolne wierzcholki metoda zachlannego cyklu
            while (cycle1.size() != size / 2 + 1 && cycle2.size() != size / 2 + 1) {
                GreedyCycle.cycle_creation(distances, not_used, cycle1);
                GreedyCycle.cycle_creation(distances, not_used, cycle2);
            }
            while (cycle1.size() != size / 2 + 1)
                GreedyCycle.cycle_creation(distances, not_used, cycle1);

            while (cycle2.size() != size / 2 + 1)
                GreedyCycle.cycle_creation(distances, not_used, cycle2);

            //ten warunek sprawia ze dziala, bo sa przez sciezki duplikaty czasem, nie wiem czemu,
            //a z duplikatami steepest głupieje
            if (hasDuplicates(cycle1.subList(1, cycle1.size())) || hasDuplicates(cycle2.subList(1, cycle2.size())) || hasDuplicates(Stream.concat(cycle1.subList(1, cycle1.size() - 1).stream(), cycle2.subList(1, cycle2.size() - 1).stream()).collect(toList())))
                continue;

            Cycles x = new Cycles(cycle1, cycle2);

            //zakomentuj linijkę niżej, by usunąć lokalne przeszukiwanie
            x = Steepest.steepest(distances, x);
            list_of_cycles.add(x);
            var d = HelperFunctions.get_total_dist(distances, x);
            if(d<32000.0){
                System.out.println(d);
            }
        }
        return list_of_cycles.get(0);
    }

    private static List<Cycles> remove_same_dist_solutions(double[][] dist, List<Cycles> list_of_cycles) {
        var to_remove = new ArrayList<Cycles>();
        for (int i = 1; i < list_of_cycles.size(); i++) {
            if (HelperFunctions.get_total_dist(dist, list_of_cycles.get(i - 1)) == HelperFunctions.get_total_dist(dist, list_of_cycles.get(i))) {
                to_remove.add(list_of_cycles.get(i - 1));
            }
        }
        list_of_cycles.removeAll(to_remove);
        while (list_of_cycles.size() > population) {
            list_of_cycles.remove(list_of_cycles.get(list_of_cycles.size() - 1));
        }
        return list_of_cycles;
    }

    static int cycle_creation_paths(double[][] dist, List<List<Integer>> not_used_paths, List<Integer> cycle1, List<Integer> cycle2) {
        for (List<Integer> i : not_used_paths) {
            double min_dist = Double.MAX_VALUE;
            int min_id = -1;
            int after_whom_in_solution = -1;
            List<Integer> better_cycle = null;
            for (List<Integer> solution : Arrays.asList(cycle1, cycle2)) {
                if (solution.size() + i.size() <= size / 2 + 1) {
                    for (int j = 0; j + 1 < solution.size(); j++) {
                        double w = dist[solution.get(j)][i.get(0)] + dist[i.get(i.size() - 1)][solution.get(j + 1)] - dist[solution.get(j)][solution.get(j + 1)];
                        if (w < min_dist) {
                            better_cycle = solution;
                            min_id = not_used_paths.indexOf(i);
                            min_dist = w;
                            after_whom_in_solution = solution.get(j);
                        }
                    }
                }
            }
            if (better_cycle == null)
                return -1;

            better_cycle.addAll(better_cycle.indexOf(after_whom_in_solution) + 1, not_used_paths.get(min_id));
        }
        return 0;
    }

    public static int findListIndex(List<List<Integer>> listOfLists, int target) {
        for (int i = 0; i < listOfLists.size(); i++) {
            List<Integer> currentList = listOfLists.get(i);
            if (currentList.contains(target)) {
                return i;
            }
        }
        return -1;  // Return -1 if the target is not found in any list
    }

    private static List<Integer> find_two_most_distant_nodes_from_available(double[][] distances, List<Integer> not_used) {
        List<Integer> result_nodes = new ArrayList<>(
        ) {{
            add(-1);
            add(-1);
        }};
        double max_dist = 0.0;
        for (int x : not_used) {
            for (int y : not_used) {
                if (x == y)
                    continue;

                if (max_dist < distances[x][y]) {
                    max_dist = distances[x][y];
                    result_nodes.set(0, x);
                    result_nodes.set(1, y);
                }
            }
        }
        return result_nodes;
    }

    private static List<List<Integer>> find_same_paths(Cycles first_solution, Cycles second_solution) {
        List<List<Integer>> same_paths = new ArrayList<>();
        perform_findind_between_two_cycles(same_paths, first_solution.first_cycle, second_solution.first_cycle);
        perform_findind_between_two_cycles(same_paths, first_solution.first_cycle, second_solution.second_cycle);
        perform_findind_between_two_cycles(same_paths, first_solution.second_cycle, second_solution.first_cycle);
        perform_findind_between_two_cycles(same_paths, first_solution.second_cycle, second_solution.second_cycle);
        same_paths = mergeLists(same_paths);
        same_paths = eliminateSublists(same_paths);
        same_paths = eliminateDuplicateLists(same_paths);
        return same_paths;
    }

    private static List<List<Integer>> eliminateDuplicateLists(List<List<Integer>> listOfLists) {
        Set<List<Integer>> uniqueLists = new HashSet<>(listOfLists);
        return new ArrayList<>(uniqueLists);
    }

    private static List<List<Integer>> eliminateSublists(List<List<Integer>> setOfLists) {
        List<List<Integer>> filteredList = new ArrayList<>();
        for (List<Integer> currentList : setOfLists) {
            boolean isSubset = false;
            for (List<Integer> otherList : setOfLists) {
                if (currentList != otherList && otherList.containsAll(currentList)) {
                    isSubset = true;
                    break;
                }
            }
            if (!isSubset) {
                filteredList.add(currentList);
            }
        }
        return filteredList;
    }

    private static List<List<Integer>> perform_findind_between_two_cycles(List<List<Integer>> commonPaths, List<Integer> cycle1, List<Integer> cycle2) {
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

    public static List<List<Integer>> mergeLists(List<List<Integer>> listOfLists) {
        Map<Integer, List<Integer>> startMap = new HashMap<>();
        Map<Integer, List<Integer>> endMap = new HashMap<>();

        for (List<Integer> list : listOfLists) {
            int start = list.get(0);
            int end = list.get(list.size() - 1);

            List<Integer> mergedList = new ArrayList<>(list);

            if (endMap.containsKey(start)) {
                List<Integer> previousList = endMap.get(start);
                mergedList.remove(0);
                previousList.addAll(mergedList);
                endMap.put(end, previousList);
                endMap.remove(start);
                startMap.remove(start);
                startMap.put(end, previousList);
            } else if (startMap.containsKey(end)) {
                List<Integer> nextList = startMap.get(end);
                mergedList.remove(mergedList.size() - 1);
                mergedList.addAll(nextList);
                startMap.remove(end);
                endMap.remove(end);
                startMap.put(start, mergedList);
                endMap.put(start, mergedList);
            } else {
                startMap.put(start, mergedList);
                endMap.put(end, mergedList);
            }
        }

        return new ArrayList<>(startMap.values());
    }
}