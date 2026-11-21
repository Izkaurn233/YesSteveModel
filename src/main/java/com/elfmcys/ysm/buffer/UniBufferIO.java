package com.elfmcys.ysm.buffer;

import java.lang.ref.Reference;
import java.nio.ByteBuffer;

public final class UniBufferIO {
    private UniBufferIO() {}

    public static void copy(UniBuffer source, int sourceOffset, UniBuffer target, int targetOffset, int length) {
        try {
            checkRange(source.size(), sourceOffset, length);
            checkRange(target.size(), targetOffset, length);
            if (length == 0) {
                return;
            }
            if (source instanceof ArrayBuffer sourceArray && target instanceof ArrayBuffer targetArray) {
                System.arraycopy(sourceArray.array(), sourceArray.arrayOffset() + sourceOffset,
                    targetArray.array(), targetArray.arrayOffset() + targetOffset, length);
            } else {
                target.nio().put(targetOffset, source.nio(), sourceOffset, length);
            }
        } finally {
            Reference.reachabilityFence(source);
            Reference.reachabilityFence(target);
        }
    }

    public static boolean equals(UniBuffer source, int sourceOffset, byte[] target, int targetOffset, int length) {
        try {
            checkRange(source.size(), sourceOffset, length);
            checkRange(target.length, targetOffset, length);
            return source.nio().slice(sourceOffset, length)
                .mismatch(ByteBuffer.wrap(target, targetOffset, length)) == -1;
        } finally {
            Reference.reachabilityFence(source);
            Reference.reachabilityFence(target);
        }
    }

    public static boolean equals(UniBuffer left, int leftOffset, UniBuffer right, int rightOffset, int length) {
        try {
            checkRange(left.size(), leftOffset, length);
            checkRange(right.size(), rightOffset, length);
            return left.nio().slice(leftOffset, length).mismatch(right.nio().slice(rightOffset, length)) == -1;
        } finally {
            Reference.reachabilityFence(left);
            Reference.reachabilityFence(right);
        }
    }

    private static void checkRange(int size, int offset, int length) {
        if (offset < 0 || length < 0 || offset > size - length) {
            throw new IndexOutOfBoundsException();
        }
    }
}
