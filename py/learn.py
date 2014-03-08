#!/usr/local/bin/python
import serial
import numpy as np
import re
from sklearn import metrics
from sklearn.cluster import AffinityPropagation


def main():


    # Set up serial connection
    ser = serial.Serial('/dev/tty.usbmodem641')
    print ser.name
    ser.write('a')

    # skip shit data
    ser.readline()
    X = []

    count = 0

    while count < 1000:
        input_data = ser.readline()
        parsed_data = re.split(r'\t+', input_data.strip('\r\n'))
        outdata = map(float, parsed_data[1:])
        print outdata
        X.append(outdata)
        count+=1
        
    ser.close()

    X = np.array(X)    

    # compute affinity propagation
    af = AffinityPropagation(damping=.7, convergence_iter=50, max_iter=1000, verbose=True).fit(X)
    cluster_centers_indices = af.cluster_centers_indices_
    labels = af.labels_

    n_clusters_ = len(cluster_centers_indices)

    print labels
    print cluster_centers_indices

    print('Estimated number of clusters: %d' % n_clusters_)
    #print("Homogeneity: %0.3f" % metrics.homogeneity_score(labels_true, labels))
    #print("Completeness: %0.3f" % metrics.completeness_score(labels_true, labels))
    #print("V-measure: %0.3f" % metrics.v_measure_score(labels_true, labels))
    #print("Adjusted Rand Index: %0.3f"
          #% metrics.adjusted_rand_score(labels_true, labels))
    #print("Adjusted Mutual Information: %0.3f"
          #% metrics.adjusted_mutual_info_score(labels_true, labels))
    print("Silhouette Coefficient: %0.3f"
          % metrics.silhouette_score(X, labels, metric='sqeuclidean'))

if __name__ == "__main__":
    main()
