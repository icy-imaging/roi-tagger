package plugins.fab.ROITagger;

import icy.canvas.Canvas2D;
import icy.canvas.IcyCanvas;
import icy.gui.util.GuiUtil;
import icy.painter.AbstractPainter;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.util.XMLUtil;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

// TODO: support 3D


/**
 * paint tags of sequences
 * 
 * @author Fabrice de Chaumont
 */
public class TagPainter extends AbstractPainter {

	// TODO: cache tag render.
	// Will be possible when XML Persitance Listener will be introduced in the kernel of ICY.
	//HashMap< ROI , ArrayList<Tag> > roi2TagList = new HashMap< ROI , ArrayList<Tag> >();
	
	final int FONT_SIZE = 16;
	
	@Override
	public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas) {				
		
		
		if ( canvas instanceof Canvas2D )
		{
			int test = 0;
			
			
			
			if ( g == null ) return;
			
			int fontSize = (int)ROI2D.canvasToImageLogDeltaX(canvas, FONT_SIZE ) ;
			if ( fontSize < 1 ) fontSize = 1;
			Font font = new Font( "Arial" , Font.BOLD , fontSize );	
			Rectangle2D oneLetterBound = GuiUtil.getStringBounds( g , font, "X" );
			g.setFont( font );		
			
			Node tagNode = sequence.getNode("tags");
			if ( tagNode== null ) return;			
		
			ArrayList<Element> roiElementList = XMLUtil.getSubElements( tagNode );
			
			for ( Element roiElement : roiElementList )
			{
				int roiId = XMLUtil.getAttributeIntValue( roiElement, "ROI_ID" , -1 );				

				ArrayList<Element> roiTagList = XMLUtil.getSubElements( roiElement );	
				ArrayList<ROI> roiList = sequence.getROIs();
				float offsetYIncrementer = (int)ROI2D.canvasToImageLogDeltaX(canvas, FONT_SIZE ) ;				
				
				for ( ROI roi : roiList )
				{

					ArrayList<DisplayTxt> displayTxtList = new ArrayList<DisplayTxt>();

					if ( roi.getId() == roiId )
					{						
						g.setColor( roi.getColor() );
						

						int offsetY = (int)(offsetYIncrementer+((roiTagList.size()/2f) * -offsetYIncrementer ));
						for ( Element tag : roiTagList )
						{
							
							String name = XMLUtil.getAttributeValue( tag , "name", "no name");

							name = " "+name+" "; 
							Rectangle2D nameBounds = GuiUtil.getStringBounds( g , font, name );							

							if ( roi instanceof ROI2D )
							{
								test++;
								ROI2D roi2D = (ROI2D) roi;		
								displayTxtList.add( new DisplayTxt ( name, new Rectangle2D.Double( 
										roi2D.getBounds2D().getCenterX() - nameBounds.getWidth() / 2  ,
										roi2D.getBounds2D().getCenterY()+offsetY ,
										nameBounds.getWidth(),
										nameBounds.getHeight()
										) ) );
								//								g.drawString( name , 
								//										(int)(roi2D.getBounds2D().getCenterX() - nameBound.getWidth() / 2 ) , 
								//										(int)roi2D.getBounds2D().getCenterY()+offsetY );
							}
							offsetY+=offsetYIncrementer;


						}
					}
							// display the background

							Rectangle2D blackBox = null;
							for ( DisplayTxt dt : displayTxtList )
							{
								if ( blackBox == null )
								{
									blackBox = new Rectangle2D.Double( dt.bounds.getX() , dt.bounds.getY() , dt.bounds.getWidth() , dt.bounds.getHeight() ) ;
								}else
								{
									blackBox = blackBox.createUnion( dt.bounds );
								}
							}

							if ( blackBox != null )
							{
								g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f ) );
								Color oldColor = g.getColor();
								g.setColor( Color.white );			
								g.translate( 0 , -oneLetterBound.getHeight()*0.8 );
								g.fill( blackBox );
								g.translate( 0 , +oneLetterBound.getHeight()*0.8 );
								g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f ) );
								g.setColor( oldColor );
							}

							// display the text
							g.setColor( Color.black );

							for ( DisplayTxt dt : displayTxtList )
							{
								g.drawString( dt.string , (float)dt.bounds.getX() , (float) dt.bounds.getY() );
							}

//						}
//					}
				}

			}
			
		}
		
	}
	
	
	class DisplayTxt
	{
		String string;
		Rectangle2D bounds;
		public DisplayTxt( String string , Rectangle2D bounds ) {
			this.string = string;
			this.bounds = bounds;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
