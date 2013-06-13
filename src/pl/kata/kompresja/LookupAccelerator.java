package pl.kata.kompresja;

import java.nio.ByteBuffer;
import java.util.*;



public class LookupAccelerator {

    FastMap suggestions;
    byte[] buffer;

    int sizeLimit;
    Integer sinceStart;
    Integer discarded;

    public LookupAccelerator(int limit) {
        sizeLimit = limit;
        buffer = new byte[2];

        suggestions = new FastMap();
        sinceStart = 0;
        discarded = 0;
    }

    private int trigramToInt(byte... T) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.put((byte)0);
        bb.put(T[0]);
        bb.put(T[1]);
        bb.put(T[2]);
        return bb.getInt(0);
    }

    public void addLetter(byte letter) {
        if(sinceStart < 2) {
            buffer[sinceStart] = letter;
            sinceStart++;
        } else {
            int trigram = trigramToInt(buffer[0], buffer[1], letter);
            buffer[0] = buffer[1]; buffer[1] = letter;

            if(!suggestions.containsKey(trigram)) {
                suggestions.put(trigram, new TreeSet<Integer>());
            }
            TreeSet<Integer> s = suggestions.get(trigram);
            s.add(sinceStart - 2);
            suggestions.put(trigram, s);
            sinceStart++;
            if(sinceStart>sizeLimit) {
                discarded++;
            }
        }

    }

    public void addLetters(byte[] letters) {
        for(byte l : letters) {
            addLetter(l);
        }
    }


    public int[] getIndexSuggestions(byte... trigram) {
        Integer tg = trigramToInt(trigram);

        if(!suggestions.containsKey(tg) || suggestions.get(tg).isEmpty()) {
            return new int[0];
        }

        TreeSet<Integer> sugs = (TreeSet<Integer>) suggestions.get(tg).tailSet(discarded);
        Object[] suggest = sugs.toArray();
        int[] ss = new int[suggest.length];

        for(int i = 0; i < suggest.length; i++) {
            ss[i] = (int)suggest[i] - discarded ;
        }
        suggestions.put(tg, sugs);
        return ss;
    }

    private static class FastMap {

        TreeSet[] array = new TreeSet[256*256*256];

        public boolean containsKey(int trigram) {
            return array[trigram] != null;
        }

        public void put(int trigram, TreeSet<Integer> integers) {
            array[trigram] = integers;
        }

        public TreeSet<Integer> get(Integer tg) {
            return array[tg];
        }
    }
}
