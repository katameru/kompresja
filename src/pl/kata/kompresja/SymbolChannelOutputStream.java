package pl.kata.kompresja;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: kata
 * Date: 11.06.13
 * Time: 18:43
 * To change this template use File | Settings | File Templates.
 */
public interface SymbolChannelOutputStream {
    void writeSymbol(int symbol, int channel);
    void close() throws IOException;
}
