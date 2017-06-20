package exercise.util;


import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ContentComparator {

    private final String original;
    private final String revised;

    public ContentComparator(String original, String revised) {
        this.original = original;
        this.revised = revised;
    }

    public List<Chunk> getChangesFromOriginal() throws IOException {
        return getChunksByType(Delta.TYPE.CHANGE);
    }

    public List<Chunk> getInsertsFromOriginal() throws IOException {
        return getChunksByType(Delta.TYPE.INSERT);
    }

    public List<Chunk> getDeletesFromOriginal() throws IOException {
        return getChunksByType(Delta.TYPE.DELETE);
    }

    public List<Chunk> getAllChangedFromOriginal() throws IOException {
        final List<Chunk> listOfChanges = new ArrayList<Chunk>();
        listOfChanges.addAll(getChunksByType(Delta.TYPE.CHANGE));
        listOfChanges.addAll(getChunksByType(Delta.TYPE.INSERT));
        listOfChanges.addAll(getChunksByType(Delta.TYPE.DELETE));
        return listOfChanges;
    }

    public long getChangesCount() throws IOException {
        long count = 0;
        List<Chunk> listOfChanges = getAllChangedFromOriginal();
        for (Chunk chunk : listOfChanges) {
            for (Object o : chunk.getLines()) {
                String line = (String) o;
                count += line.length();
            }
        }
        return count;
    }

    public double getChangesPercentage() throws IOException {
        long changesCount = getChangesCount();
        return 100*changesCount/original.length();
    }

    private List<Chunk> getChunksByType(Delta.TYPE type) throws IOException {
        final List<Chunk> listOfChanges = new ArrayList<Chunk>();
        final List<Delta> deltas = getDeltas();
        for (Delta delta : deltas) {
            if (delta.getType() == type) {
                listOfChanges.add(delta.getRevised());
            }
        }
        return listOfChanges;
    }

    private List<Delta> getDeltas() throws IOException {

        final List<String> originalFileLines = contentToLines(original);
        final List<String> revisedFileLines = contentToLines(revised);

        final Patch patch = DiffUtils.diff(originalFileLines, revisedFileLines);

        return patch.getDeltas();
    }

    private List<String> contentToLines(String content) throws IOException {
        final List<String> lines = new ArrayList<String>();
        String line;
        final BufferedReader in = new BufferedReader(new StringReader(content));
        while ((line = in.readLine()) != null) {
            lines.add(line);
        }

        return lines;
    }

}