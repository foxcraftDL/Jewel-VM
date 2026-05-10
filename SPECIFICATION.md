# Jewel-VM Bytecode Specification (Version 1.0)

The `.bfc` format is a binary representation of Brainfuck logic designed for high-density execution and rapid instruction fetching within the **Jewel-VM** environment. Every instruction in a `.bfc` file occupies exactly **3 bytes**.

## Instruction Structure
Each 3-byte block follows this layout:
- **Byte 0:** OpCode (The operation to be performed).
- **Bytes 1-2:** Argument (A 16-bit big-endian value).

## Instruction Set (ISA)


| OpCode | Mnemonic | Argument Description | Behavior |
| :--- | :--- | :--- | :--- |
| `0x01` | `MOVE` | Signed 16-bit Integer | Offsets the data pointer (`ptr`) by N. |
| `0x02` | `ADD` | Signed 16-bit Integer | Modifies the current cell value by N. |
| `0x03` | `OUT` | Unused (`0x0000`) | Outputs the ASCII character of the current cell. |
| `0x04` | `IN` | Unused (`0x0000`) | Reads a byte from STDIN into the current cell. |
| `0x05` | `JZ` | Unsigned 16-bit Index | Jump to instruction index N if current cell is 0. |
| `0x06` | `JNZ` | Unsigned 16-bit Index | Jump to instruction index N if current cell is NOT 0. |
| `0x07` | `ZERO` | Unused (`0x0000`) | Sets the current cell value to 0. |

## Technical Implementation Details
- **Endianness:** Arguments are stored in Big-Endian (Network Byte Order).
- **Jump Resolution:** In Jewel-VM, jump targets refer to the array index of the instruction, though they are stored as byte offsets in the file for portability.
- **Memory Management:** The VM uses a tape size defined at runtime (default 64KB), adjustable via the `-ms` flag.
