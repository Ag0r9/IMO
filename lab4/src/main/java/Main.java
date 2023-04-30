import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Main {
    static int size = 200;

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

        while (solution1.size() < size / 2 + 1)
            cycle_creation(dist, not_used, solution1);

        while (solution2.size() < size / 2 + 1)
            cycle_creation(dist, not_used, solution2);

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

        cycles = small_perturbation(distances, cycles);

        HelperFunctions.print_result(distances, cycles, args);
        cycles[0].forEach(i -> System.out.print(i + " "));
        System.out.println();
        cycles[1].forEach(i -> System.out.print(i + " "));
    }

    private static ArrayList[] small_perturbation(double[][] dist, ArrayList[] cycles) {
        Random random = new Random();
        double best_dist = HelperFunctions.get_total_dist(dist, cycles);
        for (int iteration_no = 0; iteration_no < 10; iteration_no++) {
            ArrayList[] cycles_for_perturbation = cycles.clone();
            for (int perturbation_no = 0; perturbation_no < 13; perturbation_no++) {

            }
            double dist_after_repair = HelperFunctions.get_total_dist(dist, cycles);
            if (dist_after_repair < best_dist) {
                best_dist = dist_after_repair;
                cycles = cycles_for_perturbation;
            }
        }
        return cycles;
    }


}