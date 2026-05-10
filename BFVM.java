import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BFVM {
    
    // Contexto de ejecución para máxima velocidad
    static final class Context {
        byte[] tape;
        int ptr = 0;
        int ip = 0;
        Context(int size) { tape = new byte[size]; }
    }

    // Interfaz de instrucción para que la JVM haga Inlining
    interface Op { void exec(Context ctx); }

    // Clases de instrucciones optimizadas
    static final class Add implements Op {
        private final byte val;
        Add(short v) { this.val = (byte) v; }
        public void exec(Context ctx) { ctx.tape[ctx.ptr] += val; ctx.ip++; }
    }

    static final class Move implements Op {
        private final short val;
        private final int mask;
        Move(short v, int m) { this.val = v; this.mask = m; }
        public void exec(Context ctx) { 
            ctx.ptr = (ctx.ptr + val) & mask; 
            ctx.ip++; 
        }
    }

    static final class JZ implements Op {
        int target;
        public void exec(Context ctx) {
            if (ctx.tape[ctx.ptr] == 0) ctx.ip = target;
            else ctx.ip++;
        }
    }

    static final class JNZ implements Op {
        int target;
        public void exec(Context ctx) {
            if (ctx.tape[ctx.ptr] != 0) ctx.ip = target;
            else ctx.ip++;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Uso: java -jar BFVM.jar <archivo.bfc> [-ms <size>]");
            return;
        }

        int memorySize = 65536; // Por defecto 64KB
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-ms") && i + 1 < args.length) {
                memorySize = Integer.parseInt(args[i + 1]);
            }
        }

        byte[] bfc = Files.readAllBytes(Paths.get(args[0]));
        List<Op> ops = new ArrayList<>();
        Map<Integer, Integer> byteToOpIdx = new HashMap<>();

        // Máscara para el puntero (solo funciona si memorySize es potencia de 2)
        // Si no es potencia de 2, usaremos el modulo tradicional en Move
        int mask = ((memorySize & (memorySize - 1)) == 0) ? memorySize - 1 : -1;

        // FASE 1: Decodificación
        int b = 0;
        while (b < bfc.length) {
            byteToOpIdx.put(b, ops.size());
            int opCode = bfc[b];
            short arg = (short) (((bfc[b + 1] & 0xFF) << 8) | (bfc[b + 2] & 0xFF));

            switch (opCode) {
                case 0x01: ops.add(new Move(arg, mask != -1 ? mask : memorySize)); break;
                case 0x02: ops.add(new Add(arg)); break;
                case 0x03: ops.add(ctx -> { System.out.print((char)(ctx.tape[ctx.ptr] & 0xFF)); ctx.ip++; }); break;
                case 0x04: ops.add(ctx -> { try { ctx.tape[ctx.ptr] = (byte)System.in.read(); } catch(Exception e){} ctx.ip++; }); break;
                case 0x05: ops.add(new JZ()); break;
                case 0x06: ops.add(new JNZ()); break;
                case 0x07: ops.add(ctx -> { ctx.tape[ctx.ptr] = 0; ctx.ip++; }); break;
            }
            b += 3;
        }

        // FASE 2: Linkeo de saltos
        b = 0;
        for (int i = 0; i < ops.size(); i++) {
            Op current = ops.get(i);
            if (current instanceof JZ || current instanceof JNZ) {
                int byteTarget = ((bfc[b + 1] & 0xFF) << 8) | (bfc[b + 2] & 0xFF);
                int targetOpIdx = byteToOpIdx.get(byteTarget);
                if (current instanceof JZ) ((JZ)current).target = targetOpIdx;
                else ((JNZ)current).target = targetOpIdx;
            }
            b += 3;
        }

        Op[] program = ops.toArray(new Op[0]);
        Context ctx = new Context(memorySize);
        int progLen = program.length;

        // FASE 3: Ejecución
        long start = System.nanoTime();
        while (ctx.ip < progLen) {
            program[ctx.ip].exec(ctx);
        }
        long end = System.nanoTime();

        System.out.printf("\n\n--- Tiempo: %.4f ms (Memoria: %d) ---\n", (end - start) / 1_000_000.0, memorySize);
    }
}
