"""
This mutant library generator program traverses a program given by the input
and generates arithmetic operation mutants and store them in the 'library_of_mutants.txt' file
"""
import sys

def generate(file):
    # define mutant dict
    # keys = types of mutants
    # values = total number of each mutant
    MUTANTS = {}.fromkeys(['+', '-', '*', '/'], 0)

    # redirect output to file that stores the library of mutants
    sys.stdout = open('library_of_mutants.txt', 'w')

    # reads program code line by line
    print('line number, original operation, mutant inserted, killed, test vector')
    line_number = 0
    for line in file:
        line_number += 1
        for char in line:
            if char in MUTANTS.keys():
                for mutant in MUTANTS.keys():
                    if mutant != char:
                        print(str(line_number) + "," + char + ',' + mutant)
                        MUTANTS[mutant] += 1

    try:
        file.close()
    except Exception:
        pass

    # summary of mutants generated
    print('\nTotal number of mutants of each type generated: ')
    print("\n".join("{}\t{}".format(k, v) for k, v in MUTANTS.items()))
    sys.stdout = sys.__stdout__

