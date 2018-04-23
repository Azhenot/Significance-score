/**
 * Created by Tristan on 23-04-18.
 */
public class Main {

    public static void main(String[] args) {

        Text text = new Text("C:\\Users\\Tristan\\Documents\\GitHub\\SignificanceScore\\src\\subtitles genere.txt");
        text.readFile();
        text.setOccToMots();
        text.setPositionMots();
        text.calculateNValueMots();
        text.calculateSigValue();
        text.calculateCorrespondances(15);
        text.writeInFile();
        text.readPhrases();



    }

}
