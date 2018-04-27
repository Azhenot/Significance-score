import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Tristan on 23-04-18.
 */

//hello
public class Text {
    ArrayList<Phrase> phrases = new ArrayList<>();
    String fileName;
    String text = "";
    double wordCount = 0;
    ArrayList<Double> correspondances = new ArrayList<>();
    ArrayList<Integer> localMinimums = new ArrayList<>();



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

    public void readFile2(){
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

            String sCurrentLine;
            String before = "";
            String timeStamp;
            while ((sCurrentLine = br.readLine()) != null) {
                if(!sCurrentLine.equals("")){
                    if(sCurrentLine.contains("-->")){
                        timeStamp = sCurrentLine;
                    }else{
                        text += before;
                        text += " ";
                    }
                    before = sCurrentLine;
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
            if( c == '.' || c == '?' || c == '!'/*cptMots == 15*/){
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
            int cpt = 0;
            for (Double corr : correspondances) {
                ++cpt;
                if(!String.valueOf(corr).equals("NaN") && !String.valueOf(corr).equals("-Infinity")){
                    fw.write(String.valueOf(corr)+"\n");
                }
            }
            fw.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fw = new FileWriter("outMinimums.txt");
            int compteur = 0;
            int compteurMin = 0;
            int autreCpt = 0;
            ArrayList<Double> newLocalMinimums = new ArrayList<>();
            while(compteur < localMinimums.get(localMinimums.size()-1) && compteurMin < localMinimums.size()){
                if(compteur == localMinimums.get(compteurMin)){
                    newLocalMinimums.add(correspondances.get(localMinimums.get(compteurMin)));
                    ++compteurMin;
                }else{
                    newLocalMinimums.add(0.0);
                }
                ++compteur;
            }
            for (Double corr : newLocalMinimums) {
                fw.write(String.valueOf(corr)+"\n");
            }
            fw.close();

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

    public void smoothing(){
        int cpt = 0;
        ArrayList<Double> correspondances2 = new ArrayList<>();
        correspondances2.add(correspondances.get(0));

        while(cpt < correspondances.size()-2){
            Double A = (correspondances.get(cpt+2) - correspondances.get(cpt))/2;
            A = A + correspondances.get(cpt);
            Double B = (correspondances.get(cpt+1) + A)/2;

            correspondances2.add(cpt+1,B);
            ++cpt;
        }
        correspondances2.add(correspondances.get(cpt));
        correspondances = correspondances2;

    }

    public void localMinima(Double cohesion) {
        int cpt = 0;
        Double min;
        Double max;
        boolean descending = false;
        ArrayList<Integer> maximums = new ArrayList<>();
        ArrayList<Integer> minimums = new ArrayList<>();
        if(correspondances.get(0) > correspondances.get(1)) {
            descending = true;
            maximums.add(0);
        }
        while(cpt < correspondances.size()-1) {
            if (!descending) {
                if (correspondances.get(cpt + 1) > correspondances.get(cpt)) {
                    max = correspondances.get(cpt);
                } else {
                    maximums.add(cpt);
                    descending = true;
                }
            }else {
                if (correspondances.get(cpt + 1) < correspondances.get(cpt)) {
                    min = correspondances.get(cpt);
                } else {
                    minimums.add(cpt);
                    descending = false;
                }
            }
            ++cpt;
        }

        cpt = 0;
        System.out.println(minimums.size() + " " + maximums.size());
        while(cpt < minimums.size() && cpt < maximums.size()-1){
            Double difference1 = correspondances.get(maximums.get(cpt)) - correspondances.get(minimums.get(cpt));
            Double difference2 = correspondances.get(maximums.get(cpt+1)) - correspondances.get(minimums.get(cpt));
            Double toDiff = (difference1+difference2)/2;
            System.out.println(correspondances.get(maximums.get(cpt))+ " " + correspondances.get(maximums.get(cpt+1)) + " "+ correspondances.get(minimums.get(cpt)) + " " +difference1 + " " + difference2 + " " +toDiff);
            if(difference1 > toDiff-cohesion && difference2 > toDiff-cohesion){
                localMinimums.add(minimums.get(cpt));
            }
            ++cpt;
        }

    }

    public Double average(ArrayList<Integer> list){
        double somme = 0;
        for(Integer cpt: list){
            somme += correspondances.get(cpt);
        }
        return somme/correspondances.size();
    }

    public void generateGraph() {
        ProcessBuilder pb = new ProcessBuilder("node", "generateSigGraph.js");
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        try {
            Process p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
