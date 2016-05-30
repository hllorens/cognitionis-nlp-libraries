#!/bin/python

import csv
import numpy as np
from scipy import stats

data_filename = 'ex1data1.txt'
data_reader = csv.reader(open(data_filename, 'rb'))
DATA = np.array(list(data_reader), dtype='float64')
y = DATA[:,1]
m = y.size
X_raw = DATA[:,0]

slope, intercept, r_value, p_value, std_err = stats.linregress(X_raw, y)

print("slope: \t" + str(slope))           # corresponds to theta_1
print("intercept: \t" + str(intercept)) # corresponds to theta_0
