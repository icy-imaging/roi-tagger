package plugins.fab.ROITagger;

import icy.file.FileUtil;
import icy.gui.component.ComponentUtil;
import icy.gui.frame.IcyFrame;
import icy.gui.util.GuiUtil;
import icy.main.Icy;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginImageAnalysis;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.util.XMLUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import plugins.fab.ROIPool.TextDialog;

public class ROITagger extends Plugin implements PluginImageAnalysis , ActionListener {

	ArrayList<ROI> tagList = new ArrayList<ROI>();
	
	HashMap< ROI , ArrayList<Tag> > roi2TagList = new HashMap< ROI , ArrayList<Tag> >();
	
	File XMLFile = null;
	JPanel panelTagList = new JPanel();
	ArrayList<Element> tagElements = new ArrayList<Element>();
	JButton addANewTagToPool = new JButton("Add a new tag");
	JPanel mainPanel;
	Document document;
	IcyFrame mainFrame;
	JCheckBox alwaysOnTopCheckBox = new JCheckBox("Keep this window on top" , true );
	JButton removeTagFromSelectedROIButton = new JButton("Remove tags from selected ROI");
//	JButton addANewPainter = new JButton("Painter test");

	
	@Override
	public void compute() {


		mainPanel = new JPanel();		
		mainFrame = GuiUtil.generateTitleFrame("ROI Tagger", mainPanel, new Dimension( 200,70 ), true, true , true , true );	
		mainFrame.detach();
		mainFrame.setAlwaysOnTop( true );
		mainPanel.setLayout( new BorderLayout() );
		

		JPanel commandPanel = new JPanel();
		commandPanel.setLayout( new BoxLayout( commandPanel , BoxLayout.PAGE_AXIS ) );

		commandPanel.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
		commandPanel.add( GuiUtil.createLineBoxPanel( addANewTagToPool ) );
		commandPanel.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
		commandPanel.add( GuiUtil.createLineBoxPanel( removeTagFromSelectedROIButton ) );
		removeTagFromSelectedROIButton.addActionListener( this );
//		commandPanel.add( GuiUtil.createLineBoxPanel( addANewPainter ) );
//		addANewPainter.addActionListener( this );
		commandPanel.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
		commandPanel.add( GuiUtil.createLineBoxPanel( alwaysOnTopCheckBox ) );
		commandPanel.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
		
		ComponentUtil.setFixedHeight( addANewTagToPool , 30 );
		addANewTagToPool.addActionListener( this );
		
		alwaysOnTopCheckBox.addActionListener( this );
		// Init XML File
		
		String fileName = System.getProperty("user.home") ;
		fileName += FileUtil.separator + "icy" + FileUtil.separator + "ROI tagger plugin";
		FileUtil.createDir( new File( fileName ) );		
		fileName += FileUtil.separator + "ROITagger.xml";
		XMLFile = new File( fileName );
		
		// ROI list
		
		panelTagList.setLayout( new BoxLayout( panelTagList , BoxLayout.PAGE_AXIS ) );		
		
		document = loadXMLDocument();
		refreshPanelROIList();
		//commandPanel.add( GuiUtil.createLineBoxPanel( panelROIList ) );
		
		// Tag List
		
		// Init frame
		
		mainPanel.add( commandPanel , BorderLayout.NORTH );
		JScrollPane roiScrollPanel = new JScrollPane( panelTagList );
		mainPanel.add( roiScrollPanel , BorderLayout.CENTER );
		roiScrollPanel.setBorder( new TitledBorder( "Tag List" ) );
		
		ComponentUtil.setFixedWidth( panelTagList , 250 );		
		
		mainFrame.pack();
		mainFrame.setVisible( true );
		mainFrame.addToMainDesktopPane();
		mainFrame.requestFocus();
		
		new TagPainterManager();		
	
	}
	
	
	private Document loadXMLDocument()
	{
		Document document = XMLUtil.loadDocument( XMLFile );
		tagElements = new ArrayList<Element>();
		
		if ( document == null )
		{			
			document = XMLUtil.createDocument( true );
			// create a pool for further improvement like multi pool.
			Element poolElement = XMLUtil.addElement( document.getDocumentElement() , "Pool" ); 
			poolElement.setAttribute("name", "default");
		}
		return document;
	}


	private void refreshPanelROIList() {

		tagElements = new ArrayList<Element>();

		ArrayList<Element> poolList = XMLUtil.getSubElements( document.getDocumentElement() , "Pool" );
		
		for ( Element poolElement : poolList )
		{
			ArrayList<Element> tagList = XMLUtil.getSubElements( poolElement , "tag" );
			for ( Element tagElement : tagList )
			{
				tagElements.add( tagElement );				
			}
			
		}
		
		
		panelTagList.removeAll();
		
		for ( Element tagElement : tagElements )
		{		
			panelTagList.add( GuiUtil.createLineBoxPanel( createTagButton( tagElement ) ) );
		}
		
		panelTagList.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
		
		if ( tagElements.size() == 0 )
		{
			panelTagList.add( GuiUtil.createLineBoxPanel( new JLabel("The list is empty.") ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
		}
		
		if ( tagElements.size() < 5 )
		{
			panelTagList.add( GuiUtil.createLineBoxPanel( new JLabel("To add a tag, click 'add a tag'") ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( new JLabel("The tags will then be listed here, clicking a tag") ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( new JLabel("in the list will attach it to the current roi") ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( new JLabel("This tag list is automaticaly saved on your") ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( new JLabel("computer in the file home/icy/ROI Tagger.xml") ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( new JLabel("This help message will not be displayed as") ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( new JLabel("you will have add some tags in the list.") ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( new JLabel("This plugin support multi selection (ctrl+click)") ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( new JLabel("You can then add tag to multiple ROI, or") ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( new JLabel("remove all of them in one click.") ) );
			panelTagList.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 10 ) ) );
		}
		
		panelTagList.add( GuiUtil.createLineBoxPanel( Box.createVerticalGlue() ) );		
				
		mainPanel.updateUI();
		
	}

	private JPanel createTagButton(final Element tagElement) {
		
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel , BoxLayout.PAGE_AXIS ) );
		
		final String name = XMLUtil.getElementValue( tagElement, "name", "no name" );
	
		JButton tagButton = new JButton( name );

		tagButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				Sequence sequence = Icy.getMainInterface().getFocusedSequence();
				if ( sequence != null )
				{
					sequence.refreshXMLData();					
					ArrayList<ROI> roiList = sequence.getSelectedROIs();

					for ( ROI roi : roiList )
					{
						Element roiElement = getROIElement( sequence, roi.getId() );

//						System.out.println( "create ROI : " + roi.getId() );						

						Element newTagElement = XMLUtil.addElement( roiElement , "tag" );
						XMLUtil.setAttributeValue( newTagElement , "name", name );

					}
					sequence.painterChanged( null );

				}

			}

			/**
			 * Look for the ROI element of the given id,
			 * if none is found, create a new one.
			 */
			private Element getROIElement( Sequence sequence , int id )
			{
				// get the tags node.
				Node tagNode = sequence.getNode("tags");
				if ( tagNode == null )
				{
					tagNode = sequence.setNode("tags");
				}
				
				// parse ROI to find the good node
				ArrayList<Element> elementROIList = XMLUtil.getSubElements( tagNode );
				for ( Element roiElement : elementROIList )
				{
//					System.out.println("loooking for roi id : " + id );
//					System.out.println("found:"+  XMLUtil.getAttributeIntValue( roiElement, "ROI_ID" , -1 ) );
//					System.out.println( roiElement );
					if ( XMLUtil.getAttributeIntValue( roiElement, "ROI_ID" , -1 ) == id )
					{
//						System.out.println("roi found !");
						return roiElement;
					}
				}
				
				// no corresponding ROI element found. create one.				
				
				Element roiElement = XMLUtil.addElement( tagNode , "roi" );
				XMLUtil.setAttributeValue( roiElement , "ROI_ID", ""+id );

				return roiElement;
				// create a new ROI if no node exists for the current ROI.
//				if ( Integer.parseInt( XMLUtil.getElementValue( roiElement , "ROI_ID" , "-1")  ) != roi.getId() )
//				{
//					roiElement = XMLUtil.addElement( tagNode , "roi" );
//				}
			}
		}

		);
		
		
		JButton removeButton = new JButton("x");
		removeButton.setToolTipText("Remove this tag from this list.");
		removeButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				XMLUtil.removeAllChilds( tagElement );
				XMLUtil.removeNode( tagElement.getParentNode() , tagElement ) ;
				XMLUtil.saveDocument( document , XMLFile ) ;
				refreshPanelROIList();
			}
			
		});
		
		ComponentUtil.setFixedWidth( tagButton , 130 );
		ComponentUtil.setFixedWidth( removeButton , 30 );

		panel.add( GuiUtil.createLineBoxPanel( tagButton , removeButton ) );
	
		ComponentUtil.setFixedHeight( panel , 20 );
		
		return panel;
	}


	/**
	 * remove ROI node of ID id.
	 */
	private void removeROINode(Sequence sequence, int id) {

		// get the tags node.
		Node tagNode = sequence.getNode("tags");
		if ( tagNode == null ) return;

		// list of ROIs
		ArrayList<Element> elementROIList = XMLUtil.getSubElements( tagNode );
		for ( Element roiElement : elementROIList )
		{
			// Remove if ID is found
			if ( XMLUtil.getAttributeIntValue( roiElement, "ROI_ID" , -1 ) == id )
			{
				XMLUtil.removeAllChilds( roiElement );
				XMLUtil.removeNode( roiElement.getParentNode(), roiElement );
			}
		}
		sequence.painterChanged( null );
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

//		if ( e.getSource() == addANewPainter ) // FIXME : remove this test code
//		{
//			getFocusedSequence().addPainter( new TagPainter() );
//		}
		
		if ( e.getSource() == removeTagFromSelectedROIButton )
		{
			Sequence sequence = Icy.getMainInterface().getFocusedSequence();
			if ( sequence != null )
			{
				sequence.refreshXMLData();					
				ArrayList<ROI> roiList = sequence.getSelectedROIs();

				for ( ROI roi : roiList )
				{
					removeROINode( sequence , roi.getId() );
				}
			}			
		}
		
		if ( e.getSource() == alwaysOnTopCheckBox )
		{
			mainFrame.setAlwaysOnTop( alwaysOnTopCheckBox.isSelected() );			
		}

		if ( e.getSource() == addANewTagToPool )
		{
			try
			{
				Element poolNode = XMLUtil.getSubElements( document.getDocumentElement() , "Pool" ).get( 0 );			

				String name ="";
				mainFrame.setVisible( false );
				TextDialog textDialog = new TextDialog("Name of new tag", "Name of new tag:", "" );
				textDialog.pack();
				//textDialog.setSize( 200 , 200 );

				textDialog.setLocationRelativeTo( null );
				textDialog.requestFocus();
				textDialog.setVisible(true);

				if ( textDialog.isOk )
				{
					name = textDialog.getText();
					Element tagElement = XMLUtil.addElement( poolNode , "tag" ); 
					XMLUtil.setElementValue( tagElement , "name", name );
					XMLUtil.saveDocument( document , XMLFile );
					refreshPanelROIList();
				}
			}
			finally
			{
				mainFrame.setVisible( true );
			}

		}
		
	}



	
	
}
