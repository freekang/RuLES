package de.mpii.util;

import java.io.PrintWriter;
import java.util.*;

/**
 * Created by hovinhthinh on 3/21/18.
 */
public class DataSamplingPredicates {

    public static ArrayList<String[]> filter_1(ArrayList<String[]> input) {
        HashMap<String, Integer> e2deg = new HashMap<>();
        for (String[] s : input) {
            e2deg.put(s[0], e2deg.getOrDefault(s[0], 0) + 1);
            e2deg.put(s[2], e2deg.getOrDefault(s[2], 0) + 1);
        }
        ArrayList<String[]> good = new ArrayList<>();
        for (String[] s : input) {
            if (e2deg.get(s[0]) <= 1 || e2deg.get(s[2]) <= 1) {
                continue;
            }
            good.add(s);
        }
        return good;
    }

    public static void writeStat(ArrayList<String[]> input) {
        HashSet<String> e = new HashSet<>();
        HashMap<String, PredStats> p = new HashMap<String, PredStats>();
        for (String[] s : input) {
            e.add(s[0]);
            e.add(s[2]);
            if (!p.containsKey(s[1])) {
                p.put(s[1], new PredStats());
            }
            PredStats stat = p.get(s[1]);
            stat.count++;
            stat.US.add(s[0]);
            stat.UO.add(s[2]);
        }
        System.out.println("--------------------------------------");
        System.out.println("nEntities: " + e.size());
        System.out.println("nPredicates: " + p.size());
        System.out.println("nFacts: " + input.size());
        System.out.println("--------------------------------------");

        ArrayList<Map.Entry<String, PredStats>> pred2count = new ArrayList<>(p.entrySet());
        Collections.sort(pred2count, new Comparator<Map.Entry<String, PredStats>>() {
            @Override
            public int compare(Map.Entry<String, PredStats> o1, Map.Entry<String, PredStats> o2) {
                return Integer.compare(o2.getValue().count, o1.getValue().count);
            }
        });
        for (Map.Entry<String, PredStats> stat : pred2count) {
            System.out.printf("%s\tF: %d\tUS: %d\tUO: %d\n", stat.getKey(), stat.getValue().count, stat.getValue()
                    .US.size(), stat.getValue().UO.size());
        }
    }

    // args: <input> <#pred> <remove_1_rounds> <output>
    public static void main(String[] args) throws Exception {
//        args = "../data/visualGnome/raw 100 50 ../data/visualGnome/good".split("\\s++");
        List<String> input = IO.readlines(args[0]);
        HashMap<String, Integer> pred2count = new HashMap<>();
        int limPred = Integer.parseInt(args[1]);

        for (String l : input) {
            String pred = l.split("\t")[1];
            pred2count.put(pred, pred2count.getOrDefault(pred, 0) + 1);
        }
        ArrayList<Map.Entry<String, Integer>> topPreds = new ArrayList<>(pred2count.entrySet());
        Collections.sort(topPreds, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return Integer.compare(o2.getValue(), o1.getValue());
            }
        });

        HashSet<String> chosenPred = new HashSet<>();
        for (int i = 0; i < topPreds.size(); ++i) {
            if (i >= limPred) {
                break;
            }
            chosenPred.add(topPreds.get(i).getKey());
        }

        Set<String> ignorePred = new HashSet<>(
//                Arrays.asList("ON", "has", "IN", "OF", "with", "behind", "near",
//                        "WEARING", "next to", "on top of")
        );
        ArrayList<String[]> good = new ArrayList<>();
        for (String l : input) {
            String[] pred = l.split("\t");
            if (chosenPred.contains(pred[1]) && !pred[0].equals(pred[2]) && !ignorePred.contains(pred[1])) {
                good.add(pred);
            }
        }

        int rm_1 = Integer.parseInt(args[2]);
        for (int i = 0; i < rm_1; ++i) {
            ArrayList<String[]> filtered = filter_1(good);
            if (good.size() == filtered.size()) {
                System.out.println("Cannot filter more.");
                break;
            }
            good = filtered;
        }

        PrintWriter out = IO.openForWrite(args[3]);
        for (String[] s : good) {
            out.printf("%s\t%s\t%s\n", s[0], s[1], s[2]);
        }
        out.close();
        writeStat(good);
    }

    public static class PredStats {
        int count = 0;
        HashSet<String> US = new HashSet<>(), UO = new HashSet<>();
    }
}
