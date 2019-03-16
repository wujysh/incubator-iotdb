/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.iotdb.tsfile.read;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import org.apache.iotdb.tsfile.common.conf.TSFileConfig;
import org.apache.iotdb.tsfile.utils.TsFileGeneratorForTest;
import org.apache.iotdb.tsfile.write.writer.IncompleteFileTestUtil;
import org.junit.Test;

public class TsFileRestorableReaderTest {

  private static final String FILE_PATH = TsFileGeneratorForTest.outputDataFile;

  @Test
  public void testToReadDamagedFileAndRepair() throws IOException {
    File file = new File(FILE_PATH);

    IncompleteFileTestUtil.writeFileWithOneIncompleteChunkHeader(file);

    TsFileSequenceReader reader = new TsFileRestorableReader(FILE_PATH, true);
    String tailMagic = reader.readTailMagic();
    reader.close();

    // Check if the file was repaired
    assertEquals(TSFileConfig.MAGIC_STRING, tailMagic);
    assertTrue(file.delete());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToReadDamagedFileNoRepair() throws IOException {
    File file = new File(FILE_PATH);

    IncompleteFileTestUtil.writeFileWithOneIncompleteChunkHeader(file);

    // This should throw an Illegal Argument Exception
    TsFileSequenceReader reader = new TsFileRestorableReader(FILE_PATH, false);
  }
}
