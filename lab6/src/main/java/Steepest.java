import java.util.*;

public class Steepest {
    static void steep_vertex_between_two_exchange(
            double[][] dist, List<Integer> first_cycle, List<Integer> second_cycle, List<Operation> candidate_moves) {

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

    static void steep_edge_exchange(double[][] dist, List<Integer> cycle, List<Operation> candidate_moves) {
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
    static Main.Cycles steepest(double[][] distances, Main.Cycles cycles) {

        List<Operation> candidate_moves = new ArrayList<>();
        while (true) {
            candidate_moves.clear();
            Steepest.steep_edge_exchange(distances, cycles.second_cycle, candidate_moves);
            Steepest.steep_edge_exchange(distances, cycles.first_cycle, candidate_moves);
            Steepest.steep_vertex_between_two_exchange(distances, cycles.first_cycle, cycles.second_cycle, candidate_moves);
            if (candidate_moves.isEmpty()) {
                break;
            }
            candidate_moves.sort(Operation::compareTo);
            make_best_move(candidate_moves.get(0), cycles);
        }
        return cycles;
    }
    private static void make_best_move(Operation move, Main.Cycles cycles) {
        var from_in_first = cycles.first_cycle.indexOf(move.from);
        var from_in_second = cycles.second_cycle.indexOf(move.from);
        var to_in_first = cycles.first_cycle.indexOf(move.to);
        var to_in_second = cycles.second_cycle.indexOf(move.to);
        if (move.type.equals("vertex")) {
            if (from_in_first != -1 && to_in_second != -1) {
                cycles.first_cycle.set(from_in_first, move.to);
                cycles.second_cycle.set(to_in_second, move.from);

            } else if (from_in_second != -1 && to_in_first != -1) {
                cycles.second_cycle.set(from_in_second, move.to);
                cycles.first_cycle.set(to_in_first, move.from);
            }

        } else if (move.type.equals("edge")) {
            boolean first = new HashSet<>(cycles.first_cycle).containsAll(Arrays.asList(move.from, move.from_next, move.to, move.to_next));
            boolean second = new HashSet<>(cycles.second_cycle).containsAll(Arrays.asList(move.from, move.from_next, move.to, move.to_next));
            if (first) {
                if (from_in_first > to_in_first) {
                    var temp = from_in_first;
                    from_in_first = to_in_first;
                    to_in_first = temp;
                }
                Collections.reverse(cycles.first_cycle.subList(from_in_first + 1, to_in_first + 1));
            } else if (second) {
                if (from_in_second > to_in_second) {
                    var temp = from_in_second;
                    from_in_second = to_in_second;
                    to_in_second = temp;
                }
                Collections.reverse(cycles.second_cycle.subList(from_in_second + 1, to_in_second + 1));
            }
        }
    }
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

}
