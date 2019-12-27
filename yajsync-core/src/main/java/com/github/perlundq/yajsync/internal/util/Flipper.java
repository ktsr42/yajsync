package com.github.perlundq.yajsync.internal.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

// Helper class to deal with this issue:  https://stackoverflow.com/questions/58120357/jeromq-on-android-no-virtual-method-clearljava-nio-bytebuffer


public final class Flipper {
  // flip
  public static CharBuffer flipCB(CharBuffer cb) {
    Buffer b = (Buffer)cb;
    return (CharBuffer)b.flip();
  }

  public static ByteBuffer flipBB(ByteBuffer bb) {
    Buffer b = (Buffer)bb;
    return (ByteBuffer)b.flip();
  }

  // position
  public static ByteBuffer positionBB(ByteBuffer bb, int pos) {
    Buffer b = (Buffer)bb;
    return (ByteBuffer)b.position(pos);
  }

  public static CharBuffer positionCB(CharBuffer bb, int pos) {
    Buffer b = (Buffer)bb;
    return (CharBuffer)b.position(pos);
  }

  // limit
  public static CharBuffer limitCB(CharBuffer cb, int end) {
    Buffer b = (Buffer)cb;
    return (CharBuffer)b.limit(end);
  }

  public static ByteBuffer limitBB(ByteBuffer cb, int end) {
    Buffer b = (Buffer)cb;
    return (ByteBuffer)b.limit(end);
  }

  // clear does not seem to be used

}
