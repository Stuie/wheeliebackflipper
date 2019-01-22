package app.sonderful.wheeliebackflipper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class UacElevator {
    private static String batName = "elevate.bat";

    public static void run() {
        if (isUacOn()) {
            // Attempt elevation
            new UacElevator().elevate();
            System.exit(0);
        }
    }

    private static boolean isUacOn() {
        final File dummyFile = new File("c:/aaa.txt");
        dummyFile.deleteOnExit();

        try {
            // Attempt to create file in C:/
            try (final FileWriter fw = new FileWriter(dummyFile, true)) {
            }
        } catch (final IOException ex) {
            // Can't write file, UAC must be on
            return true;
        }

        return false;
    }

    private void elevate() {
        // Create batch file in temporary directory as we have access to it regardless of whether UAC is on or off
        final File file = new File(System.getProperty("java.io.tmpdir") + "/" + batName);
        //file.deleteOnExit();

        createBatchFile(file);

        runBatchFile();
    }

    private String getJarLocation() {
        return getClass().getProtectionDomain().getCodeSource().getLocation().getPath().substring(1);
    }

    private void runBatchFile() {
        final Runtime runtime = Runtime.getRuntime();
        final String[] cmd = new String[]{"cmd.exe", "/C",
                System.getProperty("java.io.tmpdir") + "/" + batName + " java -jar " + getJarLocation()};
        try {
            runtime.exec(cmd);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    private void createBatchFile(final File file) {
        try (final FileWriter fw = new FileWriter(file, false)) {
            fw.write(
                    "@echo Set objShell = CreateObject(\"Shell.Application\") > %temp%\\sudo.tmp.vbs\r\n"
                            + "@echo args = Right(\"%*\", (Len(\"%*\") - Len(\"%1\"))) >> %temp%\\sudo.tmp.vbs\r\n"
                            + "@echo objShell.ShellExecute \"%1\", args, \"\", \"runas\" >> %temp%\\sudo.tmp.vbs\r\n"
                            + "@cscript %temp%\\sudo.tmp.vbs\r\n"
                            + "del /f %temp%\\sudo.tmp.vbs\r\n");
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }
}
