package pl.kata.kompresja;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: kata
 * Date: 11.06.13
 * Time: 19:10
 * To change this template use File | Settings | File Templates.
 */
public interface SymbolChannelInputStreamFactory {
    SymbolChannelInputStream getInputStream(InputStream in, int... channelSizes);
}
