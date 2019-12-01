"""
This program creates mutants of the 'SUT.py' from the mutants fault list file (library_of_mutants.txt)
and creates a new directory 'mutants_file/' to store the mutant files
"""
import csv
import os

mutants_file_path = "library_of_mutants.txt"
mutant_dict = dict({"+":'plus', "-":'minus', "*":'multiply', "/":"divide"})

def injection(source_path):
    # open mutants fault list file
    try:
        mutants_file = open(mutants_file_path,'r')
        mutants_reader = csv.reader(mutants_file, delimiter=',')
    except FileNotFoundError as e:
        print("Error processing mutants file.")
        sys.exit(1)

    # skip the header
    next(mutants_reader, None)

    # make directory to store the mutant files
    path = "mutants_file/"
    try:
        os.mkdir(path)
    except OSError:
        print ("Directory %s already exist" % path)

    i = 2
    for row in mutants_reader:
        # skip empty lines
        if not row:
            break
        # process the mutant fault list
        line_number = int(row[0])
        original_operation = row[1]
        mutant = row[2]
        file_name = "{}/{},line{},{}->{}.py".format(path, str(i),str(line_number),
                                                            mutant_dict[original_operation], mutant_dict[mutant])
        i += 1
        # make a copy of original source file
        with open(file_name, "w+") as mutated_file:
            with open(source_path, "r") as original_file:
                line_count = 1
                for line in original_file:
                    # replace the specific line with mutant injected
                    if line_count == line_number:
                        mutated_file.write(line.replace(original_operation, mutant, 1))
                        line_count += 1
                    else:
                        mutated_file.write(line)
                        line_count += 1
