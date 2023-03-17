// Mansi Patel
import java.io.*;
import java.util.Scanner;
import java.lang.Runtime;

public class CPU {

	public static void main(String[] args) {
		
		// If the argument number on the command line is not 2, then return an error message and
		// end the program.
		if (args.length != 2) {
			System.out.println("One argument must be provided: the input file name and the timer interrupt value.");
			return;
		}
		
		// Create flags for user mode and system mode.
		boolean user_mode = true;
		boolean kernel_mode = false;
		
		// Set up the registers of the CPU.
		// PC is the program counter, SP is the stack pointer, IR is the instruction register,
		// AC is the accumulator, X and Y are temporary registers
		int PC = 0;
		int SP = 0; 
		int IR = 0;
		int AC = 0;
		int X = 0;
		int Y = 0;
		
		// The operand variable will hold the values given with an instruction
		// that requires an operand.
		int operand = 0;
		// The timer interrupt value will store the command line argument that indicates
		// the value at which an interrupt occurs in the program.
		int timer_interrupt_value = Integer.parseInt(args[1]);
		// The user SP variable will hold the user stack pointer temporarily when we are
		// changing modes and the value of the SP register. .
		int user_SP = 0;
		// The value variable is a temporary variable to hold values from addresses in memory.
		int value = 0;
		// The line variable will hold the instruction that was just executed, but it is not
		// used for anything (simply a holder for output stream).
		String line = "";
		
		// If we are in user mode, initialize the SP to 1000.
		if (user_mode) {
			SP = 1000;
		// If we are in kernel mode, initialize the SP to 2000.
		} else if (kernel_mode) {
			SP = 2000;
		}
		// Set booleans that indicate if an interrupt or system call is occurring.
		// This will help to prevent interrupts occurring during a system call or vice versa. 
		boolean timer_interrupt = false;
		boolean system_call = false;
		
		// Create a variable for the instruction count.
		int instruction_count = 0;
		
		
		try {
			// Run MainMemory.java as the child process of the CPU
			Runtime run_time = Runtime.getRuntime();
			String command_line = "java MainMemory " + args[0];
			Process main_memory_process = run_time.exec(command_line);
			
			// Create input stream and output stream to perform read/writes between MainMemory and CPU
			InputStream input = main_memory_process.getInputStream();
			OutputStream output = main_memory_process.getOutputStream();
			// System.out.println(main_memory_process.exitValue());
			// Create a PrintWriter object to write to MainMemory -- MainMemory will receive
			// information via System.in
			PrintWriter writer = new PrintWriter(output);
			
			InputStream errors = main_memory_process.getErrorStream();
			Scanner my_scanner = new Scanner(errors);
			
			// This will help with printing errors from main memory process
//			while (my_scanner.hasNext()) {
//				System.out.println(my_scanner.nextLine());
//			}
			
			// Create a scanner object to read in instructions from MainMemory -- MainMemory will
			// send instructions via System.out.println
			Scanner memory_input_reader = new Scanner(input);
			
			// This will allow an iteration of the loop in MainMemory to occur. 
			// Send the command "read" to the main memory.
			writer.printf("read\n");
			writer.flush();
			// Send the program counter value to MainMemory to retrieve its value.
			writer.printf(PC + "\n");
			// Flush writer.
			writer.flush();
			// Store the instruction value that was sent by main memory. 
			IR = Integer.parseInt(memory_input_reader.nextLine());
			// Send the command "done" to main memory since we are done with an initial
			// read.
			writer.printf("done\n");
			writer.flush();


			while(memory_input_reader.hasNext()) {
				// This reads in the next line of input from main memory in order
				// to progress the hasNext function of the while loop to continue.
				// Line should contain the last instruction value.
				line = memory_input_reader.nextLine();
		
				// Send the program counter value to main memory.
				writer.printf("read\n");
				writer.flush();
				writer.printf(PC + "\n");
				// Flush writer.
				writer.flush();
				// Store fetched instruction into IR.
				IR = Integer.parseInt(memory_input_reader.nextLine());

				// This switch case will execute the proper instruction based off of IR.
				switch (IR) {
				case 1: // Instruction: Load the value on the next line into the AC register
					// Increment the PC to get the next line after the instruction, which is the operand.
					PC++;
					// We want to read an operand from memory, so send the string "read" to memory.
					writer.printf("read\n");
					// Flush writer.
					writer.flush();
					// Send the PC to main memory to retrieve the value we want to load, which is at the address.
					writer.printf(PC + "\n");
					// Flush writer.
					writer.flush();
					// Main memory will send the operand based on the current value of the PC.
					operand = Integer.parseInt(memory_input_reader.nextLine());
					// AC register now holds the operand value
					AC = operand;
					// Increment the PC to be ready to fetch the next instruction.
					PC++;
					break;
					
				case 2: // Instruction: Load the value at the address (on the next line) into the AC
					// Increment the PC to get the next line after the instruction, which is the operand.
					PC++;
					// We want to first read the operand value from memory, so send the string "read" to memory.
					writer.printf("read\n");
					// Flush writer.
					writer.flush();
					// Send the PC to main memory to retrieve the operand at the address, which is the address
					// whose value we want to load into the AC.
					writer.printf(PC + "\n"); 
					// Flush writer.
					writer.flush();
					// Main memory will send the operand based on the current value of the PC.
					operand = Integer.parseInt(memory_input_reader.nextLine());
					// If we are in user mode, and the given address is outside of
					// the user program bounds, print an error message.
					if (user_mode && operand > 999) {
						// Send string "exit" to main memory to stop its process.
						writer.printf("exit\n"); 
						// Flush writer.
						writer.flush();
						// Wait for main memory to finish.
						main_memory_process.waitFor();
						// Print an error message.
						System.out.println("Error: Operand address out of user bounds"); 
						return;
					} else {
						// If the given address is in user bounds...
						// We want to read the value at the address in memory to then store it into the AC.
						writer.printf("read\n");
						// Flush writer.
						writer.flush();
						// Now send the address to memory to obtain the value at that address.
						writer.printf(operand + "\n");
						// Flush writer.
						writer.flush();
						// Store the value at that address into the value variable.
						value = Integer.parseInt(memory_input_reader.nextLine());
						// System.out.println("value: " + value);
						AC = value;
						// Update AC to hold the value at that address.
						// Increment the PC to be ready to fetch the next instruction.
						PC++;
					}
					break;
					
				case 3: // Instruction: Load the value from the address found in the given address into the AC
					// for example, if LoadInd 500, and address 500 contains 100, then load from address 100 into the AC
					// Increment the PC to get the next line after the instruction, which is the operand.
					PC++;
					// We want to first read the operand value from memory, so send the string "read" to memory.
					writer.printf("read\n");
					// Flush writer.
					writer.flush();
					// Send the PC to main memory to retrieve the operand at the address, which is the address
					// that has the address whose value we want to load  into the AC.
					writer.printf(PC + "\n"); 
					// Flush writer.
					writer.flush();
					// Main memory will send the operand (address) based on the current value of the PC.
					// Case w example: 500 is sent from memory
					operand = Integer.parseInt(memory_input_reader.nextLine());
					// If we are in user mode, and the given address is outside of
					// the user program bounds, print an error message.
					if (user_mode && operand > 999) {
						// Send string "exit" to main memory to stop its process.
						writer.printf("exit\n"); 
						// Flush writer.
						writer.flush();
						// Wait for main memory to finish.
						main_memory_process.waitFor();
						// Print an error message.
						System.out.println("Error: Address from operand address out of user bounds"); 
						return;
					} else {
						// We want to read the value at the address (stored in operand) in memory, as
						// that value will be the address whose value will be loaded into the AC.
						writer.printf("read\n");
						// Flush writer.
						writer.flush();
						// Send the address to main memory to receive its value.
						writer.printf(operand + "\n");
						// Flush writer.
						writer.flush();
						// Store the value at that address into the operand variable.
						// Case w example: 100 is sent from memory
						operand = Integer.parseInt(memory_input_reader.nextLine());
						// Now we want to read the value at this address.
						writer.printf("read\n");
						// Flush writer.
						writer.flush();
						// Send the address to main memory to receive its value.
						writer.printf(operand + "\n"); 
						// Flush writer.
						writer.flush();
						// Store the value at that address into the value variable.
						// Case w example: value at address 100 is sent from memory
						value = Integer.parseInt(memory_input_reader.nextLine());
						// The AC is now updated to hold the value at the last address we read from in memory.
						AC = value;
						// Increment the PC to be ready to fetch the next instruction.
						PC++;
					}
					break;
					
				case 4: // Instruction: Load the value at (address+X) into the AC
					// for example, if LoadIdxX 500, and X contains 10, then load from 510.
					// Increment the PC to get the next line after the instruction, which is the operand.
					PC++;
					// We want to first read the operand value from memory, so send the string "read" to memory.
					writer.printf("read\n");
					// Flush writer.
					writer.flush();
					// Send the PC to main memory to retrieve the operand at the address, which is the address
					// we will use to add to X.
					writer.printf(PC + "\n"); 
					// Flush writer.
					writer.flush();
					// Main memory will send the operand (address) based on the current value of the PC.
					// Case w example: 500 is sent from memory
					operand = Integer.parseInt(memory_input_reader.nextLine());
					// If we are in user mode, and the given address is outside of
					// the user program bounds, print an error message.
					if (user_mode && operand > 999) {
						// Send string "exit" to main memory to stop its process.
						writer.printf("exit\n"); 
						// Flush writer.
						writer.flush();
						// Wait for main memory to finish.
						main_memory_process.waitFor();
						// Print an error message.
						System.out.println("Error: Address out of user bounds"); 
						return;
					} else { 
						// Add value of X to operand.
						// Case w example: 500 + 10 = 510
						operand += X;
						// Having updated operand, operand now holds the address whose value we want to load into AC.
						// Again, check if we are in user mode, and the given address is outside of
						// the user program bounds, print an error message.
						if (user_mode && operand > 999) {
							// Send string "exit" to main memory to stop its process.
							writer.printf("exit\n"); 
							// Flush writer.
							writer.flush();
							// Wait for main memory to finish.
							main_memory_process.waitFor();
							// Print an error message.
							System.out.println("Error: Address from Address + X out of user bounds"); 
							return;
						} else {
							// If we are in user bounds, we can read the value at the address in memory.
							writer.printf("read\n");
							// Flush writer.
							writer.flush();
							// Send the address whose value we want to read from memory.
							writer.printf(operand + "\n"); 
							// Flush writer.
							writer.flush();
							// Main memory sends over the value at that address, it is stored in the value variable.
							// Case w example: value at 510 is read from memory.
							value = Integer.parseInt(memory_input_reader.nextLine());
							// Update AC to hold that value.
							AC = value;
							// Increment the PC to be ready to fetch the next instruction.
							PC++;
						}
					}
					break;
				
				case 5: // Instruction: Load the value at (address+Y) into the AC
					// This instruction is similar to case 4, except now we use Y instead of X.
					// Increment the PC to get the next line after the instruction, which is the operand.
					PC++;
					// We want to first read the operand value from memory, so send the string "read" to memory.
					writer.printf("read\n");
					// Flush writer.
					writer.flush();
					// Send the PC to main memory to retrieve the operand at the address, which is the address
					// we will use to add to Y.
					writer.printf(PC + "\n"); 
					// Flush writer.
					writer.flush();
					// Main memory will send the operand (address) based on the current value of the PC.
					operand = Integer.parseInt(memory_input_reader.nextLine());
					// If we are in user mode, and the given address is outside of
					// the user program bounds, print an error message.
					if (user_mode && operand > 999) {
						// Send string "exit" to main memory to stop its process.
						writer.printf("exit\n"); 
						// Flush writer.
						writer.flush();
						// Wait for main memory to finish.
						main_memory_process.waitFor();
						// Print an error message.
						System.out.println("Error: Address out of user bounds"); 
						return;
					} else { 
						// Add value of Y to operand.
						operand += Y;
						// Having updated operand, operand now holds the address whose value we want to load into AC.
						// Again, check if we are in user mode, and the given address is outside of
						// the user program bounds, print an error message.
						if (user_mode && operand > 999) {
							// Send string "exit" to main memory to stop its process.
							writer.printf("exit\n"); 
							// Flush writer.
							writer.flush();
							// Wait for main memory to finish.
							main_memory_process.waitFor();
							// Print an error message.
							System.out.println("Error: Address from Address + Y out of user bounds"); 
							return;
						} else {
							// If we are in user bounds, we can read the value at the address in memory.
							writer.printf("read\n");
							// Flush writer.
							writer.flush();
							// Send the address whose value we want to read from memory.
							writer.printf(operand + "\n"); 
							// Flush writer.
							writer.flush();
							// Main memory sends over the value at that address, it is stored in the value variable.
							value = Integer.parseInt(memory_input_reader.nextLine());
							// Update AC to hold that value.
							AC = value;
							// Increment the PC to be ready to fetch the next instruction.
							PC++;
						}
					}
					break;
					
				case 6: // Instruction: Load from (Sp+X) into the AC (if SP is 990, and X is 1, load from 991).
					// Add SP and X values together to get the address whose value we want to load into the AC.
					operand = SP + X;
					// If we are in user mode, and the address is outside of
					// the user program bounds, print an error message.
					if (user_mode && operand > 999) {
						// Send string "exit" to main memory to stop its process.
						writer.printf("exit\n"); 
						// Flush writer.
						writer.flush();
						// Wait for main memory to finish.
						main_memory_process.waitFor();
						// Print an error message.
						System.out.println("Error: Address from SP + X out of user bounds"); 
						return;
					} else {
						// If the address is in user bounds...
						// Read the value at that address from memory.
						writer.printf("read\n");
						// Flush writer.
						writer.flush();
						// Send the operand (which contains the address we want to read from) to memory.
						writer.printf(operand + "\n");
						// Flush writer.
						writer.flush();
						// Main memory will send the value at that address, store it into the value variable.
						value = Integer.parseInt(memory_input_reader.nextLine());
						// Load the value into AC.
						AC = value;
						// Increment the PC to be ready to fetch the next instruction.
						PC++;
					}
					break;
					
					
				case 7: // Instruction: Store the value in the AC into the address (on the next line)?
					// Increment the PC to get the next line after the instruction, which is the operand.
					PC++;
					// We want to first read the operand value from memory, so send the string "read" to memory.
					writer.printf("read\n");
					// Flush writer.
					writer.flush();
					// Send the PC to main memory to retrieve the operand at the address.
					writer.printf(PC + "\n"); 
					// Flush writer.
					writer.flush();
					// Main memory will send the operand based on the current value of the PC.
					operand = Integer.parseInt(memory_input_reader.nextLine());
					// If we are in user mode, and the given address is outside of
					// the user program bounds, print an error message.
					if (user_mode && operand > 999) {
						// Send string "exit" to main memory to stop its process.
						writer.printf("exit\n"); 
						// Flush writer.
						writer.flush();
						// Wait for main memory to finish.
						main_memory_process.waitFor();
						// Print an error message.
						System.out.println("Error: Address out of user bounds"); 
						return;
					} else {
						// If the given address is in user bounds...
						// Now we want to store the value in AC into the address.
						writer.printf("write\n");
						// Flush writer.
						writer.flush();
						// Provide the address we want to write to.
						writer.printf(operand + "\n");
						// Flush writer.
						writer.flush();
						// Send the value of AC to be written to memory.
						writer.printf(AC + "\n");
						// Flush writer.
						writer.flush();
						// Increment the PC to be ready to fetch the next instruction.
						PC++;
					}
					break;
					
				case 8: // Instruction: Gets a random int from 1 to 100 into the AC
					// Generate a random integer to store into AC
					int min_val = 1;
					int max_val = 100;
					value = (int) Math.floor(Math.random()*(max_val-min_val+1)+min_val);
					AC = value;
					// Increment the PC to be ready to fetch the next instruction.
					PC++;
					break;
					
				case 9: // Instruction: Prints integer or character to the screen based on operand
					// If port=1, writes AC as an int to the screen
					// If port=2, writes AC as a char to the screen
					// Increment the PC to get the next line after the instruction, which is the operand.
					PC++;
					// We want to first read the operand value from memory, so send the string "read" to memory.
					writer.printf("read\n");
					// Send the PC to main memory to retrieve the operand at the address.
					writer.printf(PC + "\n"); 
					// Flush writer.
					writer.flush();
					// Main memory will send the operand based on the current value of the PC.
					operand = Integer.parseInt(memory_input_reader.nextLine());
					// If the operand == 1, print the AC register as an int.
					if (operand == 1) {
						System.out.print(AC);
					// If the operand == 1, print the AC register as its designated char (ASCII).
					} else if (operand == 2) {
						System.out.print((char)AC);
					}
					// Increment the PC to be ready to fetch the next instruction.
					PC++;
					break;
				
				case 10: // Instruction: Add the value in X to the AC
					// Add the value in X to the AC.
					AC += X;
					PC++;
					break;
					
				case 11: // Instruction: Add the value in Y to the AC
					// Add the value in Y to the AC.
					AC += Y;
					// Increment the PC to be ready to fetch the next instruction.
					PC++;
					break;
					
				case 12: // Instruction: Subtract the value in X from the AC
					// Subtract the value in X from AC.
					AC -= X;
					// Increment the PC to be ready to fetch the next instruction.
					PC++;
					break;
					
				case 13: // Instruction: Subtract the value in Y from the AC
					// Subtract the value in Y from AC.
					AC -= Y;
					// Increment the PC to be ready to fetch the next instruction.
					PC++;
					break;	
					
				case 14: // Instruction: Copy the value in the AC to X
					// Store the value of the AC register into the X register.
					X = AC;
					// Increment the PC to be ready to fetch the next instruction.
					PC++;
					break;
					
				case 15: // Instruction: Copy the value in X to the AC
					// Store the value of the X register into the AC register.
					AC = X;
					// Increment the PC to be ready to fetch the next instruction.
					PC++;
					break;
				
				case 16: // Instruction: Copy the value in the AC to Y
					// Store the value of the AC register into the Y register.
					Y = AC;
					// Increment the PC to be ready to fetch the next instruction.
					PC++;
					break;
					
				
				case 17: // Instruction: Copy the value in Y to the AC
					// Store the value of the Y register into the AC register.
					AC = Y;
					// Increment the PC to be ready to fetch the next instruction.
					PC++;
					break;
			
				case 18: // Instruction: Copy the value in AC to the SP
					// Store the value of the AC register into the SP register.
					SP = AC;
					// Increment the PC to be ready to fetch the next instruction.
					PC++;
					break;
				
				case 19: // Instruction: Copy the value in SP to the AC
					// Store the value of the SP register into the AC register.
					AC = SP;
					// Increment the PC to be ready to fetch the next instruction.
					PC++;
					break;
					
				case 20: // Instruction: jump to the address provided in the next line
					// Increment the PC to get the next line after the instruction, which is the operand.
					PC++;
					// We want to first read the operand value from memory, so send the string "read" to memory.
					writer.printf("read\n");
					// Flush writer.
					writer.flush();
					// Send the PC to main memory to retrieve the operand at the address, which will be
					// the address we want to jump to.
					writer.printf(PC + "\n"); 
					// Flush writer.
					writer.flush();
					// Main memory will send the operand based on the current value of the PC.
					operand = Integer.parseInt(memory_input_reader.nextLine());
					// Set PC to the address.
					PC = operand;
					break;
					
				case 21: // Instruction: Jump to the address in the next line only if the value in the AC is zero
					// Increment the PC to get the next line after the instruction, which is the operand.
					PC++;
					// We want to first read the operand value from memory, so send the string "read" to memory.
					writer.printf("read\n");
					// Flush writer.
					writer.flush();
					// Send the PC to main memory to retrieve the operand at the address, which will be
					// the address we want to jump to.
					writer.printf(PC + "\n"); 
					// Flush writer.
					writer.flush();
					// Main memory will send the operand based on the current value of the PC.
					operand = Integer.parseInt(memory_input_reader.nextLine());
					// If the AC = 0, jump to the address.
					if (AC == 0) {
						// Set PC to the address.
						PC = operand;
					} else {
						// Increment PC since we are done with this instruction.
						PC++;
					}
					break;
				
				case 22: // Instruction: Jump to the address in the next line only if the value in the AC is not zero
					// Increment the PC to get the next line after the instruction, which is the operand.
					PC++;
					// We want to first read the operand value from memory, so send the string "read" to memory.
					writer.printf("read\n");
					// Flush writer.
					writer.flush();
					// Send the PC to main memory to retrieve the operand at the address, which will be
					// the address we want to jump to.
					writer.printf(PC + "\n"); 
					// Flush writer.
					writer.flush();
					// Main memory will send the operand based on the current value of the PC.
					operand = Integer.parseInt(memory_input_reader.nextLine());
					// If the AC doesn't equal 0, jump to the address.
					if (AC != 0) {
						// Set PC to the address.
						PC = operand;
					} else {
						// Increment PC since we are done with this instruction.
						PC++;
					}
					break;
					
				case 23: // Instruction: Push return address (on next line) onto stack, jump to the address.
					// Increment the PC to get the next line after the instruction, which is the operand.
					PC++;
					// We want to first read the operand value from memory, so send the string "read" to memory.
					writer.printf("read\n");
					// Flush writer.
					writer.flush();
					// Send the PC to main memory to retrieve the operand at the address, which will be
					// the address we want to jump to.
					writer.printf(PC + "\n"); 
					// Flush writer.
					writer.flush();
					// Main memory will send the operand based on the current value of the PC.
					operand = Integer.parseInt(memory_input_reader.nextLine());
					// If we are in user mode, and the given address is outside of
					// the user program bounds, print an error message.
					if (user_mode && operand > 999) {
						// Send string "exit" to main memory to stop its process.
						writer.printf("exit\n"); 
						// Flush writer.
						writer.flush();
						// Wait for main memory to finish.
						main_memory_process.waitFor();
						// Print an error message.
						System.out.println("Error: Address out of user bounds"); 
						return;
					} else {
						// If the given address is in user bounds...
						// To push a value to the stack, decrement the SP value first.
						SP--;
						// We want to write the operand to memory, so we send the string "write" to main memory. 
						writer.printf("write\n");
						// Flush writer.
						writer.flush();
						// Send the address we want to write to in memory, which is the SP value.
						writer.printf(SP + "\n");
						// Flush writer.
						writer.flush();
						// Send the instruction after the operand to be stored on
						// the stack for the return address.
						PC = PC + 1;
						writer.printf(PC + "\n");
						// Flush writer.
						writer.flush();
						// Set the PC equal to the operand.
						PC = operand;
						break;
					}
					
				case 24: // Instruction: Pop return address from the stack, jump to the address
					// 	When we push a value to the stack, we decrement the SP value first and then push
					// it. This means that the current value of SP will point to the value that was last pushed. 
					// We want to first read the value from memory, so send the string "read" to memory.
					writer.printf("read\n");
					// Flush writer.
					writer.flush();
					// Send the SP to main memory to read the return address at the SP address, this will be
					// the address we want to jump to.
					writer.printf(SP + "\n"); 
					// Flush writer.
					writer.flush();
					// Main memory will send the operand based on the current value of the SP.
					operand = Integer.parseInt(memory_input_reader.nextLine());
					// We need to write 0 to the SP address since we are popping off the stack.
					writer.printf("write\n");
					// Flush the writer.
					writer.flush();
					// Send the address we want to write to in memory, which is the SP value.
					writer.printf(SP + "\n");
					// Flush writer.
					writer.flush();
					// Send the value 0 to main memory to store at the SP address (no longer contains previous value). 
					writer.printf(0 + "\n");
					// Flush writer.
					writer.flush();
					// We want to jump to the return address, so update PC to hold the operand (address that was
					// just pushed to the stack).
					PC = operand;
					// Increment the SP as an item was just popped off the stack.
					SP++;
					// We want to start execution at the new PC. Does continue work here?
					break;
					
				case 25: // Instruction: Increment the value in X
					// Increment the value of X by 1.
					X++;
					// Increment the PC to be ready to fetch the next instruction.
					 PC++;
					break;
				
				case 26: // Instruction: Decrement the value in X
					// Decrement the value of X by 1.
					X--;
					// Increment the PC to be ready to fetch the next instruction.
					PC++;
					break;
					
				case 27: // Instruction: Push AC onto stack
					// To push a value to the stack, decrement the SP value first.
					SP--;
					// We want to write the AC to memory, so we send the string "write" to main memory. 
					writer.printf("write\n");
					// Flush writer.
					writer.flush();
					// Send the address we want to write to in memory, which is the SP value.
					writer.printf(SP + "\n");
					// Flush writer.
					writer.flush();
					// Send the AC value to main memory to write at the SP address.
					writer.printf(AC + "\n");
					PC++;
					break;
				
				case 28: // Instruction: Pop from stack into AC
					// We want to read the last pushed value from the stack, so
					// send the string "read" to memory. 
					writer.printf("read\n");
					// Flush writer.
					writer.flush();
					// Send the SP to main memory to read the value at the SP address, this will be
					// the value we want to store into AC.
					writer.printf(SP + "\n"); 
					// Flush writer.
					writer.flush();
					// Main memory will send the value based on the current value of the SP, store into
					// the value variable.
					value = Integer.parseInt(memory_input_reader.nextLine());
					// We need to write 0 to the SP address since we are popping off the stack.
					writer.printf("write\n");
					// Flush the writer.
					writer.flush();
					// Send the address we want to write to in memory, which is the SP value.
					writer.printf(SP + "\n");
					// Flush writer.
					writer.flush();
					// Send the value 0 to main memory to store at the SP address (no longer contains previous value). 
					writer.printf(0 + "\n");
					// Flush writer.
					writer.flush();
					// Increment the SP since we just pushed from the stack.
					SP++;
					// Store the last pushed value (now popped off) on the stack into the AC.
					AC = value;
					PC++;
					break;
					
				case 29: // Instruction: Perform system call
					// The int instruction should cause execution at address 1500.
					// Order of pushing PC and user SP: user_SP is pushed first, and then the PC, 
					// so PC will be on top.
					// Since we are now performing a system call, set kernel_mode
					// to true, set user_mode to false, and set system_call to true.
					// System.out.println("Entering a sys call...");
					kernel_mode = true;
					user_mode = false;
					system_call = true;
					// Save the user SP to a temporary variable, since SP will now be
					// initialized to the system stack value.
					user_SP = SP;
					// Initialize the SP to 2000 for it to now point to the system stack.
					SP = 2000;
					
					// Save the PC and user SP value onto the system stack.
					// To push a value to the stack, decrement the system SP value first.
					SP--;
					// We want to push the user SP to the stack, so we send the string "write" to main memory. 
					writer.printf("write\n");
					// Flush writer.
					writer.flush();
					// Send the address we want to write to in memory, which is the system SP value.
					writer.printf(SP + "\n");
					// Flush writer.
					writer.flush();
					// Send the user_SP value to main memory to write at the system SP address.
					writer.printf(user_SP + "\n");
					
					// Decrement SP again since we want to push another value to the system stack.
					SP--;
					// We want to push the PC to the system stack, so we sent the string "write" main memory. 
					writer.printf("write\n");
					// Flush writer.
					writer.flush();
					// Send the address we want to write to in memory, which is the system SP value.
					writer.printf(SP + "\n");
					// Flush writer.
					writer.flush();
					// Send the PC value to main memory to write at the system SP address.
					writer.printf((PC+1) + "\n");

					// Set the PC equal to 1500 to begin execution at that address.
					PC = 1500;
					break;
					
				case 30: // Instruction: Return from system call
					// To return from a system call, we would pop the PC and the user SP off the
					// system stack and restore those values. We would also set user mode to true and
					// kernel mode to false.
					// System.out.println("Returning from sys call...");
					user_mode = true;
					kernel_mode = false;
					// Set system_call to false since we are returning from it. 
					if (system_call) {
						// System.out.println("System call ending...");
						system_call = false;
					} else if (timer_interrupt){
						// System.out.println("Interrupt ending...");
						timer_interrupt = false;
					}
					// We want to read the last pushed value from the stack, so
					// send the string "read" to memory. 
					writer.printf("read\n");
					// Flush writer.
					writer.flush();
					// Send the SP to main memory to read the value at the SP address, this will be
					// the value we want to store into the PC to restore its original value
					// before the system call.
					writer.printf(SP + "\n"); 
					// Flush writer.
					writer.flush();
					// Main memory will send the value based on the current value of the SP, store into
					// the value variable.
					value = Integer.parseInt(memory_input_reader.nextLine());
					// System.out.println("PC value: " + value);
					
					// We need to write 0 to the SP address since we are popping off the stack.
					writer.printf("write\n");
					// Flush the writer.
					writer.flush();
					// Send the address we want to write to in memory, which is the SP value.
					writer.printf(SP + "\n");
					// Flush writer.
					writer.flush();
					// Send the value 0 to main memory to store at the SP address (no longer contains previous value). 
					writer.printf(0 + "\n");
					// Flush writer.
					writer.flush();
						
					// Store the value popped off the stack to the PC. 
					PC = value;
					// System.out.println("PC we are returning to: " + PC);
					// Increment the SP since an item was just popped off the stack.
					SP++;
					
					// We want to again read the last pushed value from the stack, so
					// send the string "read" to memory. 
					writer.printf("read\n");
					// Flush writer.
					writer.flush();
					// Send the SP to main memory to read the value at the SP address, this will be
					// the value we want to store into the user_SP to restore the SP original value
					// before the system call.
					writer.printf(SP + "\n"); 
					// Flush writer.
					writer.flush();
					// Main memory will send the value based on the current value of the SP, store into
					// the value variable. Value now holds the user SP before the system call.
					value = Integer.parseInt(memory_input_reader.nextLine());
					
					// We want to pop the user_SP off the system stack, so
					// we need to write 0 to the SP address since we are popping off the stack.
					writer.printf("write\n");
					// Flush the writer.
					writer.flush();
					// Send the address we want to write to in memory, which is the SP value.
					writer.printf(SP + "\n");
					// Flush writer.
					writer.flush();
					// Send the value 0 to main memory to store at the SP address (no longer contains previous value). 
					writer.printf(0 + "\n");
					// Flush writer.
					writer.flush();
					// Increment SP since we just popped an item off the system stack.
					SP++;
					// Store value into the SP variable. SP will now point to a value in the user stack.
					SP = value;
					break;
					

				case 50: // Instruction: end execution
					// Send string "exit" to main memory to stop its process.
					writer.printf("exit\n"); 
					// Flush writer.
					writer.flush();
					// Wait for main memory to finish.
					main_memory_process.waitFor();
					// Print an exit message.
					System.out.println("\nProgram ending..."); 
					break;
					
				default: 
					// Move onto the next instruction.
					break;
					
				} // end switch
				
				// Increment the instruction count since we just finished executing the current instruction.
				instruction_count++;
				
				// Check if it is time for a timer interrupt and check if a system call or another
				// interrupt is not already occurring.
				if (instruction_count >= timer_interrupt_value && !system_call & !timer_interrupt) {
					// System.out.println("Interrupt taking place");
					// System.out.println("\nPC before interrupt took place: " + PC);
					// If it is time for a timer interrupt AND a system call isn't occurring, 
					// set timer_interrupt = true. 
					timer_interrupt = true;
					// Set kernel_mode to true and user_mode to false since we're processing
					// an interrupt.
					kernel_mode = true;
					user_mode = false;
					// Save the PC and user SP to the system stack.
					// Save the user SP to a temporary variable, since SP will now be
					// initialized to the system stack value.
					user_SP = SP;
					// System.out.println("User SP: " + user_SP);
					// Initialize the SP to 2000 for it to now point to the system stack.
					SP = 2000;
					// Save the PC and user SP value onto the system stack.
					// To push a value to the stack, decrement the system SP value first.
					SP--;
					// We want to push the user SP to the stack, so we send the string "write" to main memory. 
					writer.printf("write\n");
					// Flush writer.
					writer.flush();
					// Send the address we want to write to in memory, which is the system SP value.
					writer.printf(SP + "\n");
					// Flush writer.
					writer.flush();
					// Send the user_SP value to main memory to write at the system SP address.
					writer.printf(user_SP + "\n");
					
					// Decrement SP again since we want to push another value to the system stack.
					SP--;
					// We want to push the PC to the system stack, so we sent the string "write" main memory. 
					writer.printf("write\n");
					// Flush writer.
					writer.flush();
					// Send the address we want to write to in memory, which is the system SP value.
					writer.printf(SP + "\n");
					// Flush writer.
					writer.flush();
				
					// Send the PC value to main memory to write at the system SP address.
					// We sent PC+1 so we can resume at the line after when the interrupt
					// was made. 
					writer.printf(PC + "\n");
					// System.out.println("PC to return to: " + PC);
					// Send the string "done" to main memory so it knows that we are done with this instruction.
					writer.printf("done\n");
					// Flush writer.
					writer.flush();
					// SP now equals 2000 - 2 (because PC and user SP were just pushed on).
					
					// Timer interrupt causes execution at address 1000, so PC = 1000. 
					PC = 1000;
					// Reset instruction_count.
					instruction_count = instruction_count - timer_interrupt_value + 1;
					// Now continue to the next instruction cycle.  
				} else {
					// Send the string "done" to main memory so it knows that we are done with this instruction.
					writer.printf("done\n");
				   // Flush writer.
					writer.flush();
				}
				
			} // end while
				
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
