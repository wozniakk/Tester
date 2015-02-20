package local.tester;

import org.JMathStudio.DataStructure.Vector.Vector;
import org.JMathStudio.DataStructure.Vector.VectorStack;
import org.JMathStudio.Exceptions.UnSupportedAudioFormatException;
import org.JMathStudio.Interface.AudioInterface.AudioBuffer;
import org.JMathStudio.Interface.AudioInterface.AudioDecoder;
import org.JMathStudio.SignalToolkit.GeneralTools.SignalSpectrum;
import javax.naming.SizeLimitExceededException;
import java.io.IOException;

public class DSP {

    public static void analyse(String nazwa1, String nazwa2)
    {
        AudioDecoder ad1 = new AudioDecoder(); //tworzymy decodery do plików
        AudioDecoder ad2 = new AudioDecoder();
        AudioBuffer data1 = null, data2 = null; // i buffery
        try {
            data1 = ad1.decodeAudioData(nazwa1);
            data2 = ad2.decodeAudioData(nazwa2);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SizeLimitExceededException e) {
            e.printStackTrace();
        } catch (UnSupportedAudioFormatException e) {
            e.printStackTrace();
        }
        System.out.println("Decoding finished succesfully");
        VectorStack allChannels = data1.accessAudioBuffer(); // tworzymy stos wektorów dla poszczególnych kanałów
        Vector channel1 = allChannels.accessVector(0); // i wczytujemy pierwszy - bo mamy jednokanalowy dzwiek
        allChannels = data2.accessAudioBuffer();
        Vector channel2 = allChannels.accessVector(0);
        if (channel1.hasSameLength(channel2)) System.out.println("Dlugosci są identyczne!"); // sprawdzamy, czy pliki mają taką samą długość
        int zgodne = 0;
        for (int i = 0; i < Math.max(channel1.length(), channel2.length()); i++)
        {
            if( (double)channel1.getElement(i)/(double)channel2.getElement(i) > 0.7 && (double)channel1.getElement(i)/(double)channel2.getElement(i) < 1.42) zgodne++;
        }
        double zgodnosc = 100 * zgodne / channel1.length();
        System.out.println("Dane są zgodne w " + zgodnosc + " %");
        // policzyliśmy zgodność dla raw data

        SignalSpectrum myspect = new SignalSpectrum();
        Vector psd1 = myspect.PSD(channel1);
        Vector psd2 = myspect.PSD(channel2);
        // tutaj zrobiliśmy spektrum mocy dla obydwu sygnałów

        zgodne = 0;
        for (int i = 0; i < Math.max(psd1.length(), psd2.length()); i++)
        {
            if( (double)psd1.getElement(i)/(double)psd2.getElement(i) > 0.7*(double)Math.max(psd1.getElement(i),psd2.getElement(i)) && (double)psd1.getElement(i)/(double)psd2.getElement(i) < 1.42/(double)Math.min(psd1.getElement(i),psd2.getElement(i))) zgodne++;
        }
        double zgodnosc2 = 100 * zgodne / psd1.length();
        System.out.println("Spektra są zgodne w " + zgodnosc2 + " %");
        // i dla tych spektr policzylismy zgodnosc
    }
}
