/**
 * 
 */
package Systeme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Time;
import java.util.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.filter.*;

import bdd.*;

/**
 * @author Tonya Vo Thanh et Alexander Remen
 *
 */
public class Gestion_BDD {

	//Definition des constantes
	private static final String ficXml ="XML/bdedt2.xml";
	private static final String ficXml2 ="XML/bdedt2.xml";
	private static final String nomDtd = "bdedt";
	private static final String ficSauvegarde = "tmp/system";
	
	//Definition des attributs
	private File fichier;
	private org.jdom.Document document;
	private Element racine;
	private Vector<Personne> utilisateurs;
	private Vector<Promotion> promotions;
	private Vector<Groupe> groupes;
	private Vector<Matiere> matieres;
	private Vector<Salle> salles;
	private Vector<Cours> cours; 
	
	//Création d'un singleton
	private static Gestion_BDD instance;
	
	private Gestion_BDD(Boolean chargeXml)
	{
		//Création du fichier se sauvegarde si il n existe pas
		fichier = new File(ficSauvegarde);
		if(!fichier.exists())
		{
			try {
				fichier.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		//Initialisation des vecteurs
		utilisateurs = new Vector<Personne>();
		promotions = new Vector<Promotion>();
		groupes = new Vector<Groupe>();
		salles = new Vector <Salle>();
		cours = new Vector<Cours>();
		matieres = new Vector<Matiere>();
		
		//SAXBuilder sxb = new SAXBuilder(true);
		SAXBuilder sxb = new SAXBuilder();
		sxb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		
		if(chargeXml)
		{
			try {
				document = sxb.build(new File(ficXml));
				System.out.println("Création du jdom");
				
				//On initialise un nouvel élément racine avec 
				//l'élément racine du document .
				racine = document.getRootElement();
				
				//affiche();
								
				System.out.println("debut du chargement");
				//Chargement du fichier xml
				chargement();			
			 }
		    // indicates a well-formedness or validity error
		    catch (JDOMException e) { 
		      System.out.println(ficXml + " is not valid.");
		      System.out.println(e.getMessage());
		    }  
		    catch (IOException e) { 
		      System.out.println("Could not check " + ficXml);
		      System.out.println(" because " + e.getMessage());
		    } 
		    catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			try {
				chargeBDD();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static Gestion_BDD getInstance()
	{	
		if(instance==null)
		{
			instance = new Gestion_BDD(true);
		}
		return instance;
	}
	
	public static Gestion_BDD getInstance(boolean ok)
	{	
		if(instance==null)
		{
			instance = new Gestion_BDD(ok);
		}
		return instance;
	}
	
//==============================================================
//  Fonctions de chargement a partir d'un fichier xml
//==============================================================
	
	/**
	 * Fonction qui charge le document xml dans la bdd
	 */
	
	private void chargement() throws Exception
	{
		chargePromotion(racine.getChild("étudiants").getChildren());
		chargeResp(racine.getChild("inspecteurs").getChildren());
		chargeEns(racine.getChild("enseignants").getChildren());
		chargeGroupes(racine.getChild("groupes").getChildren());
		chargeMatieres(racine.getChild("matières").getChildren());
		chargeSalles(racine.getChild("salles").getChildren());
		chargeEdt(racine.getChild("edt").getChildren());		
	}
	
	/**
	 * Fonction qui charge les responsables
	 * @param listResponsables
	 * @throws Exception
	 */
	private void chargeResp(List listResponsables) throws Exception
	{
		Iterator i = listResponsables.iterator();
		
		while(i.hasNext())
		{
			Element courant = (Element)i.next();
			Promotion promo = getPromotion(courant.getAttributeValue("promotion"));			
			utilisateurs.add(new Responsable(courant.getAttributeValue("id"),courant.getChildText("login"), courant.getChildText("pass"), courant.getChildText("nom"), courant.getChildText("prénom"),"", courant.getChildText("mél"),0, promo));			
		}
	}
	
	/**
	 * Fonction qui charge les enseignants
	 * @param listEnseignants
	 */
	private void chargeEns(List listEnseignants)
	{
		Iterator i = listEnseignants.iterator();
		
		while(i.hasNext())
		{
			Element courant = (Element)i.next();
			
			utilisateurs.add(new Enseignant(courant.getAttributeValue("id"),courant.getChildText("login"), courant.getChildText("pass"), courant.getChildText("nom"), courant.getChildText("prénom"),"", courant.getChildText("mél"),0));			
		}
	}
	
	/**
	 * Fonction qui charge les promotions et leurs etudiants
	 * @param listPromotion
	 */
	private void chargePromotion(List listPromotion)
	{
		Iterator i = listPromotion.iterator();
		
		while(i.hasNext())
		{
			Element courant = (Element)i.next();
			Vector<Etudiant> etudiants = ChargeEtudiants(courant.getChildren("étudiant"));
			promotions.add(new Promotion(courant.getAttributeValue("id"), etudiants));		
		}
	}
	
	/**
	 * Fonction qui charge des etudiants
	 * @param listEtudiants
	 * @return un vector des etudiants cree
	 */
	private Vector<Etudiant> ChargeEtudiants(List listEtudiants)
	{
		Vector<Etudiant> v = new Vector<Etudiant>();
		Iterator i = listEtudiants.iterator();
		
		while(i.hasNext())
		{
			Element courant = (Element)i.next();
			Etudiant et = new Etudiant(courant.getAttributeValue("id"),courant.getChildText("login"), courant.getChildText("pass"), courant.getChildText("nom"), courant.getChildText("prénom"),"", courant.getChildText("mél"),0);
			v.add(et);
			utilisateurs.add(et);
		}
		return v;	
	}
	
	/**
	 * Fonction qui charge les groupes 
	 * @param listGroupes
	 * @throws Exception
	 */
	private void chargeGroupes(List listGroupes) throws Exception
	{
		Iterator i = listGroupes.iterator();
				
		while(i.hasNext())
		{
			boolean ok = false;
			
			Element courant = (Element)i.next();
			
			//Création du groupe et ajout à la liste de groupes
			Groupe gp = new Groupe(courant.getAttributeValue("id"));
			groupes.add(gp);
			
			//Ajout du groupe aux étudiants
			String [] etudiants = (courant.getAttributeValue("étudiants")).split(" ");
			 
			for(int j=0; j<etudiants.length; j++)
			{
				Etudiant etud = getEtudiant(etudiants[j]);
				
				etud.ajouteGroupe(gp);
				
				//On initialise une fois le responsable du groupe
				if(!ok)
				{
					Responsable resp=etud.getPromo().getResp();
					if (resp==null) throw new Exception("responsable inexistant");
					gp.setResponsable(resp);
					ok=true;
				}
			}
			
		}
	}
	/**
	 * Fonction qui charge les matieres
	 * @param listMatieres
	 * @throws Exception
	 */
	private void chargeMatieres(List listMatieres) throws Exception
	{
		Iterator i = listMatieres.iterator();
		
		while(i.hasNext())
		{
			Element courant = (Element)i.next();
		
			//Détermination du type
			int type = Matiere.optionnelle;
			if (courant.getAttributeValue("type")=="obligatoire")
			{
				type = Matiere.obligatoire;
			}
			
			//==Création de la matiere==//
			Matiere mat = new Matiere(courant.getAttributeValue("id"),courant.getChildText("intitulé"),type);
			
			//==Création et ajout des enseignements==//
			Iterator j = courant.getChild("enseignements").getChildren().iterator();
			while(j.hasNext())
			{
				Element courant2 = (Element)j.next();
				
				//Détermination du type d'enseignement
				int type_enseignement;
				if((type_enseignement=Enseignement.getTypeEnseignement(courant2.getName()))==-1)
				{
					throw new Exception("Erreur chargement Enseignement : Type inexistant");
				}
				
				//Détermination du volume horaire
				Time volume_horaire;
				try
				{
					volume_horaire= Time.valueOf(courant2.getAttributeValue("volume")+":00");
				}
				catch (Exception e)
				{
					throw new Exception("Erreur chargement Enseignement : format volume horaire incorect");
				}
				
				Enseignement enseign = new Enseignement(type_enseignement, volume_horaire);
				
				//Ajout des groupes et des enseignants à l'enseignement
				String enseignants [] = courant2.getAttributeValue("enseignants").split(" ");				
				String groupes [] = courant2.getAttributeValue("groupes").split(" ");
				if (groupes.length!=enseignants.length)
				{
					throw new Exception("Erreur chargement Matieres : nbGroupe différent de nbEnseignants");
				}
				
				for(int k=0; k<groupes.length; k++)
    			{
					Groupe gp = getGroupe(groupes[k]);
					Enseignant ens = getEnseignant(enseignants[k]);
    				enseign.ajoutGroupeEnseignant(gp, ens);
    			}
				
				//Ajout de l'enseignement à la matiere
				mat.ajoutEnseignement(enseign);
			}
			matieres.add(mat);
		}
	}
	/**
	 * Fonction qui charge les salles
	 * @param listSalles
	 * @throws Exception
	 */
	private void chargeSalles(List listSalles) throws Exception
	{
		Iterator i = listSalles.iterator();
		
		while(i.hasNext())
		{
			Element courant = (Element)i.next();
			int type_salle;
			if((type_salle = Salle.getTypeSalle(courant.getAttributeValue("type")))==-1)
			{
				throw new Exception("chargeSalle : type de salle inexistant");
			}
			
			salles.add(new Salle(courant.getAttributeValue("id"),type_salle, Integer.parseInt(courant.getAttributeValue("taille"))));		
		}
	}
	/**
	 * Fonction qui charge les cours
	 * @param listEdt
	 * @throws Exception
	 */
	private void chargeEdt(List listEdt) throws Exception
	{
		Iterator i = listEdt.iterator();
				
		while(i.hasNext())
		{
			Element courant = (Element)i.next();
			Creneau c = new Creneau(courant.getAttributeValue("date"), courant.getAttributeValue("heure"), courant.getAttributeValue("durée"));
			Salle s = getSalle(courant.getAttributeValue("salle"));
			
			Groupe gp=null;
			String groupe = courant.getAttributeValue("groupe");
			if(groupe.compareTo("")!=0)gp = getGroupe(groupe);
			Matiere mat=null;
			String matiere = courant.getAttributeValue("matière");
			if(groupe.compareTo("")!=0) mat = getMatiere(matiere);

			addCours(new Cours(c,s,gp,mat));		
		}
	}

//==============================================================
//  Fonctions de sauvegarde dans un fichier xml
//==============================================================
	public void sauvegarde()
	{
		racine = new Element("bdedt");
		document = new Document(racine);
		
		//Ajout des responsables et des enseignants
		Element inspecteurs = new Element("inspecteurs");
		racine.addContent(inspecteurs);
		Element enseignants = new Element("enseignants");
		racine.addContent(enseignants);
		sauvegardeUtilisateurs(inspecteurs, enseignants);
		
		//Ajout des promotions
		Element etudiants = new Element("étudiants");
		racine.addContent(etudiants);
		sauvegardePromotion(etudiants);
		
		//Ajout des groupes
		Element gpes = new Element("groupes");
		racine.addContent(gpes);
		sauvegardeGroupe(gpes);
		
		//Ajout des matieres
		Element mats = new Element("matières");
		racine.addContent(mats);
		sauvegardeMatiere(mats);
		
		//Ajouts des salles
		Element sals = new Element("salles");
		racine.addContent(sals);
		sauvegardeSalles(sals);
		
		//Ajout des reservations
		Element edt= new Element("edt");
		racine.addContent(edt);
		sauvegardeCours(edt, cours);
		
		document.setDocType(new DocType(nomDtd,nomDtd+".dtd"));
		//afficheXML(document);
		
		try
		   {
		      //On utilise ici un affichage classique avec getPrettyFormat()
		      XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
		      sortie.output(document, new FileOutputStream(ficXml2));
		   }
		   catch (java.io.IOException e){}
	}
	
	public static void sauvegardeCours(Element edt, Vector<Cours> listeCours) {
		Iterator i = listeCours.iterator();
		
		while(i.hasNext())
		{
			Cours c = (Cours)i.next();
			Element reservation = new Element("réservation");
			
			reservation.setAttribute("date", c.getCreneau().date());
			reservation.setAttribute("heure", c.getCreneau().heure());
			reservation.setAttribute("durée", c.getCreneau().duree());
			reservation.setAttribute("salle", c.getSalle().getNom_salle());
			String gp ="";
			if(c.getGroupe()!=null)gp= c.getGroupe().getnum_groupe();
			reservation.setAttribute("groupe", gp);
			String mat ="";
			if(c.getMatiere()!=null)mat= c.getMatiere().getNum_matiere();
			reservation.setAttribute("matière", mat);
			
			edt.addContent(reservation);
		}
	}

	private void sauvegardeSalles(Element sals) {
		Iterator i = salles.iterator();
		while(i.hasNext())
		{
			Salle s = (Salle) i.next();
			Element sal = new Element("salle");
			sal.setAttribute("id", s.getNom_salle());
			sal.setAttribute("taille", ""+s.getTaille());
			
			switch(s.getType_Salle())
			{
				case Salle.AMPHI : sal.setAttribute("type", "amphi"); break;
				case Salle.COURS : sal.setAttribute("type", "cours"); break;
				case Salle.TP : sal.setAttribute("type", "tp"); break;
			}
			
			sals.addContent(sal);
		}
	}

	private void sauvegardeMatiere(Element mats) {
		Iterator i = matieres.iterator();
		
		while(i.hasNext())
		{
			Matiere mat =(Matiere)i.next();
			Element matier = new Element("matière");
			
			matier.setAttribute("id",mat.getNum_matiere());
			switch(mat.getType())
			{
				case Matiere.obligatoire: matier.setAttribute("type", "obligatoire");break;
				case Matiere.optionnelle: matier.setAttribute("type", "facultatif");break;
			}
			
			Element intitule = new Element("intitulé");
			intitule.setText(mat.getIntitule());
			matier.addContent(intitule);
			
			Element enseignements = new Element("enseignements");
			matier.addContent(enseignements);
			
			Iterator j = mat.getListe_enseignement().iterator();
			while(j.hasNext())
			{
				Enseignement ens = (Enseignement)j.next();
				String typeEns="";
				switch(ens.getType_enseignement())
				{
					case Enseignement.BE:typeEns ="BE";break;
					case Enseignement.CM:typeEns ="CM";break;
					case Enseignement.TD:typeEns ="TD";break;
					case Enseignement.TP:typeEns ="TP";break;
				}
				
				Element enseignement = new Element(typeEns);
				enseignement.setAttribute("enseignants", ens.recupEnseignants());
				enseignement.setAttribute("groupes", ens.recupGroupes());
				enseignement.setAttribute("volume", ens.recupvolume());
				
				//Ajout de l'enseignement a la matiere
				enseignements.addContent(enseignement);
			}
			
			//Ajout de la matiere
			mats.addContent(matier);
		}	
	}

	private void sauvegardeGroupe(Element gpes) {
		Iterator i = groupes.iterator();
		
		while(i.hasNext())
		{
			Groupe gp =(Groupe)i.next();
			Element gpe = new Element("groupe");
			gpe.setAttribute("id", gp.getnum_groupe());
			gpe.setAttribute("étudiants",recupEtudiant(gp));
			
			gpes.addContent(gpe);
		}
	
	}

	private String recupEtudiant(Groupe gp) {
		String etudiants="";
		
		for (int i=0; i<utilisateurs.size(); i++)
		{
			if(utilisateurs.elementAt(i).getClass()==Etudiant.class)
			{
				Etudiant etud = (Etudiant) utilisateurs.elementAt(i);
				if (etud.estDuGroupe(gp))
				{
					etudiants += etud.getNum_personne()+" ";
				}
			}
		}
		return etudiants.substring(0, etudiants.length()-1);
	}

	private void sauvegardeUtilisateurs(Element inspecteurs, Element enseignants) {
		for (int i=0; i<utilisateurs.size(); i++)
		{
			if(utilisateurs.elementAt(i).getClass()==Responsable.class)
			{
				//Création du responsable
				Responsable resp = (Responsable)utilisateurs.elementAt(i);
				Element inspecteur = new Element("inspecteur");
				Attribute id = new Attribute("id",resp.getNum_personne());
				Attribute promotion = new Attribute("promotion",resp.getPromo().getNom_promotion());
				
				inspecteur.setAttribute(id);
				inspecteur.setAttribute(promotion);
				
				Element nom = new Element("nom");
				nom.setText(resp.getNom());
				inspecteur.addContent(nom);
				Element prenom = new Element("prénom");
				prenom.setText(resp.getPrenom());
				inspecteur.addContent(prenom);
				Element mel = new Element("mél");
				mel.setText(resp.getEmail());
				inspecteur.addContent(mel);
				Element login = new Element("login");
				login.setText(resp.getUsername());
				inspecteur.addContent(login);
				Element pass = new Element("pass");
				pass.setText(resp.getPassword());
				inspecteur.addContent(pass);
				
				//Ajout du responsable
				inspecteurs.addContent(inspecteur);
			}
			else if (utilisateurs.elementAt(i).getClass()==Enseignant.class)
			{
				//création de l enseignant
				Enseignant ens = (Enseignant)utilisateurs.elementAt(i);
				Element enseignant = new Element("enseignant");
				Attribute id = new Attribute("id",ens.getNum_personne());
				
				enseignant.setAttribute(id);
				
				Element nom = new Element("nom");
				nom.setText(ens.getNom());
				enseignant.addContent(nom);
				Element prenom = new Element("prénom");
				prenom.setText(ens.getPrenom());
				enseignant.addContent(prenom);
				Element mel = new Element("mél");
				mel.setText(ens.getEmail());
				enseignant.addContent(mel);
				Element login = new Element("login");
				login.setText(ens.getUsername());
				enseignant.addContent(login);
				Element pass = new Element("pass");
				pass.setText(ens.getPassword());
				enseignant.addContent(pass);
				
				//Ajout de l'enseignant
				enseignants.addContent(enseignant);
			}
		}
		
		
	}

	private void sauvegardePromotion(Element etudiants) {
		Iterator i = promotions.iterator();
		
		while(i.hasNext())
		{
			Promotion promo = (Promotion)i.next();
			Element promotion = new Element("promotion");
			promotion.setAttribute("id", promo.getNom_promotion());
			
			Iterator j = promo.getListe_etudiant().iterator();
			while(j.hasNext())
			{
				//Creation de l etudiant
				Etudiant etud = (Etudiant) j.next();
				Element etudiant = new Element("étudiant");
				Attribute id = new Attribute("id",etud.getNum_personne());
				
				etudiant.setAttribute(id);
				
				Element nom = new Element("nom");
				nom.setText(etud.getNom());
				etudiant.addContent(nom);
				Element prenom = new Element("prénom");
				prenom.setText(etud.getPrenom());
				etudiant.addContent(prenom);
				//Element mel = new Element("mél");
				//mel.setText(etud.getEmail());
				//etudiant.addContent(mel);
				Element login = new Element("login");
				login.setText(etud.getUsername());
				etudiant.addContent(login);
				Element pass = new Element("pass");
				pass.setText(etud.getPassword());
				etudiant.addContent(pass);
				
				//ajout de l etudiant
				promotion.addContent(etudiant);
			}
			etudiants.addContent(promotion);
		}
		
	}

	
//==============================================================
//  Accesseurs
//==============================================================
	
	/**
	 * Fonction qui retourne une promotion à partir de son nom
	 * @param name - nom de la promotion
	 * @return la promotion correspondant
	 * @throws Exception si la promotion n'existe pas
	 */
	public Promotion getPromotion(String name) throws Exception
	{
		Promotion promo= null;
		boolean trouve = false;
		
		Iterator i = promotions.iterator();
		while( i.hasNext() && (!trouve))
		{
			promo = (Promotion)i.next();
			if(promo.getNom_promotion().compareTo(name)==0)
			{
				trouve = true;
			}
		}
		if(!trouve) throw new Exception("Promotion inexistante");
		return promo;
	}
	

	/**
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	private Groupe getGroupe(String name) throws Exception
	{
		Groupe gp= null;
		boolean trouve = false;
		
		Iterator i = groupes.iterator();
		while( i.hasNext() && (!trouve))
		{
			gp = (Groupe)i.next();
			if(gp.getnum_groupe().compareTo(name)==0)
			{
				trouve = true;
			}
		}
		if(!trouve) throw new Exception("Groupe inexistant");
		return gp;
	}
	
	private Etudiant getEtudiant(String id) throws Exception
	{
		Etudiant etudiant = null;
		boolean trouve = false;
		int i = 0;
		
		while (i<utilisateurs.size() && (!trouve))
		{
			if(utilisateurs.elementAt(i).getClass()==Etudiant.class)
			{
				etudiant = (Etudiant) utilisateurs.elementAt(i);
				if(etudiant.getNum_personne().compareTo(id)==0)
				{
					trouve = true;
				}
			}
			i++;
		}
		if(!trouve) throw new Exception("Etudiant inexistant");
		return etudiant;
	}
	private Enseignant getEnseignant(String id) throws Exception
	{
		Enseignant enseignant = null;
		boolean trouve = false;
		int i = 0;
		
		while (i<utilisateurs.size() && (!trouve))
		{
			if(utilisateurs.elementAt(i).getClass()==Enseignant.class)
			{
				enseignant = (Enseignant) utilisateurs.elementAt(i);
				if(enseignant.getNum_personne().compareTo(id)==0)
				{
					trouve = true;
				}
			}
			i++;
		}
		if(!trouve) throw new Exception("Enseignant inexistant");
		return enseignant;
	}

	private Salle getSalle(String name) throws Exception
	{
		Salle s= null;
		boolean trouve = false;
		
		Iterator i = salles.iterator();
		while( i.hasNext() && (!trouve))
		{
			s = (Salle)i.next();
			if(s.getNom_salle().compareTo(name)==0)
			{
				trouve = true;
			}
		}
		if(!trouve) throw new Exception("Salle inexistante");
		return s;
	}

	private Matiere getMatiere(String name) throws Exception
	{
		Matiere mat= null;
		boolean trouve = false;
		
		Iterator i = matieres.iterator();
		while( i.hasNext() && (!trouve))
		{
			mat = (Matiere)i.next();
			if(mat.getNum_matiere().compareTo(name)==0)
			{
				trouve = true;
			}
		}
		if(!trouve) throw new Exception("Matiere inexistante");
		return mat;
	}
	
	/**
	 * @return Retourne les utilisateurs.
	 */
	public Vector<Personne> getUtilisateurs() {
		return utilisateurs;
	}

	public Vector<Cours> getCoursGroupes(Vector<Groupe> liste_groupe)
	{
		int j;
		boolean ok;
		Vector<Cours> liste_cours = new Vector<Cours>();
		
		Iterator i = cours.iterator();
		while(i.hasNext())
		{
			ok = false;
			Cours c = (Cours)i.next();
			if (liste_groupe == null){
				if(c.getGroupe()==null && c.getEnseignant() ==null && c.getMatiere() == null)
					ok=true;
			}
			else
			{
				j=0;
				while(j<liste_groupe.size() && !ok)
				{
					if(c.getGroupe().egal(liste_groupe.elementAt(j)))
					{
						ok=true;
					}
					j++;
				}
			}
			if(ok) liste_cours.add(c);
		}
		
		return liste_cours;
	}
	
	public Vector<Cours> getCoursPromotion(Responsable resp)
	{
		Vector<Cours> liste_cours = new Vector<Cours>();
		
		Iterator i = cours.iterator();
		while(i.hasNext())
		{
			Cours c = (Cours)i.next();
			if(c.getGroupe()!=null)
			{
				if(c.getGroupe().getResponsable().egal(resp))
				{
					liste_cours.add(c);
				}
			}
		}
		
		return liste_cours;
	}

	public Vector<Groupe> getGroupesResp(Responsable resp)
	{
		Vector<Groupe> liste_groupe = new Vector<Groupe>();
		
		Iterator i = groupes.iterator();
		while(i.hasNext())
		{
			Groupe g = (Groupe)i.next();
			if(g.getResponsable().egal(resp))
			{
					liste_groupe.add(g);
			}
		}
		
		return liste_groupe;
	}
	
	public Vector<Cours> getCoursSalle(Salle s)
	{
		Vector<Cours> liste_cours = new Vector<Cours>();
		
		Iterator i = cours.iterator();
		while(i.hasNext())
		{
			Cours c = (Cours)i.next();
			if(c.getSalle().egal(s))
			{
				liste_cours.add(c);
			}
		}
		
		return liste_cours;
	}
	
	/**
	 * Fonction qui ajoute un cours à la liste des cours
	 * dans l'ordre croissant
	 * @param c
	 * @throws Exception 
	 */
	public boolean addCours(Cours c) throws Exception
	{
		boolean ok = false;
		int pos = 0;
		
		//if(cours.size()!=0)
		//{
			pos = cherchePosition2(c, 0, cours.size());	
		//}
		cours.add(pos,c);
		//cours.add(c);
		
		return ok;
	}

	private int cherchePosition2(Cours c, int deb, int nbElts) throws Exception
	{
		int pos=0;
		//System.out.println("*********************");
		//System.out.println("deb: "+deb +" et nbElts: "+nbElts);
		//System.out.println("date du cours a inserer "+c.getCreneau().date()+" "+c.getCreneau().heure());
		
		if(nbElts<=0)
		{
			pos = deb;
		}
		else if(nbElts==1)
		{
			//System.out.println("date du cours au debut "+cours.elementAt(deb).getCreneau().date()+" "+cours.elementAt(deb).getCreneau().heure());
			
			switch(cours.elementAt(deb).getCreneau().compare(c.getCreneau()))
			{
				case Creneau.AVANT : pos=deb+1;break;
				case Creneau.APRES : pos=deb;break;
				case Creneau.ERREUR : 
				{
					boolean ok= true;
					
					ok = ( ok && c.getGroupe()!=null && !c.getGroupe().egal(cours.elementAt(deb).getGroupe()));
					ok = (ok && c.getEnseignant()!=null && !c.getEnseignant().egal(cours.elementAt(deb).getEnseignant()));
					if(!ok || c.getSalle().egal(cours.elementAt(deb).getSalle()))
					{
						throw new Exception("Probleme de créneau");
					}
					pos= deb+1;
				}
			}
		}
		else
		{
			pos = deb+(nbElts/2);

			//System.out.println("deb("+deb+") "+cours.elementAt(deb).getCreneau().date()+" "+cours.elementAt(deb).getCreneau().heure());
			//System.out.println("c "+c.getCreneau().date()+" "+c.getCreneau().heure());
			//System.out.println("pos ("+pos+")" +cours.elementAt(pos).getCreneau().date()+" "+cours.elementAt(pos).getCreneau().heure());
			//System.out.println("fin("+fin+")" +cours.elementAt(fin).getCreneau().date()+" "+cours.elementAt(fin).getCreneau().heure());
			switch(cours.elementAt(pos).getCreneau().compare(c.getCreneau()))
			{
				case Creneau.AVANT : pos=cherchePosition2(c,pos+1, nbElts-(pos-deb+1));break;
				case Creneau.APRES : pos = cherchePosition2(c,deb,pos-deb);break;
				case Creneau.ERREUR : 
				{
					boolean ok= true;
					
					ok = ( ok && c.getGroupe()!=null && !c.getGroupe().egal(cours.elementAt(pos).getGroupe()));
					ok = (ok && c.getEnseignant()!=null && !c.getEnseignant().egal(cours.elementAt(pos).getEnseignant()));
					if(!ok || c.getSalle().egal(cours.elementAt(pos).getSalle()))
					{
						throw new Exception("Probleme de créneau");
					}
					pos=pos+1;
				}
			}			
				
		}
		//System.out.println("Position finale : "+pos);
		return pos;
	}
	
	/**
	 * Fonction qui indique la position ou inserer le cours
	 * @param c
	 * @param deb
	 * @param fin
	 * @return la position
	 * @throws Exception 
	 */
	private int cherchePosition(Cours c, int deb, int fin) throws Exception
	{
		fin--;
		int pos=0;
		System.out.println("deb: "+deb +" et fin: "+fin);
		if(fin==deb)
		{
			System.out.println("date du cours a inserer "+c.getCreneau().date()+" "+c.getCreneau().heure());
			System.out.println("date du cours au debut "+cours.elementAt(deb).getCreneau().date()+" "+cours.elementAt(deb).getCreneau().heure());
			switch(c.getCreneau().compare(cours.elementAt(deb).getCreneau()))
			{
				case Creneau.AVANT : pos=deb;break;
				case Creneau.APRES : 
				{
					pos=deb+1;
						
					break;
				}
				case Creneau.ERREUR : 
				{
					if(c.getGroupe()==cours.elementAt(deb).getGroupe() || c.getSalle()==cours.elementAt(deb).getSalle())
					{
						throw new Exception("Probleme de créneau");
					}
					pos= deb+1;
				}
			}
		}
		else
		{
			pos = (fin+deb)/2;

			System.out.println("deb("+deb+") "+cours.elementAt(deb).getCreneau().date()+" "+cours.elementAt(deb).getCreneau().heure());
			System.out.println("c "+c.getCreneau().date()+" "+c.getCreneau().heure());
			System.out.println("pos ("+pos+")" +cours.elementAt(pos).getCreneau().date()+" "+cours.elementAt(pos).getCreneau().heure());
			System.out.println("fin("+fin+")" +cours.elementAt(fin).getCreneau().date()+" "+cours.elementAt(fin).getCreneau().heure());
			switch(c.getCreneau().compare(cours.elementAt(pos).getCreneau()))
			{
				case Creneau.AVANT : pos=cherchePosition(c,deb, pos);break;
				case Creneau.ERREUR : 
				{
					if(c.getGroupe()==cours.elementAt(deb).getGroupe() || c.getSalle()==cours.elementAt(deb).getSalle())
					{
						throw new Exception("Probleme de créneau");
					}
					pos=pos+1;
				}
				case Creneau.APRES : 
				{
					if((pos+1)<(fin))
					{
						pos=cherchePosition(c,pos+1, fin+1);
					}
					else pos++;
					break;
					
				}
				default:break;
			}			
				
		}
		return pos;
	}
	
	public Vector<Personne> getRespEns() {
		Vector<Personne> liste_personne = getResp();
		
		liste_personne.addAll(getEns());
		
		return liste_personne;
	}
	
	public Vector<Personne> getEns() {
		Vector<Personne> liste_personne = new Vector<Personne>();
		
		for(int i =0; i<utilisateurs.size(); i++)
		{
			if(utilisateurs.elementAt(i).getClass()!= Etudiant.class  && utilisateurs.elementAt(i).getClass()!= Responsable.class)
			{
				liste_personne.add(utilisateurs.elementAt(i));
			}
		}
		return liste_personne;
	}
	
	public Vector<Personne> getResp() {
		Vector<Personne> liste_personne = new Vector<Personne>();
		
		for(int i =0; i<utilisateurs.size(); i++)
		{
			if(utilisateurs.elementAt(i).getClass()!= Etudiant.class  && utilisateurs.elementAt(i).getClass()!= Enseignant.class)
			{
				liste_personne.add(utilisateurs.elementAt(i));
			}
		}
		return liste_personne;
	}
	
//==============================================================
//  Fonctions de chargement et de sauvegarde de la base 
//	dans un fichier (serialization)
//==============================================================
	/**
	 * Sauvegarde la base de donnees sur un fichier de sauvegarde
	 * @throws IOException
	 */
	public void sauveBDD() throws IOException
	{
		if(fichier.exists())fichier.delete();
		fichier.createNewFile();
		FileOutputStream fos = new FileOutputStream(fichier);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		oos.writeObject(this.utilisateurs);
		oos.writeObject(this.promotions);
		oos.writeObject(this.groupes);
		oos.writeObject(this.matieres);
		oos.writeObject(this.salles);
		oos.writeObject(this.cours);
		oos.close();
	}
	
	/**
	 * Charge la base de donnees a partir d'un fichier de sauvegarde
	 * @throws Exception
	 */
	private void chargeBDD()throws Exception
	{
		FileInputStream fis = new FileInputStream(fichier);
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		this.utilisateurs=(Vector<Personne>)ois.readObject();
		this.promotions=(Vector<Promotion>)ois.readObject();
		this.groupes=(Vector<Groupe>)ois.readObject();
		this.matieres=(Vector<Matiere>)ois.readObject();
		this.salles=(Vector<Salle>)ois.readObject();
		this.cours=(Vector<Cours>)ois.readObject();
		
		ois.close();
	}

//===================================================
//   Fonctions de tests
//===================================================
	
	private void afficheObjets(Vector v)
	{
		Iterator i = v.iterator();
		while(i.hasNext())
		{
			System.out.println((i.next()).toString());
		}
	}
		
	
	public void testAffiche()
	{
		//Vérifications a retirer par la suite
		/*System.out.println("Fin chargement");
		System.out.println(this.utilisateurs.size()+" utilisateurs");
		System.out.println(this.promotions.size()+" promotions");
		System.out.println(this.groupes.size()+" groupes");
		System.out.println(this.matieres.size()+" matieres");
		System.out.println(this.cours.size()+" cours");
		System.out.println(this.salles.size()+" salles");
		afficheObjets(utilisateurs);*/
		afficheObjets(cours);
	}
	
	/**
	 * Procedure affiche qui affiche un document xml
	 *
	 */
	public static void afficheXML(Document doc)
	{
		try
		{
			//On utilise ici un affichage classique avec getPrettyFormat()
			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			sortie.output(doc, System.out);
		}
		catch (java.io.IOException e){}
	}

	public static void main(String[] args) 
	{
		Gestion_BDD bd = Gestion_BDD.getInstance(true);
		bd.testAffiche();
		//System.out.println("====================================================");
		bd.sauvegarde();
		try {
			bd.sauveBDD();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Vector<Cours> getCours() {
		return cours;
	}

	public Vector<Groupe> getGroupes() {
		return groupes;
	}

	public Vector<Matiere> getMatieres() {
		return matieres;
	}

	public Vector<Salle> getSalles() {
		return salles;
	}

	
	public Cours getCours(Creneau creneau, Salle salle) {
		Boolean ok=false;
		int i =0;
		
		while(i<cours.size() && !ok)
		{
			if(cours.elementAt(i).getCreneau().egal(creneau) && cours.elementAt(i).getSalle().egal(salle))
			{
				ok= true;
			}
			else i++;
		}
		
		if(ok)return cours.elementAt(i);
		else return null;
	}

	
	public Boolean supprime_cours(Cours cours2) {
		return cours.remove(cours2);
	}


	
}
