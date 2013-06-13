package pl.kata.kompresja;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: kata
 * Date: 06.06.13
 * Time: 23:18
 * To change this template use File | Settings | File Templates.
 */
class BitInputStream {

    private final InputStream input;
    private byte buffer;
    private byte index;
    private boolean empty;
    private boolean finished;

    public BitInputStream(InputStream in) {
        input = in;
        index = 0;
        empty = true;
        finished = false;
    }

    public boolean hasMore() {
        return index < 8 || !finished;
    }

    public byte getBit() throws IOException {
        if(index == 8 || empty) {
            buffer = (byte)input.read();
            if(input.available() < 1) {
                finished = true;
            }
            index = 0;
            empty = false;
        }
        byte res = (byte)((buffer & (1 << (7-index))) >> (7-index));
        index++;
        return res;
    }

    public void getBits(byte[] dst) throws IOException {
        for(int i = 0; i < dst.length; i++) {
            dst[i] = getBit();
        }
    }
}
