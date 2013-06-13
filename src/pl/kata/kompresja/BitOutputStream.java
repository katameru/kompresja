package pl.kata.kompresja;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: kata
 * Date: 08.06.13
 * Time: 01:45
 * To change this template use File | Settings | File Templates.
 */
class BitOutputStream {

    private final OutputStream output;
    private byte buffer;
    private int index;

    public BitOutputStream(OutputStream out) {
        output = out;
        index = 0;
    }

    public void putBit(byte i) throws IOException {
        if(index == 8) {
            output.write(buffer);
            buffer = 0;
            index = 0;
        }
        buffer |= i << (7-index);
        index++;
    }

    public void putBits(byte[] symbol) throws IOException {
        for(byte b : symbol) {
            putBit(b);
        }
    }

    public void close() throws IOException {
        while(index != 8) {
            putBit((byte) 0);
        }
        output.write(buffer);
        output.close();
    }
}
