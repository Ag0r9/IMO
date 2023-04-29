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

    static List<Integer> get_random_order() {
        Random rand = new Random();
        List<Integer> indexes = IntStream.range(1, size / 2).boxed().collect(Collectors.toList());
        for (int i = 0; i < indexes.size(); i++) {
            Collections.swap(indexes, i, rand.nextInt(indexes.size()));
        }
        return indexes;
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
            int first_id = rand.nextInt(size);
            int second_id = find_second_starting_node(first_id, distances);
            ArrayList[] cycles = generate_random_cycles(first_id, second_id);

            double gain = -1;
            while (gain < 0) {
                gain = greedy_vertex_between_two_exchange(distances, cycles[0], cycles[1]);
                gain += greedy_edge_exchange(distances, cycles[0]);
                gain += greedy_edge_exchange(distances, cycles[1]);
            }
            double total_dist = get_result(distances, cycles[0]) + get_result(distances, cycles[1]);
            if (total_dist < best_dist) {
                best_cycles = cycles;
                best_dist = total_dist;
            }
        }
        return best_cycles;
    }


}