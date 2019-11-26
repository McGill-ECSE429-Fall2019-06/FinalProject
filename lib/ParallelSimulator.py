from os import listdir
from os.path import isfile, join
from subprocess import run, PIPE
import csv
import numpy as np
import threading


path = "mutants_file/"
test_vectors_file = "TestVectors.txt"
mutants_library_path = "library_of_mutants.txt"
SUT_file = 'SUT.py'
num_threads = 3

# custom sorting function
def cmp_number(f1):
    return int(f1.split(',')[0])


def split(a,n):
    k,m = divmod(a,n)
    return (a[i*k + min(i,m):(i+1) * k + min(i+1,m)] for i in range(n))


def get_lines(file_path):
    with open(file_path, 'r') as file:
        lines = file.read().splitlines();
        return lines


def sort_files():
    # get all mutated files
    mutated_files = [f for f in listdir(path) if isfile(join(path, f))]
    mutated_files = [join(path, f) for f in sorted(mutated_files, key=cmp_number)]
    # key in the dict represent the line number of the mutant in the mutant list
    #return dict(list(enumerate(mutated_files, 2)))
    return mutated_files


def get_test_vector():
    with open(test_vectors_file) as tv_file:
        test_vectors = tv_file.read().splitlines();
        # removing the header
        test_vectors.pop(0)
        return test_vectors


def evaluation(name, tasks, mutated_files, test_vectors, mutant_list):
    print("Thread {}: starting {}".format(name, tasks))
    for i in tasks:
        #print(f + "in thread " + str(name))
        for v in test_vectors:
            arg1, arg2 = v.split(',')

            # running the SUT with test vector
            correct_out = run(['python', SUT_file, arg1, arg2], stdout=PIPE).stdout.decode('utf-8')

            # running the mutant files with test vector
            # spawning a subprocess to execute the mutated files
            mutant_out = run(['python', mutated_files[i], arg1, arg2], stdout=PIPE).stdout.decode('utf-8')

            if correct_out != mutant_out:
                line_number = int(mutated_files[i].split('/')[1].split(',')[0])
                if 'Y' in mutant_list[line_number-1]:
                    mutant_list[line_number-1] = mutant_list[line_number-1] + "(" + arg1 + "," + arg2 + ")"
                else:
                    mutant_list[line_number-1] = mutant_list[line_number-1] + ',Y,' + "(" + arg1 + "," + arg2 + ")"


def write_back(lines):
    # writing back the result of simulation to mutant fault list
    with open(mutants_library_path, 'w') as file:
        killed = 0
        total_mutants = 0
        i = 1;
        file.write(lines[0] + '\n')
        while i < len(lines):
            if not lines[i]:
                break
            elif 'Y' not in lines[i]:
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

        print("\nMutant Coverage: {0:.2%}".format(killed / total_mutants))


def parallel_simulation():
    # splitting the mutated files among number of threads
    mutated_files = sort_files()
    # tasks splits the indexes in the mutated_files
    tasks = np.array_split(range(len(mutated_files)),num_threads)

    # get test vectors
    test_vectors = get_test_vector()

    # get each line of the mutant list
    mutant_list = get_lines(mutants_library_path)

    # each thread is assigned to some mutated files for simulation
    threads = list()
    for index in range(num_threads):
        print("Main    : create and start thread {}.".format(index))
        t = threading.Thread(target=evaluation, args=(index, tasks[index],
                                                      mutated_files, test_vectors, mutant_list))
        threads.append(t)
        t.start()

    for t in threads:
        t.join()

    write_back(mutant_list)










