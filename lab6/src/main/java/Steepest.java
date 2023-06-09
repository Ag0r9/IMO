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

        public void setValid(boolean b) {
            this.valid = b;
        }
    }


    static Main.Cycles list_of_moves(double[][] distances, Main.Cycles cycles) {
        List<Operation> candidate_moves = new ArrayList<>();
        steep_vertex_between_two_exchange(distances, cycles.first_cycle, cycles.second_cycle, candidate_moves);
        steep_edge_exchange(distances, cycles.first_cycle, candidate_moves);
        steep_edge_exchange(distances, cycles.second_cycle, candidate_moves);

        while (true) {
            candidate_moves.sort(Operation::compareTo);
            Operation best_move = null;
            for (Operation move : candidate_moves) {
                if (move.valid) {
                    best_move = move;
                    break;
                }
            }

            if (candidate_moves.isEmpty() || best_move == null)
                break;

            make_best_move(best_move, cycles);

            Operation finalBest_move = best_move;
            List<Integer> ids_to_update = new ArrayList<>() {{
                addAll(Arrays.asList(finalBest_move.from, finalBest_move.to));
            }};
            if (best_move.type.equals("edge"))
                ids_to_update.addAll(Arrays.asList(best_move.from_next, best_move.to_next));

            ids_to_update.removeAll(Arrays.asList(cycles.first_cycle.get(0), cycles.second_cycle.get(0)));
            candidate_moves.remove(best_move);

            candidate_moves = remove_not_applicable(candidate_moves, ids_to_update, cycles);
            candidate_moves = add_new_moves(candidate_moves, cycles, ids_to_update, distances);

        }
        return cycles;
    }

    private static List<Operation> add_new_moves(List<Operation> candidate_moves, Main.Cycles cycles, List<Integer> ids_to_update, double[][] dist) {
        for (int id : ids_to_update) {
            List<Integer> cycle_with_id = cycles.first_cycle.contains(id) ? cycles.first_cycle : cycles.second_cycle;
            List<Integer> second_cycle = (cycle_with_id == cycles.first_cycle) ? cycles.second_cycle : cycles.first_cycle;

            candidate_moves = vertex_exchange_new_id(candidate_moves, cycle_with_id, second_cycle, dist, id);
            candidate_moves = edge_exchange_new_id(candidate_moves, cycle_with_id, dist, id);

        }
        return candidate_moves;
    }
    static List<Operation> remove_not_applicable(List<Operation> candidate_moves, List<Integer> ids_to_update, Main.Cycles cycles) {
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

            if (cycles.first_cycle.contains(move.from)) {
                if (cycles.first_cycle.indexOf(move.from) + 1 == cycles.first_cycle.indexOf(move.from_next)
                        && cycles.first_cycle.indexOf(move.to) == cycles.first_cycle.indexOf(move.to_next) + 1 ||
                        cycles.first_cycle.indexOf(move.to) + 1 == cycles.first_cycle.indexOf(move.to_next)
                                && cycles.first_cycle.indexOf(move.from) == cycles.first_cycle.indexOf(move.from_next) + 1) {
                    move.setValid(false);
                } else if (cycles.first_cycle.indexOf(move.from) + 1 == cycles.first_cycle.indexOf(move.from_next)
                        && cycles.first_cycle.indexOf(move.to) + 1 == cycles.first_cycle.indexOf(move.to_next)) {
                    move.setValid(true);
                }

            } else {
                if (cycles.second_cycle.indexOf(move.from) + 1 == cycles.second_cycle.indexOf(move.from_next)
                        && cycles.second_cycle.indexOf(move.to) == cycles.second_cycle.indexOf(move.to_next) + 1 ||
                        cycles.second_cycle.indexOf(move.to) + 1 == cycles.second_cycle.indexOf(move.to_next)
                                && cycles.second_cycle.indexOf(move.from) == cycles.second_cycle.indexOf(move.from_next) + 1) {
                    move.setValid(false);
                } else if (cycles.second_cycle.indexOf(move.from) + 1 == cycles.second_cycle.indexOf(move.from_next)
                        && cycles.second_cycle.indexOf(move.to) + 1 == cycles.second_cycle.indexOf(move.to_next)) {
                    move.setValid(true);
                }
            }
        }

        return candidate_moves;
    }

    private static List<Operation> edge_exchange_new_id(List<Operation> candidate_moves, List<Integer> cycle, double[][] dist, int id) {

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

    private static List<Operation> vertex_exchange_new_id(List<Operation> candidate_moves, List<Integer> cycle_with_id, List<Integer> second_cycle, double[][] dist, int id) {
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

}
