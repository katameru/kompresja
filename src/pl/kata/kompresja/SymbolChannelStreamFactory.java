package pl.kata.kompresja;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: kata
 * Date: 11.06.13
 * Time: 19:10
 * To change this template use File | Settings | File Templates.
 */

public class SymbolChannelStreamFactory implements SymbolChannelInputStreamFactory, SymbolChannelOutputStreamFactory{
    private Instrumentation instrumentation;

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    @Override
    public SymbolChannelInputStream getInputStream(InputStream in, int... channelSizes) {
        BitInputStream bi = new BitInputStream(in);
        SymbolChannelInputStream sci = new CompressedInputStream(bi, channelSizes);
        return sci;
    }

    @Override
    public SymbolChannelOutputStream getOutputStream(OutputStream out, int... channelSizes) {
        instrumentation = new Instrumentation();
        BitOutputStream bo = new BitOutputStream(out);
        CompressedOutputStream sco = new CompressedOutputStream(bo, channelSizes);
        sco.setInstrumentation(instrumentation);
        return sco;
    }
}
