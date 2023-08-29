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

import icy.canvas.Canvas2D;
import icy.canvas.IcyCanvas;
import icy.painter.Overlay;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.util.GraphicsUtil;
import icy.util.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

// TODO: support 3D

/**
 * paint tags of sequences
 *
 * @author Fabrice de Chaumont
 */
public class TagPainter extends Overlay {

    // TODO: cache tag render.
    // Will be possible when XML Persitance Listener will be introduced in the kernel of ICY.
    //HashMap< ROI , ArrayList<Tag> > roi2TagList = new HashMap< ROI , ArrayList<Tag> >();

    final int FONT_SIZE = 16;

    public TagPainter() {
        super("TagPainter");
    }

    @Override
    public void paint(final Graphics2D g, final Sequence sequence, final IcyCanvas canvas) {
        if (canvas instanceof Canvas2D) {
            //int test = 0;

            if (g == null) return;

            int fontSize = (int) canvas.canvasToImageLogDeltaX(FONT_SIZE);
            if (fontSize < 1) fontSize = 1;
            final Font font = new Font("Arial", Font.BOLD, fontSize);
            final Rectangle2D oneLetterBound = GraphicsUtil.getStringBounds(g, font, "X");
            g.setFont(font);

            final Node tagNode = sequence.getNode("tags");
            if (tagNode == null) return;

            final ArrayList<Element> roiElementList = XMLUtil.getElements(tagNode);

            for (final Element roiElement : roiElementList) {
                final int roiId = XMLUtil.getAttributeIntValue(roiElement, "ROI_ID", -1);

                final ArrayList<Element> roiTagList = XMLUtil.getElements(roiElement);
                final ArrayList<ROI> roiList = sequence.getROIs();
                final float offsetYIncrementer = (int) canvas.canvasToImageLogDeltaX(FONT_SIZE);

                for (final ROI roi : roiList) {

                    final ArrayList<DisplayTxt> displayTxtList = new ArrayList<>();

                    if (roi.getId() == roiId) {
                        g.setColor(roi.getColor());


                        int offsetY = (int) (offsetYIncrementer + ((roiTagList.size() / 2f) * -offsetYIncrementer));
                        for (final Element tag : roiTagList) {

                            String name = XMLUtil.getAttributeValue(tag, "name", "no name");

                            name = " " + name + " ";
                            final Rectangle2D nameBounds = GraphicsUtil.getStringBounds(g, font, name);

                            if (roi instanceof ROI2D) {
                                //test++;
                                final ROI2D roi2D = (ROI2D) roi;
                                displayTxtList.add(new DisplayTxt(name, new Rectangle2D.Double(
                                        roi2D.getBounds2D().getCenterX() - nameBounds.getWidth() / 2,
                                        roi2D.getBounds2D().getCenterY() + offsetY,
                                        nameBounds.getWidth(),
                                        nameBounds.getHeight()
                                )));
                                //								g.drawString( name ,
                                //										(int)(roi2D.getBounds2D().getCenterX() - nameBound.getWidth() / 2 ) ,
                                //										(int)roi2D.getBounds2D().getCenterY()+offsetY );
                            }
                            offsetY += (int) offsetYIncrementer;


                        }
                    }
                    // display the background

                    Rectangle2D blackBox = null;
                    for (final DisplayTxt dt : displayTxtList) {
                        if (blackBox == null) {
                            blackBox = new Rectangle2D.Double(dt.bounds.getX(), dt.bounds.getY(), dt.bounds.getWidth(), dt.bounds.getHeight());
                        }
                        else {
                            blackBox = blackBox.createUnion(dt.bounds);
                        }
                    }

                    if (blackBox != null) {
                        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                        final Color oldColor = g.getColor();
                        g.setColor(Color.white);
                        g.translate(0, -oneLetterBound.getHeight() * 0.8);
                        g.fill(blackBox);
                        g.translate(0, oneLetterBound.getHeight() * 0.8);
                        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                        g.setColor(oldColor);
                    }

                    // display the text
                    g.setColor(Color.black);

                    for (final DisplayTxt dt : displayTxtList) {
                        g.drawString(dt.string, (float) dt.bounds.getX(), (float) dt.bounds.getY());
                    }

//						}
//					}
                }

            }

        }

    }

    static class DisplayTxt {
        String string;
        Rectangle2D bounds;

        public DisplayTxt(final String string, final Rectangle2D bounds) {
            this.string = string;
            this.bounds = bounds;
        }
    }
}
