package pl.kata.kompresja;

import java.io.IOException;
import java.util.*;

class CompressedOutputStream implements SymbolChannelOutputStream {

    private final BitOutputStream outputStream;
    private final int[] symbolSizes;
    private final List<Integer>[] outputBuffers;
    private final Map<Integer, Integer>[] symbolCounts;
    private final Queue<Integer> channels;
    private long uncompressedSize;
    private long compressedSize;

    Instrumentation instrumentation;


    public CompressedOutputStream(BitOutputStream out, int symbolSize[]) {
        outputStream = out;
        symbolSizes = symbolSize;
        outputBuffers = new List[symbolSize.length];
        symbolCounts = new Map[symbolSize.length];
        for(int i = 0; i < symbolSize.length; i++) {
            outputBuffers[i] = new LinkedList<>();
            symbolCounts[i] = new HashMap<>();
        }
        channels = new LinkedList<>();
        uncompressedSize = 0;
        compressedSize = 0;
    }

    void setInstrumentation(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public void writeSymbol(int symbol, int channel) {
        outputBuffers[channel].add(symbol);
        if(!symbolCounts[channel].containsKey(symbol)) {
            symbolCounts[channel].put(symbol, 0);
        }
        Integer count = symbolCounts[channel].get(symbol);
        count++;
        symbolCounts[channel].put(symbol, count);
        channels.add(channel);
        uncompressedSize += symbolSizes[channel];
    }

    @Override
    public void close() throws IOException {
        Tree[] codeTrees = new Tree[symbolSizes.length];
        int padding = 0;
        int treeSize = 0;
        for(int i = 0; i < codeTrees.length; i++) {
            codeTrees[i] = Tree.construct(symbolCounts[i], symbolSizes[i]);
            int treeSizeLocal = codeTrees[i].encodedTreeLength();
            treeSize += treeSizeLocal;
            padding+=treeSizeLocal%8;
            for(Integer symbol : outputBuffers[i]) {
                padding+=codeTrees[i].codeForSymbol(symbol).length%8;
            }
        }
        padding = 8 - (padding % 8);
        for(int i = 1; i < padding; i++) {
            outputStream.putBit((byte) 0);
        }
        outputStream.putBit((byte) 1);

        for (Tree codeTree : codeTrees) {
            codeTree.encode(outputStream);
        }

        for(Integer channel : channels) {
            byte[] code = codeTrees[channel].codeForSymbol(outputBuffers[channel].remove(0));
            outputStream.putBits(code);
            compressedSize += code.length;
        }

        if(instrumentation != null) {
            for(int i = 0; i < symbolSizes.length; i++) {
                long weightedSum = 0;
                long sum = 0;
                for(Map.Entry<Integer, Integer> kv : symbolCounts[i].entrySet()) {
                    weightedSum += kv.getKey()*kv.getValue();
                    sum += kv.getValue();
                }
                instrumentation.add(i+"_average", "Weighted average symbol value in tree for channel " + i, "", (double)weightedSum/sum);
            }
            instrumentation.add("001uncompressed_size", "Output size before Huffman compression", "bits", uncompressedSize);
            instrumentation.add("002compressed_size", "Output size after Huffman compression", "bits", compressedSize);
            instrumentation.add("004padding", "Byte padding", "bits", padding);
            instrumentation.add("003tree_size", "Huffman code tree size", "bits", treeSize);
        }

        outputStream.close();
    }
}
