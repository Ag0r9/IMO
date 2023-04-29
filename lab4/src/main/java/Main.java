import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Main {
    static int size = 200;

    static class Node {
        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int x;
        int y;
    }

    static class Operation implements Comparable {
        Operation(String type, int from, int to, double evaluation, boolean valid) {
            this.type = type;
            this.from = from;
            this.to = to;
            this.evaluation = evaluation;
            this.from_next = -1;
            this.to_next = -1;
            this.valid = valid;
        }

        Operation(String type, int from, int from_next, int to, int to_next, double evaluation, boolean valid) {
            this.type = type;
            this.from = from;
            this.from_next = from_next;
            this.to = to;
            this.to_next = to_next;
            this.evaluation = evaluation;
            this.valid = valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        String type;
        double evaluation;
        int from;
        int from_next;
        int to;
        int to_next;

        boolean valid;

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

    static void load_data(Node[] data, String filename) throws IOException {
        Scanner sc = new Scanner(Objects.requireNonNull(Main.class.getClassLoader().getResource(filename)).openStream());

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

    static List<Operation> remove_not_applicable(List<Operation> candidate_moves, List<Integer> ids_to_update, ArrayList<Integer>[] cycles) {
        Set<Operation> moves_to_remove = new HashSet<>();
        for (int id : ids_to_update) {
            for (Operation move : candidate_moves) {
                if (Arrays.asList(move.from, move.from_next, move.to, move.to_next).contains(id))
                    moves_to_remove.add(move);
            }
        }
        candidate_moves.removeAll(moves_to_remove);

        for (Operation move : candidate_moves) {
            if (move.type.equals("vertex"))
                continue;

            if (cycles[0].contains(move.from)) {
                if (cycles[0].indexOf(move.from) + 1 == cycles[0].indexOf(move.from_next)
                        && cycles[0].indexOf(move.to) == cycles[0].indexOf(move.to_next) + 1 ||
                        cycles[0].indexOf(move.to) + 1 == cycles[0].indexOf(move.to_next)
                                && cycles[0].indexOf(move.from) == cycles[0].indexOf(move.from_next) + 1) {
                    move.setValid(false);
                } else if (cycles[0].indexOf(move.from) + 1 == cycles[0].indexOf(move.from_next)
                        && cycles[0].indexOf(move.to) + 1 == cycles[0].indexOf(move.to_next)) {
                    move.setValid(true);
                }

            } else {
                if (cycles[1].indexOf(move.from) + 1 == cycles[1].indexOf(move.from_next)
                        && cycles[1].indexOf(move.to) == cycles[1].indexOf(move.to_next) + 1 ||
                        cycles[1].indexOf(move.to) + 1 == cycles[1].indexOf(move.to_next)
                                && cycles[1].indexOf(move.from) == cycles[1].indexOf(move.from_next) + 1) {
                    move.setValid(false);
                } else if (cycles[1].indexOf(move.from) + 1 == cycles[1].indexOf(move.from_next)
                        && cycles[1].indexOf(move.to) + 1 == cycles[1].indexOf(move.to_next)) {
                    move.setValid(true);
                }
            }
        }

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

                if (cost < 0 && !candidate_moves.contains(new Operation("vertex", i_value, j_value, cost, true))) {
                    candidate_moves.add(new Operation("vertex", i_value, j_value, cost, true));
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

                if (cost < 0 && !candidate_moves.contains(new Operation("edge", i_value, i_next, j_value, j_next, cost, true))) {
                    candidate_moves.add(new Operation("edge", i_value, i_next, j_value, j_next, cost, true));
                }
            }
        }
        return candidate_moves;
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
            } else {
                System.out.println("Nie działa w vertex");
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
            } else {
                System.out.println("Nie działa w edges");
            }
        }
    }

    static void print_result(double[][] distances, ArrayList<Integer>[] cycles, String[] type) {
        double total_dist = 0.0;
        for (ArrayList<Integer> cycle : cycles) {
            for (int node_no = 0; node_no < cycle.size() - 1; node_no++) {
                total_dist += distances[cycle.get(node_no)][cycle.get(node_no + 1)];
            }
        }
        StringBuilder args = new StringBuilder();
        for (String s : type) {
            args.append(s).append(" ");
        }
        System.out.println(args.toString() + total_dist);
    }

    static double get_result(double[][] distances, ArrayList<Integer> cycle) {
        double total_dist = 0.0;
        for (int node_no = 0; node_no < cycle.size() - 1; node_no++) {
            total_dist += distances[cycle.get(node_no)][cycle.get(node_no + 1)];
        }

        return total_dist;
    }

    static ArrayList<Integer>[] list_of_moves(double[][] distances, ArrayList<Integer>[] cycles) {
        List<Operation> candidate_moves = new ArrayList<>();
        steep_vertex_between_two_exchange(distances, cycles[0], cycles[1], candidate_moves);
        steep_edge_exchange(distances, cycles[0], candidate_moves);
        steep_edge_exchange(distances, cycles[1], candidate_moves);

        while (true) {
            boolean no_valid_moves = true;
            for (Operation move : candidate_moves) {
                if (move.valid) {
                    no_valid_moves = false;
                    break;
                }
            }

            if (candidate_moves.isEmpty() || no_valid_moves == true)
                break;

            List<Operation> moves_to_random_choose = candidate_moves.stream().filter(move -> move.valid).collect(Collectors.toList());

            Random random = new Random();
            Operation best_move = moves_to_random_choose.get(random.nextInt(moves_to_random_choose.size()));
            make_best_move(best_move, cycles);

            Operation finalBest_move = best_move;
            List<Integer> ids_to_update = new ArrayList<>() {{
                addAll(Arrays.asList(finalBest_move.from, finalBest_move.to));
            }};
            if (best_move.type.equals("edge"))
                ids_to_update.addAll(Arrays.asList(best_move.from_next, best_move.to_next));

            ids_to_update.removeAll(Arrays.asList(cycles[0].get(0), cycles[1].get(0)));
            candidate_moves.remove(best_move);

            candidate_moves = remove_not_applicable(candidate_moves, ids_to_update, cycles);
            candidate_moves = add_new_moves(candidate_moves, cycles, ids_to_update, distances);

        }
        return cycles;
    }

    private static List<Operation> add_new_moves(List<Operation> candidate_moves, ArrayList<Integer>[] cycles, List<Integer> ids_to_update, double[][] dist) {
        for (int id : ids_to_update) {
            ArrayList<Integer> cycle_with_id = cycles[0].contains(id) ? cycles[0] : cycles[1];
            ArrayList<Integer> second_cycle = (cycle_with_id == cycles[0]) ? cycles[1] : cycles[0];

            candidate_moves = vertex_exchange_new_id(candidate_moves, cycle_with_id, second_cycle, dist, id);
            candidate_moves = edge_exchange_new_id(candidate_moves, cycle_with_id, dist, id);

        }
        return candidate_moves;
    }

    private static List<Operation> edge_exchange_new_id(List<Operation> candidate_moves, ArrayList<Integer> cycle, double[][] dist, int id) {

        int id_id = cycle.indexOf(id);
        int i_next = cycle.get(cycle.indexOf(id) + 1);
        for (int j = 1; j < cycle.size() - 1; j++) {
            if (Math.abs(id_id - j) < 2)
                continue;

            int j_value = cycle.get(j);
            int j_next = cycle.get(j + 1);

            double cost = (dist[id][j_value] + dist[i_next][j_next]) - (dist[id][i_next] + dist[j_value][j_next]);

            if (cost < 0 && !candidate_moves.contains(new Operation("edge", id, i_next, j_value, j_next, cost, true)))
                candidate_moves.add(new Operation("edge", id, i_next, j_value, j_next, cost, true));

        }


        return candidate_moves;
    }

    private static List<Operation> vertex_exchange_new_id(List<Operation> candidate_moves, ArrayList<Integer> cycle_with_id, ArrayList<Integer> second_cycle, double[][] dist, int id) {
        int i_prev = cycle_with_id.get(cycle_with_id.indexOf(id) - 1);
        int i_next = cycle_with_id.get(cycle_with_id.indexOf(id) + 1);

        for (int j = 1; j < second_cycle.size() - 1; j++) {
            int j_prev = second_cycle.get(j - 1);
            int j_value = second_cycle.get(j);
            int j_next = second_cycle.get(j + 1);

            double cost =
                    (dist[i_prev][j_value] + dist[j_value][i_next] +
                            dist[j_prev][id] + dist[id][j_next]) -
                            (dist[i_prev][id] + dist[id][i_next] +
                                    dist[j_prev][j_value] + dist[j_value][j_next]);

            if (cost < 0 && !candidate_moves.contains(new Operation("vertex", id, j_value, cost, true))) {
                candidate_moves.add(new Operation("vertex", id, j_value, cost, true));
            }

        }
        return candidate_moves;
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


    public static void main(String[] args) throws IOException {
        Node[] nodes = new Node[size];
        load_data(nodes, "kroA200.tsp");
        double[][] distances = calculate_distance(nodes);
        Random rand = new Random();
        //int first_id = rand.nextInt(size);
        //int second_id = find_second_starting_node(first_id, distances);

        ArrayList[] cycles = MSLS(rand, distances);


        print_result(distances, cycles, args);
        cycles[0].forEach(i -> System.out.print(i + " "));
        System.out.println();
        cycles[1].forEach(i -> System.out.print(i + " "));
    }

    private static ArrayList[] MSLS(Random rand, double[][] distances) {
        ArrayList[] best_cycles = new ArrayList[0];
        double best_dist = Double.MAX_VALUE;
        for (int i = 0; i < 100; i++) {
            ArrayList[] cycles = null;
            int first_id = rand.nextInt(size);
            int second_id = find_second_starting_node(first_id, distances);

            cycles = generate_random_cycles(first_id, second_id);

            list_of_moves(distances, cycles);
            double total_dist = get_result(distances, cycles[0]) + get_result(distances, cycles[1]);
            if (total_dist < best_dist) {
                best_cycles = cycles;
                best_dist = total_dist;
            }
        }
        return best_cycles;
    }


}