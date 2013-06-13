package pl.kata.kompresja;

import java.io.*;
import java.util.Arrays;

public class LZ77 implements Compressor, Decompressor {
    private final int dictSize;
    private final int bufferSize;
    private final SymbolChannelOutputStreamFactory outputFactory;
    private final SymbolChannelInputStreamFactory inputFactory;

    public LZ77(int aDictSize, int aBufferSize, SymbolChannelStreamFactory factory) {
        dictSize = aDictSize;
        bufferSize = aBufferSize;
        outputFactory = factory;
        inputFactory = factory;
    }

    @Override
    public void compress(InputStream in, OutputStream out) throws IOException {
        SlidingWindow window = new SlidingWindow(dictSize, bufferSize);
        int offsetTagSize = (int) Math.ceil(Math.log(dictSize)/Math.log(2));
        int lengthTagSize = (int) Math.ceil(Math.log(bufferSize)/Math.log(2));
        SymbolChannelOutputStream output = outputFactory.getOutputStream(out, offsetTagSize, lengthTagSize, 8);

        byte firstByte = (byte) in.read();

        window.readFirst(firstByte);
        window.read(in, bufferSize);

        output.writeSymbol(firstByte, 2);
        while(!window.isCodingBufferEmpty()) {
            int[] t = window.longestMatch();
            output.writeSymbol(t[0], 0);
            output.writeSymbol(t[1], 1);
            output.writeSymbol(t[2], 2);

            window.shift(t[1] +1);
            window.read(in, t[1] +1);
        }

        output.close();
    }

    @Override
    public void decompress(InputStream in, OutputStream output) throws IOException {
        SlidingWindow window = new SlidingWindow(dictSize, bufferSize);
        int offsetTagSize = (int) Math.ceil(Math.log(dictSize)/Math.log(2));
        int lengthTagSize = (int) Math.ceil(Math.log(bufferSize)/Math.log(2));
        SymbolChannelInputStream input = inputFactory.getInputStream(in, offsetTagSize, lengthTagSize, 8);

        byte firstByte = (byte)input.readSymbol(2);
        window.readFirst(firstByte);
        output.write(firstByte);

        while(input.hasMore()) {
            int offset = input.readSymbol(0);
            int length = input.readSymbol(1);
            int character = input.readSymbol(2);
            byte[] decoded = window.getSubstring(offset, length);

            byte[] ds = Arrays.copyOf(decoded, decoded.length + 1);
            ds[decoded.length] = (byte)character;
            output.write(ds);

            InputStream stream = new ByteArrayInputStream(ds);

            window.read(stream, length+1);
            window.shift(length+1);
        }
    }
}
