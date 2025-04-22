/*
 * Copyright (c) 2010-2024. Institut Pasteur.
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

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.extension.plugin.abstract_.PluginActionable;
import org.bioimageanalysis.icy.extension.plugin.annotation_.IcyPluginName;
import org.bioimageanalysis.icy.gui.GuiUtil;
import org.bioimageanalysis.icy.gui.component.ComponentUtil;
import org.bioimageanalysis.icy.gui.frame.IcyFrame;
import org.bioimageanalysis.icy.io.FileUtil;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.bioimageanalysis.icy.model.overlay.Overlay;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import plugins.fab.ROIPool.TextDialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

@IcyPluginName("ROI Tagger")
public class ROITagger extends PluginActionable implements ActionListener {
    ArrayList<ROI> tagList = new ArrayList<>();

    HashMap<ROI, ArrayList<Tag>> roi2TagList = new HashMap<>();

    File XMLFile = null;
    JPanel panelTagList = new JPanel();
    ArrayList<Element> tagElements = new ArrayList<>();
    JButton addANewTagToPool = new JButton("Add a new tag");
    JPanel mainPanel;
    Document document;
    IcyFrame mainFrame;
    JCheckBox alwaysOnTopCheckBox = new JCheckBox("Keep this window on top", true);
    JButton removeTagFromSelectedROIButton = new JButton("Remove tags from selected ROI");
//	JButton addANewPainter = new JButton("Painter test");

    @Override
    public void run() {
        mainPanel = new JPanel();
        mainFrame = GuiUtil.generateTitleFrame("ROI Tagger", mainPanel, new Dimension(200, 70), true, true, true, true);
        mainFrame.detach();
        mainFrame.setAlwaysOnTop(true);
        mainPanel.setLayout(new BorderLayout());

        final JPanel commandPanel = new JPanel();
        commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.PAGE_AXIS));

        commandPanel.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
        commandPanel.add(GuiUtil.createLineBoxPanel(addANewTagToPool));
        commandPanel.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
        commandPanel.add(GuiUtil.createLineBoxPanel(removeTagFromSelectedROIButton));
        removeTagFromSelectedROIButton.addActionListener(this);
//		commandPanel.add( GuiUtil.createLineBoxPanel( addANewPainter ) );
//		addANewPainter.addActionListener( this );
        commandPanel.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
        commandPanel.add(GuiUtil.createLineBoxPanel(alwaysOnTopCheckBox));
        commandPanel.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));

        ComponentUtil.setFixedHeight(addANewTagToPool, 30);
        addANewTagToPool.addActionListener(this);

        alwaysOnTopCheckBox.addActionListener(this);
        // Init XML File

        String fileName = System.getProperty("user.home");
        fileName += FileUtil.separator + "icy" + FileUtil.separator + "ROI tagger plugin";
        FileUtil.createDir(new File(fileName));
        fileName += FileUtil.separator + "ROITagger.xml";
        XMLFile = new File(fileName);

        // ROI list

        panelTagList.setLayout(new BoxLayout(panelTagList, BoxLayout.PAGE_AXIS));

        document = loadXMLDocument();
        refreshPanelROIList();
        //commandPanel.add( GuiUtil.createLineBoxPanel( panelROIList ) );

        // Tag List

        // Init frame

        mainPanel.add(commandPanel, BorderLayout.NORTH);
        final JScrollPane roiScrollPanel = new JScrollPane(panelTagList);
        mainPanel.add(roiScrollPanel, BorderLayout.CENTER);
        roiScrollPanel.setBorder(new TitledBorder("Tag List"));

        ComponentUtil.setFixedWidth(panelTagList, 250);

        mainFrame.pack();
        mainFrame.setVisible(true);
        mainFrame.addToDesktopPane();
        mainFrame.requestFocus();

        new TagPainterManager();
    }


    private @NotNull Document loadXMLDocument() {
        Document document = XMLUtil.loadDocument(XMLFile);
        tagElements = new ArrayList<>();

        if (document == null) {
            document = XMLUtil.createDocument(true);
            // create a pool for further improvement like multi pool.
            final Element poolElement = XMLUtil.addElement(document.getDocumentElement(), "Pool");
            poolElement.setAttribute("name", "default");
        }
        return document;
    }


    private void refreshPanelROIList() {
        tagElements = new ArrayList<>();

        final ArrayList<Element> poolList = XMLUtil.getElements(document.getDocumentElement(), "Pool");

        for (final Element poolElement : poolList) {
            final ArrayList<Element> tagList = XMLUtil.getElements(poolElement, "tag");
            tagElements.addAll(tagList);

        }

        panelTagList.removeAll();

        for (final Element tagElement : tagElements) {
            panelTagList.add(GuiUtil.createLineBoxPanel(createTagButton(tagElement)));
        }

        panelTagList.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));

        if (tagElements.isEmpty()) {
            panelTagList.add(GuiUtil.createLineBoxPanel(new JLabel("The list is empty.")));
            panelTagList.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
        }

        if (tagElements.size() < 5) {
            panelTagList.add(GuiUtil.createLineBoxPanel(new JLabel("To add a tag, click 'add a tag'")));
            panelTagList.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
            panelTagList.add(GuiUtil.createLineBoxPanel(new JLabel("The tags will then be listed here, clicking a tag")));
            panelTagList.add(GuiUtil.createLineBoxPanel(new JLabel("in the list will attach it to the current roi")));
            panelTagList.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
            panelTagList.add(GuiUtil.createLineBoxPanel(new JLabel("This tag list is automaticaly saved on your")));
            panelTagList.add(GuiUtil.createLineBoxPanel(new JLabel("computer in the file home/icy/ROI Tagger.xml")));
            panelTagList.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
            panelTagList.add(GuiUtil.createLineBoxPanel(new JLabel("This help message will not be displayed as")));
            panelTagList.add(GuiUtil.createLineBoxPanel(new JLabel("you will have add some tags in the list.")));
            panelTagList.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
            panelTagList.add(GuiUtil.createLineBoxPanel(new JLabel("This plugin support multi selection (ctrl+click)")));
            panelTagList.add(GuiUtil.createLineBoxPanel(new JLabel("You can then add tag to multiple ROI, or")));
            panelTagList.add(GuiUtil.createLineBoxPanel(new JLabel("remove all of them in one click.")));
            panelTagList.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
        }

        panelTagList.add(GuiUtil.createLineBoxPanel(Box.createVerticalGlue()));

        mainPanel.updateUI();
    }

    private @NotNull JPanel createTagButton(final Element tagElement) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        final String name = XMLUtil.getElementValue(tagElement, "name", "no name");

        final JButton tagButton = new JButton(name);

        tagButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {

                        final Sequence sequence = Icy.getMainInterface().getActiveSequence();
                        if (sequence != null) {
                            sequence.refreshXMLData();
                            final ArrayList<ROI> roiList = sequence.getSelectedROIs();

                            for (final ROI roi : roiList) {
                                final Element roiElement = getROIElement(sequence, roi.getId());

                                //System.out.println( "create ROI : " + roi.getId() );

                                final Element newTagElement = XMLUtil.addElement(roiElement, "tag");
                                XMLUtil.setAttributeValue(newTagElement, "name", name);

                            }
                            sequence.overlayChanged((Overlay) null);

                        }

                    }

                    /**
                     * Look for the ROI element of the given id,
                     * if none is found, create a new one.
                     */
                    private Element getROIElement(final Sequence sequence, final int id) {
                        // get the tags node.
                        final Node tagNode = sequence.getNode("tags");
                        /*if (tagNode == null) {
                            tagNode = sequence.setNode("tags");
                        }*/

                        // parse ROI to find the good node
                        final ArrayList<Element> elementROIList = XMLUtil.getElements(tagNode);
                        for (final Element roiElement : elementROIList) {
                            //System.out.println("loooking for roi id : " + id );
                            //System.out.println("found:"+  XMLUtil.getAttributeIntValue( roiElement, "ROI_ID" , -1 ) );
                            //System.out.println( roiElement );
                            if (XMLUtil.getAttributeIntValue(roiElement, "ROI_ID", -1) == id) {
                                //System.out.println("roi found !");
                                return roiElement;
                            }
                        }

                        // no corresponding ROI element found. create one.

                        final Element roiElement = XMLUtil.addElement(tagNode, "roi");
                        XMLUtil.setAttributeValue(roiElement, "ROI_ID", "" + id);

                        return roiElement;
                        // create a new ROI if no node exists for the current ROI.
                        //if ( Integer.parseInt( XMLUtil.getElementValue( roiElement , "ROI_ID" , "-1")  ) != roi.getId() )
                        //{
                        //  roiElement = XMLUtil.addElement( tagNode , "roi" );
                        //}
                    }
                }
        );


        final JButton removeButton = new JButton("x");
        removeButton.setToolTipText("Remove this tag from this list.");
        removeButton.addActionListener(e-> {
            XMLUtil.removeAllChildren(tagElement);
            XMLUtil.removeNode(tagElement.getParentNode(), tagElement);
            XMLUtil.saveDocument(document, XMLFile);
            refreshPanelROIList();
        });

        ComponentUtil.setFixedWidth(tagButton, 130);
        ComponentUtil.setFixedWidth(removeButton, 30);

        panel.add(GuiUtil.createLineBoxPanel(tagButton, removeButton));

        ComponentUtil.setFixedHeight(panel, 20);

        return panel;
    }


    /**
     * remove ROI node of ID id.
     */
    private void removeROINode(final @NotNull Sequence sequence, final int id) {
        // get the tags node.
        final Node tagNode = sequence.getNode("tags");
        if (tagNode == null) return;

        // list of ROIs
        final ArrayList<Element> elementROIList = XMLUtil.getElements(tagNode);
        for (final Element roiElement : elementROIList) {
            // Remove if ID is found
            if (XMLUtil.getAttributeIntValue(roiElement, "ROI_ID", -1) == id) {
                XMLUtil.removeAllChildren(roiElement);
                XMLUtil.removeNode(roiElement.getParentNode(), roiElement);
            }
        }
        sequence.overlayChanged((Overlay) null);
    }

    @Override
    public void actionPerformed(final @NotNull ActionEvent e) {
        //if ( e.getSource() == addANewPainter ) // FIXME : remove this test code
        //{
        //	getFocusedSequence().addPainter( new TagPainter() );
        //}

        if (e.getSource() == removeTagFromSelectedROIButton) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            if (sequence != null) {
                sequence.refreshXMLData();
                final ArrayList<ROI> roiList = sequence.getSelectedROIs();

                for (final ROI roi : roiList) {
                    removeROINode(sequence, roi.getId());
                }
            }
        }

        if (e.getSource() == alwaysOnTopCheckBox) {
            mainFrame.setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
        }

        if (e.getSource() == addANewTagToPool) {
            try {
                final Element poolNode = XMLUtil.getElements(document.getDocumentElement(), "Pool").getFirst();

                final String name;
                mainFrame.setVisible(false);
                final TextDialog textDialog = new TextDialog("Name of new tag", "Name of new tag:", "");
                textDialog.pack();
                //textDialog.setSize( 200 , 200 );

                textDialog.setLocationRelativeTo(null);
                textDialog.requestFocus();
                textDialog.setVisible(true);

                if (textDialog.isOk) {
                    name = textDialog.getText();
                    final Element tagElement = XMLUtil.addElement(poolNode, "tag");
                    XMLUtil.setElementValue(tagElement, "name", name);
                    XMLUtil.saveDocument(document, XMLFile);
                    refreshPanelROIList();
                }
            }
            finally {
                mainFrame.setVisible(true);
            }
        }
    }
}
