/*
 * Copyright (C) 2014 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package exomesuite.vcfreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class MISTReader {

    private final Queue<Mist> exons;
    private BufferedReader input;
    private final static int MAX_BUFFER = 10;
    private final File file;

    private final MyQueue<Mist> list;

    public MISTReader(File file) {
        list = new MyQueue<>(10);
        this.file = file;
        exons = new ArrayDeque<>(MAX_BUFFER);
        try {
            input = new BufferedReader(new FileReader(file));
            // Header
            input.readLine();
            // A first exon, courtesy of the house.
            list.add(new Mist(input.readLine()));

//            for (int i = 0; i < MAX_BUFFER; i++) {
//                String line = input.readLine();
//                if (line != null) {
//                    exons.offer(new Mist(line));
//                }
//            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MISTReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MISTReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Checks if the variant is inside a mist region of this file. Check variants in order, since
     * this method do not reread the file, it only walks straight, until an exon contains the
     * variant or an exon overheads the variant.
     *
     * @param variant
     * @return true if an exon contains the variant, false if reached an exon with a higher position
     * than the variant.
     */
    public boolean contains(Variant variant) {
        return getRegion(variant) != null;
    }

    /**
     *
     * @param variant
     * @return
     */
    public Mist getRegion(Variant variant) {
        int pos = variant.getPos();
        for (Mist mist : list.elements) {
            if (mist.contains(variant)) {
                return mist;
            }
        }

        if (list.see().getChrom().equals(variant.getChrom())) {
            if (list.see().getPoorEnd() < variant.getPos()) {
                while (readRegion()) {
                    if (list.see().contains(variant)) {
                        return list.see();
                    } else if (list.see().getPoorStart() > variant.getPos()) {
                        return null;
                    }

                }
            }
        } else {

        }
//        for (Mist mist : exons) {
//            if (mist.contains(variant)) {
//                return mist;
//            }
//        }
//        // Fetch new chrom.
//        while (!exons.peek().getChrom().equals(variant.getChrom())) {
//            if (!readRegion()) {
//                return null;
//            }
//        }
        // Let's check if the exons are too low in position.
        // ¿Is the last exon lower than the variant coordinate? Then, I need to fetch more exons.
        if (exons.peek().getPoorEnd() < pos) {
            // Will add mist lines until it falls into a mis region (true) or an entry exon overheads the variant (false)
            while (true) {
                if (!readRegion()) {
                    return null;
                }
                if (exons.peek().contains(variant)) {
                    return exons.peek();
                } else if (exons.peek().getPoorStart() > pos) {
                    return null;
                }
            }
        }
        return null;
    }

    public boolean crossContains(Variant v) {
        for (Mist mist : exons) {
            if (mist.contains(v)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads a line from mist file, creates a MIST with it and adds it to the queue. If the queue is
     * full, removes the first element.
     *
     * @return true if a region could be read
     */
    private boolean readRegion() {
        try {
            String s = input.readLine();
            if (s != null) {
                list.add(new Mist(s));
//                if (exons.size() == MAX_BUFFER) {
//                    exons.poll();
//                }
//                exons.offer(new Mist(s));
                return true;
            } else {
                return false;
            }
        } catch (IOException ex) {
            Logger.getLogger(MISTReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;

    }

    /**
     * The more recent first.
     *
     * @param <T>
     */
    private static class MyQueue<T> {

        private final List<T> elements;
        private final int capacity;

        public MyQueue(int capacity) {
            this.elements = new ArrayList<>();
            this.capacity = capacity;
        }

        public int getCapacity() {
            return capacity;
        }

        /**
         * Adds an element on first position (0). If queue reaches capacity, the last element is
         * dropped.
         *
         * @param element
         */
        public void add(T element) {
            if (elements.size() == capacity) {
                elements.remove(capacity - 1);
            }
            elements.add(element);
        }

        public T see() {
            return elements.get(0);
        }
    }
}
