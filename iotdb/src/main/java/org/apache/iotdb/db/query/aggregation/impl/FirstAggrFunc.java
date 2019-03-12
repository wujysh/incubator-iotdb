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
import org.apache.iotdb.db.utils.TsPrimitiveType;
import org.apache.iotdb.tsfile.file.header.PageHeader;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.apache.iotdb.tsfile.read.common.BatchData;

public class FirstAggrFunc extends AggregateFunction {

  public FirstAggrFunc(TSDataType dataType) {
    super(AggregationConstant.FIRST, dataType);
  }

  @Override
  public void init() {

  }

  @Override
  public BatchData getResult() {
    return resultData;
  }

  @Override
  public void calculateValueFromPageHeader(PageHeader pageHeader) throws ProcessorException {
    if (resultData.length() != 0) {
      return;
    }

    Object firstVal = pageHeader.getStatistics().getFirst();
    if (firstVal == null) {
      throw new ProcessorException("PageHeader contains no FIRST value");
    }
    resultData.putTime(0);
    resultData.putAnObject(firstVal);
  }

  @Override
  public void calculateValueFromPageData(BatchData dataInThisPage, IPointReader unsequenceReader)
      throws IOException, ProcessorException {
    if (resultData.length() != 0) {
      return;
    }
    if (dataInThisPage.hasNext() && unsequenceReader.hasNext()) {
      if (dataInThisPage.currentTime() >= unsequenceReader.current().getTimestamp()) {
        resultData.putTime(0);
        resultData.putAnObject(unsequenceReader.current().getValue().getValue());
        unsequenceReader.next();
        return;
      } else {
        resultData.putTime(0);
        resultData.putAnObject(dataInThisPage.currentValue());
        return;
      }
    }

    if (dataInThisPage.hasNext()) {
      resultData.putTime(0);
      resultData.putAnObject(dataInThisPage.currentValue());
      return;
    }
  }

  @Override
  public void calculateValueFromUnsequenceReader(IPointReader unsequenceReader)
      throws IOException, ProcessorException {
    if (resultData.length() != 0) {
      return;
    }
    if (unsequenceReader.hasNext()) {
      resultData.putTime(0);
      resultData.putAnObject(unsequenceReader.current().getValue().getValue());
      return;
    }
  }

  @Override
  public void calcAggregationUsingTimestamps(List<Long> timestamps,
      EngineReaderByTimeStamp dataReader) throws IOException, ProcessorException {
    if (resultData.length() != 0) {
      return;
    }

    for (long time : timestamps) {
      TsPrimitiveType value = dataReader.getValueInTimestamp(time);
      if (value != null) {
        resultData.putTime(0);
        resultData.putAnObject(value.getValue());
        break;
      }
    }
  }

  @Override
  public void calcGroupByAggregation(long partitionStart, long partitionEnd, long intervalStart,
      long intervalEnd, BatchData data) throws ProcessorException {

  }
}