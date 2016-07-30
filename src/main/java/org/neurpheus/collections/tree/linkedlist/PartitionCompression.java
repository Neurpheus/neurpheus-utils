/*
 * Neurpheus - Utilities Package
 *
 * Copyright (C) 2006-2016 Jakub Strychowski
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 3.0 of the License, or (at your option)
 *  any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 *  for more details.
 */

package org.neurpheus.collections.tree.linkedlist;

import java.util.concurrent.Callable;

/**
 * The parallel task responsible for compression of a single partition.
 * 
 * @author Jakub Strychowski
 */
class PartitionCompression implements Callable<Integer> {

    /** Working structure used by the compression algorithm. */
    private final LZTrieCompression compr;
    
    /** Start position of the partition. */
    private final int partitionStart;
    
    /** End position of the partition. */
    private final int partitionEnd;
    
    protected PartitionCompression(LZTrieCompression compression, int start, int end) {
        this.compr = compression;
        this.partitionStart = start;
        this.partitionEnd = end;
    }
    
    @Override
    public Integer call() throws Exception {
        compr.processPartition(partitionStart, partitionEnd);
        return 0;
    }
    
}
