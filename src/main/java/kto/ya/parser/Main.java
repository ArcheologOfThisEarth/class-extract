package kto.ya.parser;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class Main {

    public static void main(String[] args) {
        String filePath = "C:\\Users\\Root\\AppData\\Roaming\\.minecraft\\journaltrace-dumper.bin";
        String jarFilePath = "output.jar";
        try {
            List<byte[]> extractedClasses = extractClasses(filePath);
            if (extractedClasses != null && !extractedClasses.isEmpty()) {
                createJarWithClasses(extractedClasses, jarFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<byte[]> extractClasses(String filePath) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(filePath))) {
            byte[] buffer = new byte[dis.available()];
            dis.readFully(buffer);
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);
            int magicNumber = 0xCAFEBABE;

            List<byte[]> classes = new ArrayList<>();
            int startPosition = -1;
            int endPosition = -1;

            while (byteBuffer.hasRemaining() && byteBuffer.remaining() >= 4) { //0xCAFEBABE
                int current = byteBuffer.getInt();
                if (current == magicNumber) {
                    if (startPosition != -1) {
                        endPosition = byteBuffer.position() - 4;
                        byte[] extractedData = new byte[endPosition - startPosition];
                        System.arraycopy(buffer, startPosition, extractedData, 0, extractedData.length);
                        classes.add(extractedData);
                    }
                    startPosition = byteBuffer.position() - 4;
                }
            }

            if (startPosition != -1 && byteBuffer.position() > startPosition) {
                endPosition = byteBuffer.position();
                byte[] extractedData = new byte[endPosition - startPosition];
                System.arraycopy(buffer, startPosition, extractedData, 0, extractedData.length);
                classes.add(extractedData);
            }

            System.out.println("classes: " + classes.size());
            return classes;
        }
    }

    public static void createJarWithClasses(List<byte[]> extractedClasses, String jarFilePath) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFilePath))) {
            for (int i = 0; i < extractedClasses.size(); i++) {
                byte[] classData = extractedClasses.get(i);
                JarEntry jarEntry = new JarEntry("clazz" + i + ".class");
                jos.putNextEntry(jarEntry);
                jos.write(classData);
                jos.closeEntry();
            }
        }
    }
}
