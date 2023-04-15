import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class Main {

    static class Node {
        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int x;
        int y;
    }

    static void load_data(Node[] data, String filename) throws IOException {
        Scanner sc = new Scanner(Main.class.getClassLoader().getResource(filename).openStream());

        for (int i = 0; i < 6; i++)
            sc.nextLine();

        for (int i = 0; i < 100; i++) {
            sc.next();
            int x = Integer.parseInt(sc.next());
            int y = Integer.parseInt(sc.next());
            data[i] = new Node(x, y);
        }
        sc.close();
    }

    static double[][] calculate_distance(Node[] nodes) {
        double[][] dist = new double[100][100];
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                double dx = nodes[i].x - nodes[j].x;
                double dy = nodes[i].y - nodes[j].y;
                dist[i][j] = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
            }
        }
        return dist;
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

    static ArrayList<Integer>[] generate_greedy_cycles(double[][] dist, int first_start, int second_start) {
        ArrayList<Integer> not_used = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
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

        while (solution1.size() < 51)
            cycle_creation(dist, not_used, solution1);

        while (solution2.size() < 51)
            cycle_creation(dist, not_used, solution2);

        ArrayList<Integer> x[] = new ArrayList[]{solution1, solution2};
        return x;
    }

    static int find_second_starting_node(int first_id, double[][] distances) {
        int second_id = -1;
        double max_dist = 0.0;
        for (int i = 0; i < 100; i++) {
            if (distances[i][first_id] > max_dist) {
                second_id = i;
                max_dist = distances[i][first_id];
            }
        }
        return second_id;
    }
    static ArrayList<Integer>[] steep_vertex_between_two_exchange(
            double[][] dist, ArrayList<Integer> first_cycle, ArrayList<Integer> second_cycle) {
        double min_cost = Double.MAX_VALUE;
        int i_idx_to_switch = -1, j_idx_to_switch = -1, i_val = -1, j_val = -1;

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

                if (cost < 0 && min_cost > cost) {
                    min_cost = cost;
                    i_idx_to_switch = i;
                    j_idx_to_switch = j;
                    j_val = j_value;
                    i_val = i_value;
                }
            }
        }
        if (min_cost != Double.MAX_VALUE) {
            first_cycle.set(i_idx_to_switch, j_val);
            second_cycle.set(j_idx_to_switch, i_val);
        }
        return new ArrayList[]{first_cycle, second_cycle};
    }

    static ArrayList<Integer> steep_edge_exchange(double[][] dist, ArrayList<Integer> first_cycle) {
        double min_cost = Double.MAX_VALUE;
        int i_idx_to_reverse = -1, j_idx_to_reverse = -1;

        for (int i = 0; i < first_cycle.size() - 1; i++) {
            for (int j = 0; j < first_cycle.size() - 1; j++) {
                if (Math.abs(i - j) < 3)
                    continue;

                int i_value = first_cycle.get(i);
                int i_next = first_cycle.get(i + 1);

                int j_value = first_cycle.get(j);
                int j_next = first_cycle.get(j + 1);

                double cost = (dist[i_value][j_value] + dist[i_next][j_next]) - (dist[i_value][i_next] + dist[j_value][j_next]);
                if (cost < 0 && min_cost > cost) {
                    min_cost = cost;
                    i_idx_to_reverse = i + 1;
                    j_idx_to_reverse = j + 1;
                }
            }
        }
        if (min_cost != Double.MAX_VALUE)
            Collections.reverse(first_cycle.subList(i_idx_to_reverse, j_idx_to_reverse));

        return first_cycle;
    }

    static void print_result(double distances[][], ArrayList<Integer>[] cycles, String[] type) {
        double total_dist = 0.0;
        for (int cycle_no = 0; cycle_no < cycles.length; cycle_no++) {
            for (int node_no = 0; node_no < cycles[cycle_no].size() - 1; node_no++) {
                total_dist += distances[cycles[cycle_no].get(node_no)][cycles[cycle_no].get(node_no + 1)];
            }
        }
        String args = "";
        for (String s : type) {
            args += (s + " ");
        }
        System.out.println(args + total_dist);
    }

    static double get_result(double distances[][], ArrayList<Integer> cycle) {
        double total_dist = 0.0;
        for (int node_no = 0; node_no < cycle.size() - 1; node_no++) {
            total_dist += distances[cycle.get(node_no)][cycle.get(node_no + 1)];
        }

        return total_dist;
    }

    public static void main(String[] args) throws IOException {
        Node[] nodes = new Node[100];
        load_data(nodes, "kroA100.tsp");
        double[][] distances = calculate_distance(nodes);

        Random rand = new Random();
        int first_id = rand.nextInt(100);
        int second_id = find_second_starting_node(first_id, distances);
        ArrayList<Integer> cycles[];

        cycles = generate_greedy_cycles(distances, first_id, second_id);

        for (int i = 0; i < 250; i++) {
                cycles = steep_vertex_between_two_exchange(distances, cycles[0], cycles[1]);
                cycles[0] = steep_edge_exchange(distances, cycles[0]);
                cycles[1] = steep_edge_exchange(distances, cycles[1]);
        }

        print_result(distances, cycles, args);
        cycles[0].forEach(i -> System.out.print(i + " "));
        System.out.println();
        cycles[1].forEach(i -> System.out.print(i + " "));
    }
}