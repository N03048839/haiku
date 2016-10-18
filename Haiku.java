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
public class Haiku extends JFrame implements ActionListener 
{	
	private static final long serialVersionUID = 1L;
	
     // =========================== INTERNAL COMPONENTS =========================== \\

		//stores desired sentence structure
		private SentenceGraph graph;
	
		//stores information about loaded words
		private Dictionary dictionary;
	

		// GUI components
		private JButton generateButton;
		private JTextPane output;

	
	
     // =========================== CONSTRUCTOR AND MAIN =========================== \\
	
	public Haiku() 
	{		
		setupDictionary();
		setupWindow();
		System.out.println("   SETUP COMPLETE");
	}
	
	
	public static void main(String[] args) {
		new Haiku();
	}
	
	
     // ============================ PRIMARY METHODS ================================ \\
     
	/**
	 * The backbone of the program.
	 * @return a complete haiku.
	 */
	public String generate() 
	{
		System.out.print("   Generating a haiku...");		
		graph = new SentenceGraph();
		
		String[] outString = new String[3];
		
		do {
			outString[0] = buildSentence(5, graph.getIndex());
			
			if(graph.reachedEnd())
				graph.reset();
			
			outString[1] = buildSentence(7, graph.getIndex());
			
			if(graph.reachedEnd())
				graph.reset();
			
			outString[2] = buildSentence(5, graph.getIndex());
		} 
		while (containsNull(outString));
			
		System.out.println("\n==== Cleaning up output ===\n\n-- capitalizing first letter --");
		//capitalize first letter
		outString[0] = outString[0].substring(0, 1).toUpperCase() + outString[0].substring(1);
		
		//adjust 'a' to 'an' where applicable
		System.out.println("\n-- checking for article agreement --");
		for (int i = 0; i < outString.length; i++) 
		{
			String[] st = outString[i].split("\\s");
			System.out.print("tokenized string:  ");
			for (int k = 0; k < st.length; k++)
				System.out.print(" + " + st[k]);
			System.out.println();
			
			for (int j = 0; j < st.length-1; j++) {
				if (st[j].matches("[Aa]")) {
					System.out.println("\'a\' found");
					if (st[j+1].matches("[AaEeIiOoUu].*")) {
						System.out.println("rectifying output");	
						st[j] += "n";
					}
				}
			}
		}
		
		System.out.println("done");
		
		//-- consolidate strings for output
		String haiku = "";
		for (int i = 0; i < outString.length; i++)
			haiku += " " + outString[i] + "\n";
		
		return haiku;
	}
	 
	
	private boolean containsNull(String[] array)
	{		
		for (int i = 0; i < array.length; i++)
			if (array[i] == null)
				return true;
		
		return false;
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
		
		//BASE CASE: the current line contains exactly (target) syllables
		if (syllablesLeft <= 0)
			return "";
		
		//BASE CASE: end of sentence is reached
		if (startIndex >= graph.size() - 2 && syllablesLeft <= 0)
			return "";
		
		
		//Pick a word (in this call) to add. If the dictionary runs out, or if 0 syllables are specified,
		// this will return null.
		PartOfSpeech nextPos = graph.getNode(startIndex);
		String word = nextWord(nextPos, syllablesLeft);
		
		// if (word == null), no words can be found that meet the criteria.
		if(word != null) {
			
			// Iterate through the edges accessible from this position
			int i = graph.nextEdge(startIndex);
			
			//this stops the sentence from ending on a preposition or article
			if(graph.reachedEnd() || syllablesLeft - Dictionary.sylCount(word) <1)
					if(nextPos == PartOfSpeech.ARTICLE || nextPos == PartOfSpeech.PREPOSITION ) {
						System.out.println(" Error: cannot end on a preposition or article. (BACKTRACKING)");
						return null;
					}
			
			while (graph.hasNextEdge(i) && i < graph.size() - 1) {
				
				//attempt travel to the next available edge
				System.out.println("attempting travel to edge: " + i + "    (pos: " + graph.getNode(i) + ")");
				String temp = buildSentence(syllablesLeft - Dictionary.sylCount(word), i);
				
				// if sentence can be completed by following this edge, commit the result.
				// if (temp == null), method is backtracking (a dead end was reached in subsequent recursion).
				if (temp != null) {
					
					if (!(graph.reachedEnd() || syllablesLeft - Dictionary.sylCount(word) <1)) {
						if(nextPos == PartOfSpeech.ADVERB) // this call is an adverb
							if((i != 2 || i != 8) && (i != 6))	   // next call is not a prep or verb
								word = word.trim() + ", ";
						if(nextPos == PartOfSpeech.ADJECTIVE) // this call is an adjective
							if(i == 4 || i == 10)			  // next call is an adjective
								word = word.trim() + ", ";
					}
					
					return word + temp;
				}
				
				i = graph.nextEdge(i);
			}
		}
		// if this point is reached, the method either has no more available edges or no words.
		System.out.println("\n           DEAD END -- BACKTRACKING\n");
		return null;
	}

	
	
	/**
	 * Pick a random word from the dictionary that fits the given criteria.
	 * @param pos the desired part of speech
	 * @param sMax the MAXIMUM number of syllables that the word can have
	 */
	private String nextWord(PartOfSpeech pos, int sMax) {
		
		System.out.println(" Searching for a " + pos + " with <" + sMax + " syllables...");
		
		if (pos == PartOfSpeech.BLANK)
			return "";  // Advances sentence without using syllables or triggering backtracking
		if (sMax <= 0)
			return null;
		
		// Create a set of all words that meet desired criteria
		Set<String> words = dictionary.wordSet(pos, 1, sMax);
		if(words.size() == 0)
			return null;
		
		// Choose one word from this set at random
		int target = new Random().nextInt(words.size());
		
		int i = 0;
		for(String s : words) {
			if (i == target)
				return s;
		 	i++;
		}
		return null;
	}
	
	
	
	// =================== SETUP METHODS ========================= \\
	
	/**
	 * Initialize the supporting data structure for a Haiku generator.
	 */
	private void setupDictionary() 
	{
		final String dictFileName = "dictionary.txt";
		
		
		try {
			dictionary = new Dictionary(dictFileName);	
		} 
		catch (IOException exception) {
			
			exception.printStackTrace();
			JOptionPane.showMessageDialog(this, exception.getMessage(), this.getTitle(), 
					JOptionPane.ERROR_MESSAGE);
			
			System.exit(1);
		}
	}
	
	
	/**
	 * Initialize the GUI components of a Haiku generator window.
	 */
	private void setupWindow() 
	{
		System.out.print("  Creating GUI window...");
		JPanel background = new JPanel(new BorderLayout());
		
		//setup generation button
		generateButton = new JButton("Haiku");
		generateButton.addActionListener(this);
		background.add(generateButton, BorderLayout.SOUTH);
		
		//setup output field 
		output = new JTextPane();
		output.setEditable(false);
		//output.setLineWrap(true);
		output.setText("   A click below this, \n"
					+ "   and a haiku will appear. \n"
					+ "   Why don't you try it?");
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
	
	
	/**
	 * Catch an ActionEvent -- used for identification of button clicks
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == generateButton)
			output.setText(generate());
	}
}