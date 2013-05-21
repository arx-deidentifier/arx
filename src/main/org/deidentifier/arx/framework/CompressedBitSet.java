/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.framework;

/**
 * A compressed bitset
 *
 * @author Prasser, Kohlmayer
 */
public class CompressedBitSet {

    /** The Constant ADDRESS_BITS_PER_UNIT. */
    private static final int ADDRESS_BITS_PER_UNIT = 3;

    /** The Constant BIT_INDEX_MASK. */
    private static final int BIT_INDEX_MASK        = 7;

    /** The Constant NUM_CHUNKS. */
    private static final int NUM_CHUNKS            = 10;

    /** The chunks. */
    private final byte[][]   chunks;

    /** The chunk size in bytes. */
    private final int        chunkSizeInBytes;

    /** The modulo mask. */
    private final int        moduloMask;

    /** The pre-calculated bitmask. */
    private final int        bitMask;

    /** The shift value. */
    private final int        shiftValue;

    /**
     * Instantiates a new bit set compressed byte chunks shift.
     *
     * @param size the size
     */
    public CompressedBitSet(final int size) {

        // Calculate next power of 2
        final int sizeinBits = ((size + CompressedBitSet.NUM_CHUNKS) - 1) / CompressedBitSet.NUM_CHUNKS;
        int chunkSizeTemp = 1;
        while (chunkSizeTemp < sizeinBits) {
            chunkSizeTemp <<= 1;
        }
        final int chunkSizeInBits = chunkSizeTemp;
        chunkSizeInBytes = Math.max(chunkSizeTemp / 8, 1);
        shiftValue = (32 - Integer.numberOfLeadingZeros(chunkSizeInBits - 1));
        final int newNumChunks = ((size + chunkSizeInBits) - 1) / chunkSizeInBits;
        chunks = new byte[newNumChunks][];
        moduloMask = chunkSizeInBits - 1;
        bitMask = CompressedBitSet.BIT_INDEX_MASK & moduloMask;
    }

    /**
     * Gets the bit
     *
     * @param bit the bit
     * @return true, if successful
     */
    public final boolean get(final int bit) {

        int idx = bit >> shiftValue;

        if (chunks[idx] == null) { return false; }

        return ((chunks[idx][(bit & moduloMask) >> CompressedBitSet.ADDRESS_BITS_PER_UNIT] & (1 << (bit & bitMask))) != 0);
    }

    /**
     * Sets the bit
     *
     * @param bit the bit
     */
    public final void set(final int bit) {

        int idx = bit >> shiftValue;

        if (chunks[idx] == null) {
            chunks[idx] = new byte[chunkSizeInBytes];
        }

        chunks[idx][(bit & moduloMask) >> CompressedBitSet.ADDRESS_BITS_PER_UNIT] |= 1 << (bit & bitMask);
    }
}
