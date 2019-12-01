from os import listdir
from os.path import isfile, join
from subprocess import run, PIPE
import csv

path = "mutants_file/"
test_vectors_file = "TestVectors.txt"
mutants_library_path = "library_of_mutants.txt"
SUT_file = 'SUT.py'

# custom sorting function
def cmp_number(f1):
    return int(f1.split(',')[0])

# get all mutated files
mutated_files = [f for f in listdir(path) if isfile(join(path, f))]
mutated_files = [join(path,f) for f in sorted(mutated_files, key=cmp_number)]


with open(mutants_library_path, 'r') as file:
    with open(test_vectors_file) as tv_file:
        test_vectors = tv_file.read().splitlines();
        # removing the header
        test_vectors.pop(0)
        lines = file.read().splitlines();
        i = 1

        for f in mutated_files:
            print(f)
            # skip empty lines
            if not lines[i]:
               break
            print('current line ' + lines[i])

            for vector in test_vectors:
                arg1, arg2 = vector.split(',')


                # running the SUT with test vector
                correct_out = run(['python', SUT_file, arg1, arg2], stdout=PIPE).stdout.decode('utf-8')

                # running the mutant files with test vector
                # spawning a subprocess to execute the mutated files
                mutant_out = run(['python', f, arg1, arg2], stdout=PIPE).stdout.decode('utf-8')


                if correct_out != mutant_out:
                    if 'Y' in lines[i]:
                        lines[i] = lines[i] + "(" + arg1 + "," + arg2 + ")"
                    else:
                        lines[i] = lines[i] + ',Y,' + "(" + arg1 + "," + arg2 + ")"

            i += 1


# writing back the result of simulation to mutant fault list
with open(mutants_library_path, 'w') as file:
    killed = 0
    total_mutants = 0
    i = 0;
    while i < len(lines):
        if not lines[i]:
            break
        elif 'Y' not in lines[i] and i != 0:
            lines[i] = lines[i] + ',N'
        else:
            killed += 1

        if any(c.isdigit() for c in lines[i]):
            total_mutants += 1

        file.write(lines[i] + '\n')
        i += 1

    while i < len(lines):
        file.write(lines[i] + '\n')
        i += 1


tv_file.close()

print("Mutant Coverage: {0:.2%}".format(killed / total_mutants))
