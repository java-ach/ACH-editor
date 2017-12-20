package com.ach.parser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import com.ach.achViewer.ach.ACHBatch;
import com.ach.achViewer.ach.ACHEntry;
import com.ach.achViewer.ach.ACHFile;
import com.ach.achViewer.ach.ACHRecord;
import com.ach.achViewer.ach.ACHRecordAddenda;
import com.ach.achViewer.ach.ACHRecordBatchControl;
import com.ach.achViewer.ach.ACHRecordBatchHeader;
import com.ach.achViewer.ach.ACHRecordEntryDetail;
import com.ach.achViewer.ach.ACHRecordFileControl;
import com.ach.achViewer.ach.ACHRecordFileHeader;
import com.ach.achViewer.ach.AchParserException;

public class ACHFileParser {
	
	private ACHFileParser () {}

	public static ACHFile fromLines(List<String> lines) throws AchParserException {
	    final Vector<String> errorMessages = new Vector<String>(10, 10);

		final ACHFile achFile = new ACHFile();
		boolean foundFileControl = false;
        int rowCount = 0;

        if (lines.isEmpty()) {
            throw new AchParserException("Data is empty");
        }

        if (!lines.get(0).substring(0, 1).equals("1")) {
            throw new AchParserException("Data is not an ACH file.  First character must be a \"1\"");
        }

        ACHBatch achBatch = null;
        ACHEntry achEntry = null;
        int recLength = 94;
        for (String record : lines) {
            rowCount++;
            for (int recStart = 0; recStart < record.length(); recStart += recLength) {
                int endRec = recStart + recLength;
                if (endRec > record.length()) {
                    endRec = record.length();
                }
                ACHRecord achRecord = ACHRecord.parseACHRecord(record
                        .substring(recStart, endRec));
                if (achRecord.isFileHeaderType()) {
                	achFile.setFileHeader((ACHRecordFileHeader) achRecord);
                    recLength = Integer.parseInt(achFile.getFileHeader()
                            .getRecordSize());
                    achEntry = null;
                    achBatch = null;
                } else if (achRecord.isFileControlType()) {
                    if (achEntry != null) {
                        achBatch.addEntryRecs(achEntry);
                        achEntry = null;
                    }
                    if (achBatch != null) {
                    	achFile.addBatch(achBatch);
                        achBatch = null;
                    }
                    if (!foundFileControl) {
                    	achFile.setFileControl((ACHRecordFileControl) achRecord);
                        foundFileControl = true;
                    }
                } else if (achRecord.isBatchHeaderType()) {
                    if (achEntry != null) {
                        achBatch.addEntryRecs(achEntry);
                        achEntry = null;
                    }
                    if (achBatch != null) {
                    	achFile.addBatch(achBatch);
                        achBatch = null;
                    }

                    achBatch = new ACHBatch();
                    achBatch
                            .setBatchHeader((ACHRecordBatchHeader) achRecord);
                } else if (achRecord.isBatchControlType()) {
                    if (achEntry != null) {
                        achBatch.addEntryRecs(achEntry);
                        achEntry = null;
                    }
                    if (achBatch == null) {
                        achBatch = new ACHBatch();
                    }
                    achBatch
                            .setBatchControl((ACHRecordBatchControl) achRecord);
                } else if (achRecord.isEntryDetailType()) {
                    if (achEntry != null) {
                        achBatch.addEntryRecs(achEntry);
                        achEntry = null;
                    }
                    if (achBatch == null) {
                        achBatch = new ACHBatch();
                    }
                    achEntry = new ACHEntry();
                    achEntry
                            .setEntryDetail((ACHRecordEntryDetail) achRecord);
                } else if (achRecord.isAddendaType()) {
                    if (achEntry == null) {
                        achEntry = new ACHEntry();
                    }
                    achEntry.addAddendaRecs((ACHRecordAddenda) achRecord);
                } else {
                    errorMessages.add("Invalid record at row " + rowCount);
                }
            }
        }
        if (rowCount == 1 && errorMessages.size() == 0) {
        	achFile.setFedFile(true);
        }
		return achFile;
	}

	public static ACHFile fromFilename(String filename) {
		ACHFile achfile = new ACHFile();
		
	    final Vector<String> errorMessages = new Vector<String>(10, 10);

        File achFile = new File(filename);
        if (!achFile.exists()) {
            throw new AchParserException("File " + achFile.getPath() + " does not exist");
        }
        if (!achFile.isFile()) {
            throw new AchParserException("File " + achFile.getPath() + " is not a file");
        }
        if (!achFile.canRead()) {
            throw new AchParserException("File " + achFile.getPath() + " cannot be read");
        }

        BufferedReader achReader = null;
        try {
            achReader = new BufferedReader(new FileReader(achFile.getPath()));
        } catch (FileNotFoundException ex) {
            throw new AchParserException("File " + achFile.getPath()
                    + " could not be opened. Reason " + ex.getMessage());
        }

        boolean foundFileControl = false;
        int rowCount = 0;
        try {
            String record = achReader.readLine();
            if (!record.substring(0, 1).equals("1")) {
                throw new AchParserException(
                        "File "
                        + achFile.getPath()
                        + " is not an ACH file.  First character must be a \"1\"");
            }
            ACHBatch achBatch = null;
            ACHEntry achEntry = null;
            int recLength = 94;
            while (record != null) {
                rowCount++;
                for (int recStart = 0; recStart < record.length(); recStart += recLength) {
                    int endRec = recStart + recLength;
                    if (endRec > record.length()) {
                        endRec = record.length();
                    }
                    ACHRecord achRecord = ACHRecord.parseACHRecord(record
                            .substring(recStart, endRec));
                    if (achRecord.isFileHeaderType()) {
                    	achfile.setFileHeader((ACHRecordFileHeader) achRecord);
                        recLength = Integer.parseInt(achfile.getFileHeader()
                                .getRecordSize());
                        achEntry = null;
                        achBatch = null;
                    } else if (achRecord.isFileControlType()) {
                        if (achEntry != null) {
                            achBatch.addEntryRecs(achEntry);
                            achEntry = null;
                        }
                        if (achBatch != null) {
                        	achfile.addBatch(achBatch);
                            achBatch = null;
                        }
                        if (!foundFileControl) {
                        	achfile.setFileControl((ACHRecordFileControl) achRecord);
                            foundFileControl = true;
                        }
                    } else if (achRecord.isBatchHeaderType()) {
                        if (achEntry != null) {
                            achBatch.addEntryRecs(achEntry);
                            achEntry = null;
                        }
                        if (achBatch != null) {
                        	achfile.addBatch(achBatch);
                            achBatch = null;
                        }

                        achBatch = new ACHBatch();
                        achBatch
                                .setBatchHeader((ACHRecordBatchHeader) achRecord);
                    } else if (achRecord.isBatchControlType()) {
                        if (achEntry != null) {
                            achBatch.addEntryRecs(achEntry);
                            achEntry = null;
                        }
                        if (achBatch == null) {
                            achBatch = new ACHBatch();
                        }
                        achBatch
                                .setBatchControl((ACHRecordBatchControl) achRecord);
                    } else if (achRecord.isEntryDetailType()) {
                        if (achEntry != null) {
                            achBatch.addEntryRecs(achEntry);
                            achEntry = null;
                        }
                        if (achBatch == null) {
                            achBatch = new ACHBatch();
                        }
                        achEntry = new ACHEntry();
                        achEntry
                                .setEntryDetail((ACHRecordEntryDetail) achRecord);
                    } else if (achRecord.isAddendaType()) {
                        if (achEntry == null) {
                            achEntry = new ACHEntry();
                        }
                        achEntry.addAddendaRecs((ACHRecordAddenda) achRecord);
                    } else {
                        errorMessages.add("Invalid record at row " + rowCount);
                    }
                }
                record = achReader.readLine();
            }
        } catch (IOException ex) {
            errorMessages.add("File " + achFile.getPath()
                    + " could not be processed. Reason " + ex.getMessage());
        }
        try {
            achReader.close();
        } catch (Exception ex) {
        }
        if (rowCount == 1 && errorMessages.size() == 0) {
            achfile.setFedFile(true);
        }
		return achfile;
	}


}