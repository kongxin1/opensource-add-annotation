/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.util;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Holder for many bitArrays - used for message audit
 */
/**
 * 最多只保存maxNumberOfArrays个BitArray对象，如果超了，就将以前的删除，而且这里面的longFirstIndex属性记录了本对象中保存的最小的bit位
 * @ClassName: BitArrayBin
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月31日 下午2:40:52
 */
public class BitArrayBin implements Serializable {
	private static final long serialVersionUID = 1L;
	private final LinkedList<BitArray> list;
	// 记录需要使用BitArray个数
	private int maxNumberOfArrays;
	private int firstIndex = -1; // leave 'int' for old serialization compatibility and introduce
									// new 'long' field
	private long lastInOrderBit = -1;
	private long longFirstIndex = -1;// 记录第一个bit位的位置，比这个bit位小的都是已经超出了本对象的表示范围

	/**
	 * Create a BitArrayBin to a certain window size (number of messages to keep)
	 * @param windowSize
	 */
	public BitArrayBin(int windowSize) {
		maxNumberOfArrays = ((windowSize + 1) / BitArray.LONG_SIZE) + 1;
		maxNumberOfArrays = Math.max(maxNumberOfArrays, 1);
		list = new LinkedList<BitArray>();
		for (int i = 0; i < maxNumberOfArrays; i++) {
			list.add(null);
		}
	}
	/**
	 * Set a bit
	 * @param index
	 * @param value
	 * @return true if set
	 */
	public boolean setBit(long index, boolean value) {
		boolean answer = false;
		BitArray ba = getBitArray(index);
		if (ba != null) {
			int offset = getOffset(index);
			if (offset >= 0) {
				answer = ba.set(offset, value);
			}
		}
		return answer;
	}
	/**
	 * Test if in order
	 * @param index
	 * @return true if next message is in order
	 */
	public boolean isInOrder(long index) {
		boolean result = false;
		if (lastInOrderBit == -1) {
			result = true;
		} else {
			result = lastInOrderBit + 1 == index;
		}
		lastInOrderBit = index;
		return result;
	}
	/**
	 * Get the boolean value at the index
	 * @param index
	 * @return true/false
	 */
	public boolean getBit(long index) {
		boolean answer = index >= longFirstIndex;
		BitArray ba = getBitArray(index);
		if (ba != null) {
			int offset = getOffset(index);
			if (offset >= 0) {
				answer = ba.get(offset);
				return answer;
			}
		} else {
			// gone passed range for previous bins so assume set
			answer = true;
		}
		return answer;
	}
	/**
	 * Get the BitArray for the index
	 * @param index
	 * @return BitArray
	 */
	/**
	 * 获得index位所在的BitArray对象
	 * @Title: getBitArray
	 * @Description: TODO
	 * @param index
	 * @return
	 * @return: BitArray
	 */
	private BitArray getBitArray(long index) {
		int bin = getBin(index);
		BitArray answer = null;
		if (bin >= 0) {
			if (bin >= maxNumberOfArrays) {
				int overShoot = bin - maxNumberOfArrays + 1;
				while (overShoot > 0) {
					// 将以前的旧对象删除，保留最新的
					list.removeFirst();
					longFirstIndex += BitArray.LONG_SIZE;
					list.add(new BitArray());
					overShoot--;
				}
				bin = maxNumberOfArrays - 1;
			}
			answer = list.get(bin);
			if (answer == null) {
				answer = new BitArray();
				list.set(bin, answer);
			}
		}
		return answer;
	}
	/**
	 * Get the index of the bin from the total index
	 * @param index
	 * @return the index of the bin
	 */
	/**
	 * 根据入参得到BitArray对象的位置，没太看懂
	 * @Title: getBin
	 * @Description: TODO
	 * @param index
	 * @return
	 * @return: int
	 */
	private int getBin(long index) {
		int answer = 0;
		if (longFirstIndex < 0) {
			longFirstIndex = (int) (index - (index % BitArray.LONG_SIZE));
		} else if (longFirstIndex >= 0) {
			answer = (int) ((index - longFirstIndex) / BitArray.LONG_SIZE);
		}
		return answer;
	}
	/**
	 * Get the offset into a bin from the total index
	 * @param index
	 * @return the relative offset into a bin
	 */
	/**
	 * 获得index在BitArray对象中的偏移位置
	 * @Title: getOffset
	 * @Description: TODO
	 * @param index
	 * @return
	 * @return: int
	 */
	private int getOffset(long index) {
		int answer = 0;
		if (longFirstIndex >= 0) {
			answer = (int) ((index - longFirstIndex) - (BitArray.LONG_SIZE * getBin(index)));
		}
		return answer;
	}
	/**
	 * 获得最大的一个bit位位置
	 * @Title: getLastSetIndex
	 * @Description: TODO
	 * @return
	 * @return: long
	 */
	public long getLastSetIndex() {
		long result = -1;
		if (longFirstIndex >= 0) {
			result = longFirstIndex;
			BitArray last = null;
			for (int lastBitArrayIndex = maxNumberOfArrays - 1; lastBitArrayIndex >= 0; lastBitArrayIndex--) {
				last = list.get(lastBitArrayIndex);
				if (last != null) {
					result += last.length() - 1;
					result += lastBitArrayIndex * BitArray.LONG_SIZE;
					break;
				}
			}
		}
		return result;
	}
	public static void main(String argv[]) {
		BitArrayBin b = new BitArrayBin(73);
		b.getBitArray(2000);
		b.getBitArray(2001);
		b.getBitArray(10);
	}
}
