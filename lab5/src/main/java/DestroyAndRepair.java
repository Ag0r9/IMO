import java.util.*;

public class DestroyAndRepair {
    static Main.Cycles destroy_and_repair(double[][] distances, Main.Cycles cycles) {
        List<Nearest> nearests = new ArrayList<>();
        //utworz listę najbliższych sobie wierzchołków
        for (int i = 1; i < cycles.first_cycle.size() - 1; i++) {
            var val = cycles.first_cycle.get(i);
            for (int x = val + 1; x < Main.size; x++) {
                if (cycles.first_cycle.contains(x) || (x == cycles.first_cycle.get(0)) || (x == cycles.second_cycle.get(0))) {
                    continue;
                }
                nearests.add(new Nearest(val, x, distances[val][x]));
            }
        }
        for (int i = 1; i < cycles.second_cycle.size() - 1; i++) {
            var val = cycles.second_cycle.get(i);
            for (int x = val + 1; x < Main.size; x++) {
                if (cycles.second_cycle.contains(x) || (x == cycles.first_cycle.get(0)) || (x == cycles.second_cycle.get(0))) {
                    continue;
                }
                nearests.add(new Nearest(x, val, distances[val][x]));
            }
        }
        Collections.sort(nearests);
        //posortuj, by znaleźć najbliższe sobie

        Set<Integer> removed_nodes = new HashSet<>();

        //idiotyczna inicjalizacja tablicy pomocnicznej, bo zazwyczaj idzie po referencji zamiast po wartościach XD
        Main.Cycles cycles_for_destroy = new Main.Cycles(cycles.first_cycle, cycles.second_cycle);

        //usun 20 procent najbliższych sobie wierzchołków, które są w osobnych cyklach
        for (int i = 0; i < nearests.size() &&
                cycles_for_destroy.first_cycle.size() + cycles_for_destroy.second_cycle.size() > Main.size * 0.8; i++) {
            cycles_for_destroy.first_cycle.remove((Integer) nearests.get(i).x);
            removed_nodes.add(nearests.get(i).x);
            cycles_for_destroy.second_cycle.remove((Integer) nearests.get(i).y);
            removed_nodes.add(nearests.get(i).y);
        }

        //usun losowe 6 procent wierzchołków
        Random rand = new Random();
        for (int i = 0; i < 0.03 * Main.size; i++) {
            int id_1 = rand.nextInt(cycles_for_destroy.first_cycle.size() - 2) + 1;
            int element_1 = cycles_for_destroy.first_cycle.get(id_1);
            cycles_for_destroy.first_cycle.remove(id_1);
            int id_2 = rand.nextInt(cycles_for_destroy.first_cycle.size() - 2) + 1;
            int element_2 = cycles_for_destroy.first_cycle.get(id_2);
            cycles_for_destroy.first_cycle.remove(id_2);
            removed_nodes.add(element_1);
            removed_nodes.add(element_2);
        }

        ArrayList<Integer> removed_nodes_ale_to_lista = new ArrayList<>(removed_nodes);
        //napraw, również za pomocą greedy cycle
        while (!removed_nodes_ale_to_lista.isEmpty() && cycles_for_destroy.first_cycle.size() < 101 && cycles_for_destroy.second_cycle.size() < 101) {
            GreedyCycle.cycle_creation(distances, removed_nodes_ale_to_lista, cycles_for_destroy.second_cycle);
            GreedyCycle.cycle_creation(distances, removed_nodes_ale_to_lista, cycles_for_destroy.first_cycle);
        }
        if (cycles_for_destroy.first_cycle.size() == 101) {
            while (!removed_nodes_ale_to_lista.isEmpty())
                GreedyCycle.cycle_creation(distances, removed_nodes_ale_to_lista, cycles_for_destroy.second_cycle);
        } else {
            while (!removed_nodes_ale_to_lista.isEmpty())
                GreedyCycle.cycle_creation(distances, removed_nodes_ale_to_lista, cycles_for_destroy.first_cycle);
        }

        cycles_for_destroy = Steepest.steepest(distances, cycles_for_destroy);
        //jak jest poprawa to zwróć poprawione, jak nie to CHLIP, ale no trudno i zwróć stare
        if (HelperFunctions.get_total_dist(distances, cycles) - HelperFunctions.get_total_dist(distances, cycles_for_destroy) > 0) {
            return cycles_for_destroy;
        } else {
            return cycles;
        }
    }

    static class Nearest implements Comparable {
        int x;
        int y;
        double evaluation;

        Nearest(int x, int y, double evaluation) {
            this.x = x;
            this.y = y;
            this.evaluation = evaluation;
        }

        @Override
        public int compareTo(Object o) {
            return Double.compare(evaluation, ((Nearest) o).evaluation);
        }
    }

}
