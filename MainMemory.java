/* Mansi Patel
If using Java, you must use the Runtime exec method to create processes and streams for communication.  
Your project will receive no credit if not using processes or if using threads instead of processes.
All code must run successfully on our cs1.utdallas.edu server or csgrads1.utdallas.edu server.
 * 
 * in memory, system out translates to CPU input stream
 * in memory, scanner in translates to CPU output stream
 */
import java.io.*;
import java.util.Scanner;

public class MainMemory {
	
	public static void main(String[] args) {

		// Create an integer array that has 2000 entries.
		// Indexes 0-999 for the user program, 1000-1999 for system code.
		int[] main_memory_arr = new int[2000];
		
		// If the argument number on the command line is not 1, then return an error message and
		// end the program.
		if (args.length != 1) {
			System.err.println("One argument must be provided: the input file name\n");
			System.exit(-2);
		}
		
		// Create an index variable and a variable to hold the instruction line
		// read in from the file. 
		int current_index = 0;
		String instruction = "";

		// This portion should fill the main_memory_arr with values according to
		// the input file. 
		try {
			// Create a File object using the input file name argument from the command line.
			File input_file = new File(args[0]);
			// Create a Scanner object to read in contents of the input file.
			Scanner input_reader = new Scanner(input_file);
		
			// Read through the input file.
			while (input_reader.hasNextLine()) {
				// Store the current line into a String variable.
				String current_line = input_reader.nextLine();
				
				// Split the current line by spaces and store into a String array.
				// If the line is blank, then instruction should contain "".
				// Example: "5 //...", then instruction should contain 5.
				String inputArr[] = current_line.split("[ ]");
				
				// Store the first index of the input array into
				// the instruction variable: this portion will contain an instruction or an operand.
				instruction = inputArr[0];
				
				// If the instruction is empty (because the line was blank), then
				// skip the instruction but don't advance the loading address.
				if (instruction.equals("")) {
					continue;
				// If the instruction has a period as the first character, this indicates
				// a change in load address. Example: ".1000"
				} else if (instruction.charAt(0) == '.') {
					// Address will contain an integer value of the address to jump to.
					// The substring portion will get the integer value that comes after the
					// period. 
					// Example: ".1000", so address should contain 1000.
					int curr_address = Integer.parseInt(instruction.substring(1,instruction.length()));
					// Change current index to be the given address, begin loading instructions/operands 
					// at that value.
					current_index = curr_address;
					continue;
				} else {
					// If the instruction didn't start with a period or it is not
					// a blank line, store the instruction/operand into the array at current_index and
					// advance current_index.
					main_memory_arr[current_index] = Integer.parseInt(instruction);
					current_index++;
				}
			}
			
		} catch (Exception e) {
			// Catch a FileNotFoundException if a valid file name is not given.
			System.err.println("Error with input file.\n");
			System.exit(-2);
		}
		
		// Once the main_memory_arr has been set, we can begin to process instructions with the CPU.
		
		// Create a Scanner object to read in commands from the CPU.
		Scanner CPU_input_reader = new Scanner(System.in);

		// Create a variable to store the current address value. Address will have
		// the starting PC because we wrote to the output stream in the CPU
		// before beginning the instruction cycle.
		int address = 0;

		// Create a variable to hold data that the CPU wants to write to memory.
		int data = 0;
		
		// Create a variable to hold commands from the CPU.
		String read_or_write = "";
		
		// Create a loop for the main memory to continuing iterating while the CPU
		// is still fetching instructions or information from memory.
		// Inside the loop: use scanner in to read in CPU requests (memory will either read
		// or write based off of what the CPU wants and the address given)
		while(true) {
			
			// Initialize a variable that will read in the function the CPU wants Main Memory
			// to do: either read or write.
			read_or_write = CPU_input_reader.nextLine();
			// Continue to iterate to receive input from the CPU while it is 
			// performing an instruction. For a given instruction, the CPU
			// may need to read from memory multiple times to receive needed information.
			while (!read_or_write.equals("done")) {
				// If the command from the CPU equals read..
				if (read_or_write.equals("read")) {
					// Read from main memory array using the address sent by the CPU.
					address = Integer.parseInt(CPU_input_reader.nextLine());
					// Send the value at the PC address in the main memory array.
					System.out.println(main_memory_arr[address]);
					// Read in the next command from the CPU.
					read_or_write = CPU_input_reader.nextLine();
				// If the command from the CPU equals write...
				} else if (read_or_write.equals("write")) {
					// Write to main memory using the PC as the address we want to write to.
					// The CPU sends over the address we want to write to.
					address = Integer.parseInt(CPU_input_reader.nextLine());
					// The CPU then sends over what we would like to write.
					data = Integer.parseInt(CPU_input_reader.nextLine());
					// Store the given data at the given address in the main memory array.
					main_memory_arr[address] = data;
					// Read in the next command from the CPU.
					read_or_write = CPU_input_reader.nextLine();
				// If the command from the CPU equals exit...
				} else if (read_or_write.equals("exit")) {
				// If the CPU is done processing all instructions, it will
				// send an exit message to Main Memory.
				// Main Memory then breaks out of the nested loop.
					return;
				} // end inner if
			} // end inner while
			
			// This sends a print statement to the loop in the CPU class,
			// which helps the while loop move forward to the next instruction.
			System.out.println(main_memory_arr[address]);
			
		} // end outer while

	} // end main
	
}
