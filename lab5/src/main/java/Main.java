import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

class Main {
    static class Cycles {
        List<Integer> first_cycle;
        List<Integer> second_cycle;

        Cycles(List<Integer> first_cycle, List<Integer> second_cycle) {
            this.first_cycle = first_cycle;
            this.second_cycle = second_cycle;
        }
    }

    static class Nearest implements Comparable {
        int x;
        int y;
        double evaluation;

        Nearest(int x, int y, double evaluation) {
            this.x = x;
            this.y = y;
            this.evaluation = evaluation;
        }

        @Override
        public int compareTo(Object o) {
            return Double.compare(evaluation, ((Nearest) o).evaluation);
        }
    }

    static int size = 200;

    static class Operation implements Comparable {
        Operation(String type, int from, int to, double evaluation, boolean valid) {
            this.type = type;
            this.from = from;
            this.to = to;
            this.evaluation = evaluation;
            this.from_next = -1;
            this.to_next = -1;
        }

        Operation(String type, int from, int from_next, int to, int to_next, double evaluation, boolean valid) {
            this.type = type;
            this.from = from;
            this.from_next = from_next;
            this.to = to;
            this.to_next = to_next;
            this.evaluation = evaluation;
        }

        String type;
        double evaluation;
        int from;
        int from_next;
        int to;
        int to_next;


        @Override
        public int compareTo(Object o) {
            return Double.compare(this.evaluation, ((Operation) o).evaluation);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Operation operation = (Operation) o;
            return Double.compare(operation.evaluation, this.evaluation) == 0 &&
                    Objects.equals(this.type, operation.type);
        }
    }

    static void steep_vertex_between_two_exchange(
            double[][] dist, List<Integer> first_cycle, List<Integer> second_cycle, List<Operation> candidate_moves) {

        for (int i = 1; i < first_cycle.size() - 1; i++) {
            for (int j = 1; j < second_cycle.size() - 1; j++) {

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

                if (cost < 0 && !candidate_moves.contains(new Operation("vertex", i_value, j_value, cost, true))) {
                    candidate_moves.add(new Operation("vertex", i_value, j_value, cost, true));
                }
            }
        }
    }

    static void steep_edge_exchange(double[][] dist, List<Integer> cycle, List<Operation> candidate_moves) {
        for (int i = 0; i < cycle.size() - 1; i++) {
            for (int j = 0; j < cycle.size() - 1; j++) {
                if (Math.abs(i - j) < 2)
                    continue;

                int i_value = cycle.get(i);
                int i_next = cycle.get(i + 1);
                int j_value = cycle.get(j);
                int j_next = cycle.get(j + 1);

                double cost = (dist[i_value][j_value] + dist[i_next][j_next]) - (dist[i_value][i_next] + dist[j_value][j_next]);

                if (cost < 0 && !candidate_moves.contains(new Operation("edge", i_value, i_next, j_value, j_next, cost, true))) {
                    candidate_moves.add(new Operation("edge", i_value, i_next, j_value, j_next, cost, true));
                }
            }
        }
    }

    static void cycle_creation(double[][] dist, ArrayList<Integer> not_used, List<Integer> solution) {
        double min_dist = Double.MAX_VALUE;
        int min_id = -1;
        int after_whom_in_solution = -1;
        for (int i : not_used) {
            for (int j = 0; j + 1 < solution.size(); j++) {
                double w = dist[solution.get(j)][i] + dist[i][solution.get(j + 1)] - dist[solution.get(j)][solution.get(j + 1)];
                if (w < min_dist) {
                    min_id = i;
                    min_dist = w;
                    after_whom_in_solution = solution.get(j);
                }
            }
        }
        solution.add(solution.indexOf(after_whom_in_solution) + 1, min_id);
        not_used.remove(Integer.valueOf(min_id));
    }

    static Cycles generate_greedy_cycles(double[][] dist, int first_start, int second_start) {
        ArrayList<Integer> not_used = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (i != first_start && i != second_start)
                not_used.add(i);
        }
        ArrayList<Integer> solution1 = new ArrayList<>() {{
            add(first_start);
            add(first_start);
        }};
        ArrayList<Integer> solution2 = new ArrayList<>() {{
            add(second_start);
            add(second_start);
        }};

        while (solution1.size() < size / 2 + 1) {
            cycle_creation(dist, not_used, solution1);
            cycle_creation(dist, not_used, solution2);
        }
        return new Cycles(solution1, solution2);
    }

    public static void main(String[] args) throws IOException {
        HelperFunctions.Node[] nodes = new HelperFunctions.Node[size];
        HelperFunctions.load_data(nodes, "kroA200.tsp");
        double[][] distances = HelperFunctions.calculate_distance(nodes);
        Random rand = new Random();
        int first_id, second_id;
        first_id = rand.nextInt(size);
        second_id = HelperFunctions.find_second_starting_node(first_id, distances);
        //Cycles cycles = hybrid_evolutionary(distances);

        Cycles cycles = generate_greedy_cycles(distances, first_id, second_id);
        cycles = destroy_and_repair(distances, cycles);
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
            list_of_cycles.add(generate_greedy_cycles(distances, first_id, second_id));
        }
        list_of_cycles.sort(Comparator.comparingDouble(c -> HelperFunctions.get_total_dist(distances, c)));
        //Utwórz początkową populację

        Set<List<Integer>> same_paths = find_same_paths(list_of_cycles.get(0), list_of_cycles.get(1));
        same_paths.stream().forEach(x -> {
            x.stream().forEach(y -> System.out.print(y + " "));
            System.out.println();
        });

        return list_of_cycles.get(0);
    }

    private static Set<List<Integer>> find_same_paths(Cycles first_solution, Cycles second_solution) {
        Set<List<Integer>> same_paths = new HashSet<>();
        same_paths.addAll(perform_findind_between_two_cycles(first_solution.first_cycle, second_solution.first_cycle));
        same_paths.addAll(perform_findind_between_two_cycles(first_solution.first_cycle, second_solution.second_cycle));
        same_paths.addAll(perform_findind_between_two_cycles(first_solution.second_cycle, second_solution.first_cycle));
        same_paths.addAll(perform_findind_between_two_cycles(first_solution.second_cycle, second_solution.second_cycle));
        return same_paths;
    }

    private static Set<List<Integer>> perform_findind_between_two_cycles(List<Integer> cycle1, List<Integer> cycle2) {
        Set<List<Integer>> commonPaths = new HashSet<>();

        for (int i = 0; i < cycle1.size() - 1; i++) {
            int node1 = cycle1.get(i);
            int node2 = cycle1.get(i + 1);

            for (int j = 0; j < cycle2.size() - 1; j++) {
                int otherNode1 = cycle2.get(j);
                int otherNode2 = cycle2.get(j + 1);

                if (node1 == otherNode1 && node2 == otherNode2) {
                    List<Integer> commonPath = new ArrayList<>();
                    commonPath.addAll(cycle1.subList(i, i + 2));
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

                    commonPath.addAll(cycle2.subList(j + 2, l));
                    commonPaths.add(commonPath);
                }
            }
        }
        return commonPaths;
    }


    private static Cycles destroy_and_repair(double[][] distances, Cycles cycles) {
        List<Nearest> nearests = new ArrayList<>();
        //utworz listę najbliższych sobie wierzchołków
        for (int i = 1; i < cycles.first_cycle.size() - 1; i++) {
            var val = cycles.first_cycle.get(i);
            for (int x = val + 1; x < size; x++) {
                if (cycles.first_cycle.contains(x) || (x == cycles.first_cycle.get(0)) || (x == cycles.second_cycle.get(0))) {
                    continue;
                }
                nearests.add(new Nearest(val, x, distances[val][x]));
            }
        }
        for (int i = 1; i < cycles.second_cycle.size() - 1; i++) {
            var val = cycles.second_cycle.get(i);
            for (int x = val + 1; x < size; x++) {
                if (cycles.second_cycle.contains(x) || (x == cycles.first_cycle.get(0)) || (x == cycles.second_cycle.get(0))) {
                    continue;
                }
                nearests.add(new Nearest(x, val, distances[val][x]));
            }
        }
        Collections.sort(nearests);
        //posortuj, by znaleźć najbliższe sobie

        Set<Integer> removed_nodes = new HashSet<>();

        //idiotyczna inicjalizacja tablicy pomocnicznej, bo zazwyczaj idzie po referencji zamiast po wartościach XD
        Cycles cycles_for_destroy = new Cycles(cycles.first_cycle, cycles.second_cycle);

        //usun 20 procent najbliższych sobie wierzchołków, które są w osobnych cyklach
        for (int i = 0; i < nearests.size() &&
                cycles_for_destroy.first_cycle.size() + cycles_for_destroy.second_cycle.size() > size * 0.8; i++) {
            cycles_for_destroy.first_cycle.remove((Integer) nearests.get(i).x);
            removed_nodes.add(nearests.get(i).x);
            cycles_for_destroy.second_cycle.remove((Integer) nearests.get(i).y);
            removed_nodes.add(nearests.get(i).y);
        }

        //usun losowe 6 procent wierzchołków
        Random rand = new Random();
        for (int i = 0; i < 0.03 * size; i++) {
            int id_1 = rand.nextInt(cycles_for_destroy.first_cycle.size() - 2) + 1;
            int element_1 = (int) cycles_for_destroy.first_cycle.get(id_1);
            cycles_for_destroy.first_cycle.remove(id_1);
            int id_2 = rand.nextInt(cycles_for_destroy.first_cycle.size() - 2) + 1;
            int element_2 = (int) cycles_for_destroy.first_cycle.get(id_2);
            cycles_for_destroy.first_cycle.remove(id_2);
            removed_nodes.add(element_1);
            removed_nodes.add(element_2);
        }

        ArrayList<Integer> removed_nodes_ale_to_lista = (ArrayList<Integer>) removed_nodes.stream().collect(Collectors.toList());
        //napraw, również za pomocą greedy cycle
        while (!removed_nodes_ale_to_lista.isEmpty() && cycles_for_destroy.first_cycle.size() < 101 && cycles_for_destroy.second_cycle.size() < 101) {
            cycle_creation(distances, removed_nodes_ale_to_lista, cycles_for_destroy.second_cycle);
            cycle_creation(distances, removed_nodes_ale_to_lista, cycles_for_destroy.first_cycle);
        }
        switch (cycles_for_destroy.first_cycle.size()) {
            case 101:
                while (!removed_nodes_ale_to_lista.isEmpty())
                    cycle_creation(distances, removed_nodes_ale_to_lista, cycles_for_destroy.second_cycle);
                break;
            default:
                while (!removed_nodes_ale_to_lista.isEmpty())
                    cycle_creation(distances, removed_nodes_ale_to_lista, cycles_for_destroy.first_cycle);
                break;
        }

        cycles_for_destroy = steepest(distances, cycles_for_destroy);
        //jak jest poprawa to zwróć poprawione, jak nie to CHLIP, ale no trudno i zwróć stare
        if (HelperFunctions.get_total_dist(distances, cycles) - HelperFunctions.get_total_dist(distances, cycles_for_destroy) > 0) {
            return cycles_for_destroy;
        } else {
            return cycles;
        }
    }

    private static void make_best_move(Operation move, Cycles cycles) {
        var from_in_first = cycles.first_cycle.indexOf(move.from);
        var from_in_second = cycles.second_cycle.indexOf(move.from);
        var to_in_first = cycles.first_cycle.indexOf(move.to);
        var to_in_second = cycles.second_cycle.indexOf(move.to);
        if (move.type.equals("vertex")) {
            if (from_in_first != -1 && to_in_second != -1) {
                cycles.first_cycle.set(from_in_first, move.to);
                cycles.second_cycle.set(to_in_second, move.from);

            } else if (from_in_second != -1 && to_in_first != -1) {
                cycles.second_cycle.set(from_in_second, move.to);
                cycles.first_cycle.set(to_in_first, move.from);
            }

        } else if (move.type.equals("edge")) {
            boolean first = cycles.first_cycle.containsAll(Arrays.asList(move.from, move.from_next, move.to, move.to_next));
            boolean second = cycles.second_cycle.containsAll(Arrays.asList(move.from, move.from_next, move.to, move.to_next));
            if (first) {
                if (from_in_first > to_in_first) {
                    var temp = from_in_first;
                    from_in_first = to_in_first;
                    to_in_first = temp;
                }
                Collections.reverse(cycles.first_cycle.subList(from_in_first + 1, to_in_first + 1));
            } else if (second) {
                if (from_in_second > to_in_second) {
                    var temp = from_in_second;
                    from_in_second = to_in_second;
                    to_in_second = temp;
                }
                Collections.reverse(cycles.second_cycle.subList(from_in_second + 1, to_in_second + 1));
            }
        }
    }

    private static Cycles steepest(double[][] distances, Cycles cycles) {

        List<Operation> candidate_moves = new ArrayList<>();
        while (true) {
            candidate_moves.clear();
            steep_edge_exchange(distances, cycles.second_cycle, candidate_moves);
            steep_edge_exchange(distances, cycles.first_cycle, candidate_moves);
            steep_vertex_between_two_exchange(distances, cycles.first_cycle, cycles.second_cycle, candidate_moves);
            if (candidate_moves.isEmpty()) {
                break;
            }
            candidate_moves.sort(Operation::compareTo);
            make_best_move(candidate_moves.get(0), cycles);
        }
        return cycles;
    }

}