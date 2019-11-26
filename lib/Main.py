import MutantListGenerator
import MutantInjection
import ParallelSimulator

if __name__ == "__main__":
    MutantListGenerator.generate()
    MutantInjection.injection()
    ParallelSimulator.parallel_simulation()
