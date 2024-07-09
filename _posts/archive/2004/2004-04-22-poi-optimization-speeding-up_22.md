---
layout: post
title: POI Optimization - Speeding up org.apache.poi.hssf.record.BlankRecord.compareTo()
date: '2004-04-22T15:28:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2004-04-22T15:32:33.436-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108266210589477241
blogger_orig_url: http://affy.blogspot.com/2004/04/poi-optimization-speeding-up_22.md
year: 2004
theme: java
---

<p>This optimization is nearly identical to the one applied to <code>HSSFRow.compareTo()</code> - just used a local copy
    of the row and column values. This change reduced exceution time in my test case by 17.5 seconds.</p>


<pre>
    /**
     * switched to using a local copy of the row and column. Also moved the equality test to
	 * the last test because it is most complex and probably least likely to be true.
     */
    public int compareTo(Object obj) {
        int rv = -1;
        CellValueRecordInterface loc = (CellValueRecordInterface) obj;

        int thisRow = this.getRow();
        int locRow = loc.getRow();

        if (thisRow < locRow) {
            rv = -1;
        } else if (thisRow > locRow) {
            rv = 1;
        } else {
            int thisColumn = this.getColumn();
            int locColumn = loc.getColumn();

            if (thisColumn > locColumn) {
                rv = 1;
            } else if (thisColumn < locColumn) {
                rv = -1;
            } else if ((thisRow == locRow) && (thisColumn == locColumn)) {
                rv = 0;
            }
        }
        return rv;
    }
</pre>