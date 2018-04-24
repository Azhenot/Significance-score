/**
 * Created by Tristan on 23-04-18.
 */
public class Main {

    public static void main(String[] args) {

        Text text = new Text("C:\\Users\\Tristan\\Documents\\GitHub\\Significance-Score\\src\\subtitles.txt");
        text.readFile();
        text.setOccToMots();
        text.setPositionMots();
        text.calculateNValueMots();
        text.calculateSigValue();
        text.calculateCorrespondances(15);
        text.smoothing();
        text.smoothing();
        text.smoothing();
        text.smoothing();
        text.smoothing();
        text.smoothing();
        text.smoothing();
        text.smoothing();
        text.smoothing();
        text.smoothing();
        text.smoothing();
        text.smoothing();
        text.localMinima(10.0);
        text.writeInFile();
        //text.readPhrases();



    }

}
