package cn.edu.tsinghua.iotdb.engine.querycontext;

import cn.edu.tsinghua.iotdb.engine.memtable.TimeValuePairSorter;
import cn.edu.tsinghua.tsfile.file.metadata.enums.TSDataType;
import cn.edu.tsinghua.tsfile.read.common.Path;

import java.util.List;


public class OverflowSeriesDataSource {

    private Path seriesPath;
    private TSDataType dataType;
    // overflow tsfile
    private List<OverflowInsertFile> overflowInsertFileList;
    // unSeq mem-table
    private TimeValuePairSorter readableMemChunk;

    public OverflowSeriesDataSource(Path seriesPath) {
        this.seriesPath = seriesPath;
    }

    public OverflowSeriesDataSource(Path seriesPath, TSDataType dataType, List<OverflowInsertFile> overflowInsertFileList, TimeValuePairSorter readableMemChunk) {
        this.seriesPath = seriesPath;
        this.dataType = dataType;
        this.overflowInsertFileList = overflowInsertFileList;
        this.readableMemChunk = readableMemChunk;
    }

    public List<OverflowInsertFile> getOverflowInsertFileList() {
        return overflowInsertFileList;
    }

    public void setOverflowInsertFileList(List<OverflowInsertFile> overflowInsertFileList) {
        this.overflowInsertFileList = overflowInsertFileList;
    }

    public TimeValuePairSorter getReadableMemChunk() {
        return readableMemChunk;
    }

    public void setReadableMemChunk(TimeValuePairSorter rawChunk) {
        this.readableMemChunk = rawChunk;
    }

    public Path getSeriesPath() {
        return seriesPath;
    }

    public void setSeriesPath(Path seriesPath) {
        this.seriesPath = seriesPath;
    }

    public TSDataType getDataType() {
        return dataType;
    }

    public boolean hasRawChunk() {
        return readableMemChunk != null && !readableMemChunk.isEmpty();
    }
}