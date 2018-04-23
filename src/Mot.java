import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tristan on 23-04-18.
 */
public class Mot {
    String mot;
    double nValue;
    double occurences;
    double sigScore;
    int positionText;
    ArrayList<Double> distances = new ArrayList<>();


    public Mot(String mot) {
        this.mot = mot;
    }

    public Mot(String mot, int positionPhrase) {
        this.mot = mot;
    }

    public String getMot() {
        return mot;
    }

    public void setOccurences(double occurences) {
        this.occurences = occurences;
    }

    public void calculateN(double maxMots){
        nValue = (8/(1+Math.pow(Math.E,(-200)*((occurences/maxMots)-0.02))))+2;
    }

    public void setPositionText(int positionText) {
        this.positionText = positionText;

    }

    public int getPositionText() {
        return positionText;
    }

    public void addDistance(double distanceToAdd){
        distances.add(distanceToAdd);
    }


    public void calculateSigValue(List<Integer> distances, Double motMax) {
        int cpt = 0;
        int out = 0;
        double somme = 0;
        while(cpt < distances.size() && out == 0){
            double newToAdd = Math.atan(distances.get(cpt)/(motMax/occurences));
            if(cpt < nValue){
                somme = somme + newToAdd;
            }else{
                out = 1;
            }
            ++cpt;
        }
        sigScore = (1/nValue)*somme;
    }

    public double getSigScore() {
        return sigScore;
    }

    public void normalize(Double min, Double max) {
        sigScore = (sigScore - min) / (max-min);
    }


}
