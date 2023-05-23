import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class HelperFunctions {

    static double get_result(double[][] distances, List<Integer> cycle) {
        double total_dist = 0.0;
        for (int node_no = 0; node_no < cycle.size() - 1; node_no++) {
            total_dist += distances[cycle.get(node_no)][cycle.get(node_no + 1)];
        }

        return total_dist;
    }

    static double get_total_dist(double[][] distances, Main.Cycles cycles) {
        return get_result(distances, cycles.first_cycle) + get_result(distances, cycles.second_cycle);
    }

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

        for (int i = 0; i < Main.size; i++) {
            sc.next();
            int x = Integer.parseInt(sc.next());
            int y = Integer.parseInt(sc.next());
            data[i] = new Node(x, y);
        }
        sc.close();
    }

    static double[][] calculate_distance(Node[] nodes) {
        double[][] dist = new double[Main.size][Main.size];
        for (int i = 0; i < Main.size; i++) {
            for (int j = 0; j < Main.size; j++) {
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
        for (int i = 0; i < Main.size; i++) {
            if (distances[i][first_id] > max_dist) {
                second_id = i;
                max_dist = distances[i][first_id];
            }
        }
        return second_id;
    }

}
