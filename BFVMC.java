import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BFVMC {
    public static final byte OP_MOVE = 0x01, OP_ADD = 0x02, OP_OUT = 0x03, 
                             OP_IN = 0x04, OP_JZ = 0x05, OP_JNZ = 0x06, OP_ZERO = 0x07;

    public static void main(String[] args) throws IOException {
        if (args.length < 1) return;
        String content = new String(Files.readAllBytes(Paths.get(args[0])));
        StringBuilder sb = new StringBuilder();
        for (char c : content.toCharArray()) {
            if ("><+-.,[]".indexOf(c) != -1) sb.append(c);
        }
        String code = sb.toString();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);

            if (c == '[' && i + 2 < code.length() && code.charAt(i+1) == '-' && code.charAt(i+2) == ']') {
                baos.write(OP_ZERO); baos.write(0); baos.write(0);
                i += 2; continue;
            }

            if ("><+-".indexOf(c) != -1) {
                int count = 0; char current = c;
                while (i < code.length() && code.charAt(i) == current) { count++; i++; }
                i--;
                byte op = (current == '>' || current == '<') ? OP_MOVE : OP_ADD;
                int val = (current == '<' || current == '-') ? -count : count;
                baos.write(op);
                baos.write((val >> 8) & 0xFF);
                baos.write(val & 0xFF);
            } 
            else if (c == '.') { baos.write(OP_OUT); baos.write(0); baos.write(0); }
            else if (c == ',') { baos.write(OP_IN);  baos.write(0); baos.write(0); }
            else if (c == '[') {
                stack.push(baos.size());
                baos.write(OP_JZ); baos.write(0); baos.write(0);
            } 
            else if (c == ']') {
                int startPos = stack.pop();
                int currentPos = baos.size() + 3;
                baos.write(OP_JNZ);
                baos.write((startPos >> 8) & 0xFF);
                baos.write(startPos & 0xFF);
                
                byte[] b = baos.toByteArray();
                b[startPos + 1] = (byte)((currentPos >> 8) & 0xFF);
                b[startPos + 2] = (byte)(currentPos & 0xFF);
                baos.reset(); baos.write(b);
            }
        }
        Files.write(Paths.get(args[0].replace(".bf", ".bfc")), baos.toByteArray());
        System.out.println("Archivo .bfc listo.");
    }
}
