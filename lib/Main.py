import MutantListGenerator
import MutantInjection
import ParallelSimulator

if __name__ == "__main__":
    # reads file path from input
    file_path = input("Enter the name of the file to perform mutation testing on: \n")

    # open file
    try:
        file = open(file_path, 'r')
    except FileNotFoundError as e:
        print("Error processing file path.")
        sys.exit(1)


    print("Generating the mutant list file ...\n")
    MutantListGenerator.generate(file)

    print("Injecting mutants ...\n")
    MutantInjection.injection(file_path)

    print("Simulating mutants in parallel ...\n")
    ParallelSimulator.parallel_simulation(file_path)
