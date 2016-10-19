package haiku;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import java.util.Set;

import javax.swing.*;
import javax.swing.text.*;

/**
 * This program is a haiku generator.  It loads words from a dictionary file, and arranges them according to
 * part of speech and syllabic order.
 * 
 * @author Jobin
 * @version 0.3.2
 */
public class Haiku extends JFrame
{	
	private static final long serialVersionUID = 1L;

	// ---- stores desired sentence structure --- //
	private static final SentenceGraph graph = new SentenceGraph();
	// ---- stores information about loaded words -- //
	private Dictionary dictionary;

	// ---- GUI components ---- //
	private JButton generateButton;
	private JTextPane output;

	
	public Haiku() 
	{		
		final String dictfilename = "dictionary.dic";
		setupWindow();
		try {
			dictionary = new Dictionary(dictfilename);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "Error loading dictionary file \"" 
					+ dictfilename + "\": file not found!", "Haiku Generator", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(1);
		} catch (IllegalStateException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Haiku Generator", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(1);
		}
		
		generateButton.setEnabled(true);
		output.setText("   With a click below, \n"
				+ "   a haiku will soon appear. \n"
				+ "   Why don't you try it?");
		System.out.println("   SETUP COMPLETE");
	}
	
	
	public static void main(String[] args) {
		new Haiku();
	}
	
     
	/**
	 * The backbone of the program.
	 * @return a complete haiku.
	 */
	public void generate() 
	{
		generateButton.setEnabled(false);
		output.setText("Generating a haiku...");
		
		String[] outString = new String[3];
		//TODO: Generate haiku
		graph.reset();
		

		outString[0] = buildSentence(5, graph.getIndex());
		outString[1] = buildSentence(7, graph.getIndex());
		outString[2] = buildSentence(5, graph.getIndex());
		
		System.out.println("done");
		
		//-- consolidate strings for output
		String haiku = "";
		for (int i = 0; i < outString.length; i++)
			haiku += " " + outString[i] + "\n";
		
		// capitalize first letter
		haiku = haiku.substring(0,1).toUpperCase() + haiku.substring(1);
		
		generateButton.setEnabled(true);
		output.setText(haiku);
	}
	  
	/**
	 * 
	 * @param syl
	 * @return
	 */
	private String buildSentence(int syl) 
	{
		//BASE CASES: end of sentence || out of syllables
		if (graph.reachedEnd() || syl <= 0)
			return "";
		
		PartOfSpeech nextPos = graph.advance();
		
		System.out.print("Searching for a " + syl + "-syllable " + nextPos);
		String word = dictionary.getRandomWord(nextPos, syl);
		System.out.println(": " + word);
		
		
		
		return null;
	}
	
	
	/**
	 * This method recursively traverses the supporting sentence structure graph.
	 * 
	 * @param syllableCount the number of syllables remaining in the current line.
	 * @param startIndex the index of the current graph node.
	 * @return a string containing the current haiku line
	 */
	private String buildSentence(int syllablesLeft, int startIndex) 
	{
		//BASE CASES: end of sentence || out of syllables
		if (graph.reachedEnd() || syllablesLeft <= 0)
			return "";
		
		
		//Pick a word for this call to add. If the dictionary runs out, or if 0 syllables are specified,
		// this will return null.
		
		
		PartOfSpeech nextPos = graph.advance();  
		
		System.out.print("Searching for a " + syllablesLeft + "-syllable " + nextPos);
		String word = dictionary.getRandomWord(nextPos, syllablesLeft);
		System.out.println(": " + word);
		
		// if (word == null), no words can be found that meet the criteria.
		if (word != null) {
			
			// Iterate through the edges accessible from this position
			int i = graph.advance(startIndex);
			
			//this stops the sentence from ending on a preposition or article
			if (graph.reachedEnd() || syllablesLeft - SyllableCounter.syllables(word) <1)
					if(nextPos == PartOfSpeech.ARTICLE || nextPos == PartOfSpeech.PREPOSITION ) {
						System.out.println(" Error: cannot end on a preposition or article. (BACKTRACKING)");
						return null;
					}
			
			while (graph.hasNextEdge(i) && i < graph.size() - 1) {
				
				//attempt travel to the next available edge
				System.out.println("attempting travel to edge: " + i + "    (pos: " + graph.getPOS(i) + ")");
				String temp = buildSentence(syllablesLeft - SyllableCounter.syllables(word), i);
				
				// if sentence can be completed by following this edge, commit the result.
				// if (temp == null), method is backtracking (a dead end was reached in subsequent recursion).
				if (temp != null) {
					
					if (!(graph.reachedEnd() || syllablesLeft - SyllableCounter.syllables(word) <1)) {
						if(nextPos == PartOfSpeech.ADVERB) // this call is an adverb
							if((i != 2 || i != 8) && (i != 6))	   // next call is not a prep or verb
								word = word.trim() + ", ";
						if(nextPos == PartOfSpeech.ADJECTIVE) // this call is an adjective
							if(i == 4 || i == 10)			  // next call is an adjective
								word = word.trim() + ", ";
					}
					
					return word + temp;
				}
				
				i = graph.advance(i);
			}
		}
		// if this point is reached, the method either has no more available edges or no words.
		System.out.println("\n           DEAD END -- BACKTRACKING\n");
		return null;
	}

	
	
	// =================== SETUP METHODS ========================= \\
	
	
	/**
	 * Initialize the GUI components of a Haiku generator window.
	 */
	private void setupWindow() 
	{
		System.out.print("  Creating GUI window...");
		JPanel background = new JPanel(new BorderLayout());
		
		//setup generation button
		generateButton = new JButton("Haiku");
		generateButton.setEnabled(false);
		background.add(generateButton, BorderLayout.SOUTH);
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				generate();
			}
		});
		
		//setup output field 
		output = new JTextPane();
		output.setEditable(false);
		//output.setLineWrap(true);
		output.setText("Loading dictionary...");
		background.add(output, BorderLayout.CENTER);
		
		this.getContentPane().add(background);
		
		//-- set text to center-alignment
		StyledDocument doc = output.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		
		//window setup -- general
		this.setTitle("Haiku Generator");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setSize(240,130);
		this.setVisible(true);
		
		System.out.println("done");
	}
}
