package pl.kata.kompresja;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: kata
 * Date: 11.06.13
 * Time: 10:17
 * To change this template use File | Settings | File Templates.
 */
public interface Decompressor {
    void decompress(InputStream in, OutputStream output) throws IOException;
}
