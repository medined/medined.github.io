---
layout: post
title: POI Optimization - Speeding up org.apache.poi.hssf.usermodel.HSSFSheet.setPropertiesFromSheet()
date: '2004-04-22T16:24:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2004-04-22T16:28:31.280-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108266546361845487
blogger_orig_url: http://affy.blogspot.com/2004/04/poi-optimization-speeding-up.md
year: 2004
theme: java
---

<p>This setPropertiesFromSheet() method potentially logs a lot of information. The logging calls are expensive because
    of the objects created when assembling the log messages. Using the check() method of the logging sub-system can
    prevent object creation when debugging is turned off. This change reduced execution time of my test case by 14.2
    seconds and prevented the creation of 781,632 objects.</p>


<pre>
    /**
     * used internally to set the properties given a Sheet object
     */
    private void setPropertiesFromSheet(Sheet sheet) {
        long timestart = 0; // only used for debugging.
        int sloc = sheet.getLoc();
        RowRecord row = sheet.getNextRow();

        while (row != null) {
            createRowFromRecord(row);
            row = sheet.getNextRow();
        }
        sheet.setLoc(sloc);
        CellValueRecordInterface cval = sheet.getNextValueRecord();

        if (log.check(DEBUG)) {
            timestart = System.currentTimeMillis();
            log.log(DEBUG, "Time at start of cell creating in HSSF sheet = ", new Long(timestart));
        }
        HSSFRow lastrow = null;

        while (cval != null) {
            long cellstart = System.currentTimeMillis();
            HSSFRow hrow = lastrow;

            if ((lastrow == null) || (lastrow.getRowNum() != cval.getRow())) {
                hrow = getRow(cval.getRow());
            }
            if (hrow != null) {
                lastrow = hrow;
                if (log.check(DEBUG)) {
                    log.log(DEBUG, "record id = " + Integer.toHexString(((Record) cval).getSid()));
                }
                hrow.createCellFromRecord(cval);
                cval = sheet.getNextValueRecord();
                if (log.check(DEBUG)) {
                    log.log(DEBUG, "record took ", new Long(System.currentTimeMillis() - cellstart));
                }
            } else {
                cval = null;
            }
        }
        if (log.check(DEBUG)) {
            log.log(DEBUG, "total sheet cell creation took ", new Long(System.currentTimeMillis() - timestart));
        }
    }
</pre>