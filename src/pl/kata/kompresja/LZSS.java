package pl.kata.kompresja;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: kata
 * Date: 11.06.13
 * Time: 08:16
 * To change this template use File | Settings | File Templates.
 */
public class LZSS implements Compressor, Decompressor{
    private final int dictSize;
    private final int bufferSize;
    private final SymbolChannelOutputStreamFactory outputFactory;
    private final SymbolChannelInputStreamFactory inputFactory;

    public LZSS(int aDictSize, int aBufferSize, SymbolChannelStreamFactory factory) {
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
        SymbolChannelOutputStream output = outputFactory.getOutputStream(out, 1, offsetTagSize, lengthTagSize, 8);

        byte firstByte = (byte) in.read();

        window.readFirst(firstByte);
        window.read(in, bufferSize);

        output.writeSymbol(firstByte, 3);
        while(!window.isCodingBufferEmpty()) {
            int[] t = window.longestMatch();
            if(t[1] < 2 + (1 + offsetTagSize + lengthTagSize)/9) {
                output.writeSymbol(0, 0);
                output.writeSymbol(window.getFirst(), 3);
                window.shift(1);
                window.read(in, 1);
            } else {
                output.writeSymbol(1, 0);
                output.writeSymbol(t[0], 1);
                output.writeSymbol(t[1], 2);
                window.shift(t[1]);
                window.read(in, t[1]);
            }
        }

        output.close();
    }

    @Override
    public void decompress(InputStream in, OutputStream out) throws IOException {
        SlidingWindow window = new SlidingWindow(dictSize, bufferSize);
        int offsetTagSize = (int) Math.ceil(Math.log(dictSize)/Math.log(2));
        int lengthTagSize = (int) Math.ceil(Math.log(bufferSize)/Math.log(2));
        SymbolChannelInputStream input = inputFactory.getInputStream(in, 1, offsetTagSize, lengthTagSize, 8);

        byte firstByte = (byte)input.readSymbol(3);
        window.readFirst(firstByte);
        out.write(firstByte);

        while(input.hasMore()) {
            int marker = input.readSymbol(0);
            if(marker == 0) {
                byte[] ds = new byte[] {(byte) input.readSymbol(3)};
                out.write(ds);

                InputStream stream = new ByteArrayInputStream(ds);

                window.read(stream, 1);
                window.shift(1);
            } else {
                int offset = input.readSymbol(1);
                int length = input.readSymbol(2);
                byte[] decoded = window.getSubstring(offset, length);

                out.write(decoded);

                InputStream stream = new ByteArrayInputStream(decoded);

                window.read(stream, length+1);
                window.shift(length+1);
            }
        }
    }
}