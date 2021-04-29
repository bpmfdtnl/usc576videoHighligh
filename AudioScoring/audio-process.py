import sys
import librosa
import matplotlib.pyplot as plt
import matplotlib
import librosa.display
import numpy as np
import datetime
from sklearn.svm import OneClassSVM


def audio_process(audio_file, shots_file, output_file):
    # import Audio File (.wav file) 
    # Sample Rate = 16200 = 30 Hz 
    audio_path = audio_file
    x, sr = librosa.load(audio_path, sr=16200)
    _x = x[:]

    # Generate MFCCs Features by taking the first 13 coefficients
    mfccs = librosa.feature.mfcc(_x, sr=sr, n_mfcc = 13)
    # Taking the 1st derivatie of MFCCs
    delta_mfccs = librosa.feature.delta(librosa.feature.mfcc(_x, sr=sr, n_mfcc = 13))
    # Feature Concatenation
    comprehensive_mfccs = np.concatenate((mfccs, delta_mfccs))


    # Graph Showing 
    # mfcc_img = plt.figure(figsize=(100, 10))
    # librosa.display.specshow(mfccs, sr=sr, x_axis='time')
    # plt.colorbar()
    # mfcc_img.show()

    # Transpose of Comprehensive mfccs 
    X = np.transpose(comprehensive_mfccs)

    # outlier detection model by using One Class SVM with RBF Kernel
    clf = OneClassSVM(kernel="rbf",gamma='scale', nu=0.1).fit(X)
    result = clf.predict(X)

    # Import Shot File (.txt file) 
    file_path = shots_file

    shots_list = []
    with open(file_path) as fp:
        line = fp.readline()
        cnt = 1
        while line:
            line_text = line.strip().split()
            shots_list.append(int(line_text[0])/16200)
            line = fp.readline()
    shots_list.append(1)
    shots_np_array = np.array(shots_list)
    audio_shots = np.rint(shots_np_array * len(result))

    scores = []
    for i in range(len(audio_shots)-1):
        start = audio_shots[i]
        end = audio_shots[i+1]
        shot_result = result[int(start): int(end+1)]
        scores.append(str(np.count_nonzero(shot_result == -1)/len(shot_result))+"\n")


    F = open(output_file, "a")
    F.truncate(0)
    F.writelines(scores)
    F.close()

if __name__ == "__main__":
    args = sys.argv[1:]
    audio_file = args[0]
    shots_file = args[1]
    output_file = args[2]
    audio_process(audio_file, shots_file, output_file)
