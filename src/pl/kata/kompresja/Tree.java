package pl.kata.kompresja;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kata
 * Date: 08.06.13
 * Time: 20:29
 * To change this template use File | Settings | File Templates.
 */
public abstract class Tree {
    public int counts;
    Set<Integer> symbols;

    public abstract void encode(BitOutputStream out) throws IOException;
    public abstract int encodedTreeLength();
    protected abstract int symbolDepth(Integer symbol);
    public abstract byte[] getSymbolFromCode(BitInputStream input) throws IOException;
    protected abstract void code(Integer symbol, byte[] code, int depth);

    public byte[] codeForSymbol(Integer symbol) {
        byte[] code = new byte[this.symbolDepth(symbol)];
        this.code(symbol, code, 0);
        return code;
    }

    public static Tree decode(BitInputStream encoded, int symbolSize) throws IOException {
        if(encoded.getBit() == 0) {
            Node n = new Node();
            n.left = decode(encoded, symbolSize);
            n.right = decode(encoded, symbolSize);
            return n;
        } else {
            Leaf l = new Leaf();
            l.symbol = new byte[symbolSize];
            encoded.getBits(l.symbol);
            return l;
        }
    }

    public static Tree construct(Map<Integer, Integer> symbol_counts, int symbolSize) {
        PriorityQueue<Tree> cs = new PriorityQueue<>(symbol_counts.size(), new Comparator<Tree>() {
            @Override
            public int compare(Tree o1, Tree o2) {
                return Integer.compare(o1.counts, o2.counts);
            }
        });
        for(Integer k : symbol_counts.keySet()) {
            byte[] bitSymbol = new byte[symbolSize];
            for(int i = bitSymbol.length-1; i >= 0; i--) {
                bitSymbol[i] = (byte)((k & (1<<bitSymbol.length-1-i)) >> bitSymbol.length-1-i);
            }
            Leaf l = new Leaf();
            l.symbol = bitSymbol;
            l.counts = symbol_counts.get(k);
            l.symbols = Collections.singleton(k);
            cs.add(l);
        }
        while(cs.size() > 1) {
            Node t = new Node();
            t.left = cs.remove();
            t.right = cs.remove();
            t.counts = t.left.counts + t.right.counts;
            t.symbols = new HashSet<>(t.left.symbols);
            t.symbols.addAll(t.right.symbols);
            cs.add(t);
        }

        return cs.remove();
    }

    private static class Node extends Tree {
        public Tree left;
        public Tree right;

        @Override
        public void encode(BitOutputStream out) throws IOException {
            out.putBit((byte)0);
            left.encode(out);
            right.encode(out);
        }

        public byte[] getSymbolFromCode(BitInputStream input) throws IOException {
            if(input.getBit() == 0) {
                return  left.getSymbolFromCode(input);
            } else {
                return  right.getSymbolFromCode(input);
            }
        }

        @Override
        public int encodedTreeLength() {
            return 1 + left.encodedTreeLength() + right.encodedTreeLength();
        }

        @Override
        public int symbolDepth(Integer symbol) {
            return 1 + (left.symbols.contains(symbol) ? left.symbolDepth(symbol) : right.symbolDepth(symbol));
        }

        @Override
        public void code(Integer symbol, byte[] code, int depth) {
            if(left.symbols.contains(symbol)) {
                code[depth] = 0;
                left.code(symbol, code, depth+1);
            } else {
                code[depth] = 1;
                right.code(symbol, code, depth + 1);
            }

        }
    }

    private static class Leaf extends Tree {
        byte[] symbol;
        @Override
        public void encode(BitOutputStream out) throws IOException {
            out.putBit((byte)1);
            out.putBits(symbol);
        }

        public byte[] getSymbolFromCode(BitInputStream input) {
            return symbol;
        }


        @Override
        public int encodedTreeLength() {
            return 1 + symbol.length;
        }

        @Override
        public int symbolDepth(Integer symbol) {
            return 0;
        }

        @Override
        public void code(Integer symbol, byte[] code, int depth) {}

    }
}
