package pl.kata.kompresja;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: kata
 * Date: 02.06.13
 * Time: 20:59
 * To change this template use File | Settings | File Templates.
 */
class SlidingWindow {
    private final int dictSize;
    private final int inputSize;
    private final byte[] codingWindow;
    private int dictBegin;
    private int inputEnd = 0;
    private final LookupAccelerator accelerator;

    public SlidingWindow(int dictionarySize, int inputBufferSize) {
        dictSize = dictionarySize;
        inputSize = inputBufferSize;
        codingWindow = new byte[dictionarySize + inputBufferSize];
        dictBegin = dictSize;
        accelerator = new LookupAccelerator(dictSize);
    }

    public int[] longestMatch() {
        int P=0, C=0;

        int[] suggestions = accelerator.getIndexSuggestions(codingWindow[dictSize], codingWindow[dictSize + 1], codingWindow[dictSize + 2]);
        if(suggestions.length > 0) {
            for(int suggestion : suggestions) {
                int dictPosition = dictBegin + suggestion;
                int matchLength = 0;
                while(matchLength < inputEnd
                   && dictPosition + matchLength < dictSize
                   && codingWindow[dictPosition + matchLength] == codingWindow[dictSize + matchLength]) {
                   matchLength++;
                }
                if(C < matchLength) {
                   P = dictPosition - dictBegin;
                   C = matchLength;
                }
            }
        } else {
            int dictPosition = dictBegin;
            int[] table = preProcessPattern(Arrays.copyOfRange(codingWindow, dictSize, dictSize+inputEnd));
            while(dictPosition < dictSize) {
                int matchLength = 0;
                while(matchLength < inputEnd
                   && dictPosition + matchLength < dictSize
                   && codingWindow[dictPosition + matchLength] == codingWindow[dictSize + matchLength]) {
                    matchLength++;
                }
                if(C < matchLength) {
                    P = dictPosition - dictBegin;
                    C = matchLength;
                }
                if(matchLength == 2) {
                    break;
                }

                matchLength-=table[matchLength];
                dictPosition+=matchLength;
            }
        }
        if(C >= inputEnd) {
            C--;
        }
        return new int[]{P, C, codingWindow[dictSize+C]};
    }

    public int[] preProcessPattern(byte[] ptrn) {
        int i = 0, j = -1;
        int ptrnLen = ptrn.length;
        int[] b = new int[ptrnLen + 1];

        b[i] = j;
        while (i < ptrnLen) {
            while (j >= 0 && ptrn[i] != ptrn[j]) {
                j = b[j];
            }
            i++;
            j++;
            b[i] = j;
        }
        return b;
    }

    public void read(InputStream input, int length) throws IOException {
        int read = input.read(codingWindow, dictSize+inputEnd, length);
        if(read == -1) {
            return;
        }

        inputEnd += read;
    }

    public void readFirst(byte firstByte) {
        dictBegin--;
        codingWindow[dictBegin] = firstByte;
        accelerator.addLetter(firstByte);
    }

    public void shift(int C) {
        C = Math.min(C, inputEnd);
        accelerator.addLetters(Arrays.copyOfRange(codingWindow, dictSize, dictSize+C));
        System.arraycopy(codingWindow, C, codingWindow, 0, dictSize+inputSize-C);
        dictBegin = Math.max(0, dictBegin - C);
        inputEnd-= C;

    }

    public boolean isCodingBufferEmpty() {
        return inputEnd == 0;
    }

    public byte[] getSubstring(int offset, int length) {
        byte[] res = new byte[length];
        System.arraycopy(codingWindow, dictBegin+offset, res, 0, length);
        return res;
    }

    public byte getFirst() {
        return codingWindow[dictSize];
    }
}
