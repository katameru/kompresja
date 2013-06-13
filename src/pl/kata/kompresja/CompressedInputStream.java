package pl.kata.kompresja;

import java.io.IOException;

class CompressedInputStream implements SymbolChannelInputStream {
    private final BitInputStream input;
    private boolean ready;
    private final Tree[] codeTrees;
    private final int[] symbolSize;


    public CompressedInputStream(BitInputStream inputStream,  int aSymbolSize[]) {
        input = inputStream;
        symbolSize = aSymbolSize;
        codeTrees = new Tree[symbolSize.length];
        ready = false;
    }

    private void init() throws IOException {
        while(input.getBit() != 1) {}
        for(int i = 0; i < codeTrees.length; i++) {
            codeTrees[i] = Tree.decode(input, symbolSize[i]);
        }
        ready = true;
    }

    @Override
    public boolean hasMore() {
        return input.hasMore();
    }

    @Override
    public int readSymbol(int channel) throws IOException {
        if(!ready) {
            init();
        }
        int symbol = 0;
        byte[] symbolBits = codeTrees[channel].getSymbolFromCode(input);
        for(int i = 0; i < symbolSize[channel]; i++) {
            symbol |= symbolBits[i] << (symbolSize[channel] - 1 - i);
        }
        return symbol;
    }
}
