/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.collections.tree.linkedlist;

import java.util.concurrent.Callable;

/**
 *
 * @author Kuba
 */
public class PartitionCompression implements Callable<Integer> {

    private LZTrieCompression compr;
    private int partitionStart;
    private int partitionEnd;
    
    public PartitionCompression(LZTrieCompression compression, int start, int end) {
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
