import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BFVM {
    
    static final class Context {
        byte[] tape;
        int ptr = 0;
        int ip = 0;
        Context(int size) { tape = new byte[size]; }
    }

    interface Op { void exec(Context ctx); }

    static final class Add implements Op {
        private final byte val;
        Add(short v) { this.val = (byte) v; }
        public void exec(Context ctx) { ctx.tape[ctx.ptr] += val; ctx.ip++; }
    }

    static final class Move implements Op {
        private final short val;
        private final int size;
        Move(short v, int s) { this.val = v; this.size = s; }
        public void exec(Context ctx) { 
            // Manejo de wrap-around del puntero (evita ArrayIndexOutOfBounds)
            ctx.ptr = (ctx.ptr + val) % size;
            if (ctx.ptr < 0) ctx.ptr += size;
            ctx.ip++; 
        }
    }

    static final class JZ implements Op {
        int target;
        public void exec(Context ctx) {
            // FIX v1.1.0: Comparación unsigned para evitar bucles infinitos
            if ((ctx.tape[ctx.ptr] & 0xFF) == 0) ctx.ip = target;
            else ctx.ip++;
        }
    }

    static final class JNZ implements Op {
        int target;
        public void exec(Context ctx) {
            // FIX v1.1.0: Comparación unsigned
            if ((ctx.tape[ctx.ptr] & 0xFF) != 0) ctx.ip = target;
            else ctx.ip++;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Uso: java -jar BFVM.jar <archivo.bfc> [-ms <size>]");
            return;
        }

        int memorySize = 65536; 
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-ms") && i + 1 < args.length) {
                memorySize = Integer.parseInt(args[i + 1]);
            }
        }

        byte[] bfc = Files.readAllBytes(Paths.get(args[0]));
        List<Op> ops = new ArrayList<>();
        Map<Integer, Integer> byteToOpIdx = new HashMap<>();

        int b = 0;
        while (b < bfc.length) {
            byteToOpIdx.put(b, ops.size());
            int opCode = bfc[b];
            short arg = (short) (((bfc[b + 1] & 0xFF) << 8) | (bfc[b + 2] & 0xFF));

            switch (opCode) {
                case 0x01: ops.add(new Move(arg, memorySize)); break;
                case 0x02: ops.add(new Add(arg)); break;
                case 0x03: ops.add(ctx -> { System.out.print((char)(ctx.tape[ctx.ptr] & 0xFF)); ctx.ip++; }); break;
                case 0x04: ops.add(ctx -> { try { ctx.tape[ctx.ptr] = (byte)System.in.read(); } catch(Exception e){} ctx.ip++; }); break;
                case 0x05: ops.add(new JZ()); break;
                case 0x06: ops.add(new JNZ()); break;
                case 0x07: ops.add(ctx -> { ctx.tape[ctx.ptr] = 0; ctx.ip++; }); break;
            }
            b += 3;
        }

        for (int i = 0; i < ops.size(); i++) {
            Op current = ops.get(i);
            int bytePos = i * 3; // Reconstrucción de posición para el linkeo
            if (current instanceof JZ || current instanceof JNZ) {
                int byteTarget = ((bfc[bytePos + 1] & 0xFF) << 8) | (bfc[bytePos + 2] & 0xFF);
                int targetOpIdx = byteToOpIdx.get(byteTarget);
                if (current instanceof JZ) ((JZ)current).target = targetOpIdx;
                else ((JNZ)current).target = targetOpIdx;
            }
        }

        Op[] program = ops.toArray(new Op[0]);
        Context ctx = new Context(memorySize);
        int progLen = program.length;

        long start = System.nanoTime();
        while (ctx.ip < progLen) {
            program[ctx.ip].exec(ctx);
        }
        long end = System.nanoTime();

        System.out.printf("\n\n--- Jewel-VM v1.1.0 | Tiempo: %.4f ms ---\n", (end - start) / 1_000_000.0);
    }
}

