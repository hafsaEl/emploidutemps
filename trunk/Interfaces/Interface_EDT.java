package Interfaces;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import Systeme.Jours;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.*;

import java.util.*;

public class Interface_EDT {
	
	public static Calendar maintenant = Calendar.getInstance();
	public static JFrame fenetre = new JFrame();
	private static JLabel LLundi = new JLabel();
	private static JLabel LMardi = new JLabel();
	private static JLabel LMercredi = new JLabel();
	private static JLabel LJeudi = new JLabel();
	private static JLabel LVendredi = new JLabel();
	private static JButton LSemaine = new JButton();
	private static JButton SemainePrec = new JButton();
	private static JButton SemaineSuiv = new JButton();
	private static JTextPane PLundi =  new JTextPane();
	private static JTextPane PMardi =  new JTextPane();
	private static JTextPane PMercredi =  new JTextPane();
	private static JTextPane PJeudi =  new JTextPane();
	private static JTextPane PVendredi =  new JTextPane();
	
	
    private static void AddtexttoPane(String[] initString,String[] initStyles, JTextPane textPane) {

        StyledDocument doc = textPane.getStyledDocument();
        addStylesToDocument(doc);

        try {
            for (int i=0; i < initString.length; i++) {
                doc.insertString(doc.getLength(), initString[i],
                                 doc.getStyle(initStyles[i]));
            }
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }
    }
    
	protected static void addStylesToDocument(StyledDocument doc) {
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("horaire", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style s = doc.addStyle("salle", regular);
        StyleConstants.setItalic(s, true);

        s = doc.addStyle("prof", regular);
        StyleConstants.setFontSize(s, 10);

        s = doc.addStyle("cours", regular);
        StyleConstants.setBold(s, true);
	}
	/**
	 * @param args
	 */
	public static void afficher_contenu(Jours Semaine){
		LLundi.setText("     " +Semaine.getStringJour1());
		LMardi.setText("     " +Semaine.getStringJour2());
		LMercredi.setText("     " +Semaine.getStringJour3());
		LJeudi.setText("     " +Semaine.getStringJour4());
		LVendredi.setText("     " +Semaine.getStringJour5());
		LSemaine.setText(" Semaine: "+ Semaine.getStringSemaine());
		SemainePrec.setText(" Semaine " + Semaine.getStringSemaineprec());
		SemaineSuiv.setText(" Semaine " + Semaine.getStringSemaineproch());
        String[] initString ={"\n************\n","MDSI \n","Salle 110 \n","10:00 - 11:15 \n","Daniel Marre\n","************"};
        String[] initStyles ={"cours","cours","salle","horaire","prof","cours"};
        AddtexttoPane(initString,initStyles,PLundi);
        AddtexttoPane(initString,initStyles,PMardi);
        AddtexttoPane(initString,initStyles,PMercredi);
        AddtexttoPane(initString,initStyles,PJeudi);
        AddtexttoPane(initString,initStyles,PVendredi);
		
	}
	public static void main(String[] args) {
		
		fenetre.setTitle("Emploi du temps");
		fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		Jours Semaine = new Jours(maintenant);
		
		/* Labels et boutons NORD */
		JPanel headerpane = new JPanel();
		headerpane.setLayout(new BorderLayout());
		LSemaine.setBackground(new Color(255,255,255));
		headerpane.add(SemainePrec,BorderLayout.WEST);
		headerpane.add(LSemaine,BorderLayout.CENTER);
		headerpane.add(SemaineSuiv,BorderLayout.EAST);
		
		
		/* Labels jours CENTER */
		afficher_contenu(Semaine);

		JPanel JoursSemaine = new JPanel();
		JoursSemaine.setLayout(new GridLayout(1,5));
		JoursSemaine.add(LLundi);
		JoursSemaine.add(LMardi);
		JoursSemaine.add(LMercredi);
		JoursSemaine.add(LJeudi);
		JoursSemaine.add(LVendredi);
		
		/* Contenu panels jours */
		LLundi.setBorder(new LineBorder(new Color(0,0,0)));
		LMardi.setBorder(new LineBorder(new Color(0,0,0)));
		LMercredi.setBorder(new LineBorder(new Color(0,0,0)));
		LJeudi.setBorder(new LineBorder(new Color(0,0,0)));
		LVendredi.setBorder(new LineBorder(new Color(0,0,0)));
				
		
		JPanel contenu = new JPanel();
		contenu.setLayout(new GridLayout(1,5));
		System.out.println("test: "+PLundi.getText());
		PLundi.setBorder(new LineBorder(new Color(0,0,0)));
		contenu.add(PLundi);
		contenu.add(PMardi);
		contenu.add(PMercredi);
		contenu.add(PJeudi);
		contenu.add(PVendredi);
		
		fenetre.getContentPane().add(contenu,BorderLayout.SOUTH);
		
		JPanel toppanel = new JPanel();
		toppanel.setLayout(new BorderLayout());
		toppanel.add(headerpane,BorderLayout.NORTH);
		toppanel.add(JoursSemaine,BorderLayout.SOUTH);
		
		fenetre.setLayout(new BorderLayout());
		fenetre.getContentPane().add(toppanel,BorderLayout.NORTH);
		
		JMenuBar menu = new JMenuBar();
		JMenu mfichier = new JMenu("Fichier");
		JMenuItem quitter = new JMenuItem("Quitter");
		
		
		
		ActionListener fermer = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int r = JOptionPane.showConfirmDialog(null,"Veux tu vraiment quitter?","Fermeture",JOptionPane.YES_NO_OPTION);
				if (r == JOptionPane.YES_OPTION){
					System.exit(1);
				}
				
			}
		};
		quitter.addActionListener(fermer);
		
		ActionListener SemaineSuivante = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				maintenant.add(Calendar.WEEK_OF_YEAR,+1);
				Jours Semaine = new Jours(maintenant);
				afficher_contenu(Semaine);
			}
		};
		SemaineSuiv.addActionListener(SemaineSuivante);
		
		ActionListener SemainePrecedente = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				maintenant.add(Calendar.WEEK_OF_YEAR,-1);
				Jours Semaine = new Jours(maintenant);
				afficher_contenu(Semaine);
				
			}
		};
		SemainePrec.addActionListener(SemainePrecedente);
		
		mfichier.add(quitter);
		menu.add(mfichier);
		
		
		fenetre.setSize(800,600);
		
		fenetre.setVisible(true);
		
		
	}
	
}

