import MutantListGenerator
import MutantInjection
import ParallelSimulator
import time

if __name__ == "__main__":
    # reads file path from input
    file_path = input("Enter the name of the file to perform mutation testing on: ")
    num_threads = int(input("Enter the number of threads for parallel simulation: "))

    # open file
    try:
        file = open(file_path, 'r')
    except FileNotFoundError as e:
        print("Error processing file path.")
        sys.exit(1)


    file_path = 'SUT.py'
    file = open(file_path, 'r')
    print("Generating the mutant list file ...\n")
    MutantListGenerator.generate(file)

    print("Injecting mutants ...\n")
    MutantInjection.injection(file_path)


    print("Simulating mutants in parallel ...\n")
    t0 = time.time_ns()
    ParallelSimulator.parallel_simulation(file_path, num_threads)
    t1 = time.time_ns()
    print("Parallel Execution time (ms): {}".format((t1 - t0)/float(10**6)))
