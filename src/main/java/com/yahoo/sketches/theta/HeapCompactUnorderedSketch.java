/*
 * Copyright 2015, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.theta;

import static com.yahoo.sketches.Util.checkSeedHashes;
import static com.yahoo.sketches.Util.computeSeedHash;
import static com.yahoo.sketches.theta.PreambleUtil.extractCurCount;
import static com.yahoo.sketches.theta.PreambleUtil.extractPreLongs;
import static com.yahoo.sketches.theta.PreambleUtil.extractSeedHash;
import static com.yahoo.sketches.theta.PreambleUtil.extractThetaLong;

import com.yahoo.memory.Memory;
import com.yahoo.memory.WritableMemory;

/**
 * An on-heap, compact, read-only sketch.
 *
 * @author Lee Rhodes
 */
final class HeapCompactUnorderedSketch extends HeapCompactSketch {

  /**
   * Constructs this sketch from correct, valid components.
   * @param cache in compact form
   * @param empty The correct <a href="{@docRoot}/resources/dictionary.html#empty">Empty</a>.
   * @param seedHash The correct
   * <a href="{@docRoot}/resources/dictionary.html#seedHash">Seed Hash</a>.
   * @param curCount correct value
   * @param thetaLong The correct
   * <a href="{@docRoot}/resources/dictionary.html#thetaLong">thetaLong</a>.
   */
  private HeapCompactUnorderedSketch(final long[] cache, final boolean empty, final short seedHash,
      final int curCount, final long thetaLong) {
    super(cache, empty, seedHash, curCount, thetaLong);
  }

  /**
   * Heapifies the given source Memory with seed
   * @param srcMem <a href="{@docRoot}/resources/dictionary.html#mem">See Memory</a>
   * @param seed <a href="{@docRoot}/resources/dictionary.html#seed">See Update Hash Seed</a>.
   * @return this sketch
   */
  static HeapCompactUnorderedSketch heapifyInstance(final Memory srcMem, final long seed) {
    final Object memObj = ((WritableMemory)srcMem).getArray(); //may be null
    final long memAdd = srcMem.getCumulativeOffset(0L);

    final short memSeedHash = (short) extractSeedHash(memObj, memAdd);
    final short computedSeedHash = computeSeedHash(seed);
    checkSeedHashes(memSeedHash, computedSeedHash);

    final int preLongs = extractPreLongs(memObj, memAdd);
    final boolean empty = PreambleUtil.isEmpty(memObj, memAdd);
    int curCount = 0;
    long thetaLong = Long.MAX_VALUE;
    long[] cache = new long[0];

    if (preLongs == 1) {
      if (!empty) { //singleItem
        curCount = 1;
        cache = new long[] { srcMem.getLong(8) };
      } //else empty
    } else { //preLongs > 1
      curCount = extractCurCount(memObj, memAdd);
      cache = new long[curCount];
      if (preLongs == 2) {
        srcMem.getLongArray(16, cache, 0, curCount);
      } else { //preLongs == 3
        srcMem.getLongArray(24, cache, 0, curCount);
        thetaLong = extractThetaLong(memObj, memAdd);
      }
    }
    return new HeapCompactUnorderedSketch(cache, empty, memSeedHash, curCount, thetaLong);
  }

  /**
   * Converts the given UpdateSketch to this compact form.
   * @param sketch the given UpdateSketch
   */
  static HeapCompactUnorderedSketch compact(final UpdateSketch sketch) {
    final long thetaLong = sketch.getThetaLong();
    final boolean empty = sketch.isEmpty();
    final int curCount = sketch.getRetainedEntries(true);

    final short seedHash = sketch.getSeedHash();
    final long[] cache = sketch.getCache();
    final boolean ordered = false;
    final long[] cacheOut = CompactSketch.compactCache(cache, curCount, thetaLong, ordered);
    return new HeapCompactUnorderedSketch(cacheOut, empty, seedHash, curCount, thetaLong);
  }

  /**
   * Constructs this sketch from correct, valid arguments.
   * @param cache in compact form
   * @param empty The correct <a href="{@docRoot}/resources/dictionary.html#empty">Empty</a>.
   * @param seedHash The correct
   * <a href="{@docRoot}/resources/dictionary.html#seedHash">Seed Hash</a>.
   * @param curCount correct value
   * @param thetaLong The correct
   * <a href="{@docRoot}/resources/dictionary.html#thetaLong">thetaLong</a>.
   * @return this sketch
   */
  static HeapCompactUnorderedSketch compact(final long[] cache, final boolean empty,
      final short seedHash, final int curCount, final long thetaLong) {
    return new HeapCompactUnorderedSketch(cache, empty, seedHash, curCount, thetaLong);
  }

  //Sketch interface

  @Override
  public byte[] toByteArray() {
    return toByteArray(false);
  }

  //restricted methods

  @Override
  public boolean isOrdered() {
    return false;
  }

}