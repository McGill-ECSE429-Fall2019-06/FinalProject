"""
program used for mutation testing
"""
import sys
def fn(a,b):
    if a > 1:
        b = a + 4
    return a - b * 2

a = int(sys.argv[1])
b = int(sys.argv[2])
print(fn(a,b))

