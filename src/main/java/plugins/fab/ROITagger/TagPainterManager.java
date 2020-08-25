package plugins.fab.ROITagger;

import icy.gui.main.MainAdapter;
import icy.gui.main.MainEvent;
import icy.main.Icy;
import icy.sequence.Sequence;

import java.util.ArrayList;

/**
 * 
 * Manage painter of tag. Ensure a new sequence will get the painter.
 * 
 * @author Fabrice de Chaumont
 *
 */
public class TagPainterManager extends MainAdapter {

	static TagPainter tagPainter = new TagPainter();	
	
	public TagPainterManager() {
		
		ArrayList<Sequence> sequenceList = Icy.getMainInterface().getSequences();
		for ( Sequence sequence : sequenceList )
		{
			sequence.addPainter( tagPainter );
		}
		
		Icy.getMainInterface().addListener( this );
		
	}
	
	@Override
	public void sequenceOpened(MainEvent event) {

		Sequence sequence = (Sequence) event.getSource();

		sequence.addPainter( tagPainter );
	}
	
	public void stopManager()
	{
		Icy.getMainInterface().removeListener( this );
		ArrayList<Sequence> sequenceList = Icy.getMainInterface().getSequencesContaining( tagPainter );
		for ( Sequence sequence : sequenceList )
		{
			sequence.removePainter( tagPainter );
		}
	}
	
	
}
