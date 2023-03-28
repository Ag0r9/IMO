import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    static ArrayList<Integer> greedy_vertex_inside_one_exchange(
            double[][] dist, ArrayList<Integer> cycle) {
        //jesli działamy wewnątrz jednego cyklu, to nie porównuj wierzchołków oddalonych
        //o mniej niż dwa, bo już są ze sobą połączone. Jesli są z innych cykli omin warunek,
        //gdyz abs > -2
        for (int i = 1; i < cycle.size() - 1; i++) {
            for (int j = 1; j < cycle.size() - 1; j++) {
                if (Math.abs(i - j) < 2)
                    continue;

                int i_prev = cycle.get(i - 1);
                int i_value = cycle.get(i);
                int i_next = cycle.get(i + 1);

                int j_prev = cycle.get(j - 1);
                int j_value = cycle.get(j);
                int j_next = cycle.get(j + 1);

                double cost =
                        (dist[i_prev][j_value] + dist[j_value][i_next] +
                                dist[j_prev][i_value] + dist[i_value][j_next]) -
                                (dist[i_prev][i_value] + dist[i_value][i_next] +
                                        dist[j_prev][j_value] + dist[j_value][j_next]);

                if (cost < 0) {
                    Collections.swap(cycle, i, j);
                    return cycle;
                }
            }
        }
        return cycle;
    }

    static ArrayList<Integer>[] greedy_vertex_between_two_exchange(
            double[][] dist, ArrayList<Integer> first_cycle, ArrayList<Integer> second_cycle) {
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
                    first_cycle.set(i, j_value);
                    second_cycle.set(j, i_value);
                    return new ArrayList[]{first_cycle, second_cycle};
                }
            }
        }
        return new ArrayList[]{first_cycle, second_cycle};
    }

    static ArrayList<Integer>[] steep_vertex_between_two_exchange(
            double[][] dist, ArrayList<Integer> first_cycle, ArrayList<Integer> second_cycle) {
        double min_cost = Double.MAX_VALUE;
        int i_idx_to_switch = -1, j_idx_to_switch = -1;
        int are_cycles_the_same = first_cycle.equals(second_cycle) ? 1 : -1;
        //jesli działamy wewnątrz jednego cyklu, to nie porównuj wierzchołków oddalonych
        //o mniej niż dwa, bo już są ze sobą połączone. Jesli są z innych cykli omin warunek,
        //gdyz abs > -2
        for (int i = 1; i < first_cycle.size() - 1; i++) {
            for (int j = 1; j < second_cycle.size() - 1; j++) {
                if (Math.abs(i - j) < 2 * are_cycles_the_same)
                    continue;

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
                }
            }
        }
        if (min_cost != Double.MAX_VALUE) {
            first_cycle.set(i_idx_to_switch, second_cycle.get(j_idx_to_switch));
            second_cycle.set(j_idx_to_switch, first_cycle.get(i_idx_to_switch));
        }
        return new ArrayList[]{first_cycle, second_cycle};
    }

    static ArrayList<Integer> steep_vertex_inside_one_exchange(
            double[][] dist, ArrayList<Integer> cycle) {
        double min_cost = Double.MAX_VALUE;
        int i_idx_to_switch = -1, j_idx_to_switch = -1;
        for (int i = 1; i < cycle.size() - 1; i++) {
            for (int j = 1; j < cycle.size() - 1; j++) {
                if (Math.abs(i - j) < 2)
                    continue;

                int i_prev = cycle.get(i - 1);
                int i_value = cycle.get(i);
                int i_next = cycle.get(i + 1);

                int j_prev = cycle.get(j - 1);
                int j_value = cycle.get(j);
                int j_next = cycle.get(j + 1);

                double cost =
                        (dist[i_prev][j_value] + dist[j_value][i_next] +
                                dist[j_prev][i_value] + dist[i_value][j_next]) -
                                (dist[i_prev][i_value] + dist[i_value][i_next] +
                                        dist[j_prev][j_value] + dist[j_value][j_next]);

                if (cost < 0 && min_cost > cost) {
                    min_cost = cost;
                    i_idx_to_switch = i;
                    j_idx_to_switch = j;
                }
            }
        }
        if (min_cost != Double.MAX_VALUE) {
            Collections.swap(cycle, i_idx_to_switch, j_idx_to_switch);
        }
        return cycle;
    }

    static ArrayList<Integer> greedy_edge_exchange(double[][] dist, ArrayList<Integer> first_cycle) {
        for (int i = 0; i < first_cycle.size() - 1; i++) {
            for (int j = 0; j < first_cycle.size() - 1; j++) {
                if (Math.abs(i - j) < 3)
                    continue;

                int i_value = first_cycle.get(i);
                int i_next = first_cycle.get(i + 1);

                int j_value = first_cycle.get(j);
                int j_next = first_cycle.get(j + 1);

                double cost = (dist[i_value][j_value] + dist[i_next][j_next]) - (dist[i_value][i_next] + dist[j_value][j_next]);
                if (cost < 0) {
                    Collections.reverse(first_cycle.subList(i + 1, j + 1));
                    return first_cycle;
                }
            }
        }
        return first_cycle;
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

    static void count_result(double distances[][], ArrayList<Integer>[] cycles, String type) {
        double total_dist = 0.0;
        for (int cycle_no = 0; cycle_no < cycles.length; cycle_no++) {
            for (int node_no = 0; node_no < cycles[cycle_no].size() - 1; node_no++) {
                total_dist += distances[cycles[cycle_no].get(node_no)][cycles[cycle_no].get(node_no + 1)];
            }
        }
        System.out.println(type + "  " + total_dist);
    }

    static ArrayList<Integer>[] generate_random_cycles(int first_id, int second_id) {
        List<Integer> not_used = IntStream.range(0, 100).filter(i -> i != first_id && i != second_id).boxed().collect(Collectors.toList());
        ArrayList<Integer> first_cycle = new ArrayList<>() {{
            add(first_id);
        }};
        ArrayList<Integer> second_cycle = new ArrayList<>() {{
            add(second_id);
        }};

        Random rand = new Random();
        while (first_cycle.size() < 50) {
            int idx = rand.nextInt(not_used.size());
            first_cycle.add(idx);
            not_used.remove(idx);
        }
        first_cycle.add(first_id);
        while (second_cycle.size() < 50) {
            int idx = rand.nextInt(not_used.size());
            second_cycle.add(idx);
            not_used.remove(idx);
        }
        second_cycle.add(first_id);
        return new ArrayList[]{first_cycle, second_cycle};
    }

    public static void main(String[] args) throws IOException {
        Node[] nodes = new Node[100];
        load_data(nodes, "kroA100.tsp");
        double[][] distances = calculate_distance(nodes);

        Random rand = new Random();
        int first_id = rand.nextInt(100);
        int second_id = find_second_starting_node(first_id, distances);

        ArrayList<Integer> cycles[];
        if (args[0].equals("random"))
            cycles = generate_random_cycles(first_id, second_id);
        else
            cycles = generate_greedy_cycles(distances, first_id, second_id);

        if (args[1].equals("steep")) {
            cycles = steep_vertex_between_two_exchange(distances, cycles[0], cycles[1]);

            if (args[2].equals("edges")) {
                cycles[0] = steep_edge_exchange(distances, cycles[0]);
                cycles[1] = steep_edge_exchange(distances, cycles[1]);
            } else {
                cycles[0] = steep_vertex_inside_one_exchange(distances, cycles[0]);
                cycles[1] = steep_vertex_inside_one_exchange(distances, cycles[1]);
            }
        } else if (args[1].equals("greedy")) {
            cycles = greedy_vertex_between_two_exchange(distances, cycles[0], cycles[1]);

            if (args[2] == "edges") {
                cycles[0] = greedy_edge_exchange(distances, cycles[0]);
                cycles[1] = greedy_edge_exchange(distances, cycles[1]);
            } else {
                cycles[0] = greedy_vertex_inside_one_exchange(distances, cycles[0]);
                cycles[1] = greedy_vertex_inside_one_exchange(distances, cycles[1]);
            }
        }
        count_result(distances, cycles, args[0] + " " + args[1] + " " + args[2]);
    }
}