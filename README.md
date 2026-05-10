# Jewel-VM: A High-Performance Brainfuck Engine

Jewel-VM is a specialized execution environment for the Brainfuck language, designed with a focus on high-density bytecode and Just-In-Time (JIT) optimization. By implementing an Ahead-of-Time (AOT) compilation phase and a polymorphic instruction dispatcher, this engine achieves execution times in the microsecond range for complex algorithms.

## Technical Architecture

The system is divided into two main components: an optimizing compiler and a virtual machine.

### 1. The Optimizing Compiler (BFVMC)
The compiler transforms standard Brainfuck source code into a proprietary 3-byte fixed-width instruction set. During this phase, it applies several optimization passes:

*   **Run-Length Encoding (RLE):** Consecutive arithmetic and pointer operations are collapsed into single instructions with 16-bit arguments.
*   **Pattern Matching:** Common idioms such as the "clear loop" `[-]` are identified and replaced with a single `OP_ZERO` operation.
*   **Static Jump Resolution:** Branch targets are pre-calculated during the linking phase, eliminating the need for runtime bracket matching.

### 2. The Virtual Machine (BFVM)
The VM is engineered to leverage the Java HotSpot VM's optimization capabilities:

*   **Instruction Inlining:** By using a polymorphic dispatcher with final classes, the VM encourages the JIT compiler to inline instruction logic directly into the main execution loop.
*   **Memory Locality:** The default memory tape is optimized to interact efficiently with L1/L2 CPU caches, minimizing latency during high-frequency pointer manipulation.
*   **Efficient Dispatch:** The 3-byte fixed-width bytecode allows for predictable instruction fetching and improves branch prediction accuracy.

## Performance Benchmarks

*   **Compression Ratio:** Reduces source code size by up to 64% (e.g., 25KB source to 9KB bytecode).
*   **Execution Speed:** Average "Hello World" execution in approximately 0.0086 ms.
*   **Complexity Handling:** Fully compatible with self-hosting compilers (awib) and intensive mathematical scripts (PI16.bf).

## Installation and Usage

### Compilation
To build the binaries, compile the source files and package them into executable JAR files:

```bash
javac BFVMC.java BFVM.java
jar cfe BFVMC.jar BFVMC BFVMC.class
jar cfe BFVM.jar BFVM BFVM*.class
```

### Deployment Workflow
1. Compile the Brainfuck source into optimized bytecode:
```bash
java -jar BFVMC.jar program.bf
```

2. Execute the resulting .bfc file:
```bash
java -jar BFVM.jar program.bfc
```

### Configuration Flags
The VM supports memory configuration via the `-ms` flag:
```bash
java -jar BFVM.jar program.bfc -ms 131072
```

## License
Jewel-VM is released under a custom License. You are permitted to use and modify the code for personal and educational purposes. Redistribution of modified versions is strictly prohibited. See the LICENSE file for full details.
