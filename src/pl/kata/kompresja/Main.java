package pl.kata.kompresja;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Main {

    private static byte[] createChecksum(String filename) throws IOException, NoSuchAlgorithmException {
        InputStream fis =  new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    private static String getMD5Checksum(String filename) throws IOException, NoSuchAlgorithmException {
        byte[] b = createChecksum(filename);
        String result = "";

        for (byte aB : b) {
            result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static void compressDecompress(String source, Compressor c, Decompressor d, SymbolChannelStreamFactory factory) throws IOException, NoSuchAlgorithmException {
        File in = new File(source);
        long before = in.length()*8;

        InputStream raw = new BufferedInputStream(new FileInputStream(in));

        OutputStream comp = new BufferedOutputStream(new FileOutputStream("compressed.txt"));
        InputStream check = new FileInputStream("compressed.txt");
        OutputStream decompressed = new FileOutputStream("decompressed.txt");
        try {
            long startTime, endTime;
            startTime = System.currentTimeMillis();
            c.compress(raw, comp);
            endTime = System.currentTimeMillis();
            factory.getInstrumentation().add("comp_time", "Total compression time", "ms", (endTime - startTime));
            startTime = System.currentTimeMillis();
            d.decompress(check, decompressed);
            endTime = System.currentTimeMillis();
            factory.getInstrumentation().add("decomp_time", "Total decompression time", "ms", (endTime - startTime));
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert(getMD5Checksum(source).equals(getMD5Checksum("decompressed.txt")));
        File o = new File("compressed.txt");
        long after = o.length()*8;
        factory.getInstrumentation().add("comp_ratio", "Compression ratio", "", ((double) after / (double) before));
        raw.close();
        comp.close();
        check.close();
        decompressed.close();
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        SymbolChannelStreamFactory factory = new SymbolChannelStreamFactory();

        String[] cantenbury = "alice29.txt , asyoulik.txt, cp.html, fields.c, grammar.lsp, kennedy.xls, lcet10.txt, plrabn12.txt, ptt5, sum, xargs.1".split(", ");
        String[] calgary = "bib, book1, book2, geo, news, obj1, obj2, paper1, paper2, paper3, paper4, paper5, paper6, pic, progc, progl, progp, trans".split(", ");

        for(int dictSize : new int[] {8*1024}) {
            for(int lookahead : new int[] {128}) {
                System.out.println(dictSize + " " + lookahead);
                LZ77 lz77 = new LZ77(dictSize, lookahead, factory);
                LZSS lzss = new LZSS(dictSize, lookahead, factory);
                FileWriter log = new FileWriter(dictSize + "-" + lookahead +".txt");
                for(String source : cantenbury) {
                    compressDecompress("cantenbury\\" + source, lz77, lz77, factory);
                    factory.getInstrumentation().add("000file", "Filename", "", "cantenbury\\" + source);
                    factory.getInstrumentation().add("000method", "Compression method", "", "lz77");
                    log.append(factory.getInstrumentation().toString());
                    log.append('\n');
                    log.flush();

                    compressDecompress("cantenbury\\" + source, lzss, lzss, factory);
                    factory.getInstrumentation().add("000file", "Filename", "", "calgary\\" + source);
                    factory.getInstrumentation().add("000method", "Compression method", "", "lzss");
                    log.append(factory.getInstrumentation().toString());
                    log.append('\n');
                    log.flush();

                }
                for(String source : calgary) {
                    compressDecompress("calgary\\" + source, lz77, lz77, factory);
                    factory.getInstrumentation().add("000file", "Filename", "", "cantenbury\\" + source);
                    factory.getInstrumentation().add("000method", "Compression method", "", "lz77");
                    log.append(factory.getInstrumentation().toString());
                    log.append('\n');
                    log.flush();

                    compressDecompress("calgary\\" + source, lzss, lzss, factory);
                    factory.getInstrumentation().add("000file", "Filename", "", "calgary\\" + source);
                    factory.getInstrumentation().add("000method", "Compression method", "", "lzss");
                    log.append(factory.getInstrumentation().toString());
                    log.append('\n');
                    log.flush();
                }
                log.close();
            }
        }
    }
}
