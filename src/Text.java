import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Tristan on 23-04-18.
 */
public class Text {
    ArrayList<Phrase> phrases = new ArrayList<>();
    String fileName;
    String text = "";
    double wordCount = 0;
    ArrayList<Double> correspondances = new ArrayList<>();

    public Text(String fileName) {
        this.fileName = fileName;
    }

    public void readFile(){
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

                String sCurrentLine;

                while ((sCurrentLine = br.readLine()) != null) {
                    if(!sCurrentLine.equals("")){
                        text += sCurrentLine;
                        text += " ";
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        handleText();
    }

    private void handleText() {
        text = text.toLowerCase();
        text.replace("/\n"," ");
        text.trim();
        text = text.replace("  "," ");//2 or more space to 1
        String phrase = "";
        int cptMots = 0;
        for(int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            phrase += c;
            if(c == ' '){
                ++cptMots;
            }
            if(/*c == '.' || c == '?' || c == '!'*/cptMots == 15){
                //If line is a timestamp, saved and phrase gets reference
                phrases.add(new Phrase(phrase));
                phrase = "";
                cptMots = 0;
            }

        }
    }

    public double getNbMots(){
        if(wordCount == 0){
            double nbMots = 0;
            for (Phrase phrase: phrases) {
                nbMots += phrase.getNbMots();
            }
            wordCount = nbMots;
        }
        return wordCount;

    }

    public double occMot(String motToLook){
        double motOcc = 0;
        for(Phrase phrase: phrases){
            motOcc += phrase.occMot(motToLook);
        }
        return motOcc;
    }

    public void setOccToMots(){
        for(Phrase phrase: phrases){
            for(Mot mot: phrase.getMots()){
                mot.setOccurences(occMot(mot.getMot()));
                mot.calculateN(getNbMots());
            }
        }
    }

    public void setPositionMots(){
        int positionMot = 0;
        int positionPhrase = 0;
        for(Phrase phrase: phrases){
            for(Mot mot: phrase.getMots()){
                mot.setPositionText(positionMot);
                ++positionMot;
            }
            ++positionPhrase;
        }
    }

    public void calculateNValueMots(){
        for(Phrase phrase: phrases){
            for(Mot mot: phrase.getMots()){
                mot.setOccurences(occMot(mot.getMot()));
                mot.calculateN(getNbMots());
            }
        }
    }

    public List<Integer> calculateDistancesMot(Mot motToCalculate){
        ArrayList<Integer> distances = new ArrayList<>();

        for(Phrase phrase: phrases){
            for(Mot mot: phrase.getMots()){
                int distanceValue = motToCalculate.getPositionText() - mot.getPositionText();
                if(motToCalculate.getMot().equals(mot.getMot())){
                    if(distanceValue < 0){
                        distanceValue = distanceValue * (-1);
                    }
                    if(motToCalculate != mot){
                        distances.add(distanceValue);
                    }
                }

            }
        }
        Collections.sort(distances, Collections.reverseOrder());
        if(distances.size() > 10){
            return distances.subList(0,9);
        }else{
            return distances.subList(0,distances.size());
        }
    }

    public void calculateSigValue(){
        for(Phrase phrase: phrases){
            for(Mot mot: phrase.getMots()){
                mot.calculateSigValue(calculateDistancesMot(mot), getNbMots());
            }
        }
        normalizing();
    }

    public void normalizing(){
        Double min = 100.0;
        Double max = 0.0;
        for(Phrase phrase: phrases){
            for(Mot mot: phrase.getMots()){
                if(mot.getSigScore() > max){
                    max = mot.getSigScore();
                }else if(mot.getSigScore() < min){
                    min = mot.getSigScore();
                }
            }
        }

        for(Phrase phrase: phrases){
            for(Mot mot: phrase.getMots()){
                mot.normalize(min, max);
            }
        }
    }

    public void breakPointPoints(int debut, int clusterSize) {
        int cpt = debut;
        ArrayList<Phrase> clusterPhrases1 = new ArrayList<>();
        ArrayList<Phrase> clusterPhrases2 = new ArrayList<>();
        double clusterScore1 = 0;
        double clusterScore2 = 0;
        while(cpt < phrases.size() && cpt < debut+(clusterSize*2)){
            if(cpt < debut+clusterSize){
                clusterPhrases1.add(phrases.get(cpt));
                clusterScore1 += phrases.get(cpt).getScore();
            }else if(cpt >= debut+clusterSize && cpt < debut+(clusterSize*2)){
                clusterPhrases2.add(phrases.get(cpt));
                clusterScore2 += phrases.get(cpt).getScore();
            }
            ++cpt;
        }

        ArrayList<Double> scores  = wordsCluster1In2(clusterPhrases1, clusterPhrases2);
        double Ap = scores.get(0);
        double Bp = scores.get(0);
        double App = scores.get(1);
        double Bpp = scores.get(1);

        double correspondance = (((Ap-App)/clusterScore1)+((Bp-Bpp)/clusterScore2))/2;
        correspondances.add(correspondance);
    }

    public void writeInFile() {
        FileWriter fw = null;
        try {
            fw = new FileWriter("out.txt");
            for (Double correspondance : correspondances) {
                fw.write(String.valueOf(correspondance+"\n"));
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Double> wordsCluster1In2(ArrayList<Phrase> clusterPhrases1,ArrayList<Phrase>  clusterPhrases2){
        double score = 0;
        double score2 = 0;

        for(Phrase phrase: clusterPhrases1){
            for(Mot mot: phrase.getMots()){
                boolean ok = true;
                for(Phrase phrase2: clusterPhrases2){
                    for(Mot mot2: phrase.getMots()){
                        if(mot.getMot().equals(mot.getMot())){
                            score += mot.getSigScore();
                            ok = false;
                        }
                    }
                }
                if(ok){
                    score2 += mot.getSigScore();
                }
            }
        }
        ArrayList<Double> toReturn  = new ArrayList<>();
        toReturn.add(score);
        toReturn.add(score2);
        return toReturn;
    }

    public void calculateCorrespondances(int clusterSize){
        int cpt = 0;
        while(cpt < phrases.size()){
            breakPointPoints(cpt, clusterSize);
            ++cpt;
        }
    }

    public void readPhrases(){
        for(Phrase phrase: phrases){
            phrase.read();
        }
    }

}
