import java.util.*;
import java.io.*;

class Main {
    static int size = 100;

    static class Node {
        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int x;
        int y;
    }

    static class Operation implements Comparable {
        Operation(String type, int from, int to, double evaluation) {
            this.type = type;
            this.from = from;
            this.to = to;
            this.evaluation = evaluation;
            this.from_next = -1;
            this.to_next = -1;
        }

        Operation(String type, int from, int from_next, int to, int to_next, double evaluation) {
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
        int from_next;//
        int to;
        int to_next;

        @Override
        public int compareTo(Object o) {
            return Double.compare(this.evaluation, ((Operation) o).evaluation);
        }
    }

    static void load_data(Node[] data, String filename) throws IOException {
        Scanner sc = new Scanner(Main.class.getClassLoader().getResource(filename).openStream());

        for (int i = 0; i < 6; i++)
            sc.nextLine();

        for (int i = 0; i < size; i++) {
            sc.next();
            int x = Integer.parseInt(sc.next());
            int y = Integer.parseInt(sc.next());
            data[i] = new Node(x, y);
        }
        sc.close();
    }

    static double[][] calculate_distance(Node[] nodes) {
        double[][] dist = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double dx = nodes[i].x - nodes[j].x;
                double dy = nodes[i].y - nodes[j].y;
                dist[i][j] = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
            }
        }
        return dist;
    }

    static int[][] closest_vertexes(double[][] dist) {
        int[][] closest = new int[size][10];
        Map<Integer, Double> min_distances = new HashMap();
        for (int node = 0; node < size; node++) {
            min_distances.clear();
            for (int sec_node = 0; sec_node < size; sec_node++) {
                min_distances.put(node, dist[node][sec_node]);
            }

            List<Map.Entry<Integer, Double>> sortedDistances = new ArrayList<>(min_distances.entrySet());
            Collections.sort(sortedDistances, Map.Entry.comparingByValue());
            for (int i = 0; i < 10 && i < sortedDistances.size(); i++) {
                closest[node][i] = sortedDistances.get(i).getKey();
            }
        }
        return closest;
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
        for (int i = 0; i < size; i++) {
            if (distances[i][first_id] > max_dist) {
                second_id = i;
                max_dist = distances[i][first_id];
            }
        }
        return second_id;
    }

    static List<Operation> remove_not_applicable(List<Operation> candidate_moves, ArrayList<Integer>[] cycles) {
        List<Operation> to_remove = new ArrayList<>();
        for (Operation move : candidate_moves) {
            int from_in_first = cycles[0].indexOf(move.from);
            int from_in_second = cycles[1].indexOf(move.from);
            int to_in_first = cycles[0].indexOf(move.to);
            int to_in_second = cycles[1].indexOf(move.to);

            if (move.type.equals("vertex")) {
                if (!(from_in_first != -1 && to_in_second != -1 || from_in_second != -1 && to_in_first != -1)) {
                    to_remove.add(move);
                }
            } else if (move.type.equals("edge")) {
                int from_next_in_first = cycles[0].indexOf(move.from_next);
                int from_next_in_second = cycles[1].indexOf(move.from_next);
                int to_next_in_first = cycles[0].indexOf(move.to_next);
                int to_next_in_second = cycles[1].indexOf(move.to_next);

                if (!(from_in_first != -1 && from_next_in_first != -1 && from_in_first + 1 == from_next_in_first &&
                        to_in_second != -1 && to_next_in_second != -1 && to_in_second + 1 == to_next_in_second ||
                        from_in_second != -1 && from_next_in_second != -1 && from_in_second + 1 == from_next_in_second &&
                                to_in_first != -1 && to_next_in_first != -1 && to_in_first + 1 == to_next_in_first)) {
                    to_remove.add(move);
                }
            }
        }
        candidate_moves.removeAll(to_remove);
        return candidate_moves;
    }

    static List<Operation> steep_vertex_between_two_exchange(
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

                if (cost < 0) {
                    candidate_moves.add(new Operation("vertex", i, j, cost));
                }
            }
        }
        return candidate_moves;
    }

    static List<Operation> steep_edge_exchange(double[][] dist, ArrayList<Integer> cycle, List<Operation> candidate_moves) {
        for (int i = 0; i < cycle.size() - 1; i++) {
            for (int j = 0; j < cycle.size() - 1; j++) {
                if (Math.abs(i - j) < 2)
                    continue;

                int i_value = cycle.get(i);
                int i_next = cycle.get(i + 1);
                int j_value = cycle.get(j);
                int j_next = cycle.get(j + 1);

                double cost = (dist[i_value][j_value] + dist[i_next][j_next]) - (dist[i_value][i_next] + dist[j_value][j_next]);

                if (cost < 0) {
                    candidate_moves.add(new Operation("edge", i_value, i_next, j_value, j_next, cost));
                }
            }
        }
        return candidate_moves;
    }

    private static List<Operation> make_best_move(List<Operation> candidate_moves, ArrayList<Integer>[] cycles) {
        Operation move = candidate_moves.get(0);
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
            } else {
                System.out.println("Nie działa w vertex");
            }

        } else if (move.type.equals("edge")) {
            boolean first = cycles[0].containsAll(Arrays.asList(move.from, move.from_next, move.to, move.to_next));
            boolean second = cycles[1].containsAll(Arrays.asList(move.from, move.from_next, move.to, move.to_next));
            if (first) {
                Collections.reverse(cycles[0].subList(from_in_first + 1, to_in_first + 1));
            } else if (second) {
                Collections.reverse(cycles[1].subList(from_in_second + 1, to_in_second + 1));
            } else {
                System.out.println("Nie działa w edges");
            }
        }
        candidate_moves.remove(move);
        return candidate_moves;
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

    static ArrayList<Integer>[] list_of_moves(double[][] distances, ArrayList<Integer>[] cycles) {
        List<Operation> candidate_moves = new ArrayList<>();

        while (true) {
            System.out.println(get_result(distances, cycles[0]) + get_result(distances, cycles[1]));
            candidate_moves = remove_not_applicable(candidate_moves, cycles);

            candidate_moves = steep_vertex_between_two_exchange(distances, cycles[0], cycles[1], candidate_moves);
            candidate_moves = steep_edge_exchange(distances, cycles[0], candidate_moves);
            candidate_moves = steep_edge_exchange(distances, cycles[1], candidate_moves);

            Collections.sort(candidate_moves, Operation::compareTo);
            if (candidate_moves.isEmpty())
                break;
            else {
                candidate_moves = make_best_move(candidate_moves, cycles);
            }
        }
        return cycles;
    }

    static ArrayList<Integer>[] nearest_vertex(double[][] distances, ArrayList<Integer>[] cycles) {
        List<Operation> candidate_moves = new ArrayList<>();
        var x = closest_vertexes(distances);
        for (int node = 0; node < cycles[0].size() - 1; node++) {

        }
        for (int node = 0; node < cycles[0].size() - 1; node++) {

        }
        return cycles;
    }

    public static void main(String[] args) throws IOException {
        Node[] nodes = new Node[size];
        load_data(nodes, "kroA100.tsp");
        double[][] distances = calculate_distance(nodes);

        Random rand = new Random();
        int first_id = rand.nextInt(size);
        int second_id = find_second_starting_node(first_id, distances);
        ArrayList<Integer> cycles[];

        cycles = generate_greedy_cycles(distances, first_id, second_id);

        cycles = list_of_moves(distances, cycles);
        cycles = nearest_vertex(distances, cycles);
        print_result(distances, cycles, args);
        cycles[0].forEach(i -> System.out.print(i + " "));
        System.out.println();
        cycles[1].forEach(i -> System.out.print(i + " "));
    }


}