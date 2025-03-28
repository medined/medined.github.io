---
layout: post
title: POI Optimization - eliminating trailing empty rows from an HSSFSheet.
date: '2004-04-26T13:17:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2004-04-26T13:22:32.013-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108299986325091506
blogger_orig_url: http://affy.blogspot.com/2004/04/poi-optimization-eliminating-trailing.md
year: 2004
theme: java
---

<p>While my spreadsheet has only 7 rows with data, POI creates over 65,000 rows in the HSSFSheet object. This leads to a
    large amount of essentially unused memory. In order to free that memory, the following code snippet from my version
    of the <code>public HSSFWorkbook(POIFSFileSystem fs, boolean preserveNodes)</code> method works from bottom towards
    the top of the sheet removing empty rows. The code in <b>bold</b> performs the optimization.</p>


<pre>
            HSSFSheet hsheet = new HSSFSheet(workbook, sheet);
<b>
            boolean stop = false;
            boolean nonBlankRowFound;
            short c;
            HSSFRow lastRow = null;
            HSSFCell cell = null;

            while (stop == false) {
                nonBlankRowFound = false;
                lastRow = hsheet.getRow(hsheet.getLastRowNum());
                for (c = lastRow.getFirstCellNum(); c <= lastRow.getLastCellNum(); c++) {
                    cell = lastRow.getCell(c);
                    if (cell != null && lastRow.getCell(c).getCellType() != HSSFCell.CELL_TYPE_BLANK) {
                        nonBlankRowFound = true;
                    }
                }
                if (nonBlankRowFound == true) {
                    stop = true;
                } else {
                    hsheet.removeRow(lastRow);
                }
            }
</b>
            sheets.add(hsheet);
</pre>