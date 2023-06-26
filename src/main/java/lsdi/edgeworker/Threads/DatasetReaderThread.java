package lsdi.edgeworker.Threads;
import lsdi.edgeworker.Services.EsperService;

import java.io.*;

import java.util.Scanner;

public class DatasetReaderThread extends Thread {
    EsperService esperService = EsperService.getInstance();

    @Override
    public void run() {
        super.run();

        try {
            String path = "src/main/java/lsdi/edgeworker/Dataset/Combed/";
            Scanner scannerCurrent = new Scanner(new File(path + "Current.csv"));
            Scanner scannerEnergy = new Scanner(new File(path + "Energy.csv"));
            Scanner scannerPower = new Scanner(new File(path + "Power.csv"));

            while (scannerCurrent.hasNextLine() && scannerEnergy.hasNextLine() && scannerPower.hasNextLine()) {
                String[] current = scannerCurrent.nextLine().split(",");
                String[] energy = scannerEnergy.nextLine().split(",");
                String[] power = scannerPower.nextLine().split(",");

                Thread.sleep(500);

                // SmartMeterMeasurement smartMeterMeasurement =
                //         new SmartMeterMeasurement(Double.parseDouble(power[1]), Double.parseDouble(current[1]), Double.parseDouble(energy[1]));

                // esperService.sendEvent(smartMeterMeasurement, "SmartMeterMeasurement");
            }

        } catch (FileNotFoundException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
