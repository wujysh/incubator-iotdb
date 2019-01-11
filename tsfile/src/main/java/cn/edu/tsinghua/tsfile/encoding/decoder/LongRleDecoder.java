package cn.edu.tsinghua.tsfile.encoding.decoder;

import cn.edu.tsinghua.tsfile.exception.encoding.TSFileDecodingException;
import cn.edu.tsinghua.tsfile.utils.ReadWriteForEncodingUtils;
import cn.edu.tsinghua.tsfile.encoding.bitpacking.LongPacker;
import cn.edu.tsinghua.tsfile.encoding.common.EndianType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Decoder for long value using rle or bit-packing
 */
public class LongRleDecoder extends RleDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(LongRleDecoder.class);

    /**
     * current value for rle repeated value
     */
    private long currentValue;

    /**
     * buffer to save all values in group using bit-packing
     */
    private long[] currentBuffer;

    /**
     * packer for unpacking long value
     */
    private LongPacker packer;

    public LongRleDecoder(EndianType endianType) {
        super(endianType);
        currentValue = 0;
    }

    /**
     * read a long value from InputStream
     *
     * @param buffer - InputStream
     * @return value - current valid value
     */
    @Override
    public long readLong(ByteBuffer buffer) {
        if (!isLengthAndBitWidthReaded) {
            //start to read a new rle+bit-packing pattern
            readLengthAndBitWidth(buffer);
        }

        if (currentCount == 0) {
            try {
                readNext();
            } catch (IOException e) {
                LOGGER.error("tsfile-encoding IntRleDecoder: error occurs when reading all encoding number, length is {}, bit width is {}", length, bitWidth, e);
            }
        }
        --currentCount;
        long result = 0;
        switch (mode) {
            case RLE:
                result = currentValue;
                break;
            case BIT_PACKED:
                result = currentBuffer[bitPackingNum - currentCount - 1];
                break;
            default:
                throw new TSFileDecodingException(String.format("tsfile-encoding LongRleDecoder: not a valid mode %s", mode));
        }

        if (!hasNextPackage()) {
            isLengthAndBitWidthReaded = false;
        }
        return result;
    }

    @Override
    protected void initPacker() {
        packer = new LongPacker(bitWidth);
    }

    @Override
    protected void readNumberInRLE() throws IOException {
        currentValue = ReadWriteForEncodingUtils.readLongLittleEndianPaddedOnBitWidth(byteCache, bitWidth);
    }

    @Override
    protected void readBitPackingBuffer(int bitPackedGroupCount, int lastBitPackedNum) throws IOException {
        currentBuffer = new long[bitPackedGroupCount * config.RLE_MIN_REPEATED_NUM];
        byte[] bytes = new byte[bitPackedGroupCount * bitWidth];
        int bytesToRead = bitPackedGroupCount * bitWidth;
        bytesToRead = Math.min(bytesToRead, byteCache.remaining());
        byteCache.get(bytes, 0, bytesToRead);

        // save all long values in currentBuffer
        packer.unpackAllValues(bytes, 0, bytesToRead, currentBuffer);
    }

    @Override
    public void reset() {
        super.reset();
    }
}