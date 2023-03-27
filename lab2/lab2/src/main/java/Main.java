import java.util.*;
import java.io.*;

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

    static ArrayList<Integer>[] greedy_cycle(double[][] dist, int first_start, int second_start) {
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


    static ArrayList<Integer>[] greedy_vertex_exchange(
            double[][] dist, ArrayList<Integer> first_cycle, ArrayList<Integer> second_cycle) {
        int are_cycles_the_same = first_cycle.equals(second_cycle) ? 1 : -1;
        //jesli działamy wewnątrz jednego cyklu, to nie porównuj wierzchołków oddalonych
        //o mniej niż dwa, bo już są ze sobą połączone. Jesli są z innych cykli omin warunek,
        //gdyz abs > -2
        for (int i = 1; i < first_cycle.size() - 1; i++) {
            for (int j = 1; j < second_cycle.size() - 1; j++) {
                if (Math.abs(i - j) < 2 * are_cycles_the_same)
                    continue;

                int i_prev = first_cycle.get(i-1);
                int i_value = first_cycle.get(i);
                int i_next = first_cycle.get(i+1);

                int j_prev = second_cycle.get(j-1);
                int j_value = second_cycle.get(j);
                int j_next = second_cycle.get(j+1);

                double gain =
                        (dist[i_prev][j_value] + dist[j_value][i_next] +
                        dist[j_prev][i_value] + dist[i_value][j_next]) -
                                (dist[i_prev][i_value] + dist[i_value][i_next] +
                                dist[j_prev][j_value] + dist[j_value][j_next]);

                if (gain < 0) {
                    first_cycle.set(i, j_value);
                    second_cycle.set(j, i_value);

                    ArrayList<Integer> x[] = new ArrayList[]{first_cycle, second_cycle};
                    return x;
                }
            }
        }
        ArrayList<Integer> x[] = new ArrayList[]{first_cycle, second_cycle};
        return x;
    }

    public static void main(String[] args) throws IOException {
        Node[] nodes = new Node[100];
        load_data(nodes, "kroA100.tsp");
        double[][] distances = calculate_distance(nodes);

        Random rand = new Random();
        int first_id = rand.nextInt(100);
        int second_id = find_second_starting_node(first_id, distances);

        ArrayList<Integer> x[] = greedy_cycle(distances, first_id, second_id);
        var y = x;
        double d = 0.0;
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < 50; i++) {
                d += distances[y[j].get(i)][y[j].get(i + 1)];
            }
        }
        System.out.println("Greedy cycle:             " + d);
        int q = 0;
        var w =y;
        do{
            w = x;
            x = greedy_vertex_exchange(distances,w[0],w[0]);
            q++;
        }while(q<100);

        y[0]=x[0];
        d = 0.0;
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < 50; i++) {
                d += distances[y[j].get(i)][y[j].get(i + 1)];
            }
        }
        System.out.println("Greedy cycle, first cycle:"+ d);

        q = 0;
        x = y;
        do{
            w = x;
            x = greedy_vertex_exchange(distances,w[1],w[1]);
            q++;
        }while(q<100);

        y[1]=x[1];
        d = 0.0;
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < 50; i++) {
                d += distances[y[j].get(i)][y[j].get(i + 1)];
            }
        }
        System.out.println("Greedy cycle, second cycle:"+ d);
    }
}