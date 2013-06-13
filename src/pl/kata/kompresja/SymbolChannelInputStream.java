package pl.kata.kompresja;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: kata
 * Date: 11.06.13
 * Time: 18:50
 * To change this template use File | Settings | File Templates.
 */
public interface SymbolChannelInputStream {
    boolean hasMore();
    int readSymbol(int channel) throws IOException;
}
