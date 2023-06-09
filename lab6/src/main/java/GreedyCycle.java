import java.util.*;

public class GreedyCycle {
    static void cycle_creation(double[][] dist, List<Integer> not_used, List<Integer> solution) {
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

    static Main.Cycles generate_greedy_cycles(double[][] dist, int first_start, int second_start) {
        ArrayList<Integer> not_used = new ArrayList<>();
        for (int i = 0; i < Main.size; i++) {
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

        while (solution1.size() < Main.size / 2 + 1) {
            cycle_creation(dist, not_used, solution1);
            cycle_creation(dist, not_used, solution2);
        }
        return new Main.Cycles(solution1, solution2);
    }


    static Main.Cycles generate_grasp_cycles(double[][] dist, int first_start, int second_start) {
        ArrayList<Integer> not_used = new ArrayList<>();
        for (int i = 0; i < Main.size; i++) {
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

        while (solution1.size() < Main.size / 2 + 1) {
            grasp_creation(dist, not_used, solution1);
            grasp_creation(dist, not_used, solution2);
        }
        return new Main.Cycles(solution1, solution2);
    }


    static void grasp_creation(double[][] dist, List<Integer> not_used, List<Integer> solution) {
        List<List<Integer>> options = new ArrayList<>();
        for (int i : not_used) {
            for (int j = 0; j + 1 < solution.size(); j++) {
                double w = dist[solution.get(j)][i] + dist[i][solution.get(j + 1)] - dist[solution.get(j)][solution.get(j + 1)];
                options.add(new ArrayList<>(List.of((int)w,  i,  solution.get(j))));
            }
        }
        options.sort(Comparator.comparingDouble(a-> a.get(0)));
        Random random = new Random();
        int chosen = random.nextInt(6);
        solution.add(solution.indexOf(  options.get(chosen).get(2))+1, options.get(chosen).get(1));
        not_used.remove(options.get(chosen).get(1));
    }

}
