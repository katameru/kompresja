package pl.kata.kompresja;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kata
 * Date: 11.06.13
 * Time: 20:23
 * To change this template use File | Settings | File Templates.
 */
public class Instrumentation {

    private List<Stat> stats;

    public Instrumentation() {
        stats = new ArrayList<>(10);
    }

    public void add(String shortName, String longName, String unit, Object stat) {
        stats.add(new Stat(shortName, longName, unit, stat));
    }

    public List<Stat> getStats() {
        Collections.sort(stats);
        return stats;
    }

    @Override
    public String toString() {
        Collections.sort(stats);
        String res = "";
        for(Stat s : stats) {
            res = res + s.toString() + "\n";
        }
        return res;
    }

    public static class Stat implements Comparable<Stat> {
        private String shortName;
        private String longName;
        private String unit;
        private Object val;

        public Stat(String aShortName, String aLongName, String aUnit, Object aVal) {
            shortName = aShortName;
            longName = aLongName;
            unit = aUnit;
            val = aVal;
        }

        @Override
        public int compareTo(Stat o) {
            return shortName.compareTo(o.shortName);
        }

        @Override
        public String toString() {
            return longName + ": " + val + unit;
        }
    }
}
