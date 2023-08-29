/*
 * Copyright (c) 2010-2023. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package plugins.fab.ROITagger;

import icy.gui.main.GlobalSequenceListener;
import icy.main.Icy;
import icy.sequence.Sequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage painter of tag. Ensure a new sequence will get the painter.
 *
 * @author Fabrice de Chaumont
 */
public class TagPainterManager implements GlobalSequenceListener {

    static TagPainter tagPainter = new TagPainter();

    public TagPainterManager() {

        final ArrayList<Sequence> sequenceList = Icy.getMainInterface().getSequences();
        for (final Sequence sequence : sequenceList) {
            sequence.addOverlay(tagPainter);
        }

        Icy.getMainInterface().addGlobalSequenceListener(this);

    }

    @Override
    public void sequenceOpened(final Sequence sequence) {
        sequence.addOverlay(tagPainter);
    }

    /**
     * {@inheritDoc}
     *
     * @param sequence {@inheritDoc}
     */
    @Override
    public void sequenceClosed(final Sequence sequence) {

    }

    public void stopManager() {
        Icy.getMainInterface().removeGlobalSequenceListener(this);
        final List<Sequence> sequenceList = Icy.getMainInterface().getSequencesContaining(tagPainter);
        for (final Sequence sequence : sequenceList) {
            sequence.removeOverlay(tagPainter);
        }
    }
}
