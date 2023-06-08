import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class Main {
    static class Cycles {
        List<Integer> first_cycle;
        List<Integer> second_cycle;

        Cycles(List<Integer> first_cycle, List<Integer> second_cycle) {
            this.first_cycle = first_cycle;
            this.second_cycle = second_cycle;
        }
    }

    static int size = 200;
    static int population = 15;

    public static void main(String[] args) throws IOException, InterruptedException {
        HelperFunctions.Node[] nodes = new HelperFunctions.Node[size];
        HelperFunctions.load_data(nodes, "kroA200.tsp");
        double[][] distances = HelperFunctions.calculate_distance(nodes);
        Random rand = new Random();

        Cycles b = hybrid_evolutionary(distances);
//        List<Cycles> input = new ArrayList<>();
//        input.add(new Cycles( List.of(5, 1, 6, 5),List.of(3,4,7,3)));
//        input.add(new Cycles( List.of(5, 1, 6, 5),List.of(3,4,7,3)));
//        input.add(new Cycles( List.of(6, 1, 2, 6),List.of(3,9,7,3)));
//
//
//        var naj = input.get(0);
//        for (Cycles cycles : input) {
//            System.out.println(HelperFunctions.get_total_dist(distances, cycles));
//            List<List<Integer>> same_paths = find_same_paths(cycles, naj);
//
//            System.out.println((double) same_paths.stream().flatMap(List::stream).count() / 6);
//
//            System.out.println( 1+ determine_vertex_similarity(cycles, naj));
//
//            System.out.println("\n");
//        }

//
//        Cycles cycles = GreedyCycle.generate_greedy_cycles(distances, first_id, second_id);
//        cycles = DestroyAndRepair.destroy_and_repair(distances, cycles);

            List<Cycles> solutions = new ArrayList<>();
            int first_id, second_id;

            for (int i = 0; i < 1001; i++) {
                Thread.sleep(150);
                first_id = rand.nextInt(size);
                second_id = HelperFunctions.find_second_starting_node(first_id, distances);
                Cycles x = generate_random_cycles(first_id, second_id);

                int gain = -1;
                while (gain < 0) {
                    gain = greedy_vertex_between_two_exchange(distances, x.first_cycle, x.second_cycle);
                    gain += greedy_edge_exchange(distances, x.first_cycle);
                    gain += greedy_edge_exchange(distances, x.second_cycle);
                }
                solutions.add(x);
            }
            solutions.sort(Comparator.comparingDouble(c -> HelperFunctions.get_total_dist(distances, c)));
            solutions.add(0,b);
            var naj = solutions.get(0);
            for (Cycles cycles : solutions) {
                System.out.println(HelperFunctions.get_total_dist(distances, cycles));
                List<List<Integer>> same_paths = find_same_paths(cycles, naj);

                System.out.printf("%.5f",(double) same_paths.stream().flatMap(List::stream).count() / size);
                System.out.println();
                System.out.printf("%.5f", 1+ determine_vertex_similarity(cycles, naj));
                System.out.println();

                List<Double> mean_edges = new ArrayList<>();
                List<Double> mean_vertex= new ArrayList<>();
                for (Cycles c : solutions){
                    if(c.equals(cycles))
                        continue;
                    List<List<Integer>> paths = find_same_paths(cycles, c);
                    mean_edges.add((double) paths.stream().flatMap(List::stream).count() / size);
                    mean_vertex.add( 1+ determine_vertex_similarity(cycles, c));
                }
                System.out.printf("%.5f",mean_edges
                        .stream()
                        .mapToDouble(a -> a)
                        .average().getAsDouble());
                System.out.println();
                System.out.printf("%.5f", mean_vertex
                                        .stream()
                                        .mapToDouble(a -> a)
                                        .average().getAsDouble());
                System.out.println("\n");

            }

        System.out.println(HelperFunctions.get_total_dist(distances, solutions.get(0)));
        solutions.get(0).first_cycle.forEach(i -> System.out.print(i + " "));
        System.out.println();
        solutions.get(0).second_cycle.forEach(i -> System.out.print(i + " "));
        System.out.println("\n");
    }
    private static double determine_vertex_similarity(Cycles cycles, Cycles naj) {
        Set y=new HashSet(), x = new HashSet(), z=new HashSet();
        x.addAll(cycles.first_cycle);
        x.addAll(naj.first_cycle);

        y.addAll(cycles.first_cycle);
        y.addAll(naj.second_cycle);

        z.addAll(cycles.second_cycle);

        if(x.size()<y.size()){
            z.addAll(naj.second_cycle);
            return (double)(-x.size()- z.size() + size)/size;
        }
        else{
            z.addAll(naj.first_cycle);
            return (double)(-y.size()- z.size() + size)/size;
        }
    }

    static Cycles generate_random_cycles(int first_id, int second_id) {
        List<Integer> not_used = IntStream.range(0, size).filter(i -> i != first_id && i != second_id).boxed().collect(Collectors.toList());
        ArrayList<Integer> first_cycle = new ArrayList<>() {{
            add(first_id);
        }};
        ArrayList<Integer> second_cycle = new ArrayList<>() {{
            add(second_id);
        }};

        Random rand = new Random();
        while (first_cycle.size() < size / 2) {
            int idx = rand.nextInt(not_used.size());
            first_cycle.add(not_used.get(idx));
            not_used.remove(idx);
        }
        first_cycle.add(first_id);
        while (second_cycle.size() < size / 2) {
            int idx = rand.nextInt(not_used.size());
            second_cycle.add(not_used.get(idx));
            not_used.remove(idx);
        }
        second_cycle.add(second_id);
        return new Cycles(first_cycle, second_cycle);
    }

    static List<Integer> get_random_order() {
        Random rand = new Random();
        List<Integer> indexes = IntStream.range(1, size / 2).boxed().collect(Collectors.toList());
        for (int i = 0; i < indexes.size(); i++) {
            Collections.swap(indexes, i, rand.nextInt(indexes.size()));
        }
        return indexes;
    }

    static int greedy_edge_exchange(double[][] dist, List<Integer> first_cycle) {
        List<Integer> indexes = get_random_order();
        for (int i : indexes) {
            for (int j : indexes) {
                if (Math.abs(i - j) < 3 || i > j)
                    continue;

                int i_value = first_cycle.get(i);
                int i_next = first_cycle.get(i + 1);

                int j_value = first_cycle.get(j);
                int j_next = first_cycle.get(j + 1);

                double cost = (dist[i_value][j_value] + dist[i_next][j_next]) - (dist[i_value][i_next] + dist[j_value][j_next]);
                if (cost < 0) {
                    Collections.reverse(first_cycle.subList(i + 1, j + 1));
                    return -1000;
                }
            }
        }
        return 1;
    }

    static int greedy_vertex_between_two_exchange(
            double[][] dist, List<Integer> first_cycle, List<Integer> second_cycle) {
        List<Integer> indexes = get_random_order();
        for (int i : indexes) {
            for (int j : indexes) {

                int i_prev = first_cycle.get(i - 1);
                int i_value = first_cycle.get(i);
                int i_next = first_cycle.get(i + 1);

                int j_prev = second_cycle.get(j - 1);
                int j_value = second_cycle.get(j);
                int j_next = second_cycle.get(j + 1);

                double cost =
                        (dist[i_prev][j_value] + dist[j_value][i_next] +
                                dist[j_prev][i_value] + dist[i_value][j_next]) -
                                (dist[i_prev][i_value] + dist[i_value][i_next] +
                                        dist[j_prev][j_value] + dist[j_value][j_next]);

                if (cost < 0) {
                    first_cycle.set(i, j_value);
                    second_cycle.set(j, i_value);

                    return -1000;
                }
            }
        }
        return 1;
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

        for (int q = 0; q < 100; q++) {
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
                for (Integer integer : x) {
                    not_used.remove(integer);
                }
            }

            //znajdz najbardziej od siebie oddalone punkty startowe, ktore sa albo wolnymi wierzcholkami albo krawedziami sciezki
            //i utworz poczatkowy cykl skladajacy sie z 1 punktu albo ze sciezki
            List<Integer> two_most_distant_nodes_from_available = find_two_most_distant_nodes_from_available(distances, not_used);

            List<Integer> cycle1 = new ArrayList<>();
            List<Integer> cycle2 = new ArrayList<>();
            int first_start = two_most_distant_nodes_from_available.get(0);
            int second_start = two_most_distant_nodes_from_available.get(1);
            if (first_start == -1 || second_start == -1) {
                continue;
            }


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
        return same_paths;
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

    public static List<List<Integer>> mergeLists(List<List<Integer>> input) {
        Set<List<Integer>> exclusionSet = new HashSet<>();
        List<List<Integer>> result = new ArrayList<>();

        for (List<Integer> currentList : input) {
            boolean foundSublist = false;

            for (List<Integer> otherList : input) {
                if (!currentList.equals(otherList) && isSublist(currentList, otherList)) {
                    foundSublist = true;
                    break;
                }
            }

            if (!foundSublist && !exclusionSet.contains(currentList)) {
                result.add(new ArrayList<>(currentList));
                exclusionSet.add(currentList);
            }
        }

        return result;
    }

    private static boolean isSublist(List<Integer> sublist, List<Integer> list) {
        if (sublist.size() > list.size()) {
            return false;
        }

        for (int i = 0; i <= list.size() - sublist.size(); i++) {
            if (list.subList(i, i + sublist.size()).equals(sublist)) {
                return true;
            }
        }

        return false;
    }
}