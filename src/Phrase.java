import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Tristan on 23-04-18.
 */
public class Phrase {
    ArrayList<Mot> mots = new ArrayList<>();
    String phrase = "";
    int firstWordPosition = 0;
    char endPhraseChar;

    public Phrase(String phrase) {
        this.phrase = phrase;
        handlePhrase();
    }

    public Phrase(String phrase, int firstWordPosition) {
        this.phrase = phrase;
        this.firstWordPosition = firstWordPosition;
        handlePhrase();
    }

    private void handlePhrase(){
        String mot = "";
        for(int i = 0; i < phrase.length(); i++)
        {
            char c = phrase.charAt(i);
            if(c == ' ' || c == '\n' || c == '.' || c == 13 || c == ','|| c == ';' || c == '!' || c == '?' || c == '\''|| c == '-' || c == ';'){
                if(mot.length() != 0){
                    mots.add(new Mot(mot));
                    mot = "";
                }
                if(c == '.' || c == '!' || c == '?'){
                    endPhraseChar = c;
                }
            }else{
                mot += c;
            }
        }
    }

    public double getNbMots(){
        return mots.size();
    }

    public double occMot(String motToLook){
        double motOcc = 0;
        for(Mot mot: mots){
            if(mot.getMot().equals(motToLook)){
                ++motOcc;
            }
        }

        return motOcc;
    }

    public ArrayList<Mot> getMots() {
        return mots;
    }

    public Double getScore() {
        double score = 0;
        for(Mot mot: mots){
            score += mot.getSigScore();
        }
        return score;
    }

    public void read() {
        for(Mot mot: mots){
            System.out.println(mot.getMot());
        }
        System.out.println(endPhraseChar);
    }
}
