/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.iotdb.db.query.aggregation.impl;

import java.io.IOException;
import java.util.List;
import org.apache.iotdb.db.exception.ProcessorException;
import org.apache.iotdb.db.query.aggregation.AggregateFunction;
import org.apache.iotdb.db.query.aggregation.AggregationConstant;
import org.apache.iotdb.db.query.reader.IPointReader;
import org.apache.iotdb.db.query.reader.merge.EngineReaderByTimeStamp;
import org.apache.iotdb.db.utils.TimeValuePair;
import org.apache.iotdb.db.utils.TsPrimitiveType;
import org.apache.iotdb.tsfile.file.header.PageHeader;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.apache.iotdb.tsfile.read.common.BatchData;

public class LastAggrFunc extends AggregateFunction {

  public LastAggrFunc(TSDataType dataType) {
    super(AggregationConstant.LAST, dataType);
  }

  @Override
  public void init() {

  }

  @Override
  public BatchData getResult() {
    if (resultData.length() != 0) {
      resultData.setTime(0, 0);
    }
    return resultData;
  }

  @Override
  public void calculateValueFromPageHeader(PageHeader pageHeader) throws ProcessorException {
    Object lastVal = pageHeader.getStatistics().getLast();
    updateLastResult(pageHeader.getMaxTimestamp(), lastVal);
  }

  @Override
  public void calculateValueFromPageData(BatchData dataInThisPage, IPointReader unsequenceReader)
      throws IOException, ProcessorException {
    long time = -1;
    Object lastVal = null;
    int maxIndex = dataInThisPage.length() - 1;
    if (maxIndex < 0) {
      return;
    }
    time = dataInThisPage.getTimeByIndex(maxIndex);
    lastVal = dataInThisPage.getValueByIndex(maxIndex);
    while (unsequenceReader.hasNext()) {
      if (unsequenceReader.current().getTimestamp() < time) {
        unsequenceReader.next();
      } else if (unsequenceReader.current().getTimestamp() == time) {
        lastVal = unsequenceReader.current().getValue().getValue();
        unsequenceReader.next();
      } else {
        break;
      }
    }

    //has inited lastVal and time in the batch(dataInThisPage).
    if (time != -1) {
      updateLastResult(time, lastVal);
    }
  }

  @Override
  public void calculateValueFromUnsequenceReader(IPointReader unsequenceReader)
      throws IOException, ProcessorException {
    TimeValuePair pair = null;
    while (unsequenceReader.hasNext()) {
      pair = unsequenceReader.next();
    }

    if (pair != null) {
      updateLastResult(pair.getTimestamp(), pair.getValue().getValue());
    }

  }

  @Override
  public void calcAggregationUsingTimestamps(List<Long> timestamps,
      EngineReaderByTimeStamp dataReader) throws IOException, ProcessorException {

    long time = -1;
    Object lastVal = null;
    for (int i = 0; i < timestamps.size(); i++) {
      TsPrimitiveType value = dataReader.getValueInTimestamp(timestamps.get(i));
      if (value != null) {
        time = timestamps.get(i);
        lastVal = value.getValue();
      }
    }
    if(time != -1){
      updateLastResult(-1, lastVal);
    }
  }

  @Override
  public void calcGroupByAggregation(long partitionStart, long partitionEnd, long intervalStart,
      long intervalEnd, BatchData data) throws ProcessorException {

  }

  private void updateLastResult(long time, Object value) {
    if (resultData.length() == 0) {
      resultData.putAnObject(value);
      resultData.putTime(time);
    } else {
      if (time >= resultData.currentTime()) {
        resultData.setAnObject(0, (Comparable<?>) value);
        resultData.setTime(0, time);
      }
    }
  }

}