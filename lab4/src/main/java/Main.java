import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Main {
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
            double[][] dist, ArrayList<Integer> first_cycle, ArrayList<Integer> second_cycle, List<Operation> candidate_moves) {

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

    static void steep_edge_exchange(double[][] dist, ArrayList<Integer> cycle, List<Operation> candidate_moves) {
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

    static ArrayList[] generate_random_cycles(int first_id, int second_id) {
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
        return new ArrayList[]{first_cycle, second_cycle};
    }

    static double greedy_vertex_between_two_exchange(
            double[][] dist, ArrayList<Integer> first_cycle, ArrayList<Integer> second_cycle) {
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

                    return cost;
                }
            }
        }
        return 0.0;
    }

    private static ArrayList[] MSLS(Random rand, double[][] distances) {
        ArrayList[] best_cycles = new ArrayList[0];
        double best_dist = Double.MAX_VALUE;
        for (int i = 0; i < 100; i++) {
            int first_id = rand.nextInt(size);
            int second_id = HelperFunctions.find_second_starting_node(first_id, distances);
            ArrayList[] cycles = generate_random_cycles(first_id, second_id);

            double gain = -1;
            while (gain < 0) {
                gain = greedy_vertex_between_two_exchange(distances, cycles[0], cycles[1]);
                gain += greedy_edge_exchange(distances, cycles[0]);
                gain += greedy_edge_exchange(distances, cycles[1]);
            }
            double total_dist = HelperFunctions.get_result(distances, cycles[0]) + HelperFunctions.get_result(distances, cycles[1]);
            if (total_dist < best_dist) {
                best_cycles = cycles;
                best_dist = total_dist;
            }
        }
        return best_cycles;
    }

    static void cycle_creation(double[][] dist, ArrayList<Integer> not_used, ArrayList<Integer> solution) {
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

    static List<Integer> get_random_order() {
        Random rand = new Random();
        List<Integer> indexes = IntStream.range(1, size / 2).boxed().collect(Collectors.toList());
        for (int i = 0; i < indexes.size(); i++) {
            Collections.swap(indexes, i, rand.nextInt(indexes.size()));
        }
        return indexes;
    }

    static ArrayList[] generate_greedy_cycles(double[][] dist, int first_start, int second_start) {
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
        return new ArrayList[]{solution1, solution2};
    }

    static double greedy_edge_exchange(double[][] dist, ArrayList<Integer> first_cycle) {
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
                    return cost;
                }
            }
        }
        return 0.0;
    }

    public static void main(String[] args) throws IOException {
        HelperFunctions.Node[] nodes = new HelperFunctions.Node[size];
        HelperFunctions.load_data(nodes, "kroA200.tsp");
        double[][] distances = HelperFunctions.calculate_distance(nodes);
        Random rand = new Random();
        int first_id = rand.nextInt(size);
        int second_id = HelperFunctions.find_second_starting_node(first_id, distances);

        //ArrayList[] cycles = MSLS(rand, distances);
        ArrayList[] cycles = generate_greedy_cycles(distances, first_id, second_id);
        //cycles = small_perturbation(distances, cycles);
        HelperFunctions.print_result(distances, cycles, args);
        cycles = destroy_and_repair(distances, cycles);

        HelperFunctions.print_result(distances, cycles, args);

        cycles[0].forEach(i -> System.out.print(i + " "));
        System.out.println();
        cycles[1].forEach(i -> System.out.print(i + " "));
    }

    private static ArrayList[] destroy_and_repair(double[][] distances, ArrayList<Integer>[] cycles) {
        List<Nearest> nearests = new ArrayList<>();
        //utworz listę najbliższych sobie wierzchołków
        for (int i = 1; i < cycles[0].size() - 1; i++) {
            var val = cycles[0].get(i);
            for (int x = val + 1; x < size; x++) {
                if (cycles[0].contains(x) || (x == cycles[0].get(0)) || (x == cycles[1].get(0))) {
                    continue;
                }
                nearests.add(new Nearest(val, x, distances[val][x]));
            }
        }
        for (int i = 1; i < cycles[1].size() - 1; i++) {
            var val = cycles[1].get(i);
            for (int x = val + 1; x < size; x++) {
                if (cycles[1].contains(x) || (x == cycles[0].get(0)) || (x == cycles[1].get(0))) {
                    continue;
                }
                nearests.add(new Nearest(x, val, distances[val][x]));
            }
        }
        Collections.sort(nearests);
        //posortuj, by znaleźć najbliższe sobie

        Set<Integer> removed_nodes = new HashSet<>();

        //idiotyczna inicjalizacja tablicy pomocnicznej, bo zazwyczaj idzie po referencji zamiast po wartościach XD
        ArrayList[] cycles_for_destroy = new ArrayList[cycles.length];
        for (int i = 0; i < cycles.length; i++) {
            ArrayList originalCycle = cycles[i];
            ArrayList newCycle = new ArrayList(originalCycle);
            cycles_for_destroy[i] = newCycle;
        }

        //usun 20 procent najbliższych sobie wierzchołków, które są w osobnych cyklach
        for (int i = 0; i < nearests.size() &&
                cycles_for_destroy[0].size() + cycles_for_destroy[1].size() > size * 0.8; i++) {
            cycles_for_destroy[0].remove((Integer) nearests.get(i).x);
            removed_nodes.add(nearests.get(i).x);
            cycles_for_destroy[1].remove((Integer) nearests.get(i).y);
            removed_nodes.add(nearests.get(i).y);
        }

        //usun losowe 6 procent wierzchołków
        Random rand = new Random();
        for (int i = 0; i < 0.03 * size; i++) {
            int id_1 = rand.nextInt(cycles_for_destroy[0].size() - 2) + 1;
            int element_1 = (int) cycles_for_destroy[0].get(id_1);
            cycles_for_destroy[0].remove(id_1);
            int id_2 = rand.nextInt(cycles_for_destroy[0].size() - 2) + 1;
            int element_2 = (int) cycles_for_destroy[0].get(id_2);
            cycles_for_destroy[0].remove(id_2);
            removed_nodes.add(element_1);
            removed_nodes.add(element_2);
        }

        ArrayList<Integer> removed_nodes_ale_to_lista = (ArrayList<Integer>) removed_nodes.stream().collect(Collectors.toList());
        //napraw, również za pomocą greedy cycle
        while (!removed_nodes_ale_to_lista.isEmpty() && cycles_for_destroy[0].size() < 101 && cycles_for_destroy[1].size() < 101) {
            cycle_creation(distances, removed_nodes_ale_to_lista, cycles_for_destroy[1]);
            cycle_creation(distances, removed_nodes_ale_to_lista, cycles_for_destroy[0]);
        }
        switch (cycles_for_destroy[0].size()) {
            case 101:
                while (!removed_nodes_ale_to_lista.isEmpty())
                    cycle_creation(distances, removed_nodes_ale_to_lista, cycles_for_destroy[1]);
                break;
            default:
                while (!removed_nodes_ale_to_lista.isEmpty())
                    cycle_creation(distances, removed_nodes_ale_to_lista, cycles_for_destroy[0]);
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

    private static ArrayList[] small_perturbation(double[][] dist, ArrayList[] cycles) {
        System.out.println(HelperFunctions.get_total_dist(dist, cycles));
        Random random = new Random();
        double best_dist = HelperFunctions.get_total_dist(dist, cycles);
        for (int iteration_no = 0; iteration_no < 17; iteration_no++) {
            ArrayList[] cycles_for_perturbation = Arrays.copyOf(cycles, cycles.length);
            for (int perturbation_no = 0; perturbation_no < 15; perturbation_no++) {
                int operation_no = random.nextInt(3);
                int cycle_no = random.nextInt(2);
                switch (operation_no) {
                    case 0:
                        cycles_for_perturbation[cycle_no] = random_edge_exchange(cycles_for_perturbation[cycle_no]);
                        break;
                    case 1:
                        cycles_for_perturbation[(cycle_no + 1) % 2] = random_edge_exchange(cycles_for_perturbation[(cycle_no + 1) % 2]);
                        break;
                    case 2:
                        random_vertex_exchange(cycles_for_perturbation);
                        break;
                }
            }

            steepest(dist, cycles_for_perturbation);
            double dist_after_repair = HelperFunctions.get_total_dist(dist, cycles_for_perturbation);
            if (dist_after_repair < best_dist) {
                best_dist = dist_after_repair;
                cycles = Arrays.copyOf(cycles_for_perturbation, cycles_for_perturbation.length);
            }
        }
        return cycles;
    }

    private static void make_best_move(Operation move, ArrayList<Integer>[] cycles) {
        var from_in_first = cycles[0].indexOf(move.from);
        var from_in_second = cycles[1].indexOf(move.from);
        var to_in_first = cycles[0].indexOf(move.to);
        var to_in_second = cycles[1].indexOf(move.to);
        if (move.type.equals("vertex")) {
            if (from_in_first != -1 && to_in_second != -1) {
                cycles[0].set(from_in_first, move.to);
                cycles[1].set(to_in_second, move.from);

            } else if (from_in_second != -1 && to_in_first != -1) {
                cycles[1].set(from_in_second, move.to);
                cycles[0].set(to_in_first, move.from);
            }

        } else if (move.type.equals("edge")) {
            boolean first = cycles[0].containsAll(Arrays.asList(move.from, move.from_next, move.to, move.to_next));
            boolean second = cycles[1].containsAll(Arrays.asList(move.from, move.from_next, move.to, move.to_next));
            if (first) {
                if (from_in_first > to_in_first) {
                    var temp = from_in_first;
                    from_in_first = to_in_first;
                    to_in_first = temp;
                }
                Collections.reverse(cycles[0].subList(from_in_first + 1, to_in_first + 1));
            } else if (second) {
                if (from_in_second > to_in_second) {
                    var temp = from_in_second;
                    from_in_second = to_in_second;
                    to_in_second = temp;
                }
                Collections.reverse(cycles[1].subList(from_in_second + 1, to_in_second + 1));
            }
        }
    }

    private static ArrayList<Integer>[] steepest(double[][] distances, ArrayList<Integer>[] cycles) {

        List<Operation> candidate_moves = new ArrayList<>();
        while (true) {
            candidate_moves.clear();
            steep_edge_exchange(distances, cycles[1], candidate_moves);
            steep_edge_exchange(distances, cycles[0], candidate_moves);
            steep_vertex_between_two_exchange(distances, cycles[0], cycles[1], candidate_moves);
            if (candidate_moves.isEmpty()) {
                break;
            }
            candidate_moves.sort(Operation::compareTo);
            make_best_move(candidate_moves.get(0), cycles);
        }
        return cycles;
    }

    private static ArrayList[] random_vertex_exchange(ArrayList<Integer>[] cycles) {
        Random random = new Random();
        int id_in_first_cycle = random.nextInt((size / 2) - 1) + 1;
        int id_in_second_cycle = random.nextInt((size / 2) - 1) + 1;
        int val_from_first_cycle = cycles[0].get(id_in_first_cycle);
        int val_from_second_cycle = cycles[1].get(id_in_second_cycle);

        cycles[0].set(id_in_first_cycle, val_from_second_cycle);
        cycles[1].set(id_in_second_cycle, val_from_first_cycle);

        return cycles;
    }

    private static ArrayList random_edge_exchange(ArrayList cycle) {
        Random random = new Random();
        int first_id = random.nextInt((size / 2) - 1) + 1;
        int second_id = random.nextInt((size / 2) - first_id) + first_id + 1;
        Collections.reverse(cycle.subList(first_id, second_id));
        return cycle;
    }
}