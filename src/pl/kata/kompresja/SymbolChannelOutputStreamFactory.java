package pl.kata.kompresja;

import java.io.OutputStream;

public interface SymbolChannelOutputStreamFactory {
    SymbolChannelOutputStream getOutputStream(OutputStream out, int... channelSizes);
}
