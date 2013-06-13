package pl.kata.kompresja;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: kata
 * Date: 26.05.13
 * Time: 18:50
 * To change this template use File | Settings | File Templates.
 */
public interface Compressor {
    void compress(InputStream in, OutputStream out) throws IOException;
}
